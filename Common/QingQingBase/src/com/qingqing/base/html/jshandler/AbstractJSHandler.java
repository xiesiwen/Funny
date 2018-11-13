package com.qingqing.base.html.jshandler;

import org.json.JSONException;
import org.json.JSONObject;

import com.qingqing.base.html.HtmlFragment;
import com.qingqing.base.hybrid.JSManager;
import com.qingqing.base.view.html.BaseJSWebView;

import android.app.Activity;
import android.text.TextUtils;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public abstract class AbstractJSHandler implements BaseJSHandler{
    BaseJSWebView mWebView;
    HtmlFragment mHtmlFragment;

    @Override
    public void initHandlerOnViewCreated(BaseJSWebView webView, HtmlFragment htmlFragment) {
        mWebView = webView;
        mHtmlFragment = htmlFragment;
    }

    public boolean couldOperateUI(){
        if (mHtmlFragment != null) {
            return mHtmlFragment.couldOperateUI();
        }
        return false;
    }

    public Activity getActivity(){
        if (mHtmlFragment != null) {
            return mHtmlFragment.getActivity();
        }
        return null;
    }

    public HtmlFragment getHtmlFragment(){
        return mHtmlFragment;
    }

    public BaseJSWebView getBaseJSWebView(){
        return mWebView;
    }

    public void callJsMethod(final String method, final String... param) {
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

    @Override
    public void onDetach() {

    }
}
