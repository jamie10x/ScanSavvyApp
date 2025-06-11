package com.jamie.scansavvy.presentation.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jamie.scansavvy.data.database.DocumentWithPages
import com.jamie.scansavvy.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState.showDeleteConfirmation?.let { docToDelete ->
        DeleteConfirmationDialog(
            document = docToDelete,
            onConfirm = { viewModel.onConfirmDelete() },
            onDismiss = { viewModel.onDismissDeleteConfirmation() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanned Documents") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search Documents") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator()
                    uiState.error != null -> Text(text = uiState.error!!)
                    uiState.documents.isEmpty() -> Text(text = if (uiState.searchQuery.isNotBlank()) "No results found." else "No documents scanned yet.")
                    else -> {
                        DocumentGrid(
                            documents = uiState.documents,
                            expandedMenuDocId = uiState.expandedMenuDocumentId,
                            onDocumentClick = { documentId ->
                                navController.navigate(Screen.Viewer.createRoute(documentId))
                            },
                            onDocumentLongClick = { viewModel.onDocumentLongPress(it) },
                            onDismissMenu = { viewModel.onDismissMenu() },
                            onDeleteClick = { viewModel.onDeleteRequest(it) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentGrid(
    documents: List<DocumentWithPages>,
    expandedMenuDocId: Int?,
    onDocumentClick: (Int) -> Unit,
    onDocumentLongClick: (Int) -> Unit,
    onDismissMenu: () -> Unit,
    onDeleteClick: (DocumentWithPages) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(documents, key = { it.document.id }) { docWithPages ->
            val docId = docWithPages.document.id
            Box {
                Column(
                    modifier = Modifier.combinedClickable(
                        onClick = { onDocumentClick(docId) },
                        onLongClick = { onDocumentLongClick(docId) }
                    )
                ) {
                    val thumbnailUrl = docWithPages.pages.firstOrNull()?.imageUri?.toUri()
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = "Thumbnail of ${docWithPages.document.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.aspectRatio(1f)
                    )
                    Text(
                        text = docWithPages.document.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                DropdownMenu(
                    expanded = (expandedMenuDocId == docId),
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { onDeleteClick(docWithPages) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    document: DocumentWithPages,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Document") },
        text = { Text("Are you sure you want to permanently delete '${document.document.title}'?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}