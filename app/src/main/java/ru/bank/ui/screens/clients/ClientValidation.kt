package ru.bank.ui.screens.clients


object ClientValidation {

    private val passportRegex = Regex("""^\d{4}\s?\d{6}$""")
    private val phoneRegex = Regex("""^(\+7|8)[\s-]?\(?\d{3}\)?[\s-]?\d{3}[\s-]?\d{2}[\s-]?\d{2}$""")

    fun validateFullName(value: String): String? {
        val parts = value.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            value.isBlank() -> "Введите ФИО клиента"
            parts.size < 2 -> "Введите фамилию и имя полностью"
            parts.any { it.any { ch -> ch.isDigit() } } -> "ФИО не должно содержать цифры"
            else -> null
        }
    }

    fun validatePassport(value: String): String? = when {
        value.isBlank() -> "Введите паспортные данные"
        !passportRegex.matches(value.trim()) -> "Формат: 4 цифры серии и 6 цифр номера (например, 6912 345678)"
        else -> null
    }

    fun validatePhone(value: String): String? = when {
        value.isBlank() -> "Введите номер телефона"
        !phoneRegex.matches(value.trim()) -> "Формат: +7 900 111-22-33"
        else -> null
    }
}
