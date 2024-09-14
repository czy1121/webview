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
    implementation "me.reezy.cosmo:webview-jsbridge:0.9.0"
    implementation "me.reezy.cosmo:webview-bundle:0.9.0"
    implementation "me.reezy.cosmo:webview-simple:0.9.0"
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

- 资源包，将资源包解压到缓存目录(`files/web-bundle`)，再从中加载资源
  - 离线资源包，直接打包到APK里
  - 动态资源包，需要动态预加载
- 离线资源，直接从`assets`目录加载资源
- HTTP缓存，使用Okhttp的代替WebView的缓存管理


假设有资源包 `BundleItem(id = "bundle1", uri = "https://cdn.a.com/bundle1-0.1.0.zip", baseUrl = "http://a.com/bundle1/")`   
要加载资源 `http://a.com/bundle1/js/test.js`
 
- 会先偿试从资源包加载 `files/web-bundle/bundle1/js/test.js`
- 然后偿试从 `assets` 目录加载 `web-asset/a.com/bundle1/js/test.js`
- 之后偿试用 `okhttp` 加载
- 最后从网络加载


使用 

```kotlin   

// 初始化
WebBundleManager.init(this)

// 加载资源包
WebBundleManager.loadBundles(mapOf(
    // 加载离线包，离线包直接打包到APK里，放在`assets`目录，
    BundleItem(id = "local", uri = "asset://local-0.1.0.zip", baseUrl = "http://a.com/local/"),
    // 加载动态资源包，可覆盖离线包和离线资源，服务端动态下发，可预加载 
    BundleItem(id = "remote", uri = "https://a.com/remote-0.1.0.zip", baseUrl = "http://a.com/remote/"),
)) 

// 使用Okhttp的代替WebView的请求给定路径下的资源，使用Okhttp的缓存管理
WebBundleManager.addCacheUrls("http://example.com/a/", "http://example.com/b/")


// 拦截资源的请求并返回缓存资源
web.webViewClient = object : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return WebBundleManager.get(request)
    }
}
``` 

 



## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).
