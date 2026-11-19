package com.raimundo.smartpos

import android.app.Application

class SmartPosApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Recupera transações interrompidas (ex.: app morto no meio do polling).
        container.transactionManager.start()
    }
}
