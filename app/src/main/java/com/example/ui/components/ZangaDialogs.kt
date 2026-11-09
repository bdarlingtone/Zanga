package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ActivityEntity
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BudgetEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.theme.ZangaDeepBlue
import com.example.ui.theme.ZangaPrimaryBlue
import com.example.ui.theme.ZangaSecondaryBlue
import com.example.ui.theme.ZangaWhite
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateActivityDialog(
    initialActivity: ActivityEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ActivityEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialActivity?.title ?: "") }
    var description by remember { mutableStateOf(initialActivity?.description ?: "") }
    var category by remember { mutableStateOf(initialActivity?.category ?: "Education") }
    var durationMinutes by remember { mutableIntStateOf(initialActivity?.allocatedDurationMinutes ?: 60) }
    var priority by remember { mutableStateOf(initialActivity?.priority ?: "MEDIUM") }
    var location by remember { mutableStateOf(initialActivity?.location ?: "") }
    var notes by remember { mutableStateOf(initialActivity?.notes ?: "") }

    var warning30m by remember { mutableStateOf(initialActivity?.isWarning30mEnabled ?: true) }
    var warning15m by remember { mutableStateOf(initialActivity?.isWarning15mEnabled ?: true) }
    var warning5m by remember { mutableStateOf(initialActivity?.isWarning5mEnabled ?: true) }
    var deadlineAlarm by remember { mutableStateOf(initialActivity?.isDeadlineAlarmEnabled ?: true) }

    val categories = listOf(
        "Personal", "Education", "Professional", "Business", "Family",
        "Health & Fitness", "Social Activities", "Household Responsibilities",
        "Religious & Spiritual", "Other"
    )
    val durations = listOf(15, 30, 45, 60, 90, 120, 180)
    val priorities = listOf("LOW", "MEDIUM", "HIGH", "URGENT")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("create_activity_dialog"),
        title = {
            Text(
                text = if (initialActivity == null) "Schedule Activity" else "Edit Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title*") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Objectives") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Text(
                    text = "Allocated Duration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    durations.forEach { d ->
                        FilterChip(
                            selected = durationMinutes == d,
                            onClick = { durationMinutes = d },
                            label = { Text("${d}m", fontSize = 12.sp) }
                        )
                    }
                }

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Venue (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Intelligent Alarm & Countdown Engine",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = warning30m, onCheckedChange = { warning30m = it })
                    Text("30 min warning alert", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = warning15m, onCheckedChange = { warning15m = it })
                    Text("15 min warning alert", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = warning5m, onCheckedChange = { warning5m = it })
                    Text("5 min urgent countdown", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = deadlineAlarm, onCheckedChange = { deadlineAlarm = it })
                    Text("0 min deadline alarm (TIME IS UP)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val now = System.currentTimeMillis()
                    val startTime = initialActivity?.startTimeMillis ?: now
                    val endTime = startTime + (durationMinutes * 60 * 1000L)

                    val activity = initialActivity?.copy(
                        title = title.trim(),
                        description = description.trim(),
                        category = category,
                        allocatedDurationMinutes = durationMinutes,
                        endTimeMillis = endTime,
                        priority = priority,
                        location = location.trim(),
                        notes = notes.trim(),
                        isWarning30mEnabled = warning30m,
                        isWarning15mEnabled = warning15m,
                        isWarning5mEnabled = warning5m,
                        isDeadlineAlarmEnabled = deadlineAlarm
                    ) ?: ActivityEntity(
                        title = title.trim(),
                        description = description.trim(),
                        category = category,
                        dateMillis = startTime,
                        startTimeMillis = startTime,
                        endTimeMillis = endTime,
                        allocatedDurationMinutes = durationMinutes,
                        priority = priority,
                        location = location.trim(),
                        notes = notes.trim(),
                        status = "PENDING",
                        isWarning30mEnabled = warning30m,
                        isWarning15mEnabled = warning15m,
                        isWarning5mEnabled = warning5m,
                        isDeadlineAlarmEnabled = deadlineAlarm,
                        isAcademicOrProfessional = category == "Education" || category == "Professional",
                        subType = if (category == "Education") "STUDY" else "GENERAL"
                    )
                    onSave(activity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_activity_confirm_btn")
            ) {
                Text("Schedule & Activate", fontWeight = FontWeight.Bold, color = ZangaWhite)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateTransactionDialog(
    currency: String = "MWK",
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food & Groceries") }
    var paymentMethod by remember { mutableStateOf("Mobile Money") }
    var description by remember { mutableStateOf("") }

    val expenseCategories = listOf(
        "Food & Groceries", "Transport & Fuel", "Housing & Rent",
        "Bills & Utilities", "Education", "Healthcare", "Business",
        "Leisure & Personal", "Miscellaneous"
    )
    val incomeSources = listOf(
        "Salary & Wage", "Business Revenue", "Consulting & Retainer",
        "Investments", "Gifts & Support", "Other Income"
    )
    val paymentMethods = listOf("Cash", "Mobile Money", "Bank Transfer", "Card")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (type == "EXPENSE") "Record Expense" else "Record Income",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            type = "EXPENSE"
                            category = "Food & Groceries"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "EXPENSE") ZangaPrimaryBlue else Color.Gray.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Expense", color = if (type == "EXPENSE") ZangaWhite else MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = {
                            type = "INCOME"
                            category = "Consulting & Retainer"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") ZangaPrimaryBlue else Color.Gray.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Income", color = if (type == "INCOME") ZangaWhite else MaterialTheme.colorScheme.onSurface)
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currency)*") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input")
                )

                Text(
                    text = if (type == "EXPENSE") "Expense Category" else "Income Source",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                val activeCategories = if (type == "EXPENSE") expenseCategories else incomeSources
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    activeCategories.take(6).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Text(text = "Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    paymentMethods.forEach { pm ->
                        FilterChip(
                            selected = paymentMethod == pm,
                            onClick = { paymentMethod = pm },
                            label = { Text(pm, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: return@Button
                    val tx = TransactionEntity(
                        type = type,
                        category = category,
                        amount = amt,
                        currency = currency,
                        dateMillis = System.currentTimeMillis(),
                        paymentMethod = paymentMethod,
                        description = description.ifBlank { category }
                    )
                    onSave(tx)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_transaction_btn")
            ) {
                Text("Record Entry", fontWeight = FontWeight.Bold, color = ZangaWhite)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReportExportDialog(
    reportText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ZangaDeepBlue,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ZANGA Executive Report", fontWeight = FontWeight.Bold, color = ZangaWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ZangaWhite)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0E1526))
                        .padding(12.dp)
                ) {
                    Text(
                        text = reportText,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZangaWhite,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("ZANGA Report", reportText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ZangaWhite, modifier = Modifier.padding(end = 6.dp))
                Text("Copy Report", color = ZangaWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Close", color = ZangaWhite)
            }
        }
    )
}
