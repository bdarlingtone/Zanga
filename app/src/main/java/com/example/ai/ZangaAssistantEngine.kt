package com.example.ai

import com.example.data.entity.ActivityEntity
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BudgetEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.DisciplineScoreBreakdown
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

sealed class AssistantAction {
    data class CreateActivity(val activity: ActivityEntity) : AssistantAction()
    data class LogExpense(val transaction: TransactionEntity) : AssistantAction()
    data class LogIncome(val transaction: TransactionEntity) : AssistantAction()
    data class ScheduleAppointment(val appointment: AppointmentEntity) : AssistantAction()
    data object None : AssistantAction()
}

data class AssistantMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "ZANGA"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val pendingAction: AssistantAction = AssistantAction.None,
    val actionLabel: String? = null,
    val isActionExecuted: Boolean = false
)

object ZangaAssistantEngine {

    fun processQuery(
        query: String,
        activities: List<ActivityEntity>,
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        habits: List<HabitEntity>,
        disciplineScore: DisciplineScoreBreakdown,
        currency: String = "MWK"
    ): AssistantMessage {
        val q = query.trim().lowercase(Locale.ROOT)

        // 1. Incomplete / Pending activities query
        if (q.contains("incomplete") || q.contains("pending") || q.contains("what's left") || q.contains("uncompleted")) {
            val pending = activities.filter { it.status == "PENDING" || it.status == "IN_PROGRESS" }
            if (pending.isEmpty()) {
                return AssistantMessage(
                    sender = "ZANGA",
                    text = "Outstanding discipline! You have zero incomplete activities. All scheduled responsibilities are either completed or ahead of schedule."
                )
            } else {
                val listStr = pending.take(5).mapIndexed { idx, act ->
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(act.startTimeMillis))
                    "${idx + 1}. ${act.title} ($timeStr - ${act.category})"
                }.joinToString("\n")
                return AssistantMessage(
                    sender = "ZANGA",
                    text = "You have ${pending.size} incomplete activities requiring your attention:\n\n$listStr\n\nStay focused and own your time."
                )
            }
        }

        // 2. Financial Spending Query
        if (q.contains("how much") && (q.contains("spent") || q.contains("spend") || q.contains("expense"))) {
            val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val monthlyBudget = budgets.firstOrNull { it.period == "MONTHLY" }?.limitAmount ?: 750000.0
            val remaining = (monthlyBudget - totalExpense).coerceAtLeast(0.0)

            val formattedSpent = String.format(Locale.US, "%,.2f", totalExpense)
            val formattedRemaining = String.format(Locale.US, "%,.2f", remaining)

            return AssistantMessage(
                sender = "ZANGA",
                text = "Financial Audit Summary:\n• Total recorded expenditure: $currency $formattedSpent\n• Remaining monthly budget: $currency $formattedRemaining\n• Budget health: ${if (remaining > 0) "Within limits" else "Warning: Over budget"}"
            )
        }

        // 3. Organize my day / schedule overview
        if (q.contains("organise") || q.contains("organize") || q.contains("my day") || q.contains("daily plan")) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            val endOfDay = startOfDay + 86400000L

            val todayActs = activities.filter { it.startTimeMillis in startOfDay..endOfDay }
                .sortedBy { it.startTimeMillis }

