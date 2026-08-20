package com.fitifinance.comrade

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.navigation.FitiNavGraph
import com.fitifinance.comrade.navigation.Routes
import com.fitifinance.comrade.ui.theme.FitiFinanceTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val app = application as FitiApplication
            val scope = rememberCoroutineScope()

            // ---- Runtime permissions: SMS parser + location context engine ----
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { /* Results observed lazily; features simply stay dormant until granted. */ }

            LaunchedEffect(Unit) {
                val permissions = mutableListOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions += Manifest.permission.POST_NOTIFICATIONS
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }

            // Decide start destination based on whether onboarding was already completed.
            var startDestination by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                val profile = app.repository.getProfile()
                startDestination = if (profile.onboardingComplete) Routes.DASHBOARD else Routes.ONBOARDING
            }

            val themeMode by app.locationContextEngine.currentMode.collectAsState()

            FitiFinanceTheme(themeMode = themeMode) {
                startDestination?.let { destination ->
                    FitiNavGraph(startDestination = destination)
                }
            }
        }
    }
}
