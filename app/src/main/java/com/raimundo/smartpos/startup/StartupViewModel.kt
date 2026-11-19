package com.raimundo.smartpos.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.BuildConfig
import com.raimundo.smartpos.core.domain.usecase.TerminalValidation
import com.raimundo.smartpos.core.domain.usecase.ValidateTerminalUseCase
import com.raimundo.smartpos.core.security.Aad
import com.raimundo.smartpos.core.security.PayloadCipher
import com.raimundo.smartpos.feature.blocking.BlockingReason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StartupState {
    data object Loading : StartupState

    data object Ready : StartupState

    data class Blocked(
        val reason: BlockingReason,
    ) : StartupState
}

/**
 * Cenários 22/23: valida merchantId/terminalId e a chave de criptografia ANTES de liberar
 * qualquer tela de venda. Nunca deixa uma configuração inválida chegar a um crash.
 */
@HiltViewModel
class StartupViewModel
    @Inject
    constructor(
        private val validateTerminal: ValidateTerminalUseCase,
        private val payloadCipher: PayloadCipher,
    ) : ViewModel() {
        private val _state = MutableStateFlow<StartupState>(StartupState.Loading)
        val state: StateFlow<StartupState> = _state.asStateFlow()

        init {
            check()
        }

        fun retry() {
            _state.value = StartupState.Loading
            check()
        }

        private fun check() {
            viewModelScope.launch {
                val terminal = validateTerminal(BuildConfig.MERCHANT_ID, BuildConfig.TERMINAL_ID)
                if (terminal is TerminalValidation.Invalid) {
                    _state.value = StartupState.Blocked(BlockingReason.TERMINAL_NOT_CONFIGURED)
                    return@launch
                }

                val keyWorks =
                    runCatching {
                        val aad = Aad.of("GET", "/startup-check", "startup")
                        val envelope = payloadCipher.encrypt("ok".encodeToByteArray(), aad)
                        payloadCipher.decrypt(envelope, aad)
                    }.isSuccess

                _state.value =
                    if (keyWorks) {
                        StartupState.Ready
                    } else {
                        StartupState.Blocked(BlockingReason.INVALID_SECURITY_KEY)
                    }
            }
        }
    }
