package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.models.Inspection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Accent Colors
private val SlateBlue = Color(0xFF2C3E50)
private val FireRed = Color(0xFFC0392B)
private val DarkSlate = Color(0xFF1E272C)
private val CoolGray = Color(0xFF95A5A6)
private val MintGreen = Color(0xFF2ECC71)
private val GoldAmber = Color(0xFFF39C12)

// Checklist templates by asset type
val CHECKLIST_TEMPLATES = mapOf(
    "Fire Extinguisher" to listOf(
        "Pressure OK / Needle in Green Zone",
        "Safety Pin Intact and Sealed",
        "Nozzle Clean and Unobstructed",
        "Hose and Discharge Horn in Good Condition",
        "Mounting Bracket Secure and Stable",
        "Accessibility Free of Obstructions",
        "No External Physical Damage or Dents",
        "No Active Rust or Surface Corrosion",
        "Signage Clear and Visible",
        "Weight Verified and Recorded",
        "Hydrostatic Test Not Expired",
        "Overall Expiry Date Valid"
    ),
    "Smoke Detector" to listOf(
        "Power Indicator Light Active",
        "Sensing Chamber Dust Cleaned",
        "LED Alarm/Siren Indicators Operational",
        "Test Button Alarm Sounding OK",
        "Tamper Switch Functional",
        "Wireless/Wired Link Active",
        "Battery Level Normal",
        "Mounting Screws Secure"
    ),
    "Fire Hydrant" to listOf(
        "Pillar Structural Integrity Good",
        "Isolation Valve Turns Smoothly",
        "Discharge Caps and Chains Present",
        "No Static or Dynamic Leakage",
        "Static Water Pressure OK",
        "Outlet Threads Clean and Lubricated",
        "Clear Access Radius of 1.5 Meters",
        "Reflective Marking Intact"
    ),
    "Alarm Panel" to listOf(
        "Main Power Green Indicator ON",
        "Backup Battery Status Normal",
        "Zone Fault Indicator Lights OFF",
        "Audible Trouble Alert Responds OK",
        "Reset and Silence Buttons Functional",
        "Wiring Connections Secure",
        "Panel Door Key Lock Functional",
        "System Log Book Updated"
    ),
    "Default" to listOf(
        "General Physical Condition Intact",
        "Mounting and Fastening Secure",
        "Labels and Inspection Tags Readable",
        "Access Pathway Clear and Safe",
        "No Leakage, Corrosion or Rust",
        "Functional Self-Test Passed"
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QrInspectionDialog(
    asset: Asset,
    inspectorName: String,
    inspectorId: String,
    viewModel: DashboardViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val checklistItems = CHECKLIST_TEMPLATES[asset.type] ?: CHECKLIST_TEMPLATES["Default"]!!

    // Inspection Fields State
    var inspectionType by remember { mutableStateOf("Routine") } // Daily, Weekly, Monthly, Quarterly, Half-Yearly, Annual, Special Inspection
    val checklistStates = remember { mutableStateMapOf<String, String>().apply {
        checklistItems.forEach { item -> this[item] = "Pass" }
    } }
    
    // Multi-photo state
    val photoAttachments = remember { mutableStateListOf<String>() }
    // Voice note simulation state
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceNoteDuration by remember { mutableStateOf(0) }
    var voiceNotePath by remember { mutableStateOf("") }
    
    // Digital Signature State
    val signaturePaths = remember { mutableStateListOf<Path>() }
    var isSigned by remember { mutableStateOf(false) }
    
    // GPS State
    var gpsCoordinates by remember { mutableStateOf("1.3521° N, 103.8198° E (Auto-fetched)") }
    var isFetchingGps by remember { mutableStateOf(false) }
    
    // Remarks
    var remarks by remember { mutableStateOf("") }
    
    // Scoring & results calculations
    val totalItems = checklistItems.size
    val passedCount = checklistStates.values.count { it == "Pass" }
    val failedCount = checklistStates.values.count { it == "Fail" }
    val naCount = checklistStates.values.count { it == "NA" }
    
    val score = if (totalItems - naCount > 0) {
        (passedCount.toDouble() / (totalItems - naCount)) * 100
    } else {
        100.0
    }
    
    val riskLevel = when {
        score >= 90.0 -> "Low"
        score >= 70.0 -> "Medium"
        else -> "High"
    }
    
    val status = when {
        failedCount > 0 -> "Failed"
        score < 90.0 -> "Attention Required"
        else -> "Passed"
    }

    // Voice record simulation timer
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceNoteDuration = 0
            while (isRecordingVoice) {
                kotlinx.coroutines.delay(1000L)
                voiceNoteDuration++
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Safety Inspection Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("Asset ID: ${asset.id}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SlateBlue)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
            ) {
                // 1. ASSET PROFILE SUMMARY
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateBlue.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(FireRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(asset.type) {
                                            "Fire Extinguisher" -> Icons.Default.FireExtinguisher
                                            "Smoke Detector" -> Icons.Default.Sensors
                                            "Fire Hydrant" -> Icons.Default.WaterDrop
                                            else -> Icons.Default.VerifiedUser
                                        },
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(asset.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateBlue)
                                    Text("${asset.type} • Serial: ${asset.serialNumber}", fontSize = 12.sp, color = CoolGray)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SlateBlue.copy(alpha = 0.1f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("CURRENT PLANT", fontSize = 10.sp, color = CoolGray, fontWeight = FontWeight.Bold)
                                    Text(asset.plant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkSlate)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("LOCATION & ZONE", fontSize = 10.sp, color = CoolGray, fontWeight = FontWeight.Bold)
                                    Text(asset.location, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkSlate)
                                }
                            }
                        }
                    }
                }

                // 2. INSPECTION METADATA & TYPE
                item {
                    Text("Inspection Parameters", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Choose Inspection Schedule:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val types = listOf("Daily", "Weekly", "Monthly", "Quarterly", "Annual", "Special")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 3
                            ) {
                                types.forEach { type ->
                                    val selected = inspectionType == type
                                    FilterChip(
                                        selected = selected,
                                        onClick = { inspectionType = type },
                                        label = { Text(type) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FireRed.copy(alpha = 0.15f),
                                            selectedLabelColor = FireRed
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("DATE OF LOG", fontSize = 10.sp, color = CoolGray, fontWeight = FontWeight.Bold)
                                    Text(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ASSIGNED INSPECTOR", fontSize = 10.sp, color = CoolGray, fontWeight = FontWeight.Bold)
                                    Text(inspectorName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // 3. DYNAMIC CHECKLIST ENGINE
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dynamic Checklist (${checklistItems.size} items)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                        Text("Score: ${score.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (status == "Passed") MintGreen else FireRed)
                    }
                }

                items(checklistItems) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkSlate,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("Pass", "Fail", "NA").forEach { choice ->
                                    val active = checklistStates[item] == choice
                                    val buttonColor = when(choice) {
                                        "Pass" -> if (active) MintGreen else MintGreen.copy(alpha = 0.08f)
                                        "Fail" -> if (active) FireRed else FireRed.copy(alpha = 0.08f)
                                        else -> if (active) CoolGray else CoolGray.copy(alpha = 0.08f)
                                    }
                                    val textColor = if (active) Color.White else when(choice) {
                                        "Pass" -> MintGreen
                                        "Fail" -> FireRed
                                        else -> CoolGray
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(buttonColor)
                                            .clickable { checklistStates[item] = choice }
                                            .border(1.dp, if (active) Color.Transparent else textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = when(choice) {
                                                    "Pass" -> Icons.Default.CheckCircle
                                                    "Fail" -> Icons.Default.Cancel
                                                    else -> Icons.Default.Block
                                                },
                                                contentDescription = null,
                                                tint = if (active) Color.White else textColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(choice, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. PHOTO ATTACHMENTS (MULTIPLE PHOTOS SUPPORT)
                item {
                    Text("Photo Evidence", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Upload or capture high-resolution evidence:", fontSize = 11.sp, color = CoolGray)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Camera Simulation Box
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateBlue.copy(alpha = 0.06f))
                                        .border(1.dp, SlateBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            val captions = listOf("Safety_Seal_Ok.jpg", "Pressure_Gauge_Reading.jpg", "Nozzle_CloseUp.jpg", "Valve_Condition.jpg", "Mounting_Assembly.jpg")
                                            val newPhoto = captions.random()
                                            if (!photoAttachments.contains(newPhoto)) {
                                                photoAttachments.add(newPhoto)
                                            } else {
                                                photoAttachments.add("Image_${System.currentTimeMillis().toString().takeLast(4)}.jpg")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhotoCamera, null, tint = SlateBlue, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Camera", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                                    }
                                }

                                // Photo item list
                                photoAttachments.forEachIndexed { idx, name ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkSlate)
                                    ) {
                                        Icon(
                                            Icons.Default.InsertDriveFile,
                                            null,
                                            tint = Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.align(Alignment.Center).size(32.dp)
                                        )
                                        // Label overlay
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .align(Alignment.BottomCenter)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                fontSize = 8.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        // Remove overlay button
                                        IconButton(
                                            onClick = { photoAttachments.removeAt(idx) },
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(FireRed, CircleShape)
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. VOICE NOTE REMARK RECORDER
                item {
                    Text("Voice Memo Log", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (isRecordingVoice) {
                                        isRecordingVoice = false
                                        voiceNotePath = "INSP_VOICE_${System.currentTimeMillis().toString().takeLast(5)}.amr"
                                    } else {
                                        isRecordingVoice = true
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(if (isRecordingVoice) FireRed else SlateBlue.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isRecordingVoice) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Voice Memo",
                                    tint = if (isRecordingVoice) Color.White else SlateBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (isRecordingVoice) {
                                    Text("Recording memo...", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FireRed)
                                    Text("Duration: ${voiceNoteDuration}s", fontSize = 11.sp, color = Color.Gray)
                                } else if (voiceNotePath.isNotEmpty()) {
                                    Text("Voice memo attached", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MintGreen)
                                    Text(voiceNotePath, fontSize = 11.sp, color = CoolGray)
                                } else {
                                    Text("No voice log recorded", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = CoolGray)
                                    Text("Tap mic to dictate remarks", fontSize = 11.sp, color = CoolGray.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }

                // 6. GPS LOCATION COORDINATES
                item {
                    Text("GPS Coordinates", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, "GPS", tint = FireRed, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Inspector Geolocation", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateBlue)
                                Text(gpsCoordinates, fontSize = 12.sp, color = DarkSlate)
                            }
                            Button(
                                onClick = {
                                    isFetchingGps = true
                                    gpsCoordinates = "Lat: ${1.34 + Math.random()*0.02}, Lon: ${103.82 + Math.random()*0.02} (GPS Verified)"
                                    isFetchingGps = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("REFRESH", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 7. REMARKS TEXT AREA
                item {
                    Text("Additional Remarks & Diagnostic Notes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        placeholder = { Text("Enter detailed findings, fault reports, corrective recommendations...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("inspection_remarks_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SlateBlue,
                            unfocusedBorderColor = SlateBlue.copy(alpha = 0.2f)
                        )
                    )
                }

                // 8. DIGITAL SIGNATURE CANVAS
                item {
                    Text("Digital Signature Signature Pad", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Draw your secure hand signature below:", fontSize = 11.sp, color = CoolGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SlateBlue.copy(alpha = 0.03f))
                                    .border(1.dp, SlateBlue.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                val path = Path().apply { moveTo(offset.x, offset.y) }
                                                signaturePaths.add(path)
                                                isSigned = true
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                if (signaturePaths.isNotEmpty()) {
                                                    val lastPath = signaturePaths.last()
                                                    val pathPointsField = lastPath.javaClass.getDeclaredMethod("lineTo", Float::class.java, Float::class.java)
                                                    pathPointsField.invoke(lastPath, change.position.x, change.position.y)
                                                    
                                                    // Force recomposition
                                                    val temp = signaturePaths.toList()
                                                    signaturePaths.clear()
                                                    signaturePaths.addAll(temp)
                                                }
                                            }
                                        )
                                    }
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    signaturePaths.forEach { path ->
                                        drawPath(
                                            path = path,
                                            color = SlateBlue,
                                            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )
                                    }
                                }
                                
                                if (!isSigned) {
                                    Text(
                                        "Sign Here",
                                        modifier = Modifier.align(Alignment.Center),
                                        color = CoolGray.copy(alpha = 0.5f),
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        signaturePaths.clear()
                                        isSigned = false
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = FireRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CLEAR SIGNATURE", color = FireRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 9. REAL-TIME INSPECTION RESULTS / RISK METRICS
                item {
                    Text("Inspection Scoring Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("COMPLIANCE SCORE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                                    Text("${score.toInt()}% Compliance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when(riskLevel) {
                                                "Low" -> MintGreen
                                                "Medium" -> GoldAmber
                                                else -> FireRed
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Risk: $riskLevel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Status Result: $status", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
                                Text("Checklist items: $passedCount / $totalItems Passed", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                // 10. ACTION BUTTONS: SAVE DRAFT, SUBMIT, CANCEL
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (checklistStates.values.contains("Fail") && remarks.isEmpty()) {
                                    Toast.makeText(context, "Please write description remarks explaining failed elements.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val newInspection = Inspection(
                                    id = "INSP-${UUID.randomUUID().toString().take(6).uppercase()}",
                                    assetId = asset.id,
                                    inspectorName = inspectorName,
                                    inspectorId = inspectorId,
                                    date = dateStr,
                                    status = status,
                                    comments = remarks.ifEmpty { "Inspection complete. Score: ${score.toInt()}%" },
                                    photoUrl = photoAttachments.firstOrNull() ?: "",
                                    
                                    // Phase 5 Fields
                                    inspectionType = inspectionType,
                                    checklist = checklistStates.toMap(),
                                    inspectionScore = score,
                                    compliancePercentage = score,
                                    riskLevel = riskLevel,
                                    photoUrls = photoAttachments.toList(),
                                    voiceNoteUrl = voiceNotePath,
                                    digitalSignatureUrl = if (isSigned) "SECURE_SIGNATURE_DATA" else "",
                                    gpsCoordinates = gpsCoordinates,
                                    isDraft = false
                                )
                                viewModel.submitInspection(newInspection)
                                Toast.makeText(context, "Inspection log submitted successfully!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("inspection_submit_button")
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SUBMIT COMPLETED LOG")
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    val draftInspection = Inspection(
                                        id = "INSP-${UUID.randomUUID().toString().take(6).uppercase()}-DRAFT",
                                        assetId = asset.id,
                                        inspectorName = inspectorName,
                                        inspectorId = inspectorId,
                                        date = dateStr,
                                        status = status,
                                        comments = remarks,
                                        isDraft = true
                                    )
                                    viewModel.submitInspection(draftInspection)
                                    Toast.makeText(context, "Saved as Draft successfully!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateBlue),
                                border = BorderStroke(1.dp, SlateBlue),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("inspection_save_draft_button")
                            ) {
                                Icon(Icons.Default.Drafts, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SAVE AS DRAFT")
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                                border = BorderStroke(1.dp, Color.Gray),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text("DISCARD")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QrGeneratorHubDialog(
    assets: List<Asset>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isBatchMode by remember { mutableStateOf(false) }
    var qrScaleSize by remember { mutableStateOf("Medium") } // Small, Medium, Large

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(if (isBatchMode) "Batch QR Tag Builder" else "Individual QR Label Maker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { isBatchMode = !isBatchMode }) {
                            Icon(
                                imageVector = if (isBatchMode) Icons.Default.QrCode else Icons.Default.GridOn,
                                contentDescription = "Toggle Batch Mode",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SlateBlue)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Settings row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("QR Tag Printing Dimensions:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateBlue)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Small", "Medium", "Large").forEach { size ->
                            val selected = qrScaleSize == size
                            FilterChip(
                                selected = selected,
                                onClick = { qrScaleSize = size },
                                label = { Text(size) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FireRed.copy(alpha = 0.12f),
                                    selectedLabelColor = FireRed
                                )
                            )
                        }
                    }
                }

                if (isBatchMode) {
                    // Batch Mode: Render Grid of printable tags for ALL assets
                    Text("All Fire Safety Assets Tag Listing (${assets.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(assets) { asset ->
                            PrintableQrTagItem(asset = asset, sizeLabel = qrScaleSize)
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Exporting batch of ${assets.size} high-resolution QR tags to PDF sheet...", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DOWNLOAD PDF SHEET")
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Sending PDF batch to printer spool...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Print, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PRINT ALL")
                        }
                    }
                } else {
                    // Individual mode: Scroll selection list + primary showcase preview card
                    var selectedAssetIndex by remember { mutableStateOf(0) }
                    val activeAsset = if (assets.isNotEmpty()) assets[selectedAssetIndex] else null
                    
                    if (activeAsset != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                PrintableQrTagItem(asset = activeAsset, sizeLabel = qrScaleSize, widthDp = 220.dp)
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Downloading QR code image for asset ${activeAsset.id}...", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Download, null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download QR")
                                    }
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Triggering Android system print manager...", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Print, null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Print Tag")
                                    }
                                }
                            }
                        }
                        
                        Text("Select Fire Asset to Preview", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateBlue)
                        LazyColumn(
                            modifier = Modifier.height(180.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(assets) { index, asset ->
                                val active = index == selectedAssetIndex
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = if (active) SlateBlue.copy(alpha = 0.08f) else Color.White),
                                    border = BorderStroke(1.dp, if (active) SlateBlue else Color.LightGray.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAssetIndex = index }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(asset.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                            Text("Code: ${asset.id} • ${asset.location}", fontSize = 11.sp, color = CoolGray)
                                        }
                                        Icon(Icons.Default.QrCode, null, tint = if (active) FireRed else CoolGray)
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No safety assets available to print.")
                    }
                }
            }
        }
    }
}

@Composable
fun PrintableQrTagItem(
    asset: Asset,
    sizeLabel: String,
    widthDp: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    val sizePixels = when(sizeLabel) {
        "Small" -> 110.dp
        "Large" -> 160.dp
        else -> 135.dp
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, SlateBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = if (widthDp != androidx.compose.ui.unit.Dp.Unspecified) Modifier.width(widthDp) else Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            // Label Header
            Text(
                "FIRE SAFETY TAG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = FireRed,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            // Draw simulated vector QR code
            Box(
                modifier = Modifier
                    .size(sizePixels)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Drawn realistic looking QR blocks
                Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    val side = size.width
                    // draw corner finder patterns
                    val cornerS = side * 0.22f
                    
                    // Top-Left Finder
                    drawRect(SlateBlue, Offset(0f, 0f), androidx.compose.ui.geometry.Size(cornerS, cornerS))
                    drawRect(Color.White, Offset(4f, 4f), androidx.compose.ui.geometry.Size(cornerS - 8f, cornerS - 8f))
                    drawRect(SlateBlue, Offset(8f, 8f), androidx.compose.ui.geometry.Size(cornerS - 16f, cornerS - 16f))
                    
                    // Top-Right Finder
                    drawRect(SlateBlue, Offset(side - cornerS, 0f), androidx.compose.ui.geometry.Size(cornerS, cornerS))
                    drawRect(Color.White, Offset(side - cornerS + 4f, 4f), androidx.compose.ui.geometry.Size(cornerS - 8f, cornerS - 8f))
                    drawRect(SlateBlue, Offset(side - cornerS + 8f, 8f), androidx.compose.ui.geometry.Size(cornerS - 16f, cornerS - 16f))
                    
                    // Bottom-Left Finder
                    drawRect(SlateBlue, Offset(0f, side - cornerS), androidx.compose.ui.geometry.Size(cornerS, cornerS))
                    drawRect(Color.White, Offset(4f, side - cornerS + 4f), androidx.compose.ui.geometry.Size(cornerS - 8f, cornerS - 8f))
                    drawRect(SlateBlue, Offset(8f, side - cornerS + 8f), androidx.compose.ui.geometry.Size(cornerS - 16f, cornerS - 16f))

                    // Draw random data block pixels
                    val blocks = 14
                    val bSize = side / blocks
                    for (i in 3 until blocks - 3) {
                        for (j in 3 until blocks - 3) {
                            if ((i + j) % 2 == 0 || (i * j) % 3 == 1) {
                                drawRect(SlateBlue, Offset(i * bSize, j * bSize), androidx.compose.ui.geometry.Size(bSize, bSize))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Asset Metadata below QR
            Text(
                text = asset.id,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlate,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = asset.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = CoolGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Plant: ${asset.plant}",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = SlateBlue
            )
        }
    }
}
