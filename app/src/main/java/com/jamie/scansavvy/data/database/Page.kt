package com.jamie.scansavvy.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pages",
    foreignKeys = [ForeignKey(
        entity = Document::class,
        parentColumns = ["id"],
        childColumns = ["document_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["document_id"])]
)
data class Page(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "document_id")
    val documentId: Int,

    @ColumnInfo(name = "page_number")
    val pageNumber: Int,

    @ColumnInfo(name = "image_uri")
    val imageUri: String,

    @ColumnInfo(name = "ocr_text")
    val ocrText: String? = null
)