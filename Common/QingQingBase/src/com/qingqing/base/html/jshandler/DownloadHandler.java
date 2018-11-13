package com.qingqing.base.html.jshandler;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class DownloadHandler extends AbstractJSHandler {
    
    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.METHOD_DOWNLOAD;
    }
    
    @Override
    public void handleJSMethod(String methodName, String params) {
        try {
            JSONObject jsonObject = new JSONObject(params);
            String url = jsonObject.optString("url");
            
            if (!TextUtils.isEmpty(url) && couldOperateUI()) {
                Uri uri = Uri.parse(url);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setDataAndType(uri, "text/html");
                browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                mHtmlFragment.startActivity(browserIntent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (android.content.ActivityNotFoundException ex) {
            // 未能打开浏览器
            ex.printStackTrace();
        }
    }
}
