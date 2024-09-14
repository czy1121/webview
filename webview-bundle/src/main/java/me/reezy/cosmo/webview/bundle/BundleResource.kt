package me.reezy.cosmo.webview.bundle

import android.content.res.AssetManager
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class BundleResource(private val assets: AssetManager, private val bundleDir: File, private val httpClient: OkHttpClient) {


    private val asset = "asset://"

    private val loaded = mutableMapOf<String, BundleItem>()

    fun add(bundles: List<BundleItem>) {
        if (loaded.isEmpty()) {
            getCachedBundles().forEach { item ->
                if (loaded[item.id]?.uri != item.uri) {
                    load(item)
                }
            }
        }

        bundles.forEach { item ->
            if (loaded[item.id]?.uri != item.uri) {
                load(item)
            }
        }
    }

    fun get(request: WebResourceRequest): WebResourceResponse? {

        val url = request.url.toString().split("?", "#")[0]

        for (bundle in loaded.values) {
            if (url.startsWith(bundle.baseUrl)) {
                val file = File(bundleDir, url.replace(bundle.baseUrl, bundle.id))

                if (file.exists() && file.isFile) {
                    return WebResourceResponse(getMimeType(url), "", file.inputStream())
                }
                return null
            }
        }
        return null
    }


    private fun load(item: BundleItem) {

        if (item.uri.startsWith(asset)) {
            val file = item.uri.substring(asset.length)
            extract(assets.open(file), item)
        } else {
            val request: Request = Request.Builder().url(item.uri).build()
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val bis = response.body?.byteStream() ?: return
                    extract(bis, item)
                }

                override fun onFailure(call: Call, e: IOException) {}
            })
        }
    }


    private fun extract(bundle: InputStream, item: BundleItem) {
        try {
            extractTo(bundle, File(bundleDir, item.id))

            loaded[item.id] = item

            val bundles = loaded.values.map { mapOf("id" to it.id, "uri" to it.uri, "baseUrl" to it.baseUrl) }
            val json = JSONArray(bundles).toString()
            File(bundleDir, "bundles").writeText(json)
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }

    private fun getCachedBundles(): List<BundleItem> {

        try {
            val file = File(bundleDir, "bundles")
            if (file.exists()) {
                val ja = JSONArray(file.readText())

                val array = mutableListOf<BundleItem>()

                for (i in 0 until ja.length()) {
                    val jo = ja.optJSONObject(i)
                    val bundle = BundleItem(
                        id = jo.getString("id"),
                        uri = jo.getString("uri"),
                        baseUrl = jo.getString("baseUrl")
                    )
                    array.add(bundle)
                }

                return array
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }

        return emptyList()
    }


    private fun extractTo(bundle: InputStream, dest: File) {
        val files = mutableListOf<String>()
        for (file in dest.walkTopDown()) {
            if (file.isFile) {
                val filename = file.absolutePath.replace(dest.absolutePath, "").substring(1)
//                    Logger.error("entry => $filename")
                files.add(filename)
            }
        }
        ZipInputStream(bundle).use {
            var entry: ZipEntry? = it.nextEntry

            while (entry != null) {
                if (entry.name.contains("__MACOSX")) {
                    entry = it.nextEntry
                    continue
                }
                if (entry.isDirectory) {
                    File(dest, entry.name).mkdirs()
                } else if (entry.size > 0 && !files.remove(entry.name)) {
//                  Logger.error("append => ${entry.name}")
                    try {
                        it.copyTo(File(dest, entry.name).outputStream())
                    } catch (ex: Throwable) {
                        ex.printStackTrace()
                    }
                }
                entry = it.nextEntry
            }

            it.closeEntry()
        }
        files.forEach {
//                Logger.error("delete => $it")
            File(dest, it).delete()
        }
    }

    private fun getMimeType(url: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url))
    }
}