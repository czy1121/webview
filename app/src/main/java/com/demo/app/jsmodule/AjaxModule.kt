package com.demo.app.jsmodule

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import me.reezy.cosmo.jsbridge.JSBridgeMethod
import me.reezy.cosmo.jsbridge.JSBridgeModule
import me.reezy.cosmo.jsbridge.argument.JSMap
import org.json.JSONObject

class AjaxModule(val activity: AppCompatActivity) : JSBridgeModule {
    override val name: String = "ajax"


    @JSBridgeMethod
    fun get(url: String, params: JSONObject?, options: JSMap) {
        Log.e("jsbridge", "ajax.get()")


    }
    @JSBridgeMethod
    fun post(url: String, data: String?, options: JSMap) {
        Log.e("jsbridge", "ajax.request()")


    }
}


/*
*
*
*
*
*
*
* */