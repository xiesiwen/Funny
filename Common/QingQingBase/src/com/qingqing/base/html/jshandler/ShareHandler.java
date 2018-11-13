package com.qingqing.base.html.jshandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qingqing.base.html.HtmlFragment;
import com.qingqing.base.hybrid.JSManager;
import com.qingqing.base.view.html.BaseJSWebView;

/**
 * 点击页面中的分享按钮，与点右上角的分享不同
 * Created by xiejingwen on 2017/11/9.
 */

public class ShareHandler extends AbstractJSHandler{
    private int mDefaultShareIcon;
    @Override
    public void initHandlerOnViewCreated(BaseJSWebView webView, HtmlFragment htmlFragment) {
        super.initHandlerOnViewCreated(webView, htmlFragment);
    }

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.CB_SHARE;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        try {
            JSONObject jsonObject = new JSONObject(params);
            String pageId = jsonObject.optString("pageId");
            String chnid = jsonObject.optString("chnid");
            String title = jsonObject.optString("title");
            String content = jsonObject.optString("content");
            String link = jsonObject.optString("link");
            String icon = jsonObject.optString("icon");
            JSONArray type = jsonObject.optJSONArray("type");
            JSONObject callback = jsonObject.optJSONObject("callbacks");

            JSManager.JsAsyncMethodCallback shareCallback = null;
            if (callback != null) {
                shareCallback = JSManager.INSTANCE.new JsAsyncMethodCallback();

                shareCallback.fail = callback.optString("fail");
                shareCallback.success = callback.optString("success");
                shareCallback.cancel = callback.optString("cancel");
            }

            shareWithParams(pageId, chnid, title, content, link, icon, type,
                    shareCallback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据指定参数弹出分享
     */
    private void shareWithParams(String pageId, String chnid, String title,
                                 String content, String link, String icon, JSONArray type,
                                 JSManager.JsAsyncMethodCallback callback) {
    }


}
