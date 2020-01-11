package com.smartpocket.cuantoteroban

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(applicationContext)
    }

    companion object {
        private var instance: MyApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}