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
        billingHelper = BillingHelper(applicationContext)
    }

    companion object {
        private var instance: MyApplication? = null
        private var billingHelper: BillingHelper? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun billingHelper() : BillingHelper {
            return billingHelper!!
        }
    }
}