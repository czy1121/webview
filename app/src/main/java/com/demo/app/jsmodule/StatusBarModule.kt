package com.demo.app.jsmodule

import me.reezy.cosmo.jsbridge.JSBridgeMethod
import me.reezy.cosmo.jsbridge.JSBridgeModule

class StatusBarModule : JSBridgeModule {
    override val name: String = "statusbar"

    @JSBridgeMethod
    fun setColor(color: String) {
//        logE("color = $color")
    }

    @JSBridgeMethod
    fun setLight(value: Boolean) {
//        logE("light = $value")
    }
}