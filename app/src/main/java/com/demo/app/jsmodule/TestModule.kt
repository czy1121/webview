package com.demo.app.jsmodule

import com.demo.app.logE
import me.reezy.cosmo.jsbridge.JSBridgeMethod
import me.reezy.cosmo.jsbridge.JSBridgeModule
import me.reezy.cosmo.jsbridge.argument.JSCallback

class TestModule() : JSBridgeModule {
    override val name: String = "test"

    @JSBridgeMethod
    fun getValue(a: Int, b: Float, c: String, d: Boolean): String {
        logE("this is ${javaClass.simpleName}::getValue in ${Thread.currentThread().name}")
        return "a = $a, b = $b, c = $c, d = $d"
    }

    @JSBridgeMethod
    fun hello(who:String, callback: JSCallback) {
        logE("this is ${javaClass.simpleName}::hello in ${Thread.currentThread().name}")

        callback.invoke("$who: ", mapOf("a" to 1, "b" to 2.5, "c" to true, "d" to "wahaha"))
    }
}