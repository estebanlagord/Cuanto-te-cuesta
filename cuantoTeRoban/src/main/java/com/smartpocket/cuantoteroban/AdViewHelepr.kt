package com.smartpocket.cuantoteroban

import android.app.Activity
import android.util.DisplayMetrics
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// This is an ad unit ID for a test ad. Replace with your own banner ad unit ID.
//private const val AD_UNIT_ID = "ca-app-pub-6954073861191346/2251963282" //REAL ADS
private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"  //TEST ADS

class AdViewHelper(private val adViewContainer: ViewGroup, private val activity: Activity) {

    private var adView: AdView? = null

    init {
        adViewContainer.post { loadBanner() }
    }

    fun resume() = adView?.resume()
    fun pause() = adView?.pause()
    fun destroy() {
        adView?.destroy()
        adView = null
    }

    private fun loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        adView = AdView(activity)
        adView?.setAdUnitId(AD_UNIT_ID)
        adViewContainer.removeAllViews()
        adViewContainer.addView(adView)

        val adSize: AdSize = adSize
        adView?.adSize = adSize

        val adRequest = AdRequest.Builder()
                .addTestDevice("97EB45A0B9C0380783B9EC4628B453EB")
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build()

        // Start loading the ad in the background.
        adView?.loadAd(adRequest)
    }

    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.
    private val adSize: AdSize
        get() {
            val display = activity.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val density = outMetrics.density

            var adWidthPixels = adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
        }
}