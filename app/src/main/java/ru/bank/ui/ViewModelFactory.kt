package ru.bank.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.bank.BankApp
import ru.bank.data.BankRepository
import ru.bank.data.session.SessionManager

class ViewModelFactory(
    private val repository: BankRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
            modelClass.getConstructor(BankRepository::class.java, SessionManager::class.java)
                .newInstance(repository, sessionManager)
        } catch (e: NoSuchMethodException) {
            modelClass.getConstructor(BankRepository::class.java).newInstance(repository)
        }
    }
}

@Composable
fun rememberRepository(): BankRepository {
    val app = LocalContext.current.applicationContext as BankApp
    return app.repository
}

@Composable
fun rememberSessionManager(): SessionManager {
    val app = LocalContext.current.applicationContext as BankApp
    return app.sessionManager
}

@Composable
inline fun <reified T : ViewModel> repositoryViewModel(): T {
    val repository = rememberRepository()
    val session = rememberSessionManager()
    return viewModel(factory = ViewModelFactory(repository, session))
}
