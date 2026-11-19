package com.raimundo.smartpos

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/** Permite que os testes instrumentados do :app usem o grafo Hilt completo. */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
