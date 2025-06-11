package com.jamie.scansavvy.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.jamie.scansavvy.data.database.Document
import com.jamie.scansavvy.data.database.DocumentDao
import com.jamie.scansavvy.data.database.DocumentWithPages
import com.jamie.scansavvy.data.database.Page
import com.jamie.scansavvy.domain.DocumentAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PictureRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentDao: DocumentDao,
    private val documentAnalyzer: DocumentAnalyzer
) {
    fun getAllDocuments(): Flow<List<DocumentWithPages>> {
        return documentDao.getAllDocumentsWithPages()
    }

    fun getDocumentById(documentId: Int): Flow<DocumentWithPages?> {
        return documentDao.getDocumentWithPagesById(documentId)
    }

    fun searchDocuments(query: String): Flow<List<DocumentWithPages>> {
        val ftsQuery = if (query.isNotBlank()) "$query*" else query
        return documentDao.searchDocuments(ftsQuery)
    }

    suspend fun renameDocument(document: Document, newTitle: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            documentDao.updateDocument(document.copy(title = newTitle))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(document: Document): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val docWithPagesFlow = documentDao.getDocumentWithPagesById(document.id)
            docWithPagesFlow.firstOrNull()?.pages?.forEach { page ->
                val file = File(page.imageUri.toUri().path!!)
                if (file.exists()) {
                    file.delete()
                }
            }
            documentDao.deleteDocument(document)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveNewScan(pageUris: List<Uri>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val permanentPageList = mutableListOf<Page>()
            val permanentUris = mutableListOf<Uri>()

            pageUris.forEachIndexed { index, uri ->
                val permanentUri = copyFileToInternalStorage(uri, "page_${System.currentTimeMillis()}_$index.jpg")
                if (permanentUri != null) {
                    permanentUris.add(permanentUri)
                }
            }

            if (permanentUris.isEmpty()) {
                throw Exception("Failed to copy scanned images to permanent storage.")
            }

            val analysisResult = documentAnalyzer.analyze(permanentUris)

            permanentUris.forEachIndexed { index, uri ->
                permanentPageList.add(
                    Page(
                        documentId = 0,
                        pageNumber = index + 1,
                        imageUri = uri.toString(),
                        ocrText = if (index == 0) analysisResult.fullText else ""
                    )
                )
            }

            val newDocument = Document(title = analysisResult.smartTitle)
            val newDocumentId = documentDao.insertDocument(newDocument)
            val pagesWithDocumentId = permanentPageList.map { it.copy(documentId = newDocumentId.toInt()) }
            documentDao.insertPages(pagesWithDocumentId)

            Result.success(newDocumentId.toInt())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyFileToInternalStorage(sourceUri: Uri, newFileName: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val file = File(context.filesDir, newFileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}