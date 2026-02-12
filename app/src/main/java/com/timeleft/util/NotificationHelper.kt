package com.timeleft.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.timeleft.MainActivity
import com.timeleft.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.Year
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_DAILY = "daily_updates"
    const val CHANNEL_MILESTONES = "milestones"
    const val CHANNEL_CUSTOM = "custom_dates"

    private const val DAILY_WORK_TAG = "daily_notification_work"

    fun createNotificationChannels(context: Context) {
        val dailyChannel = NotificationChannel(
            CHANNEL_DAILY,
            "Daily Updates",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Daily time left updates"
        }

        val milestoneChannel = NotificationChannel(
            CHANNEL_MILESTONES,
            "Milestones",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Year progress milestone alerts"
        }

        val customChannel = NotificationChannel(
            CHANNEL_CUSTOM,
            "Countdown Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Custom date countdown reminders"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannels(listOf(dailyChannel, milestoneChannel, customChannel))
    }

    fun scheduleDailyNotification(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelDailyNotification(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_WORK_TAG)
    }

    fun sendNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}

class DailyNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefsRepo = UserPreferencesRepository(context)
        val prefs = prefsRepo.preferences.first()

        if (!prefs.dailyNotificationEnabled) return Result.success()

        val daysLeft = TimeCalculations.daysLeftInYear()
        val year = Year.now().value

        NotificationHelper.sendNotification(
            context = context,
            channelId = NotificationHelper.CHANNEL_DAILY,
            title = "TimeLeft",
            message = "$daysLeft days left in $year",
            notificationId = 1001
        )

        // Check milestones
        if (prefs.milestoneNotificationEnabled) {
            val totalDays = TimeCalculations.totalDaysInYear()
            val elapsed = TimeCalculations.daysElapsedInYear()
            val percent = (elapsed.toFloat() / totalDays * 100).toInt()

            val milestones = listOf(25, 50, 75, 90)
            val yesterdayPercent = ((elapsed - 1).toFloat() / totalDays * 100).toInt()

            milestones.forEach { milestone ->
                if (percent >= milestone && yesterdayPercent < milestone) {
                    NotificationHelper.sendNotification(
                        context = context,
                        channelId = NotificationHelper.CHANNEL_MILESTONES,
                        title = "Milestone: $milestone% of $year",
                        message = "You've passed $milestone% of the year. $daysLeft days remaining.",
                        notificationId = 2000 + milestone
                    )
                }
            }
        }

        return Result.success()
    }
}
