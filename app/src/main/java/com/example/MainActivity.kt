package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.ActivityEntity
import com.example.ui.components.AlarmExpirationModal
import com.example.ui.components.CreateActivityDialog
import com.example.ui.components.CreateTransactionDialog
import com.example.ui.screens.ActivitiesScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AppointmentsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.FocusModeScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.theme.ZangaDeepBlue
import com.example.ui.theme.ZangaPrimaryBlue
import com.example.ui.theme.ZangaSecondaryBlue
import com.example.ui.theme.ZangaTheme
import com.example.ui.theme.ZangaWhite
import com.example.viewmodel.ZangaScreen
import com.example.viewmodel.ZangaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val zangaViewModel: ZangaViewModel = viewModel()
            val isDark by zangaViewModel.isDarkMode.collectAsState()

            // Request Notification Permission on Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            ZangaTheme(darkTheme = isDark) {
                ZangaMainApp(viewModel = zangaViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZangaMainApp(viewModel: ZangaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeAlarm by viewModel.activeAlarmAlert.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()

    var showCreateActivityDialog by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<ActivityEntity?>(null) }
    var showCreateTransactionDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    // Custom back navigation handler
    BackHandler(enabled = currentScreen != ZangaScreen.DASHBOARD) {
        viewModel.handleBack()
    }

    // Full screen focus mode handles its own chrome
    if (currentScreen == ZangaScreen.FOCUS_MODE) {
        FocusModeScreen(
            viewModel = viewModel,
            onExit = { viewModel.navigateTo(ZangaScreen.DASHBOARD) }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZangaPrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Z", color = ZangaWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ZANGA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Take Control. Own Your Time.",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZangaSecondaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ZangaScreen.ASSISTANT) },
                        modifier = Modifier.testTag("topbar_ai_assistant_btn")
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "ZANGA AI",
                            tint = if (currentScreen == ZangaScreen.ASSISTANT) ZangaPrimaryBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(ZangaScreen.FOCUS_MODE) },
                        modifier = Modifier.testTag("topbar_focus_mode_btn")
                    ) {
                        Icon(
                            Icons.Default.HourglassTop,
                            contentDescription = "Focus Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(ZangaScreen.SETTINGS) },
                        modifier = Modifier.testTag("topbar_settings_btn")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentScreen == ZangaScreen.SETTINGS) ZangaPrimaryBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentScreen == ZangaScreen.DASHBOARD,
                    onClick = { viewModel.navigateTo(ZangaScreen.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZangaWhite,
                        selectedTextColor = ZangaPrimaryBlue,
                        indicatorColor = ZangaPrimaryBlue
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == ZangaScreen.ACTIVITIES,
                    onClick = { viewModel.navigateTo(ZangaScreen.ACTIVITIES) },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Activities") },
                    label = { Text("Activities", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZangaWhite,
                        selectedTextColor = ZangaPrimaryBlue,
                        indicatorColor = ZangaPrimaryBlue
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == ZangaScreen.TIMELINE,
                    onClick = { viewModel.navigateTo(ZangaScreen.TIMELINE) },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Timeline") },
                    label = { Text("Timeline", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZangaWhite,
                        selectedTextColor = ZangaPrimaryBlue,
                        indicatorColor = ZangaPrimaryBlue
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == ZangaScreen.FINANCE,
                    onClick = { viewModel.navigateTo(ZangaScreen.FINANCE) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finances") },
                    label = { Text("Finance", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZangaWhite,
                        selectedTextColor = ZangaPrimaryBlue,
                        indicatorColor = ZangaPrimaryBlue
                    )
                )

                NavigationBarItem(
                    selected = currentScreen in listOf(
                        ZangaScreen.HABITS,
                        ZangaScreen.APPOINTMENTS,
                        ZangaScreen.ANALYTICS,
                        ZangaScreen.ASSISTANT,
                        ZangaScreen.SETTINGS
                    ),
                    onClick = { showMoreMenu = true },
                    icon = {
                        Box {
                            Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Routines & Habits") },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.navigateTo(ZangaScreen.HABITS)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = "Habits") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Meetings & Appointments") },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.navigateTo(ZangaScreen.APPOINTMENTS)
                                    },
                                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Appointments") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Discipline & Analytics") },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.navigateTo(ZangaScreen.ANALYTICS)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") }
                                )
                                DropdownMenuItem(
                                    text = { Text("ZANGA AI Assistant") },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.navigateTo(ZangaScreen.ASSISTANT)
                                    },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings & Backup") },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.navigateTo(ZangaScreen.SETTINGS)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                                )
                            }
                        }
                    },
                    label = { Text("More", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZangaWhite,
                        selectedTextColor = ZangaPrimaryBlue,
                        indicatorColor = ZangaPrimaryBlue
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ZangaScreen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) },
                    onRequestAddActivity = {
                        activityToEdit = null
                        showCreateActivityDialog = true
                    }
                )
                ZangaScreen.ACTIVITIES -> ActivitiesScreen(
                    viewModel = viewModel,
                    onRequestAddActivity = {
                        activityToEdit = null
                        showCreateActivityDialog = true
                    },
                    onEditActivity = {
                        activityToEdit = it
                        showCreateActivityDialog = true
                    },
                    onStartFocus = {
                        viewModel.startFocusSession(it)
                    }
                )
                ZangaScreen.TIMELINE -> TimelineScreen(
                    viewModel = viewModel,
                    onRequestAddActivity = {
                        activityToEdit = null
                        showCreateActivityDialog = true
                    },
                    onStartFocus = {
                        viewModel.startFocusSession(it)
                    }
                )
                ZangaScreen.FINANCE -> FinanceScreen(
                    viewModel = viewModel,
                    onRequestAddTransaction = {
                        showCreateTransactionDialog = true
                    }
                )
                ZangaScreen.FOCUS_MODE -> FocusModeScreen(
                    viewModel = viewModel,
                    onExit = { viewModel.navigateTo(ZangaScreen.DASHBOARD) }
                )
                ZangaScreen.HABITS -> HabitsScreen(
                    viewModel = viewModel
                )
                ZangaScreen.APPOINTMENTS -> AppointmentsScreen(
                    viewModel = viewModel
                )
                ZangaScreen.ANALYTICS -> AnalyticsScreen(
                    viewModel = viewModel
                )
                ZangaScreen.ASSISTANT -> AiAssistantScreen(
                    viewModel = viewModel
                )
                ZangaScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }

            // In-App Alarm Expiration / Warning Alert Modal
            AlarmExpirationModal(
                alert = activeAlarm,
                onComplete = { activityId ->
                    viewModel.completeActivity(activityId)
                    viewModel.dismissAlarmAlert()
                },
                onExtend = { activityId, minutes ->
                    viewModel.extendActivity(activityId, minutes)
                    viewModel.dismissAlarmAlert()
                },
                onReschedule = { activityId ->
                    viewModel.postponeActivity(activityId, 60)
                    viewModel.dismissAlarmAlert()
                },
                onDismiss = {
                    viewModel.dismissAlarmAlert()
                }
            )

            // Dialog for creating or editing an Activity
            if (showCreateActivityDialog) {
                CreateActivityDialog(
                    initialActivity = activityToEdit,
                    onDismiss = {
                        showCreateActivityDialog = false
                        activityToEdit = null
                    },
                    onSave = {
                        if (activityToEdit != null) {
                            viewModel.updateActivity(it)
                        } else {
                            viewModel.createActivity(it)
                        }
                        showCreateActivityDialog = false
                        activityToEdit = null
                    }
                )
            }

            // Dialog for recording financial transaction
            if (showCreateTransactionDialog) {
                CreateTransactionDialog(
                    currency = currency,
                    onDismiss = { showCreateTransactionDialog = false },
                    onSave = {
                        viewModel.addTransaction(it)
                        showCreateTransactionDialog = false
                    }
                )
            }
        }
    }
}
