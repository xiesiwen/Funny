package com.qingqing.base;

import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.SPManager;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.utils.UtilsMgr;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.qingqingbase.ui.crash.GlobalCrashHandler;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.support.annotation.StringRes;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;


/**
 * @author richie.wang
 * @createTime 2015年7月7日
 * @summary
 */
public class BaseApplication extends Application {
    
    private static final String TAG = "Tinker.BaseApplication";
    private static final String LAST_VERSION = "last_version";
    protected static Context sCtx;
    private static OnPageStatisticListener mOnPageStatisticListener = null;

    private static final String ADHOC_LICENCE = "ADHOC_70d28365-9bb2-4905-a4aa-6b343908d247";

    public static boolean isTinkerEnable() {
        return sIsTinkerEnable;
    }

    public static void setTinkerEnable(boolean sIsTinkerEnable) {
        BaseApplication.sIsTinkerEnable = sIsTinkerEnable;
    }

    private static boolean sIsTinkerEnable = false;
    private static boolean sIsAppFirstRun = false;
    private static boolean sIsCurrentVersionFirstRun = false;
    private static boolean sIsFreshStart = false;// 是否是新的启动

    public static Context getCtx() {
        return sCtx;
    }

    /**
     * 判断是否是新的一次启动，此方法中会重置标记，所以此方法只是第一次调用有效
     */
    public static boolean isFreshStart() {
        final boolean ret = sIsFreshStart;
        sIsFreshStart = false;
        return ret;
    }
    
    public static OnPageStatisticListener getOnPageStatisticListener() {
        return mOnPageStatisticListener;
    }
    
    public static void setOnPageStatisticListener(
            OnPageStatisticListener onPageStatisticListener) {
        mOnPageStatisticListener = onPageStatisticListener;
    }

    /**
     * install multiDex before install tinker so we don't need to put the tinker
     * lib classes in the main dex
     *
     * @param base
     */
    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sCtx = this;
        // you must install multiDex whatever tinker is installed!
        GlobalCrashHandler.INSTANCE.init();
        MultiDex.install(base);
        UtilsMgr.init(sCtx);// 工具类初始化
        if (AppUtil.isMainProcess()) {
            SPManager.init(null);
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        sIsFreshStart = true;
        Logger.o(TAG,"onCreate");

        if (AppUtil.isMainProcess()) {
            DefaultDataCache.INSTANCE().loadDataFromDB();
            setTheme(getApplicationInfo().theme);
        }
    }

    private String getWeiboRedirectUrl() {
        return "https://api.weibo.com/oauth2/default.html";
    }

    private String getWeiboScope() {
        return "email,follow_app_official_microblog";
    }


    @Override
    public void onTrimMemory(int level) {
        Logger.w(TAG,"onTrimMemory -- " + level);
        super.onTrimMemory(level);
        try {
            if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) { // 60
//                ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
            }
        } catch (Exception e) {
            Logger.w(TAG,"onTrimMemory ",e);
        }
    }

    @Override
    public void onLowMemory() {
        Logger.w(TAG,"onLowMemory");
        super.onLowMemory();
    }

    /**
     * 判断一下版本信息
     */
    private static void determineVersion() {
        final String lastVersion = SPManager.getString(LAST_VERSION);
        sIsAppFirstRun = TextUtils.isEmpty(lastVersion);

        final String currentVersion = PackageUtil.getVersionName();
        boolean ret = !currentVersion.equals(lastVersion);
        if (ret) {
            sIsCurrentVersionFirstRun = true;
            SPManager.put(LAST_VERSION, currentVersion);
        }else{
            sIsCurrentVersionFirstRun = false;
        }
    }

    /**
     * app 是否 第一次运行
     */
    public static boolean isAppFirstRun() {
        return sIsAppFirstRun;
    }
    
    /**
     * 当前版本是否 第一次 运行
     */
    public static boolean isCurrentVersionFirstRun() {
        return sIsCurrentVersionFirstRun;
    }

    public static void quitUI(@StringRes int stringRes){
        if(UIUtil.doubleClickQuitApp()){
            determineVersion();
        }else{
            ToastWrapper.show(stringRes);
        }
    }

    public static String getAppNameInternal() {
        String appname = "assistant";
        return appname;
    }

    public interface OnPageStatisticListener {
        void onResume(Fragment fragment);
        
        void onPause(Fragment fragment);
        
        void onResume(AppCompatActivity activity);
        
        void onPause(AppCompatActivity activity);
    }
}
