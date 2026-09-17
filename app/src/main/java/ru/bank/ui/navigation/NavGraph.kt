package ru.bank.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.bank.ui.rememberSessionManager
import ru.bank.ui.screens.clients.*
import ru.bank.ui.screens.login.LoginScreen
import ru.bank.ui.screens.profile.ProfileScreen
import ru.bank.ui.screens.rates.CurrencyRatesScreen

@Composable
fun BankNavHost() {
    val navController = rememberNavController()
    val session = rememberSessionManager()
    val startDestination = if (session.isLoggedIn) Destination.Clients.route else ROUTE_LOGIN

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(Destination.Clients.route, Destination.Rates.route, Destination.Profile.route)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { if (showBottomBar) BankBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .statusBarsPadding()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Destination.Clients.route) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                })
            }

            composable(Destination.Clients.route) {
                ClientListScreen(
                    onOpenClient = { id -> navController.navigate(clientAccountsRoute(id)) },
                    onAddClient = { navController.navigate(ROUTE_ADD_CLIENT) }
                )
            }
            composable(ROUTE_ADD_CLIENT) {
                AddClientScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                ROUTE_CLIENT_ACCOUNTS,
                arguments = listOf(navArgument("clientId") { type = NavType.LongType })
            ) { entry ->
                val clientId = entry.arguments?.getLong("clientId") ?: 0L
                ClientAccountsScreen(
                    clientId = clientId,
                    onBack = { navController.popBackStack() },
                    onOpenAccount = { accountId -> navController.navigate(accountOperationsRoute(accountId)) },
                    onAddAccount = { navController.navigate(addAccountRoute(clientId)) },
                    onEditClient = { navController.navigate(editClientRoute(clientId)) }
                )
            }
            composable(
                ROUTE_EDIT_CLIENT,
                arguments = listOf(navArgument("clientId") { type = NavType.LongType })
            ) { entry ->
                val clientId = entry.arguments?.getLong("clientId") ?: 0L
                EditClientScreen(
                    clientId = clientId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                ROUTE_ADD_ACCOUNT,
                arguments = listOf(navArgument("clientId") { type = NavType.LongType })
            ) { entry ->
                val clientId = entry.arguments?.getLong("clientId") ?: 0L
                AddAccountScreen(
                    clientId = clientId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                ROUTE_ACCOUNT_OPERATIONS,
                arguments = listOf(navArgument("accountNumber") { type = NavType.StringType })
            ) { entry ->
                val accountNumber = entry.arguments?.getString("accountNumber") ?: ""
                AccountOperationsScreen(accountNumber = accountNumber, onBack = { navController.popBackStack() })
            }

            composable(Destination.Rates.route) { CurrencyRatesScreen() }

            composable(Destination.Profile.route) {
                ProfileScreen(onLoggedOut = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
    }
}

@Composable
private fun BankBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(tonalElevation = 0.dp) {
        Destination.bottomBarItems.forEach destinationLoop@{ destination ->
            if (destination == null) return@destinationLoop
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.background)
            )
        }
    }
}
