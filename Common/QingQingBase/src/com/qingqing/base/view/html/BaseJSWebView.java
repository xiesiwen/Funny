package com.qingqing.base.view.html;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.log.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class BaseJSWebView extends WebView {
    public BaseJSWebView(Context context) {
        super(context);
        init();
    }
    
    public BaseJSWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public BaseJSWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        
        boolean needCacheOpt = false;
//        DefaultDataCache.INSTANCE().getAppType() == AppCommon.AppType.qingqing_ta?
//                DefaultDataCache.INSTANCE().needH5CacheOptimizeTa(): DefaultDataCache.INSTANCE().needH5CacheOptimize();
        if (!needCacheOpt) {
            clearCache(true);
            clearFormData();
        }
        getSettings().setCacheMode(
                needCacheOpt ? WebSettings.LOAD_DEFAULT : WebSettings.LOAD_NO_CACHE);// 去除webview的本地缓存
        getSettings().setJavaScriptEnabled(true);// 默认支持JS
    }
    
    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
        Logger.v("BaseJSWebView", "load url=" + url);
    }
    
    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }
}
