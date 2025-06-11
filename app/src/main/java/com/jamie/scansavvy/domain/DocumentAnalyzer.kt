package com.jamie.scansavvy.domain

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class AnalysisResult(
    val fullText: String,
    val smartTitle: String
)

@Singleton
class DocumentAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // We use the Latin-based recognizer, which covers English and most European languages.
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Analyzes a list of page URIs, performs OCR, and generates a smart title.
     * It prioritizes the first page for title generation.
     */
    suspend fun analyze(pageUris: List<Uri>): AnalysisResult {
        if (pageUris.isEmpty()) return AnalysisResult("", "Untitled Scan")

        val firstPageText = performOcr(pageUris.first())
        val smartTitle = generateSmartTitle(firstPageText)

        // For a complete analysis, you could OCR all pages and combine the text.
        // For now, using the first page's text is sufficient.
        return AnalysisResult(firstPageText, smartTitle)
    }

    private suspend fun performOcr(uri: Uri): String {
        return try {
            val inputImage = InputImage.fromFilePath(context, uri)
            val result = recognizer.process(inputImage).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            "" // Return empty string on failure
        }
    }

    private fun generateSmartTitle(text: String): String {
        // A simple but effective smart title algorithm:
        // 1. Split the text into lines.
        // 2. Find the first line that has more than one word and some length.
        // 3. Use it as the title. If none found, use a default.
        val lines = text.lines()
        val potentialTitle = lines.firstOrNull { it.trim().length > 5 && it.contains(" ") }
            ?.trim()

        return if (!potentialTitle.isNullOrBlank()) {
            potentialTitle.take(50)
        } else {
            "Scan - ${System.currentTimeMillis()}"
        }
    }
}