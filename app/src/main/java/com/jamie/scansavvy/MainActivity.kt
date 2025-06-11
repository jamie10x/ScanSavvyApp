package com.jamie.scansavvy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jamie.scansavvy.data.SettingsRepository
import com.jamie.scansavvy.presentation.camera.CameraScreen
import com.jamie.scansavvy.presentation.gallery.GalleryScreen
import com.jamie.scansavvy.presentation.navigation.Screen
import com.jamie.scansavvy.presentation.settings.SettingsScreen
import com.jamie.scansavvy.presentation.viewer.ViewerScreen
import com.jamie.scansavvy.ui.theme.ScanSavvyTheme
import com.jamie.scansavvy.util.BiometricAuthenticator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val biometricAuthenticator = BiometricAuthenticator(this)

        setContent {
            ScanSavvyTheme {
                val settingsState by settingsRepository.appSettingsFlow.collectAsState(initial = null)
                var isAuthenticated by rememberSaveable { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (settingsState == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val needsAuth = settingsState!!.isBiometricLockEnabled
                        if (!needsAuth || isAuthenticated) {
                            ScanSavvyNavHost()
                        } else {
                            LockedScreen(
                                onUnlock = {
                                    biometricAuthenticator.prompt(
                                        onSuccess = { isAuthenticated = true },
                                        onFailure = { /* Handle failure */ },
                                        onError = { _, _ -> /* Handle error */ }
                                    )
                                }
                            )
                            LaunchedEffect(Unit) {
                                if (!isAuthenticated) {
                                    biometricAuthenticator.prompt(
                                        onSuccess = { isAuthenticated = true },
                                        onFailure = {},
                                        onError = { _, _ ->}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanSavvyNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Camera.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = Screen.Camera.route) {
            CameraScreen(navController = navController)
        }
        composable(route = Screen.Gallery.route) {
            GalleryScreen(navController = navController)
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(
            route = Screen.Viewer.route,
            arguments = listOf(navArgument("documentId") { type = NavType.IntType })
        ) {
            ViewerScreen(navController = navController)
        }
    }
}

@Composable
fun LockedScreen(onUnlock: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Fingerprint Icon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("ScanSavvy is Locked", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please authenticate to continue",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onUnlock) {
                Text("Unlock")
            }
        }
    }
}