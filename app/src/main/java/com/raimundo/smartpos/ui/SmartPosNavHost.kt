package com.raimundo.smartpos.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.raimundo.smartpos.core.ui.component.BottomDestination
import com.raimundo.smartpos.feature.blocking.BlockingScreen
import com.raimundo.smartpos.feature.home.HomeRoute
import com.raimundo.smartpos.feature.home.homeScreen
import com.raimundo.smartpos.feature.home.navigateToHome
import com.raimundo.smartpos.feature.more.moreScreen
import com.raimundo.smartpos.feature.more.navigateToMore
import com.raimundo.smartpos.feature.pendings.navigateToPendings
import com.raimundo.smartpos.feature.pendings.pendingsScreen
import com.raimundo.smartpos.feature.receipt.navigateToReceipt
import com.raimundo.smartpos.feature.receipt.receiptScreen
import com.raimundo.smartpos.feature.sale.navigateToSale
import com.raimundo.smartpos.feature.sale.saleScreen
import com.raimundo.smartpos.feature.transactiondetail.navigateToTransactionDetail
import com.raimundo.smartpos.feature.transactiondetail.transactionDetailScreen
import com.raimundo.smartpos.feature.transactions.navigateToTransactions
import com.raimundo.smartpos.feature.transactions.transactionsScreen
import com.raimundo.smartpos.startup.StartupState
import com.raimundo.smartpos.startup.StartupViewModel

@Composable
fun SmartPosApp() {
    val startupViewModel: StartupViewModel = hiltViewModel()
    val startupState by startupViewModel.state.collectAsStateWithLifecycle()

    when (val state = startupState) {
        StartupState.Loading -> {
            LoadingScreen()
        }

        is StartupState.Blocked -> {
            BlockingScreen(
                reason = state.reason,
                onRetryClick = startupViewModel::retry,
                onSupportClick = {},
            )
        }

        StartupState.Ready -> {
            SmartPosNavHost()
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SmartPosNavHost(navController: NavHostController = rememberNavController()) {
    val snackbarHostState = remember { SnackbarHostState() }

    val goToBottomDestination: (BottomDestination) -> Unit = { destination ->
        when (destination) {
            BottomDestination.HOME -> navController.navigateToHome()
            BottomDestination.TRANSACTIONS -> navController.navigateToTransactions()
            BottomDestination.MORE -> navController.navigateToMore()
        }
    }

    NavHost(navController = navController, startDestination = HomeRoute) {
        homeScreen(
            snackbarHostState = snackbarHostState,
            onNavigateToSale = { navController.navigateToSale() },
            onNavigateToDetail = { navController.navigateToTransactionDetail(it) },
            onNavigateToTransactions = { navController.navigateToTransactions() },
            onNavigateToPendings = { navController.navigateToPendings() },
            onBottomNavigate = goToBottomDestination,
        )
        saleScreen(
            snackbarHostState = snackbarHostState,
            onBack = { navController.popBackStack() },
            onSaleSubmitted = { navController.popBackStack() },
        )
        transactionDetailScreen(
            snackbarHostState = snackbarHostState,
            onBack = { navController.popBackStack() },
            onShowReceipt = { navController.navigateToReceipt(it) },
        )
        receiptScreen(
            snackbarHostState = snackbarHostState,
            onClose = { navController.popBackStack() },
        )
        transactionsScreen(
            onTransactionClick = { navController.navigateToTransactionDetail(it) },
            onBottomNavigate = goToBottomDestination,
        )
        pendingsScreen(
            snackbarHostState = snackbarHostState,
            onBack = { navController.popBackStack() },
            onTransactionClick = { navController.navigateToTransactionDetail(it) },
        )
        moreScreen(
            appVersion = com.raimundo.smartpos.BuildConfig.VERSION_NAME,
            terminalId = com.raimundo.smartpos.BuildConfig.TERMINAL_ID,
            onPendingsClick = { navController.navigateToPendings() },
            onTerminalDataClick = {},
            onSupportClick = {},
            onBottomNavigate = goToBottomDestination,
        )
    }
}
