package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ZangaDeepBlue
import com.example.ui.theme.ZangaExpired
import com.example.ui.theme.ZangaPrimaryBlue
import com.example.ui.theme.ZangaPureBlack
import com.example.ui.theme.ZangaSecondaryBlue
import com.example.ui.theme.ZangaWhite
import com.example.viewmodel.ZangaViewModel
import java.util.Locale

@Composable
fun FocusModeScreen(
    viewModel: ZangaViewModel,
    onExit: () -> Unit
) {
    BackHandler {
        onExit()
    }

    val focusActivity by viewModel.focusActivity.collectAsState()
    val remainingSeconds by viewModel.focusRemainingSeconds.collectAsState()
    val elapsedSeconds by viewModel.focusElapsedSeconds.collectAsState()
    val isRunning by viewModel.isFocusRunning.collectAsState()

    val totalAllocatedSeconds = ((focusActivity?.allocatedDurationMinutes ?: 60) * 60).coerceAtLeast(1)
    val progress = (remainingSeconds.toFloat() / totalAllocatedSeconds).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "focus_countdown"
    )

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timerText = if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    val elapsedMinutes = elapsedSeconds / 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZangaPureBlack)
            .padding(24.dp)
            .testTag("focus_mode_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ZangaWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ZangaDeepBlue)
                        .border(1.dp, ZangaSecondaryBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "FOCUS IMMERSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZangaSecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            // Center Focus Information & Dial
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = focusActivity?.title ?: "Deep Work & Execution",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = ZangaWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${focusActivity?.category ?: "Personal"} • ${focusActivity?.allocatedDurationMinutes ?: 60}m Allocated",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZangaSecondaryBlue,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Giant Circular Progress Countdown Dial
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(260.dp)
                ) {
                    Canvas(modifier = Modifier.size(250.dp)) {
                        // Track
                        drawCircle(
                            color = ZangaDeepBlue,
                            style = Stroke(width = 16.dp.toPx())
                        )
                        // Progress
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(ZangaPrimaryBlue, ZangaSecondaryBlue, ZangaWhite)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timerText,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = if (remainingSeconds <= 300) ZangaExpired else ZangaWhite,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRunning) "TIME REMAINING" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZangaWhite.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Actual time recorded: $elapsedMinutes min $seconds sec",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZangaWhite.copy(alpha = 0.8f)
                )

                Text(
                    text = "“Own your focus. The world yields to disciplined action.”",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZangaWhite.copy(alpha = 0.5f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )
            }

            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.toggleFocusTimer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) ZangaDeepBlue else ZangaPrimaryBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("focus_pause_resume_btn")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Resume",
                            tint = ZangaWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRunning) "Pause" else "Resume", color = ZangaWhite, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            focusActivity?.let { viewModel.extendActivity(it.id, 15) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16233B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("focus_extend_btn")
                    ) {
                        Icon(Icons.Default.Update, contentDescription = "Extend", tint = ZangaWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+15 Min", color = ZangaWhite, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { viewModel.completeFocusSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("focus_complete_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Complete", tint = ZangaWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Activity & Record Progress", color = ZangaWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
