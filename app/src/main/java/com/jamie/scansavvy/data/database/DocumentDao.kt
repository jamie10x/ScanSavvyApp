package com.jamie.scansavvy.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Transaction
    suspend fun insertDocumentWithPages(document: Document, pages: List<Page>) {
        val documentId = insertDocument(document)
        val pagesWithDocumentId = pages.map { it.copy(documentId = documentId.toInt()) }
        insertPages(pagesWithDocumentId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<Page>)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Transaction
    @Query("SELECT * FROM documents ORDER BY creation_timestamp DESC")
    fun getAllDocumentsWithPages(): Flow<List<DocumentWithPages>>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :documentId")
    fun getDocumentWithPagesById(documentId: Int): Flow<DocumentWithPages?>

    @Transaction
    @Query(
        """
        SELECT * FROM documents
        WHERE id IN (
            SELECT p.document_id FROM pages AS p
            JOIN pages_fts AS pf ON p.rowid = pf.rowid
            WHERE pf.pages_fts MATCH :query
        ) OR title LIKE '%' || :query || '%'
        ORDER BY creation_timestamp DESC
        """
    )
    fun searchDocuments(query: String): Flow<List<DocumentWithPages>>
}