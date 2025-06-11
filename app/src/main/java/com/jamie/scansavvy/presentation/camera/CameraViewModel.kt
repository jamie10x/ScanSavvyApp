package com.jamie.scansavvy.presentation.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.jamie.scansavvy.data.PictureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CameraEvent {
    data class ScanSuccess(val documentId: Int) : CameraEvent()
    data class ScanFailure(val errorMessage: String) : CameraEvent()
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val pictureRepository: PictureRepository
) : ViewModel() {

    private val eventChannel = Channel<CameraEvent>()
    val events = eventChannel.receiveAsFlow()

    val scannerOptions: GmsDocumentScannerOptions = GmsDocumentScannerOptions.Builder()
        .setScannerMode(SCANNER_MODE_FULL)
        .setResultFormats(RESULT_FORMAT_JPEG)
        .setGalleryImportAllowed(true)
        .setPageLimit(10)
        .build()

    fun onScanResult(uris: List<Uri>) {
        viewModelScope.launch {
            if (uris.isEmpty()) {
                eventChannel.send(CameraEvent.ScanFailure("No pages were scanned."))
                return@launch
            }

            // Correctly handle the Result<Int>
            pictureRepository.saveNewScan(uris)
                .onSuccess { newDocumentId ->
                    // Send a success event with the ID for navigation.
                    eventChannel.send(CameraEvent.ScanSuccess(newDocumentId))
                }.onFailure { exception ->
                    eventChannel.send(CameraEvent.ScanFailure(exception.message ?: "Failed to save scan."))
                }
        }
    }
}