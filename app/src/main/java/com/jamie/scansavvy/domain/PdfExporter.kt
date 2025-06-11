package com.jamie.scansavvy.domain

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.net.toUri
import com.jamie.scansavvy.data.database.DocumentWithPages
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun createPdf(document: DocumentWithPages): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val sortedPages = document.pages.sortedBy { it.pageNumber }

            sortedPages.forEachIndexed { index, page ->
                val imageStream = context.contentResolver.openInputStream(page.imageUri.toUri())
                val bitmap = BitmapFactory.decodeStream(imageStream)

                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val pdfPage = pdfDocument.startPage(pageInfo)
                val canvas = pdfPage.canvas

                canvas.drawBitmap(bitmap, 0f, 0f, Paint())
                pdfDocument.finishPage(pdfPage)
                bitmap.recycle()
            }

            val pdfFile = File(context.cacheDir, "${document.document.title}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            Result.success(pdfFile.toUri())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}