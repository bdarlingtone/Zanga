package com.example.data.repository

import android.content.Context
import com.example.alarm.ZangaAlarmScheduler
import com.example.data.entity.ActivityEntity
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BudgetEntity
import com.example.data.entity.CustomCategoryEntity
import com.example.data.entity.FinancialObligationEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.ProductivityLogEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.data.local.ZangaDatabase
import com.example.data.model.DisciplineScoreBreakdown
import com.example.data.model.NextActivityInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ScheduleConflict(
    val activity1: ActivityEntity,
    val activity2: ActivityEntity,
    val overlapMinutes: Long,
    val suggestedAlternativeSlot: String
)

class ZangaRepository(
    private val context: Context,
    private val database: ZangaDatabase = ZangaDatabase.getDatabase(context)
) {
    private val activityDao = database.activityDao()
    private val financeDao = database.financeDao()
    private val habitDao = database.habitDao()
    private val appointmentDao = database.appointmentDao()
    private val categoryDao = database.categoryDao()
    private val productivityDao = database.productivityDao()

    // Activities
    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()
    fun getAcademicAndProfessional(): Flow<List<ActivityEntity>> = activityDao.getAcademicAndProfessionalActivities()

    suspend fun saveActivity(activity: ActivityEntity): Long {
        val id = activityDao.insert(activity)
        val saved = activity.copy(id = if (activity.id == 0L) id else activity.id)
        ZangaAlarmScheduler.scheduleActivityAlarms(context, saved)
        return id
    }

    suspend fun updateActivity(activity: ActivityEntity) {
        activityDao.update(activity)
        if (activity.status == "COMPLETED" || activity.status == "CANCELLED") {
            ZangaAlarmScheduler.cancelActivityAlarms(context, activity.id)
        } else {
            ZangaAlarmScheduler.scheduleActivityAlarms(context, activity)
        }
    }

    suspend fun completeActivity(activityId: Long) {
        val act = activityDao.getActivityByIdDirect(activityId) ?: return
        val now = System.currentTimeMillis()
        val actualMins = ((now - act.startTimeMillis) / 60000).coerceAtLeast(1).toInt()
        val updated = act.copy(
            status = "COMPLETED",
            completedAtMillis = now,
            actualDurationMinutes = actualMins
        )
        activityDao.update(updated)
        ZangaAlarmScheduler.cancelActivityAlarms(context, activityId)
    }

    suspend fun extendActivity(activityId: Long, additionalMinutes: Int) {
        val act = activityDao.getActivityByIdDirect(activityId) ?: return
        val updated = act.copy(
            endTimeMillis = act.endTimeMillis + (additionalMinutes * 60 * 1000L),
            allocatedDurationMinutes = act.allocatedDurationMinutes + additionalMinutes,
            timeExtendedMinutes = act.timeExtendedMinutes + additionalMinutes,
            status = "EXTENDED"
        )
        activityDao.update(updated)
        ZangaAlarmScheduler.scheduleActivityAlarms(context, updated)
    }

    suspend fun postponeActivity(activityId: Long, postponeMinutes: Int) {
        val act = activityDao.getActivityByIdDirect(activityId) ?: return
        val shiftMillis = postponeMinutes * 60 * 1000L
        val updated = act.copy(
            startTimeMillis = act.startTimeMillis + shiftMillis,
            endTimeMillis = act.endTimeMillis + shiftMillis,
            status = "PENDING"
        )
        activityDao.update(updated)
        ZangaAlarmScheduler.scheduleActivityAlarms(context, updated)
    }

    suspend fun deleteActivity(activity: ActivityEntity) {
        ZangaAlarmScheduler.cancelActivityAlarms(context, activity.id)
        activityDao.delete(activity)
    }

    // Next Activity Countdown Flow
    fun observeNextActivity(): Flow<NextActivityInfo?> {
        return activityDao.getAllActivities().combine(getAllAppointments()) { activities, _ ->
            val now = System.currentTimeMillis()
            // Find current running activity or next upcoming
            val running = activities.firstOrNull {
                it.status != "COMPLETED" && it.status != "CANCELLED" &&
                        now in it.startTimeMillis..it.endTimeMillis
            }

            if (running != null) {
                return@combine NextActivityInfo(
                    id = running.id,
                    title = running.title,
                    category = running.category,
                    startTimeMillis = running.startTimeMillis,
                    endTimeMillis = running.endTimeMillis,
                    remainingMillis = (running.endTimeMillis - now).coerceAtLeast(0),
                    isRunning = true,
                    isOverdue = false
                )
            }

            val overdue = activities.firstOrNull {
                it.status == "OVERDUE" || (it.status == "PENDING" && now > it.endTimeMillis)
            }
            if (overdue != null) {
                return@combine NextActivityInfo(
                    id = overdue.id,
                    title = overdue.title,
                    category = overdue.category,
                    startTimeMillis = overdue.startTimeMillis,
                    endTimeMillis = overdue.endTimeMillis,
                    remainingMillis = 0L,
                    isRunning = false,
                    isOverdue = true
                )
            }

            val nextUpcoming = activities
                .filter { it.status == "PENDING" && it.startTimeMillis > now }
                .minByOrNull { it.startTimeMillis }

            if (nextUpcoming != null) {
                NextActivityInfo(
                    id = nextUpcoming.id,
                    title = nextUpcoming.title,
                    category = nextUpcoming.category,
                    startTimeMillis = nextUpcoming.startTimeMillis,
                    endTimeMillis = nextUpcoming.endTimeMillis,
                    remainingMillis = (nextUpcoming.startTimeMillis - now).coerceAtLeast(0),
                    isRunning = false,
                    isOverdue = false
                )
            } else {
                null
            }
        }
    }

    // Schedule Conflict Detection
    fun detectConflicts(activities: List<ActivityEntity>): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()
        val active = activities.filter { it.status != "COMPLETED" && it.status != "CANCELLED" }
            .sortedBy { it.startTimeMillis }

        for (i in 0 until active.size) {
            for (j in i + 1 until active.size) {
                val a1 = active[i]
                val a2 = active[j]
                if (a1.endTimeMillis > a2.startTimeMillis && a1.startTimeMillis < a2.endTimeMillis) {
                    val overlap = (minOf(a1.endTimeMillis, a2.endTimeMillis) - maxOf(a1.startTimeMillis, a2.startTimeMillis)) / 60000
                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val suggestion = "Shift to ${sdf.format(Date(maxOf(a1.endTimeMillis, a2.endTimeMillis) + 15 * 60 * 1000L))}"
                    conflicts.add(ScheduleConflict(a1, a2, overlap, suggestion))
                }
            }
        }
        return conflicts
    }

    // Discipline Score Calculation
    fun calculateDisciplineScore(
        activities: List<ActivityEntity>,
        habits: List<HabitEntity>,
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>
    ): DisciplineScoreBreakdown {
        if (activities.isEmpty() && habits.isEmpty()) {
            return DisciplineScoreBreakdown(
                totalScore = 85,
                punctualityScore = 35,
                habitConsistencyScore = 25,
                overduePenalty = 0,
                budgetScore = 25,
                rating = "Disciplined"
            )
        }

        val completed = activities.filter { it.status == "COMPLETED" }
        val overdue = activities.filter { it.status == "OVERDUE" || (it.status == "PENDING" && System.currentTimeMillis() > it.endTimeMillis) }

        // Punctuality: completed on time (allocated time or within +15m)
        val onTimeCount = completed.count {
            val completedTime = it.completedAtMillis ?: it.endTimeMillis
            completedTime <= it.endTimeMillis + 5 * 60 * 1000L
        }
        val punctualityRatio = if (completed.isNotEmpty()) onTimeCount.toFloat() / completed.size else 0.8f
        val punctualityScore = (punctualityRatio * 40).toInt()

        // Habit consistency (25 points max)
        val averageStreak = if (habits.isNotEmpty()) habits.map { it.currentStreak }.average().toFloat() else 5f
        val habitConsistencyScore = ((averageStreak / 14f).coerceIn(0.2f, 1f) * 25).toInt()

        // Overdue penalty
        val overduePenalty = (overdue.size * 8).coerceAtMost(30)

        // Budget discipline (15 points max)
        val monthlyBudget = budgets.firstOrNull { it.period == "MONTHLY" }?.limitAmount ?: 750000.0
        val monthExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val budgetRatio = if (monthlyBudget > 0) (monthExpenses / monthlyBudget).toFloat() else 0.5f
        val budgetScore = if (budgetRatio <= 1.0f) 15 else (15 - ((budgetRatio - 1.0f) * 20).toInt()).coerceAtLeast(0)

        val rawScore = punctualityScore + habitConsistencyScore + budgetScore + 20 - overduePenalty
        val total = rawScore.coerceIn(10, 100)

        val rating = when {
            total >= 90 -> "Commander"
            total >= 80 -> "Exemplary"
            total >= 70 -> "Disciplined"
            total >= 55 -> "Focused"
            else -> "Building Momentum"
        }

        return DisciplineScoreBreakdown(
            totalScore = total,
            punctualityScore = punctualityScore,
            habitConsistencyScore = habitConsistencyScore,
            overduePenalty = overduePenalty,
            budgetScore = budgetScore,
            rating = rating
        )
    }

    // Finances
    fun getAllTransactions(): Flow<List<TransactionEntity>> = financeDao.getAllTransactions()
    suspend fun addTransaction(tx: TransactionEntity) = financeDao.insertTransaction(tx)
    suspend fun deleteTransaction(tx: TransactionEntity) = financeDao.deleteTransaction(tx)

    fun getAllBudgets(): Flow<List<BudgetEntity>> = financeDao.getAllBudgets()
    suspend fun saveBudget(budget: BudgetEntity) = financeDao.insertBudget(budget)
    suspend fun updateBudget(budget: BudgetEntity) = financeDao.updateBudget(budget)

    fun getAllObligations(): Flow<List<FinancialObligationEntity>> = financeDao.getAllObligations()
    suspend fun saveObligation(obligation: FinancialObligationEntity) = financeDao.insertObligation(obligation)
    suspend fun updateObligation(obligation: FinancialObligationEntity) = financeDao.updateObligation(obligation)
    suspend fun deleteObligation(obligation: FinancialObligationEntity) = financeDao.deleteObligation(obligation)

    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>> = financeDao.getAllSavingsGoals()
    suspend fun saveSavingsGoal(goal: SavingsGoalEntity) = financeDao.insertSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = financeDao.updateSavingsGoal(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = financeDao.deleteSavingsGoal(goal)

    // Habits
    fun getAllHabits(): Flow<List<HabitEntity>> = habitDao.getAllHabits()
    suspend fun saveHabit(habit: HabitEntity) = habitDao.insert(habit)
    suspend fun updateHabit(habit: HabitEntity) = habitDao.update(habit)
    suspend fun deleteHabit(habit: HabitEntity) = habitDao.delete(habit)
    suspend fun toggleHabitCompletedToday(habitId: Long) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val historyList = habit.historyDatesCsv.split(",").filter { it.isNotBlank() }.toMutableList()

        val isAlreadyDoneToday = historyList.contains(todayStr)
        val newHistory = if (isAlreadyDoneToday) {
            historyList.remove(todayStr)
            historyList
        } else {
            historyList.add(todayStr)
            historyList
        }

        val newStreak = if (isAlreadyDoneToday) {
            (habit.currentStreak - 1).coerceAtLeast(0)
        } else {
            habit.currentStreak + 1
        }
        val best = maxOf(newStreak, habit.bestStreak)

        habitDao.update(
            habit.copy(
                currentStreak = newStreak,
                bestStreak = best,
                lastCompletedDate = if (!isAlreadyDoneToday) System.currentTimeMillis() else habit.lastCompletedDate,
                historyDatesCsv = newHistory.joinToString(",")
            )
        )
    }

    // Appointments
    fun getAllAppointments(): Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()
    suspend fun saveAppointment(apt: AppointmentEntity) = appointmentDao.insert(apt)
    suspend fun updateAppointment(apt: AppointmentEntity) = appointmentDao.update(apt)
    suspend fun deleteAppointment(apt: AppointmentEntity) = appointmentDao.delete(apt)

    // Categories
    fun getCustomCategories(): Flow<List<CustomCategoryEntity>> = categoryDao.getAllCategories()
    suspend fun addCustomCategory(name: String, colorHex: String) =
        categoryDao.insert(CustomCategoryEntity(name = name, colorHex = colorHex))

    // Backup & Restore (JSON offline export/import)
    suspend fun exportDataToJson(
        activities: List<ActivityEntity>,
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        habits: List<HabitEntity>,
        savings: List<SavingsGoalEntity>
    ): String {
        val root = JSONObject()
        root.put("app", "ZANGA")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val actsArr = JSONArray()
        for (a in activities) {
            val obj = JSONObject()
            obj.put("title", a.title)
            obj.put("description", a.description)
            obj.put("category", a.category)
            obj.put("startTimeMillis", a.startTimeMillis)
            obj.put("endTimeMillis", a.endTimeMillis)
            obj.put("duration", a.allocatedDurationMinutes)
            obj.put("priority", a.priority)
            obj.put("status", a.status)
            actsArr.put(obj)
        }
        root.put("activities", actsArr)

        val txArr = JSONArray()
        for (t in transactions) {
            val obj = JSONObject()
            obj.put("type", t.type)
            obj.put("category", t.category)
            obj.put("amount", t.amount)
            obj.put("currency", t.currency)
            obj.put("dateMillis", t.dateMillis)
            obj.put("paymentMethod", t.paymentMethod)
            obj.put("description", t.description)
            txArr.put(obj)
        }
        root.put("transactions", txArr)

        val habArr = JSONArray()
        for (h in habits) {
            val obj = JSONObject()
            obj.put("title", h.title)
            obj.put("streak", h.currentStreak)
            obj.put("bestStreak", h.bestStreak)
            obj.put("history", h.historyDatesCsv)
            habArr.put(obj)
        }
        root.put("habits", habArr)

        return root.toString(2)
    }

    suspend fun restoreDataFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.optString("app") != "ZANGA") return false

            val activitiesArray = root.optJSONArray("activities")
            if (activitiesArray != null) {
                for (i in 0 until activitiesArray.length()) {
                    val obj = activitiesArray.getJSONObject(i)
                    activityDao.insert(
                        ActivityEntity(
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Personal"),
                            dateMillis = obj.optLong("startTimeMillis", System.currentTimeMillis()),
                            startTimeMillis = obj.optLong("startTimeMillis", System.currentTimeMillis()),
                            endTimeMillis = obj.optLong("endTimeMillis", System.currentTimeMillis() + 3600000L),
                            allocatedDurationMinutes = obj.optInt("duration", 60),
                            priority = obj.optString("priority", "MEDIUM"),
                            status = obj.optString("status", "PENDING")
                        )
                    )
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    // CSV Report Export
    fun generateCsvReport(
        activities: List<ActivityEntity>,
        transactions: List<TransactionEntity>,
        habits: List<HabitEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("ZANGA - Executive Productivity & Life Management Report\n")
        sb.append("Generated,${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")

        sb.append("--- ACTIVITIES ---\n")
        sb.append("Title,Category,Priority,Status,Duration(Min),ActualDuration(Min)\n")
        for (a in activities) {
            sb.append("\"${a.title}\",\"${a.category}\",\"${a.priority}\",\"${a.status}\",${a.allocatedDurationMinutes},${a.actualDurationMinutes}\n")
        }

        sb.append("\n--- FINANCIAL TRANSACTIONS ---\n")
        sb.append("Type,Category,Amount,Currency,PaymentMethod,Description\n")
        for (t in transactions) {
            sb.append("${t.type},\"${t.category}\",${t.amount},${t.currency},\"${t.paymentMethod}\",\"${t.description}\"\n")
        }

        sb.append("\n--- HABITS & DISCIPLINE ---\n")
        sb.append("Habit,CurrentStreak,BestStreak,TargetPerWeek\n")
        for (h in habits) {
            sb.append("\"${h.title}\",${h.currentStreak},${h.bestStreak},${h.targetDaysPerWeek}\n")
        }

        return sb.toString()
    }
}
