package me.reezy.cosmo.jsbridge.argument

import android.webkit.WebView
import org.json.JSONObject

class JSMap internal constructor(private val webView: WebView, o: JSONObject)
    : JSONObject(o, o.keys().asSequence().toList().toTypedArray()) {

    fun optCallback(name: String): JSCallback {
        return JSCallback(webView, optString(name))
    }
}