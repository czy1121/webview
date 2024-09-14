package me.reezy.cosmo.webview.bundle

import android.content.res.AssetManager
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.FileNotFoundException

class AssetResource(private val assets: AssetManager, private val assetDir: String = "web-asset") {

    fun get(request: WebResourceRequest): WebResourceResponse? {
        val uri = request.url

        try {
            val path = uri.path ?: return null
            val stream = assets.open("$assetDir/${uri.authority}/$path")
            return WebResourceResponse(getMimeType(path), "", stream)
        } catch (ex: FileNotFoundException) {
            //
        }
        return null
    }

    private fun getMimeType(url: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url))
    }
}