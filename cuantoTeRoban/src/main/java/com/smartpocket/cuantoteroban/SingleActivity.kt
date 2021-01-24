package com.smartpocket.cuantoteroban

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.android.billingclient.api.BillingClient
import com.google.android.material.snackbar.Snackbar
import com.smartpocket.cuantoteroban.databinding.ActivitySingleBinding

class SingleActivity : AppCompatActivity() {

    private lateinit var adViewHelper: AdViewHelper
    private lateinit var singleActivityVM: SingleActivityVM
    private lateinit var billingHelper: BillingHelper
    private lateinit var binding: ActivitySingleBinding
//    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        val toolbar = toolbar as Toolbar
//        setSupportActionBar(toolbar)
//        val navController = findNavController(R.id.nav_host_fragment)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        toolbar.setupWithNavController(navController, appBarConfiguration)
//        setupActionBarWithNavController(this, navController)
        adViewHelper = AdViewHelper(binding.adViewContainer, this)
        billingHelper = BillingHelper(this)

        singleActivityVM = ViewModelProvider(this)[SingleActivityVM::class.java]
        singleActivityVM.billingStatusLD.observe(this, this::onBillingHelperStatusChanged)
        singleActivityVM.showAdsLD.observe(this, adViewHelper::showBanner)
        singleActivityVM.launchPurchaseLD.observe(this, {
            billingHelper.launchBillingFlow(this)
        })
        singleActivityVM.launchRestoreAdsLD.observe(this, {
            billingHelper.consumeRemoveAdsPurchase()
        })
        singleActivityVM.snackbarLD.observe(this, { str ->
            Snackbar.make(view, str, Snackbar.LENGTH_LONG).show()
        })
    }

    override fun onDestroy() {
        adViewHelper.destroy()
        super.onDestroy()
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.nav_host_fragment).navigateUp()

    private fun onBillingHelperStatusChanged(code: Int) {
        @StringRes val msg: Int
        when (code) {
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                msg = R.string.billing_status_already_owned
                adViewHelper.destroy()
            }
            BillingClient.BillingResponseCode.OK -> {
                msg = R.string.billing_status_thanks_for_buying
                adViewHelper.destroy()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> msg = R.string.billing_status_canceled
            BillingClient.BillingResponseCode.ERROR -> msg = R.string.billing_status_error
            PURCHASE_STATE_PENDING -> msg = R.string.billing_status_pending
            else -> msg = R.string.billing_status_connection_error
        }
        singleActivityVM.snackbarLD.value = getString(msg)
    }
}
