package com.qingqing.base.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import com.qingqing.base.R;
import com.qingqing.base.data.PropertiesWrapper;
import com.qingqing.base.log.Logger;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
 */
public final class DeviceUtil {
    
    private static final String TAG_RANDOM_UUID = "changing_uuid_origin";
    private static final String TAG_IDENTIFICATION = "changing_identification_origin";
    
    public static int getSDKVerInt() {
        return Build.VERSION.SDK_INT;
    }
    
    public static String getSDKVersion() {
        return Build.VERSION.SDK;
    }
    
    public static boolean isSDCardExists() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
    
    /**
     * 判断是否为手机
     *
     * @return
     */
    public static boolean isPhone() {
        // TelephonyManager telephony = (TelephonyManager) mCtx
        // .getSystemService(Context.TELEPHONY_SERVICE);
        // int type = telephony.getPhoneType();
        // if (type == TelephonyManager.PHONE_TYPE_NONE) {
        // return false;
        // }
        // else {
        // return true;
        // }
        
        // 4.9.0 开始，使用屏幕是否大于7寸来判断
        return !DisplayUtil.isScreenPadSize();
    }
    
    /**
     * 判断手机是否处理睡眠
     *
     * @param context
     * @return
     */
    public static boolean isSleeping(Context context) {
        KeyguardManager kgMgr = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);
        boolean isSleeping = kgMgr.inKeyguardRestrictedInputMode();
        return isSleeping;
    }
    
    /**
     * 获取android id
     */
    public static String getAndroidID(String defaultValue) {
        String val = Settings.Secure.getString(UtilsMgr.getCtx().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        if (TextUtils.isEmpty(val)) {
            val = defaultValue;
        }

        return val;
    }

    /**
     * 获取android id
     */
    public static String getAndroidID() {
        return getAndroidID("");
    }

    /**
     * 获取device id
     */
    public static String getDeviceID(String defaultValue) {
        String val = ((TelephonyManager) UtilsMgr.getCtx().getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();

        if (TextUtils.isEmpty(val)) {
            val = defaultValue;
        }

        return val;
    }

    public static String getDeviceID() {
        return getDeviceID("");
    }

    public static int getSDKInt() {
        return Build.VERSION.SDK_INT;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean isAdbEnableLevel17() {
        int nRet = Settings.Secure.getInt(UtilsMgr.getCtx().getContentResolver(),
                Settings.Global.ADB_ENABLED, 0);
        return nRet > 0;
    }

    @SuppressWarnings("deprecation")
    private static boolean isAdbEnableLevelDeprecated() {
        int nRet = Settings.Secure.getInt(UtilsMgr.getCtx().getContentResolver(),
                Settings.Secure.ADB_ENABLED, 0);
        return nRet > 0;
    }

    /**
     * 获得手机是否已经启用adb调试选项
     */
    public static boolean isAdbEnabled(Context context) {
        if (getSDKInt() >= 17)
            return isAdbEnableLevel17();
        else
            return isAdbEnableLevelDeprecated();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAllowedUnknownSourceLevel17() {
        int nRet = Settings.Secure.getInt(UtilsMgr.getCtx().getContentResolver(),
                Settings.Global.INSTALL_NON_MARKET_APPS, 0);
        return nRet == 1;
    }

    @SuppressWarnings("deprecation")
    public static boolean isAllowedUnknownSourceLevelDown() {
        int nRet = Settings.Secure.getInt(UtilsMgr.getCtx().getContentResolver(),
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        return nRet == 1;
    }

    /**
     * 获得手机是否打开了“允许未知来源”的apk安装选项
     */
    public static boolean isAllowedUnknownSource() {
        if (getSDKInt() >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            return isAllowedUnknownSourceLevel17();
        else
            return isAllowedUnknownSourceLevelDown();
    }

    /**
     * 生成随机UUID
     */
    private static void generateRandomUUID() {
        ContentResolver resolver = UtilsMgr.getCtx().getContentResolver();
        String uuid = UUID.randomUUID().toString();
        if (Settings.System.getString(resolver, TAG_RANDOM_UUID) == null)
            Settings.System.putString(resolver, TAG_RANDOM_UUID, uuid);
    }

    /**
     * 获取随机UUID
     */
    public static String getRandomUUID() {
        ContentResolver resolver = UtilsMgr.getCtx().getContentResolver();
        String val = Settings.System.getString(resolver, TAG_RANDOM_UUID);
        if (TextUtils.isEmpty(val)) {
            generateRandomUUID();
            val = Settings.System.getString(resolver, TAG_RANDOM_UUID);
        }

        return val;
    }

    /**
     * 生成设备标识
     */
    private static String generateIdentification() {
        generateRandomUUID();
        String s = getDeviceID();
        String s1 = getAndroidID();
        String s2 = getDeviceSerial();
        String s3 = NetworkUtil.getMacAddress();
        String s4 = s + s1 + s2 + s3;
        if (TextUtils.isEmpty(s4)) {
            s4 = getRandomUUID();
        }
        s4 = MD5Util.encode(s4);
        return s4;
    }

    /**
     * 获取设备标识
     **/
    public static String getIdentification() {
        ContentResolver resolver = UtilsMgr.getCtx().getContentResolver();
        String val = null;

        try {
            val = Settings.System.getString(resolver, TAG_IDENTIFICATION);
            if (TextUtils.isEmpty(val)) {
                val = generateIdentification();
                if (!TextUtils.isEmpty(val)) {
                    Settings.System.putString(resolver, TAG_IDENTIFICATION, val);
                }
            }
        } catch (Exception e) {
            Logger.w("getIdentification", e);
        }

        if (TextUtils.isEmpty(val)) {
            val = "default";
        }
        return val;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String getDeviceSerialLevel9() {
        return Build.SERIAL;
    }

    public static String getDeviceSerialLevelDown() {
        String s;

        try {
            Method method = Class.forName("android.os.Build")
                    .getDeclaredMethod("getString", String.class);
            if (!method.isAccessible())
                method.setAccessible(true);
            s = (String) method.invoke(new Build(), "ro.serialno");
        } catch (ClassNotFoundException classnotfoundexception) {
            classnotfoundexception.printStackTrace();
            return "";
        } catch (NoSuchMethodException nosuchmethodexception) {
            nosuchmethodexception.printStackTrace();
            return "";
        } catch (InvocationTargetException invocationtargetexception) {
            invocationtargetexception.printStackTrace();
            return "";
        } catch (IllegalAccessException illegalaccessexception) {
            illegalaccessexception.printStackTrace();
            return "";
        }
        return s;
    }

    public static String getDeviceSerial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return getDeviceSerialLevel9();
        }
        else {
            return getDeviceSerialLevelDown();
        }
    }

    public static String getBuildVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 手机号码
     */
    public static String getLine1Number() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) UtilsMgr.getCtx()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getLine1Number();
        } catch (Exception e) {
            Log.w("network", e.toString(), e);
        }
        return "";
    }

    /**
     * SIM 卡的 IMSI
     * */
    public static String getIMSI() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) UtilsMgr.getCtx()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getSubscriberId();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static String getDefaultUA() {
        String userAgent = System.getProperty("http.agent");
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = "default";
        }

        return userAgent;
    }

    public static String getDefaultIllegalUA() {
        return "illegal agent";
    }

    /**
     * 4.9.5之后，大部分接口不再返回电话号码，打电话时需调用接口获取电话号码之后再进行拨打。 确定当前有电话号码时才可使用本方法。
     * <p/>
     * 使用 ChatManager.callContact() 替代
     *
     * @param phoneNum
     *            电话号码
     *            
     *            @return  返回错误信息，null表示无错
     */
    public static String makeCall(String phoneNum) {

        // 判断当前是否有拨打电话的权限
        if (!PermissionUtil.hasCallPhonePermission()) {
            Logger.w("当前没有拨打电话的权限");
            return UtilsMgr.getCtx().getString(R.string.base_no_call_permission);
        }

        if (TextUtils.isEmpty(phoneNum)) {
            return UtilsMgr.getCtx().getString(R.string.base_empty_call_number_default);
        }
        Intent intent;
        // 在API 19及以上，CALL_PHONE权限被禁止后，在某些机型上无任何提示。
        // 跳转到拨打电话电话界面
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNum));
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                UtilsMgr.getCtx().startActivity(intent);
                return null;
            } catch (ActivityNotFoundException e) {
                Logger.w(e);
                return UtilsMgr.getCtx().getString(R.string.not_support_call_tips);
            } catch (Exception e) {
                Logger.w(e);
                return UtilsMgr.getCtx().getString(R.string.base_call_unknown_error);
            }
        }
        else {
            try {
                intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                UtilsMgr.getCtx().startActivity(intent);
                return null;
            } catch (SecurityException e) {
                Logger.w("make call  security exception", e);
                return UtilsMgr.getCtx().getString(R.string.base_no_call_permission);
            } catch (ActivityNotFoundException e) {
                Logger.w(e);
                return UtilsMgr.getCtx().getString(R.string.not_support_call_tips);
            } catch (Exception e) {
                Logger.w("make call exception", e);
                return UtilsMgr.getCtx().getString(R.string.base_call_unknown_error);
            }
        }
    }

    /**
     * 获取手机型号
     *
     * @return String
     */
    public static String getModel() {
        return Build.MODEL != null ? Build.MODEL.replace(" ", "")
                : "unknown";
    }

    /**
     * 获取手机生产
     *
     * @return String
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER != null ? Build.MANUFACTURER
                : "unknown";
    }
    
    /** 是否是vivo手机 */
    public static boolean isVivo() {
        return Build.BRAND.equalsIgnoreCase("vivo")
                || Build.MANUFACTURER.equalsIgnoreCase("vivo")
                || Build.MODEL.toLowerCase().contains("vivo");
    }

    /** 是否是vivo手机 */
    public static boolean isOppo() {
        return Build.BRAND.equalsIgnoreCase("oppo")
                || Build.MANUFACTURER.equalsIgnoreCase("oppo")
                || Build.MODEL.toLowerCase().contains("oppo");
    }
    
    /**
     * 获取磁盘可用空间
     */
    public static long getAvailableStorage(File file) {
        String storageDirectory = file.getParentFile().getAbsolutePath();
        try {
            StatFs stat = new StatFs(storageDirectory);
            return getAvailableBlocks(stat) * getBlockSize(stat);
        } catch (RuntimeException ex) {
            return 0;
        }
    }
    
    private static long getAvailableBlocks(StatFs sf) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return sf.getAvailableBlocksLong();
        }
        else {
            return sf.getAvailableBlocks();
        }
    }
    
    private static long getBlockSize(StatFs sf) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return sf.getBlockSizeLong();
        }
        else {
            return sf.getBlockSize();
        }
    }
    
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static PropertiesWrapper sBuildPropWrapper;

    /**
     * 判断当前设备系统是否为miui
     * 链接：https://www.zhihu.com/question/22102139/answer/24834510 小米开发者文档
     * http://dev.xiaomi.com/doc/?p=254
     * 
     * @return
     */
    public static boolean isMIUI() {
        try {
            if (sBuildPropWrapper == null) {
                sBuildPropWrapper = new PropertiesWrapper(
                        new File(Environment.getRootDirectory(), "build.prop"));
            }
            return sBuildPropWrapper.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || sBuildPropWrapper.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || sBuildPropWrapper.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }
    
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";
    
    /**
     * 判断当前设备系统是否是EMUI
     * 
     * @return
     */
    public static boolean isEMUI() {
        return !TextUtils.isEmpty(getEmuiVersion());
    }

    /** http://club.huawei.com/thread-10205061-1-1-2851.html
     *  华为官方团队给出的如何判断手机是EMUI的方法，只要返回的不是""，就是EMUI版本
     * @return
     */
    public static String getEmuiVersion() {
        String emuiVerion = "";
        Class<?>[] clsArray = new Class<?>[] { String.class };
        Object[] objArray = new Object[] { "ro.build.version.emui" };
        try {
            Class<?> SystemPropertiesClass = Class
                    .forName("android.os.SystemProperties");
            Method get = SystemPropertiesClass.getDeclaredMethod("get",
                    clsArray);
            String version = (String) get.invoke(SystemPropertiesClass,
                    objArray);
            Logger.d( "get EMUI version is:" + version);
            if (!TextUtils.isEmpty(version)) {
                return version;
            }
        } catch (ClassNotFoundException e) {
            Logger.e( " getEmuiVersion wrong, ClassNotFoundException");
        } catch (LinkageError e) {
            Logger.e( " getEmuiVersion wrong, LinkageError");
        } catch (NoSuchMethodException e) {
            Logger.e( " getEmuiVersion wrong, NoSuchMethodException");
        } catch (NullPointerException e) {
            Logger.e( " getEmuiVersion wrong, NullPointerException");
        } catch (Exception e) {
            Logger.e( " getEmuiVersion wrong");
        }
        return emuiVerion;
    }

    
    /**
     * 判断当前设备系统是否是FlyMe
     * 
     * @return
     */
    public static boolean isFlyme() {
        try {
            // Invoke Build.hasSmartBar()
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }
    
    /**
     * 返回电池的状态
     */
    public static int getBatteryStatus() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = UtilsMgr.getCtx().registerReceiver(null, filter);
        if (batteryStatus != null) {
            return batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
        }
        else {
            return BatteryManager.BATTERY_STATUS_UNKNOWN;
        }
    }
    
    /**
     * 返回电池的电量
     */
    public static float getBatteryPowerPercent() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = UtilsMgr.getCtx().registerReceiver(null, filter);
        if (batteryStatus != null) {
            // 当前剩余电量
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            // 电量最大值
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            return (float) level / scale;
        }
        else {
            return -1;
        }
    }
    
    /**
     * 当前是否在充电
     */
    public static boolean isBatteryCharging() {
        return getBatteryStatus() == BatteryManager.BATTERY_STATUS_CHARGING;
    }
    
}
