package ru.bank.ui.screens.rates

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import ru.bank.data.entity.CurrencyHistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RatesChartWebView(history: List<CurrencyHistoryEntity>, accentColorHex: String, modifier: Modifier = Modifier) {
    val dateFmt = SimpleDateFormat("dd.MM", Locale("ru"))
    var isPageReady by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier.fillMaxWidth().height(140.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                setBackgroundColor(0x00000000)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        isPageReady = true
                    }
                }
                loadUrl("file:///android_asset/chart.html")
            }
        },
        update = { webView ->
            if (!isPageReady) return@AndroidView
            val labels = JSONArray(history.map { dateFmt.format(Date(it.date)) })
            val values = JSONArray(history.map { it.rate })
            webView.evaluateJavascript(
                "if (typeof updateChart === 'function') { updateChart($labels, $values, '$accentColorHex'); }",
                null
            )
        }
    )
}
