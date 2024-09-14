package me.reezy.cosmo.webview.bundle

import android.content.Context
import android.content.pm.ApplicationInfo
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.Proxy
import java.util.concurrent.TimeUnit

object WebBundleManager {

    private lateinit var bundle: BundleResource

    private lateinit var asset: AssetResource

    private lateinit var cache: CacheResource

    fun init(context: Context, cacheDir: String = "web-cache", cacheMaxSize: Long = 100 * 1024 * 1024) {

        val httpClient = createOkHttpClient(File(context.cacheDir, cacheDir), cacheMaxSize, context.isDebuggable())

        bundle = BundleResource(context.assets, File(context.filesDir, "web-bundle"), httpClient)
        asset = AssetResource(context.assets, "web-asset")
        cache = CacheResource(httpClient)
    }


    fun addBundles(bundles: List<BundleItem>) {
        bundle.add(bundles)
    }

    fun addCacheUrls(vararg baseUrls: String) {
        cache.add(*baseUrls)
    }


    fun get(request: WebResourceRequest): WebResourceResponse? {
        return bundle.get(request) ?: asset.get(request) ?: cache.get(request)
    }


    private fun Context.isDebuggable(): Boolean = try {
        applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }


    private fun createOkHttpClient(cacheDir: File, cacheMaxSize: Long = 100 * 1024 * 1024, isDebuggable: Boolean = true): OkHttpClient {
        return OkHttpClient.Builder()
            .proxy(if (isDebuggable) null else Proxy.NO_PROXY)
            .cache(Cache(cacheDir, cacheMaxSize))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addNetworkInterceptor {
                val response = it.proceed(it.request())
                if (response.headers["Cache-Control"] == null) {
                    response.newBuilder()
                        .header("Cache-Control", "max-age=864000")
                        .build()
                } else {
                    response
                }
            }
            .build()
    }
}