package me.reezy.cosmo.webview.bundle

import android.content.Context
import android.content.res.AssetManager
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import okhttp3.*
import okio.Buffer
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class BundleResource(context: Context, private val httpClient: OkHttpClient) {

    private class BundleItem(val uri: String, val hash: String)

    private val assets: AssetManager = context.assets

    private val rootDir: File = File(context.filesDir, "webview-bundle")

    private val loaded = mutableSetOf<BundleItem>()

    private var isCacheLoaded = false

    fun load(items: Map<String, String>) {
        if (isCacheLoaded) {
            isCacheLoaded = true
            load(getCachedItems())
        }
        load(items.map { entry ->
            if (entry.key.startsWith("asset://")) {
                assets.open(entry.key.substring(8)).use {
                    val buffer = Buffer()
                    buffer.readFrom(it)
                    BundleItem(entry.key, buffer.md5().hex())
                }
            } else {
                BundleItem(entry.key, entry.value)
            }
        })
    }

    private fun load(items: List<BundleItem>) {
        items.forEach { item ->
            if (!loaded.any { it.hash == item.hash }) {
                loaded.removeAll { item.uri == it.uri }
                load(item)
            }
        }
    }


    private fun load(item: BundleItem) {

        if (item.uri.startsWith("asset://")) {
            assets.open(item.uri).unzip(item)
        } else {
            val request: Request = Request.Builder().url(item.uri).build()
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    response.body?.byteStream()?.unzip(item)
                }

                override fun onFailure(call: Call, e: IOException) {

                }
            })
        }
    }

    private fun getCachedItems(): List<BundleItem> {

        try {
            val json = File(rootDir, "bundles").readText()
            val ja = JSONArray(json)
            val array = mutableListOf<BundleItem>()
            for (i in 0 until ja.length()) {
                val jo = ja.optJSONObject(i)
                val bundle = BundleItem(
                    uri = jo.getString("uri"),
                    hash = jo.getString("hash")
                )
                array.add(bundle)
            }

            return array
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }

        return emptyList()
    }

    private fun save() {
        try {
            val bundles = loaded.map { mapOf("uri" to it.uri, "hash" to it.hash) }
            val json = JSONArray(bundles).toString()
            File(rootDir, "bundles").writeText(json)
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }

    private fun InputStream.unzip(item: BundleItem) {
        try {
            val dest = File(rootDir, item.hash)
            ZipInputStream(this).use {
                var entry: ZipEntry? = it.nextEntry

                while (entry != null) {
                    if (entry.isDirectory) {
                        File(dest, entry.name).mkdirs()
                    } else {
                        val os = File(dest, entry.name).outputStream()
                        it.copyTo(os)
                        it.closeEntry()
                    }
                    entry = it.nextEntry
                }
            }
            loaded.add(item)
            save()
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }


    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        val uri = request.url
        val base = File(rootDir, uri.authority ?: return null)

        base.list()?.let {
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