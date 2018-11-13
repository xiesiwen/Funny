package com.qingqing.base.html.jshandler;

import com.qingqing.base.dns.DNSManager;
import com.qingqing.base.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class ReportHostHandler extends AbstractJSHandler{

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.CB_REPORT_HOST;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        Logger.o("--H5--", "hostUse : " + params);
        try {
            JSONObject jo = new JSONObject(params);
            String host = jo.getString("host");
            boolean ret = jo.getBoolean("ret");
            Logger.o("--H5--", "host : " + host + "  ret=" + ret);
            if (ret) {
                DNSManager.INSTANCE().notifyHostOK(host);
            }
            else {
                DNSManager.INSTANCE().notifyHostTimeout(host);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
