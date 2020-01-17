package com.smartpocket.cuantoteroban

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.*
import java.util.logging.Level
import java.util.logging.Logger


class BillingHelper(val context: Context) : PurchasesUpdatedListener {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private lateinit var billingClient: BillingClient
    private val skuList = listOf("android.test.purchased")
    private var skuDetails: SkuDetails? = null
    private var isErrorState = false
    private var listener : BillingHelperStatusListener? = null

    init {
        setupBillingClient()
    }

    fun launchBillingFlow(activity: Activity, listener: BillingHelperStatusListener) {
        this.listener = listener
        if (isErrorState) setupBillingClient() //TODO ?

        val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(skuDetails)
                .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    fun isRemoveAdsPurchased(): Boolean {
        var result = false
        val purchaseList = billingClient.queryPurchases(SkuType.INAPP).purchasesList
        if (purchaseList.isNotEmpty()) {
            val purchase = purchaseList[0]
            //TODO SECURITY ON JSON
            result = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        logger.log(Level.INFO, "Is Remove Ads purchased? $result")
        return result
    }

    fun consumeRemoveAdsPurchase() {
        val purchaseList = billingClient.queryPurchases(SkuType.INAPP).purchasesList
        for (purchase in purchaseList) {
            consumePurchase(purchase)
        }
    }

    private fun setupBillingClient() {
        logger.log(Level.FINE, "Setting up BillingHelper")
        billingClient = newBuilder(context)
                .enablePendingPurchases()
                .setListener(this)
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    // The BillingClient is setup successfully
                    logger.log(Level.FINE, "Setup Billing Done")
                    loadAllSKUs()
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
        logger.log(Level.FINE, "Billing Client not ready")
        isErrorState = true
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        listener?.onBillingHelperStatusChanged(billingResult.responseCode)
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
//            val purchaseList = billingClient.queryPurchases(SkuType.INAPP).purchasesList
//            for (purchase in purchaseList) {
//                consumePurchase(purchase.purchaseToken)
//            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        purchase.originalJson //todo SECURITY

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant the item to the user, and then acknowledge the purchase
//            ...
            logger.log(Level.INFO, "User is entitled to purchase")
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
//            consumePurchase(purchase)  //TODO REMOVE, TESTING ONLY
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that
            // are given to them. You can also choose to remind the user in the
            // future to complete the purchase if you detect that it is still
            // pending.
        }

    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
        }
    }


    private fun consumePurchase(purchase: Purchase) {
        logger.log(Level.INFO, "Trying to consume purchase")
        val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient.consumeAsync(consumeParams, object : ConsumeResponseListener {
            override fun onConsumeResponse(p0: BillingResult?, p1: String?) {
                logger.log(Level.INFO, "on consume response $p0")
            }

        })
    }
}