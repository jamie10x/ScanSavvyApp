package com.jamie.scansavvy.utils

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import com.jamie.scansavvy.data.AppSettings
import com.jamie.scansavvy.data.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppReviewManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    // We'll prompt after 3 scans, and not again for at least 90 days.
    private val scansRequiredForReview = 3
    private val coolDownPeriodMillis = 90L * 24 * 60 * 60 * 1000 // 90 days in milliseconds

    /**
     * Checks if the conditions are met to show the review prompt and, if so, shows it.
     */
    suspend fun C(activity: Activity, appSettings: AppSettings) {
        if (shouldShowReview(appSettings)) {
            val reviewManager = ReviewManagerFactory.create(activity)
            val request = reviewManager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even if the dialog was shown. This is by design.
                        // We mark that we've asked, regardless of the outcome.
                    }
                }
            }
            // Mark that we've shown the prompt so we don't ask again too soon.
            settingsRepository.updateReviewRequestTimestamp()
        }
    }

    internal fun shouldShowReview(appSettings: AppSettings): Boolean {
        val enoughScans = appSettings.scanCount >= scansRequiredForReview
        val coolDownPeriodPassed = (System.currentTimeMillis() - appSettings.lastReviewRequestTimestamp) > coolDownPeriodMillis

        return enoughScans && coolDownPeriodPassed
    }
}