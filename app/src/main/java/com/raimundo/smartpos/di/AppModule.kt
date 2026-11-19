package com.raimundo.smartpos.di

import com.raimundo.smartpos.BuildConfig
import com.raimundo.smartpos.core.data.repository.TerminalConfigProvider
import com.raimundo.smartpos.core.model.TerminalConfig
import com.raimundo.smartpos.startup.RawTerminalIds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**
     * O StartupGate já validou merchantId/terminalId (ValidateTerminalUseCase) antes de
     * qualquer venda ser possível, então a construção aqui é segura.
     */
    @Provides
    @Singleton
    fun provideTerminalConfigProvider(): TerminalConfigProvider =
        TerminalConfigProvider { TerminalConfig(BuildConfig.MERCHANT_ID, BuildConfig.TERMINAL_ID) }

    /** Strings cruas (não validadas ainda) usadas pelo StartupGate antes de liberar a venda. */
    @Provides
    @Singleton
    fun provideRawTerminalIds(): RawTerminalIds = RawTerminalIds(BuildConfig.MERCHANT_ID, BuildConfig.TERMINAL_ID)
}
