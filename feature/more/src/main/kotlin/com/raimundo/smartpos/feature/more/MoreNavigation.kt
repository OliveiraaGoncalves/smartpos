package com.raimundo.smartpos.feature.more

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.raimundo.smartpos.core.ui.component.BottomDestination
import kotlinx.serialization.Serializable

@Serializable
data object MoreRoute

/** Raiz de aba: ver HomeNavigation.navigateToHome para o porquê do popUpTo/singleTop/restoreState. */
fun NavController.navigateToMore() =
    navigate(MoreRoute) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

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
