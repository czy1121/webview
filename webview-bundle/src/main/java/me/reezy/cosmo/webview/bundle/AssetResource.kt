package me.reezy.cosmo.webview.bundle

import android.content.res.AssetManager
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse

class AssetResource(private val assets: AssetManager) {

    private var rootDir: String = "webview-asset"


    fun setRootDir(dir: String): AssetResource {
        rootDir = dir
        return this
    }

    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        val uri = request.url

        val base = "$rootDir/${uri.authority}"

        assets.list(base)?.let {
            val filename = uri.path ?: return null
            if (it.contains(filename)) {
                val mime = MimeTypeMap.getFileExtensionFromUrl(filename)
                val stream = assets.open("$base/$filename")
                return WebResourceResponse(mime, "", stream)
            }
        }
        return null
    }

}