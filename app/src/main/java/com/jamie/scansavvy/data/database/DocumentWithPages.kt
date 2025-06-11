package com.jamie.scansavvy.data.database

import androidx.room.Embedded
import androidx.room.Relation

// This class represents the one-to-many relationship between a Document and its Pages.
data class DocumentWithPages(
    @Embedded val document: Document,
    @Relation(
        parentColumn = "id",
        entityColumn = "document_id"
    )
    val pages: List<Page>
)