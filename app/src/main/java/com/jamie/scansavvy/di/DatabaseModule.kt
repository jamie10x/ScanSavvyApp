package com.jamie.scansavvy.di

import android.content.Context
import androidx.room.Room
import com.jamie.scansavvy.data.database.DocumentDao
import com.jamie.scansavvy.data.database.ScanSavvyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScanSavvyDatabase {
        return Room.databaseBuilder(
            context,
            ScanSavvyDatabase::class.java,
            "scansavvy_database"
        ).build()
    }

    @Provides
    fun provideDocumentDao(database: ScanSavvyDatabase): DocumentDao {
        return database.documentDao()
    }
}