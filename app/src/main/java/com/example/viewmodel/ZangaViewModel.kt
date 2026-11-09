package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AssistantAction
import com.example.ai.AssistantMessage
import com.example.ai.ZangaAssistantEngine
import com.example.data.entity.ActivityEntity
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BudgetEntity
import com.example.data.entity.FinancialObligationEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.DisciplineScoreBreakdown
import com.example.data.model.NextActivityInfo
import com.example.data.repository.ScheduleConflict
import com.example.data.repository.ZangaRepository
import com.example.receiver.ActiveAlarmAlert
import com.example.receiver.AlarmEventBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ZangaScreen(val title: String) {
    DASHBOARD("Dashboard"),
    ACTIVITIES("Activities"),
    TIMELINE("Timeline"),
    FINANCE("Finances"),
    FOCUS_MODE("Focus Mode"),
    HABITS("Routines & Habits"),
    APPOINTMENTS("Appointments"),
    ANALYTICS("Discipline & Analytics"),
    ASSISTANT("ZANGA AI"),
    SETTINGS("Settings")
}

class ZangaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ZangaRepository(application.applicationContext)

    // Current Screen
    private val _currentScreen = MutableStateFlow(ZangaScreen.DASHBOARD)
    val currentScreen: StateFlow<ZangaScreen> = _currentScreen.asStateFlow()

    // Navigation Stack for back handling
    private val screenStack = mutableListOf(ZangaScreen.DASHBOARD)

    fun navigateTo(screen: ZangaScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    // Settings
    val isDarkMode = MutableStateFlow(true)
    val userCurrency = MutableStateFlow("MWK")
    val userName = MutableStateFlow("Alex")
    val defaultDurationMinutes = MutableStateFlow(60)
    val soundEnabled = MutableStateFlow(true)
    val vibrationEnabled = MutableStateFlow(true)
    val isAppLocked = MutableStateFlow(false)
    val isPinRequired = MutableStateFlow(false)
    val appPin = MutableStateFlow("1234")

    // Flows from DB
    val allActivities: StateFlow<List<ActivityEntity>> = repository.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val academicActivities: StateFlow<List<ActivityEntity>> = repository.getAcademicAndProfessional()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextActivityInfo: StateFlow<NextActivityInfo?> = repository.observeNextActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allObligations: StateFlow<List<FinancialObligationEntity>> = repository.getAllObligations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.getAllSavingsGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitEntity>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<AppointmentEntity>> = repository.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customCategories = repository.getCustomCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Schedule Conflicts
    val scheduleConflicts: StateFlow<List<ScheduleConflict>> = allActivities.combine(allAppointments) { acts, _ ->
        repository.detectConflicts(acts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Discipline Score
    val disciplineScore: StateFlow<DisciplineScoreBreakdown> = combine(
        allActivities,
        allHabits,
        allTransactions,
        allBudgets
    ) { acts, habits, txs, budgets ->
        repository.calculateDisciplineScore(acts, habits, txs, budgets)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DisciplineScoreBreakdown(85, 35, 25, 0, 25, "Disciplined")
    )

    // In-App Alarm Alert (Modal dialog when alarm fires or app is open)
    private val _activeAlarmAlert = MutableStateFlow<ActiveAlarmAlert?>(null)
    val activeAlarmAlert: StateFlow<ActiveAlarmAlert?> = _activeAlarmAlert.asStateFlow()

    fun dismissAlarmAlert() {
        _activeAlarmAlert.value = null
    }

    init {
        // Collect real-time alarm alerts from AlarmReceiver
        viewModelScope.launch {
            AlarmEventBus.alarmEvents.collect { alert ->
                _activeAlarmAlert.value = alert
            }
        }
    }

    // Activity Actions
    fun createActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.saveActivity(activity)
        }
    }

    fun updateActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
        }
    }

    fun completeActivity(activityId: Long) {
        viewModelScope.launch {
            repository.completeActivity(activityId)
            if (_activeAlarmAlert.value?.activityId == activityId) {
                _activeAlarmAlert.value = null
            }
        }
    }

    fun extendActivity(activityId: Long, additionalMinutes: Int) {
        viewModelScope.launch {
            repository.extendActivity(activityId, additionalMinutes)
            if (_activeAlarmAlert.value?.activityId == activityId) {
                _activeAlarmAlert.value = null
            }
        }
    }

    fun postponeActivity(activityId: Long, postponeMinutes: Int) {
        viewModelScope.launch {
            repository.postponeActivity(activityId, postponeMinutes)
            if (_activeAlarmAlert.value?.activityId == activityId) {
                _activeAlarmAlert.value = null
            }
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
            if (_activeAlarmAlert.value?.activityId == activity.id) {
                _activeAlarmAlert.value = null
            }
        }
    }

    // Finance Actions
    fun addTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun saveBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.saveBudget(budget)
        }
    }

    fun saveObligation(obligation: FinancialObligationEntity) {
        viewModelScope.launch {
            repository.saveObligation(obligation)
        }
    }

    fun toggleObligationSettled(obligation: FinancialObligationEntity) {
        viewModelScope.launch {
            repository.updateObligation(obligation.copy(isSettled = !obligation.isSettled))
        }
    }

    fun deleteObligation(obligation: FinancialObligationEntity) {
        viewModelScope.launch {
            repository.deleteObligation(obligation)
        }
    }

    fun saveSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.saveSavingsGoal(goal)
        }
    }

    fun addSavingsDeposit(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            repository.updateSavingsGoal(goal.copy(currentAmount = goal.currentAmount + amount))
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    // Habits Actions
    fun toggleHabitToday(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitCompletedToday(habitId)
        }
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.saveHabit(habit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Appointment Actions
    fun saveAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.saveAppointment(appointment)
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }

    // Categories
    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.addCustomCategory(name, colorHex)
        }
    }

    // Focus Mode
    private val _focusActivity = MutableStateFlow<ActivityEntity?>(null)
    val focusActivity: StateFlow<ActivityEntity?> = _focusActivity.asStateFlow()

    private val _focusRemainingSeconds = MutableStateFlow(0)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _focusElapsedSeconds = MutableStateFlow(0)
    val focusElapsedSeconds: StateFlow<Int> = _focusElapsedSeconds.asStateFlow()

    private val _isFocusRunning = MutableStateFlow(false)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    private var focusTimerJob: Job? = null

    fun startFocusSession(activity: ActivityEntity) {
        _focusActivity.value = activity
        val totalSecs = activity.allocatedDurationMinutes * 60
        _focusRemainingSeconds.value = totalSecs
        _focusElapsedSeconds.value = 0
        _isFocusRunning.value = true
        startFocusTimer()
        navigateTo(ZangaScreen.FOCUS_MODE)
    }

    fun toggleFocusTimer() {
        if (_isFocusRunning.value) {
            _isFocusRunning.value = false
            focusTimerJob?.cancel()
        } else {
            _isFocusRunning.value = true
            startFocusTimer()
        }
    }

    fun completeFocusSession() {
        val act = _focusActivity.value ?: return
        completeActivity(act.id)
        _isFocusRunning.value = false
        focusTimerJob?.cancel()
        _focusActivity.value = null
        navigateTo(ZangaScreen.DASHBOARD)
    }

    private fun startFocusTimer() {
        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (_isFocusRunning.value && _focusRemainingSeconds.value > 0) {
                delay(1000)
                _focusRemainingSeconds.value -= 1
                _focusElapsedSeconds.value += 1
            }
            if (_focusRemainingSeconds.value <= 0) {
                _isFocusRunning.value = false
                val act = _focusActivity.value
                if (act != null) {
                    _activeAlarmAlert.value = ActiveAlarmAlert(
                        activityId = act.id,
                        title = act.title,
                        isExpired = true,
                        minutesRemaining = 0
                    )
                }
            }
        }
    }

    // AI Assistant
    private val _assistantMessages = MutableStateFlow<List<AssistantMessage>>(
        listOf(
            AssistantMessage(
                sender = "ZANGA",
                text = "Greetings, Commander. I am your ZANGA Personal Assistant. I can schedule activities with intelligent alarms, record financial transactions, check your schedule, or audit your monthly spending. What would you like to accomplish?"
            )
        )
    )
    val assistantMessages: StateFlow<List<AssistantMessage>> = _assistantMessages.asStateFlow()

    fun sendAssistantQuery(query: String) {
        if (query.isBlank()) return
        val userMsg = AssistantMessage(sender = "USER", text = query)
        val currentList = _assistantMessages.value.toMutableList()
        currentList.add(userMsg)
        _assistantMessages.value = currentList

        viewModelScope.launch {
            delay(350) // simulate natural cognitive processing
            val botReply = ZangaAssistantEngine.processQuery(
                query = query,
                activities = allActivities.value,
                transactions = allTransactions.value,
                budgets = allBudgets.value,
                habits = allHabits.value,
                disciplineScore = disciplineScore.value,
                currency = userCurrency.value
            )
            val updated = _assistantMessages.value.toMutableList()
            updated.add(botReply)
            _assistantMessages.value = updated
        }
    }

    fun executeAssistantAction(messageId: String) {
        val msg = _assistantMessages.value.firstOrNull { it.id == messageId } ?: return
        when (val action = msg.pendingAction) {
            is AssistantAction.CreateActivity -> {
                createActivity(action.activity)
            }
            is AssistantAction.LogExpense -> {
                addTransaction(action.transaction)
            }
            is AssistantAction.LogIncome -> {
                addTransaction(action.transaction)
            }
            is AssistantAction.ScheduleAppointment -> {
                saveAppointment(action.appointment)
            }
            AssistantAction.None -> {}
        }

        // Mark as executed in message list
        _assistantMessages.value = _assistantMessages.value.map {
            if (it.id == messageId) it.copy(isActionExecuted = true) else it
        }
    }

    // Data Export / Backup
    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataToJson(
                allActivities.value,
                allTransactions.value,
                allBudgets.value,
                allHabits.value,
                allSavingsGoals.value
            )
            onResult(json)
        }
    }

    fun restoreBackupJson(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreDataFromJson(json)
            onComplete(success)
        }
    }

    fun exportCsvReport(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.generateCsvReport(
                allActivities.value,
                allTransactions.value,
                allHabits.value
            )
            onResult(csv)
        }
    }
}
