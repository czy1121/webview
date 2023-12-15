package me.reezy.cosmo.webview.simple

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.ClientCertRequest
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.HttpAuthHandler
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.annotation.RequiresApi

class SimpleWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {

    private var progress: ProgressBar? = null

    private var outerWebViewClient: WebViewClient = WebViewClient()
    private var outerWebChromeClient: WebChromeClient? = null


    private var onReceivedTitle: ValueCallback<String>? = null

    init {
        initWebSettings()

        setWebContentsDebuggingEnabled(context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)

        super.setWebViewClient(InnerWebViewClient())

        super.setWebChromeClient(InnerWebChromeClient())
    }

    fun setOnReceivedTitle(callback: ValueCallback<String>?) {
        onReceivedTitle = callback
    }

    

    override fun onFinishInflate() {
        super.onFinishInflate()
        progress = findViewById(android.R.id.progress)
    }


    override fun getWebChromeClient(): WebChromeClient? {
        return outerWebChromeClient
    }

    override fun setWebChromeClient(client: WebChromeClient?) {
        outerWebChromeClient = client
    }

    override fun getWebViewClient(): WebViewClient {
        return outerWebViewClient
    }

    override fun setWebViewClient(client: WebViewClient) {
        outerWebViewClient = client
    }

    override fun destroy() {
        (parent as? ViewGroup)?.removeView(this)
        removeAllViews()
        stopLoading()
        pauseTimers()
        loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        clearHistory()
        super.destroy()
    }



    private fun initWebSettings() {

        // 存储(storage)
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        // 定位(location)
        settings.setGeolocationEnabled(true)

        // 缩放(zoom)
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        // 文件权限
        settings.allowContentAccess = true
        settings.allowFileAccess = true

        //
        settings.textZoom = 100

        @SuppressLint("SetJavaScriptEnabled")
        // 支持Javascript
        settings.javaScriptEnabled = true

        // 支持https
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // 页面自适应手机屏幕，支持viewport属性
        settings.useWideViewPort = true
        // 缩放页面，使页面宽度等于WebView宽度
        settings.loadWithOverviewMode = true

        // 是否自动加载图片
        settings.loadsImagesAutomatically = true
        // 禁止加载网络图片
        settings.blockNetworkImage = false
        // 禁止加载所有网络资源
        settings.blockNetworkLoads = false


        // deprecated
//        settings.setAppCacheEnabled(false)
//        settings.setAppCachePath(context.cacheDir.absolutePath)
//        settings.saveFormData = true
//        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
//        settings.databasePath = context.getDir("database", Context.MODE_PRIVATE).path
//        settings.setGeolocationDatabasePath(context.filesDir.path)
//        settings.allowFileAccessFromFileURLs = false
//        settings.allowUniversalAccessFromFileURLs = false
    }

    private inner class InnerWebViewClient : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            return outerWebViewClient.shouldInterceptRequest(view, request)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return outerWebViewClient.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
            return outerWebViewClient.shouldOverrideKeyEvent(view, event)
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            outerWebViewClient.onPageStarted(view, url, favicon)
            progress?.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String?) {
            outerWebViewClient.onPageFinished(view, url)
            progress?.visibility = View.GONE
        }

        override fun onPageCommitVisible(view: WebView, url: String) {
            outerWebViewClient.onPageCommitVisible(view, url)
        }


        override fun onLoadResource(view: WebView, url: String) {
            outerWebViewClient.onLoadResource(view, url)
        }

        override fun onFormResubmission(view: WebView, dontResend: Message, resend: Message) {
            outerWebViewClient.onFormResubmission(view, dontResend, resend)
        }

        override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
            outerWebViewClient.onScaleChanged(view, oldScale, newScale)
        }

        override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest?) {
            outerWebViewClient.onReceivedClientCertRequest(view, request)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            outerWebViewClient.onReceivedError(view, request, error)
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            outerWebViewClient.onReceivedSslError(view, handler, error)
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            outerWebViewClient.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onReceivedLoginRequest(view: WebView, realm: String, account: String?, args: String) {
            outerWebViewClient.onReceivedLoginRequest(view, realm, account, args)
        }

        override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
            outerWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm)
        }

        override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
            outerWebViewClient.onUnhandledKeyEvent(view, event)
        }

        override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
            outerWebViewClient.doUpdateVisitedHistory(view, url, isReload)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            return outerWebViewClient.onRenderProcessGone(view, detail)
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onSafeBrowsingHit(view: WebView, request: WebResourceRequest, threatType: Int, callback: SafeBrowsingResponse) {
            outerWebViewClient.onSafeBrowsingHit(view, request, threatType, callback)
        }
    }

    private inner class InnerWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            outerWebChromeClient?.onProgressChanged(view, newProgress)
            progress?.progress = newProgress
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            outerWebChromeClient?.onReceivedTitle(view, title)
            onReceivedTitle?.onReceiveValue(title)
        }

        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            return outerWebChromeClient?.onCreateWindow(view, isDialog, isUserGesture, resultMsg) ?: false
        }

        override fun onCloseWindow(window: WebView) {
            outerWebChromeClient?.onCloseWindow(window)
        }

        override fun onGeolocationPermissionsHidePrompt() {
            outerWebChromeClient?.onGeolocationPermissionsHidePrompt()
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            outerWebChromeClient?.onGeolocationPermissionsShowPrompt(origin, callback)
        }

        override fun onHideCustomView() {
            outerWebChromeClient?.onHideCustomView()
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            outerWebChromeClient?.onShowCustomView(view, callback)
        }


        override fun onPermissionRequest(request: PermissionRequest) {
            outerWebChromeClient?.onPermissionRequest(request)
        }

        override fun onPermissionRequestCanceled(request: PermissionRequest) {
            outerWebChromeClient?.onPermissionRequestCanceled(request)
        }

        override fun onRequestFocus(view: WebView) {
            outerWebChromeClient?.onRequestFocus(view)
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            outerWebChromeClient?.onReceivedIcon(view, icon)
        }

        override fun onReceivedTouchIconUrl(view: WebView, url: String, precomposed: Boolean) {
            outerWebChromeClient?.onReceivedTouchIconUrl(view, url, precomposed)
        }

        override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
            return outerWebChromeClient?.onShowFileChooser(webView, filePathCallback, fileChooserParams) ?: false
        }

        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            return outerWebChromeClient?.onJsAlert(view, url, message, result) ?: false
        }

        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            return outerWebChromeClient?.onJsPrompt(view, url, message, defaultValue, result) ?: false
        }

        override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
            return outerWebChromeClient?.onJsConfirm(view, url, message, result) ?: false
        }

        override fun onJsBeforeUnload(view: WebView, url: String, message: String, result: JsResult): Boolean {
            return outerWebChromeClient?.onJsBeforeUnload(view, url, message, result) ?: false
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            return outerWebChromeClient?.onConsoleMessage(consoleMessage) ?: false
        }


        override fun getDefaultVideoPoster(): Bitmap? {
            return outerWebChromeClient?.defaultVideoPoster
        }

        override fun getVideoLoadingProgressView(): View? {
            return outerWebChromeClient?.videoLoadingProgressView
        }

        override fun getVisitedHistory(callback: ValueCallback<Array<String>>) {
            outerWebChromeClient?.getVisitedHistory(callback)
        }

    }
}
