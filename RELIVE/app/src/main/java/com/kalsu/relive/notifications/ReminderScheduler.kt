package com.kalsu.relive.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun schedule(context: Context, title: String, message: String, delayMinutes: Long) {
        require(delayMinutes in 1..43_200) { "Le délai doit être compris entre 1 minute et 30 jours." }
        val safeTitle = title.trim().take(80).ifBlank { "Un petit rappel" }
        val safeMessage = message.trim().take(500).ifBlank { "Chama te rappelle ce que tu avais prévu." }
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, safeTitle)
            .putString(ReminderWorker.KEY_MESSAGE, safeMessage)
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
