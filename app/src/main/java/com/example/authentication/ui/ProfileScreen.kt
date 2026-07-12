package com.example.authentication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.authentication.viewmodel.ProfileState
import com.example.authentication.viewmodel.ProfileViewModel
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.FireRed
import com.example.ui.theme.SlateBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val userState by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ProfileState.LogoutSuccess) {
            viewModel.resetState()
            onLogoutSuccess()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out Confirmation", fontWeight = FontWeight.Bold, color = SlateBlue) },
            text = { Text("Are you sure you want to end your secure session on this device? This will clear local cached records.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FireRed)
                ) {
                    Text("CONFIRM LOGOUT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SlateBlue)
                }
            },
            modifier = Modifier.testTag("logout_confirmation_dialog")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAMS User Profile", fontWeight = FontWeight.Bold, color = SlateBlue) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.testTag("profile_logout_action")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = FireRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val user = userState
            if (user == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FireRed)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar / Photo Area
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(SlateBlue.copy(alpha = 0.1f))
                            .border(3.dp, FireRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user.profilePhoto.isNullOrEmpty()) {
                            AsyncImage(
                                model = user.profilePhoto,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = if (user.fullName.isNotEmpty()) {
                                user.fullName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                    .uppercase()
                            } else {
                                "O"
                            }
                            Text(
                                text = initials,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = SlateBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user.fullName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateBlue,
                        modifier = Modifier.testTag("profile_user_name")
                    )

                    // Role Badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FireRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            color = FireRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Detail Blocks
                    Text(
                        text = "CORPORATE ASSIGNMENT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Start
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ProfileDetailRow(icon = Icons.Default.Business, label = "Company", value = user.companyCode)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                            ProfileDetailRow(icon = Icons.Default.Badge, label = "Employee ID", value = user.employeeId)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                            ProfileDetailRow(icon = Icons.Default.Group, label = "Department", value = user.department)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                            ProfileDetailRow(icon = Icons.Default.Work, label = "Designation", value = user.designation)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                            ProfileDetailRow(icon = Icons.Default.LocationOn, label = "Plant Location", value = user.plant)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "CONTACT & STATUS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Start
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ProfileDetailRow(icon = Icons.Default.Email, label = "Corporate Email", value = user.email)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                            ProfileDetailRow(icon = Icons.Default.Phone, label = "Mobile Number", value = user.mobile)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                            ProfileDetailRow(icon = Icons.Default.CheckCircle, label = "Profile Status", value = user.status, valueColor = Color(0xFF2E7D32))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                            ProfileDetailRow(
                                icon = Icons.Default.Schedule,
                                label = "Last Session Sync",
                                value = formatDate(user.lastLogin)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons
                    Button(
                        onClick = onNavigateToEditProfile,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_edit_nav_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EDIT PROFILE DETAILS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onNavigateToChangePassword,
                        border = BorderStroke(1.dp, SlateBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_change_pass_nav_button")
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp), tint = SlateBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CHANGE ACCESS PASSWORD", color = SlateBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SlateBlue.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value.ifEmpty { "Not Specified" }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = valueColor)
        }
    }
}

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm z", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
