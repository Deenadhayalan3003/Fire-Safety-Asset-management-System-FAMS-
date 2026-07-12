package com.example.admin

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.User
import java.text.SimpleDateFormat
import java.util.*

// Colors
private val SlateBlue = Color(0xFF1E293B)
private val FireRed = Color(0xFFDC2626)
private val AccentOrange = Color(0xFFF97316)
private val TealClean = Color(0xFF0F766E)
private val CardBackground = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val users by viewModel.usersList.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val plants by viewModel.plantsList.collectAsState()
    val customRoles by viewModel.customRolesList.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Users, 2: Plants, 3: Roles/Permissions, 4: Audit Trails

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FAMS CENTRAL CONSOLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Enterprise Administration & Compliance Portal",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.refreshData()
                        Toast.makeText(context, "System state synchronized", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateBlue)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Enterprise Navigation Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SlateBlue.copy(alpha = 0.05f),
                contentColor = SlateBlue,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = FireRed
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Users", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Plants", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Roles", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Audit Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "admin_tabs"
            ) { targetTab ->
                when (targetTab) {
                    0 -> OverviewTabContent(state)
                    1 -> UsersTabContent(users, viewModel)
                    2 -> PlantsTabContent(plants, viewModel)
                    3 -> RolesAndPermissionsTabContent(customRoles, viewModel)
                    4 -> AuditLogsTabContent(auditLogs)
                }
            }
        }
    }
}

