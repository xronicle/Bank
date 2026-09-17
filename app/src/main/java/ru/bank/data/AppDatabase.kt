package ru.bank.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.bank.data.dao.*
import ru.bank.data.entity.*
import java.util.concurrent.TimeUnit


@Database(
    entities = [
        BranchEntity::class,
        EmployeeEntity::class,
        ClientEntity::class,
        CurrencyEntity::class,
        AccountEntity::class,
        OperationEntity::class,
        CurrencyHistoryEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun branchDao(): BranchDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun clientDao(): ClientDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun accountDao(): AccountDao
    abstract fun operationDao(): OperationDao
    abstract fun currencyHistoryDao(): CurrencyHistoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oplot.db"
                )
                    .addCallback(seedCallback)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }

        private val seedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedSynchronously(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                catchUpCurrencyHistory(db)
            }
        }

        private fun catchUpCurrencyHistory(db: SupportSQLiteDatabase) {
            val now = normalizeToStartOfDay(System.currentTimeMillis())
            val day = TimeUnit.DAYS.toMillis(1)
            val currencies = listOf("USD", "EUR", "CNY", "GBP", "KZT", "JPY")

            currencies.forEach { code ->
                db.query("SELECT date, rate FROM currency_history WHERE currencyCode = ? ORDER BY date DESC LIMIT 1", arrayOf(code)).use { cursor ->
                    if (cursor.moveToFirst()) {
                        val lastDate = cursor.getLong(0)
                        var lastRate = cursor.getDouble(1)

                        var nextDate = lastDate + day
                        while (nextDate <= now) {
                            val step = (Math.random() - 0.5) * lastRate * 0.01
                            lastRate -= step

                            db.execSQL(
                                "INSERT INTO currency_history (currencyCode, date, rate) VALUES (?, ?, ?)",
                                arrayOf<Any?>(code, nextDate, lastRate)
                            )
                            nextDate += day
                        }
                    }
                }
            }
        }

        private fun seedSynchronously(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            val day = TimeUnit.DAYS.toMillis(1)

            db.execSQL(
                "INSERT INTO branches (branchNumber, address) VALUES (?, ?)",
                arrayOf<Any?>(14L, "г. Томск, пр. Ленина, 30")
            )
            db.execSQL(
                "INSERT INTO branches (branchNumber, address) VALUES (?, ?)",
                arrayOf<Any?>(21L, "г. Томск, ул. Вершинина, 39а")
            )
            val centralBranch = 14L
            val vershininaBranch = 21L

            db.execSQL(
                "INSERT INTO employees (id, fullName, position, branchNumber, password) VALUES (?, ?, ?, ?, ?)",
                arrayOf<Any?>(1L, "Иванова Мария Сергеевна", "Специалист по обслуживанию клиентов", centralBranch, "1234")
            )
            db.execSQL(
                "INSERT INTO employees (id, fullName, position, branchNumber, password) VALUES (?, ?, ?, ?, ?)",
                arrayOf<Any?>(2L, "Коротков Максим", "Специалист по обслуживанию клиентов", vershininaBranch, "1234")
            )

            val currencies = listOf(
                arrayOf<Any?>("RUB", "Российский рубль", "Россия", 1),
                arrayOf<Any?>("USD", "Доллар США", "США", 1),
                arrayOf<Any?>("EUR", "Евро", "Евросоюз", 1),
                arrayOf<Any?>("CNY", "Китайский юань", "Китай", 1),
                arrayOf<Any?>("GBP", "Фунт стерлингов", "Великобритания", 1),
                arrayOf<Any?>("KZT", "Казахстанский тенге", "Казахстан", 100),
                arrayOf<Any?>("JPY", "Японская йена", "Япония", 100)
            )
            currencies.forEach { row ->
                db.execSQL("INSERT INTO currencies (code, name, issuerCountry, unit) VALUES (?, ?, ?, ?)", row)
            }

            val clients = listOf(
                arrayOf<Any?>(1L, "Смирнова Елена Андреевна", "6912 345678", "+7 900 111-22-33"),
                arrayOf<Any?>(2L, "Ковалёв Роман Игоревич", "6913 987654", "+7 900 222-33-44"),
                arrayOf<Any?>(3L, "Павлов Дмитрий Олегович", "6914 456789", "+7 900 333-44-55"),
                arrayOf<Any?>(4L, "Морозова Анна Викторовна", "6915 224466", "+7 900 444-55-66"),
                arrayOf<Any?>(5L, "Егоров Артём Николаевич", "6916 778899", "+7 900 555-66-77")
            )
            clients.forEach { row ->
                db.execSQL("INSERT INTO clients (id, fullName, passportData, phoneNumber) VALUES (?, ?, ?, ?)", row)
            }

            data class SeedAccount(
                val number: String,
                val clientId: Long,
                val employeeId: Long,
                val branchNumber: Long,
                val currencyCode: String,
                val accountType: String,
                val contractNumber: String,
                val openedAt: Long,
                val openingDeposit: Double
            )
            val accounts = listOf(
                SeedAccount(genAccountNumber(), 1L, 1L, centralBranch, "RUB", "Текущий", "Д-2024-00101", dateMillis(2024, 2, 12), 500_000.0),
                SeedAccount(genAccountNumber(), 1L, 1L, centralBranch, "USD", "Накопительный", "Д-2024-00102", dateMillis(2024, 2, 12), 1_000.0),
                SeedAccount(genAccountNumber(), 2L, 1L, centralBranch, "RUB", "Текущий", "Д-2024-00145", dateMillis(2024, 4, 20), 25_000.0),
                SeedAccount(genAccountNumber(), 3L, 2L, vershininaBranch, "EUR", "Депозитный", "Д-2024-00201", dateMillis(2024, 6, 3), 700.0),
                SeedAccount(genAccountNumber(), 4L, 2L, vershininaBranch, "RUB", "Текущий", "Д-2024-00256", dateMillis(2024, 8, 15), 200_000.0),
                SeedAccount(genAccountNumber(), 4L, 2L, vershininaBranch, "CNY", "Накопительный", "Д-2024-00257", dateMillis(2024, 8, 15), 2_500.0),
                SeedAccount(genAccountNumber(), 5L, 1L, centralBranch, "GBP", "Текущий", "Д-2024-00310", dateMillis(2024, 11, 2), 500.0)
            )


            var operationId = 1L
            accounts.forEachIndexed { index, acc ->
                val scale = 1.0 + index * 0.15
                val ops = listOf(
                    Triple("INCOME", acc.openingDeposit, acc.openedAt),
                    Triple("INCOME", 25_000.0 * scale, addMonths(acc.openedAt, 1)),
                    Triple("EXPENSE", 6_400.0 * scale, addMonths(acc.openedAt, 5)),
                    Triple("INCOME", 31_500.0 * scale, addMonths(acc.openedAt, 9)),
                    Triple("EXPENSE", 12_200.0 * scale, addMonths(acc.openedAt, 13)),
                    Triple("INCOME", 8_900.0 * scale, addMonths(acc.openedAt, 16)),
                    Triple("INCOME", 18_000.0 * scale, now - 1 * day),
                    Triple("EXPENSE", 4_300.0 * scale, now - 2 * day),
                    Triple("EXPENSE", 1_150.0 * scale, now - 3 * day),
                    Triple("INCOME", 94_000.0 * scale, now - 4 * day),
                    Triple("INCOME", 6_750.0 * scale, now - 6 * day)
                )
                val balance = ops.sumOf { (type, amount, _) -> if (type == "INCOME") amount else -amount }

                db.execSQL(
                    """INSERT INTO accounts
                       (accountNumber, clientId, employeeId, branchNumber, currencyCode, accountType, contractNumber, openedAt, balance)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                    arrayOf<Any?>(
                        acc.number, acc.clientId, acc.employeeId, acc.branchNumber,
                        acc.currencyCode, acc.accountType, acc.contractNumber, acc.openedAt, balance
                    )
                )
                ops.forEach { (type, amount, date) ->
                    db.execSQL(
                        "INSERT INTO operations (id, accountNumber, type, amount, date) VALUES (?, ?, ?, ?, ?)",
                        arrayOf<Any?>(operationId, acc.number, type, amount, date)
                    )
                    operationId++
                }
            }


            val baseRates = mapOf(
                "USD" to 96.4,
                "EUR" to 103.1,
                "CNY" to 13.3,
                "GBP" to 121.8,
                "KZT" to 17.9,
                "JPY" to 62.0
            )
            val historyDays = 30
            for ((code, base) in baseRates) {
                val points = DoubleArray(historyDays)
                points[0] = base
                for (i in 1 until historyDays) {
                    val step = (Math.random() - 0.5) * base * 0.01
                    points[i] = points[i - 1] - step
                }

                for (i in 0 until historyDays) {
                    db.execSQL(
                        "INSERT INTO currency_history (currencyCode, date, rate) VALUES (?, ?, ?)",
                        arrayOf<Any?>(code, normalizeToStartOfDay(now - i * day), points[i])
                    )
                }
            }
        }

        private fun normalizeToStartOfDay(millis: Long): Long {
            val day = TimeUnit.DAYS.toMillis(1)
            return (millis / day) * day
        }

        private fun dateMillis(year: Int, month: Int, day: Int): Long =
            java.time.LocalDate.of(year, month, day)
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()

        private fun addMonths(millis: Long, months: Long): Long =
            java.time.Instant.ofEpochMilli(millis)
                .atZone(java.time.ZoneOffset.UTC)
                .toLocalDate()
                .plusMonths(months)
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()

        private fun genAccountNumber(): String =
            "40817810" + (100000000..999999999).random()
    }
}