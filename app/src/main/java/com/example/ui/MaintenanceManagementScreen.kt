package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dashboard.DashboardViewModel
import com.example.models.Asset
import com.example.models.TimelineEvent
import com.example.models.Vendor
import com.example.models.WorkOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Design Theme Elements
private val SlateBlue = Color(0xFF2C3E50)
private val FireRed = Color(0xFFC0392B)
private val DarkSlate = Color(0xFF1E272C)
private val CoolGray = Color(0xFF95A5A6)
private val MintGreen = Color(0xFF2ECC71)
private val GoldAmber = Color(0xFFF39C12)

val MAINTENANCE_TASKS = listOf(
    "Fire Extinguisher Refill",
    "Hydro Test",
    "Pressure Check",
    "Valve Replacement",
    "Gauge Replacement",
    "Hose Replacement",
    "Battery Replacement",
    "Smoke Detector Cleaning",
    "Fire Pump Test",
    "Hydrant Flow Test",
    "Emergency Light Battery Test"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MaintenanceManagementDialog(
    viewModel: DashboardViewModel,
    userRole: String,
    userName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val workOrders by viewModel.workOrders.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val vendors by viewModel.vendors.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Active WO, 1: Create Request, 2: Vendors, 3: Cost & Reports
    
    // Create WO dialog/form states
    var createAssetId by remember { mutableStateOf("") }
    var createPriority by remember { mutableStateOf("Medium") } // Low, Medium, High, Critical
    var createType by remember { mutableStateOf("Preventive") } // Preventive, Corrective, Breakdown, Emergency, Calibration, AMC
    var createDescription by remember { mutableStateOf("") }
    var createProblem by remember { mutableStateOf("") }
    var createAssignedTo by remember { mutableStateOf("John Doe") }
    var createVendor by remember { mutableStateOf("") }
    var createDepartment by remember { mutableStateOf("") }
    var createCost by remember { mutableStateOf("") }
    var createBeforePhoto by remember { mutableStateOf("") }

    // Detail dialog
    var selectedWorkOrderForDetail by remember { mutableStateOf<WorkOrder?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("FAMS Maintenance Control", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Dismiss", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SlateBlue)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Engineering, null) },
                        label = { Text("Active WO", fontSize = 11.sp) },
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.PostAdd, null) },
                        label = { Text("Request", fontSize = 11.sp) },
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Business, null) },
                        label = { Text("Vendors", fontSize = 11.sp) },
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Assessment, null) },
                        label = { Text("Reports", fontSize = 11.sp) },
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (activeTab) {
                    0 -> {
                        // TAB 0: WORK ORDERS GRID/LIST
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
                                Text("Work Orders Database", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                                Text("${workOrders.size} logged", fontSize = 12.sp, color = CoolGray)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (workOrders.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No maintenance work orders found", color = CoolGray)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(workOrders) { wo ->
                                        WorkOrderCardItem(
                                            workOrder = wo,
                                            onClick = { selectedWorkOrderForDetail = wo }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // TAB 1: CREATE MAINTENANCE REQUEST
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Log New Maintenance Request", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                            
                            // Select Asset ID dropdown or search field
                            Text("Select Target Asset Code:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                            OutlinedTextField(
                                value = createAssetId,
                                onValueChange = { createAssetId = it },
                                placeholder = { Text("e.g. FE-101") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("wo_create_asset_id")
                            )

                            // Select task / Problem category
                            Text("Select Maintenance Task:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MAINTENANCE_TASKS.take(6).forEach { task ->
                                    val active = createProblem == task
                                    FilterChip(
                                        selected = active,
                                        onClick = { createProblem = task },
                                        label = { Text(task) }
                                    )
                                }
                            }

                            // Priority
                            Text("Set Work Priority:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Low", "Medium", "High", "Critical").forEach { prio ->
                                    val active = createPriority == prio
                                    val color = when(prio) {
                                        "Low" -> MintGreen
                                        "Medium" -> GoldAmber
                                        "High" -> FireRed
                                        else -> DarkSlate
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) color else color.copy(alpha = 0.08f))
                                            .clickable { createPriority = prio }
                                            .border(1.dp, if (active) Color.Transparent else color.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(prio, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else color)
                                    }
                                }
                            }

                            // Type
                            Text("Maintenance Classification:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Preventive", "Corrective", "Breakdown", "Calibration").forEach { ty ->
                                    val active = createType == ty
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) SlateBlue else SlateBlue.copy(alpha = 0.08f))
                                            .clickable { createType = ty }
                                            .border(1.dp, if (active) Color.Transparent else SlateBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(ty, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else SlateBlue)
                                    }
                                }
                            }

                            // Description input
                            OutlinedTextField(
                                value = createDescription,
                                onValueChange = { createDescription = it },
                                label = { Text("Diagnostic Description & Service Plan") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Vendor & Assign
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = createAssignedTo,
                                    onValueChange = { createAssignedTo = it },
                                    label = { Text("Assign Tech") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = createVendor,
                                    onValueChange = { createVendor = it },
                                    label = { Text("Service Vendor") },
                                    placeholder = { Text("e.g. FireShield Ltd") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = createDepartment,
                                    onValueChange = { createDepartment = it },
                                    label = { Text("Dept Area") },
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = createCost,
                                    onValueChange = { createCost = it },
                                    label = { Text("Est. Cost ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.8f)
                                )
                            }

                            // Before Photo attachment simulation
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateBlue.copy(alpha = 0.04f)),
                                border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AttachFile, null, tint = SlateBlue)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Before-Repair Photo Evidence", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(createBeforePhoto.ifEmpty { "No photo attached yet" }, fontSize = 11.sp, color = CoolGray)
                                    }
                                    Button(
                                        onClick = {
                                            createBeforePhoto = "Evidence_Fault_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("SNAP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (createAssetId.isEmpty() || createProblem.isEmpty()) {
                                        Toast.makeText(context, "Please specify Asset ID and Maintenance Task", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    val matchedAsset = assets.find { it.id.equals(createAssetId, ignoreCase = true) }
                                    
                                    val newWO = WorkOrder(
                                        workOrderNumber = "WO-${1000 + (workOrders.size + 1)}",
                                        assetId = createAssetId.uppercase(),
                                        assetName = matchedAsset?.name ?: "Fire Safety Component",
                                        priority = createPriority,
                                        description = createDescription.ifEmpty { "Maintenance required: $createProblem" },
                                        problem = createProblem,
                                        assignedTo = createAssignedTo,
                                        vendor = createVendor.ifEmpty { matchedAsset?.manufacturer ?: "Standard FAMS Vendor" },
                                        department = createDepartment.ifEmpty { matchedAsset?.department ?: "Warehouse" },
                                        startDate = dateStr,
                                        dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 14 * 24 * 3600 * 1000L)),
                                        status = "Open",
                                        maintenanceType = createType,
                                        cost = createCost.toDoubleOrNull() ?: 0.0,
                                        beforePhotoUrl = createBeforePhoto,
                                        timeline = listOf(
                                            TimelineEvent("Open", dateStr + " 10:00", "Work order logged manually.", userName)
                                        )
                                    )
                                    viewModel.createWorkOrder(newWO)
                                    Toast.makeText(context, "Work order ${newWO.workOrderNumber} logged!", Toast.LENGTH_SHORT).show()
                                    
                                    // Reset fields
                                    createAssetId = ""
                                    createDescription = ""
                                    createProblem = ""
                                    createBeforePhoto = ""
                                    createCost = ""
                                    activeTab = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("wo_submit_button")
                            ) {
                                Icon(Icons.Default.Check, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LOG WORK ORDER REQUEST")
                            }
                        }
                    }
                    2 -> {
                        // TAB 2: VENDOR DIRECTORY & PERFORMANCE
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
                                Text("Approved Service Contractors", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                                Text("${vendors.size} active", fontSize = 12.sp, color = CoolGray)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(vendors) { vendor ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                                    Text("Contact: ${vendor.contactPerson}", fontSize = 12.sp, color = CoolGray)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, "Rating", tint = GoldAmber, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(vendor.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("SUPPORT SERVICES PROVIDED:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoolGray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                maxItemsInEachRow = 3
                                            ) {
                                                vendor.services.forEach { service ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(SlateBlue.copy(alpha = 0.06f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(service, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = SlateBlue)
                                                    }
                                                }
                                            }
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SlateBlue.copy(alpha = 0.06f))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Phone, null, tint = SlateBlue, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(vendor.phone, fontSize = 11.sp, color = DarkSlate)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Email, null, tint = SlateBlue, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(vendor.email, fontSize = 11.sp, color = DarkSlate)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // TAB 3: COST & REPORTS ANALYTICS
                        val completedOrders = workOrders.filter { it.status == "Completed" || it.status == "Closed" }
                        val openOrders = workOrders.filter { it.status == "Open" || it.status == "Assigned" || it.status == "In Progress" }
                        val totalSpent = workOrders.sumOf { it.cost }
                        val preventiveSpent = workOrders.filter { it.maintenanceType == "Preventive" }.sumOf { it.cost }
                        val correctiveSpent = workOrders.filter { it.maintenanceType == "Corrective" || it.maintenanceType == "Breakdown" }.sumOf { it.cost }
                        val totalDowntime = workOrders.sumOf { it.downtimeMinutes }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Maintenance & Reliability Analytics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                            
                            // Metrics Grid Rows
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CostMetricsCard(title = "CUMULATIVE SPEND", value = "$${totalSpent.toInt()}", subText = "All operations logs", color = SlateBlue, modifier = Modifier.weight(1f))
                                CostMetricsCard(title = "SYSTEM DOWNTIME", value = "${totalDowntime}m", subText = "Critical offline clocks", color = FireRed, modifier = Modifier.weight(1f))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CostMetricsCard(title = "PREVENTIVE COST", value = "$${preventiveSpent.toInt()}", subText = "Standard schedule cost", color = MintGreen, modifier = Modifier.weight(1f))
                                CostMetricsCard(title = "CORRECTIVE COST", value = "$${correctiveSpent.toInt()}", subText = "Failure response cost", color = GoldAmber, modifier = Modifier.weight(1f))
                            }

                            // Work order ratio
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("WORK ORDER COMPLETION RATIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoolGray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${completedOrders.size} Completed", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MintGreen)
                                            Text("${openOrders.size} Active Pending", fontSize = 12.sp, color = CoolGray)
                                        }
                                        
                                        val pct = if (workOrders.isNotEmpty()) (completedOrders.size.toFloat() / workOrders.size * 100).toInt() else 100
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(SlateBlue.copy(alpha = 0.06f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$pct%", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SlateBlue)
                                        }
                                    }
                                }
                            }

                            // Cost breakdown bar visualization
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("SPENDING DISTRIBUTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoolGray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Simulated bar charts
                                    CategoryCostBar("Preventive Servicing", preventiveSpent, totalSpent, MintGreen)
                                    CategoryCostBar("Corrective Repairs", correctiveSpent, totalSpent, GoldAmber)
                                    CategoryCostBar("Emergency Breakdown Support", workOrders.filter { it.maintenanceType == "Emergency" }.sumOf { it.cost }, totalSpent, FireRed)
                                    CategoryCostBar("Calibration & Tests", workOrders.filter { it.maintenanceType == "Calibration" }.sumOf { it.cost }, totalSpent, SlateBlue)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DETAIL PANEL DIALOG
    if (selectedWorkOrderForDetail != null) {
        WorkOrderDetailDialog(
            workOrder = selectedWorkOrderForDetail!!,
            userRole = userRole,
            userName = userName,
            viewModel = viewModel,
            onDismiss = { selectedWorkOrderForDetail = null }
        )
    }
}

@Composable
fun CategoryCostBar(
    category: String,
    cost: Double,
    total: Double,
    color: Color
) {
    val pct = if (total > 0) (cost / total).toFloat() else 0f
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category, fontSize = 11.sp, color = DarkSlate)
            Text("$${cost.toInt()} (${(pct * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(SlateBlue.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = pct.coerceAtLeast(0.02f))
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun CostMetricsCard(
    title: String,
    value: String,
    subText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subText, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun WorkOrderCardItem(
    workOrder: WorkOrder,
    onClick: () -> Unit
) {
    val prioColor = when (workOrder.priority) {
        "Low" -> MintGreen
        "Medium" -> GoldAmber
        "High" -> FireRed
        else -> DarkSlate
    }

    val statusColor = when (workOrder.status) {
        "Completed", "Closed" -> MintGreen
        "In Progress" -> SlateBlue
        "Waiting Spare" -> GoldAmber
        "Cancelled" -> CoolGray
        else -> FireRed
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        workOrder.workOrderNumber,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = SlateBlue,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(prioColor.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        workOrder.priority,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = prioColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(workOrder.description, fontSize = 13.sp, color = DarkSlate, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Asset ID: ${workOrder.assetId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoolGray)
                Text("Status: ${workOrder.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkOrderDetailDialog(
    workOrder: WorkOrder,
    userRole: String,
    userName: String,
    viewModel: DashboardViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var notesInput by remember { mutableStateOf("") }
    
    // Status workflows update
    var currentStatus by remember { mutableStateOf(workOrder.status) }
    
    // Attachment simulated strings
    var afterPhotoUrl by remember { mutableStateOf(workOrder.afterPhotoUrl) }
    var invoiceUrl by remember { mutableStateOf(workOrder.invoiceUrl) }
    var completionCertificateUrl by remember { mutableStateOf(workOrder.completionCertificateUrl) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Work Order detail: ${workOrder.workOrderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SlateBlue)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Core Profile
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = SlateBlue.copy(alpha = 0.05f))) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(workOrder.assetName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                            Text("Asset ID: ${workOrder.assetId} • Type: ${workOrder.maintenanceType}", fontSize = 11.sp, color = CoolGray)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("DUE DATE", fontSize = 9.sp, color = CoolGray, fontWeight = FontWeight.Bold)
                                    Text(workOrder.dueDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ASSIGNED VENDOR", fontSize = 9.sp, color = CoolGray, fontWeight = FontWeight.Bold)
                                    Text(workOrder.vendor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // 2. Troubleshooting Problem & Action
                item {
                    Text("Service Requirement Description", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateBlue)
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.08f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("PROBLEM DIAGNOSED:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FireRed)
                            Text(workOrder.problem, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SlateBlue.copy(alpha = 0.05f))
                            Text("DETAILED DESCRIPTION:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                            Text(workOrder.description, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                // 3. ATTACHMENT HUB
                item {
                    Text("Service Attachments & Certificates", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AttachmentStatusItem("Before Photo Evidence", workOrder.beforePhotoUrl.ifEmpty { "None" }, true)
                            AttachmentStatusItem(
                                title = "After Photo Evidence",
                                statusText = afterPhotoUrl.ifEmpty { "Pending capture" },
                                hasAttached = afterPhotoUrl.isNotEmpty(),
                                onTriggerAction = {
                                    afterPhotoUrl = "Evidence_Resolved_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
                                }
                            )
                            AttachmentStatusItem(
                                title = "Vendor Service Report / Invoice",
                                statusText = invoiceUrl.ifEmpty { "Pending bill file" },
                                hasAttached = invoiceUrl.isNotEmpty(),
                                onTriggerAction = {
                                    invoiceUrl = "INV_SEC_${System.currentTimeMillis().toString().takeLast(4)}.pdf"
                                }
                            )
                            AttachmentStatusItem(
                                title = "Completion Certificate",
                                statusText = completionCertificateUrl.ifEmpty { "Pending certification" },
                                hasAttached = completionCertificateUrl.isNotEmpty(),
                                onTriggerAction = {
                                    completionCertificateUrl = "CERT_COMP_${System.currentTimeMillis().toString().takeLast(4)}.pdf"
                                }
                            )
                        }
                    }
                }

                // 4. Status Workflow Actions
                item {
                    Text("Update Work Order Status Workflow", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val statuses = listOf("Open", "Assigned", "In Progress", "Waiting Spare", "Completed", "Closed", "Cancelled")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        statuses.forEach { st ->
                            val active = currentStatus == st
                            val isAllowed = when(userRole) {
                                "Technician" -> st in listOf("In Progress", "Waiting Spare", "Completed")
                                else -> true // Admins can do everything
                            }
                            
                            FilterChip(
                                selected = active,
                                onClick = {
                                    if (isAllowed) {
                                        currentStatus = st
                                    } else {
                                        Toast.makeText(context, "Only Admins can set status to $st", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(st) },
                                enabled = isAllowed
                            )
                        }
                    }
                }

                // Comment log field
                item {
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Log State Change Remarks") },
                        placeholder = { Text("Explain parts replaced, test outcomes, or delay reasons...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 5. Timeline History
                item {
                    Text("Work Order Timeline History", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        workOrder.timeline.forEach { event ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(SlateBlue)
                                        .padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("[${event.status}]", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FireRed)
                                        Text(event.timestamp, fontSize = 10.sp, color = CoolGray)
                                    }
                                    Text(event.note, fontSize = 11.sp, color = DarkSlate)
                                    Text("Logged by: ${event.user}", fontSize = 9.sp, color = CoolGray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 6. Action Button Save Changes
                item {
                    Button(
                        onClick = {
                            val today = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                            val newEvent = TimelineEvent(
                                status = currentStatus,
                                timestamp = today,
                                note = notesInput.ifEmpty { "Status updated to $currentStatus" },
                                user = userName
                            )
                            
                            val updatedWO = workOrder.copy(
                                status = currentStatus,
                                afterPhotoUrl = afterPhotoUrl,
                                invoiceUrl = invoiceUrl,
                                completionCertificateUrl = completionCertificateUrl,
                                completionDate = if (currentStatus == "Completed" || currentStatus == "Closed") SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else workOrder.completionDate,
                                timeline = workOrder.timeline + newEvent
                            )
                            viewModel.updateWorkOrder(updatedWO)
                            Toast.makeText(context, "Work order ${workOrder.workOrderNumber} updated successfully!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("wo_save_detail_button")
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE CHANGES & UPDATE WORKFLOW")
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentStatusItem(
    title: String,
    statusText: String,
    hasAttached: Boolean,
    onTriggerAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
            Text(statusText, fontSize = 10.sp, color = if (hasAttached) MintGreen else CoolGray)
        }
        if (onTriggerAction != null) {
            Button(
                onClick = onTriggerAction,
                colors = ButtonDefaults.buttonColors(containerColor = if (hasAttached) MintGreen else SlateBlue),
                contentPadding = PaddingValues(horizontal = 10.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(if (hasAttached) Icons.Default.Check else Icons.Default.AttachFile, null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (hasAttached) "UPLOADED" else "ATTACH", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Icon(
                imageVector = if (hasAttached) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (hasAttached) MintGreen else CoolGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
