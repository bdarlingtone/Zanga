package com.example

import com.example.ai.AssistantAction
import com.example.ai.ZangaAssistantEngine
import com.example.data.entity.ActivityEntity
import com.example.data.model.DisciplineScoreBreakdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun assistantParsesExpenseQueryCorrectly() {
        val query = "Record an expense of K15,000 for transport"
        val response = ZangaAssistantEngine.processQuery(
            query = query,
            activities = emptyList(),
            transactions = emptyList(),
            budgets = emptyList(),
            habits = emptyList(),
            disciplineScore = DisciplineScoreBreakdown(85, 35, 25, 0, 25, "Disciplined"),
            currency = "MWK"
        )

        assertTrue(response.pendingAction is AssistantAction.LogExpense)
        val expenseAction = response.pendingAction as AssistantAction.LogExpense
        assertEquals(15000.0, expenseAction.transaction.amount, 0.01)
        assertEquals("Transport", expenseAction.transaction.category)
    }

    @Test
    fun assistantParsesActivitySchedulingQuery() {
        val query = "Remind me to study C++ tomorrow from 8 AM to 10 AM"
        val response = ZangaAssistantEngine.processQuery(
            query = query,
            activities = emptyList(),
            transactions = emptyList(),
            budgets = emptyList(),
            habits = emptyList(),
            disciplineScore = DisciplineScoreBreakdown(85, 35, 25, 0, 25, "Disciplined"),
            currency = "MWK"
        )

        assertTrue(response.pendingAction is AssistantAction.CreateActivity)
        val actAction = response.pendingAction as AssistantAction.CreateActivity
        assertTrue(actAction.activity.title.contains("C++", ignoreCase = true))
        assertEquals("Education", actAction.activity.category)
        assertTrue(actAction.activity.allocatedDurationMinutes >= 60)
    }

    @Test
    fun assistantAnswersIncompleteActivitiesZeroState() {
        val query = "Show me all my incomplete activities"
        val response = ZangaAssistantEngine.processQuery(
            query = query,
            activities = emptyList(),
            transactions = emptyList(),
            budgets = emptyList(),
            habits = emptyList(),
            disciplineScore = DisciplineScoreBreakdown(85, 35, 25, 0, 25, "Disciplined"),
            currency = "MWK"
        )

        assertTrue(response.text.contains("zero incomplete activities"))
    }
}
