package com.raimundo.smartpos.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.raimundo.smartpos.core.common.SystemClock
import com.raimundo.smartpos.core.data.journal.RoomTransactionJournal
import com.raimundo.smartpos.core.domain.engine.EngineConfig
import com.raimundo.smartpos.core.domain.engine.TransactionEngine
import com.raimundo.smartpos.core.localstorage.db.SmartPosDatabase
import com.raimundo.smartpos.core.localstorage.db.TransactionEntity
import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.model.RemoteStatus
import com.raimundo.smartpos.core.model.TerminalConfig
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.printer.PrintQueue
import com.raimundo.smartpos.core.printer.ReceiptFormatter
import com.raimundo.smartpos.core.printer.ReceiptPrinter
import com.raimundo.smartpos.core.testing.FakeGateway
import com.raimundo.smartpos.core.testing.MERCHANT
import com.raimundo.smartpos.core.testing.TERMINAL
import com.raimundo.smartpos.core.testing.result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private class FakePrinter : ReceiptPrinter {
    val printed = mutableListOf<String>()

    override suspend fun print(receipt: String) {
        printed += receipt
    }
}

@RunWith(AndroidJUnit4::class)
class TransactionCoordinatorRepositoryTest {
    private lateinit var db: SmartPosDatabase
    private lateinit var gateway: FakeGateway
    private lateinit var scope: CoroutineScope
    private lateinit var repository: TransactionCoordinatorRepository

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SmartPosDatabase::class.java)
                .build()
        gateway = FakeGateway()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val journal = RoomTransactionJournal(db.transactionDao(), db.transactionEventDao())
        val engine =
            TransactionEngine(
                gateway,
                journal,
                EngineConfig(initialPollDelayMs = 10, pollIntervalMs = 10),
                SystemClock(),
            )
        val printQueue = PrintQueue(FakePrinter(), ReceiptFormatter(), scope, SystemClock())
        repository =
            TransactionCoordinatorRepository(
                engine = engine,
                journal = journal,
                transactionDao = db.transactionDao(),
                eventDao = db.transactionEventDao(),
                printQueue = printQueue,
                terminal = { TerminalConfig(MERCHANT, TERMINAL) },
                clock = SystemClock(),
                appScope = scope,
            )
    }

    @After
    fun tearDown() {
        scope.cancel()
        db.close()
    }

    @Test
    fun vendasSimultaneasTerminamIndependentemente() {
        runBlocking {
            gateway.onPoll = { tx, attempt -> if (attempt >= 2) tx.result(RemoteStatus.APROVADA) else null }

            repository.startSale("tx-a", 1_000, PaymentType.CREDITO)
            repository.startSale("tx-b", 2_000, PaymentType.DEBITO)
            repository.startSale("tx-c", 3_000, PaymentType.CREDITO)

            withTimeout(5_000) {
                repository.transactions.first { list ->
                    list.size == 3 && list.all { it.status == TransactionStatus.APROVADA }
                }
            }
        }
    }

    @Test
    fun idDuplicadoDoTequeDuploEhIgnorado() {
        runBlocking {
            gateway.onPoll = { tx, _ -> tx.result(RemoteStatus.APROVADA) }

            val first = repository.startSale("tx-dup", 1_000, PaymentType.CREDITO)
            val second = repository.startSale("tx-dup", 1_000, PaymentType.CREDITO)

            assertThat(first).isNotNull()
            assertThat(second).isNull()

            withTimeout(5_000) {
                repository.observe("tx-dup").first { it?.status == TransactionStatus.APROVADA }
            }
            assertThat(gateway.authorizeCalls.count { it.id == "tx-dup" }).isEqualTo(1)
        }
    }

    @Test
    fun recuperacaoAoAbrirDesfazTransacaoOrfa() {
        runBlocking {
            db.transactionDao().upsert(
                TransactionEntity(
                    id = "tx-orfa",
                    amountCents = 500,
                    paymentType = "CREDITO",
                    merchantId = MERCHANT,
                    terminalId = TERMINAL,
                    status = "AGUARDANDO_RESULTADO",
                    createdAtMillis = 0,
                    updatedAtMillis = 0,
                    undoReason = null,
                    receiptPrinted = false,
                ),
            )
            gateway.onUndo = { _, _ -> }

            repository.start()

            withTimeout(5_000) {
                repository.observe("tx-orfa").first { it?.status == TransactionStatus.DESFEITA }
            }
            assertThat(gateway.undoCalls.any { it.id == "tx-orfa" }).isTrue()
        }
    }
}
