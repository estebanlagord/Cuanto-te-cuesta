package com.smartpocket.cuantoteroban.repository

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.*
import androidx.annotation.RequiresApi
import com.smartpocket.cuantoteroban.MyApplication
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level

const val timeoutSecs = 30L

@SuppressLint("SetJavaScriptEnabled")
class CurrencyDownloaderXE : CurrencyDownloader() {

    @Suppress("RemoveExplicitTypeArguments")
    override suspend fun getExchangeRateFor1(currencyFrom: String, currencyTo: String): CurrencyResult {
        return withContext<CurrencyResult>(Dispatchers.Main) {
            val isFinishedLoading = AtomicBoolean(false)
            val isFinishedParsing = AtomicBoolean(false)
            val start = Date()
            logger.log(Level.INFO, "Downloading exchange rate for ${currencyFrom}x$currencyTo")
            val context = MyApplication.applicationContext()
            val url = "https://www.xe.com/pt/currencyconverter/convert/?Amount=1&From=$currencyFrom&To=$currencyTo"
            if (isActive.not()) throw CancellationException()
            val webView = WebView(context)
            with(webView.settings) {
                javaScriptEnabled = true
                blockNetworkImage = true
                loadsImagesAutomatically = false
//                allowFileAccess = false
//                allowContentAccess = false
            }
            val jInterface = MyJavaScriptInterface(isFinishedLoading, isFinishedParsing, currencyFrom)
            webView.addJavascriptInterface(jInterface, "HtmlViewer")
            webView.webViewClient = object : WebViewClient() {

/*                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    logger.log(Level.INFO, "on received HTTP error")
                }

                @RequiresApi(Build.VERSION_CODES.M)
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    logger.log(Level.INFO, "on received error")
                    logger.severe(request?.url.toString())
                    logger.severe("Eror was: ${error?.errorCode} - ${error?.description}")
                    isFinishedLoading.set(true)
                    isFinishedParsing.set(true)
                }*/

                override fun onPageFinished(view: WebView?, url: String?) {
//                    logger.log(Level.INFO, "on page finished loading")
                    isFinishedLoading.set(true)
                    extractHtml(webView)
                }
            }
            if (isActive.not()) throw CancellationException()
            webView.loadUrl(url)
            while (jInterface.result == null
                    && isFinishedParsing.get().not()
                    && isTimedOut(start).not()
                    && isActive) {
                delay(250)
                extractHtml(webView)
                delay(250)
            }
            webView.stopLoading()

            val result = jInterface.result
            if (result != null) {
                val end = Date()
                val elapsed = (end.time - start.time) / 1000.0
                logger.log(Level.INFO, "Found XE rate from $currencyFrom to $currencyTo in ${elapsed}s: ${result.official}")
                return@withContext result
            } else {
                val msg = if (isTimedOut(start)) {
                    "Timeout getting exchange rate for $currencyFrom"
                } else {
                    "Unable to get exchange rate for $currencyFrom"
                }
                throw IllegalStateException(msg)
            }
        }
    }

    private fun isTimedOut(start: Date) = Date().time - start.time > TimeUnit.SECONDS.toMillis(timeoutSecs)

    private fun extractHtml(wv: WebView) {
        wv.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
    }

    inner class MyJavaScriptInterface(private val isFinishedLoading: AtomicBoolean, private val isFinishedParsing: AtomicBoolean, val currencyFrom: String) {
        var result: CurrencyResult? = null

        @JavascriptInterface
        fun showHTML(html: String) {
//            logger.log(Level.INFO, "showHTML for XE downloader: ${html?.length}")
            try {
                val regex = """1 $currencyFrom = (\S+) ARS"""
                val match = regex.toRegex().find(html)
                if (match != null) {
                    val rateWithComma = match.groupValues[1]
                    val rate = rateWithComma
                            .replace(".", "")
                            .replace(",", ".")
                            .toDouble()
                    result = CurrencyResult(rate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logger.log(Level.WARNING, "Error parsing html")
            }
            if (isFinishedLoading.get()) {
                isFinishedParsing.set(true)
            }
        }
    }
}