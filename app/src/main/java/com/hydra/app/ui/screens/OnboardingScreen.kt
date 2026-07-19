package com.hydra.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.hydra.app.HydraApp
import kotlinx.coroutines.launch

enum class OnboardingStep {
    WELCOME,
    NOTIFICATION_RATIONALE,
    NOTIFICATION_DENIED,
    COMPLETED
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    
    // Notification permission launcher
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentStep = OnboardingStep.COMPLETED
        } else {
            currentStep = OnboardingStep.NOTIFICATION_DENIED
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = currentStep,
            label = "Onboarding Animation"
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> {
                    OnboardingPage(
                        icon = Icons.Outlined.WaterDrop,
                        title = "Welcome to Hydra",
                        description = "Hydra is your personal hydration assistant. It uses a smart, event-driven engine to remind you to drink water only when you actually need it, without annoying spam.",
                        buttonText = "Get Started",
                        onButtonClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val isGranted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                
                                if (isGranted) {
                                    currentStep = OnboardingStep.COMPLETED
                                } else {
                                    currentStep = OnboardingStep.NOTIFICATION_RATIONALE
                                }
                            } else {
                                // Notification permission not required before Android 13
                                currentStep = OnboardingStep.COMPLETED
                            }
                        }
                    )
                }
                
                OnboardingStep.NOTIFICATION_RATIONALE -> {
                    OnboardingPage(
                        icon = Icons.Outlined.Notifications,
                        title = "Smart Reminders",
                        description = "Hydra needs notification access to remind you to drink water. We only use this to deliver hydration reminders—no ads, no spam.",
                        buttonText = "Continue",
                        onButtonClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
                
                OnboardingStep.NOTIFICATION_DENIED -> {
                    OnboardingPage(
                        icon = Icons.Filled.Settings,
                        title = "Notifications Disabled",
                        description = "Without notifications, you won't receive hydration reminders. You can always enable this later in your device Settings.",
                        buttonText = "Next",
                        onButtonClick = {
                            currentStep = OnboardingStep.COMPLETED
                        },
                        isWarning = true
                    )
                }

                OnboardingStep.COMPLETED -> {
                    var scale by remember { mutableStateOf(0.5f) }
                    var alpha by remember { mutableStateOf(0f) }
                    
                    LaunchedEffect(Unit) {
                        androidx.compose.animation.core.animate(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.EaseOutBack)
                        ) { value, _ -> scale = value }
                    }
                    LaunchedEffect(Unit) {
                        androidx.compose.animation.core.animate(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.tween(500)
                        ) { value, _ -> alpha = value }
                        
                        kotlinx.coroutines.delay(1000)
                        
                        // Proceed
                        val app = context.applicationContext as HydraApp
                        app.preferencesManager.setOnboardingCompleted(true)
                        onFinish()
                    }
                    
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    isWarning: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            colors = if (isWarning) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                ButtonDefaults.buttonColors()
            }
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
