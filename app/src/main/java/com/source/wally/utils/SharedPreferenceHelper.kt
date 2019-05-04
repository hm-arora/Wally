package com.source.wally.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceHelper(context: Context) {
    private var mPref: SharedPreferences? = null
    private var mEditor: SharedPreferences.Editor? = null

    init {
        this.mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private var mSharePrefs: SharedPreferenceHelper? = null
        private const val SETTINGS_NAME = "kWallyPrefs"
        fun getInstance(context: Context): SharedPreferenceHelper {
            if (mSharePrefs == null) {
                mSharePrefs = SharedPreferenceHelper(context)
            }
            return mSharePrefs as SharedPreferenceHelper
        }
    }

    object SharedPrefKey {
        const val FOLDER_PATH = "folder_path"
        const val WALLPAPER_CHOICE = "wallpaper_choice"
        const val SELECTED_COLORS = "selected_colors"
        const val IS_TIME_VARIABLE = "is_time_variable"
    }

    fun put(key: String, value: String) {
        if (mPref != null) {
            mEditor = mPref!!.edit()
            mEditor?.putString(key, value)
            mEditor?.apply()
        }
    }

    fun put(key: String, value: Boolean) {
        if (mPref != null) {
            mEditor = mPref!!.edit()
            mEditor?.putBoolean(key, value)
            mEditor?.apply()
        }
    }



    fun getString(key: String, defaultValue: String?): String? {
        return mPref?.getString(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        if (mPref == null) {
            return false
        }
        return mPref!!.getBoolean(key, defaultValue)
    }

}