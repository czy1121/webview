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

    // js/css
    private lateinit var bundle: BundleResource

    // html
    private lateinit var asset: AssetResource

    //
    private lateinit var cache: CacheResource

    fun init(context: Context, cacheDir: String = "okhttp-web", cacheMaxSize: Long = 100 * 1024 * 1024) {

        val httpClient = context.createOkHttpClient(cacheDir, cacheMaxSize)

        bundle = BundleResource(context, httpClient)
        asset = AssetResource(context.assets)
        cache = CacheResource(httpClient)
    }


    fun loadBundles(locals: List<String>) {
        bundle.load(locals.associate { "asset://$it" to "" })
    }

    fun loadBundles(remotes: Map<String, String>) {
        bundle.load(remotes)
    }

    fun setAssetRootDir(dir: String) {
        asset.setRootDir(dir)
    }

    fun addCacheUrls(vararg baseUrls: String) {
        cache.add(*baseUrls)
    }


    fun get(request: WebResourceRequest): WebResourceResponse? {
        bundle.intercept(request)?.let {
            return it
        }
        asset.intercept(request)?.let {
            return it
        }
        cache.intercept(request)?.let {
            return it
        }
        return null
    }


    private fun Context.isDebuggable(): Boolean = try {
        applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }


    private fun Context.createOkHttpClient(cacheDir: String, cacheMaxSize: Long = 100 * 1024 * 1024): OkHttpClient {
        return OkHttpClient.Builder()
            .proxy(if (isDebuggable()) null else Proxy.NO_PROXY)
            .cache(Cache(File(cacheDir, cacheDir), cacheMaxSize))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addNetworkInterceptor {
                val response = it.proceed(it.request())
                if (response.headers["Cache-Control"] == null) {
                    response.newBuilder().header("Cache-Control", "max-age=7200").build()
                } else {
                    response
                }
            }
            .build()
    }
}