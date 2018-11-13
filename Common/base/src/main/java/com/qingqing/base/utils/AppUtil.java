package com.qingqing.base.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.qingqing.base.utils.PackageUtil.getPackageName;

public final class AppUtil {
    
    private static HandlerThread sHandlerThread = null;
    private static Handler mHandler = null;
    private static HashMap<String, UninstallRunnable> mUninstallTaskMap = null;
    
    public static final int INSTALL_NO_ERROR = 0;
    public static final int INSTALL_FAILED_NO_SPACE = -1;
    public static final int INSTALL_FAILED_FILE_BROKEN = -2;
    public static final int INSTALL_FAILED_INVALID_FILE = -3;
    public static final int INSTALL_FAILED_OTHER_ERROR = -100;
    
    public interface OnInstallListener {
        void onInstallBegin(String filePath);
        
        void onInstallOver(String filePath, int retCode);
    }
    
    public interface OnUninstallListener {
        void onUninstallBegin(String packName);
        
        void onUninstallOver(String packName, int code, String errString);
    }
    
    static void prepareSilentActions() {
        sHandlerThread = new HandlerThread("qing app work thread");
        sHandlerThread.start();
        mHandler = new Handler(sHandlerThread.getLooper());
        mUninstallTaskMap = new HashMap<String, UninstallRunnable>();
    }

    public static void silentInstall(String apkAbsolutePath, OnInstallListener l) {
        mHandler.post(new InstallRunnable(apkAbsolutePath, l));
    }

    public static void silentUninstall(String packName, OnUninstallListener l) {

        UninstallRunnable r = new UninstallRunnable(packName, l);
        mUninstallTaskMap.put(packName, r);
        mHandler.post(r);
    }

    public static void cancelAllSilentUninstallTasks() {

        if (mUninstallTaskMap.isEmpty())
            return;

        for (String key : mUninstallTaskMap.keySet()) {
            UninstallRunnable r = mUninstallTaskMap.get(key);
            mHandler.removeCallbacks(r);
        }

        mUninstallTaskMap.clear();
    }

    public static ArrayList<ResolveInfo> getAllApps() {
        ArrayList<ResolveInfo> appInfos = new ArrayList<ResolveInfo>();
        PackageManager pm = UtilsMgr.getCtx().getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appInfos.addAll(pm.queryIntentActivities(mainIntent, 0));
        return appInfos;
    }

