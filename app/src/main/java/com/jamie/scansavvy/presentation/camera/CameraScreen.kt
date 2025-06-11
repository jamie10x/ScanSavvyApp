package com.jamie.scansavvy.presentation.camera

import android.app.Activity
import android.content.IntentSender
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.jamie.scansavvy.presentation.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(key1 = true) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CameraEvent.ScanSuccess -> {
                    Toast.makeText(context, "Scan saved successfully!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Viewer.createRoute(event.documentId))
                }
                is CameraEvent.ScanFailure -> {
                    Toast.makeText(context, event.errorMessage, Toast.LENGTH_LONG).show()
                }
                // Handle the new event
                is CameraEvent.RequestReview -> {
                    val reviewManager = ReviewManagerFactory.create(activity)
                    val request = reviewManager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            reviewManager.launchReviewFlow(activity, reviewInfo)
                        }
                    }
                }
            }
        }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pageUris = scanningResult?.pages?.map { it.imageUri } ?: emptyList()
            viewModel.onScanResult(pageUris)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    val scanner = GmsDocumentScanning.getClient(viewModel.scannerOptions)
                    scanner.getStartScanIntent(context)
                        .addOnSuccessListener { intentSender: IntentSender ->
                            scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        }
                        .addOnFailureListener {
                            println("Scanner failed to start: ${it.message}")
                        }
                },
                modifier = Modifier.size(width = 200.dp, height = 60.dp)
            ) {
                Icon(imageVector = Icons.Default.Scanner, contentDescription = "Scan Icon")
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Scan Document")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { navController.navigate(Screen.Gallery.route) },
                modifier = Modifier.size(width = 200.dp, height = 60.dp)
            ) {
                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery Icon")
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "View Gallery")
            }
        }
        IconButton(
            onClick = { navController.navigate(Screen.Settings.route) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}