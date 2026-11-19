package com.raimundo.smartpos.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raimundo.smartpos.core.domain.usecase.RetryPendingUndoUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Retry de `DESFAZIMENTO_PENDENTE` com o app fechado. O WorkManager não agenda periódicos
 * com menos de 15 min (limite do sistema); com o app aberto, o retry de 30 s é feito pelo
 * próprio `TransactionCoordinatorRepository`.
 */
@HiltWorker
class RetryPendingUndoWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val retryPendingUndo: RetryPendingUndoUseCase,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result =
            try {
                retryPendingUndo(null)
                Result.success()
            } catch (
                @Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception,
            ) {
                Result.retry()
            }

        companion object {
            const val UNIQUE_WORK_NAME = "retry-pending-undo"
        }
    }
