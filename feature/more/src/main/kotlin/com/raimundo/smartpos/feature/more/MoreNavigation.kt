package com.raimundo.smartpos.feature.more

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.raimundo.smartpos.core.ui.component.BottomDestination
import kotlinx.serialization.Serializable

@Serializable
data object MoreRoute

fun NavController.navigateToMore() = navigate(MoreRoute)

fun NavGraphBuilder.moreScreen(
    appVersion: String,
    merchantId: String,
    terminalId: String,
    onPendingsClick: () -> Unit,
    onBottomNavigate: (BottomDestination) -> Unit,
) {
    composable<MoreRoute> {
        MoreScreen(
            appVersion = appVersion,
            merchantId = merchantId,
            terminalId = terminalId,
            onPendingsClick = onPendingsClick,
            onBottomNavigate = onBottomNavigate,
        )
    }
}
