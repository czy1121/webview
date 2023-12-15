package me.reezy.cosmo.jsbridge.internal

import android.os.Looper
import android.webkit.WebView
import me.reezy.cosmo.jsbridge.argument.JSCallback
import me.reezy.cosmo.jsbridge.argument.JSMap
import me.reezy.cosmo.jsbridge.JSBridgeModule
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Method

internal data class JSMethod(
    val module: JSBridgeModule,
    val method: Method,
    val name: String,
    val hasReturn: Boolean,
    val argumentTypes: List<Int>,
    val mainThread: Boolean
) {

    fun invoke(webView: WebView, args: String): String {

        val arguments = parseArguments(webView, args, argumentTypes)

        if (hasReturn) {
            val returnValue = method.invoke(module, *arguments)
            return JSONObject().put("success", true).put("data", returnValue).toString()
        } else if (mainThread && Looper.getMainLooper() != Looper.myLooper()) {

            mainHandler.post {
                method.invoke(module, *arguments)
            }
        } else {
            method.invoke(module, *arguments)
        }
        return JSONObject().put("success", true).toString()
    }


    private fun parseArguments(webView: WebView, args: String, types: List<Int>): Array<Any> {
//        log("args = $args, types = $types")
        val ja = JSONArray(args)

        val arguments = types.mapIndexed { index, type ->
            return@mapIndexed when (type) {
                JSType.TYPE_BOOL -> ja.optBoolean(index)
                JSType.TYPE_INT -> ja.optInt(index)
                JSType.TYPE_LONG -> ja.optLong(index)
                JSType.TYPE_FLOAT -> ja.optDouble(index).toFloat()
                JSType.TYPE_DOUBLE -> ja.optDouble(index)
                JSType.TYPE_STRING -> ja.optString(index)
                JSType.TYPE_ARRAY -> ja.optJSONArray(index)
                JSType.TYPE_OBJECT -> ja.optJSONObject(index)
                JSType.TYPE_CALLBACK -> JSCallback(webView, ja.optString(index))
                JSType.TYPE_MAP -> JSMap(webView, ja.optJSONObject(index))
                else -> throw Throwable("parameter error: invalid type")
            }
        }
        return arguments.toTypedArray()

//        return if (hasContext) {
//            (listOf<Any>(JSContext(webView)) + arguments).toTypedArray()
//        } else {
//            arguments.toTypedArray()
//        }
    }
}