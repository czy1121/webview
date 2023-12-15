# WebView
 
- webview-jsbridge - 简单易用的 WebView 和 Javascript 交互框架。
- webview-bundle - WebView 资源包管理，提升网页打开速度。
- webview-simple - 一个简单封装的 WebView

## Gradle

``` groovy
repositories { 
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
} 
dependencies {
    implementation "me.reezy.cosmo:webview-jsbridge:0.8.0"
    implementation "me.reezy.cosmo:webview-bundle:0.8.0"
    implementation "me.reezy.cosmo:webview-simple:0.8.0"
}
```

## webview-jsbridge

简单易用的 Android WebView 和 Javascript 交互框架。

```kotlin   
class MainActivity : AppCompatActivity(R.layout.layout_web) {

    private val bridge = JSBridge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) 

        // 添加模块
        bridge.addModule(TestModule()) 

        // 注入 WebView
        bridge.injectBridge(web)


        web.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                logE("onPageStarted => $url")
                // 注入模块
                bridge.injectModules()
                progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String?) {
                logE("onPageFinished => $url")
                // 注入模块
                bridge.injectModules()
                progress.visibility = View.GONE
            }

        }
    }
}
``` 


## webview-bundle

WebView 资源包管理，提升网页打开速度。
 
在 WebView 请求网络资源前，它会先依次查询以下资源，没有命中时才从网络加载。

- 资源包，将资源包解压到缓存目录(`files/webview-bundle`)，再从中加载资源
  - 离线资源包，直接打包到APK里
  - 动态资源包，需要动态预加载
- 离线资源，直接从`assets`目录加载资源
- HTTP缓存，使用Okhttp的代替WebView的缓存管理


加载资源 `http://domain.com/path/to/page.html`
 
- 会先偿试从缓存目录加载 `files/webview-bundle/domain.com/path/to/page.html`
- 然后偿试从`assets`目录加载 `assets/webview-asset/domain.com/path/to/page.html`
- 之后偿试用 `okhttp` 加载
- 最后从网络加载


使用 

```kotlin   

// 初始化
WebBundleManager.init(this) 

// 加载离线包，离线包直接打包到APK里，放在`assets`目录， 
// 资源包加载时被解压到缓存目录("webview-bundle")  
WebBundleManager.loadBundles("local1.zip", "local2.zip") 

// 加载动态资源包，可覆盖离线包和离线资源，服务端动态下发，在使用前预加载 
// 从给定链接下载资源资源包并解压到缓存目录("webview-bundle")  
val remoteBundles = mapOf(
  "http://a.com/remote.zip" to "hash-xxxxxx"
)
WebBundleManager.loadBundles(remoteBundles)


// 修改离线资源根目录 
WebBundleManager.setAssetRootDir("webview-asset")


// 使用Okhttp的代替WebView的请求给定路径下的资源，使用Okhttp的缓存管理
WebBundleManager.addCacheUrls("http://example.com/a/", "http://example.com/b/")


// 拦截资源的请求并返回缓存资源
web.webViewClient = object : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return WebCacheManager.get(request)
    }
}
``` 

 



## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).
