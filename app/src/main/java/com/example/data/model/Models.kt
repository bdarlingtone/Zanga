package com.example.data.model

enum class ActivityPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class ActivityStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE,
    EXTENDED,
    CANCELLED
}

enum class StandardCategory(val displayName: String) {
    PERSONAL("Personal"),
    EDUCATION("Education"),
    PROFESSIONAL("Professional"),
    BUSINESS("Business"),
    FAMILY("Family"),
    HEALTH_FITNESS("Health & Fitness"),
    SOCIAL("Social Activities"),
    HOUSEHOLD("Household Responsibilities"),
    RELIGIOUS("Religious & Spiritual"),
    OTHER("Other")
}

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class BudgetPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class ObligationType {
    OWED_TO_OTHERS,
    LENT_TO_OTHERS
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Transfer"),
    MOBILE_MONEY("Mobile Money"),
    CARD("Debit/Credit Card")
}

data class NextActivityInfo(
    val id: Long,
    val title: String,
    val category: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val remainingMillis: Long,
    val isRunning: Boolean,
    val isOverdue: Boolean
)

data class DisciplineScoreBreakdown(
    val totalScore: Int, // 0 - 100
    val punctualityScore: Int, // On-time completion
    val habitConsistencyScore: Int, // Streaks & daily habits
    val overduePenalty: Int, // Overdue deductions
    val budgetScore: Int, // Budget adherence
    val rating: String // e.g. "Commander", "Exemplary", "Focused", "Disciplined", "Building Momentum"
)
