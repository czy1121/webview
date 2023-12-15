package com.demo.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.demo.app.databinding.LayoutWebBinding
import com.demo.app.jsmodule.AjaxModule
import me.reezy.cosmo.jsbridge.JSBridge
import com.demo.app.jsmodule.TestModule
import me.reezy.cosmo.webview.bundle.WebBundleManager

class MainActivity : AppCompatActivity(R.layout.layout_web) {

    private val binding by lazy { LayoutWebBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    private val bridge = JSBridge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        bridge.addModule(TestModule())
        bridge.addModule(AjaxModule(this))

        bridge.injectBridge(binding.web)


        binding.web.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
//                logE("onPageStarted => $url")
                bridge.injectModules()
            }

            override fun onPageFinished(view: WebView, url: String?) {
//                logE("onPageFinished => $url")
                bridge.injectModules()
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                return WebBundleManager.get(request)
            }

        }

        binding.web.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            startActivity(intent)
        }

        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.web, true)


        binding.web.requestFocus()
        binding.web.loadUrl("file:///android_asset/demo.html")


    }


    override fun onDestroy() {
        binding.web.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (binding.web.canGoBack()) {
            binding.web.goBack()
        } else {
            super.onBackPressed()
        }
    }

}
