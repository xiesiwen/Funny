package com.qingqing.base.html.jshandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class ShowToolbarRightItemsHandler extends ShowShareHandler {
    
    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.METHOD_SHOW_TOOLBAR_RIGHT_ITEMS;
    }
    
    @Override
    public void handleJSMethod(String methodName, String params) {
        try {
            JSONArray jsonArray = new JSONArray(params);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                
                showToolbarItem(methodName, jsonObject);
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void showToolbarItem(String method, JSONObject jsonObject) {
        int type = jsonObject.optInt("type");
        switch (type) {
            case 1:
                // 分享
                mHtmlFragment.mMenuManager.showMenuShareItem(jsonObject.optString("paramDict"));
                break;
            case 2:
                // 跳转
                mHtmlFragment.mMenuManager.showMenuTextItem(jsonObject.optString("paramDict"));
                break;
            case 3:
                // 触发回调
                mHtmlFragment.mMenuManager.showMenuCallbackItem(method, jsonObject.optString("paramDict"));
                break;
            default:
                break;
        }
    }
}
