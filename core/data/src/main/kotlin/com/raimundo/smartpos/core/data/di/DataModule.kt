package com.raimundo.smartpos.core.data.di

import android.content.Context
import com.raimundo.smartpos.core.common.ApplicationScope
import com.raimundo.smartpos.core.common.Clock
import com.raimundo.smartpos.core.common.IdGenerator
import com.raimundo.smartpos.core.common.SystemClock
import com.raimundo.smartpos.core.common.UuidIdGenerator
import com.raimundo.smartpos.core.data.journal.RoomTransactionJournal
import com.raimundo.smartpos.core.data.preferences.DataStoreUserPreferences
import com.raimundo.smartpos.core.data.repository.TransactionCoordinatorRepository
import com.raimundo.smartpos.core.domain.connectivity.NetworkMonitor
import com.raimundo.smartpos.core.domain.journal.TransactionJournal
import com.raimundo.smartpos.core.domain.preferences.UserPreferences
import com.raimundo.smartpos.core.domain.repository.TransactionRepository
import com.raimundo.smartpos.core.localstorage.db.SmartPosDatabase
import com.raimundo.smartpos.core.localstorage.db.TransactionDao
import com.raimundo.smartpos.core.localstorage.db.TransactionEventDao
import com.raimundo.smartpos.core.localstorage.db.createSmartPosDatabase
import com.raimundo.smartpos.core.localstorage.prefs.UserPreferencesDataStore
import com.raimundo.smartpos.core.network.ConnectivityManagerNetworkMonitor
import com.raimundo.smartpos.core.printer.PrintQueue
import com.raimundo.smartpos.core.printer.ReceiptFormatter
import com.raimundo.smartpos.core.printer.ReceiptPrinter
import com.raimundo.smartpos.core.printer.SimulatedReceiptPrinter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {
    @Binds
    abstract fun bindTransactionJournal(impl: RoomTransactionJournal): TransactionJournal

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionCoordinatorRepository): TransactionRepository

    @Binds
    abstract fun bindReceiptPrinter(impl: SimulatedReceiptPrinter): ReceiptPrinter

    @Binds
    abstract fun bindClock(impl: SystemClock): Clock

    @Binds
    abstract fun bindIdGenerator(impl: UuidIdGenerator): IdGenerator

    @Binds
    abstract fun bindUserPreferences(impl: DataStoreUserPreferences): UserPreferences
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): SmartPosDatabase = createSmartPosDatabase(context)

    @Provides
    fun provideTransactionDao(db: SmartPosDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTransactionEventDao(db: SmartPosDatabase): TransactionEventDao = db.transactionEventDao()

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): UserPreferencesDataStore = UserPreferencesDataStore(context)

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context,
    ): NetworkMonitor = ConnectivityManagerNetworkMonitor(context)

    @Provides
    @Singleton
    fun provideReceiptFormatter(): ReceiptFormatter = ReceiptFormatter()

    @Provides
    @Singleton
    fun providePrintQueue(
        printer: ReceiptPrinter,
        formatter: ReceiptFormatter,
        clock: Clock,
        @ApplicationScope scope: CoroutineScope,
    ): PrintQueue = PrintQueue(printer, formatter, scope, clock)
}
