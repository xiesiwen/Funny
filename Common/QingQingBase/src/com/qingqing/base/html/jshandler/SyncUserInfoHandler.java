package com.qingqing.base.html.jshandler;

import com.qingqing.base.core.AccountManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class SyncUserInfoHandler extends AbstractJSHandler{

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.METHOD_SYNC_USERINFO;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        try {
            JSONObject jsonObject = new JSONObject(params);
            String userName = jsonObject.optString("username");
            String token = jsonObject.optString("token");
            String sessionId = jsonObject.optString("session_id");
            String qingqingUserId = jsonObject.optString("qingqing_user_id");
            String userSecondId = jsonObject.optString("user_second_id");
            long userId = jsonObject.optLong("user_id");

            AccountManager.INSTANCE().syncUserInfoFromH5(userName, userId, token,
                    sessionId, qingqingUserId, userSecondId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
