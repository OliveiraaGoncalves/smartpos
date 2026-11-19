package com.raimundo.smartpos.core.printer

import android.util.Log
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Abstração da impressora térmica. Em um POS real, adaptador para o SDK do fabricante. */
interface ReceiptPrinter {
    suspend fun print(receipt: String)
}

class SimulatedReceiptPrinter
    @Inject
    constructor() : ReceiptPrinter {
        override suspend fun print(receipt: String) {
            delay(PRINT_DURATION_MS) // tempo de impressão física
            Log.i(TAG, "\n$receipt")
        }

        private companion object {
            const val TAG = "SmartPOS-Printer"
            const val PRINT_DURATION_MS = 600L
        }
    }
