package me.reezy.cosmo.jsbridge.argument

import android.webkit.WebView
import me.reezy.cosmo.jsbridge.internal.mainHandler
import org.json.JSONArray
import org.json.JSONObject

class JSCallback internal constructor(private val webView: WebView, private val ref: String) {

    fun invoke(vararg args: Any) {
        val (bridge, bucket, reqId, name) = ref.split("#")
        val arguments = args.joinToString(", ", transform = this::stringify)
        mainHandler.post {
            webView.evaluateJavascript("$bridge.onCallback('$bucket', '$reqId', '$name', [$arguments])") { }
        }
    }

    private fun stringify(value: Any?): String = when (value) {
        null -> "null"
        is String -> "'$value'"
        is Number -> value.toString()
        is Boolean -> value.toString()
        is JSONObject -> value.toString()
        is JSONArray -> value.toString()
        is Map<*, *> -> JSONObject(value).toString()
        is Array<*> -> JSONArray(value).toString()
        is Collection<*> -> JSONArray(value).toString()
        else -> "'${value}'"
    }
}