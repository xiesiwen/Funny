package com.qingqing.base.html;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.cookie.BaseCookieManager;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.dns.DomainConfig;
import com.qingqing.base.dns.HostManager;
import com.qingqing.base.html.jshandler.AbstractJSHandler;
import com.qingqing.base.html.jshandler.BaseJSHandler;
import com.qingqing.base.html.jshandler.JSHandlerDispatcher;
import com.qingqing.base.html.jshandler.MenuManager;
import com.qingqing.base.hybrid.JSManager;
import com.qingqing.base.hybrid.JSManager.JSCallback;
import com.qingqing.base.hybrid.SchemeUtil;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.DeviceUtil;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.utils.UrlUtil;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.html.BaseJSWebView;
import com.qingqing.qingqingbase.R;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.qingqingbase.ui.BaseFragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class HtmlFragment extends BaseFragment {
    public static final int MSG_LOAD_BEGIN = 1;
    public static final int MSG_LOAD_ERROR = 2;
    public static final int MSG_LOAD_DONE = 3;
    public static final int MSG_LOAD_TIME_OUT = 4;
    
    private static final String TAG = "BaseHtmlFragment";
    private static final String APP_CACHE_DIRNAME = "/webcache";
    private static final String KEY_HIDE_TITLE_BAR = "/hideNativeBar";
    
    private static String sDefaultUserAgent;
    protected BaseJSWebView mWebView;
    protected String mMainUrl;
    
    protected ArrayList<JSManager.JsAsyncMethodCallback> mAsyncMethodCallbackList;
    JSManager.JsAsyncCallbackListener mAsyncCallbackListener = new JSManager.JsAsyncCallbackListener() {
        @Override
        public void onCallbackFunction(String method, String param) {
            callJsMethod(method, param);
        }
    };
    
    private boolean mLoadFail;
    private ProgressBar mProgressBar;
    private View mViewLoading;
    private View mViewRetry;
    private View mBtnRetry;
    public boolean mIsOurSite;
    private int mDefaultShareIcon;
    private AnimationDrawable mAnimLoading;
    public boolean mForceQuit;
    private int mCityId = -1;
    public String mReloadUrl;
    private int mReloadCount = 0;
    private boolean mNeedCheckSessionStorage = true;
    private boolean mInvokeSchemeFromContext = false;
    /**
     * js 回调管理用的唯一 id
     */
    private String mHtmlFragmentId;
    public boolean mForceShowShareMenu;
    
    protected JSHandlerDispatcher mJSHandlerDispatcher;
    public MenuManager mMenuManager;
    protected Menu mMenu;
    
    protected void callJsMethod(final String method, final String... param) {
        if (TextUtils.isEmpty(method)) {
            return;
        }
        
        String paramString = "";
        if (param != null) {
            for (int i = 0; i < param.length; i++) {
                paramString += param[i];
                
                if (i < param.length - 1) {
                    paramString += ",";
                }
            }
        }
        
        loadJavaScript(method + "(" + "'" + paramString + "'" + ")");
    }
    
    protected void setSessionStorage(String key, String value) {
        loadJavaScript("sessionStorage.setItem(\'" + key + "\', \'" + value + "\'); ");
    }
    
    protected void loadJavaScript(final String javaScriptString) {
        if (couldOperateUI() && mWebView != null) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    if (couldOperateUI() && mWebView != null) {
                        mWebView.loadUrl("javascript:" + javaScriptString);
                    }
                }
            });
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (DeviceUtil.getSDKInt() < 17) {
            mNeedCheckSessionStorage = false;
            BaseCookieManager.setBackupDomainCookie();
        }
        setCustomCookie();
        mJSHandlerDispatcher = new JSHandlerDispatcher();
    }
    
    protected void setCustomCookie() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_whole_webview, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle param = getArguments();
        if (param == null) {
            param = savedInstanceState;
        }
        
        mHtmlFragmentId = UUID.randomUUID().toString();
        
        String url = "";
        if (param != null) {
            url = param.getString(BaseParamKeys.PARAM_STRING_URL);
            mCityId = param.getInt(BaseParamKeys.PARAM_INT_CITY_ID, -1);
            mDefaultShareIcon = param.getInt(BaseParamKeys.PARAM_INT_DEFAULT_SAHRE_ICON,
                    0);
            mInvokeSchemeFromContext = param.getBoolean(
                    BaseParamKeys.PARAM_BOOLEAN_INVOKE_SCHEME_FROM_CONTEXT, false);
            getParams(param);
        }
        initWebView(view);
        setWebClient();
        setChromeClient();
        setWebCache();
        addDefaultJSHandlers();
        JSManager.INSTANCE.addAsyncCallbackListener(mAsyncCallbackListener);
        
        loadUrl(url);
        if (getActivity() instanceof BaseActionBarActivity) {
            ((BaseActionBarActivity) getActivity()).setActionBarTitle("");
        }
        addDefaultJSHandlers();
        mJSHandlerDispatcher.initHandlerOnViewCreated(mWebView, this);
    }
    
    private void addDefaultJSHandlers() {
        addCustomCallback();
    }
    
    protected void addCustomCallback() {
        
    }
    
    public void loadUrl(String url) {
        String host = UrlUtil.getHost(url);
        
        if (HostManager.INSTANCE().hasHostList(host)
                && HostManager.INSTANCE().hasBackupDomainEnabled(host)) {
            HostManager.INSTANCE().allUseBackupHost();
            if (DeviceUtil.getSDKInt() < 17) {
                mNeedCheckSessionStorage = false;
                BaseCookieManager.setBackupDomainCookie();
            }
            
            url = UrlUtil.setHost(url, HostManager.INSTANCE().currentHost(host));
        }
        
        mMainUrl = url;
        if (!TextUtils.isEmpty(mMainUrl) && !mMainUrl.startsWith("http")) {
            mMainUrl = (DefaultDataCache.INSTANCE().isApiHttps()
                    ? getString(R.string.url_header_https)
                    : getString(R.string.url_header_http)) + "://" + mMainUrl;
        }
        
        if (mCityId != -1) {
            mMainUrl = UrlUtil.addParamToUrl(mMainUrl, BaseParamKeys.PARAM_INT_CITY_ID,
                    String.valueOf(mCityId));
        }
        mMainUrl = UrlUtil.addParamToUrl(mMainUrl, "ts",
                String.valueOf(System.currentTimeMillis()));
        mReloadUrl = mMainUrl;
        
        Logger.o(TAG, "main url=" + mMainUrl);
        mWebView.loadUrl(mMainUrl);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMenuManager.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BaseParamKeys.PARAM_STRING_URL, mMainUrl);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.loadUrl("");
        }
    }
    
    @Override
    public void onDestroy() {
        JSManager.INSTANCE.clearCallback(mHtmlFragmentId);
        JSManager.INSTANCE.removeAsyncCallbackListener(mAsyncCallbackListener);
        
        if (mAsyncMethodCallbackList != null) {
            mAsyncMethodCallbackList.clear();
        }
        mAsyncMethodCallbackList = null;
        
        BaseCookieManager.removeBackupDomainCookie();
        super.onDestroy();
    }
    
    /**
     * 设置web view的webViewClient
     */
    public void setWebClient() {
        mWebView.setWebViewClient(new DefaultClient());
    }
    
    /**
     * 设置web view的chromeClient
     */
    public void setChromeClient() {
        mWebView.setWebChromeClient(new DefaultWebChromeClient());
    }
    
    /**
     * 从argument中获取参数
     */
    public void getParams(Bundle param) {}
    
    private void initWebView(View parent) {
        mWebView = (BaseJSWebView) parent.findViewById(R.id.html_view);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        setSoftLayer();
        setDefaultUserAgent();
        mProgressBar = (ProgressBar) parent.findViewById(R.id.pb_progress);
        mViewLoading = parent.findViewById(R.id.iv_loading);
        mViewLoading.setBackgroundResource(R.drawable.loading_progress);
        mAnimLoading = (AnimationDrawable) mViewLoading.getBackground();
        mViewRetry = parent.findViewById(R.id.layout_retry);
        mBtnRetry = parent.findViewById(R.id.tv_retry);
        mBtnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.reload();
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                    String contentDisposition, String mimetype, long contentLength) {
                Logger.o(TAG,
                        "start download: [ua]=" + userAgent + "  [content]="
                                + contentDisposition + " [mime]=" + mimetype
                                + "  [length]=" + contentLength + "  [url]=" + url);
                if (getActivity() != null) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(intent); // Fabric #2270
                    } catch (SecurityException e) {
                        Logger.d("Permission Denial"); // Permission Denial:
                                                       // starting
                                                       // Intent
                        // { act=android.intent.action.VIEW
                        // dat=https://1251606527.vod2.myqcloud.com/...
                        // cmp=com.miui.video/com.miui.videoplayer.OfflineVideoPlayerActivity
                        // }
                        // from ProcessRecord{1c25e7f
                        // 15771:com.qingqing.teacher/u0a362}
                        // (pid=15771, uid=10362) not exported from uid 10072
                    }
                }
            }
        });
        mWebView.addJavascriptInterface(new JsCallbackInternal(), "QQJSExternal");
        if (DeviceUtil.getSDKInt() >= 17) {
            mWebView.addJavascriptInterface(new SessionStorageTest(),
                    "androidSessionStorageTest");
        }
    }
    
    private void setSoftLayer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    
    // since 5.0 统一内部WebView的 UserAgent
    private void setDefaultUserAgent() {
        if (TextUtils.isEmpty(sDefaultUserAgent)) {
            // teacher/AndroidPhone/4.9.5 (Linux; U; Android 4.4.2;
            // HONOR H30-L01 Build/HonorH30-L01)
            sDefaultUserAgent = BaseApplication.getAppNameInternal()
                    + String.format("/%s/%s(%s)", AppUtil.getAppPlatformInternal(),
                            PackageUtil.getVersionName(), DeviceUtil.getDefaultUA());
        }
        
        mWebView.getSettings().setUserAgentString(sDefaultUserAgent);
    }
    
    private void setWebCache() {
        // 开启DOM storage API 功能
        mWebView.getSettings().setDomStorageEnabled(true);
        // 开启database storage API功能
        mWebView.getSettings().setDatabaseEnabled(true);
        String cacheDirPath = getActivity().getCacheDir().getAbsolutePath()
                + APP_CACHE_DIRNAME;
        // 设置数据库缓存路径
        mWebView.getSettings().setDatabasePath(cacheDirPath); // API 19
        // deprecated
        // 设置Application caches缓存目录
        mWebView.getSettings().setAppCachePath(cacheDirPath);
        // 开启Application Cache功能
        mWebView.getSettings().setAppCacheEnabled(true);
    }
    
    public void reload() {
        if (couldOperateUI()) {
            mWebView.reload();
        }
    }
    
    /**
     * 增加注册JS回调
     * 
     * @deprecated Use {@link #addCallback(BaseJSHandler)} instead.
     */
    @Deprecated
    public void addCallback(final JSCallback cb, String... cbkey) {
        for (final String key : cbkey) {
            mJSHandlerDispatcher.putJSHandler(new AbstractJSHandler() {
                @Override
                public String getJSMethodName() {
                    return key;
                }
                
                @Override
                public void handleJSMethod(String methodName, String params) {
                    cb.onCallback(methodName, params);
                }
            });
        }
    }
    
    /**
     * 在onViewCreated之前调用
     */
    public void addCallback(BaseJSHandler baseJSHandler) {
        mJSHandlerDispatcher.putJSHandler(baseJSHandler);
    }
    
    private void invalidOptionMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (couldOperateUI())
                getActivity().invalidateOptionsMenu();
        }
    }
    
    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg, String acceptType,
            String capture) {}
    
    @TargetApi(21)
    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg,
            WebChromeClient.FileChooserParams fileChooserParams) {
        String acceptType = null;
        boolean supportMultiple = false;
        if (fileChooserParams != null && fileChooserParams.getAcceptTypes() != null
                && fileChooserParams.getAcceptTypes().length > 0) {
            acceptType = fileChooserParams.getAcceptTypes()[0];
            if (fileChooserParams
                    .getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                supportMultiple = true;
            }
        }
        
    }
    
    public boolean canGoBack() {
        return mWebView != null && mWebView.canGoBack();
    }
    
    /**
     * 判断是否需要隐藏title bar
     */
    private boolean shouldHideTitleBar(URL url) {
        // #后面第一个/ 后面的string 是否是 KEY_HIDE_TITLE_BAR
        boolean ret = false;
        
        if (!mIsOurSite)
            return false;
        
        String ref = url.getRef();
        if (!TextUtils.isEmpty(ref)) {
            int firstIdx = ref.indexOf("/");
            if (firstIdx >= 0) {
                if (ref.substring(firstIdx).startsWith(KEY_HIDE_TITLE_BAR))
                    ret = true;
            }
        }
        
        return ret;
    }
    
    @Override
    public boolean onBackPressed() {
        if (!mForceQuit && canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onBackPressed();
    }
    
    private void syncWebUrl() {
        String url = mWebView.getUrl();
        if (TextUtils.isEmpty(url)) {
            Logger.w(TAG, "syncWebUrl  NULL");
            return;
        }
        try {
            URL _url = new URL(url);
            String host = _url.getHost();
            mIsOurSite = host.endsWith(DomainConfig.ROOT);
            if (getActivity() instanceof BaseActionBarActivity) {
                if (shouldHideTitleBar(_url)) {
                    ((BaseActionBarActivity) getActivity()).hideActionBar();
                }
                else {
                    ((BaseActionBarActivity) getActivity()).showActionBar();
                }
            }
            
            setHardwareAccelerator(url);
        } catch (Exception e) {
            Logger.w(TAG, "syncWebUrl exception: url = " + url, e);
            mIsOurSite = false;
        }
    }
    
    /**
     * 根据要求开启硬件加速
     */
    private void setHardwareAccelerator(String url) {
        try {
            String hardware = Uri.parse(url).getQueryParameter("hardware");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                    && !TextUtils.isEmpty(hardware) && "1".equals(hardware)) {
                mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
        } catch (Exception e2) {
            Logger.w(e2);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}
    
    @Override
    public boolean onHandlerUIMsg(Message msg) {
        if (!couldOperateUI()) {
            return true;
        }
        
        switch (msg.what) {
            case MSG_LOAD_BEGIN:
                syncWebUrl();
                mProgressBar.setProgress(1);
                mProgressBar.setVisibility(View.VISIBLE);
                mViewLoading.setVisibility(View.VISIBLE);
                mAnimLoading.start();
                mViewRetry.setVisibility(View.INVISIBLE);
                mWebView.setVisibility(View.INVISIBLE);
                break;
            case MSG_LOAD_DONE:
                // 如果不是我们的域名，则默认可以分享
                syncWebUrl();
                
                if (!mIsOurSite) {
                    mForceShowShareMenu = true;
                    invalidOptionMenu();
                }
                
                mProgressBar.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                mViewRetry.setVisibility(View.INVISIBLE);
                mAnimLoading.stop();
                mViewLoading.setVisibility(View.INVISIBLE);
                
                break;
            case MSG_LOAD_ERROR:
                // load error，则隐藏分享
                mForceShowShareMenu = false;
                invalidOptionMenu();
                
                mViewRetry.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mWebView.setVisibility(View.INVISIBLE);
                mAnimLoading.stop();
                mViewLoading.setVisibility(View.INVISIBLE);
                break;
            case MSG_LOAD_TIME_OUT:
                if (mReloadCount < DefaultDataCache.INSTANCE()
                        .getHttpRequestRetryCount()) {
                    mReloadCount++;
                    mWebView.loadUrl(mReloadUrl);
                }
                else {
                    String host = UrlUtil.getHost(mReloadUrl);
                    
                    if (HostManager.INSTANCE().hasHostList(host)
                            && !HostManager.INSTANCE().hasBackupDomainEnabled(host)) {
                        HostManager.INSTANCE().allUseBackupHost();
                        if (DeviceUtil.getSDKInt() < 17) {
                            mNeedCheckSessionStorage = false;
                            BaseCookieManager.setBackupDomainCookie();
                        }
                        
                        String url = UrlUtil.setHost(mReloadUrl,
                                HostManager.INSTANCE().currentHost(host));
                        mWebView.loadUrl(url);
                    }
                    else {
                        sendEmptyMessage(MSG_LOAD_ERROR);
                    }
                }
                break;
        }
        return super.onHandlerUIMsg(msg);
    }
    
    protected void addMethodCallback(String method, String param) {
        JSManager.JsAsyncMethodCallback methodCallback = getMethodCallback(method, param);
        if (methodCallback != null && !TextUtils.isEmpty(methodCallback.id)) {
            if (mAsyncMethodCallbackList == null) {
                mAsyncMethodCallbackList = new ArrayList<>();
            }
            mAsyncMethodCallbackList.add(methodCallback);
        }
    }
    
    protected JSManager.JsAsyncMethodCallback getMethodCallback(String method,
            String param) {
        try {
            JSONObject jsonObject = new JSONObject(param);
            JSONObject callback = jsonObject.optJSONObject("callbacks");
            
            if (callback != null) {
                JSManager.JsAsyncMethodCallback methodCallback = JSManager.INSTANCE.new JsAsyncMethodCallback();
                
                methodCallback.method = method;
                methodCallback.id = callback.optString("id");
                methodCallback.fail = callback.optString("fail");
                methodCallback.success = callback.optString("success");
                methodCallback.cancel = callback.optString("cancel");
                
                if (!TextUtils.isEmpty(methodCallback.id)) {
                    return methodCallback;
                }
                else {
                    return null;
                }
            }
        } catch (JSONException ignored) {}
        
        return null;
    }
    
    protected JSManager.JsAsyncMethodCallback getAsyncMethodCallback(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        
        if (mAsyncMethodCallbackList == null || mAsyncMethodCallbackList.size() <= 0) {
            return null;
        }
        for (JSManager.JsAsyncMethodCallback callback : mAsyncMethodCallbackList) {
            if (callback != null && callback.id.equals(id)) {
                return callback;
            }
        }
        return null;
    }
    
    private void loadPageCache(String url) {
        if (mMenuManager != null) {
            mMenuManager.loadPageCache(url);
        }
    }
    
    private String getOriginUrl(String url) {
        String result = url;
        try {
            URL urlObject = new URL(url);
            String query = urlObject.getQuery();
            if (query != null) {
                String[] queryList = query.split("&");
                query = "";
                for (int i = 0; i < queryList.length; i++) {
                    if (!queryList[i].contains("ts=")) {
                        query += (queryList[i] + "&");
                    }
                }
                
                if (query.endsWith("&")) {
                    query = query.substring(0, query.length() - 1);
                }
            }
            
            result = urlObject.getProtocol() + "://" + urlObject.getHost()
                    + (urlObject.getPort() < 0 ? "" : ":" + urlObject.getPort())
                    + (TextUtils.isEmpty(urlObject.getPath()) ? ""
                            : "/" + urlObject.getPath())
                    + (TextUtils.isEmpty(query) ? "" : "?" + query)
                    + ((TextUtils.isEmpty(urlObject.getRef())
                            || "/".equals(urlObject.getRef())) ? ""
                                    : "#" + urlObject.getRef());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private void checkSessionStorage() {
        loadJavaScript("var androidHasSessionStorage = (function() {\n" + "\ttry {\n"
                + "\t\tsessionStorage.setItem(\"test\", \"test\");\n"
                + "\t\tsessionStorage.removeItem(\"test\");\n" + "\t\treturn true;\n"
                + "\t} catch (exception) {\n" + "\t\treturn false;\n" + "\t}\n"
                + "}()); window.androidSessionStorageTest.result(androidHasSessionStorage);");
        
    }
    
    @Override
    @CallSuper
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        mMenuManager = new MenuManager(menu, mJSHandlerDispatcher);
    }
    
    @Override
    @CallSuper
    public void onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        super.onPrepareOptionsMenu(menu);
    }
    
    public interface BaseHtmlFragListener extends FragListener {
        boolean onOverrideUrlLoading(WebView view, String url);
    }
    
    private class JsCallbackInternal {
        @android.webkit.JavascriptInterface
        public void qqJSCallBackWithContentwithMethodName(final String param,
                final String methodName) {
            JSManager.INSTANCE.qqJSCallBackWithContentWithMethodName(mHtmlFragmentId,
                    param, methodName);
            mJSHandlerDispatcher.handleJSMethod(methodName, param);
        }
        
        @android.webkit.JavascriptInterface
        public String qqJSAsyncGetContentcallBack(String param, String callback) {
            return JSManager.INSTANCE.qqJSAsyncGetContentCallBack(param, callback);
        }
        
        @android.webkit.JavascriptInterface
        public String qqJSCallBackGetContent(String param) {
            return JSManager.INSTANCE.qqJSCallBackGetContent(param);
        }
    }
    
    public class DefaultClient extends WebViewClient {
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            
            Logger.d(TAG, "shouldOverrideUrlLoading  url=" + url);
            
            boolean ret = false;
            if (mFragListener != null && mFragListener instanceof BaseHtmlFragListener) {
                ret = ((BaseHtmlFragListener) mFragListener).onOverrideUrlLoading(view,
                        url);
            }
            
            if (!ret) {
                if (url.startsWith("tel:")) {
                    String telNum = url.substring(4);
                    DeviceUtil.makeCall(telNum);
                    ret = true;
                }
                else if (PackageUtil.getPackageName().equals(UrlUtil.getProtocol(url))
                        && "h5context".equals(UrlUtil.getHost(url))) {
                    String data = url.substring(
                            (PackageUtil.getPackageName() + "://").length(),
                            url.length());
                    Logger.d(TAG, "----override--scheme---" + url + "--data=" + data);
                    SchemeUtil.saveSchemeString(data);
                    if (SchemeUtil.hasScheme())
                        SchemeUtil.invokeByH5(getActivity(), mInvokeSchemeFromContext);
                    ret = true;
                }
            }
            
            return ret || url.contains("getBack.do")
                    || super.shouldOverrideUrlLoading(view, url);
        }
        
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                SslError error) {
            // 如果是证书的问题，忽略
            Logger.w(TAG, "onReceivedSslError  " + error);
            try {
                URL url = new URL(error.getUrl());
                if (url.getHost().equalsIgnoreCase("qingjy.cn")
                        || url.getHost().endsWith(DomainConfig.ROOT)) {
                    handler.proceed();
                    return;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            
            Logger.w(TAG, "onReceivedSslError  cancel");
            handler.cancel();
        }
        
        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            Logger.w(TAG, "onReceivedError, errcode=" + errorCode + "  url=" + failingUrl
                    + "  des=" + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
            mLoadFail = true;
            removeMessages(MSG_LOAD_TIME_OUT);
            sendEmptyMessage(MSG_LOAD_ERROR);
        }
        
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Logger.o(TAG, "onPageStarted  url=" + url);
            mLoadFail = false;
            if (!url.equals(mReloadUrl)) {
                mReloadCount = 0;
            }
            mReloadUrl = url;
            super.onPageStarted(view, url, favicon);
            sendEmptyMessage(MSG_LOAD_BEGIN);
            sendEmptyMessageDelayed(MSG_LOAD_TIME_OUT,
                    DefaultDataCache.INSTANCE().getHttpRequestTimeoutSeconds() * 1000);
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
            Logger.o(TAG, "onPageFinished  url=" + url);
            super.onPageFinished(view, url);
            mReloadUrl = url;
            removeMessages(MSG_LOAD_TIME_OUT);
            if (!mLoadFail) {
                sendEmptyMessage(MSG_LOAD_DONE);
            }
            
            loadPageCache(getOriginUrl(url));
        }
        
    }
    
    public class DefaultWebChromeClient extends WebChromeClient {
        @Override
        public Bitmap getDefaultVideoPoster() {
            Bitmap bitmap = super.getDefaultVideoPoster();
            
            return bitmap;
        }
        
        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                final JsResult result) {
            
            if (couldOperateUI()) {}
            
            return true;
        }
        
        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                final JsResult result) {
            
            if (couldOperateUI()) {
                TypedValue outValue = new TypedValue();
                int theme = outValue.resourceId;
            }
            
            return true;
        }
        
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            
            final ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
            final String tag = consoleMessage.sourceId();
            final String msg = consoleMessage.message();
            final int line = consoleMessage.lineNumber();
            final String format = "H5---line[%d]---%s";
            
            switch (level) {
                case DEBUG:
                    Logger.d(tag, String.format(format, line, msg));
                    break;
                case ERROR:
                    Logger.e(tag, String.format(format, line, msg));
                    break;
                case LOG:
                default:
                    Logger.o(tag, String.format(format, line, msg));
                    break;
                case WARNING:
                    Logger.w(tag, String.format(format, line, msg));
                    break;
            }
            
            return super.onConsoleMessage(consoleMessage);
        }
        
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Logger.d(TAG, "onProgressChanged  newProgress=" + newProgress);
            mProgressBar.setProgress(newProgress);
            if (newProgress > 5 && mNeedCheckSessionStorage) {
                mNeedCheckSessionStorage = false;
                checkSessionStorage();
            }
        }
        
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
        
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }
        
        @Override
        public void onReceivedTouchIconUrl(WebView view, String url,
                boolean precomposed) {
            super.onReceivedTouchIconUrl(view, url, precomposed);
        }
        
        // 扩展浏览器上传文件
        // 3.0++版本
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooserImpl(uploadMsg, acceptType, null);
        }
        
        // 3.0--版本
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooserImpl(uploadMsg, "image/*", null);
        }
        
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType,
                String capture) {
            openFileChooserImpl(uploadMsg, acceptType, capture);
        }
        
        // > 5.0
        @Override
        public boolean onShowFileChooser(WebView webView,
                ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams) {
            openFileChooserImplForAndroid5(filePathCallback, fileChooserParams);
            return true;
        }
    }
    
    private class SessionStorageTest {
        @JavascriptInterface
        public void result(String paramFromJS) {
            boolean supportStorage = Boolean.parseBoolean(paramFromJS);
            if (supportStorage) {
                setSessionStorage("bk_domain", BaseCookieManager.getBackupDomainString());
            }
            else {
                BaseCookieManager.setBackupDomainCookie();
            }
        }
    }
}
