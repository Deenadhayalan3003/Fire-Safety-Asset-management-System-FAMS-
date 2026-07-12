package com.example.authentication.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authentication.viewmodel.EditProfileState
import com.example.authentication.viewmodel.EditProfileViewModel
import com.example.ui.theme.FireRed
import com.example.ui.theme.SlateBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val editState by viewModel.editState.collectAsState()

    var mobile by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var plant by remember { mutableStateOf("") }
    var profilePhoto by remember { mutableStateOf("") }

    // Prefill fields when current user loads
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (mobile.isEmpty()) mobile = user.mobile
            if (department.isEmpty()) department = user.department
            if (designation.isEmpty()) designation = user.designation
            if (plant.isEmpty()) plant = user.plant
            if (profilePhoto.isEmpty()) profilePhoto = user.profilePhoto
        }
    }

    LaunchedEffect(editState) {
        if (editState is EditProfileState.Success) {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetStates()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit User Profile", fontWeight = FontWeight.Bold, color = SlateBlue) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("edit_profile_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Update Corporate Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Photo URL
                        OutlinedTextField(
                            value = profilePhoto,
                            onValueChange = { profilePhoto = it },
                            label = { Text("Profile Photo URL / Path") },
                            leadingIcon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("edit_profile_photo_input")
                        )

                        // Mobile Phone
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("edit_profile_mobile_input")
                        )

                        // Department
                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text("Department") },
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("edit_profile_department_input")
                        )

                        // Designation
                        OutlinedTextField(
                            value = designation,
                            onValueChange = { designation = it },
                            label = { Text("Designation") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("edit_profile_designation_input")
                        )

                        // Plant Location
                        OutlinedTextField(
                            value = plant,
                            onValueChange = { plant = it },
                            label = { Text("Plant / Location Name") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("edit_profile_plant_input")
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (editState is EditProfileState.Loading) {
                            CircularProgressIndicator(
                                color = FireRed,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Button(
                                onClick = {
                                    viewModel.updateProfile(
                                        mobile = mobile,
                                        department = department,
                                        designation = designation,
                                        plant = plant,
                                        profilePhotoUrl = profilePhoto
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("edit_profile_save_button")
                            ) {
                                Text(
                                    text = "SAVE CHANGES",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        AnimatedVisibility(visible = editState is EditProfileState.Error) {
                            val errorMsg = (editState as? EditProfileState.Error)?.message
                            if (errorMsg != null) {
                                Text(
                                    text = errorMsg,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
