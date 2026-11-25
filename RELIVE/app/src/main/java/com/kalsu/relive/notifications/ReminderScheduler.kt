package com.kalsu.relive.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun schedule(context: Context, title: String, message: String, delayMinutes: Long) {
        require(delayMinutes > 0) { "Le délai doit être supérieur à zéro." }
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_MESSAGE, message)
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .addTag("relive_user_reminder")
            .build()
        WorkManager.getInstance(context.applicationContext).enqueue(request)
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag("relive_user_reminder")
    }
}
