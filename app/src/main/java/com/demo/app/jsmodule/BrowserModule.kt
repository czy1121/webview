package com.demo.app.jsmodule

import me.reezy.cosmo.jsbridge.JSBridgeMethod
import me.reezy.cosmo.jsbridge.JSBridgeModule

class BrowserModule() : JSBridgeModule {
    override val name: String = "browser"

    @JSBridgeMethod
    fun back() {
    }

    @JSBridgeMethod
    fun close() {
    }

    @JSBridgeMethod
    fun open(uri: String) {
    }

}