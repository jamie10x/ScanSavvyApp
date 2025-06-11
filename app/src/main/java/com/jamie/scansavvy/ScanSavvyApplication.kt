package com.jamie.scansavvy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScanSavvyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}