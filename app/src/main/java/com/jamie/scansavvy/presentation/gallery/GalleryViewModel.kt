package com.jamie.scansavvy.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.scansavvy.data.PictureRepository
import com.jamie.scansavvy.data.database.DocumentWithPages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GalleryScreenUiState(
    val documents: List<DocumentWithPages> = emptyList(),
    val searchQuery: String = "",
    val expandedMenuDocumentId: Int? = null,
    val showDeleteConfirmation: DocumentWithPages? = null,
    val documentToRename: DocumentWithPages? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val pictureRepository: PictureRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(GalleryScreenUiState())
    val uiState: StateFlow<GalleryScreenUiState> = _uiState

    init {
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    pictureRepository.getAllDocuments()
                } else {
                    pictureRepository.searchDocuments(query)
                }
            }.collect { documents ->
                _uiState.update { it.copy(documents = documents, isLoading = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDocumentLongPress(documentId: Int) {
        _uiState.update { it.copy(expandedMenuDocumentId = documentId) }
    }

    fun onDismissMenu() {
        _uiState.update { it.copy(expandedMenuDocumentId = null) }
    }

    // --- Delete Logic ---
    fun onDeleteRequest(document: DocumentWithPages) {
        _uiState.update { it.copy(showDeleteConfirmation = document, expandedMenuDocumentId = null) }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            _uiState.value.showDeleteConfirmation?.let { docToDelete ->
                pictureRepository.deleteDocument(docToDelete.document)
                _uiState.update { it.copy(showDeleteConfirmation = null) }
            }
        }
    }

    fun onDismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = null) }
    }

    fun onRenameRequest(document: DocumentWithPages) {
        _uiState.update { it.copy(documentToRename = document, expandedMenuDocumentId = null) }
    }

    fun onConfirmRename(newTitle: String) {
        viewModelScope.launch {
            _uiState.value.documentToRename?.let { docToRename ->
                if (newTitle.isNotBlank()) {
                    pictureRepository.renameDocument(docToRename.document, newTitle)
                }
                _uiState.update { it.copy(documentToRename = null) }
            }
        }
    }

    fun onDismissRenameDialog() {
        _uiState.update { it.copy(documentToRename = null) }
    }
}