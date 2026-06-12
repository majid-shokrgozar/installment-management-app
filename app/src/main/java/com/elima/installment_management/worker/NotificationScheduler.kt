package com.elima.installment_management.worker

import android.content.Context
import androidx.work.*
import com.elima.installment_management.data.SettingsManager
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "loan_notification_work"

    fun scheduleDailyNotification(context: Context) {
        val settingsManager = SettingsManager(context)
        val hour = settingsManager.notificationHour
        val minute = settingsManager.notificationMinute

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val now = Calendar.getInstance()
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = calendar.timeInMillis - now.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }
}
