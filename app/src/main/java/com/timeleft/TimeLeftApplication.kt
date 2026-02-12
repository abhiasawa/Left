package com.timeleft

import android.app.Application
import com.timeleft.util.NotificationHelper
import com.timeleft.widgets.WidgetUpdateWorker

/**
 * Application subclass for one-time global initialisation.
 *
 * Registers notification channels and schedules the periodic widget-update worker
 * before any Activity is created.
 */
class TimeLeftApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        WidgetUpdateWorker.schedule(this)
    }
}
