package com.raimundo.smartpos.startup

import app.cash.turbine.test
import com.raimundo.smartpos.core.domain.usecase.ValidateTerminalUseCase
import com.raimundo.smartpos.core.security.AesGcmPayloadCipher
import com.raimundo.smartpos.core.security.EncryptedEnvelope
import com.raimundo.smartpos.core.security.PayloadCipher
import com.raimundo.smartpos.core.security.StaticKeyProvider
import com.raimundo.smartpos.core.testing.MainDispatcherRule
import com.raimundo.smartpos.feature.blocking.BlockingReason
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.security.GeneralSecurityException

/** Cenários 22 e 23: terminal mal configurado e chave inválida bloqueiam sem crashar o app. */
class StartupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workingKey = ByteArray(32) { it.toByte() }
    private val workingCipher: PayloadCipher = AesGcmPayloadCipher(StaticKeyProvider("dev-key-01", workingKey))

    private object BrokenCipher : PayloadCipher {
        override fun encrypt(
            plaintext: ByteArray,
            aad: ByteArray,
        ): EncryptedEnvelope = throw GeneralSecurityException("chave de criptografia indisponível")

        override fun decrypt(
            envelope: EncryptedEnvelope,
            aad: ByteArray,
        ): ByteArray = throw GeneralSecurityException("chave de criptografia indisponível")
    }

    private val validIds = RawTerminalIds(merchantId = "123456789012345", terminalId = "12345678")

    @Test
    fun `terminal e chave validos liberam a tela de venda`() =
        runTest {
            val viewModel =
                StartupViewModel(
                    validateTerminal = ValidateTerminalUseCase(),
                    payloadCipher = workingCipher,
                    rawTerminalIds = validIds,
                )

            viewModel.state.test {
                assertEquals(StartupState.Ready, awaitItem())
            }
        }

    @Test
    fun `merchantId invalido bloqueia com TERMINAL_NOT_CONFIGURED`() =
        runTest {
            val viewModel =
                StartupViewModel(
                    validateTerminal = ValidateTerminalUseCase(),
                    payloadCipher = workingCipher,
                    rawTerminalIds = RawTerminalIds(merchantId = "123", terminalId = "12345678"),
                )

            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is StartupState.Blocked)
                assertEquals(BlockingReason.TERMINAL_NOT_CONFIGURED, (state as StartupState.Blocked).reason)
            }
        }

    @Test
    fun `chave de criptografia que falha bloqueia com INVALID_SECURITY_KEY`() =
        runTest {
            val viewModel =
                StartupViewModel(
                    validateTerminal = ValidateTerminalUseCase(),
                    payloadCipher = BrokenCipher,
                    rawTerminalIds = validIds,
                )

            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is StartupState.Blocked)
                assertEquals(BlockingReason.INVALID_SECURITY_KEY, (state as StartupState.Blocked).reason)
            }
        }

    @Test
    fun `retry refaz a verificacao apos corrigir a chave`() =
        runTest {
            var cipher: PayloadCipher = BrokenCipher
            val viewModel =
                StartupViewModel(
                    validateTerminal = ValidateTerminalUseCase(),
                    payloadCipher = DelegatingCipher { cipher },
                    rawTerminalIds = validIds,
                )

            viewModel.state.test {
                assertTrue(awaitItem() is StartupState.Blocked)

                cipher = workingCipher
                viewModel.retry()

                assertEquals(StartupState.Loading, awaitItem())
                assertEquals(StartupState.Ready, awaitItem())
            }
        }

    private class DelegatingCipher(
        private val delegate: () -> PayloadCipher,
    ) : PayloadCipher {
        override fun encrypt(
            plaintext: ByteArray,
            aad: ByteArray,
        ): EncryptedEnvelope = delegate().encrypt(plaintext, aad)

        override fun decrypt(
            envelope: EncryptedEnvelope,
            aad: ByteArray,
        ): ByteArray = delegate().decrypt(envelope, aad)
    }
}
