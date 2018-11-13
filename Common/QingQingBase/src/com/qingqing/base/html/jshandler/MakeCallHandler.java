package com.qingqing.base.html.jshandler;

import com.qingqing.base.utils.DeviceUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class MakeCallHandler extends AbstractJSHandler{

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.CB_MAKE_CALL;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        JSONObject json = null;
        try {
            json = new JSONObject(params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json != null) {
            DeviceUtil.makeCall(json.optString("phone_number"));
        }
    }
}
