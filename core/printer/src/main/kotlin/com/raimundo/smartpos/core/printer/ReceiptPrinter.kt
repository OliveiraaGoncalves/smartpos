package com.raimundo.smartpos.core.printer

import android.util.Log
import kotlinx.coroutines.delay

/** Abstração da impressora térmica. Em um POS real, adaptador para o SDK do fabricante. */
interface ReceiptPrinter {
    suspend fun print(receipt: String)
}

class SimulatedReceiptPrinter(
    private val printDurationMs: Long = 600,
) : ReceiptPrinter {
    override suspend fun print(receipt: String) {
        delay(printDurationMs) // tempo de impressão física
        Log.i(TAG, "\n$receipt")
    }

    private companion object {
        const val TAG = "SmartPOS-Printer"
    }
}
