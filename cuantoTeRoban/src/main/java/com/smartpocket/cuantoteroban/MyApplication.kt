package com.smartpocket.cuantoteroban

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
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

        MobileAds.initialize(applicationContext)
    }

    companion object {
        private var instance: MyApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}