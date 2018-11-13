package com.qingqing.base.utils;

import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.PermissionChecker;

public final class PermissionUtil {
    
    /** 是否有安装权限 */
    public static boolean hasInstallPermission() {
        return hasPermission("android.permission.INSTALL_PACKAGES");
    }
    
    /** 是否有联网权限 */
    public static boolean hasInternetPermission() {
        return hasPermission("android.permission.INTERNET");
    }
    
    /** 是否有定位权限 */
    public static boolean hasLocationPermission() {
        return hasPermission("android.permission.ACCESS_FINE_LOCATION");
    }
    
    /** 是否有拨打电话的权限 */
    public static boolean hasCallPhonePermission() {
        return hasPermission("android.permission.CALL_PHONE")
                && hasPermission("android.permission.PROCESS_OUTGOING_CALLS");
    }
    
    public static boolean hasWriteSettingPermission() {
        return hasPermission("android.permission.WRITE_SETTINGS");
    }

    public static boolean hasRecordPermission() {
        return hasPermission("android.permission.RECORD_AUDIO");
    }

    public static boolean hasWriteContactPermission() {
        return hasPermission("android.permission.WRITE_CONTACTS");
    }
    public static boolean hasReadContactPermission() {
        return hasPermission("android.permission.READ_CONTACTS");
    }

    public static boolean hasPermission(String permissionName) {
        try {
            int permission = PermissionChecker.checkSelfPermission(UtilsMgr.getCtx(), permissionName);
            return PermissionChecker.PERMISSION_GRANTED == permission;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean hasPermission(String packageName, String permissionName) {
        return UtilsMgr.getCtx().getPackageManager().checkPermission(permissionName, packageName) >= 0;
    }

    /**
     * 获取是否有通知栏权限
     * @return >= 19 才能正常工作，19以前会返回true
     */
    public static boolean checkNotificationPermission() {
        return NotificationManagerCompat.from(UtilsMgr.getCtx()).areNotificationsEnabled();
    }
}
