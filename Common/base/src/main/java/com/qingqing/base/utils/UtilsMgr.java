package com.qingqing.base.utils;

import android.content.Context;

/**
 * 在使用任何其他Utilsxxx 之前，必须首先调用此类的 init 方法
 */
public final class UtilsMgr {
    
    private static Context sCtx;
    
    public static void init(Context ctx) {
        sCtx = ctx;
        DisplayUtil.init(sCtx);
    }

    public static Context getCtx() {
        return sCtx;
    }
    
    /** 如果需要用到UtilsApp中的静默安装卸载方法，需要调用此接口 */
    public static void allowInstallation() {
        
        if (PermissionUtil.hasInstallPermission())
            AppUtil.prepareSilentActions();
    }
}
