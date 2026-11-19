package com.raimundo.smartpos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raimundo.smartpos.AppContainer
import com.raimundo.smartpos.ui.home.HomeRoute
import com.raimundo.smartpos.ui.home.HomeViewModel
import com.raimundo.smartpos.ui.sale.SaleRoute
import com.raimundo.smartpos.ui.sale.SaleViewModel

private object Routes {
    const val HOME = "home"
    const val SALE = "sale"
}

@Composable
fun SmartPosNavHost(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        HomeViewModel(
                            transactions = container.transactionManager.transactions,
                            receipts = container.printQueue.printed,
                        )
                    }
                },
            )
            HomeRoute(
                viewModel = viewModel,
                onSellClick = { navController.navigate(Routes.SALE) { launchSingleTop = true } },
            )
        }

        composable(Routes.SALE) {
            val viewModel: SaleViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { SaleViewModel(container.transactionManager) }
                },
            )
            SaleRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaleStarted = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
    }
}
