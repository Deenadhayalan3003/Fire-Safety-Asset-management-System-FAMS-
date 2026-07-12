package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authentication.AuthViewModel
import com.example.dashboard.DashboardViewModel
import com.example.models.Asset
import com.example.models.Inspection
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.FireRed
import com.example.ui.theme.SlateBlue
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.dashboard.SearchViewModel
import com.example.dashboard.NotificationViewModel
import com.example.dashboard.ActivityViewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    searchViewModel: SearchViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    activityViewModel: ActivityViewModel = hiltViewModel(),
    onLogoutNavigate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdminConsole: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val assets by dashboardViewModel.assets.collectAsState()
    val loading by dashboardViewModel.loading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val dashboardSummary by dashboardViewModel.dashboardSummary.collectAsState()
    val upcomingInspections by dashboardViewModel.upcomingInspections.collectAsState()
    val maintenanceSummary by dashboardViewModel.maintenanceSummary.collectAsState()
    val assetDistribution by dashboardViewModel.assetDistribution.collectAsState()
    val inspectionSummary by dashboardViewModel.inspectionSummary.collectAsState()

    val unreadNotificationsCount by notificationViewModel.unreadCount.collectAsState()
    val recentNotifications by notificationViewModel.notifications.collectAsState()
    val recentActivities by activityViewModel.activities.collectAsState()

    var selectedBottomTab by remember { mutableStateOf(0) } // 0: Home, 1: Assets, 2: Scanner, 3: Reports, 4: Profile
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    
    // Dialog Controls
    var showAddAssetDialog by remember { mutableStateOf(false) }
    var showPerformInspectionDialog by remember { mutableStateOf(false) }
    var inspectionSelectedAsset by remember { mutableStateOf<Asset?>(null) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showMaintenanceListDialog by remember { mutableStateOf(false) }
    var showQrHubDialog by remember { mutableStateOf(false) }
    
    // Form States
    var newAssetId by remember { mutableStateOf("") }
    var newAssetName by remember { mutableStateOf("") }
    var newAssetType by remember { mutableStateOf("Fire Extinguisher") }
    var newAssetSerial by remember { mutableStateOf("") }
    var newAssetLocation by remember { mutableStateOf("") }
    var newAssetModel by remember { mutableStateOf("") }
    var newAssetManufacturer by remember { mutableStateOf("") }

    var inspectStatus by remember { mutableStateOf("Passed") }
    var inspectComments by remember { mutableStateOf("") }
    var inspectPressure by remember { mutableStateOf("Normal") }
    var inspectBattery by remember { mutableStateOf("Normal") }
    var inspectIntegrity by remember { mutableStateOf("Good") }

    val userRole = currentUser?.role ?: "Technician"
    val userName = currentUser?.name ?: "System User"
    val userEmail = currentUser?.email ?: "user@fams.com"
    val plantName = currentUser?.plant ?: "Main Plant Alpha"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                // Enterprise Header Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateBlue)
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "User Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = userName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$userRole • $plantName",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drawer items
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Command Dashboard") },
                    selected = selectedBottomTab == 0,
                    onClick = {
                        selectedBottomTab = 0
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    label = { Text("Assets Directory") },
                    selected = selectedBottomTab == 1,
                    onClick = {
                        selectedBottomTab = 1
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.FactCheck, contentDescription = null) },
                    label = { Text("Inspections Hub") },
                    selected = selectedBottomTab == 2,
                    onClick = {
                        selectedBottomTab = 2
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                    label = { Text("Maintenance Center") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        showMaintenanceListDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Reports & Analytics") },
                    selected = selectedBottomTab == 3,
                    onClick = {
                        selectedBottomTab = 3
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    label = { 
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Notifications")
                            if (unreadNotificationsCount > 0) {
                                Badge(containerColor = FireRed, contentColor = Color.White) {
                                    Text(unreadNotificationsCount.toString())
                                }
                            }
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        showNotificationsDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Portal Settings") },
                    selected = selectedBottomTab == 4,
                    onClick = {
                        selectedBottomTab = 4
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Divider(modifier = Modifier.padding(horizontal = 12.dp))

                // Logout Item
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = "Logout", tint = FireRed) },
                    label = { Text("Secure Logout", color = FireRed) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            authViewModel.logout()
                            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            onLogoutNavigate()
                        }
                    },
                    modifier = Modifier
                        .padding(12.dp)
                        .testTag("drawer_logout_button")
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "FAMS Enterprise Portal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = plantName,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("hamburger_menu_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showNotificationsDialog = true },
                            modifier = Modifier.testTag("notification_bell_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge(containerColor = Color.White, contentColor = FireRed) {
                                            Text(unreadNotificationsCount.toString(), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                            }
                        }
                        IconButton(
                            onClick = { selectedBottomTab = 4 },
                            modifier = Modifier.testTag("profile_top_bar_button")
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FireRed)
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(if (selectedBottomTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard, null) },
                        label = { Text("Home") },
                        selected = selectedBottomTab == 0,
                        onClick = { selectedBottomTab = 0 },
                        modifier = Modifier.testTag("bottom_tab_home")
                    )
                    NavigationBarItem(
                        icon = { Icon(if (selectedBottomTab == 1) Icons.Filled.Inventory else Icons.Outlined.Inventory, null) },
                        label = { Text("Assets") },
                        selected = selectedBottomTab == 1,
                        onClick = { selectedBottomTab = 1 },
                        modifier = Modifier.testTag("bottom_tab_assets")
                    )
                    NavigationBarItem(
                        icon = { Icon(if (selectedBottomTab == 2) Icons.Filled.QrCodeScanner else Icons.Outlined.QrCodeScanner, null) },
                        label = { Text("QR Scanner") },
                        selected = selectedBottomTab == 2,
                        onClick = { selectedBottomTab = 2 },
                        modifier = Modifier.testTag("bottom_tab_qr_scanner")
                    )
                    NavigationBarItem(
                        icon = { Icon(if (selectedBottomTab == 3) Icons.Filled.BarChart else Icons.Outlined.BarChart, null) },
                        label = { Text("Reports") },
                        selected = selectedBottomTab == 3,
                        onClick = { selectedBottomTab = 3 },
                        modifier = Modifier.testTag("bottom_tab_reports")
                    )
                    NavigationBarItem(
                        icon = { Icon(if (selectedBottomTab == 4) Icons.Filled.Person else Icons.Outlined.Person, null) },
                        label = { Text("Profile") },
                        selected = selectedBottomTab == 4,
                        onClick = { selectedBottomTab = 4 },
                        modifier = Modifier.testTag("bottom_tab_profile")
                    )
                }
            },
            floatingActionButton = {
                if (selectedBottomTab == 1 && userRole == "Administrator") {
                    FloatingActionButton(
                        onClick = { showAddAssetDialog = true },
                        containerColor = FireRed,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_asset_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Fire Asset")
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedBottomTab) {
                    0 -> DashboardOverviewTab(
                        assets = assets,
                        userRole = userRole,
                        userName = userName,
                        plantName = plantName,
                        dashboardSummary = dashboardSummary,
                        upcomingInspections = upcomingInspections,
                        maintenanceSummary = maintenanceSummary,
                        recentActivities = recentActivities,
                        searchViewModel = searchViewModel,
                        notificationViewModel = notificationViewModel,
                        onViewCatalog = { selectedBottomTab = 1 },
                        onPerformInspection = { asset ->
                            inspectionSelectedAsset = asset
                            showPerformInspectionDialog = true
                        },
                        onAddAssetClick = { showAddAssetDialog = true },
                        onOpenNotifications = { showNotificationsDialog = true },
                        onOpenMaintenanceDialog = { showMaintenanceListDialog = true },
                        onSelectScannerTab = { selectedBottomTab = 2 },
                        onSelectReportsTab = { selectedBottomTab = 3 }
                    )
                    1 -> AssetsCatalogTab(
                        viewModel = dashboardViewModel,
                        userRole = userRole,
                        userName = userName
                    )
                    2 -> QRScannerTab(
                        assets = assets,
                        onInspectAsset = { asset ->
                            inspectionSelectedAsset = asset
                            showPerformInspectionDialog = true
                        },
                        onOpenQrHub = { showQrHubDialog = true }
                    )
                    3 -> ReportsTab(
                        assetDistribution = assetDistribution,
                        inspectionSummary = inspectionSummary,
                        maintenanceSummary = maintenanceSummary
                    )
                    4 -> PortalSettingsTab(
                        userName = userName,
                        userEmail = userEmail,
                        userRole = userRole,
                        authViewModel = authViewModel,
                        onLogoutNavigate = onLogoutNavigate,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToAdminConsole = onNavigateToAdminConsole
                    )
                }

                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FireRed)
                    }
                }
            }
        }

        // Add Asset Dialog
        if (showAddAssetDialog) {
            AlertDialog(
                onDismissRequest = { showAddAssetDialog = false },
                title = { Text("Register FAMS Safety Asset", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Asset Type Selection", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
                        val types = listOf("Fire Extinguisher", "Smoke Detector", "Fire Hydrant", "Sprinkler", "Alarm Panel", "Hose Reel", "Emergency Light")
                        var expandedTypeDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            OutlinedButton(
                                onClick = { expandedTypeDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(newAssetType)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedTypeDropdown,
                                onDismissRequest = { expandedTypeDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                types.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            newAssetType = type
                                            expandedTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newAssetId,
                            onValueChange = { newAssetId = it },
                            label = { Text("Asset Code / ID") },
                            singleLine = true,
                            placeholder = { Text("FE-102") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = newAssetName,
                            onValueChange = { newAssetName = it },
                            label = { Text("Asset Name") },
                            singleLine = true,
                            placeholder = { Text("Water Extinguisher") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = newAssetSerial,
                            onValueChange = { newAssetSerial = it },
                            label = { Text("Serial Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = newAssetLocation,
                            onValueChange = { newAssetLocation = it },
                            label = { Text("Location") },
                            singleLine = true,
                            placeholder = { Text("Kitchen Pantry - 1st Floor") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = newAssetModel,
                            onValueChange = { newAssetModel = it },
                            label = { Text("Model Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = newAssetManufacturer,
                            onValueChange = { newAssetManufacturer = it },
                            label = { Text("Manufacturer") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newAssetId.isBlank() || newAssetName.isBlank() || newAssetLocation.isBlank()) {
                                Toast.makeText(context, "Fill in all key fields", Toast.LENGTH_SHORT).show()
                            } else {
                                val asset = Asset(
                                    id = newAssetId,
                                    name = newAssetName,
                                    type = newAssetType,
                                    serialNumber = newAssetSerial,
                                    location = newAssetLocation,
                                    status = "Operational",
                                    lastInspectionDate = "Not inspected",
                                    nextInspectionDue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30)),
                                    model = newAssetModel,
                                    manufacturer = newAssetManufacturer
                                )
                                dashboardViewModel.addAsset(asset)
                                // Log recent activity
                                val timestamp = System.currentTimeMillis()
                                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                activityViewModel.addActivity(
                                    com.example.models.Activity(
                                        id = "act-${timestamp}",
                                        type = "Asset Added",
                                        date = sdfDate.format(Date(timestamp)),
                                        time = sdfTime.format(Date(timestamp)),
                                        user = userName,
                                        description = "Registered new safety asset ${asset.id} (${asset.name}) at ${asset.location}."
                                    )
                                )
                                Toast.makeText(context, "Asset successfully registered!", Toast.LENGTH_SHORT).show()
                                showAddAssetDialog = false
                                // reset
                                newAssetId = ""
                                newAssetName = ""
                                newAssetSerial = ""
                                newAssetLocation = ""
                                newAssetModel = ""
                                newAssetManufacturer = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FireRed)
                    ) {
                        Text("Add Asset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAssetDialog = false }) {
                        Text("Cancel", color = SlateBlue)
                    }
                }
            )
        }

        // Perform Inspection Dialog
        if (showPerformInspectionDialog && inspectionSelectedAsset != null) {
            QrInspectionDialog(
                asset = inspectionSelectedAsset!!,
                inspectorName = userName,
                inspectorId = currentUser?.uid ?: "tech-uid",
                viewModel = dashboardViewModel,
                onDismiss = { showPerformInspectionDialog = false }
            )
        }

        // Notification Center Dialog
        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Alert Notifications", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (unreadNotificationsCount > 0) {
                            Badge(containerColor = FireRed, contentColor = Color.White) {
                                Text("$unreadNotificationsCount New")
                            }
                        }
                    }
                },
                text = {
                    Box(modifier = Modifier.heightIn(max = 400.dp).fillMaxWidth()) {
                        if (recentNotifications.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.NotificationsNone, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No alerts active", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn {
                                items(recentNotifications) { alert ->
                                    val alertColor = when (alert.type) {
                                        "Critical Alert" -> FireRed
                                        "Maintenance Due" -> OrangeAlert
                                        else -> AmberAlert
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { 
                                                notificationViewModel.markAsRead(alert.id)
                                                // Handle opening dialog based on notification
                                                Toast.makeText(context, "Marked alert as read", Toast.LENGTH_SHORT).show()
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (alert.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) 
                                                              else alertColor.copy(alpha = 0.12f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (alert.isRead) Color.Gray else alertColor)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = alert.title,
                                                    fontWeight = if (alert.isRead) FontWeight.Normal else FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (alert.isRead) Color.Gray else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = alert.message,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) {
                        Text("Close", color = FireRed, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Maintenance Alerts Dialog
        if (showMaintenanceListDialog) {
            MaintenanceManagementDialog(
                viewModel = dashboardViewModel,
                userRole = userRole,
                userName = userName,
                onDismiss = { showMaintenanceListDialog = false }
            )
        }

        if (showQrHubDialog) {
            QrGeneratorHubDialog(
                assets = assets,
                onDismiss = { showQrHubDialog = false }
            )
        }
    }
}

@Composable
fun MaintenanceAlertItem(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Build, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
        AssistChip(
            onClick = {},
            label = { Text(count.toString(), fontWeight = FontWeight.Bold, color = color) },
            colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.15f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardOverviewTab(
    assets: List<Asset>,
    userRole: String,
    userName: String,
    plantName: String,
    dashboardSummary: com.example.models.DashboardSummary,
    upcomingInspections: List<Asset>,
    maintenanceSummary: com.example.models.MaintenanceSummary,
    recentActivities: List<com.example.models.Activity>,
    searchViewModel: SearchViewModel,
    notificationViewModel: NotificationViewModel,
    onViewCatalog: () -> Unit,
    onPerformInspection: (Asset) -> Unit,
    onAddAssetClick: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMaintenanceDialog: () -> Unit,
    onSelectScannerTab: () -> Unit,
    onSelectReportsTab: () -> Unit
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }

    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val searchSuggestions by searchViewModel.suggestions.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()

    var showSuggestions by remember { mutableStateOf(false) }
    var selectedStatFilter by remember { mutableStateOf("Faulty / Critical Assets") }

    val filteredStatAssets = remember(assets, selectedStatFilter) {
        when (selectedStatFilter) {
            "All Assets" -> assets
            "Healthy Assets" -> assets.filter { it.status == "Operational" }
            "Due for Inspection" -> assets.filter { it.status == "Under Inspection" || (it.nextInspectionDue.isNotEmpty() && it.nextInspectionDue != "Not inspected") }
            "Overdue Inspections" -> assets.filter { it.status == "Damaged" || it.status == "Out of Service" }
            "Maintenance Due" -> assets.filter { it.status == "Damaged" || it.status == "Under Inspection" }
            "Faulty / Critical Assets" -> assets.filter { it.status == "Damaged" || it.condition == "Poor" || it.condition == "Damaged" }
            else -> assets.filter { it.status == "Damaged" || it.condition == "Poor" || it.condition == "Damaged" }
        }
    }

    // Dynamic greeting calculation
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().testTag("welcome_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = userName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = AmberAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = userRole,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = currentDate,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Global Search Bar Section
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchViewModel.updateSearchQuery(it)
                        showSuggestions = it.isNotEmpty()
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchViewModel.updateSearchQuery("")
                                showSuggestions = false
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    placeholder = { Text("Search by ID, Serial, Dept, Location...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("global_search_bar")
                )

                if (showSuggestions && searchSuggestions.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        Column {
                            searchSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchViewModel.updateSearchQuery(suggestion)
                                            showSuggestions = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(suggestion, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                if (searchQuery.isNotEmpty() && searchResults.isNotEmpty()) {
                    Text(
                        text = "Matching Search Results (${searchResults.size}):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                    LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                        items(searchResults) { asset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPerformInspection(asset) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(asset.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FireRed)
                                    Text(asset.name, fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (asset.status) {
                                                "Operational" -> Color(0xFFE8F5E9)
                                                "Damaged" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF8E1)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        asset.status,
                                        fontSize = 10.sp,
                                        color = when (asset.status) {
                                            "Operational" -> Color(0xFF2E7D32)
                                            "Damaged" -> FireRed
                                            else -> AmberAlert
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Summary Cards Section
        Text(
            text = "Enterprise Asset Statistics",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SlateBlue,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Tap any card below to view detailed asset logs",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    title = "Total Assets",
                    count = dashboardSummary.totalAssets.toString(),
                    color = SlateBlue,
                    icon = Icons.Default.Inventory2,
                    onClick = { selectedStatFilter = "All Assets" }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    title = "Healthy Assets",
                    count = dashboardSummary.healthyAssets.toString(),
                    color = Color(0xFF2E7D32), // Green
                    icon = Icons.Default.CheckCircle,
                    onClick = { selectedStatFilter = "Healthy Assets" }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    title = "Due Inspection",
                    count = dashboardSummary.inspectionDue.toString(),
                    color = AmberAlert,
                    icon = Icons.Default.Pending,
                    onClick = { selectedStatFilter = "Due for Inspection" }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    title = "Overdue",
                    count = dashboardSummary.overdueInspections.toString(),
                    color = OrangeAlert,
                    icon = Icons.Default.AssignmentLate,
                    onClick = { selectedStatFilter = "Overdue Inspections" }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    title = "Maintenance Due",
                    count = dashboardSummary.maintenanceDue.toString(),
                    color = OrangeAlert,
                    icon = Icons.Default.Build,
                    onClick = { selectedStatFilter = "Maintenance Due" }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    title = "Critical Assets",
                    count = dashboardSummary.criticalAssets.toString(),
                    color = FireRed,
                    icon = Icons.Default.Warning,
                    onClick = { selectedStatFilter = "Faulty / Critical Assets" }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Status details section
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Status Details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateBlue
                        )
                        Text(
                            text = "$selectedStatFilter (${filteredStatAssets.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    TextButton(
                        onClick = onViewCatalog,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Open Directory", color = FireRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, null, tint = FireRed, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (filteredStatAssets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No assets match this category", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    filteredStatAssets.forEach { asset ->
                        DashboardStatAssetCard(
                            asset = asset,
                            onInspect = { onPerformInspection(asset) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upcoming Inspections
        Text(
            text = "Upcoming Scheduled Inspections",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SlateBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (upcomingInspections.isEmpty()) {
                    Text("No scheduled inspections", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    upcomingInspections.forEach { asset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPerformInspection(asset) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = asset.id,
                                    fontWeight = FontWeight.Bold,
                                    color = FireRed,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = asset.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Location: ${asset.location}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (asset.status) {
                                                "Operational" -> Color(0xFFE8F5E9)
                                                "Damaged" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF8E1)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = asset.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (asset.status) {
                                            "Operational" -> Color(0xFF2E7D32)
                                            "Damaged" -> FireRed
                                            else -> AmberAlert
                                        }
                                    )
                                }
                                Text(
                                    text = "Due: ${asset.nextInspectionDue}",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions Row
        Text(
            text = "FAMS Core Safety Tasks",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SlateBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                QuickActionRow(
                    title = "Add New Asset",
                    description = "Register a brand new fire safety component or device to the system.",
                    icon = Icons.Default.AddCircle,
                    iconTint = FireRed,
                    onClick = onAddAssetClick
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                QuickActionRow(
                    title = "Scan QR Tag",
                    description = "Activate barcode scan simulator to instantly verify assets.",
                    icon = Icons.Default.QrCodeScanner,
                    iconTint = SlateBlue,
                    onClick = onSelectScannerTab
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                QuickActionRow(
                    title = "Reports & Visual Analytics",
                    description = "Generate safety compliance charts, trends, and distribution reports.",
                    icon = Icons.Default.BarChart,
                    iconTint = Color(0xFF4CAF50),
                    onClick = onSelectReportsTab
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Audit Log Activities
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Operations Log",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SlateBlue
            )
            TextButton(
                onClick = {
                    isRefreshing = true
                    Toast.makeText(context, "Refreshed operational feed!", Toast.LENGTH_SHORT).show()
                    isRefreshing = false
                }
            ) {
                Text("Refresh Feed", color = FireRed, fontSize = 13.sp)
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (recentActivities.isEmpty()) {
                    Text("No activities logged today", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    recentActivities.take(10).forEach { log ->
                        val actIcon = when (log.type) {
                            "Asset Added" -> Icons.Default.AddBox
                            "Inspection Completed" -> Icons.Default.Verified
                            "Maintenance Closed" -> Icons.Default.TaskAlt
                            "QR Scanned" -> Icons.Default.PhotoCamera
                            else -> Icons.Default.EditCalendar
                        }
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SlateBlue.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(actIcon, null, tint = SlateBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(log.user, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${log.date} ${log.time}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text(
                                    log.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Configure Preview Use Case
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                // Configure Barcode Scanner options for QR codes
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                // Configure ImageAnalysis Use Case
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                var isScanning = true

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        if (isScanning) {
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (rawValue != null) {
                                            isScanning = false
                                            onQrCodeDetected(rawValue)
                                            break
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    // Handle failure silently
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    android.util.Log.e("CameraPreview", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerTab(
    assets: List<Asset>,
    onInspectAsset: (Asset) -> Unit,
    onOpenQrHub: () -> Unit
) {
    val context = LocalContext.current
    var inputAssetId by remember { mutableStateOf("") }
    var scanPulseOffset by remember { mutableStateOf(0f) }
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    
    // Automatically request camera permission at runtime on entering the scanner tab
    LaunchedEffect(key1 = cameraPermissionState.status.isGranted) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    // Pulse animation simulation for visual laser line
    LaunchedEffect(key1 = true) {
        while (true) {
            kotlinx.coroutines.delay(30)
            scanPulseOffset = (scanPulseOffset + 3f) % 240f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FAMS Intelligent Scanner",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SlateBlue
        )
        Text(
            text = "Aim camera at any Asset QR Tag or manual identifier",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Viewfinder / Permission Request block
        if (!cameraPermissionState.status.isGranted) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.NoPhotography,
                        contentDescription = "Camera Access Blocked",
                        tint = FireRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Camera Access Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlateBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FAMS uses the camera to scan physical asset QR codes directly and retrieve inspection forms instantly.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GRANT CAMERA ACCESS")
                    }
                }
            }
        } else {
            // Viewfinder block with Real CameraX Preview
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(3.dp, FireRed, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeDetected = { scannedCode ->
                        inputAssetId = scannedCode
                        val matched = assets.find { it.id.equals(scannedCode, ignoreCase = true) }
                        if (matched != null) {
                            Toast.makeText(context, "QR Found: ${matched.id}", Toast.LENGTH_SHORT).show()
                            onInspectAsset(matched)
                        } else {
                            Toast.makeText(context, "Scanned Tag: $scannedCode (Not found in catalog)", Toast.LENGTH_LONG).show()
                        }
                    }
                )

                // Laser lines over live preview
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val size = this.size
                    // Draw camera corner indicators
                    val len = 30f
                    val strokeW = 8f
                    
                    // Top-Left
                    drawLine(FireRed, Offset(15f, 15f), Offset(15f + len, 15f), strokeW)
                    drawLine(FireRed, Offset(15f, 15f), Offset(15f, 15f + len), strokeW)

                    // Top-Right
                    drawLine(FireRed, Offset(size.width - 15f, 15f), Offset(size.width - 15f - len, 15f), strokeW)
                    drawLine(FireRed, Offset(size.width - 15f, 15f), Offset(size.width - 15f, 15f + len), strokeW)

                    // Bottom-Left
                    drawLine(FireRed, Offset(15f, size.height - 15f), Offset(15f + len, size.height - 15f), strokeW)
                    drawLine(FireRed, Offset(15f, size.height - 15f), Offset(15f, size.height - 15f - len), strokeW)

                    // Bottom-Right
                    drawLine(FireRed, Offset(size.width - 15f, size.height - 15f), Offset(size.width - 15f - len, size.height - 15f), strokeW)
                    drawLine(FireRed, Offset(size.width - 15f, size.height - 15f), Offset(size.width - 15f, size.height - 15f - len), strokeW)
                    
                    // Active Green Scan Pulse Line
                    drawLine(
                        color = Color.Green.copy(alpha = 0.8f),
                        start = Offset(15f, 15f + scanPulseOffset),
                        end = Offset(size.width - 15f, 15f + scanPulseOffset),
                        strokeWidth = 6f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Custom Manual Entry form
        OutlinedTextField(
            value = inputAssetId,
            onValueChange = { inputAssetId = it },
            label = { Text("Manual Asset ID / QR Value") },
            placeholder = { Text("FE-101") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.85f).testTag("scanner_manual_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val matched = assets.find { it.id.equals(inputAssetId, ignoreCase = true) }
                if (matched != null) {
                    onInspectAsset(matched)
                } else {
                    Toast.makeText(context, "Asset ID $inputAssetId not found in local catalog", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = FireRed),
            modifier = Modifier.fillMaxWidth(0.85f).height(48.dp)
        ) {
            Icon(Icons.Default.Verified, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SUBMIT TAG VALUE")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (assets.isNotEmpty()) {
                    val randAsset = assets.random()
                    Toast.makeText(context, "Simulating Scan: Found ${randAsset.id}!", Toast.LENGTH_SHORT).show()
                    onInspectAsset(randAsset)
                } else {
                    Toast.makeText(context, "No assets registered to scan", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(0.85f).height(48.dp)
        ) {
            Icon(Icons.Default.PhotoCamera, null, tint = FireRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("MOCK CAMERA SCAN", color = FireRed)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onOpenQrHub,
            colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
            modifier = Modifier.fillMaxWidth(0.85f).height(48.dp)
        ) {
            Icon(Icons.Default.QrCode, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("QR GENERATOR & PRINT HUB")
        }
    }
}

@Composable
fun ReportsTab(
    assetDistribution: List<com.example.models.AssetSummary>,
    inspectionSummary: com.example.models.InspectionSummary,
    maintenanceSummary: com.example.models.MaintenanceSummary
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "FAMS Compliance Reports & Trends",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SlateBlue
        )
        Text(
            text = "Real-time analytics representation of safety assets across plant layers",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 1. Chart 1: Asset Distribution (Pie Chart)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Asset Distribution breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateBlue)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Draw Pie / Custom Arc graphics
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(140.dp)) {
                        val strokeWidth = 35f
                        val radiusSize = size.minDimension - strokeWidth
                        
                        // Custom wedges mapping types
                        drawArc(Color(0xFFE53935), 0f, 100f, false, style = Stroke(strokeWidth)) // Extinguishers
                        drawArc(Color(0xFF3949AB), 100f, 80f, false, style = Stroke(strokeWidth)) // Detectors
                        drawArc(Color(0xFFFFA000), 180f, 60f, false, style = Stroke(strokeWidth)) // Hydrants
                        drawArc(Color(0xFF00ACC1), 240f, 70f, false, style = Stroke(strokeWidth)) // Sprinklers
                        drawArc(Color(0xFF43A047), 310f, 50f, false, style = Stroke(strokeWidth)) // Alarm Panels
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    LegendItem("Extinguishers", Color(0xFFE53935))
                    LegendItem("Detectors", Color(0xFF3949AB))
                    LegendItem("Hydrants", Color(0xFFFFA000))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceAround) {
                    LegendItem("Sprinklers", Color(0xFF00ACC1))
                    LegendItem("Panels", Color(0xFF43A047))
                }
            }
        }

        // 2. Chart 2: Monthly Inspections (Bar Chart)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Monthly Verification Activity", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateBlue)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val space = 40f
                        val width = 50f
                        
                        // Render 4 simple vertical columns
                        drawRect(Color(0xFF4CAF50), Offset(space * 2, 40f), Size(width, size.height - 40f)) // completed
                        drawRect(Color(0xFFFFC107), Offset(space * 5, 80f), Size(width, size.height - 80f)) // pending
                        drawRect(Color(0xFFF44336), Offset(space * 8, 110f), Size(width, size.height - 110f)) // overdue
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LegendItem("Completed (${inspectionSummary.completed})", Color(0xFF4CAF50))
                    LegendItem("Pending (${inspectionSummary.pending})", Color(0xFFFFC107))
                    LegendItem("Overdue (${inspectionSummary.overdue})", Color(0xFFF44336))
                }
            }
        }

        // 3. Chart 3: Maintenance Trend (Line Chart)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("12-Month System Fault Trend", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateBlue)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(0f, size.height - 50f)
                            lineTo(size.width * 0.2f, size.height - 90f)
                            lineTo(size.width * 0.4f, size.height - 40f)
                            lineTo(size.width * 0.6f, size.height - 120f)
                            lineTo(size.width * 0.8f, size.height - 70f)
                            lineTo(size.width, size.height - 110f)
                        }
                        drawPath(path, color = FireRed, style = Stroke(width = 6f))
                    }
                }
            }
        }

        // 4. Chart 4: Asset Health (Donut Chart)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Asset Health Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateBlue)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidth = 30f
                        drawArc(Color(0xFF4CAF50), 0f, 240f, false, style = Stroke(strokeWidth)) // Healthy
                        drawArc(Color(0xFFFFC107), 240f, 90f, false, style = Stroke(strokeWidth)) // Attention Required
                        drawArc(Color(0xFFF44336), 330f, 30f, false, style = Stroke(strokeWidth)) // Critical
                    }
                    Text("Overall Good", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    LegendItem("Healthy", Color(0xFF4CAF50))
                    LegendItem("Attention", Color(0xFFFFC107))
                    LegendItem("Critical", Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// Global Alert Color Definitions
val OrangeAlert = Color(0xFFFF5722)

@Composable
fun MetricCard(
    title: String,
    count: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = count,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun QuickActionRow(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun DashboardStatAssetCard(
    asset: Asset,
    onInspect: () -> Unit
) {
    val statusColor = when (asset.status) {
        "Operational" -> Color(0xFF2E7D32)
        "Under Inspection" -> AmberAlert
        "Damaged" -> FireRed
        else -> OrangeAlert
    }

    val conditionColor = when (asset.condition) {
        "New", "Good" -> Color(0xFF2E7D32)
        "Fair" -> AmberAlert
        "Poor", "Damaged" -> FireRed
        else -> Color.Gray
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (asset.type) {
                            "Fire Extinguisher" -> Icons.Default.Shield
                            "Smoke Detector" -> Icons.Default.Notifications
                            "Fire Hydrant" -> Icons.Default.Build
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = SlateBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = asset.id,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateBlue
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Status Badge
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        contentColor = statusColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = asset.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    // Condition Badge
                    Surface(
                        color = conditionColor.copy(alpha = 0.1f),
                        contentColor = conditionColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = asset.condition,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = asset.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = asset.location,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Next Inspection Due",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = if (asset.nextInspectionDue.isBlank() || asset.nextInspectionDue == "Not inspected") "N/A" else asset.nextInspectionDue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onInspect,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect", fontSize = 11.sp)
                }
            }
        }
    }
}

// Sub Tab: Assets Catalog
@Composable
fun AssetsCatalogTab(
    viewModel: DashboardViewModel,
    userRole: String,
    userName: String
) {
    FireAssetManagementModule(
        viewModel = viewModel,
        userRole = userRole,
        userName = userName
    )
}

@Composable
fun AssetItemCard(
    asset: Asset,
    onInspect: () -> Unit
) {
    val statusColor = when (asset.status) {
        "Operational" -> Color(0xFF2E7D32)
        "Under Inspection" -> AmberAlert
        else -> FireRed
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = asset.id,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateBlue
                )
                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = asset.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = asset.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Location: ${asset.location}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Next Checkup Due",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = asset.nextInspectionDue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onInspect,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verify Safety", fontSize = 12.sp)
                }
            }
        }
    }
}

// Sub Tab: Inspections Log
@Composable
fun InspectionsHistoryTab(
    assets: List<Asset>,
    onPerformCustomInspection: () -> Unit
) {
    // Generate some simulated inspection list for a beautiful UI view
    val simulatedInspections = listOf(
        Inspection("INSP-8120", "FE-101", "John Doe", "tech-uid", "2026-07-08", "Passed", "Gauge reading within green zone; lock pins intact.", "Normal", "Normal", "Good"),
        Inspection("INSP-7712", "AP-401", "System Admin", "admin-uid", "2026-07-01", "Failed", "Faulty logic board on Zone 3 loop sensor.", "Normal", "Low", "Good"),
        Inspection("INSP-6211", "SD-204", "John Doe", "tech-uid", "2026-06-28", "Passed", "Aerosol test passed within 4 seconds response.", "Normal", "Normal", "Good")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Site Safety Audit Log",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SlateBlue
            )
            Button(
                onClick = onPerformCustomInspection,
                colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Inspect Asset", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(simulatedInspections) { inspection ->
                InspectionHistoryCard(inspection = inspection)
            }
        }
    }
}

@Composable
fun InspectionHistoryCard(inspection: Inspection) {
    val statusColor = if (inspection.status == "Passed") Color(0xFF2E7D32) else FireRed
    val statusIcon = if (inspection.status == "Passed") Icons.Default.CheckCircle else Icons.Default.Cancel

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = inspection.assetId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SlateBlue
                    )
                    Text(
                        text = inspection.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
                
                Text(
                    text = "Log: ${inspection.id} • Auditor: ${inspection.inspectorName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = inspection.comments,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PortalSettingsTab(
    userName: String,
    userEmail: String,
    userRole: String,
    authViewModel: AuthViewModel,
    onLogoutNavigate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdminConsole: () -> Unit
) {
    val context = LocalContext.current
    var isMuteNotifications by remember { mutableStateOf(false) }
    var isEmergencyBroadcasting by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Enterprise Profile",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SlateBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(FireRed.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = FireRed, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = userEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        Surface(
                            color = AmberAlert.copy(alpha = 0.15f),
                            contentColor = Color(0xFFC68400),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = "Access: $userRole",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                
                TextButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("nav_to_full_profile_button")
                ) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = FireRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VIEW & EDIT FULL PROFILE", fontWeight = FontWeight.Bold, color = FireRed)
                }
            }
        }

        // Administrative Access Portal Button
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable { onNavigateToAdminConsole() }
                .testTag("admin_console_nav_button")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enterprise Admin Console",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Central dashboard, user directories, compliance metrics",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
            }
        }

        Text(
            text = "Notification Settings",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SlateBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Routine Checkup Reminders", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Get notified when an asset's inspection is due", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                    Switch(checked = !isMuteNotifications, onCheckedChange = { isMuteNotifications = !it })
                }
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Instant Defect Alerts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Emergency broadcasts for failed asset audits", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                    Switch(checked = isEmergencyBroadcasting, onCheckedChange = { isEmergencyBroadcasting = it })
                }
            }
        }

        // About & Support Info
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateBlue.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Fire Safety Asset Management System (FAMS)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Version 1.0.0 (Enterprise Release)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("For emergency system issues or regulatory reporting integrations, contact the FAMS central administrator desk.", fontSize = 12.sp)
            }
        }

        // Final Explicit Sign Out Button
        Button(
            onClick = {
                authViewModel.logout()
                Toast.makeText(context, "Logged out safely", Toast.LENGTH_SHORT).show()
                onLogoutNavigate()
            },
            colors = ButtonDefaults.buttonColors(containerColor = FireRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("settings_logout_button")
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SECURE SIGN OUT", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
