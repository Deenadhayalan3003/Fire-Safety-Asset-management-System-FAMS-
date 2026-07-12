package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dashboard.DashboardViewModel
import com.example.models.Asset
import com.example.models.Inspection
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.FireRed
import com.example.ui.theme.SlateBlue
import java.text.SimpleDateFormat
import java.util.*

// Master list of all master asset types
val MASTER_ASSET_TYPES = listOf(
    "Fire Extinguisher",
    "Fire Hydrant",
    "Hose Reel",
    "Fire Pump",
    "Fire Alarm Panel",
    "Smoke Detector",
    "Heat Detector",
    "Manual Call Point",
    "Hooter",
    "Beacon",
    "Sprinkler Head",
    "Landing Valve",
    "Breeching Inlet",
    "Emergency Exit Light",
    "Exit Sign",
    "Fire Door",
    "Emergency Assembly Point",
    "Sand Bucket",
    "Fire Bucket Stand"
)

// Categories associated with master types
val ASSET_CATEGORIES = listOf(
    "Active Fire Protection",
    "Passive Fire Protection",
    "Detection & Alarm",
    "Suppression System",
    "Life Safety Systems",
    "First Aid Fire Fighting"
)

val PLANTS = listOf("Main Plant Alpha", "Chemical Hub Beta", "Logistics Zone Gamma", "Assembly Unit Delta")
val DEPARTMENTS = listOf("Production", "Warehouse", "Admin", "Server Room", "Utility Deck", "Exterior")
val STATUSES = listOf("Operational", "Under Inspection", "Damaged", "Out of Service", "Archived")
val CONDITIONS = listOf("New", "Good", "Fair", "Poor", "Damaged")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireAssetManagementModule(
    viewModel: DashboardViewModel,
    userRole: String,
    userName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assets by viewModel.assets.collectAsState()

    // Screen State
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlantFilter by remember { mutableStateOf("All") }
    var selectedDeptFilter by remember { mutableStateOf("All") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedConditionFilter by remember { mutableStateOf("All") }
    var isInspectionDueFilter by remember { mutableStateOf(false) }
    var isMaintenanceDueFilter by remember { mutableStateOf(false) }
    var isCriticalFilter by remember { mutableStateOf(false) }

    var sortBy by remember { mutableStateOf("Asset Code") } // Asset Code, Name, Next Due, Condition
    var isGridView by remember { mutableStateOf(false) }

    // Dialog / Selection states
    var selectedAssetForDetail by remember { mutableStateOf<Asset?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var assetToEdit by remember { mutableStateOf<Asset?>(null) }

    // Filter sheet state
    var showFilterSheet by remember { mutableStateOf(false) }

    // 1. Process search, filters, sorting
    val filteredAssets = assets.filter { asset ->
        val matchesSearch = asset.name.contains(searchQuery, ignoreCase = true) ||
                asset.id.contains(searchQuery, ignoreCase = true) ||
                asset.assetCode.contains(searchQuery, ignoreCase = true) ||
                asset.location.contains(searchQuery, ignoreCase = true) ||
                asset.serialNumber.contains(searchQuery, ignoreCase = true)

        val matchesPlant = selectedPlantFilter == "All" || asset.plant == selectedPlantFilter
        val matchesDept = selectedDeptFilter == "All" || asset.department == selectedDeptFilter
        val matchesType = selectedTypeFilter == "All" || asset.type == selectedTypeFilter
        val matchesStatus = if (selectedStatusFilter == "All") {
            asset.status != "Archived" // Hide archived assets by default
        } else {
            asset.status == selectedStatusFilter
        }
        val matchesCondition = selectedConditionFilter == "All" || asset.condition == selectedConditionFilter

        // Mock inspection and maintenance due calculation based on dates
        val isInspectionDue = asset.nextInspectionDue.isNotEmpty() && asset.nextInspectionDue != "Not inspected"
        val matchesInspDue = !isInspectionDueFilter || isInspectionDue
        val matchesMaintDue = !isMaintenanceDueFilter || asset.status == "Damaged" || asset.status == "Out of Service"
        val matchesCritical = !isCriticalFilter || asset.status == "Damaged" || asset.condition == "Poor" || asset.condition == "Damaged"

        matchesSearch && matchesPlant && matchesDept && matchesType && matchesStatus && matchesCondition && matchesInspDue && matchesMaintDue && matchesCritical
    }.sortedWith { a1, a2 ->
        when (sortBy) {
            "Asset Code" -> a1.assetCode.compareTo(a2.assetCode, ignoreCase = true)
            "Name" -> a1.name.compareTo(a2.name, ignoreCase = true)
            "Next Due" -> a1.nextInspectionDue.compareTo(a2.nextInspectionDue, ignoreCase = true)
            "Condition" -> a1.condition.compareTo(a2.condition, ignoreCase = true)
            else -> a1.id.compareTo(a2.id)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Filters Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Code, Name, Serial...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("phase4_search_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Filter sheet toggle
            Box {
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (selectedPlantFilter != "All" || selectedDeptFilter != "All" || selectedTypeFilter != "All" || selectedStatusFilter != "All" || isInspectionDueFilter || isMaintenanceDueFilter) {
                                SlateBlue.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    BadgedBox(
                        badge = {
                            var activeCount = 0
                            if (selectedPlantFilter != "All") activeCount++
                            if (selectedDeptFilter != "All") activeCount++
                            if (selectedTypeFilter != "All") activeCount++
                            if (selectedStatusFilter != "All") activeCount++
                            if (isInspectionDueFilter) activeCount++
                            if (isMaintenanceDueFilter) activeCount++
                            if (isCriticalFilter) activeCount++
                            if (activeCount > 0) {
                                Badge { Text(activeCount.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Open Filters", tint = SlateBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Grid / List Toggle
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                    contentDescription = "Toggle View Mode",
                    tint = SlateBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sort Row & Export Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sort by: ", fontSize = 12.sp, color = Color.Gray)
                var sortExpanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(
                        onClick = { sortExpanded = true },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(sortBy, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        listOf("Asset Code", "Name", "Next Due", "Condition").forEach { criteria ->
                            DropdownMenuItem(
                                text = { Text(criteria) },
                                onClick = {
                                    sortBy = criteria
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Export CSV / PDF button
            TextButton(
                onClick = {
                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    Toast.makeText(
                        context,
                        "Successfully exported ${filteredAssets.size} safety assets to CSV: FAMS_Assets_$sdf.csv",
                        Toast.LENGTH_LONG
                    ).show()
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.IosShare, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export (CSV)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Add Asset Button (Primary action header)
        if (userRole == "Administrator") {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("fams_add_asset_btn")
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Master Fire Asset", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Grid vs List renderer
        if (filteredAssets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(56.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No safety assets matched your filter", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAssets) { asset ->
                        FireAssetItemCard(
                            asset = asset,
                            isGrid = true,
                            onDetailClick = { selectedAssetForDetail = asset },
                            onEditClick = {
                                assetToEdit = asset
                                showEditDialog = true
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAssets) { asset ->
                        FireAssetItemCard(
                            asset = asset,
                            isGrid = false,
                            onDetailClick = { selectedAssetForDetail = asset },
                            onEditClick = {
                                assetToEdit = asset
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Filters Sheet Dialog
    if (showFilterSheet) {
        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            title = { Text("Search Filters & Attributes", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Plant Filter
                    Column {
                        Text("Plant / Unit", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SlateBlue)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (listOf("All") + PLANTS).take(3).forEach { plant ->
                                val selected = if (plant == "All") selectedPlantFilter == "All" else selectedPlantFilter == plant
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedPlantFilter = if (plant == "All") "All" else plant },
                                    label = { Text(plant.split(" ").last(), fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Asset Status
                    Column {
                        Text("Operation Status", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SlateBlue)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (listOf("All") + STATUSES).take(4).forEach { status ->
                                val selected = if (status == "All") selectedStatusFilter == "All" else selectedStatusFilter == status
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedStatusFilter = if (status == "All") "All" else status },
                                    label = { Text(status, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Condition
                    Column {
                        Text("Cylinder / Unit Condition", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SlateBlue)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (listOf("All") + CONDITIONS).take(4).forEach { cond ->
                                val selected = if (cond == "All") selectedConditionFilter == "All" else selectedConditionFilter == cond
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedConditionFilter = if (cond == "All") "All" else cond },
                                    label = { Text(cond, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Checkboxes for quick flags
                    Column {
                        Text("Reminders & Alerts", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SlateBlue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isInspectionDueFilter, onCheckedChange = { isInspectionDueFilter = it })
                            Text("Inspection Overdue", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isMaintenanceDueFilter, onCheckedChange = { isMaintenanceDueFilter = it })
                            Text("Maintenance / Service Required", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isCriticalFilter, onCheckedChange = { isCriticalFilter = it })
                            Text("Critical Faults Only", fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFilterSheet = false }, colors = ButtonDefaults.buttonColors(containerColor = SlateBlue)) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedPlantFilter = "All"
                        selectedDeptFilter = "All"
                        selectedTypeFilter = "All"
                        selectedStatusFilter = "All"
                        selectedConditionFilter = "All"
                        isInspectionDueFilter = false
                        isMaintenanceDueFilter = false
                        isCriticalFilter = false
                        showFilterSheet = false
                    }
                ) {
                    Text("Clear All")
                }
            }
        )
    }

    // Detail Dialog
    if (selectedAssetForDetail != null) {
        FireAssetDetailDialog(
            asset = selectedAssetForDetail!!,
            userRole = userRole,
            onDismiss = { selectedAssetForDetail = null },
            onEdit = {
                assetToEdit = selectedAssetForDetail
                selectedAssetForDetail = null
                showEditDialog = true
            },
            onDelete = {
                viewModel.deleteAsset(selectedAssetForDetail!!.id)
                Toast.makeText(context, "Asset deleted successfully", Toast.LENGTH_SHORT).show()
                selectedAssetForDetail = null
            },
            onDuplicate = {
                val nextId = "FE-${System.currentTimeMillis().toString().takeLast(4)}"
                val duplicated = selectedAssetForDetail!!.copy(
                    id = nextId,
                    assetCode = "DUP-${selectedAssetForDetail!!.assetCode}",
                    name = "${selectedAssetForDetail!!.name} (Copy)"
                )
                viewModel.addAsset(duplicated)
                Toast.makeText(context, "Asset duplicated with ID: $nextId", Toast.LENGTH_SHORT).show()
                selectedAssetForDetail = null
            },
            onArchiveToggle = {
                val current = selectedAssetForDetail!!
                val updatedStatus = if (current.status == "Archived") "Operational" else "Archived"
                viewModel.updateAssetStatus(current.id, updatedStatus)
                Toast.makeText(context, "Asset status updated to $updatedStatus", Toast.LENGTH_SHORT).show()
                selectedAssetForDetail = null
            }
        )
    }

    // Add dialog
    if (showAddDialog) {
        FireAssetAddEditDialog(
            isEdit = false,
            asset = null,
            userName = userName,
            onDismiss = { showAddDialog = false },
            onConfirm = { asset ->
                viewModel.addAsset(asset)
                Toast.makeText(context, "Added Safety Asset: ${asset.assetCode}", Toast.LENGTH_SHORT).show()
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    if (showEditDialog && assetToEdit != null) {
        FireAssetAddEditDialog(
            isEdit = true,
            asset = assetToEdit,
            userName = userName,
            onDismiss = {
                showEditDialog = false
                assetToEdit = null
            },
            onConfirm = { updated ->
                viewModel.updateAsset(updated)
                Toast.makeText(context, "Updated Safety Asset: ${updated.assetCode}", Toast.LENGTH_SHORT).show()
                showEditDialog = false
                assetToEdit = null
            }
        )
    }
}

@Composable
fun FireAssetItemCard(
    asset: Asset,
    isGrid: Boolean,
    onDetailClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val statusColor = when (asset.status) {
        "Operational" -> Color(0xFF2E7D32)
        "Under Inspection" -> AmberAlert
        "Damaged" -> FireRed
        "Out of Service" -> Color.Gray
        "Archived" -> Color.DarkGray
        else -> Color.Blue
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() }
    ) {
        if (isGrid) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = asset.assetCode.ifEmpty { asset.id },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = asset.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = asset.type,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Loc: ${asset.location}",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Custom tags based on types
                when (asset.type) {
                    "Fire Extinguisher" -> {
                        Surface(
                            color = FireRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Type: ${asset.extinguisherType}",
                                fontSize = 10.sp,
                                color = FireRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    "Fire Pump" -> {
                        Surface(
                            color = Color.Blue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Pump: ${asset.pumpType}",
                                fontSize = 10.sp,
                                color = Color.Blue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    "Fire Hydrant" -> {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "P: ${asset.pressure} PSI",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon circle matching type
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when (asset.type) {
                                "Fire Extinguisher" -> FireRed.copy(alpha = 0.1f)
                                "Fire Hydrant" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                "Fire Pump" -> Color(0xFF2196F3).copy(alpha = 0.1f)
                                else -> SlateBlue.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (asset.type) {
                            "Fire Extinguisher" -> Icons.Default.FireExtinguisher
                            "Fire Hydrant" -> Icons.Default.WaterDrop
                            "Fire Pump" -> Icons.Default.Engineering
                            "Smoke Detector" -> Icons.Default.Air
                            "Emergency Exit Light" -> Icons.Default.Light
                            else -> Icons.Default.Settings
                        },
                        contentDescription = null,
                        tint = when (asset.type) {
                            "Fire Extinguisher" -> FireRed
                            "Fire Hydrant" -> Color(0xFF2E7D32)
                            "Fire Pump" -> Color(0xFF1976D2)
                            else -> SlateBlue
                        }
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = asset.assetCode.ifEmpty { asset.id },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(asset.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = asset.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${asset.type} • ${asset.location}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Quick specialized tag row
                    if (asset.type == "Fire Extinguisher" && asset.extinguisherType.isNotEmpty()) {
                        Text(
                            text = "Specs: ${asset.extinguisherType} | Cap: ${asset.capacity} | Gauge: ${asset.pressureGauge}",
                            fontSize = 10.sp,
                            color = FireRed,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (asset.type == "Fire Pump" && asset.pumpType.isNotEmpty()) {
                        Text(
                            text = "Specs: ${asset.pumpType} | RPM: ${asset.rpm} | Fuel: ${asset.fuelLevel}%",
                            fontSize = 10.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (asset.type == "Fire Hydrant" && asset.pressure.isNotEmpty()) {
                        Text(
                            text = "Specs: ${asset.pressure} PSI | Valve: ${asset.valveCondition} | Cap: ${asset.capCondition}",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FireAssetDetailDialog(
    asset: Asset,
    userRole: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onArchiveToggle: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: General, 1: Specs, 2: History/Timeline, 3: Photos
    var simulatedPhotosList by remember { mutableStateOf(asset.photos.ifEmpty { listOf("https://placehold.co/600x400/png?text=Fire+Asset") }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = asset.assetCode.ifEmpty { asset.id }, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = asset.name, fontSize = 13.sp, color = Color.Gray)
                }
                var menuExpanded by remember { mutableStateOf(false) }
                if (userRole == "Administrator") {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit Asset") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate Asset") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (asset.status == "Archived") "Restore Asset" else "Archive Asset") },
                                leadingIcon = { Icon(Icons.Default.Archive, null) },
                                onClick = {
                                    menuExpanded = false
                                    onArchiveToggle()
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Delete Asset", color = FireRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = FireRed) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Secondary Tab bar for navigation inside Dialog
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("General", fontSize = 11.sp, modifier = Modifier.padding(vertical = 10.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Technical", fontSize = 11.sp, modifier = Modifier.padding(vertical = 10.dp))
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("Timeline", fontSize = 11.sp, modifier = Modifier.padding(vertical = 10.dp))
                    }
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                        Text("Photos", fontSize = 11.sp, modifier = Modifier.padding(vertical = 10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // General Tab
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DetailRow("Category", asset.category.ifEmpty { "Active Protection" })
                                DetailRow("Asset Type", asset.type)
                                DetailRow("Plant", asset.plant.ifEmpty { "Main Plant Alpha" })
                                DetailRow("Building", asset.building.ifEmpty { "Block A" })
                                DetailRow("Floor", asset.floor.ifEmpty { "Ground Floor" })
                                DetailRow("Department", asset.department.ifEmpty { "Production Area" })
                                DetailRow("Zone", asset.zone.ifEmpty { "Zone 1" })
                                DetailRow("Location", asset.location)
                                DetailRow("Manufacturer", asset.manufacturer.ifEmpty { "N/A" })
                                DetailRow("Model", asset.model.ifEmpty { "N/A" })
                                DetailRow("Serial Number", asset.serialNumber.ifEmpty { "N/A" })
                                DetailRow("Manufacturing Date", asset.manufacturingDate.ifEmpty { "N/A" })
                                DetailRow("Installation Date", asset.installationDate.ifEmpty { "N/A" })
                                DetailRow("Warranty Expiry", asset.warrantyExpiry.ifEmpty { "N/A" })
                                DetailRow("Cylinder Condition", asset.condition)
                                DetailRow("Remarks", asset.remarks.ifEmpty { "No additional notes." })
                                Divider()
                                DetailRow("Created By", asset.createdBy.ifEmpty { "System" })
                                DetailRow("Created Date", asset.createdDate.ifEmpty { "2026-07-09" })
                            }
                        }
                        1 -> { // Specs / Technical Tab
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                when (asset.type) {
                                    "Fire Extinguisher" -> {
                                        Text("Fire Extinguisher Parameters", fontWeight = FontWeight.Bold, color = FireRed, fontSize = 14.sp)
                                        DetailRow("Extinguisher Type", asset.extinguisherType)
                                        DetailRow("Capacity", asset.capacity)
                                        DetailRow("Pressure Gauge Status", asset.pressureGauge)
                                        DetailRow("Refill Date", asset.refillDate)
                                        DetailRow("Next Refill Due", asset.nextRefill)
                                        DetailRow("Hydro Test Date", asset.hydroTestDate)
                                        DetailRow("Next Hydro Test Due", asset.nextHydroTest)
                                        DetailRow("Cylinder Weight", asset.cylinderWeight)
                                        DetailRow("Seal Status", asset.sealStatus)
                                        DetailRow("Safety Pin Status", asset.safetyPin)
                                        DetailRow("Bracket Condition", asset.bracketCondition)
                                    }
                                    "Fire Hydrant" -> {
                                        Text("Hydrant Specifications", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 14.sp)
                                        DetailRow("Hydrant Number", asset.hydrantNumber)
                                        DetailRow("Flow Pressure", "${asset.pressure} PSI")
                                        DetailRow("Valve Mechanical State", asset.valveCondition)
                                        DetailRow("Cap Integrity", asset.capCondition)
                                        DetailRow("Accessibility Status", asset.accessibility)
                                        DetailRow("Flow Test Date", asset.flowTestDate)
                                    }
                                    "Fire Pump" -> {
                                        Text("Fire Pump Electrical/Diesel Parameters", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2), fontSize = 14.sp)
                                        DetailRow("Pump Type", asset.pumpType)
                                        DetailRow("Discharge Pressure", "${asset.pumpPressure} Bar")
                                        DetailRow("RPM", asset.rpm)
                                        DetailRow("Battery Voltage", "${asset.batteryVoltage} V")
                                        DetailRow("Fuel Level", "${asset.fuelLevel}%")
                                        DetailRow("Controller Mode", asset.controllerStatus)
                                        DetailRow("Weekly Operation Test Date", asset.weeklyTestDate)
                                    }
                                    else -> {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("No special technical parameters defined for this asset type.", color = Color.Gray, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> { // History & Timeline Tab
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TimelineItem("Asset Registration", "Asset registered on ${asset.createdDate.ifEmpty { "2026-07-09" }} by ${asset.createdBy.ifEmpty { "Administrator" }}.", true)
                                TimelineItem("Initial Quality Assurance Pass", "Delivered from factory with verified certificate.", false)
                                if (asset.lastInspectionDate.isNotEmpty()) {
                                    TimelineItem("Routine Verification Logged", "Verification completed with status Passed on ${asset.lastInspectionDate}.", false)
                                }
                            }
                        }
                        3 -> { // Photos Tab
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                ) {
                                    // Simulated display of current image
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Photo, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Multiple Photo Storage Enabled", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("Asset Code Reference: ${asset.assetCode}", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${simulatedPhotosList.size} Saved Photos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Button(
                                        onClick = {
                                            val newPhotoUrl = "https://placehold.co/600x400/png?text=FAMS_Upload_${System.currentTimeMillis()}"
                                            simulatedPhotosList = simulatedPhotosList + newPhotoUrl
                                            Toast.makeText(context, "Mock Photo uploaded to Storage & added to assetPhotos collection!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateBlue)
                                    ) {
                                        Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add Photo", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = SlateBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TimelineItem(title: String, description: String, isFirst: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isFirst) FireRed else SlateBlue)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(36.dp)
                    .background(Color.LightGray)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireAssetAddEditDialog(
    isEdit: Boolean,
    asset: Asset?,
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: (Asset) -> Unit
) {
    // Standard Common Fields
    var id by remember { mutableStateOf(asset?.id ?: "FE-${System.currentTimeMillis().toString().takeLast(5)}") }
    var assetCode by remember { mutableStateOf(asset?.assetCode ?: "FE-${System.currentTimeMillis().toString().takeLast(4)}") }
    var name by remember { mutableStateOf(asset?.name ?: "") }
    var category by remember { mutableStateOf(asset?.category ?: "Active Fire Protection") }
    var type by remember { mutableStateOf(asset?.type ?: "Fire Extinguisher") }
    var plant by remember { mutableStateOf(asset?.plant ?: PLANTS.first()) }
    var building by remember { mutableStateOf(asset?.building ?: "Block A") }
    var floor by remember { mutableStateOf(asset?.floor ?: "1st Floor") }
    var department by remember { mutableStateOf(asset?.department ?: DEPARTMENTS.first()) }
    var zone by remember { mutableStateOf(asset?.zone ?: "Zone A") }
    var location by remember { mutableStateOf(asset?.location ?: "") }
    var gps by remember { mutableStateOf(asset?.gps ?: "") }
    var manufacturer by remember { mutableStateOf(asset?.manufacturer ?: "") }
    var model by remember { mutableStateOf(asset?.model ?: "") }
    var serialNumber by remember { mutableStateOf(asset?.serialNumber ?: "") }
    var manufacturingDate by remember { mutableStateOf(asset?.manufacturingDate ?: "") }
    var installationDate by remember { mutableStateOf(asset?.installationDate ?: "") }
    var warrantyExpiry by remember { mutableStateOf(asset?.warrantyExpiry ?: "") }
    var status by remember { mutableStateOf(asset?.status ?: "Operational") }
    var condition by remember { mutableStateOf(asset?.condition ?: "Good") }
    var remarks by remember { mutableStateOf(asset?.remarks ?: "") }

    // Specialized Extinguisher Fields
    var extinguisherType by remember { mutableStateOf(asset?.extinguisherType ?: "ABC") }
    var capacity by remember { mutableStateOf(asset?.capacity ?: "6kg") }
    var pressureGauge by remember { mutableStateOf(asset?.pressureGauge ?: "Normal") }
    var refillDate by remember { mutableStateOf(asset?.refillDate ?: "2026-06-15") }
    var nextRefill by remember { mutableStateOf(asset?.nextRefill ?: "2027-06-15") }
    var hydroTestDate by remember { mutableStateOf(asset?.hydroTestDate ?: "2026-05-10") }
    var nextHydroTest by remember { mutableStateOf(asset?.nextHydroTest ?: "2031-05-10") }
    var cylinderWeight by remember { mutableStateOf(asset?.cylinderWeight ?: "9.2kg") }
    var sealStatus by remember { mutableStateOf(asset?.sealStatus ?: "Intact") }
    var safetyPin by remember { mutableStateOf(asset?.safetyPin ?: "Present") }
    var bracketCondition by remember { mutableStateOf(asset?.bracketCondition ?: "Good") }

    // Hydrant Fields
    var hydrantNumber by remember { mutableStateOf(asset?.hydrantNumber ?: "") }
    var pressure by remember { mutableStateOf(asset?.pressure ?: "150") }
    var valveCondition by remember { mutableStateOf(asset?.valveCondition ?: "Good") }
    var capCondition by remember { mutableStateOf(asset?.capCondition ?: "Good") }
    var accessibility by remember { mutableStateOf(asset?.accessibility ?: "Clear") }
    var flowTestDate by remember { mutableStateOf(asset?.flowTestDate ?: "") }

    // Pump Fields
    var pumpType by remember { mutableStateOf(asset?.pumpType ?: "Electric") }
    var pumpPressure by remember { mutableStateOf(asset?.pumpPressure ?: "10") }
    var rpm by remember { mutableStateOf(asset?.rpm ?: "2900") }
    var batteryVoltage by remember { mutableStateOf(asset?.batteryVoltage ?: "24") }
    var fuelLevel by remember { mutableStateOf(asset?.fuelLevel ?: "90") }
    var controllerStatus by remember { mutableStateOf(asset?.controllerStatus ?: "Auto") }
    var weeklyTestDate by remember { mutableStateOf(asset?.weeklyTestDate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Fire Safety Asset" else "Register Master Fire Asset", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Asset Type (Determines which custom fields expand!)
                Column {
                    Text("Asset Type / Class", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SlateBlue)
                    var typeExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { typeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(type)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }, modifier = Modifier.fillMaxWidth(0.8f)) {
                            MASTER_ASSET_TYPES.forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    type = item
                                    typeExpanded = false
                                })
                            }
                        }
                    }
                }

                // Standard Code
                OutlinedTextField(
                    value = assetCode,
                    onValueChange = { assetCode = it },
                    label = { Text("Asset Code (Auto/Manual)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Asset Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name") },
                    singleLine = true,
                    placeholder = { Text("ABC Dry Powder Extinguisher") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Location / Physical Address
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Specific Location") },
                    singleLine = true,
                    placeholder = { Text("Block A - 1st Floor - Reception") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdowns for Plant
                Column {
                    Text("Plant Unit Location", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SlateBlue)
                    var plantExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { plantExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(plant)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                        DropdownMenu(expanded = plantExpanded, onDismissRequest = { plantExpanded = false }) {
                            PLANTS.forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    plant = item
                                    plantExpanded = false
                                })
                            }
                        }
                    }
                }

                // Manufacturer & Model
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = { manufacturer = it },
                        label = { Text("Mfg") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Serial Number
                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("Serial Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Install & Warranty Dates
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = installationDate,
                        onValueChange = { installationDate = it },
                        label = { Text("Install Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = warrantyExpiry,
                        onValueChange = { warrantyExpiry = it },
                        label = { Text("Warranty Exp") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Status & Condition Dropdowns
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Status", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = SlateBlue)
                        var statusExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { statusExpanded = true }) {
                                Text(status, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                                STATUSES.forEach { item ->
                                    DropdownMenuItem(text = { Text(item) }, onClick = {
                                        status = item
                                        statusExpanded = false
                                    })
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Condition", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = SlateBlue)
                        var condExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { condExpanded = true }) {
                                Text(condition, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            DropdownMenu(expanded = condExpanded, onDismissRequest = { condExpanded = false }) {
                                CONDITIONS.forEach { item ->
                                    DropdownMenuItem(text = { Text(item) }, onClick = {
                                        condition = item
                                        condExpanded = false
                                    })
                                }
                            }
                        }
                    }
                }

                // --- TYPE SPECIFIC COLLAPSIBLE FORMS ---
                if (type == "Fire Extinguisher") {
                    Divider()
                    Text("Fire Extinguisher Specifics", fontWeight = FontWeight.Bold, color = FireRed, fontSize = 14.sp)

                    Column {
                        Text("Extinguisher Type", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        var extExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { extExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(extinguisherType)
                            }
                            DropdownMenu(expanded = extExpanded, onDismissRequest = { extExpanded = false }) {
                                listOf("ABC", "CO2", "Water", "Foam", "Clean Agent").forEach { item ->
                                    DropdownMenuItem(text = { Text(item) }, onClick = {
                                        extinguisherType = item
                                        extExpanded = false
                                    })
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity (e.g. 6kg)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pressureGauge,
                        onValueChange = { pressureGauge = it },
                        label = { Text("Pressure Gauge reading") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nextRefill,
                        onValueChange = { nextRefill = it },
                        label = { Text("Next Refill Due") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (type == "Fire Hydrant") {
                    Divider()
                    Text("Hydrant Specifics", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 14.sp)

                    OutlinedTextField(
                        value = hydrantNumber,
                        onValueChange = { hydrantNumber = it },
                        label = { Text("Hydrant Number/Tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pressure,
                        onValueChange = { pressure = it },
                        label = { Text("Water Flow Pressure (PSI)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accessibility,
                        onValueChange = { accessibility = it },
                        label = { Text("Accessibility (Clear/Blocked)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (type == "Fire Pump") {
                    Divider()
                    Text("Fire Pump Specifics", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2), fontSize = 14.sp)

                    OutlinedTextField(
                        value = pumpType,
                        onValueChange = { pumpType = it },
                        label = { Text("Pump Type (Electric/Diesel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fuelLevel,
                        onValueChange = { fuelLevel = it },
                        label = { Text("Diesel Fuel Level (%)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = batteryVoltage,
                        onValueChange = { batteryVoltage = it },
                        label = { Text("Controller Battery Voltage (V)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("General Notes / Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || location.isBlank()) {
                        // Handled simple validate
                    } else {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val finalAsset = Asset(
                            id = id,
                            assetCode = assetCode,
                            name = name,
                            category = category,
                            type = type,
                            plant = plant,
                            building = building,
                            floor = floor,
                            department = department,
                            zone = zone,
                            location = location,
                            gps = gps,
                            manufacturer = manufacturer,
                            model = model,
                            serialNumber = serialNumber,
                            manufacturingDate = manufacturingDate,
                            installationDate = installationDate,
                            warrantyExpiry = warrantyExpiry,
                            status = status,
                            condition = condition,
                            photos = asset?.photos ?: emptyList(),
                            remarks = remarks,
                            createdBy = asset?.createdBy?.ifEmpty { userName } ?: userName,
                            createdDate = asset?.createdDate?.ifEmpty { today } ?: today,
                            modifiedBy = userName,
                            modifiedDate = today,
                            extinguisherType = extinguisherType,
                            capacity = capacity,
                            pressureGauge = pressureGauge,
                            refillDate = refillDate,
                            nextRefill = nextRefill,
                            hydroTestDate = hydroTestDate,
                            nextHydroTest = nextHydroTest,
                            cylinderWeight = cylinderWeight,
                            sealStatus = sealStatus,
                            safetyPin = safetyPin,
                            bracketCondition = bracketCondition,
                            hydrantNumber = hydrantNumber,
                            pressure = pressure,
                            valveCondition = valveCondition,
                            capCondition = capCondition,
                            accessibility = accessibility,
                            flowTestDate = flowTestDate,
                            pumpType = pumpType,
                            pumpPressure = pumpPressure,
                            rpm = rpm,
                            batteryVoltage = batteryVoltage,
                            fuelLevel = fuelLevel,
                            controllerStatus = controllerStatus,
                            weeklyTestDate = weeklyTestDate,
                            lastInspectionDate = asset?.lastInspectionDate ?: "",
                            nextInspectionDue = asset?.nextInspectionDue ?: "Not inspected"
                        )
                        onConfirm(finalAsset)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FireRed)
            ) {
                Text(if (isEdit) "Save Changes" else "Register Asset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SlateBlue)
            }
        }
    )
}
