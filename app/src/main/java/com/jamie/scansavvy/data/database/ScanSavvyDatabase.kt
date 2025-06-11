package com.jamie.scansavvy.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Document::class, Page::class, PageFts::class], version = 1)
abstract class ScanSavvyDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
}