package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BudgetEntity
import com.example.data.entity.FinancialObligationEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.components.ZangaCard
import com.example.ui.theme.ZangaDeepBlue
import com.example.ui.theme.ZangaExpired
import com.example.ui.theme.ZangaPrimaryBlue
import com.example.ui.theme.ZangaSecondaryBlue
import com.example.ui.theme.ZangaSuccess
import com.example.ui.theme.ZangaWarning
import com.example.ui.theme.ZangaWhite
import com.example.viewmodel.ZangaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceScreen(
    viewModel: ZangaViewModel,
    onRequestAddTransaction: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val budgets by viewModel.allBudgets.collectAsState()
    val obligations by viewModel.allObligations.collectAsState()
    val savingsGoals by viewModel.allSavingsGoals.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Budgets", "Transactions", "Obligations", "Savings")

    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRequestAddTransaction,
                containerColor = ZangaPrimaryBlue,
                contentColor = ZangaWhite,
                shape = CircleShape,
                modifier = Modifier.testTag("finance_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Financial Command Center",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Default currency: $currency (Malawi Kwacha)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Executive Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, ZangaSecondaryBlue.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = ZangaDeepBlue)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NET OPERATIONAL BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZangaSecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currency ${String.format(Locale.US, "%,.2f", netBalance)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = ZangaWhite
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ZangaSuccess.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Income", tint = ZangaSuccess, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Total Income", style = MaterialTheme.typography.labelSmall, color = ZangaWhite.copy(alpha = 0.7f))
                                Text(
                                    "$currency ${String.format(Locale.US, "%,.0f", totalIncome)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ZangaWhite
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ZangaExpired.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Expense", tint = ZangaExpired, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Total Expenses", style = MaterialTheme.typography.labelSmall, color = ZangaWhite.copy(alpha = 0.7f))
                                Text(
                                    "$currency ${String.format(Locale.US, "%,.0f", totalExpense)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ZangaWhite
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = ZangaPrimaryBlue
            ) {
                tabs.forEachIndexed { index, name ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = name,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) ZangaPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            when (selectedTabIndex) {
                0 -> BudgetTabContent(budgets, totalExpense, currency)
                1 -> TransactionsTabContent(transactions, currency, onDelete = { viewModel.deleteTransaction(it) })
                2 -> ObligationsTabContent(
                    obligations,
                    currency,
                    onToggleSettled = { viewModel.toggleObligationSettled(it) },
                    onDelete = { viewModel.deleteObligation(it) }
                )
                3 -> SavingsTabContent(
                    savingsGoals,
                    currency,
                    onDeposit = { goal, amt -> viewModel.addSavingsDeposit(goal, amt) },
                    onDelete = { viewModel.deleteSavingsGoal(it) }
                )
            }
        }
    }
}

@Composable
fun BudgetTabContent(
    budgets: List<BudgetEntity>,
    totalExpense: Double,
    currency: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
    ) {
        items(budgets) { budget ->
            val spent = if (budget.period == "DAILY") totalExpense.coerceAtMost(budget.limitAmount * 0.4) else totalExpense
            val ratio = (spent / budget.limitAmount).toFloat().coerceIn(0f, 1.5f)
            val isOver = spent > budget.limitAmount
            val remaining = (budget.limitAmount - spent).coerceAtLeast(0.0)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (isOver) ZangaExpired else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${budget.period} BUDGET",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isOver) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ZangaExpired.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("OVERSPENDING ALERT", style = MaterialTheme.typography.labelSmall, color = ZangaExpired, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "${(ratio * 100).toInt()}% utilized",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZangaSecondaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { ratio.coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isOver) ZangaExpired else if (ratio > 0.85f) ZangaWarning else ZangaPrimaryBlue,
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Limit: $currency ${String.format(Locale.US, "%,.0f", budget.limitAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Spent: $currency ${String.format(Locale.US, "%,.0f", spent)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "$currency ${String.format(Locale.US, "%,.0f", remaining)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (remaining > 0) ZangaSecondaryBlue else ZangaExpired
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsTabContent(
    transactions: List<TransactionEntity>,
    currency: String,
    onDelete: (TransactionEntity) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transactions logged yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            items(transactions, key = { it.id }) { tx ->
                val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                val isExpense = tx.type == "EXPENSE"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isExpense) ZangaExpired.copy(alpha = 0.15f) else ZangaSuccess.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = tx.type,
                                    tint = if (isExpense) ZangaExpired else ZangaSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tx.category,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${tx.paymentMethod} • ${sdf.format(Date(tx.dateMillis))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                if (tx.description.isNotBlank() && tx.description != tx.category) {
                                    Text(
                                        text = tx.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (isExpense) "-" else "+"}$currency ${String.format(Locale.US, "%,.2f", tx.amount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) ZangaExpired else ZangaSuccess
                            )
                            IconButton(onClick = { onDelete(tx) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ObligationsTabContent(
    obligations: List<FinancialObligationEntity>,
    currency: String,
    onToggleSettled: (FinancialObligationEntity) -> Unit,
    onDelete: (FinancialObligationEntity) -> Unit
) {
    if (obligations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No debts or lent funds recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            items(obligations, key = { it.id }) { ob ->
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val isLent = ob.type == "LENT_TO_OTHERS"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLent) "MONEY LENT TO" else "DEBT OWED TO",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLent) ZangaSecondaryBlue else ZangaWarning,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (ob.isSettled) ZangaSuccess.copy(alpha = 0.2f) else ZangaWarning.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (ob.isSettled) "SETTLED" else "PENDING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (ob.isSettled) ZangaSuccess else ZangaWarning,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ob.personName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Amount: $currency ${String.format(Locale.US, "%,.2f", ob.amount)} • Due: ${sdf.format(Date(ob.dueDateMillis))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onToggleSettled(ob) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (ob.isSettled) Color.Gray else ZangaPrimaryBlue
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (ob.isSettled) "Mark Unsettled" else "Mark as Settled", fontSize = 12.sp, color = ZangaWhite)
                            }

                            IconButton(onClick = { onDelete(ob) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsTabContent(
    goals: List<SavingsGoalEntity>,
    currency: String,
    onDeposit: (SavingsGoalEntity, Double) -> Unit,
    onDelete: (SavingsGoalEntity) -> Unit
) {
    if (goals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No savings targets created yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            items(goals, key = { it.id }) { goal ->
                val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ZangaSecondaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ZangaSecondaryBlue,
                            trackColor = Color.Gray.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Saved: $currency ${String.format(Locale.US, "%,.0f", goal.currentAmount)} / $currency ${String.format(Locale.US, "%,.0f", goal.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onDeposit(goal, 25000.0) },
                                colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Deposit $currency 25k", fontSize = 11.sp, color = ZangaWhite)
                            }

                            IconButton(onClick = { onDelete(goal) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
