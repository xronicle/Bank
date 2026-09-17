package ru.bank

import android.app.Application
import ru.bank.data.AppDatabase
import ru.bank.data.BankRepository
import ru.bank.data.session.SessionManager

class BankApp : Application() {
    lateinit var repository: BankRepository
        private set
    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = BankRepository(db)
        sessionManager = SessionManager(this)
    }
}
