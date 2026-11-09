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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.entity.ActivityEntity
import com.example.ui.components.ScheduleConflictBanner
import com.example.ui.theme.ZangaDeepBlue
import com.example.ui.theme.ZangaExpired
import com.example.ui.theme.ZangaPrimaryBlue
import com.example.ui.theme.ZangaSecondaryBlue
import com.example.ui.theme.ZangaSuccess
import com.example.ui.theme.ZangaWarning
import com.example.ui.theme.ZangaWhite
import com.example.viewmodel.ZangaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class TimelineSlot {
    data class ScheduledActivity(val activity: ActivityEntity) : TimelineSlot()
    data class FreeTimeSlot(val startMillis: Long, val endMillis: Long, val durationMinutes: Long) : TimelineSlot()
}

@Composable
fun TimelineScreen(
    viewModel: ZangaViewModel,
    onRequestAddActivity: () -> Unit,
    onStartFocus: (ActivityEntity) -> Unit
) {
    val activities by viewModel.allActivities.collectAsState()
    val conflicts by viewModel.scheduleConflicts.collectAsState()

    var dayOffset by remember { mutableIntStateOf(0) } // 0 = Today, 1 = Tomorrow

    val cal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, dayOffset)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfDay = cal.timeInMillis
    val endOfDay = startOfDay + 86400000L

    val dayActivities = activities.filter { it.startTimeMillis in startOfDay..endOfDay }
        .sortedBy { it.startTimeMillis }

    // Build timeline items interleaving free time slots
    val timelineSlots = remember(dayActivities, startOfDay) {
        val slots = mutableListOf<TimelineSlot>()
        var lastEnd = startOfDay + 7 * 3600 * 1000L // start visual timeline at 07:00 AM

        for (act in dayActivities) {
            if (act.startTimeMillis > lastEnd + 15 * 60 * 1000L) {
                // Free gap of at least 15 min
                val freeMinutes = (act.startTimeMillis - lastEnd) / 60000
                slots.add(TimelineSlot.FreeTimeSlot(lastEnd, act.startTimeMillis, freeMinutes))
            }
            slots.add(TimelineSlot.ScheduledActivity(act))
            lastEnd = maxOf(lastEnd, act.endTimeMillis)
        }

        // Evening free slot if before 22:00
        val eveningEnd = startOfDay + 22 * 3600 * 1000L
        if (eveningEnd > lastEnd + 30 * 60 * 1000L) {
            val freeMinutes = (eveningEnd - lastEnd) / 60000
            slots.add(TimelineSlot.FreeTimeSlot(lastEnd, eveningEnd, freeMinutes))
        }

        slots
    }

    val dayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "24-Hour Daily Timeline",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Visual chronological allocation & conflict avoidance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Day Selector Tabs (Today vs Tomorrow)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { dayOffset = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (dayOffset == 0) ZangaPrimaryBlue else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Today",
                    color = if (dayOffset == 0) ZangaWhite else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { dayOffset = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (dayOffset == 1) ZangaPrimaryBlue else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Tomorrow",
                    color = if (dayOffset == 1) ZangaWhite else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dayFormat.format(Date(startOfDay)),
            style = MaterialTheme.typography.titleSmall,
            color = ZangaSecondaryBlue,
            fontWeight = FontWeight.Bold
        )

        // Conflict Banner if any
        if (conflicts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            val c = conflicts.first()
            ScheduleConflictBanner(
                activity1Title = c.activity1.title,
                activity2Title = c.activity2.title,
                suggestion = c.suggestedAlternativeSlot,
                onDismiss = {}
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (timelineSlots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Empty",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Entire Day Available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "No activities scheduled for this day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRequestAddActivity,
                        colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue)
                    ) {
                        Text("Schedule Time Slot", color = ZangaWhite)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
            ) {
                items(timelineSlots) { slot ->
                    when (slot) {
                        is TimelineSlot.ScheduledActivity -> {
                            TimelineActivityBlock(
                                activity = slot.activity,
                                onComplete = { viewModel.completeActivity(slot.activity.id) },
                                onFocus = { onStartFocus(slot.activity) },
                                onPostpone = { viewModel.postponeActivity(slot.activity.id, 30) }
                            )
                        }
                        is TimelineSlot.FreeTimeSlot -> {
                            TimelineFreeSlot(
                                startMillis = slot.startMillis,
                                endMillis = slot.endMillis,
                                durationMinutes = slot.durationMinutes,
                                onQuickAdd = onRequestAddActivity
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineActivityBlock(
    activity: ActivityEntity,
    onComplete: () -> Unit,
    onFocus: () -> Unit,
    onPostpone: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val startStr = timeFormat.format(Date(activity.startTimeMillis))
    val endStr = timeFormat.format(Date(activity.endTimeMillis))

    val now = System.currentTimeMillis()
    val isRunning = now in activity.startTimeMillis..activity.endTimeMillis && activity.status != "COMPLETED"
    val isCompleted = activity.status == "COMPLETED"
    val isOverdue = activity.status == "OVERDUE" || (activity.status == "PENDING" && now > activity.endTimeMillis)

    val borderColor = when {
        isRunning -> ZangaSecondaryBlue
        isOverdue -> ZangaExpired
        isCompleted -> ZangaSuccess
        else -> MaterialTheme.colorScheme.outline
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Time Needle Column
        Column(
            modifier = Modifier.width(68.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = startStr,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) ZangaSecondaryBlue else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${activity.allocatedDurationMinutes}m",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Card Content
        Card(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(if (isRunning) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isRunning) ZangaDeepBlue else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) ZangaWhite else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isCompleted -> ZangaSuccess.copy(alpha = 0.2f)
                                    isOverdue -> ZangaExpired.copy(alpha = 0.2f)
                                    isRunning -> ZangaSecondaryBlue.copy(alpha = 0.25f)
                                    else -> Color.Gray.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "ACTIVE" else activity.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = when {
                                isCompleted -> ZangaSuccess
                                isOverdue -> ZangaExpired
                                isRunning -> ZangaSecondaryBlue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "$startStr - $endStr • ${activity.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRunning) ZangaWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Done", fontSize = 11.sp, color = ZangaWhite, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onFocus,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Focus", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onPostpone,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Shift", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineFreeSlot(
    startMillis: Long,
    endMillis: Long,
    durationMinutes: Long,
    onQuickAdd: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val startStr = timeFormat.format(Date(startMillis))
    val endStr = timeFormat.format(Date(endMillis))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onQuickAdd() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(68.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = startStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    RoundedCornerShape(8.dp)
                )
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ZangaSecondaryBlue.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Available Slot ($durationMinutes min free)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Quick Add",
                        tint = ZangaSecondaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZangaSecondaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
