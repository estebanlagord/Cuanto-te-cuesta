package com.smartpocket.cuantoteroban.repository

import android.content.Context
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import com.smartpocket.cuantoteroban.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.logging.Level


class CurrencyDownloaderXE : CurrencyDownloader() {

    override suspend fun getExchangeRateFor1(currencyFrom: String, currencyTo: String) =
            withContext(Dispatchers.Main) {
                logger.log(Level.INFO, "Downloading exchange rate for ${currencyFrom}x$currencyTo")
                val url = "https://www.xe.com/pt/currencyconverter/convert/" +
                        "?Amount=1&From=$currencyFrom&To=$currencyTo"
                tryWebView(url)
                /*val doc: Document = Jsoup.connect(url).get()
                val rate = doc.getElementsByAttribute("data-exchange-rate")
                        .attr("data-exchange-rate")
                        .toDouble()
                logger.log(Level.INFO, "1 $currencyFrom = $rate $currencyTo")*/
//                return@withContext CurrencyResult(rate)
                return@withContext CurrencyResult(1.0)
            }

    private fun tryWebView(startUrl: String) {
        val context = MyApplication.applicationContext()
        val webView = WebView(context)
        webView.getSettings().setJavaScriptEnabled(true)
        val jInterface = MyJavaScriptInterface(context)
        webView.addJavascriptInterface(jInterface, "HtmlViewer")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                //Load HTML
                webView.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        }
        webView.loadUrl(startUrl)
        val result = jInterface.html
        logger.log(Level.INFO, result)
    }

    class MyJavaScriptInterface internal constructor(val ctx: Context) {
        var html: String? = null

        @JavascriptInterface
        fun showHTML(_html: String?) {
            html = _html
        }

    }
}