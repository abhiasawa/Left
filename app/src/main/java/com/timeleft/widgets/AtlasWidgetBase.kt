package com.timeleft.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode

/**
 * Atlas widgets are fully responsive to launcher resize operations.
 */
abstract class AtlasWidgetBase : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
}
