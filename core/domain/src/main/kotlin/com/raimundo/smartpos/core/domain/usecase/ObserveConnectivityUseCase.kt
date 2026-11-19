package com.raimundo.smartpos.core.domain.usecase

import com.raimundo.smartpos.core.domain.connectivity.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConnectivityUseCase
    @Inject
    constructor(
        private val networkMonitor: NetworkMonitor,
    ) {
        operator fun invoke(): Flow<Boolean> = networkMonitor.isOnline
    }
