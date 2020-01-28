package com.smartpocket.cuantoteroban

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.*
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import java.util.logging.Level
import java.util.logging.Logger


const val PURCHASE_STATE_PENDING = -1

class BillingHelper(val activity: FragmentActivity) : PurchasesUpdatedListener {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private lateinit var billingClient: BillingClient
    private val preferences = PreferencesManager.getInstance()
    private val skuList = listOf("ads_removal")
    private var skuDetails: SkuDetails? = null
    private var isErrorState = false
    private var singleActivityVM: SingleActivityVM = ViewModelProvider(activity)[SingleActivityVM::class.java]

    init {
        setupBillingClient()
    }

    fun launchBillingFlow(activity: FragmentActivity) {
        if (isErrorState) setupBillingClient() //TODO ?

        val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(skuDetails)
                .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    fun isRemoveAdsPurchased(): Boolean {
        val isPurchased: Boolean
        val purchaseList = billingClient.queryPurchases(SkuType.INAPP).purchasesList
        if (purchaseList.isNullOrEmpty()) {
            isPurchased = preferences.isRemoveAdsPurchased
        } else {
            val purchase = purchaseList[0]
            //TODO SECURITY ON JSON
            isPurchased = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            preferences.setIsRemoveAdsPurchased(isPurchased)
        }
        logger.log(Level.INFO, "Is Remove Ads purchased? $isPurchased")
        singleActivityVM.showAdsLD.postValue(isPurchased.not())
        return isPurchased
    }

    fun consumeRemoveAdsPurchase() {
        val purchaseList = billingClient.queryPurchases(SkuType.INAPP).purchasesList
        for (purchase in purchaseList) {
            consumePurchase(purchase)
        }
    }

    private fun setupBillingClient() {
        logger.log(Level.INFO, "Setting up BillingHelper")
        billingClient = newBuilder(activity.applicationContext)
                .enablePendingPurchases()
                .setListener(this)
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    // The BillingClient is setup successfully
                    logger.log(Level.INFO, "Setup Billing Done")
                    loadAllSKUs()
                    isRemoveAdsPurchased()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                logger.log(Level.SEVERE, "Billing service disconnected")
                isErrorState = true
            }
        })
    }

    private fun loadAllSKUs() = if (billingClient.isReady) {
        val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(SkuType.INAPP)
                .build()
        billingClient.querySkuDetailsAsync(params) { billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails> ->
            // Process the result.
            if (billingResult.responseCode == BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                this.skuDetails = skuDetailsList[0]
            } else {
                logger.log(Level.WARNING, billingResult.debugMessage)
            }
        }
        isErrorState = false

    } else {
        logger.log(Level.INFO, "Billing Client not ready")
        isErrorState = true
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        singleActivityVM?.billingStatusLD?.postValue(billingResult.responseCode)
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
//                acknowledgePurchase(purchase.purchaseToken)
            }
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            logger.log(Level.WARNING, "User canceled purchase")
        } else {
            // Handle any other error codes.
            logger.log(Level.SEVERE, "Error during purchase: ${billingResult.responseCode}")
        }
        isRemoveAdsPurchased()
    }

    private fun handlePurchase(purchase: Purchase) {
//        purchase.originalJson //todo SECURITY

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant the item to the user, and then acknowledge the purchase
            preferences.setIsRemoveAdsPurchased(true)
            logger.log(Level.INFO, "User is entitled to purchase")
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that
            // are given to them. You can also choose to remind the user in the
            // future to complete the purchase if you detect that it is still
            // pending.
            singleActivityVM.billingStatusLD.postValue(PURCHASE_STATE_PENDING)
        }

    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient.acknowledgePurchase(params) {
            logger.log(Level.INFO, "Purchase acknowledged")
        }
    }


    private fun consumePurchase(purchase: Purchase) {
        logger.log(Level.INFO, "Trying to consume purchase")
        val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient.consumeAsync(consumeParams) { p0, _ ->
            preferences.setIsRemoveAdsPurchased(false)
            isRemoveAdsPurchased()
            logger.log(Level.INFO, "on consume response $p0")
        }
    }
}