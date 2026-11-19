package com.raimundo.smartpos.core.localstorage.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    private lateinit var db: SmartPosDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SmartPosDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.transactionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        id: String = "tx-1",
        status: String = "CRIADA",
        createdAtMillis: Long = 0,
    ) = TransactionEntity(
        id = id,
        amountCents = 1_000,
        paymentType = "CREDITO",
        merchantId = "123456789012345",
        terminalId = "12345678",
        status = status,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = createdAtMillis,
        undoReason = null,
        receiptPrinted = false,
    )

    @Test
    fun inserirEObservarPorId() =
        runTest {
            dao.upsert(entity())

            val observed = dao.observeById("tx-1").first()

            assertThat(observed?.id).isEqualTo("tx-1")
            assertThat(observed?.status).isEqualTo("CRIADA")
        }

    @Test
    fun atualizarSobrescreveOEstado() =
        runTest {
            dao.upsert(entity(status = "CRIADA"))
            dao.upsert(entity(status = "APROVADA"))

            assertThat(dao.getById("tx-1")?.status).isEqualTo("APROVADA")
            assertThat(dao.getAll()).hasSize(1)
        }

    @Test
    fun observarTodasEmOrdemDeCriacao() =
        runTest {
            dao.upsert(entity(id = "tx-1", createdAtMillis = 0))
            dao.upsert(entity(id = "tx-2", createdAtMillis = 10))

            val all = dao.observeAll().first()

            assertThat(all.map(TransactionEntity::id)).containsExactly("tx-2", "tx-1").inOrder()
        }

    @Test
    fun transacoesNaoFinaisParaRecuperacao() =
        runTest {
            dao.upsert(entity(id = "tx-1", status = "AGUARDANDO_RESULTADO"))
            dao.upsert(entity(id = "tx-2", status = "APROVADA"))
            dao.upsert(entity(id = "tx-3", status = "DESFAZIMENTO_PENDENTE"))

            val nonFinal = dao.getNonFinal()

            assertThat(nonFinal.map(TransactionEntity::id)).containsExactly("tx-1", "tx-3")
        }

    @Test
    fun pendentesDeDesfazimento() =
        runTest {
            dao.upsert(entity(id = "tx-1", status = "DESFAZIMENTO_PENDENTE"))
            dao.upsert(entity(id = "tx-2", status = "AUTORIZANDO"))

            assertThat(dao.getPendingUndo().map(TransactionEntity::id)).containsExactly("tx-1")
        }
}
