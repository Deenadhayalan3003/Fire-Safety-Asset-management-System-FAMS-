package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.authentication.AuthViewModel
import com.example.authentication.ui.ChangePasswordScreen
import com.example.authentication.ui.EditProfileScreen
import com.example.authentication.ui.ForgotPasswordScreen
import com.example.authentication.ui.ProfileScreen
import com.example.authentication.ui.RegisterScreen
import com.example.authentication.viewmodel.EditProfileViewModel
import com.example.authentication.viewmodel.ForgotPasswordViewModel
import com.example.authentication.viewmodel.LoginViewModel
import com.example.authentication.viewmodel.ProfileViewModel
import com.example.authentication.viewmodel.RegisterViewModel
import com.example.admin.AdminDashboardScreen
import com.example.admin.AdminViewModel
import com.example.dashboard.DashboardViewModel
import com.example.ui.DashboardScreen
import com.example.ui.LoginScreen
import com.example.ui.SplashScreen
import com.example.ui.SplashViewModel
import com.example.ui.theme.FamsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FamsAppNavigation()
                }
            }
        }
    }
}

@Composable
fun FamsAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // 1. Splash Screen
        composable("splash") {
            val splashViewModel: SplashViewModel = hiltViewModel()
            SplashScreen(
                viewModel = splashViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. Login Screen
        composable("login") {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                isFirebaseEnabled = true, // Defaults to enabled/fallback handled inside repo
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        // 3. Register Screen
        composable("register") {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 4. Forgot Password Screen
        composable("forgot_password") {
            val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
            ForgotPasswordScreen(
                viewModel = forgotPasswordViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Dashboard Screen
        composable("dashboard") {
            val authViewModel: AuthViewModel = hiltViewModel()
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                authViewModel = authViewModel,
                dashboardViewModel = dashboardViewModel,
                onLogoutNavigate = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToAdminConsole = {
                    navController.navigate("admin_console")
                }
            )
        }

        // 5b. Admin Console Screen
        composable("admin_console") {
            val adminViewModel: AdminViewModel = hiltViewModel()
            AdminDashboardScreen(
                viewModel = adminViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 6. Profile Screen
        composable("profile") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditProfile = {
                    navController.navigate("edit_profile")
                },
                onNavigateToChangePassword = {
                    navController.navigate("change_password")
                },
                onLogoutSuccess = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 7. Edit Profile Screen
        composable("edit_profile") {
            val editProfileViewModel: EditProfileViewModel = hiltViewModel()
            EditProfileScreen(
                viewModel = editProfileViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 8. Change Password Screen
        composable("change_password") {
            val editProfileViewModel: EditProfileViewModel = hiltViewModel()
            ChangePasswordScreen(
                viewModel = editProfileViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
