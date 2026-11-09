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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.components.DisciplineScoreCard
import com.example.ui.components.ReportExportDialog
import com.example.ui.theme.ZangaDeepBlue
import com.example.ui.theme.ZangaExpired
import com.example.ui.theme.ZangaPrimaryBlue
import com.example.ui.theme.ZangaSecondaryBlue
import com.example.ui.theme.ZangaSuccess
import com.example.ui.theme.ZangaWarning
import com.example.ui.theme.ZangaWhite
import com.example.viewmodel.ZangaViewModel
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: ZangaViewModel
) {
    val activities by viewModel.allActivities.collectAsState()
    val score by viewModel.disciplineScore.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var generatedReportText by remember { mutableStateOf("") }

    val completed = activities.filter { it.status == "COMPLETED" }
    val overdue = activities.filter { it.status == "OVERDUE" || (it.status == "PENDING" && System.currentTimeMillis() > it.endTimeMillis) }
    val totalTimePlanned = activities.sumOf { it.allocatedDurationMinutes }
    val totalTimeActual = completed.sumOf { it.actualDurationMinutes }

    // Category breakdown
    val categoryCounts = activities.groupBy { it.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Discipline & Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Punctuality, consistency, and time utilization audit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    viewModel.exportCsvReport { csv ->
                        generatedReportText = csv
                        showReportDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZangaDeepBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("export_report_btn")
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export", tint = ZangaWhite, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Report", color = ZangaWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            // Main Discipline Gauge
            item {
                DisciplineScoreCard(score = score)
            }

            // Calculation Breakdown Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = "Punctuality", tint = ZangaPrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DISCIPLINE FORMULA AUDIT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AnalyticsScoreRow(
                            label = "On-Time Completion & Punctuality",
                            scoreText = "${score.punctualityScore} / 40",
                            ratio = score.punctualityScore / 40f,
                            color = ZangaPrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        AnalyticsScoreRow(
                            label = "Daily Habit Streaks & Routines",
                            scoreText = "${score.habitConsistencyScore} / 25",
                            ratio = score.habitConsistencyScore / 25f,
                            color = ZangaSecondaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        AnalyticsScoreRow(
                            label = "Budget Discipline & Fiscal Control",
                            scoreText = "${score.budgetScore} / 15",
                            ratio = score.budgetScore / 15f,
                            color = ZangaSuccess
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        AnalyticsScoreRow(
                            label = "Overdue Expirations Deduction",
                            scoreText = "-${score.overduePenalty} pts",
                            ratio = (score.overduePenalty / 30f).coerceIn(0f, 1f),
                            color = ZangaExpired
                        )
                    }
                }
            }

            // Time Utilization (Planned vs Actual)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Time", tint = ZangaSecondaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TIME ALLOCATION VS EXECUTION",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Planned Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${totalTimePlanned / 60}h ${totalTimePlanned % 60}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Actual Executed Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${totalTimeActual / 60}h ${totalTimeActual % 60}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ZangaSecondaryBlue)
                            }
                        }
                    }
                }
            }

            // Category Distribution
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PieChart, contentDescription = "Category", tint = ZangaPrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CATEGORY COMMITMENT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        categoryCounts.forEach { (cat, list) ->
                            val percent = (list.size.toFloat() / activities.size.coerceAtLeast(1) * 100).toInt()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "${list.size} activities ($percent%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ZangaSecondaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        ReportExportDialog(
            reportText = generatedReportText,
            onDismiss = { showReportDialog = false }
        )
    }
}

@Composable
fun AnalyticsScoreRow(
    label: String,
    scoreText: String,
    ratio: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(scoreText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )
    }
}
