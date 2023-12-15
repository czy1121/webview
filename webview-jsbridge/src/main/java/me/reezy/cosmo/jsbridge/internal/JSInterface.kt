package me.reezy.cosmo.jsbridge.internal

import android.webkit.JavascriptInterface
import me.reezy.cosmo.jsbridge.JSBridge

internal class JSInterface(private val bridge: JSBridge) {
    @JavascriptInterface
    fun call(moduleName: String, methodName: String, arguments: String): String {
        return bridge.call(moduleName, methodName, arguments)
    }
}