package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authentication.viewmodel.LoginState
import com.example.authentication.viewmodel.LoginViewModel
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.FireRed
import com.example.ui.theme.SlateBlue

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    isFirebaseEnabled: Boolean,
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var companyCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is LoginState.Success) {
            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onNavigateToDashboard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // FAMS Red Shield Logo
            Card(
                colors = CardDefaults.cardColors(containerColor = FireRed),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .size(80.dp)
                    .testTag("login_brand_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Fire Safety Logo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FAMS Portal",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = SlateBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("login_title_text")
            )

            Text(
                text = "Fire Safety Asset Management System",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Input Fields Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_form_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Company Code Field
                    OutlinedTextField(
                        value = companyCode,
                        onValueChange = { companyCode = it },
                        label = { Text("Company Code (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("login_company_code_input")
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Corporate Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("login_email_input")
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("login_password_input")
                    )

                    // Forgot Password link
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 12.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = onNavigateToForgotPassword,
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = FireRed,
                                fontSize = 13.sp,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState is LoginState.Loading) {
                        CircularProgressIndicator(
                            color = FireRed,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Button(
                            onClick = { viewModel.login(email, password) },
                            colors = ButtonDefaults.buttonColors(containerColor = FireRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_action_button")
                        ) {
                            Text(
                                text = "SECURE LOGIN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    AnimatedVisibility(visible = uiState is LoginState.Error) {
                        val errorState = uiState as? LoginState.Error
                        if (errorState != null) {
                            Text(
                                text = errorState.message,
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

            Spacer(modifier = Modifier.height(20.dp))

            // Registration Transition Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Need an enterprise account?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.testTag("toggle_register_mode_button")
                ) {
                    Text(
                        text = "Register Officer",
                        color = FireRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sandbox active panel
            AnimatedVisibility(
                visible = !isFirebaseEnabled,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmberAlert.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFC68400)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sandbox Demo Mode Active",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC68400),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To test standard features offline, please use these login credentials:\n\n" +
                                    "• Safety Officer: tech@fams.com / tech123\n" +
                                    "• Administrator: admin@fams.com / admin123",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
