package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ActivityDao
import com.example.data.dao.AppointmentDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.FinanceDao
import com.example.data.dao.HabitDao
import com.example.data.dao.ProductivityDao
import com.example.data.entity.ActivityEntity
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BudgetEntity
import com.example.data.entity.CustomCategoryEntity
import com.example.data.entity.FinancialObligationEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.ProductivityLogEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        ActivityEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        FinancialObligationEntity::class,
        SavingsGoalEntity::class,
        HabitEntity::class,
        AppointmentEntity::class,
        CustomCategoryEntity::class,
        ProductivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZangaDatabase : RoomDatabase() {

    abstract fun activityDao(): ActivityDao
    abstract fun financeDao(): FinanceDao
    abstract fun habitDao(): HabitDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productivityDao(): ProductivityDao

    companion object {
        @Volatile
        private var INSTANCE: ZangaDatabase? = null

        fun getDatabase(context: Context): ZangaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZangaDatabase::class.java,
                    "zanga_master_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    seedInitialData(database)
                }
            }
        }

        private suspend fun seedInitialData(db: ZangaDatabase) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis

            // Initial Activities
            val act1Start = startOfDay + 8 * 3600 * 1000 // 08:00 AM
            val act1End = act1Start + 2 * 3600 * 1000 // 10:00 AM (2 hours)
            db.activityDao().insert(
                ActivityEntity(
                    title = "Study C++ Programming",
                    description = "Master memory management, pointers, and modern C++20 concurrency.",
                    category = "Education",
                    dateMillis = startOfDay,
                    startTimeMillis = act1Start,
                    endTimeMillis = act1End,
                    allocatedDurationMinutes = 120,
                    priority = "HIGH",
                    location = "Tech Lab / Desk",
                    notes = "Complete exercises 4 through 7.",
                    status = "PENDING",
                    isWarning30mEnabled = true,
                    isWarning15mEnabled = true,
                    isWarning5mEnabled = true,
                    isDeadlineAlarmEnabled = true,
                    isAcademicOrProfessional = true,
                    subType = "STUDY"
                )
            )

            val act2Start = startOfDay + 11 * 3600 * 1000 // 11:00 AM
            val act2End = act2Start + 90 * 60 * 1000 // 12:30 PM
            db.activityDao().insert(
                ActivityEntity(
                    title = "Business Strategy & Financial Review",
                    description = "Analyze Q4 revenue streams, operational budgets and upcoming vendor contracts.",
                    category = "Business",
                    dateMillis = startOfDay,
                    startTimeMillis = act2Start,
                    endTimeMillis = act2End,
                    allocatedDurationMinutes = 90,
                    priority = "URGENT",
                    location = "Executive Boardroom",
                    notes = "Prepare slides for department leads.",
                    status = "PENDING",
                    isAcademicOrProfessional = true,
                    subType = "WORK"
                )
            )

            val act3Start = startOfDay + 14 * 3600 * 1000 // 02:00 PM
            val act3End = act3Start + 60 * 60 * 1000 // 03:00 PM
            db.activityDao().insert(
                ActivityEntity(
                    title = "Personal Workout & Conditioning",
                    description = "High intensity interval training and core mobility session.",
                    category = "Health & Fitness",
                    dateMillis = startOfDay,
                    startTimeMillis = act3Start,
                    endTimeMillis = act3End,
                    allocatedDurationMinutes = 60,
                    priority = "MEDIUM",
                    location = "Fit Gym",
                    status = "PENDING"
                )
            )

            // Initial Habits
            db.habitDao().insert(
                HabitEntity(
                    title = "Morning Deep Work & Planning",
                    description = "30 minutes of priority setting before notifications or emails.",
                    frequency = "DAILY",
                    currentStreak = 14,
                    bestStreak = 21,
                    reminderHour = 7,
                    reminderMinute = 0
                )
            )
            db.habitDao().insert(
                HabitEntity(
                    title = "Read 25 Pages of Engineering / Finance",
                    description = "Daily reading to expand mental models.",
                    frequency = "DAILY",
                    currentStreak = 8,
                    bestStreak = 19,
                    reminderHour = 21,
                    reminderMinute = 30
                )
            )
            db.habitDao().insert(
                HabitEntity(
                    title = "Physical Conditioning & Hydration",
                    description = "Drink 3L water and 45m active movement.",
                    frequency = "DAILY",
                    currentStreak = 5,
                    bestStreak = 12,
                    reminderHour = 18,
                    reminderMinute = 0
                )
            )

            // Initial Finances (MWK default)
            db.financeDao().insertBudget(
                BudgetEntity(
                    period = "MONTHLY",
                    limitAmount = 750000.0,
                    category = "Overall",
                    currency = "MWK"
                )
            )
            db.financeDao().insertBudget(
                BudgetEntity(
                    period = "DAILY",
                    limitAmount = 25000.0,
                    category = "Daily Spending",
                    currency = "MWK"
                )
            )

            db.financeDao().insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    category = "Consulting & Retainer",
                    amount = 450000.0,
                    currency = "MWK",
                    dateMillis = now - 86400000L * 2,
                    paymentMethod = "Bank Transfer",
                    description = "Software architecture advisory retainer"
                )
            )
            db.financeDao().insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    category = "Groceries & Supplies",
                    amount = 35000.0,
                    currency = "MWK",
                    dateMillis = now - 86400000L,
                    paymentMethod = "Mobile Money",
                    description = "Weekly pantry essentials and fresh produce"
                )
            )
            db.financeDao().insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    category = "Transport & Fuel",
                    amount = 18500.0,
                    currency = "MWK",
                    dateMillis = now - 18000000L,
                    paymentMethod = "Cash",
                    description = "Vehicle fueling and toll fees"
                )
            )

            // Savings Goal
            db.financeDao().insertSavingsGoal(
                SavingsGoalEntity(
                    title = "Emergency & Opportunity Reserve",
                    targetAmount = 1500000.0,
                    currentAmount = 620000.0,
                    currency = "MWK",
                    deadlineMillis = now + 90L * 86400000L,
                    category = "Safety Net",
                    notes = "Target 6 months of living expenses."
                )
            )

            // Obligations
            db.financeDao().insertObligation(
                FinancialObligationEntity(
                    type = "LENT_TO_OTHERS",
                    personName = "Chimwemwe Phiri",
                    amount = 50000.0,
                    paidAmount = 0.0,
                    currency = "MWK",
                    dueDateMillis = now + 14L * 86400000L,
                    notes = "Short term bridge for logistics.",
                    isSettled = false
                )
            )

            // Appointment
            db.appointmentDao().insert(
                AppointmentEntity(
                    title = "Tech Team Quarterly Sprint Review",
                    venue = "Main Office / Zoom Room 1",
                    dateMillis = startOfDay,
                    startTimeMillis = startOfDay + 15 * 3600 * 1000 + 30 * 60 * 1000, // 03:30 PM
                    endTimeMillis = startOfDay + 17 * 3600 * 1000, // 05:00 PM
                    participants = "Engineering Leads, Product Manager",
                    description = "Evaluate release velocity and feature milestones.",
                    advanceReminderMinutes = 15
                )
            )
        }
    }
}
