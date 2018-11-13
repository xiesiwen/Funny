package com.qingqing.base.html.jshandler;

import com.qingqing.base.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class LogHandler extends AbstractJSHandler{

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.CB_LOG;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        new LogRun(params);
    }

    private class LogRun implements Runnable {

        private String mParam;

        LogRun(String param) {
            mParam = param;
        }

        @Override
        public void run() {
            try {
                JSONObject jb = new JSONObject(mParam);
                final String level = jb.optString("lv", "i");
                final String tag = jb.optString("tag", "default");
                final String msg = jb.optString("msg", "empty");

                final String format = "[H5]---%s";

                if ("d".equals(level)) {
                    Logger.d(tag, String.format(format, msg));
                }
                else if ("i".equals(level)) {
                    Logger.o(tag, String.format(format, msg));
                }
                else if ("w".equals(level)) {
                    Logger.w(tag, String.format(format, msg));
                }
                else if ("e".equals(level)) {
                    Logger.e(tag, String.format(format, msg));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
