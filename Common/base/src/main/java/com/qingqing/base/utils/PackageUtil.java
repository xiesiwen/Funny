package com.qingqing.base.utils;

import java.io.File;
import java.util.ArrayList;


import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

/**
 * 获取包信息工具类
 */
public final class PackageUtil {
    
    private static PackageInfo sSelfPackageInfo;
    
    /**
     * 获取版本名称
     */
    public static String getVersionName() {
        PackageInfo packInfo = getPackageInfo();
        if (packInfo != null) {
            return packInfo.versionName;
        }
        return "";
    }
    
    /**
     * 获取版本号
     */
    public static int getVersionCode() {
        PackageInfo packInfo = getPackageInfo();
        if (packInfo != null) {
            return packInfo.versionCode;
        }
        else
            return 0;
    }
    
    /**
     * 获取版本号
     */
    public static String getPackageName() {
        PackageInfo packInfo = getPackageInfo();
        if (packInfo != null) {
            return packInfo.packageName;
        }
        else
            return "";
    }
    
    /**
     * 获取 应用名称
     */
    public static String getLabel() {
        PackageInfo packInfo = getPackageInfo();
        if (packInfo != null) {
            return packInfo.applicationInfo
                    .loadLabel(UtilsMgr.getCtx().getPackageManager()).toString();
        }
        else
            return "";
    }
    
    public static PackageInfo getPackageInfo() {
        
        if (sSelfPackageInfo == null) {
            sSelfPackageInfo = getPackageInfo(UtilsMgr.getCtx().getPackageName());
        }
        
        return sSelfPackageInfo;
    }
    
    /**
     * 根据包名直接返回PackageInfo
     * 
     * @param packageName
     *            指定的包名 指定包名
     */
    public static PackageInfo getPackageInfo(String packageName) {
        try {
            return UtilsMgr.getCtx().getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String getPackagerVersion(String packageName) {
        String lsv = "";
        PackageInfo packInfo = PackageUtil.getPackageInfo(packageName);
        if (packInfo != null) {
            lsv = packInfo.versionName;
        }
        return lsv;
    }
    
    /**
     * 根据应用名获取包名
     * 
     * @param apkFileName
     *            根据文件名获取包名
     * @return string
     */
    public static String getPackageNameFromFile(String apkFileName) {
        PackageManager packageManager = UtilsMgr.getCtx().getPackageManager();
        PackageInfo info = packageManager.getPackageArchiveInfo(apkFileName,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        String packageName = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            packageName = appInfo.packageName;
        }
        return packageName;
    }
    
    public static File getPackageSourceFile(String packageName) {
        
        File sourceFile = null;
        
        do {
            PackageInfo packageInfo = getPackageInfo(packageName);
            if (packageInfo == null)
                break;
            sourceFile = new File(packageInfo.applicationInfo.publicSourceDir);
        } while (false);
        
        return sourceFile;
    }
    
    public static long getPackageSourceFileSize(String packageName) {
        
        long size = 0;
        
        do {
            File sourceFile = getPackageSourceFile(packageName);
            if (sourceFile == null)
                break;
            
            size = sourceFile.length();
            
        } while (false);
        
        return size;
    }
    
    public static boolean isPackageInstalled(String packageName) {
        return isPackageInstalled(packageName, null);
    }
    
    public static boolean isPackageInstalled(String packName, String verName) {
        
        boolean ret = false;
        do {
            PackageInfo packInfo = getPackageInfo(packName);
            if (packInfo == null)
                break;
            
            ret = TextUtils.isEmpty(verName) || packInfo.versionName.equals(verName);
        } while (false);
        
        return ret;
    }
    
    public static String getMetaData(String key) {
        try {
            ApplicationInfo info = UtilsMgr.getCtx().getPackageManager()
                    .getApplicationInfo(UtilsMgr.getCtx().getPackageName(),
                            PackageManager.GET_META_DATA);
            Object value = info.metaData.get(key);
            if (value != null) {
                String valueString = value.toString();
                // 这里兼容处理long型数字的问题
                if (!TextUtils.isEmpty(valueString)) {
                    if ((valueString.endsWith("L") || valueString.endsWith("l"))
                            && valueString.substring(0, valueString.length() - 1)
                                    .matches("[0-9]+")) {
                        return valueString.substring(0, valueString.length() - 1);
                    }
                }
                return valueString;
            }
        } catch (Exception e) {}
        return "";
    }
    
    public static String getChannel() {
        return "qq";
    }
    
    public static boolean isSelfLuancherActivity(String className) {
        ArrayList<ResolveInfo> list = findSelfLauncherActivities();
        for (ResolveInfo info : list) {
            if (className.equals(info.activityInfo.name))
                return true;
        }
        return false;
    }
    
    public static ArrayList<ResolveInfo> findSelfLauncherActivities() {
        return findLauncherActivitiesForPackage(UtilsMgr.getCtx().getPackageName());
    }
    
    public static ArrayList<ResolveInfo> findLauncherActivitiesForPackage(
            String packageName) {
        
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        
        return findActivities(mainIntent);
    }
    
    public static ArrayList<ResolveInfo> findActivities(Intent searchIntent) {
        return findActivities(searchIntent, false);
    }
    
    public static ArrayList<ResolveInfo> findActivities(Intent searchIntent,
            boolean isDefault) {
        ArrayList<ResolveInfo> matches = new ArrayList<ResolveInfo>();
        final PackageManager packageManager = UtilsMgr.getCtx().getPackageManager();
        matches.addAll(packageManager.queryIntentActivities(searchIntent,
                isDefault ? PackageManager.MATCH_DEFAULT_ONLY : 0));
        return matches;
    }
    
    public static boolean hasActivity(Intent searchIntent, boolean isDefault) {
        ArrayList<ResolveInfo> retList = findActivities(searchIntent, isDefault);
        return retList != null && !retList.isEmpty();
    }
    
    /**
     * 判断是否是debug包
     */
    public static boolean isApkDebuggable() {
        return isApkDebuggable(UtilsMgr.getCtx().getPackageName());
    }
    
    /**
     * 判断是否是debug包
     */
    public static boolean isApkDebuggable(String packageName) {
        try {
            PackageInfo pkgInfo = UtilsMgr.getCtx().getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (pkgInfo != null) {
                ApplicationInfo info = pkgInfo.applicationInfo;
                return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            }
            
        } catch (Exception e) {
            
        }
        return false;
    }
}
