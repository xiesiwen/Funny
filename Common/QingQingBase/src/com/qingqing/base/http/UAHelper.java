package com.qingqing.base.http;

import android.text.TextUtils;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.DeviceUtil;
import com.qingqing.base.utils.PackageUtil;

/**
 * Created by wangxiaxin on 2017/3/6.
 *
 * 用于定义HTTP请求中的 user-agent
 */
public final class UAHelper {
    
    private static final String DATA_UA_PREFIX = "%s-%s-%s ";
    
    private static String sDataUAPrefix;
    private static String sDownloadUAPrefix;
    private static String sImgUAPrefix;
    private static String sUAContent;
    
    private static void checkUAPrefix() {
        if (TextUtils.isEmpty(sDataUAPrefix)) {
            sDataUAPrefix = String.format(DATA_UA_PREFIX, BaseApplication.getAppNameInternal(),
                    AppUtil.getAppPlatformInternal(), PackageUtil.getVersionName());
        }
        
        if (TextUtils.isEmpty(sImgUAPrefix)) {
            sImgUAPrefix = sDataUAPrefix + "img ";
        }
        if (TextUtils.isEmpty(sDownloadUAPrefix)) {
            sDownloadUAPrefix = sDataUAPrefix + "dl ";
        }
    }
    
    private static void checkUAContent() {
        checkUAPrefix();
        if (TextUtils.isEmpty(sUAContent)) {
            okhttp3.Request.Builder reqBuilder = new okhttp3.Request.Builder();
            try {
                reqBuilder.header("demoUA", DeviceUtil.getDefaultUA());
                sUAContent = DeviceUtil.getDefaultUA();
            } catch (Exception e) {
                sUAContent = DeviceUtil.getDefaultIllegalUA();
            }
        }
    }
    
    public static String dataUA() {
        checkUAContent();
        return sDataUAPrefix + sUAContent;
    }
    
    public static String imgUA() {
        checkUAContent();
        return sImgUAPrefix + sUAContent;
    }

    public static String downloadUA() {
        checkUAContent();
        return sDownloadUAPrefix + sUAContent;
    }
}
