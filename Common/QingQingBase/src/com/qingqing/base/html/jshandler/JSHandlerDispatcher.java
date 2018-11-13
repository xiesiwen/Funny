package com.qingqing.base.html.jshandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.qingqing.base.html.HtmlFragment;
import com.qingqing.base.hybrid.JSManager;
import com.qingqing.base.view.html.BaseJSWebView;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class JSHandlerDispatcher extends AbstractJSHandler {
    public static final String CB_GOBACK = "getBack";
    public static final String CB_POPALL = "popAll";
    public static final String CB_LOG = "log";
    public static final String CB_REPORT_HOST = "hostUse";
    public static final String CB_SET_TITLE = "setTitle";
    public static final String CB_MAKE_CALL = "call";
    public static final String CB_SHARE = "share";
    public static final String CB_COPY_LINK = "copyLink";
    public static final String METHOD_SHOW_SHARE = "registShareBtn";
    public static final String METHOD_HIDE_SHARE = "unregistShareBtn";
    public static final String METHOD_DOWNLOAD = "download";
    public static final String METHOD_SHOW_MENU_TEXT = "registRightBar";
    public static final String METHOD_HIDE_MENU_TEXT = "unregistRightBar";
    public static final String METHOD_SHOW_TOOLBAR_RIGHT_ITEMS = "registRightItems";
    public static final String METHOD_SYNC_USERINFO = "syncUserInfo";
    public static final String METHOD_SHOW_GALLERY = "showgallery";
    public static final String METHOD_SNAPSHOT = "snapshot";
    private static final String TAG = "JSHandlerDispatcher";
    private Map<String, BaseJSHandler> mJSHandlerMap;
    protected ArrayList<JSManager.JsAsyncMethodCallback> mAsyncMethodCallbackList;
    public JSHandlerDispatcher(){
        mJSHandlerMap = new HashMap<>();
    }

    @Override
    public void initHandlerOnViewCreated(BaseJSWebView webView, HtmlFragment htmlFragment) {
        super.initHandlerOnViewCreated(webView, htmlFragment);
        Set<String> keySet = mJSHandlerMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            BaseJSHandler jsHandler = mJSHandlerMap.get(iterator.next());
            if (jsHandler != null) {
                jsHandler.initHandlerOnViewCreated(webView, htmlFragment);
            }
        }
    }

    @Override
    public String getJSMethodName() {
        return null;
    }

    @Override
    public void handleJSMethod(final String methodName, final String params) {
        Log.d(TAG, "handle method is " + methodName + " params is " + params);
        Set<String> keySet = mJSHandlerMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.equalsIgnoreCase(methodName)){
                final BaseJSHandler jsHandler = mJSHandlerMap.get(key);
                if (jsHandler != null) {
                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            jsHandler.handleJSMethod(methodName, params);
                        }
                    });
                }
                break;
            }
        }
    }

    public void putJSHandler(BaseJSHandler jsHandler){
        putJSHandler(jsHandler.getJSMethodName(), jsHandler);
    }

    public void putJSHandler(String key, BaseJSHandler jsHandler){
        mJSHandlerMap.put(key,jsHandler);
    }

    public void removeJSHandler(BaseJSHandler jsHandler){
        removeJSHandler(jsHandler.getJSMethodName());
    }

    public void removeJSHandler(String key){
        mJSHandlerMap.remove(key);
    }

    public BaseJSHandler getJSHandler(String key){
        return mJSHandlerMap.get(key);
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

    @Override
    public void onDetach() {
        Set<String> keySet = mJSHandlerMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            BaseJSHandler jsHandler = mJSHandlerMap.get(iterator.next());
            if (jsHandler != null) {
                jsHandler.onDetach();
            }
        }
    }
}
