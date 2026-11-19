package com.raimundo.smartpos.core.data.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Agenda o retry de pendências para quando o app estiver fechado (mínimo do sistema: 15 min). */
class PendingUndoWorkScheduler
    @Inject
    constructor(
        private val workManager: WorkManager,
    ) {
        fun schedule() {
            val request = PeriodicWorkRequestBuilder<RetryPendingUndoWorker>(15, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(
                RetryPendingUndoWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
