package com.raimundo.smartpos.core.domain.connectivity

import kotlinx.coroutines.flow.Flow

/** "Vender" fica bloqueado offline (cenário 12); a Home mostra o banner correspondente. */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
