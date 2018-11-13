package com.qingqing.base.html.jshandler;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.qingqing.base.view.ToastWrapper;
import com.qingqing.qingqingbase.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class CopyLinkHandler extends AbstractJSHandler{

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.CB_COPY_LINK;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        String link = "";
        try {
            JSONObject jsonObject = new JSONObject(params);
            link = jsonObject.optString("link");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            ClipboardManager cmb = (ClipboardManager) getActivity()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setPrimaryClip(
                    ClipData.newPlainText(null, URLDecoder.decode(link)));
            ToastWrapper.show(R.string.tip_html_copy_link);
        } catch (Exception e) {

        }
    }
}
