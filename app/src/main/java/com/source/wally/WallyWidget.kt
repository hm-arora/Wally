package com.source.wally

import android.app.PendingIntent
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.source.wally.utils.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Implementation of App Widget functionality.
 */
class WallyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val sharedPreferenceHelper = SharedPreferenceHelper(context!!.applicationContext)
        val wallpaperChoice =
            sharedPreferenceHelper.getString(SharedPreferenceHelper.SharedPrefKey.WALLPAPER_CHOICE, Constants.COLORS)
        if (ACTION_SIMPLEAPPWIDGET == intent!!.action) {
            mCounter++
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.wally_widget)
            if (wallpaperChoice == Constants.COLORS) {
                Utils.setColorWallpaper(context)
            } else {
                setImageWallpaper(context, sharedPreferenceHelper)
            }
            views.setTextViewText(R.id.tvWidget, Integer.toString(mCounter))
            // This time we dont have widgetId. Reaching our widget with that way.
            val appWidget = ComponentName(context, WallyWidget::class.java)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidget, views)
        }
    }

    companion object {

        private val ACTION_SIMPLEAPPWIDGET = "ACTION_BROADCASTWIDGETSAMPLE"
        private var mCounter = 0

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
//
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.wally_widget)
            // Construct an Intent which is pointing this class.
            val intent = Intent(context, WallyWidget::class.java)
            intent.action = ACTION_SIMPLEAPPWIDGET
            // And this time we are sending a broadcast with getBroadcast
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            views.setOnClickPendingIntent(R.id.tvWidget, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }



    private fun setImageWallpaper(context: Context, sharedPreferenceHelper: SharedPreferenceHelper) {
        val path = sharedPreferenceHelper.getString(SharedPreferenceHelper.SharedPrefKey.FOLDER_PATH, null)
        if (path != null) {
            val imageFolder = File(path)
            val files = imageFolder.listFiles()
            var imageFile = files.random()
            var isImageFileExist = false
            for (i in 0..5) {
                if (imageFile.isFile && (imageFile.name.endsWith("jpg") || imageFile.name.endsWith("png") || imageFile.name.endsWith("jpeg"))) {
                    isImageFileExist = true
                    break

                }
                imageFile = files.random()
            }
            if (isImageFileExist) {
                val fileInputStream = FileInputStream(imageFile)
                val wpManager = WallpaperManager.getInstance(context)
                wpManager.setStream(fileInputStream)
            } else {
                Toast.makeText(context, "No Image File Found", Toast.LENGTH_SHORT).show()
            }

        }
    }
}

