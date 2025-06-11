package com.jamie.scansavvy.presentation.viewer

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.scansavvy.data.PictureRepository
import com.jamie.scansavvy.data.database.DocumentWithPages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ViewerUiState {
    object Loading : ViewerUiState()
    data class Success(val document: DocumentWithPages) : ViewerUiState()
    data class Error(val message: String) : ViewerUiState()
}

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val pictureRepository: PictureRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val documentId: Int = savedStateHandle.get<Int>("documentId") ?: -1

    val uiState: StateFlow<ViewerUiState> = pictureRepository.getDocumentById(documentId)
        .map { document ->
            if (document != null) {
                ViewerUiState.Success(document)
            } else {
                ViewerUiState.Error("Document with ID $documentId not found.")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewerUiState.Loading
        )

    // The new function to handle the share logic.
    fun shareDocumentAsPdf(context: Context) {
        if (documentId == -1) return // Do nothing if the ID is invalid

        viewModelScope.launch {
            pictureRepository.generatePdfForDocument(documentId)
                .onSuccess { pdfUri ->
                    // FileProvider is required to grant temporary access to other apps.
                    val shareableUri = FileProvider.getUriForFile(
                        context,
                        // This authority must match the one in AndroidManifest.xml
                        "${context.packageName}.fileprovider",
                        File(pdfUri.path!!)
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, shareableUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(shareIntent, "Share Document PDF")
                    context.startActivity(chooser)
                }
                .onFailure { exception ->
                    // You can show a Toast here in a real app
                    println("Failed to generate PDF for sharing: ${exception.message}")
                }
        }
    }
}