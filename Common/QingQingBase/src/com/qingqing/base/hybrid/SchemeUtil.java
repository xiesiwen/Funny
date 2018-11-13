package com.qingqing.base.hybrid;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by Wangxiaxin on 2016/3/28.
 *
 * Scheme 跳转的 辅助类
 *
 */
public final class SchemeUtil {
    
    public interface SchemeH5InvokeCallback {
        void onH5Invoke(Context ctx, boolean isFromCurrentActivity);
    }
    
    private static final HashMap<String, String> sSchemeParams = new HashMap<>();
    private static SchemeH5InvokeCallback sH5InvokeCallback;
    
    public static void saveSchemeString(String schemeString) {
        String[] schemeArray = schemeString.split("[?]");
        if (schemeArray.length > 1) {
            String scheme = schemeArray[1];
            String[] schemeParamArray = scheme.split("&");
            for (String schemeParam : schemeParamArray) {
                String[] bodyArray = schemeParam.split("=");
                if (bodyArray.length > 1) {
                    saveSchemeParam(bodyArray[0], bodyArray[1]);
                }
                
            }
        }
    }
    
    public static void saveSchemeParam(String key, String value) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            sSchemeParams.put(key, value);
        }
    }
    
    public static void setH5InvokeCallback(SchemeH5InvokeCallback callback) {
        sH5InvokeCallback = callback;
    }
    
    public static void invokeByH5(Context ctx) {
        invokeByH5(ctx, true);
    }
    
    /**
     * 立刻调用 scheme 跳转
     * 
     * @param context
     *            界面上下文
     * @param isFromCurrentActivity
     *            是否由当前页面发起， false 为跳到首页再发起
     */
    public static void invokeByH5(Context context, boolean isFromCurrentActivity) {
        if (sH5InvokeCallback != null) {
            sH5InvokeCallback.onH5Invoke(context, isFromCurrentActivity);
        }
    }
    
    public static String getSchemeType() {
        return sSchemeParams.get("type");
    }
    
    public static HashMap<String, String> getSchemeParamList() {
        return sSchemeParams;
    }
    
    public static String getSchemeParam(String key) {
        return sSchemeParams.get(key);
    }
    
    public static boolean hasScheme() {
        return sSchemeParams.size() != 0;
    }
    
    public static void clear() {
        sSchemeParams.clear();
    }
    
    public static void removeScheme(String s) {
        sSchemeParams.remove(s);
    }
    
    /**
     * scheme 中是否有 unsecure 参数。 此参数用于判断 h5 页面返回时是否需要关闭掉。为 true 时需要 H5 做 goBack
     * 处理
     * 
     */
    public static boolean getUnSecure() {
        boolean unSecure;
        try {
            unSecure = Integer.parseInt(getSchemeParam("unsecure")) == 1;
        } catch (NumberFormatException e) {
            unSecure = false;
        }
        
        return unSecure;
    }
    
    /**
     * scheme 中是否有 need_refresh 参数。 此参数用于判断 h5 页面返回时是否需要刷新。为 true 时需要 H5 做
     * reload 处理
     *
     */
    public static boolean getNeedRefresh() {
        boolean needRefresh;
        try {
            needRefresh = Integer
                    .parseInt(SchemeUtil.getSchemeParam("need_refresh")) == 1;
        } catch (NumberFormatException e) {
            needRefresh = false;
        }
        
        return needRefresh;
    }
    
    /**
     * scheme 中是否有 not_go_back 参数。 此参数用于判断 h5 页面返回时是否不需要返回。为 true 时 H5 不需做
     * goBack 处理
     */
    public static boolean getNotGoBack() {
        boolean needRefresh;
        try {
            needRefresh = Integer.parseInt(SchemeUtil.getSchemeParam("not_go_back")) == 1;
        } catch (NumberFormatException e) {
            needRefresh = false;
        }
        
        return needRefresh;
    }
}
