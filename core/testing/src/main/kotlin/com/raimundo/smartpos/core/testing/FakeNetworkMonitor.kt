package com.raimundo.smartpos.core.testing

import com.raimundo.smartpos.core.domain.connectivity.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(
    online: Boolean = true,
) : NetworkMonitor {
    private val state = MutableStateFlow(online)
    override val isOnline = state

    fun setOnline(online: Boolean) {
        state.value = online
    }
}
