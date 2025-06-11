package com.jamie.scansavvy.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = Page::class)
@Entity(tableName = "pages_fts")
data class PageFts(
    @ColumnInfo(name = "ocr_text")
    val ocrText: String?
)