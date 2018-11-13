package com.qingqing.base.cookie;

import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.dns.HostManager;
import com.qingqing.base.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * 写入 cookie 的方法
 *
 * Created by lihui on 2016/11/4.
 */

public class BaseCookieManager {
    
    private static final String DOMAIN_URL = ".changingedu.com";
    
    public static void setUserCookie(String token, long userID, String session,
            String qingUserID, String secondID) {
        
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", token);
            jsonObject.put("user_id", userID);
            jsonObject.put("session_id", session);
            jsonObject.put("qingqing_user_id", qingUserID);
            jsonObject.put("user_second_id", secondID);
            jsonObject.put("appPlatform", "app");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        String cookie = jsonObject.toString();
        
        setUserCookie(URLEncoder.encode(cookie));
    }
    
    public static void removeUserCookie() {
        setUserCookie("");
    }
    
    private static void setUserCookie(String userCookie) {
        String userType = "";
        
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
            userType = CookieConstant.COOKIE_KEY_USER_TYPE_STUDENT;
        }
        else if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
            userType = CookieConstant.COOKIE_KEY_USER_TYPE_TEACHER;
        }
        else if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
            userType = CookieConstant.COOKIE_KEY_USER_TYPE_TA;
        }
        
        setCookie(userType, userCookie);
    }
    
    public static void setBackupDomainCookie() {
        setBackupDomainCookie(getBackupDomainString());
    }
    
    public static String getBackupDomainString() {
        JSONObject jsonObject = new JSONObject();
        try {
            boolean isBackupEnable = HostManager.INSTANCE().hasBackupDomainEnabled();
            
            jsonObject.put("is_open", isBackupEnable ? 1 : 0);
            
            if (isBackupEnable) {
                JSONObject backupList = new JSONObject();
                Iterator iterator = HostManager.INSTANCE().getHostListMap().entrySet()
                        .iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    
                    HostManager.HostList hostList = (HostManager.HostList) entry
                            .getValue();
                    JSONObject hostBody = new JSONObject();
                    JSONArray hostListArray = new JSONArray();
                    if (hostList.hostList != null && hostList.hostList.size() > 0) {
                        
                        for (String singleHost : hostList.hostList) {
                            hostListArray.put(singleHost);
                            
                        }
                    }
                    hostBody.put("domains", hostListArray);
                    hostBody.put("index", hostList.selIdx);
                    
                    backupList.put((String) entry.getKey(), hostBody);
                }
                
                jsonObject.put("backup_list", backupList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return URLEncoder.encode(jsonObject.toString());
    }
    
    public static void removeBackupDomainCookie() {
        setBackupDomainCookie("");
    }
    
    private static void setBackupDomainCookie(String backupDomainCookie) {
        setCookie(CookieConstant.COOKIE_KEY_BACKUP_DOMAIN, backupDomainCookie);
    }
    
    public static void setCookie(String key, String value) {
        CookieManager cookieManager = null;
        
        try {
            cookieManager = CookieManager.getInstance();
        } catch (Exception e) {
            Logger.w("CookieManager", e);
        }
        
        if (cookieManager == null)
            return;
        
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(BaseApplication.getCtx());
        }
        
        if (!TextUtils.isEmpty(value)) {
            cookieManager.setCookie(DOMAIN_URL, key + "=" + value + "; Expires=0");
        }
        else {
            cookieManager.setCookie(DOMAIN_URL,
                    key + "=; Expires=Wed, 31 Dec 1971 23:59:59 GMT");
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        }
        else {
            CookieSyncManager.getInstance().sync();
        }
    }
}