    public static boolean isTopActivity(String cpName) {
        ActivityManager am = (ActivityManager) UtilsMgr.getCtx()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && tasks.size() > 0) {
            RunningTaskInfo info = tasks.get(0);
            return !TextUtils.isEmpty(cpName) && info != null
                    && cpName.equals(info.topActivity.toString());
        }
        return false;
    }

    public static boolean isThirdApp(ApplicationInfo appInfo) {

        int appFlags = appInfo.flags;
        if (((appFlags & ApplicationInfo.FLAG_SYSTEM) == 0)
                && !appInfo.packageName.equals(UtilsMgr.getCtx().getPackageName())) {
            return true;
        }
        else
            return false;

    }

    public static boolean isThirdApp(String packageName) {
        try {
            ApplicationInfo info = UtilsMgr.getCtx().getPackageManager().getApplicationInfo(
                    packageName, 0);
            return isThirdApp(info);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<ResolveInfo> getThirdPartyApps() {
        ArrayList<ResolveInfo> appInfos = new ArrayList<ResolveInfo>();
        ArrayList<ResolveInfo> allAppInfos = getAllApps();
        for (ResolveInfo info : allAppInfos) {
            String packageName = info.activityInfo.applicationInfo.packageName;
            if (isThirdApp(packageName)) {
                appInfos.add(info);
            }
        }
        return appInfos;
    }

    public static void launchApp(String packageName) {

        Intent intent = UtilsMgr.getCtx().getPackageManager().getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        UtilsMgr.getCtx().startActivity(intent);
    }

    public static void uninstallApp(String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:"
                + packageName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        UtilsMgr.getCtx().startActivity(intent);
    }

    public static void installApp(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0+以上版本
            Uri apkUri = FileProvider.getUriForFile(UtilsMgr.getCtx(), "com.qingqing.tvboard", new File(path));  //包名.fileprovider
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(path)),
                    "application/vnd.android.package-archive");
        }
        UtilsMgr.getCtx().startActivity(intent);
    }

    /**
     * 跳转到 应用详情页面
     *
     * @return
     */
    public static void showAppDetail(String packageName) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", packageName, null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", PackageUtil.getPackageName());
        }
        UtilsMgr.getCtx().startActivity(localIntent);
    }

    public static void showAppDetail(){
        showAppDetail(getPackageName());
    }

    static class InstallRunnable implements Runnable {

        private OnInstallListener mListener;
        private String mApkPath;

        // private static int __test = 0;

        InstallRunnable(String apkPath, OnInstallListener listener) {
            mListener = listener;
            mApkPath = apkPath;
        }

        @Override
        public void run() {
            // TIP : 用来测试 磁盘不足引起的 安装失败
            // ++__test;
            //
            // if(__test%2 != 0){
            // if (mListener != null) {
            // mListener.onInstallBegin(mApkPath);
            // }
            //
            // try {
            // Thread.sleep(2000);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            //
            // if (mListener != null) {
            // mListener.onInstallOver(mApkPath, INSTALL_FAILED_NO_SPACE);
            // }
            //
            // return;
            // }

            Process process = null;
            String[] args = { "pm", "install", "-r", mApkPath };
            BufferedReader bufferedReader = null;
            BufferedReader bufferedReaderOut = null;
            InputStream errStream = null;
            InputStream outStream = null;
            StringBuilder sb = new StringBuilder();
            StringBuilder sbOut = new StringBuilder();
            try {
                if (mListener != null) {
                    mListener.onInstallBegin(mApkPath);
                }

                process = new ProcessBuilder(args).start();
                process.waitFor();

                errStream = process.getErrorStream();
                bufferedReader = new BufferedReader(new InputStreamReader(errStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                outStream = process.getInputStream();
                bufferedReaderOut = new BufferedReader(new InputStreamReader(outStream));
                while ((line = bufferedReaderOut.readLine()) != null) {
                    sbOut.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    if (mListener != null) {

                        String errString = sb.toString();
                        int retCode = INSTALL_FAILED_OTHER_ERROR;
                        if ("Success".equalsIgnoreCase(sbOut.toString())) {
                            retCode = INSTALL_NO_ERROR;
                        }
                        else if (errString.contains("INSTALL_PARSE_FAILED")
                                || errString.contains("INSTALL_FAILED_INVALID_APK")
                                || errString.contains("INSTALL_PARSE_FAILED_NOT_APK")) {

                            retCode = INSTALL_FAILED_FILE_BROKEN;

                        }
                        else if (errString.contains("INSTALL_FAILED_DEXOPT")
                                || errString
                                        .contains("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {

                            retCode = INSTALL_FAILED_NO_SPACE;

                        }
                        else if (errString.contains("INSTALL_FAILED_INVALID_URI")) {
                            retCode = INSTALL_FAILED_INVALID_FILE;
                        }

                        Log.i("---install result ---", "onInstallOver---" + mApkPath
                                + "---" + process.exitValue() + "---" + sb.toString());

                        mListener.onInstallOver(mApkPath, retCode);
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (errStream != null) {
                        try {
                            errStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (bufferedReaderOut != null) {
                        try {
                            bufferedReaderOut.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (outStream != null) {
                        try {
                            outStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    process.destroy();
                }
            }
        }
    }

    static class UninstallRunnable implements Runnable {

        private OnUninstallListener mListener;
        private String mPackName;

        UninstallRunnable(String packName, OnUninstallListener listener) {
            mListener = listener;
            mPackName = packName;
        }

        @Override
        public void run() {
            Process process = null;
            String[] args = { "pm", "uninstall", mPackName };
            BufferedReader bufferedReader = null;
            InputStream errStream = null;
            StringBuilder sb = new StringBuilder();

            try {
                if (mListener != null) {
                    mListener.onUninstallBegin(mPackName);
                }

                process = new ProcessBuilder(args).start();
                process.waitFor();

                errStream = process.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(errStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    mUninstallTaskMap.remove(mPackName);

                    if (mListener != null) {
                        mListener.onUninstallOver(mPackName, process.exitValue(),
                                sb.toString());
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (errStream != null) {
                        try {
                            errStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    process.destroy();
                }
            }
        }
    }

    public static String getAppTitle(String packageName) {

        PackageInfo info = PackageUtil.getPackageInfo(packageName);
        if (info != null) {
            return info.applicationInfo.loadLabel(UtilsMgr.getCtx().getPackageManager()).toString();
        }
        return null;
    }

    public static Drawable getAppIcon(String packageName) {

        Drawable d = null;

        do {
            PackageInfo packageInfo = PackageUtil.getPackageInfo(packageName);
            if (packageInfo == null)
                break;

            d = getAppIcon(packageInfo, -1);

        } while (false);

        return d;
    }

    public static Drawable getAppIcon(String packageName, int defaulticon) {

        Drawable d = null;

        do {
            PackageInfo packageInfo = PackageUtil.getPackageInfo(packageName);
            if (packageInfo == null)
                break;

            d = getAppIcon(packageInfo, defaulticon);

        } while (false);

        return d;
    }

    public static Drawable getAppIcon(ResolveInfo info, int defaulticon) {
        Resources resources;
        try {
            resources = UtilsMgr.getCtx().getPackageManager().getResourcesForApplication(
                    info.activityInfo.applicationInfo);
        } catch (NameNotFoundException e) {
            resources = null;
        }
        Drawable d = null;

        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                d = resources.getDrawable(iconId);
            }
        }

        if (d == null && defaulticon > 0) {
            d = UtilsMgr.getCtx().getResources().getDrawable(defaulticon);
        }
        return d;

    }

    public static Drawable getAppIcon(PackageInfo info, int iconID) {
        Resources resources;
        try {
            resources = UtilsMgr.getCtx().getPackageManager().getResourcesForApplication(
                    info.applicationInfo);
        } catch (NameNotFoundException e) {
            resources = null;
        }
        Drawable d = null;
        
        if (resources != null) {
            d = info.applicationInfo.loadIcon(UtilsMgr.getCtx().getPackageManager());
        }
        
        if (d == null && iconID > 0) {
            d = UtilsMgr.getCtx().getResources().getDrawable(iconID);
        }
        return d;
    }
    
    /**
     * 判断一个服务是否是开启状态
     *
     * @param serviceName
     *            服务名
     * @return true -开启 false -关闭
     */
    public static boolean isServiceRunning(String serviceName) {
        ActivityManager myManager = (ActivityManager) UtilsMgr.getCtx()
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(Integer.MAX_VALUE);
        if (runningService != null) {
            for (int i = 0; i < runningService.size(); i++) {
                if (runningService.get(i).service.getClassName().toString()
                        .equals(serviceName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static String getProcessName(int pid) {
        ActivityManager am = (ActivityManager) UtilsMgr.getCtx()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am
                .getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
    
    public static boolean isMainProcess(String packageName) {
        boolean result = true;
        String processName = getProcessName(android.os.Process.myPid());
        if (processName != null) {
            result = processName.equals(packageName);
        }
        
        return result;
    }
    
    public static boolean isMainProcess() {
        return isMainProcess(UtilsMgr.getCtx().getPackageName());
    }
    
    public static boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }
    
    /** 获取app是否在前台，兼容5.0+ */
    public static boolean isAppForeground() {
        
        Boolean isForeground = false;
        
        String packageName = PackageUtil.getPackageInfo().packageName;
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager.RunningAppProcessInfo currentInfo = getRunningAppInfoAfterL();
            
            if (currentInfo != null && packageName.equals(currentInfo.processName)) {
                isForeground = true;
            }
        }
        else {
            isForeground = getForegroundPackagesPreL(packageName);
        }
        
        return isForeground;
    }
    
    /** 5.0以下版本获取前台应用 */
    private static boolean getForegroundPackagesPreL(String packageName) {
        ActivityManager am = (ActivityManager) UtilsMgr.getCtx()
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName)
                && currentPackageName.equals(packageName)) {
            return true;
        }
        return false;
    }
    
    /** 5.0以上版本获取前台应用 */
    private static ActivityManager.RunningAppProcessInfo getRunningAppInfoAfterL() {
        final int PROCESS_STATE_TOP = 2;
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class
                    .getDeclaredField("processState");
        } catch (Exception ignored) {}
        ActivityManager am = (ActivityManager) UtilsMgr.getCtx()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                Integer state = null;
                try {
                    state = field.getInt(app);
                } catch (Exception e) {}
                if (state != null && state == PROCESS_STATE_TOP) {
                    currentInfo = app;
                    break;
                }
            }
        }
        return currentInfo;
    }
    
    public static String getAppPlatformInternal() {
        return "tvwall";
//        return "assistant";
    }

    public static int getStatusBarHeight() {
        Resources res = Resources.getSystem();
        int resId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            return res.getDimensionPixelSize(resId);
        }
        return 0;
    }

    public static int getNavigationBarHeight() {
        boolean hasMenuKey = ViewConfiguration.get(UtilsMgr.getCtx()).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        if (!hasMenuKey && !hasBackKey) {
            Resources resources = UtilsMgr.getCtx().getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            //获取NavigationBar的高度
            int height = resources.getDimensionPixelSize(resourceId);
            return height;
        }
        else{
            return 0;
        }
    }

    /** MiUi 6+ 设置状态栏字体颜色
     * @param activity
     * @param darkmode
     * @return
     */
    public static boolean setMiuiStatusBarDarkMode(Activity activity, boolean darkmode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return false;
    }

    /** 魅族Flyme 4+ 设置状态栏字体颜色
     * @param activity
     * @param dark
     * @return
     */
    public static boolean setMeizuStatusBarDarkIcon(Activity activity, boolean dark) {
        boolean result = false;
        if (activity != null) {
            try {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                activity.getWindow().setAttributes(lp);
                result = true;
            } catch (Exception e) {
            }
        }
        return result;
    }


    public static boolean setStatusBarTextDarkOnMarshMallow(Activity activity,boolean isDark) {
        if (Build.VERSION.SDK_INT >= 23) {
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow()
                    .clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            /*SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                Added in API level 23
                Constant Value: 8192 (0x00002000)
                int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                Flag for setSystemUiVisibility(int): Requests the status bar to draw in a mode that is compatible with light status bar backgrounds.
                For this to take effect, the window must request FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS but not FLAG_TRANSLUCENT_STATUS.*/
            int vis = activity.getWindow().getDecorView().getSystemUiVisibility();
            if(isDark){
                vis |= 0x00002000;
            } else{
                vis &= ~0x00002000;
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(vis);

          /*  if (!isDark) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|0x00002000);
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            }*/
            return true;
        }
        return false;
    }

    /** 批量设置一大堆View的可见性
     * @param wantVisible
     * @param views
     */
    public static void setVisibilityVisbleOrGoneForViewsArray(boolean wantVisible, View... views) {
        for (int i = 0,size = views.length ; i < size; i++) {
            if (wantVisible) {
                if (views[i].getVisibility() != View.VISIBLE) {
                    views[i].setVisibility(View.VISIBLE);
                }
            } else {
                if (views[i].getVisibility() != View.GONE) {
                    views[i].setVisibility(View.GONE);
                }
            }
        }

    }

}
