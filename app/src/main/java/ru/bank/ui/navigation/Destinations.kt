package ru.bank.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    object Clients : Destination("clients", "Клиенты", Icons.Outlined.People)
    object Rates : Destination("rates", "Курсы валют", Icons.Outlined.ShowChart)
    object Profile : Destination("profile", "Сотрудник", Icons.Outlined.Person)

    companion object {
        val bottomBarItems: List<Destination> by lazy { listOf(Clients, Rates, Profile) }
    }
}

const val ROUTE_LOGIN = "login"
const val ROUTE_ADD_CLIENT = "clients/add"
const val ROUTE_CLIENT_ACCOUNTS = "clients/{clientId}/accounts"
const val ROUTE_EDIT_CLIENT = "clients/{clientId}/edit"
const val ROUTE_ADD_ACCOUNT = "clients/{clientId}/accounts/add"
const val ROUTE_ACCOUNT_OPERATIONS = "accounts/{accountNumber}/operations"

fun clientAccountsRoute(clientId: Long) = "clients/$clientId/accounts"
fun editClientRoute(clientId: Long) = "clients/$clientId/edit"
fun addAccountRoute(clientId: Long) = "clients/$clientId/accounts/add"
fun accountOperationsRoute(accountNumber: String) = "accounts/$accountNumber/operations"
