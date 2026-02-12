package com.timeleft

import android.app.Application
import com.timeleft.util.NotificationHelper
import com.timeleft.widgets.WidgetUpdateWorker

class TimeLeftApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        WidgetUpdateWorker.schedule(this)
    }
}