            if (todayActs.isEmpty()) {
                return AssistantMessage(
                    sender = "ZANGA",
                    text = "Your schedule for today is currently wide open. Here is my executive recommendation:\n1. Schedule 2 deep work blocks (90 min each).\n2. Allocate 45 min for physical conditioning.\n3. Dedicate 30 min for review and personal finance.\n\nTell me what you'd like to schedule first!"
                )
            } else {
                val schedule = todayActs.joinToString("\n") { act ->
                    val startStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(act.startTimeMillis))
                    val endStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(act.endTimeMillis))
                    "• $startStr - $endStr: ${act.title} [${act.status}]"
                }
                return AssistantMessage(
                    sender = "ZANGA",
                    text = "Here is your operational blueprint for today:\n\n$schedule\n\nDiscipline Score: ${disciplineScore.totalScore}/100 (${disciplineScore.rating}). Maintain your rhythm!"
                )
            }
        }

        // 4. Discipline score query
        if (q.contains("discipline") || q.contains("productivity score") || q.contains("my score")) {
            return AssistantMessage(
                sender = "ZANGA",
                text = "ZANGA Discipline Analysis:\n• Overall Score: ${disciplineScore.totalScore}/100 (${disciplineScore.rating})\n• Punctuality: ${disciplineScore.punctualityScore}/40\n• Habit Consistency: ${disciplineScore.habitConsistencyScore}/25\n• Budget Adherence: ${disciplineScore.budgetScore}/15\n• Overdue Penalty: -${disciplineScore.overduePenalty}\n\nPunctuality is power. Complete ongoing tasks before their countdown timers expire to maximize your score."
            )
        }

        // 5. Expense Recording: "Record an expense of K15,000 for transport"
        if ((q.contains("expense") || q.contains("spent") || q.contains("record")) && (q.contains("k") || q.contains("mwk") || containsNumber(q))) {
            val amount = extractAmount(q)
            val category = extractExpenseCategory(q)
            val cleanDesc = query.replace(Regex("(?i)record|an|expense|of|k|mwk|[0-9,.]|for"), "").trim()
                .ifEmpty { category }

            val tx = TransactionEntity(
                type = "EXPENSE",
                category = category,
                amount = amount,
                currency = currency,
                dateMillis = System.currentTimeMillis(),
                paymentMethod = "Mobile Money",
                description = if (cleanDesc.isNotBlank()) cleanDesc else "Expense logged via AI Assistant"
            )

            val formattedAmount = String.format(Locale.US, "%,.2f", amount)
            return AssistantMessage(
                sender = "ZANGA",
                text = "I have prepared the expense entry:\n• Amount: $currency $formattedAmount\n• Category: $category\n• Description: ${tx.description}\n\nTap below to confirm and record to your ledger.",
                pendingAction = AssistantAction.LogExpense(tx),
                actionLabel = "Confirm & Save Expense"
            )
        }

        // 6. Meeting Scheduling: "Schedule a meeting with my team on Friday at 2 PM"
        if (q.contains("meeting") || q.contains("appointment")) {
            val title = extractMeetingTitle(query)
            val (startTime, endTime) = extractTimeInterval(query, defaultDurationHours = 1)

            val apt = AppointmentEntity(
                title = title,
                venue = "Meeting Room / Online",
                dateMillis = startTime,
                startTimeMillis = startTime,
                endTimeMillis = endTime,
                participants = "Team",
                description = "Scheduled via ZANGA AI Assistant",
                advanceReminderMinutes = 15
            )

            val sdf = SimpleDateFormat("EEEE, MMM d 'at' h:mm a", Locale.getDefault())
            return AssistantMessage(
                sender = "ZANGA",
                text = "Meeting prepared:\n• Title: $title\n• Time: ${sdf.format(Date(startTime))}\n• Duration: 1 hour\n\nTap below to confirm and add to your schedule.",
                pendingAction = AssistantAction.ScheduleAppointment(apt),
                actionLabel = "Confirm & Schedule Meeting"
            )
        }

        // 7. Activity Scheduling: "Remind me to study C++ tomorrow from 8 AM to 10 AM"
        if (q.contains("remind") || q.contains("study") || q.contains("schedule") || q.contains("activity") || q.contains("task")) {
            val title = extractActivityTitle(query)
            val (startTime, endTime) = extractTimeInterval(query, defaultDurationHours = 2)
            val durationMinutes = ((endTime - startTime) / 60000).coerceAtLeast(15).toInt()
            val category = detectActivityCategory(title)

            val activity = ActivityEntity(
                title = title,
                description = "Created with ZANGA AI Assistant",
                category = category,
                dateMillis = startTime,
                startTimeMillis = startTime,
                endTimeMillis = endTime,
                allocatedDurationMinutes = durationMinutes,
                priority = "HIGH",
                isWarning30mEnabled = true,
                isWarning15mEnabled = true,
                isWarning5mEnabled = true,
                isDeadlineAlarmEnabled = true,
                isAcademicOrProfessional = category == "Education" || category == "Professional",
                subType = if (category == "Education") "STUDY" else "GENERAL"
            )

            val sdf = SimpleDateFormat("EEE, MMM d 'from' h:mm a", Locale.getDefault())
            val endSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return AssistantMessage(
                sender = "ZANGA",
                text = "I have configured your scheduled activity with intelligent alarms:\n• Activity: $title\n• Category: $category\n• Window: ${sdf.format(Date(startTime))} to ${endSdf.format(Date(endTime))} ($durationMinutes min)\n• Alarms: 30m, 15m, 5m warnings & deadline alarm active\n\nTap below to confirm and activate countdown monitoring.",
                pendingAction = AssistantAction.CreateActivity(activity),
                actionLabel = "Confirm & Activate Activity"
            )
        }

        // Default Intelligent Fallback
        return AssistantMessage(
            sender = "ZANGA",
            text = "I am at your service. You can ask me to:\n• \"Remind me to study C++ tomorrow from 8 AM to 10 AM\"\n• \"Record an expense of K15,000 for transport\"\n• \"Schedule a meeting with my team on Friday at 2 PM\"\n• \"How much money did I spend this month?\"\n• \"Show me all my incomplete activities\"\n• \"Help me organise my day\"\n\nTake control. Own your time."
        )
    }

    private fun containsNumber(text: String): Boolean = text.any { it.isDigit() }

    private fun extractAmount(text: String): Double {
        val matcher = Pattern.compile("(?i)(?:k|mwk|\\$)?\\s*([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]+)?)").matcher(text)
        if (matcher.find()) {
            val numStr = matcher.group(1)?.replace(",", "") ?: "0"
            return numStr.toDoubleOrNull() ?: 10000.0
        }
        return 15000.0
    }

    private fun extractExpenseCategory(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            lower.contains("transport") || lower.contains("fuel") || lower.contains("taxi") -> "Transport"
            lower.contains("food") || lower.contains("groceries") || lower.contains("lunch") || lower.contains("dinner") -> "Food & Groceries"
            lower.contains("rent") || lower.contains("housing") -> "Housing"
            lower.contains("airtime") || lower.contains("data") || lower.contains("internet") || lower.contains("bill") -> "Bills & Utilities"
            lower.contains("health") || lower.contains("medical") || lower.contains("gym") -> "Healthcare"
            lower.contains("school") || lower.contains("books") || lower.contains("tuition") -> "Education"
            else -> "Miscellaneous"
        }
    }

    private fun extractActivityTitle(raw: String): String {
        var clean = raw.replace(Regex("(?i)remind me to|schedule an activity|schedule a task|schedule|add task|tomorrow|today|from|to|at|[0-9]+(?:am|pm|:00)"), "")
            .trim()
        if (clean.length < 3) clean = "Study & Execution Session"
        return clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun extractMeetingTitle(raw: String): String {
        var clean = raw.replace(Regex("(?i)schedule a meeting with|schedule meeting with|schedule meeting|on|at|friday|today|tomorrow|[0-9]+(?:am|pm|:00)"), "")
            .trim()
        if (clean.length < 3) clean = "Executive Team Sync"
        return "Meeting: " + clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun detectActivityCategory(title: String): String {
        val lower = title.lowercase(Locale.ROOT)
        return when {
            lower.contains("study") || lower.contains("c++") || lower.contains("exam") || lower.contains("read") || lower.contains("course") -> "Education"
            lower.contains("work") || lower.contains("client") || lower.contains("strategy") || lower.contains("project") -> "Professional"
            lower.contains("business") || lower.contains("revenue") || lower.contains("sales") -> "Business"
            lower.contains("workout") || lower.contains("gym") || lower.contains("run") || lower.contains("meditation") -> "Health & Fitness"
            lower.contains("family") || lower.contains("kids") -> "Family"
            lower.contains("prayer") || lower.contains("church") || lower.contains("spiritual") -> "Religious & Spiritual"
            else -> "Personal"
        }
    }

    private fun extractTimeInterval(text: String, defaultDurationHours: Int = 2): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val lower = text.lowercase(Locale.ROOT)

        if (lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        } else if (lower.contains("friday")) {
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        var startHour = 8
        var startMinute = 0
        var endHour = 10
        var endMinute = 0

        // Regex for "from X AM to Y PM" or "at X PM"
        val fromToMatcher = Pattern.compile("(?i)(?:from\\s+)?([0-9]{1,2})(?::([0-9]{2}))?\\s*(am|pm)\\s+to\\s+([0-9]{1,2})(?::([0-9]{2}))?\\s*(am|pm)").matcher(text)
        if (fromToMatcher.find()) {
            var h1 = fromToMatcher.group(1)?.toIntOrNull() ?: 8
            val m1 = fromToMatcher.group(2)?.toIntOrNull() ?: 0
            val p1 = fromToMatcher.group(3)?.lowercase() ?: "am"

            var h2 = fromToMatcher.group(4)?.toIntOrNull() ?: 10
            val m2 = fromToMatcher.group(5)?.toIntOrNull() ?: 0
            val p2 = fromToMatcher.group(6)?.lowercase() ?: "am"

            if (p1 == "pm" && h1 < 12) h1 += 12
            if (p1 == "am" && h1 == 12) h1 = 0
            if (p2 == "pm" && h2 < 12) h2 += 12
            if (p2 == "am" && h2 == 12) h2 = 0

            startHour = h1
            startMinute = m1
            endHour = h2
            endMinute = m2
        } else {
            val atMatcher = Pattern.compile("(?i)at\\s+([0-9]{1,2})(?::([0-9]{2}))?\\s*(am|pm)").matcher(text)
            if (atMatcher.find()) {
                var h = atMatcher.group(1)?.toIntOrNull() ?: 14
                val m = atMatcher.group(2)?.toIntOrNull() ?: 0
                val p = atMatcher.group(3)?.lowercase() ?: "pm"
                if (p == "pm" && h < 12) h += 12
                if (p == "am" && h == 12) h = 0

                startHour = h
                startMinute = m
                endHour = (h + defaultDurationHours).coerceAtMost(23)
                endMinute = m
            }
        }

        cal.set(Calendar.HOUR_OF_DAY, startHour)
        cal.set(Calendar.MINUTE, startMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, endHour)
        cal.set(Calendar.MINUTE, endMinute)
        val endTime = if (cal.timeInMillis <= startTime) startTime + defaultDurationHours * 3600000L else cal.timeInMillis

        return Pair(startTime, endTime)
    }
}