@Composable
fun OverviewTabContent(state: AdminDashboardState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Operational Metrics Dashboard",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SlateBlue
            )
        }

        // Metrics Grid (Rows of beautiful stats)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Total Users",
                        value = "${state.totalUsers}",
                        subtitle = "Active directory accounts",
                        icon = Icons.Default.SupervisedUserCircle,
                        color = SlateBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Online Users",
                        value = "${state.onlineUsers}",
                        subtitle = "Active live sessions",
                        icon = Icons.Default.Wifi,
                        color = TealClean,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Compliance",
                        value = "${(state.inspectionCompliance * 100).toInt()}%",
                        subtitle = "Inspections passed on-time",
                        icon = Icons.Default.FactCheck,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Critical Alerts",
                        value = "${state.criticalAlertsCount}",
                        subtitle = "Defects needing attention",
                        icon = Icons.Default.Warning,
                        color = FireRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "FAMS Plants",
                        value = "${state.totalPlants}",
                        subtitle = "Industrial complexes",
                        icon = Icons.Default.Business,
                        color = AccentOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "System Health",
                        value = "${(state.systemHealth * 100).toInt()}%",
                        subtitle = "API & Firebase uptime",
                        icon = Icons.Default.OfflineBolt,
                        color = TealClean,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section: Live Activity Visualization Charts
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Asset Distribution Matrix",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SlateBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Segmented Radial Doughnut Chart
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension / 2
                                val strokeWidth = 14.dp.toPx()
                                var currentAngle = -90f
                                val totalAssets = state.assetDistribution.values.sum()

                                val colors = listOf(
                                    FireRed, AccentOrange, SlateBlue, TealClean,
                                    Color(0xFF8B5CF6), Color(0xFFEC4899)
                                )

                                state.assetDistribution.entries.forEachIndexed { index, entry ->
                                    val sweep = (entry.value / totalAssets) * 360f
                                    drawArc(
                                        color = colors[index % colors.size],
                                        startAngle = currentAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                    )
                                    currentAngle += sweep
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${state.totalAssets}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = SlateBlue
                                )
                                Text(
                                    text = "Total Assets",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Chart legends
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val colors = listOf(
                                FireRed, AccentOrange, SlateBlue, TealClean,
                                Color(0xFF8B5CF6), Color(0xFFEC4899)
                            )
                            state.assetDistribution.entries.forEachIndexed { index, entry ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(colors[index % colors.size])
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = entry.key,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${entry.value.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Activity and Trend Graphs
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "User Activity vs. Inspections Trend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SlateBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated bar/trend chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val maxVal = state.inspectionTrendData.maxOrNull() ?: 1

                            // Draw horizontal grid lines
                            val gridLines = 4
                            for (i in 0..gridLines) {
                                val y = height * i / gridLines
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f
                                )
                            }

                            // Draw trend line
                            val points = state.inspectionTrendData
                            val stepX = width / (points.size - 1)
                            for (i in 0 until points.size - 1) {
                                val x1 = i * stepX
                                val y1 = height - (points[i].toFloat() / maxVal * height)
                                val x2 = (i + 1) * stepX
                                val y2 = height - (points[i + 1].toFloat() / maxVal * height)

                                drawLine(
                                    color = FireRed,
                                    start = Offset(x1, y1),
                                    end = Offset(x2, y2),
                                    strokeWidth = 6f,
                                    cap = StrokeCap.Round
                                )

                                drawCircle(
                                    color = FireRed,
                                    radius = 6f,
                                    center = Offset(x1, y1)
                                )
                                if (i == points.size - 2) {
                                    drawCircle(
                                        color = FireRed,
                                        radius = 6f,
                                        center = Offset(x2, y2)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mon", fontSize = 10.sp, color = Color.Gray)
                        Text("Tue", fontSize = 10.sp, color = Color.Gray)
                        Text("Wed", fontSize = 10.sp, color = Color.Gray)
                        Text("Thu", fontSize = 10.sp, color = Color.Gray)
                        Text("Fri", fontSize = 10.sp, color = Color.Gray)
                        Text("Sat", fontSize = 10.sp, color = Color.Gray)
                        Text("Sun", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersTabContent(users: List<User>, viewModel: AdminViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterRole by remember { mutableStateOf("All") }
    var showCreateUserDialog by remember { mutableStateOf(false) }

    val customRoles by viewModel.customRolesList.collectAsState()
    val plants by viewModel.plantsList.collectAsState()

    val baseRoles = listOf("All", "Super Administrator", "Administrator", "Plant Head", "Safety Manager", "Safety Officer", "Fire Technician", "Auditor", "Viewer")
    val roles = (baseRoles + customRoles.map { it.name }).distinct()

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
            Text("Corporate User Directory", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateBlue)
            IconButton(
                onClick = { showCreateUserDialog = true },
                modifier = Modifier
                    .background(FireRed, CircleShape)
                    .size(36.dp)
                    .testTag("add_user_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add User", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, email, or designation...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SlateBlue,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Role Filter horizontal bar
        ScrollableTabRow(
            selectedTabIndex = roles.indexOf(selectedFilterRole).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            indicator = {},
            divider = {}
        ) {
            roles.forEach { roleName ->
                val isSelected = roleName == selectedFilterRole
                Tab(
                    selected = isSelected,
                    onClick = { selectedFilterRole = roleName },
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) SlateBlue else Color.LightGray.copy(alpha = 0.2f)),
                    text = {
                        Text(
                            text = roleName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.DarkGray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Users List
        val filteredUsers = users.filter {
            (selectedFilterRole == "All" || it.role.equals(selectedFilterRole, ignoreCase = true)) &&
                    (searchQuery.isEmpty() || it.fullName.contains(searchQuery, ignoreCase = true) ||
                            it.email.contains(searchQuery, ignoreCase = true))
        }

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Group, null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No users matched search parameters", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredUsers) { user ->
                    UserRowCard(user = user, onUpdateRole = { newRole ->
                        viewModel.updateUserRole(user.uid, newRole)
                    }, onToggleStatus = {
                        val newStatus = if (user.status == "Active") "Inactive" else "Active"
                        viewModel.updateUserStatus(user.uid, newStatus)
                    })
                }
            }
        }
    }

    if (showCreateUserDialog) {
        CreateUserDialog(
            customRoles = customRoles,
            plants = plants,
            onDismiss = { showCreateUserDialog = false },
            onCreate = { name, email, role, dept, plant, mobile, empId, company, expiry, perms ->
                viewModel.createAdvancedUser(name, email, role, dept, plant, mobile, empId, company, expiry, perms)
                showCreateUserDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRowCard(
    user: User,
    onUpdateRole: (String) -> Unit,
    onToggleStatus: () -> Unit
) {
    var expandedRoleMenu by remember { mutableStateOf(false) }
    val roles = listOf("Super Administrator", "Administrator", "Plant Head", "Safety Manager", "Safety Officer", "Fire Technician", "Auditor", "Viewer")

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(user.getDisplayName(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                    Text(user.email, fontSize = 12.sp, color = Color.Gray)
                }

                // Action controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge (Active/Inactive)
                    TextButton(onClick = onToggleStatus) {
                        Surface(
                            color = if (user.status == "Active") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            contentColor = if (user.status == "Active") Color(0xFF15803D) else Color(0xFFB91C1C),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = user.status.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { expandedRoleMenu = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(
                            expanded = expandedRoleMenu,
                            onDismissRequest = { expandedRoleMenu = false }
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role, fontSize = 12.sp) },
                                    onClick = {
                                        onUpdateRole(role)
                                        expandedRoleMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Role / Scope", fontSize = 10.sp, color = Color.Gray)
                    Text(user.role, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateBlue)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Assigned Complex", fontSize = 10.sp, color = Color.Gray)
                    Text(user.plant.ifEmpty { "All Plants" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateBlue)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsTabContent(plants: List<PlantConfig>, viewModel: AdminViewModel) {
    var showCreatePlantDialog by remember { mutableStateOf(false) }
    var plantToEdit by remember { mutableStateOf<PlantConfig?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Industrial Complexes Directory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlateBlue
                    )
                    Text(
                        text = "Register, configure, and monitor safety ratings across physical facilities.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = { showCreatePlantDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("register_complex_button")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REGISTER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(plants) { plant ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, null, tint = SlateBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(plant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                        }

                        // Status Badge
                        Surface(
                            color = when (plant.status) {
                                "Operational" -> Color(0xFFDCFCE7)
                                "Maintenance" -> Color(0xFFFEF9C3)
                                else -> Color(0xFFFEE2E2)
                            },
                            contentColor = when (plant.status) {
                                "Operational" -> Color(0xFF15803D)
                                "Maintenance" -> Color(0xFF854D0E)
                                else -> Color(0xFFB91C1C)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = plant.status.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Location: ${plant.location}", fontSize = 12.sp, color = Color.DarkGray)
                    Text(text = "Facility Manager: ${plant.manager}", fontSize = 12.sp, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Total Zones", fontSize = 10.sp, color = Color.Gray)
                                Text("${plant.zonesCount} Zones", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateBlue)
                            }
                            Column {
                                Text("Active Staff", fontSize = 10.sp, color = Color.Gray)
                                Text("${plant.activeStaffCount} Engineers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateBlue)
                            }
                            Column {
                                Text("Safety Rating", fontSize = 10.sp, color = Color.Gray)
                                Text("${plant.safetyScore}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (plant.safetyScore >= 95) TealClean else FireRed)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { plantToEdit = plant },
                                modifier = Modifier.testTag("edit_plant_button_${plant.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Plant", tint = SlateBlue, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { viewModel.deletePlant(plant.id) },
                                modifier = Modifier.testTag("delete_plant_button_${plant.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Decommission Plant", tint = FireRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlantDialog) {
        CreatePlantDialog(
            onDismiss = { showCreatePlantDialog = false },
            onCreate = { name, loc, mgr, zones ->
                viewModel.createPlant(name, loc, mgr, zones)
                showCreatePlantDialog = false
            }
        )
    }

    if (plantToEdit != null) {
        EditPlantDialog(
            plant = plantToEdit!!,
            onDismiss = { plantToEdit = null },
            onUpdate = { name, loc, mgr, zones, status, rating ->
                viewModel.updatePlant(plantToEdit!!.id, name, loc, mgr, zones, status, rating)
                plantToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RolesAndPermissionsTabContent(customRoles: List<CustomRole>, viewModel: AdminViewModel) {
    var showCreateRoleDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enterprise Role Designer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlateBlue
                    )
                    Text(
                        text = "Define and customize roles to regulate granular field application permissions.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = { showCreateRoleDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_custom_role_button")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DESIGN ROLE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(customRoles) { role ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, null, tint = SlateBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(role.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                        }

                        if (role.isSystemRole) {
                            Surface(
                                color = SlateBlue.copy(alpha = 0.1f),
                                contentColor = SlateBlue,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "SYSTEM PROTECTED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.deleteCustomRole(role.id) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete_role_button_${role.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Retire Role", tint = FireRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = role.description, fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Assigned Permissions Matrix:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        role.permissions.forEach { perm ->
                            Surface(
                                color = SlateBlue.copy(alpha = 0.05f),
                                contentColor = SlateBlue,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(Icons.Default.Check, null, tint = TealClean, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(perm, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateRoleDialog) {
        CreateRoleDialog(
            onDismiss = { showCreateRoleDialog = false },
            onCreate = { name, desc, perms ->
                viewModel.createCustomRole(name, desc, perms)
                showCreateRoleDialog = false
            }
        )
    }
}

@Composable
fun AuditLogsTabContent(logs: List<AuditLogEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "System Audit Trails",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SlateBlue
            )
            Text(
                text = "Immutable compliance log for central monitoring and enterprise inspection verification.",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        items(logs) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when {
                                log.action.contains("CREATE") -> Color(0xFFDCFCE7)
                                log.action.contains("UPDATE") -> Color(0xFFFEF9C3)
                                log.action.contains("DELETE") -> Color(0xFFFEE2E2)
                                else -> Color(0xFFE2E8F0)
                            },
                            contentColor = when {
                                log.action.contains("CREATE") -> Color(0xFF15803D)
                                log.action.contains("UPDATE") -> Color(0xFF854D0E)
                                log.action.contains("DELETE") -> Color(0xFFB91C1C)
                                else -> SlateBlue
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = log.action,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        val date = Date(log.timestamp)
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        Text(
                            text = sdf.format(date),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = log.details, fontSize = 12.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = log.user, fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(text = "Module: ${log.module}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateUserDialog(
    customRoles: List<CustomRole>,
    plants: List<PlantConfig>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, String, String, String, String, Set<String>) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) } // Step 1: General, Step 2: Contact/ID, Step 3: Permissions

    // General Form Fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Safety Officer") }
    var department by remember { mutableStateOf("Safety Inspection") }
    var plant by remember { mutableStateOf("Plant 1") }

    // Professional/Contact Fields
    var mobile by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var companyCode by remember { mutableStateOf("FAMS-GLOBAL") }
    var expiryDuration by remember { mutableStateOf("Never") }

    // Granular Permissions
    val allPossiblePermissions = listOf(
        "asset:read" to "View Assets (Observe asset conditions, records, and location parameters)",
        "asset:write" to "Modify Assets (Configure new equipment, scan markers, and retire hardware)",
        "inspection:log" to "Log Inspections (Submit scheduled defect forms and compliance checklists)",
        "inspection:approve" to "Approve Compliance (Sign off monthly plant-wide integrity safety forms)",
        "user:manage" to "Manage System Users (Authorize console logins, design roles, and view trails)"
    )
    val selectedPermissions = remember { mutableStateListOf<String>("asset:read", "inspection:log") }

    // Validation Errors
    var validationError by remember { mutableStateOf<String?>(null) }

    val rolesList = (listOf("Super Administrator", "Administrator", "Plant Head", "Safety Manager", "Safety Officer", "Fire Technician", "Auditor", "Viewer") + customRoles.map { it.name }).distinct()
    val plantsList = (listOf("Plant 1", "Plant 2", "Plant 3", "Plant 4") + plants.map { it.name }).distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Enterprise Account Provisioning",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SlateBlue
                )
                Text(
                    text = "Step $currentStep of 3: " + when (currentStep) {
                        1 -> "General Details"
                        2 -> "Professional Identifiers"
                        else -> "App Access Permissions"
                    },
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // If there's a validation error, show it prominently
                if (validationError != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        contentColor = Color(0xFFB91C1C),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = validationError!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                when (currentStep) {
                    1 -> {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                validationError = null
                            },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                validationError = null
                            },
                            label = { Text("Corporate Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Role Dropdown Selection
                        var roleExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { roleExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Assigned Role: $role", fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                            DropdownMenu(
                                expanded = roleExpanded,
                                onDismissRequest = { roleExpanded = false }
                            ) {
                                rolesList.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r, fontSize = 13.sp) },
                                        onClick = {
                                            role = r
                                            // Automatically pre-populate default department based on role choice
                                            department = when (r) {
                                                "Super Administrator", "Administrator" -> "Corporate Safety"
                                                "Plant Head" -> "Operations"
                                                "Safety Manager", "Safety Officer" -> "Safety Inspection"
                                                "Fire Technician" -> "Maintenance"
                                                "Auditor" -> "Compliance"
                                                else -> "External Audit"
                                            }
                                            roleExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text("Department Scope") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Plant Dropdown Selection
                        var plantExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { plantExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Primary Complex: $plant", fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                            DropdownMenu(
                                expanded = plantExpanded,
                                onDismissRequest = { plantExpanded = false }
                            ) {
                                plantsList.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p, fontSize = 13.sp) },
                                        onClick = {
                                            plant = p
                                            plantExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        OutlinedTextField(
                            value = employeeId,
                            onValueChange = {
                                employeeId = it
                                validationError = null
                            },
                            label = { Text("Employee ID Card Number") },
                            placeholder = { Text("e.g. EMP-2041") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = mobile,
                            onValueChange = {
                                mobile = it
                                validationError = null
                            },
                            label = { Text("Mobile Contact Number") },
                            placeholder = { Text("e.g. +1 555-0199") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = companyCode,
                            onValueChange = { companyCode = it },
                            label = { Text("Corporate Company Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Expiry Timeframe options
                        Text("Account Expiration Frame:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                        val expiryOptions = listOf("Never", "30 Days Guest Account", "90 Days Temporary Contract")
                        expiryOptions.forEach { opt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expiryDuration = opt }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = expiryDuration == opt,
                                    onClick = { expiryDuration = opt }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(opt, fontSize = 12.sp)
                            }
                        }
                    }

                    3 -> {
                        Text(
                            text = "Regulate access scope below. Permissions govern what views and operations are authorized for this user:",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        allPossiblePermissions.forEach { (permKey, permDesc) ->
                            val isSelected = selectedPermissions.contains(permKey)
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isSelected) {
                                            selectedPermissions.remove(permKey)
                                        } else {
                                            selectedPermissions.add(permKey)
                                        }
                                        validationError = null
                                    }
                                    .background(if (isSelected) SlateBlue.copy(alpha = 0.05f) else Color.Transparent)
                                    .padding(8.dp)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked == true) {
                                            selectedPermissions.add(permKey)
                                        } else {
                                            selectedPermissions.remove(permKey)
                                        }
                                        validationError = null
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = permKey.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateBlue
                                    )
                                    Text(
                                        text = permDesc,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentStep > 1) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("PREVIOUS")
                    }
                }

                Button(
                    onClick = {
                        // Multi-step validation checks
                        if (currentStep == 1) {
                            if (name.trim().isEmpty()) {
                                validationError = "Corporate record demands a valid Full Name."
                            } else if (email.trim().isEmpty() || !email.contains("@")) {
                                validationError = "Invalid Corporate Email structure (must contain '@')."
                            } else {
                                validationError = null
                                currentStep = 2
                            }
                        } else if (currentStep == 2) {
                            if (employeeId.trim().isEmpty()) {
                                validationError = "Employee Identifier Card Number is mandatory."
                            } else if (mobile.trim().length < 5) {
                                validationError = "Valid Phone Contact Number required (minimum 5 digits)."
                            } else {
                                validationError = null
                                currentStep = 3
                            }
                        } else {
                            if (selectedPermissions.isEmpty()) {
                                validationError = "Access control demands authorizing at least one application permission."
                            } else {
                                onCreate(
                                    name,
                                    email,
                                    role,
                                    department,
                                    plant,
                                    mobile,
                                    employeeId,
                                    companyCode,
                                    expiryDuration,
                                    selectedPermissions.toSet()
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FireRed)
                ) {
                    Text(if (currentStep == 3) "PROVISION ACCOUNT" else "CONTINUE")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun CreatePlantDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var manager by remember { mutableStateOf("") }
    var zonesText by remember { mutableStateOf("6") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Industrial Facility Complex", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Facility Name (e.g. Plant 5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Physical Location (City / Region)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = manager,
                    onValueChange = { manager = it },
                    label = { Text("Appointed Facility Manager") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = zonesText,
                    onValueChange = { zonesText = it },
                    label = { Text("Total Supervised Security Zones") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val zones = zonesText.toIntOrNull()
                    if (name.trim().isEmpty() || location.trim().isEmpty() || manager.trim().isEmpty()) {
                        error = "All fields are required to register a facility complex."
                    } else if (zones == null || zones <= 0) {
                        error = "Zones count must be a positive integer."
                    } else {
                        onCreate(name, location, manager, zones)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SlateBlue)
            ) {
                Text("REGISTER COMPLEX")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun EditPlantDialog(
    plant: PlantConfig,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, Int, String, Float) -> Unit
) {
    var name by remember { mutableStateOf(plant.name) }
    var location by remember { mutableStateOf(plant.location) }
    var manager by remember { mutableStateOf(plant.manager) }
    var zonesText by remember { mutableStateOf(plant.zonesCount.toString()) }
    var status by remember { mutableStateOf(plant.status) }
    var ratingText by remember { mutableStateOf(plant.safetyScore.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    val statusOptions = listOf("Operational", "Maintenance", "Suspended")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Facility Configurations", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Facility Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Physical Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = manager,
                    onValueChange = { manager = it },
                    label = { Text("Appointed Facility Manager") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = zonesText,
                    onValueChange = { zonesText = it },
                    label = { Text("Total Security Zones") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ratingText,
                    onValueChange = { ratingText = it },
                    label = { Text("Current Safety Score (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Facility Operational Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                statusOptions.forEach { opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { status = opt }
                    ) {
                        RadioButton(
                            selected = status == opt,
                            onClick = { status = opt }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(opt, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val zones = zonesText.toIntOrNull()
                    val rating = ratingText.toFloatOrNull()
                    if (name.trim().isEmpty() || location.trim().isEmpty() || manager.trim().isEmpty()) {
                        error = "All fields are required to update facility complex."
                    } else if (zones == null || zones <= 0) {
                        error = "Zones count must be a positive integer."
                    } else if (rating == null || rating < 0f || rating > 100f) {
                        error = "Safety rating must be a float between 0 and 100."
                    } else {
                        onUpdate(name, location, manager, zones, status, rating)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SlateBlue)
            ) {
                Text("SAVE CHANGES")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun CreateRoleDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Set<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val possiblePermissions = listOf(
        "asset:read" to "View Assets & specifications",
        "asset:write" to "Modify, register, or retire assets",
        "inspection:log" to "Submit scheduled maintenance inspections",
        "inspection:approve" to "Approve monthly plant compliance checks",
        "user:manage" to "Manage users and administrative console"
    )
    val selectedPermissions = remember { mutableStateListOf<String>("asset:read") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Design Custom Corporate Role", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Role Title (e.g. Lead Safety Officer)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Role Scope Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Regulate Granular Permissions:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                possiblePermissions.forEach { (permKey, permDesc) ->
                    val isChecked = selectedPermissions.contains(permKey)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) {
                                    selectedPermissions.remove(permKey)
                                } else {
                                    selectedPermissions.add(permKey)
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked == true) {
                                    selectedPermissions.add(permKey)
                                } else {
                                    selectedPermissions.remove(permKey)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(permKey.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(permDesc, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        error = "Role Title cannot be empty."
                    } else if (description.trim().isEmpty()) {
                        error = "Role Scope Description cannot be empty."
                    } else if (selectedPermissions.isEmpty()) {
                        error = "Must authorize at least one application permission."
                    } else {
                        onCreate(name, description, selectedPermissions.toSet())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SlateBlue)
            ) {
                Text("DESIGN ROLE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
