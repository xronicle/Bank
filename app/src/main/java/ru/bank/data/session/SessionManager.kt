package ru.bank.data.session

import android.content.Context


class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("bank_session", Context.MODE_PRIVATE)

    var employeeId: Long
        get() = prefs.getLong(KEY_EMPLOYEE_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_EMPLOYEE_ID, value).apply()

    val isLoggedIn: Boolean get() = employeeId != -1L

    fun logout() {
        prefs.edit().remove(KEY_EMPLOYEE_ID).apply()
    }

    companion object {
        private const val KEY_EMPLOYEE_ID = "employee_id"
    }
}
