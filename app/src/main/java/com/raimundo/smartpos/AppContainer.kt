package com.raimundo.smartpos

import android.content.Context
import android.util.Log
import com.raimundo.smartpos.data.crypto.AesGcmPayloadCipher
import com.raimundo.smartpos.data.crypto.StaticKeyProvider
import com.raimundo.smartpos.data.crypto.hexToBytes
import com.raimundo.smartpos.data.journal.SharedPreferencesTransactionJournal
import com.raimundo.smartpos.data.mock.MockBackend
import com.raimundo.smartpos.data.mock.MockBackendInterceptor
import com.raimundo.smartpos.data.remote.HttpPaymentGateway
import com.raimundo.smartpos.domain.TransactionManager
import com.raimundo.smartpos.domain.TransactionProcessor
import com.raimundo.smartpos.domain.model.TerminalConfig
import com.raimundo.smartpos.printer.PrintQueue
import com.raimundo.smartpos.printer.ReceiptFormatter
import com.raimundo.smartpos.printer.SimulatedReceiptPrinter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Dispatcher
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Injeção de dependência manual: o grafo é pequeno e assim o projeto fica sem
 * geração de código (Hilt/KSP) – compila mais rápido e é fácil de ler no teste.
 */
class AppContainer(context: Context) {

    /** Escopo da aplicação: transações não morrem quando a tela fecha. */
    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, e ->
            Log.e("SmartPOS", "Erro não tratado em coroutine", e)
        },
    )

    val terminal = TerminalConfig(
        merchantId = BuildConfig.MERCHANT_ID,
        terminalId = BuildConfig.TERMINAL_ID,
    )

    private val cipher = AesGcmPayloadCipher(
        StaticKeyProvider(BuildConfig.PAYLOAD_KEY_ID, BuildConfig.DEV_PAYLOAD_KEY_HEX.hexToBytes()),
    )

    val mockBackend = MockBackend()

    private val httpClient = OkHttpClient.Builder()
        // Várias transações simultâneas fazem polling no mesmo host (padrão do OkHttp = 5).
        .dispatcher(Dispatcher().apply { maxRequestsPerHost = 32 })
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.USE_MOCK_BACKEND) addInterceptor(MockBackendInterceptor(mockBackend, cipher))
        }
        .build()

    private val gateway = HttpPaymentGateway(httpClient, BuildConfig.API_BASE_URL.toHttpUrl(), cipher)

    private val journal = SharedPreferencesTransactionJournal(context.applicationContext)

    val printQueue = PrintQueue(SimulatedReceiptPrinter(), ReceiptFormatter(), appScope)

    val transactionManager = TransactionManager(
        processor = TransactionProcessor(gateway, journal),
        journal = journal,
        printQueue = printQueue,
        terminal = terminal,
        scope = appScope,
    )
}
