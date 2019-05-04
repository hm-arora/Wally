/*
 * Copyright (C) 2014-2016 Appgramming
 * http://www.appgramming.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.source.wally.utils

import android.content.*
import android.graphics.Color
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.source.wally.Bucket
import com.source.wally.R
import java.io.File
import java.io.IOException

import java.util.Calendar

/**
 * Static utility methods.
 */
class Utils private constructor() {


    companion object {

        /**
         * Converts an integer color value to a hexadecimal string.
         */
        fun colorToHex(color: Int): String {
            return String.format("#%06X", 0xFFFFFF and color)
        }

        /**
         * Copies a text to clipboard.
         */
        fun copyText(context: Context, text: CharSequence) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(context.getString(R.string.app_name), text)
            clipboard.primaryClip = clip
        }

        /**
         * Goes home.
         */
        fun goHome(context: Context) {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(startMain)
        }

        val colorFromTime: String
            get() {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minutes = calendar.get(Calendar.MINUTE)
                val seconds = calendar.get(Calendar.SECOND)
                return "#" + checkTime(hour) + checkTime(minutes) + checkTime(seconds)
            }

        private fun checkTime(i: Int): String {
            var s: String
            s = i.toString()
            if (i < 10) {
                s = "0$s"
            }
            return s
        }

        fun setColorWallpaper(context: Context) {

            var colorParam: Int? = null
            try {
                colorParam = getColor(context)
            } catch (ignored: Exception) {
            }

            val color = colorParam ?: RandomColor.nextColor()
            try {

                ColorWallpaper.setColorWallpaper(context, color)

                Utils.copyText(context, Utils.colorToHex(color))

                // Go to the home screen
                Utils.goHome(context)

            } catch (e: IOException) {

                // Write the stack trace to System.err and copy the reason of the failure to clipboard
                e.printStackTrace()
                Utils.copyText(context, e.toString())
            }
        }

        fun getRandomMaterialColorAndSize(typeColor: String, context: Context): ArrayList<Any> {
            val array: ArrayList<Any> = ArrayList()
            var returnColor = Color.GRAY
            val arrayId = context.resources.getIdentifier(typeColor, "array", context.packageName)
            if (arrayId != 0) {
                val colors = context.resources.obtainTypedArray(arrayId)
                array.add(colors.length())
                val index = (Math.random() * colors.length()).toInt()
                returnColor = colors.getColor(index, Color.GRAY)
                array.add(returnColor)
                colors.recycle()
            }
            return array
        }

        fun getRandomColorFromList(listNumber: String, context: Context): Int {
            var returnColor = Color.GRAY
            Toast.makeText(context, listNumber, Toast.LENGTH_SHORT).show()
            val arrayId = context.resources.getIdentifier(listNumber, "array", context.packageName)
            if (arrayId != 0) {
                val colors = context.resources.obtainTypedArray(arrayId)
                val index = (Math.random() * colors.length()).toInt()
                returnColor = colors.getColor(index, Color.GRAY)
                colors.recycle()
            }
            return returnColor
        }

        fun getImageBuckets(mContext: Context): ArrayList<Bucket> {
            val buckets: ArrayList<Bucket> = ArrayList()
            val bucketSet = mutableSetOf<String>()
            val col = mutableMapOf<String, Int>()
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
            var totalPhotosInBucketPath = 0
            var isNewDir = false
            val cursor = mContext.contentResolver.query(uri, projection, null, null, projection[0] + " ASC")
            if (cursor != null) {
                var file: File
                while (cursor.moveToNext()) {
                    if (isNewDir && buckets.size > 1) {
                        buckets[buckets.size - 2].totalImages = totalPhotosInBucketPath - 1
                        totalPhotosInBucketPath = 1
                    }
                    totalPhotosInBucketPath++
                    val bucketPath = cursor.getString(cursor.getColumnIndex(projection[0]))
                    val firstPath = cursor.getString(cursor.getColumnIndex(projection[1]))
                    if (col.containsKey(bucketPath)) {
                        col[bucketPath] = col.getValue(bucketPath) + 1
                    } else {
                        col[bucketPath] = 1
                    }
                    if (!bucketSet.contains(bucketPath)) {
                        file = File(firstPath)
                        isNewDir = true
                        if (file.exists()) {
                            buckets.add(Bucket(bucketPath, firstPath))
                            bucketSet.add(bucketPath)
                        }
                    } else {
                        isNewDir = false
                    }
                }

                // for last object
                if (buckets.size > 0) {
                    buckets[buckets.size - 1].totalImages = totalPhotosInBucketPath
                }
                cursor.close()
            }
            return buckets
        }

        fun getColorBuckets(context: Context): ArrayList<Bucket> {
            val colorBucketList: ArrayList<Bucket> = ArrayList()

            val bucket =
                Bucket(
                    "Random Colors",
                    Constants.RANDOM_COLORS,
                    -1,
                    ContextCompat.getColor(context, R.color.green_30_opac)
                )
            colorBucketList.add(bucket)
            for (i in 1..7) {
                val listTypeNumber = "list$i"
                val arrayList = Utils.getRandomMaterialColorAndSize(listTypeNumber, context)
                val totalSize = arrayList[0] as Int
                val color = arrayList[1] as Int
                val tempBucket = Bucket("Color list $i", listTypeNumber, totalSize, color)
                colorBucketList.add(tempBucket)
            }
            return colorBucketList
        }

        private fun getClipParameter(context: Context): String? {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {

                // Get the current primary clip on the clipboard
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {

                    val description = clip.description

                    // Return null if the clipboard does not contain plain text or html text
                    if (!description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) && !description.hasMimeType(
                            ClipDescription.MIMETYPE_TEXT_HTML
                        )
                    ) {
                        return null
                    }

                    // Ignore and return null if it's a clip previously copied by LoneColor
                    val label = description.label
                    if (label != null && label == context.getString(R.string.app_name)) {
                        return null
                    }

                    // Get the text from the clipboard
                    val sequence = clip.getItemAt(0).text
                    if (sequence != null) {
                        return sequence.toString()
                    }
                }
            }

            return null
        }

        /**
         * Returns the value of the color clip parameter.
         *
         * @return An Integer color value, or null.
         */
        fun getColor(context: Context): Int? {

            // Get the clip parameter from the clipboard
            val clipText = getClipParameter(context)

            // Return null if the clip is null or empty
            if (TextUtils.isEmpty(clipText)) {
                return null
            }

            // Try to parse the clip parameter to a color value, "as it is"
            try {
                return Color.parseColor(clipText)
            } catch (ignored: IllegalArgumentException) {
                // Ignore error, try next color format
            }

            // Try to parse the clip parameter with a "#" in front
            try {
                return Color.parseColor('#' + clipText!!)
            } catch (ignored: IllegalArgumentException) {
                // Ignore error, we will return false
            }

            // No valid color, return null
            return null
        }
    }


}
