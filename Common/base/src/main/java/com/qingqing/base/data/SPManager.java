package com.qingqing.base.data;

import android.text.TextUtils;

import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.AppUtil;

/**
 * SharePreference存储
 * 
 * @author tanwei
 * 
 */
public class SPManager {
    
    private static final String TAG = "sp";
    
    private static SPManager sInstance;
    private SPWrapper mSPWrapper;
    
    /** sp默认名字是sp，即data/data/package/files/shared_prefs/sp.xml */
    public static void init(String name) {

        if (sInstance != null) {
            return;
        }

        if(!AppUtil.isMainProcess()){
            Logger.e(TAG,"只有主进程可以使用SPManager，其他进程请使用SPWrapper");
            return;
        }
        
        String spName;
        if (TextUtils.isEmpty(name)) {
            spName = TAG;
        }
        else {
            spName = name;
        }
        sInstance = new SPManager(spName);
    }
    
    private SPManager(String name) {
        mSPWrapper = new SPWrapper(name);
    }
    
    public static boolean contains(final String key) {
        return sInstance.mSPWrapper.contains(key);
    }
    
    public static void put(String key, String value) {
        sInstance.mSPWrapper.put(key, value);
    }
    
    public static boolean putImmediately(String key, String value) {
        return sInstance.mSPWrapper.putImmediately(key, value);
    }
    
    public static void put(String key, int value) {
        sInstance.mSPWrapper.put(key, value);
    }
    
    public static boolean putImmediately(String key, int value) {
        return sInstance.mSPWrapper.putImmediately(key, value);
    }
    
    public static void put(String key, long value) {
        sInstance.mSPWrapper.put(key, value);
    }
    
    public static boolean putImmediately(String key, long value) {
        return sInstance.mSPWrapper.putImmediately(key, value);
    }
    
    public static void put(String key, boolean value) {
        sInstance.mSPWrapper.put(key, value);
    }
    
    public static boolean putImmediately(String key, boolean value) {
        return sInstance.mSPWrapper.putImmediately(key, value);
    }
    
    public static String getString(String key) {
        return getString(key, null);
    }
    
    public static String getString(String key, String defaultValue) {
        return sInstance.mSPWrapper.getString(key, defaultValue);
    }
    
    public static int getInt(String key) {
        return getInt(key, 0);
    }
    
    public static int getInt(String key, int defaultValue) {
        return sInstance.mSPWrapper.getInt(key, defaultValue);
    }
    
    public static long getLong(String key) {
        return getLong(key, 0);
    }
    
    public static long getLong(String key, long defaultValue) {
        return sInstance.mSPWrapper.getLong(key, defaultValue);
    }
    
    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        return sInstance.mSPWrapper.getBoolean(key, defaultValue);
    }
    
    public static void remove(String key) {
        sInstance.mSPWrapper.remove(key);
    }
    
    public static boolean removeImmediately(String key) {
        return sInstance.mSPWrapper.removeImmediately(key);
    }

    public static SPWrapper getSPWrapper(){
        return sInstance.mSPWrapper;
    }
}
