package com.raimundo.smartpos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.raimundo.smartpos.core.data.repository.TransactionCoordinatorRepository
import com.raimundo.smartpos.core.data.worker.PendingUndoWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmartPosApplication :
    Application(),
    Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var transactionCoordinator: TransactionCoordinatorRepository

    @Inject lateinit var pendingUndoWorkScheduler: PendingUndoWorkScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // Write-ahead: desfaz transações órfãs (app morto no meio do fluxo) e retenta pendências.
        transactionCoordinator.start()
        pendingUndoWorkScheduler.schedule()
    }
}
