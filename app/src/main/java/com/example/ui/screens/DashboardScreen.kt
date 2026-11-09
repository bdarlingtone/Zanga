package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ActivityEntity
import com.example.ui.components.DisciplineScoreCard
import com.example.ui.components.NextActivityCountdownBanner
import com.example.ui.components.ScheduleConflictBanner
import com.example.ui.components.ZangaCard
import com.example.ui.theme.ZangaDeepBlue
import com.example.ui.theme.ZangaExpired
import com.example.ui.theme.ZangaPrimaryBlue
import com.example.ui.theme.ZangaSecondaryBlue
import com.example.ui.theme.ZangaWarning
import com.example.ui.theme.ZangaWhite
import com.example.viewmodel.ZangaScreen
import com.example.viewmodel.ZangaViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ZangaViewModel,
    onNavigate: (ZangaScreen) -> Unit,
    onRequestAddActivity: () -> Unit
) {
    val activities by viewModel.allActivities.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val budgets by viewModel.allBudgets.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val nextInfo by viewModel.nextActivityInfo.collectAsState()
    val disciplineScore by viewModel.disciplineScore.collectAsState()
    val conflicts by viewModel.scheduleConflicts.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()
    val userName by viewModel.userName.collectAsState()

    // Live clock
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Date()
        }
    }

    val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    val cal = Calendar.getInstance().apply { time = currentTime }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    // Counts for today
    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val endOfDay = startOfDay + 86400000L

    val todayActivities = activities.filter { it.startTimeMillis in startOfDay..endOfDay }
    val completedCount = todayActivities.count { it.status == "COMPLETED" }
    val pendingCount = todayActivities.count { it.status == "PENDING" || it.status == "IN_PROGRESS" }
    val overdueCount = todayActivities.count { it.status == "OVERDUE" || (it.status == "PENDING" && currentTime.time > it.endTimeMillis) }

    // Today's Expense
    val todaySpent = transactions.filter { it.type == "EXPENSE" && it.dateMillis in startOfDay..endOfDay }
        .sumOf { it.amount }
    val dailyBudget = budgets.firstOrNull { it.period == "DAILY" }?.limitAmount ?: 25000.0
    val remainingBudget = (dailyBudget - todaySpent).coerceAtLeast(0.0)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRequestAddActivity,
                containerColor = ZangaPrimaryBlue,
                contentColor = ZangaWhite,
                shape = CircleShape,
                modifier = Modifier.testTag("quick_add_activity_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Brand Header with Live Clock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ZANGA",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = ZangaPrimaryBlue,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ZangaSecondaryBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PREMIUM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZangaSecondaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Take Control. Own Your Time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Live digital clock pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZangaDeepBlue)
                            .border(1.dp, ZangaSecondaryBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = timeFormat.format(currentTime),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = ZangaWhite
                            )
                            Text(
                                text = "LIVE CLOCK",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = ZangaSecondaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Greeting & Date
            item {
                Column {
                    Text(
                        text = "$greeting, $userName",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateFormat.format(currentTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Conflict Warning if present
            if (conflicts.isNotEmpty()) {
                item {
                    val c = conflicts.first()
                    ScheduleConflictBanner(
                        activity1Title = c.activity1.title,
                        activity2Title = c.activity2.title,
                        suggestion = c.suggestedAlternativeSlot,
                        onDismiss = {}
                    )
                }
            }

            // Signature Countdown Banner
            item {
                NextActivityCountdownBanner(
                    nextInfo = nextInfo,
                    onEnterFocus = {
                        val activeAct = activities.firstOrNull { it.id == nextInfo?.id }
                        if (activeAct != null) {
                            viewModel.startFocusSession(activeAct)
                        } else {
                            onNavigate(ZangaScreen.FOCUS_MODE)
                        }
                    },
                    onComplete = {
                        nextInfo?.let { viewModel.completeActivity(it.id) }
                    }
                )
            }

            // Discipline Score Gauge
            item {
                DisciplineScoreCard(score = disciplineScore)
            }

            // Today's Activity Pulse Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Completed",
                        count = completedCount.toString(),
                        accentColor = ZangaSecondaryBlue,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Pending",
                        count = pendingCount.toString(),
                        accentColor = ZangaPrimaryBlue,
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Overdue",
                        count = overdueCount.toString(),
                        accentColor = if (overdueCount > 0) ZangaExpired else Color.Gray,
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Financial Overview Card
            item {
                ZangaCard(
                    onClick = { onNavigate(ZangaScreen.FINANCE) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ZangaPrimaryBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = ZangaPrimaryBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FINANCIAL SUMMARY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZangaSecondaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Daily Budget Health",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Text(
                            text = "View Ledger →",
                            style = MaterialTheme.typography.labelMedium,
                            color = ZangaSecondaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Spent Today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currency ${String.format(Locale.US, "%,.0f", todaySpent)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Remaining Daily",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currency ${String.format(Locale.US, "%,.0f", remainingBudget)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBudget > 0) ZangaSecondaryBlue else ZangaExpired
                            )
                        }
                    }
                }
            }

            // Fast Executive Command Grid
            item {
                Text(
                    text = "EXECUTIVE ECOSYSTEM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CommandGridTile(
                        title = "Focus Mode",
                        subtitle = "Immersive countdown",
                        icon = Icons.Default.HourglassTop,
                        onClick = { onNavigate(ZangaScreen.FOCUS_MODE) },
                        modifier = Modifier.weight(1f)
                    )
                    CommandGridTile(
                        title = "ZANGA AI",
                        subtitle = "Intelligent assistant",
                        icon = Icons.Default.AutoAwesome,
                        onClick = { onNavigate(ZangaScreen.ASSISTANT) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CommandGridTile(
                        title = "24h Timeline",
                        subtitle = "Schedule visualization",
                        icon = Icons.Default.Timeline,
                        onClick = { onNavigate(ZangaScreen.TIMELINE) },
                        modifier = Modifier.weight(1f)
                    )
                    CommandGridTile(
                        title = "Habits & Streaks",
                        subtitle = "${habits.size} active routines",
                        icon = Icons.Default.Repeat,
                        onClick = { onNavigate(ZangaScreen.HABITS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    count: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CommandGridTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ZangaPrimaryBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = ZangaPrimaryBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
