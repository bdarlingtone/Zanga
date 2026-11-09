package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String, // e.g. Personal, Education, Professional, etc.
    val dateMillis: Long, // Start of the scheduled day
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val allocatedDurationMinutes: Int,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val location: String = "",
    val notes: String = "",
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED, OVERDUE, EXTENDED, CANCELLED
    val completedAtMillis: Long? = null,
    val timeExtendedMinutes: Int = 0,
    val actualDurationMinutes: Int = 0,
    val isWarning30mEnabled: Boolean = true,
    val isWarning15mEnabled: Boolean = true,
    val isWarning5mEnabled: Boolean = true,
    val isDeadlineAlarmEnabled: Boolean = true,
    val isAcademicOrProfessional: Boolean = false,
    val subType: String = "GENERAL" // CLASS, ASSIGNMENT, EXAM, PROJECT, STUDY, WORK, GENERAL
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // INCOME, EXPENSE
    val category: String, // Source for income; Category for expense
    val amount: Double,
    val currency: String = "MWK",
    val dateMillis: Long,
    val paymentMethod: String = "Cash", // Cash, Bank, Mobile Money, Card
    val description: String = ""
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val period: String = "MONTHLY", // DAILY, WEEKLY, MONTHLY
    val limitAmount: Double,
    val category: String = "Overall", // Overall or specific expense category
    val currency: String = "MWK"
)

@Entity(tableName = "financial_obligations")
data class FinancialObligationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // OWED_TO_OTHERS, LENT_TO_OTHERS
    val personName: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val currency: String = "MWK",
    val dueDateMillis: Long,
    val notes: String = "",
    val isSettled: Boolean = false
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val currency: String = "MWK",
    val deadlineMillis: Long,
    val category: String = "General Savings",
    val notes: String = ""
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val frequency: String = "DAILY", // DAILY, WEEKLY
    val targetDaysPerWeek: Int = 7,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: Long? = null,
    val historyDatesCsv: String = "", // e.g. "2026-11-08,2026-11-07"
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val venue: String = "",
    val dateMillis: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val participants: String = "",
    val description: String = "",
    val advanceReminderMinutes: Int = 15,
    val notes: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "custom_categories")
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "Bookmark",
    val colorHex: String = "#1769D2"
)

@Entity(tableName = "productivity_logs")
data class ProductivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,
    val score: Int,
    val completedActivitiesCount: Int,
    val onTimeCount: Int,
    val overdueCount: Int,
    val habitsDoneCount: Int,
    val timeSpentMinutes: Int
)
