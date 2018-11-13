package com.qingqing.base.html.jshandler;

import com.qingqing.base.html.HtmlFragment;
import com.qingqing.base.hybrid.JSManager;
import com.qingqing.base.view.html.BaseJSWebView;

import android.app.Dialog;
import android.graphics.Bitmap;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class SnapshotHandler extends AbstractJSHandler{
    private Bitmap mShotBitmap;
    private Bitmap mSampleSizeBitmap;
    private Bitmap mThumbBitmap;
    private Dialog mDialog;
    private static final float THUMB_HEIGHT = 300f;
    private JSManager.JsAsyncMethodCallback mMethodCallback;
    
    @Override
    public void initHandlerOnViewCreated(BaseJSWebView webView,
            HtmlFragment htmlFragment) {
        super.initHandlerOnViewCreated(webView, htmlFragment);
    }
    
    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.METHOD_SNAPSHOT;
    }
    
    @Override
    public void handleJSMethod(String methodName, String params) {
        mMethodCallback = getMethodCallback(methodName, params);
    }
    
    public Bitmap getViewBitmap() {
        mWebView.setDrawingCacheEnabled(true);
        mWebView.buildDrawingCache();
        mWebView.invalidate();
        Bitmap b = Bitmap.createBitmap(mWebView.getDrawingCache(), 0, 0,
                mWebView.getMeasuredWidth(), mWebView.getMeasuredHeight());
        mWebView.setDrawingCacheEnabled(false);
        mWebView.destroyDrawingCache();
        return b;
    }
    
}
