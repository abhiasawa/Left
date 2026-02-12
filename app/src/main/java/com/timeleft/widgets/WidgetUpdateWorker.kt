package com.timeleft.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Periodic background worker that refreshes all home screen widgets.
 * Scheduled via WorkManager to run every 30 minutes so widget data
 * stays reasonably current without draining the battery.
 */
class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    /** Triggers an update on every widget type; retries on failure. */
    override suspend fun doWork(): Result {
        return try {
            YearProgressWidget().updateAll(context)
            YearBarcodeWidget().updateAll(context)
            MonthProgressWidget().updateAll(context)
            LifeProgressWidget().updateAll(context)
            CountdownWidget().updateAll(context)
            DayHourWidget().updateAll(context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "widget_update_work"

        /** Enqueues a unique periodic job; KEEP policy avoids duplicate scheduling. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                30, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
