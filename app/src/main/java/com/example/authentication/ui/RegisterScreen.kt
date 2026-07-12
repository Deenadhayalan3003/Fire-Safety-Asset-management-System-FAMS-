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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authentication.viewmodel.RegisterState
import com.example.authentication.viewmodel.RegisterViewModel
import com.example.ui.theme.FireRed
import com.example.ui.theme.SlateBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var companyCode by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var plant by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Safety Officer") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var rolesExpanded by remember { mutableStateOf(false) }

    val roles = listOf("Administrator", "Safety Manager", "Safety Officer", "Fire Technician", "Auditor", "Viewer")

    LaunchedEffect(uiState) {
        if (uiState is RegisterState.Success) {
            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onNavigateToDashboard()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Account", fontWeight = FontWeight.Bold, color = SlateBlue) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("register_back_button")) {
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
                    text = "Enterprise Inspector Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateBlue,
                    textAlign = TextAlign.Start,
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
                        // Company Code
                        OutlinedTextField(
                            value = companyCode,
                            onValueChange = { companyCode = it },
                            label = { Text("Company Code (e.g., FAMS-US)") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("register_company_code_input")
                        )

                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("register_fullname_input")
                        )

                        // Employee ID
                        OutlinedTextField(
                            value = employeeId,
                            onValueChange = { employeeId = it },
                            label = { Text("Employee ID (EMP-XXXX)") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("register_employee_id_input")
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
                                .padding(vertical = 6.dp)
                                .testTag("register_department_input")
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
                                .padding(vertical = 6.dp)
                                .testTag("register_designation_input")
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
                                .padding(vertical = 6.dp)
                                .testTag("register_plant_input")
                        )

                        // Mobile Number
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("register_mobile_input")
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Corporate Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("register_email_input")
                        )

                        // Role Selection Dropdown
                        ExposedDropdownMenuBox(
                            expanded = rolesExpanded,
                            onExpandedChange = { rolesExpanded = !rolesExpanded },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            OutlinedTextField(
                                value = role,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Enterprise Role") },
                                leadingIcon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolesExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = rolesExpanded,
                                onDismissRequest = { rolesExpanded = false }
                            ) {
                                roles.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            role = r
                                            rolesExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("register_password_input")
                        )

                        // Password Complexity Display
                        PasswordStrengthPanel(password = password, viewModel = viewModel)

                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("register_confirm_password_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (uiState is RegisterState.Loading) {
                            CircularProgressIndicator(
                                color = FireRed,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Button(
                                onClick = {
                                    viewModel.register(
                                        companyCode = companyCode,
                                        fullName = fullName,
                                        employeeId = employeeId,
                                        department = department,
                                        designation = designation,
                                        plant = plant,
                                        mobile = mobile,
                                        email = email,
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        role = role
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("register_submit_button")
                            ) {
                                Text(
                                    text = "REGISTER SECURE ACCOUNT",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        AnimatedVisibility(visible = uiState is RegisterState.Error) {
                            val errorMsg = (uiState as? RegisterState.Error)?.message
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

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun PasswordStrengthPanel(password: String, viewModel: RegisterViewModel) {
    if (password.isEmpty()) return

    val errors = viewModel.validatePasswordStrength(password)
    val hasMinLength = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "Password Requirements",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = SlateBlue,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        RequirementRow(label = "At least 8 characters", isMet = hasMinLength)
        RequirementRow(label = "At least one uppercase letter", isMet = hasUpper)
        RequirementRow(label = "At least one lowercase letter", isMet = hasLower)
        RequirementRow(label = "At least one digit", isMet = hasDigit)
        RequirementRow(label = "At least one special character", isMet = hasSpecial)
    }
}

@Composable
fun RequirementRow(label: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
