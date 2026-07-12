package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.AmberAlertLight
import com.example.ui.theme.FireRed
import com.example.ui.theme.FireRedLight
import com.example.ui.theme.SlateBlue

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isUserLoggedIn) {
        isUserLoggedIn?.let { loggedIn ->
            if (loggedIn) {
                onNavigateToDashboard()
            } else {
                onNavigateToLogin()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SlateBlue,
                        SlateBlue.copy(alpha = 0.95f),
                        Color(0xFF102027)
                    )
                )
            )
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Canvas-drawn Fire Shield Logo (FAMS Corporate Emblem)
            FamsEmblemLogo(modifier = Modifier.size(160.dp))
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = "F A M S",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.testTag("splash_logo_text")
            )
            
            Text(
                text = "Fire Safety Asset Management",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            CircularProgressIndicator(
                color = AmberAlert,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("splash_progress_indicator")
            )
            
            Text(
                text = "Initializing secure session...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun FamsEmblemLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // 1. Outer metallic shield border
        val shieldPath = Path().apply {
            moveTo(width * 0.5f, height * 0.05f)
            quadraticBezierTo(width * 0.9f, height * 0.05f, width * 0.9f, height * 0.45f)
            quadraticBezierTo(width * 0.9f, height * 0.8f, width * 0.5f, height * 0.95f)
            quadraticBezierTo(width * 0.1f, height * 0.8f, width * 0.1f, height * 0.45f)
            quadraticBezierTo(width * 0.1f, height * 0.05f, width * 0.5f, height * 0.05f)
            close()
        }
        
        drawPath(
            path = shieldPath,
            color = Color(0xFFE0E0E0),
            style = Stroke(width = 8f)
        )
        
        // 2. Inner safety shield gradient fill
        val innerShieldPath = Path().apply {
            moveTo(width * 0.5f, height * 0.10f)
            quadraticBezierTo(width * 0.82f, height * 0.10f, width * 0.82f, height * 0.45f)
            quadraticBezierTo(width * 0.82f, height * 0.75f, width * 0.5f, height * 0.88f)
            quadraticBezierTo(width * 0.18f, height * 0.75f, width * 0.18f, height * 0.45f)
            quadraticBezierTo(width * 0.18f, height * 0.10f, width * 0.5f, height * 0.10f)
            close()
        }
        
        drawPath(
            path = innerShieldPath,
            brush = Brush.radialGradient(
                colors = listOf(FireRedLight, FireRed),
                center = Offset(width * 0.5f, height * 0.45f),
                radius = width * 0.4f
            ),
            style = Fill
        )

        // 3. stylized safety flame inside
        val flamePath = Path().apply {
            moveTo(width * 0.5f, height * 0.75f)
            cubicTo(width * 0.35f, height * 0.75f, width * 0.3f, height * 0.55f, width * 0.42f, height * 0.35f)
            cubicTo(width * 0.35f, height * 0.45f, width * 0.38f, height * 0.55f, width * 0.45f, height * 0.48f)
            cubicTo(width * 0.45f, height * 0.30f, width * 0.5f, height * 0.22f, width * 0.5f, height * 0.22f)
            cubicTo(width * 0.5f, height * 0.22f, width * 0.55f, height * 0.30f, width * 0.55f, height * 0.48f)
            cubicTo(width * 0.62f, height * 0.55f, width * 0.65f, height * 0.45f, width * 0.58f, height * 0.35f)
            cubicTo(width * 0.70f, height * 0.55f, width * 0.65f, height * 0.75f, width * 0.5f, height * 0.75f)
            close()
        }
        
        drawPath(
            path = flamePath,
            brush = Brush.verticalGradient(
                colors = listOf(AmberAlertLight, AmberAlert)
            ),
            style = Fill
        )
    }
}
