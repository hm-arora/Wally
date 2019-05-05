package com.source.wally.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.GestureDetector
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.source.wally.utils.Utils
import android.graphics.Paint.Align
import android.util.TypedValue
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.source.wally.utils.Constants
import com.source.wally.utils.RandomColor
import com.source.wally.utils.SharedPreferenceHelper


class WallyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): WallpaperService.Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private val handler = Handler()
        private val frameDuration: Long = 1000
        private var visible = false
        private var isTimeVariable = false
        private val drawRunner = Runnable { draw() }
        private var gestureDetector: GestureDetector? = null
        private var prefs: SharedPreferenceHelper? = null
        private var broadcastReceiver: BroadcastReceiver? = null


        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            prefs = SharedPreferenceHelper.getInstance(applicationContext)
            if (prefs != null) {
                isTimeVariable = prefs!!.getBoolean(SharedPreferenceHelper.SharedPrefKey.IS_TIME_VARIABLE, false)
            }
            setUpGestureDetector()
            setUpBroadCastReceiver()
            handler.post(drawRunner)
        }

        private fun setUpGestureDetector() {
            gestureDetector = GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (!isTimeVariable) {
                        draw()
                    }
                    return true
                }
            })
        }

        private fun setUpBroadCastReceiver() {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent != null && intent.action == Constants.INTENT_ACTION_TIME_BASED) {
                        isTimeVariable = intent.getBooleanExtra(Constants.IS_TIME_VARIABLE, false)
                        draw()
                    }
                }
            }
            val intentFilter = IntentFilter()
            intentFilter.addAction(Constants.INTENT_ACTION_TIME_BASED)
            LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver!!, intentFilter)
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null

            canvas = holder.lockCanvas()

            if (canvas != null) {
                if (isTimeVariable) {
                    setColorUsingCurrentTime(canvas)
                } else {
                    setColorWithoutTime(canvas)
                }
                holder.unlockCanvasAndPost(canvas)
            }

            handler.removeCallbacks(drawRunner)
            if (visible && isTimeVariable) {
                handler.postDelayed(drawRunner, frameDuration)
            }
        }

        private fun setColorWithoutTime(canvas: Canvas) {
            var currentSelectedType =
                prefs?.getString(SharedPreferenceHelper.SharedPrefKey.SELECTED_COLORS, Constants.RANDOM_COLORS)
            if (currentSelectedType == null) {
                currentSelectedType = Constants.RANDOM_COLORS
            }
            val color = if (currentSelectedType == Constants.RANDOM_COLORS) {
                RandomColor.nextColor()
            } else {
                Utils.getRandomColorFromList(currentSelectedType, applicationContext)
            }
            canvas.drawColor(color)
        }

        private fun setColorUsingCurrentTime(canvas: Canvas) {
            val text = Utils.colorFromTime
            val bounds = Rect()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
            paint.getTextBounds(text, 0, text.length, bounds)
            paint.color = Color.RED
            paint.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                20f,
                resources.displayMetrics
            )
            paint.textAlign = Align.LEFT
            val x = canvas.width / 2 - bounds.width() / 2
            val y = canvas.height / 2 - bounds.height() / 2
            canvas.drawColor(Color.parseColor(text))
            canvas.drawText(text, x.toFloat(), y.toFloat(), paint)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (isTimeVariable) {
                if (visible) {
                    handler.post(drawRunner)
                } else {
                    handler.removeCallbacks(drawRunner)
                }
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            this.visible = false
            handler.removeCallbacks(drawRunner)
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver!!)
        }

        override fun onDestroy() {
            super.onDestroy()
            this.visible = false
            handler.removeCallbacks(drawRunner)
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver!!)
        }

        override fun onTouchEvent(event: MotionEvent) {
            gestureDetector?.onTouchEvent(event)
        }

    }


}