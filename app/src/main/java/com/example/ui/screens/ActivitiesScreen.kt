package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ActivityEntity
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivitiesScreen(
    viewModel: ZangaViewModel,
    onRequestAddActivity: () -> Unit,
    onEditActivity: (ActivityEntity) -> Unit,
    onStartFocus: (ActivityEntity) -> Unit
) {
    val activities by viewModel.allActivities.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("All", "Today", "Academic & Career", "Upcoming", "Overdue", "Completed")

    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfDay = cal.timeInMillis
    val endOfDay = startOfDay + 86400000L

    val filteredActivities = activities.filter { act ->
        val matchesTab = when (selectedTabIndex) {
            1 -> act.startTimeMillis in startOfDay..endOfDay
            2 -> act.isAcademicOrProfessional || act.category == "Education" || act.category == "Professional"
            3 -> act.startTimeMillis > now && act.status == "PENDING"
            4 -> act.status == "OVERDUE" || (act.status == "PENDING" && now > act.endTimeMillis)
            5 -> act.status == "COMPLETED"
            else -> true
        }
        val matchesCategory = selectedCategory == null || act.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || act.title.contains(searchQuery, ignoreCase = true) ||
                act.description.contains(searchQuery, ignoreCase = true)

        matchesTab && matchesCategory && matchesSearch
    }

    val allCategories = listOf(
        "All", "Education", "Professional", "Personal", "Business",
        "Health & Fitness", "Family", "Social Activities",
        "Household Responsibilities", "Religious & Spiritual"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRequestAddActivity,
                containerColor = ZangaPrimaryBlue,
                contentColor = ZangaWhite,
                shape = CircleShape,
                modifier = Modifier.testTag("activities_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Header Bar
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Activities & Timetable",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Intelligent monitoring and time allocation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search scheduled activities...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = ZangaPrimaryBlue,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, tabName ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tabName,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) ZangaPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(allCategories) { cat ->
                    val isSelected = (cat == "All" && selectedCategory == null) || (cat == selectedCategory)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = if (cat == "All") null else cat
                        },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ZangaPrimaryBlue,
                            selectedLabelColor = ZangaWhite
                        )
                    )
                }
            }

            // Academic / Career Banner if tab 2 selected
            if (selectedTabIndex == 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = ZangaDeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.School, contentDescription = "Education", tint = ZangaSecondaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Class schedules, study timetables, exam countdowns & skill practice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZangaWhite
                        )
                    }
                }
            }

            // List of Activities
            if (filteredActivities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = "Empty",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No activities in this category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap the + button to schedule a new activity with countdown alarms.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredActivities, key = { it.id }) { activity ->
                        ActivityItemCard(
                            activity = activity,
                            onComplete = { viewModel.completeActivity(activity.id) },
                            onExtend = { viewModel.extendActivity(activity.id, 15) },
                            onPostpone = { viewModel.postponeActivity(activity.id, 30) },
                            onFocus = { onStartFocus(activity) },
                            onEdit = { onEditActivity(activity) },
                            onDelete = { viewModel.deleteActivity(activity) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityItemCard(
    activity: ActivityEntity,
    onComplete: () -> Unit,
    onExtend: () -> Unit,
    onPostpone: () -> Unit,
    onFocus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val startStr = timeFormat.format(Date(activity.startTimeMillis))
    val endStr = timeFormat.format(Date(activity.endTimeMillis))
    val dateStr = dateFormat.format(Date(activity.dateMillis))

    val isOverdue = activity.status == "OVERDUE" ||
            (activity.status == "PENDING" && System.currentTimeMillis() > activity.endTimeMillis)
    val isCompleted = activity.status == "COMPLETED"

    val statusColor = when {
        isCompleted -> ZangaSuccess
        isOverdue -> ZangaExpired
        activity.status == "EXTENDED" -> ZangaSecondaryBlue
        else -> ZangaPrimaryBlue
    }

    val priorityColor = when (activity.priority) {
        "URGENT" -> ZangaExpired
        "HIGH" -> ZangaWarning
        "MEDIUM" -> ZangaSecondaryBlue
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Pill, Priority Pill, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ZangaPrimaryBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activity.category.uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.labelSmall,
                            color = ZangaPrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activity.priority,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (activity.timeExtendedMinutes > 0) {
                        Text(
                            text = "+${activity.timeExtendedMinutes}m extended",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZangaSecondaryBlue
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Activity") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                        )
                        DropdownMenuItem(
                            text = { Text("Postpone (+30 min)") },
                            onClick = {
                                menuExpanded = false
                                onPostpone()
                            },
                            leadingIcon = { Icon(Icons.Default.Update, contentDescription = "Postpone") }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Activity") },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ZangaExpired) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (activity.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time & Date Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Time",
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$startStr - $endStr (${activity.allocatedDurationMinutes}m)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "• $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (activity.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activity.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isCompleted) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = ZangaPrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Complete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ZangaWhite)
                    }

                    OutlinedButton(
                        onClick = onFocus,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Focus", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onExtend,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("+15m", fontSize = 12.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ZangaSuccess.copy(alpha = 0.15f))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ Completed in ${activity.actualDurationMinutes} mins",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZangaSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
