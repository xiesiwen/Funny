package com.qingqing.base.html.jshandler;

import com.qingqing.qingqingbase.ui.BaseActionBarActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class SetTitleHandler extends AbstractJSHandler {
    
    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.CB_SET_TITLE;
    }
    
    @Override
    public void handleJSMethod(String methodName, String params) {
        try {
            JSONObject p = new JSONObject(params);
            final String title = p.getString("title");
            if (couldOperateUI()) {
                if (getActivity() instanceof BaseActionBarActivity) {
                    ((BaseActionBarActivity) getActivity()).setActionBarTitle(title);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
