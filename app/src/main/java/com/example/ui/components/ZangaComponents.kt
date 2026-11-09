package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DisciplineScoreBreakdown
import com.example.data.model.NextActivityInfo
import com.example.receiver.ActiveAlarmAlert
import com.example.ui.theme.ZangaDeepBlue
import com.example.ui.theme.ZangaExpired
import com.example.ui.theme.ZangaPrimaryBlue
import com.example.ui.theme.ZangaPureBlack
import com.example.ui.theme.ZangaSecondaryBlue
import com.example.ui.theme.ZangaWarning
import com.example.ui.theme.ZangaWhite
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ZangaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun DisciplineScoreCard(
    score: DisciplineScoreBreakdown,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score.totalScore / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "score_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, ZangaSecondaryBlue.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = ZangaDeepBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DISCIPLINE SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZangaSecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ZangaPrimaryBlue.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = score.rating.uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.labelSmall,
                            color = ZangaWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${score.totalScore}",
                    style = MaterialTheme.typography.displayMedium,
                    color = ZangaWhite,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Punctuality: ${score.punctualityScore}/40 • Habits: ${score.habitConsistencyScore}/25",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZangaWhite.copy(alpha = 0.75f)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp)
            ) {
                Canvas(modifier = Modifier.size(70.dp)) {
                    // Background track
                    drawCircle(
                        color = Color(0xFF162542),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    // Active Score Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(ZangaPrimaryBlue, ZangaSecondaryBlue, ZangaWhite)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${score.totalScore}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZangaWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NextActivityCountdownBanner(
    nextInfo: NextActivityInfo?,
    onEnterFocus: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (nextInfo == null) {
        ZangaCard(modifier = modifier) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ZangaPrimaryBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "All Clear",
                        tint = ZangaPrimaryBlue
                    )
                }
                Column {
                    Text(
                        text = "Schedule Clear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "No pending activities right now. Maintain discipline!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    // Live ticking countdown
    var currentMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentMillis = System.currentTimeMillis()
        }
    }

    val remainingMs = if (nextInfo.isRunning) {
        (nextInfo.endTimeMillis - currentMillis).coerceAtLeast(0)
    } else {
        (nextInfo.startTimeMillis - currentMillis).coerceAtLeast(0)
    }

    val hours = (remainingMs / (1000 * 60 * 60)).toInt()
    val minutes = ((remainingMs / (1000 * 60)) % 60).toInt()
    val seconds = ((remainingMs / 1000) % 60).toInt()
    val countdownText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    val isUrgent = nextInfo.isRunning && remainingMs < 15 * 60 * 1000L
    val isExpired = nextInfo.isRunning && remainingMs <= 0

    val bannerBg = if (isExpired) ZangaExpired.copy(alpha = 0.2f) else ZangaDeepBlue
    val borderCol = if (isExpired) ZangaExpired else if (isUrgent) ZangaWarning else ZangaSecondaryBlue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, borderCol, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = bannerBg)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isExpired) Icons.Default.Alarm else Icons.Default.HourglassBottom,
                        contentDescription = "Countdown",
                        tint = if (isExpired) ZangaExpired else ZangaSecondaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExpired) "TIME EXPIRED" else if (nextInfo.isRunning) "ACTIVE COUNTDOWN" else "NEXT UPCOMING",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isExpired) ZangaExpired else ZangaSecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ZangaPrimaryBlue.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = nextInfo.category.uppercase(Locale.ROOT),
                        style = MaterialTheme.typography.labelSmall,
                        color = ZangaWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = nextInfo.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ZangaWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isExpired) "TIME IS UP!" else countdownText,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isExpired) ZangaExpired else ZangaWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (nextInfo.isRunning) "Time remaining until deadline alarm" else "Time until activity begins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZangaWhite.copy(alpha = 0.7f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onEnterFocus,
                        colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("focus_mode_quick_btn")
                    ) {
                        Text("Focus", fontWeight = FontWeight.Bold, color = ZangaWhite)
                    }
                    OutlinedButton(
                        onClick = onComplete,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZangaWhite.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("complete_activity_quick_btn")
                    ) {
                        Text("Done", color = ZangaWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleConflictBanner(
    activity1Title: String,
    activity2Title: String,
    suggestion: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, ZangaWarning.copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF231A05))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Conflict",
                tint = ZangaWarning,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Schedule Conflict Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZangaWarning
                )
                Text(
                    text = "\"$activity1Title\" overlaps with \"$activity2Title\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZangaWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Suggestion: $suggestion",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZangaWhite.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AlarmExpirationModal(
    alert: ActiveAlarmAlert?,
    onComplete: (Long) -> Unit,
    onExtend: (Long, Int) -> Unit,
    onReschedule: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(visible = alert != null) {
        if (alert == null) return@AnimatedVisibility

        val isExpired = alert.isExpired
        val dialogBg = ZangaDeepBlue
        val titleText = if (isExpired) "TIME IS UP!" else "COUNTDOWN WARNING"
        val subtitle = if (isExpired) {
            "Your allocated time for \"${alert.title}\" has expired."
        } else {
            "${alert.minutesRemaining} minutes remaining for \"${alert.title}\"."
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .border(2.dp, if (isExpired) ZangaExpired else ZangaWarning, RoundedCornerShape(20.dp))
                .testTag("alarm_expiration_dialog"),
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isExpired) ZangaExpired.copy(alpha = 0.2f) else ZangaWarning.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpired) Icons.Default.NotificationsActive else Icons.Default.Alarm,
                        contentDescription = "Alarm",
                        tint = if (isExpired) ZangaExpired else ZangaWarning,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isExpired) ZangaExpired else ZangaWarning,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ZangaWhite,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ZANGA Intelligent Time Monitor",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZangaSecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onComplete(alert.activityId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("modal_complete_btn")
                    ) {
                        Text("1. Mark as Completed", fontWeight = FontWeight.Bold, color = ZangaWhite)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onExtend(alert.activityId, 15)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2D4A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("modal_extend_15_btn")
                        ) {
                            Text("+15 Min", color = ZangaWhite)
                        }

                        Button(
                            onClick = {
                                onExtend(alert.activityId, 30)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2D4A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("modal_extend_30_btn")
                        ) {
                            Text("+30 Min", color = ZangaWhite)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onReschedule(alert.activityId)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZangaWhite.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("modal_reschedule_btn")
                        ) {
                            Text("Reschedule", color = ZangaWhite)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZangaWhite.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("modal_stop_alarm_btn")
                        ) {
                            Text("Stop Alarm", color = ZangaWhite)
                        }
                    }
                }
            },
            dismissButton = null
        )
    }
}
