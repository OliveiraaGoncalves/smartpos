package com.raimundo.smartpos.feature.blocking

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data class BlockingRoute(
    val reason: BlockingReason,
)

fun NavController.navigateToBlocking(reason: BlockingReason) = navigate(BlockingRoute(reason))

fun NavGraphBuilder.blockingScreen(onRetryClick: () -> Unit) {
    composable<BlockingRoute> { backStackEntry ->
        BlockingScreen(
            reason = backStackEntry.toRoute<BlockingRoute>().reason,
            onRetryClick = onRetryClick,
        )
    }
}
