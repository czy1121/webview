package me.reezy.cosmo.jsbridge.internal

import android.os.Handler
import android.os.Looper
import android.util.Log


internal val mainHandler = Handler(Looper.getMainLooper())

internal fun log(message: String) {
    Log.d("jsbridge", message);
}