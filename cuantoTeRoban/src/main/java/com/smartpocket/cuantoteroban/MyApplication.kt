package com.smartpocket.cuantoteroban

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import com.smartpocket.cuantoteroban.preferences.PreferencesManager.THEME_CLEAR
import com.smartpocket.cuantoteroban.preferences.PreferencesManager.THEME_DARK
import java.util.logging.Level
import java.util.logging.Logger

class MyApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        Logger.getLogger("").level =
                if (BuildConfig.DEBUG) Level.FINEST
                else Level.OFF

        setChosenTheme()
        MobileAds.initialize(applicationContext)
    }

    private fun setChosenTheme() {
        val nightMode = when (PreferencesManager.getInstance().chosenTheme) {
            THEME_CLEAR -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {
        private var instance: MyApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}