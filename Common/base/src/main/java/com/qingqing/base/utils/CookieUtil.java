package com.qingqing.base.utils;

import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * Created by wangxiaxin on 2017/5/31.
 *
 * Cookie 辅助类
 */
public class CookieUtil {
    
    private static final String DEFAULT_DOMAIN_URL = ".changingedu.com";
    private static String sDefaultDomainUrl = DEFAULT_DOMAIN_URL;
    
    public static boolean setCookie(String key, String value) {
        return setCookie(sDefaultDomainUrl, key, value);
    }
    
    /**
     * 设置默认的 域名配置
     */
    public static void setDefaultDomain(String domain) {
        sDefaultDomainUrl = domain;
    }
    
    public static boolean setCookie(String url, String key, String value) {
        
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(key))
            return false;
        
        CookieManager cookieManager = CookieManager.getInstance();
        if (DeviceUtil.getSDKInt() < android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(UtilsMgr.getCtx());
        }
        
        if (!TextUtils.isEmpty(value)) {
            cookieManager.setCookie(url, key + "=" + value);
        }
        else {
            cookieManager.setCookie(url,
                    key + "=; Expires=Wed, 31 Dec 1971 23:59:59 GMT");
        }
        
        if (DeviceUtil.getSDKInt() >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        }
        else {
            CookieSyncManager.getInstance().sync();
        }
        
        return true;
    }
}
