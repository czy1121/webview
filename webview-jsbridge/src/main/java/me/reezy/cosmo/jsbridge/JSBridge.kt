package me.reezy.cosmo.jsbridge

import android.webkit.WebView
import me.reezy.cosmo.jsbridge.internal.JSInterface
import me.reezy.cosmo.jsbridge.internal.JSModule
import me.reezy.cosmo.jsbridge.internal.JSScript
import me.reezy.cosmo.jsbridge.internal.log
import org.json.JSONObject


class JSBridge(val name: String = "JSBridge") {

    private lateinit var web: WebView

    private val modules = mutableMapOf<String, JSModule>()

    private var script: String? = null


    fun addModule(module: JSBridgeModule) {
        modules[module.name] = JSModule(module)
        script = null
    }

    fun injectBridge(webView: WebView) {
        this.web = webView
        webView.addJavascriptInterface(JSInterface(this), name)
    }

    fun injectModules() {
        if (script == null) {
            script = JSScript.gen(name, modules.values)
        }
        script?.let {
            log("inject start")
            web.evaluateJavascript(it) { result ->
                log("inject done: $result")
            }
        }

    }

    internal fun call(moduleName: String, methodName: String, arguments: String): String {
        val module = modules[moduleName] ?: return ""

        val method = module.methods[methodName] ?: return ""

        return try {
            method.invoke(web, arguments)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            JSONObject().put("success", false).put("message", throwable.message).toString()
        }
    }



}


