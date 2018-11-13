package com.qingqing.base.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.TripleDesUtil;
import com.qingqing.base.utils.UtilsMgr;

import java.util.Set;

/**
 * Created by Wangxiaxin on 2015/11/18.
 *
 * shared prefs 操作封装类
 */
public class SPWrapper {

    private SharedPreferences mSP;
    private static final String TAG = "SPWrapper";

    public SPWrapper(String spFileName) {
        mSP = UtilsMgr.getCtx().getSharedPreferences(spFileName, Context.MODE_PRIVATE);
    }

    public boolean contains(final String key) {
        return mSP.contains(key);
    }

    public void put(String key, String value) {
        Logger.v(TAG, "---put---" + key + " --- " + value);
        mSP.edit().putString(key, value).apply();
    }

    public boolean putImmediately(String key, String value) {
        Logger.v(TAG, "---put imd---" + key + " --- " + value);
        return mSP.edit().putString(key, value).commit();
    }

    public void putEncryptString(String key, String value) {

        if (TextUtils.isEmpty(value))
            return;
        try {
            final String encryptValue = TripleDesUtil.encode(value, 7);
            Logger.v(TAG,"---put encrypt---" + key  + " --- " + value);
            mSP.edit().putString(key, encryptValue).apply();
        } catch (Exception e) {
            Logger.w(e);
        }
    }
    public boolean putEncryptStringImmediately(String key, String value) {
        if (TextUtils.isEmpty(value))
            return false;
        try {
            final String encryptValue = TripleDesUtil.encode(value, 7);
            Logger.v(TAG,"---put encrypt imd---" + key  + " --- " + value);
            return mSP.edit().putString(key, encryptValue).commit();
        } catch (Exception e) {
            Logger.w(e);
            return false;
        }
    }

    public void put(String key, int value) {
        Logger.v(TAG,"---put---" + key  + " --- " + value);
        mSP.edit().putInt(key, value).apply();
    }

    public boolean putImmediately(String key, int value) {
        Logger.v(TAG,"---put imd---" + key  + " --- " + value);
        return mSP.edit().putInt(key, value).commit();
    }

    public void put(String key, long value) {
        Logger.v(TAG,"---put---" + key  + " --- " + value);
        mSP.edit().putLong(key, value).apply();
    }
    public boolean putImmediately(String key, long value) {
        Logger.v(TAG,"---put imd---" + key  + " --- " + value);
        return mSP.edit().putLong(key, value).commit();
    }

    public void put(String key, boolean value) {
        Logger.v(TAG,"---put---" + key  + " --- " + value);
        mSP.edit().putBoolean(key, value).apply();
    }
    public boolean putImmediately(String key, boolean value) {
        Logger.v(TAG,"---put imd---" + key  + " --- " + value);
        return mSP.edit().putBoolean(key, value).commit();
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
//        Logger.v(TAG,"---get string---" + key);
        return mSP.getString(key, defaultValue);
    }

    public String getEncryptString(String key, String defaultValue) {

        String encryptValue = getString(key, null);
        if (TextUtils.isEmpty(encryptValue)) {
            return defaultValue;
        }

        try {
            Logger.v(TAG,"---get encrypt string---" + key);
            return TripleDesUtil.decode(encryptValue, 7);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
//        Logger.v(TAG,"---get int---" + key);
        return mSP.getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
//        Logger.v(TAG,"---get long---" + key);
        return mSP.getLong(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
//        Logger.v(TAG,"---get boolean---" + key);
        return mSP.getBoolean(key, defaultValue);
    }

    public void clear() {
        mSP.edit().clear().apply();
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return mSP.getStringSet(key, defaultValue);
    }

    public void put(String key, Set<String> value) {
        remove(key);
        mSP.edit().putStringSet(key, value).apply();
    }
    public boolean putImmediately(String key, Set<String> value) {
        return removeImmediately(key) && mSP.edit().putStringSet(key, value).commit();
    }

    public void remove(String key) {
        mSP.edit().remove(key).apply();
    }

    public boolean removeImmediately(String key) {
        return mSP.edit().remove(key).commit();
    }
}
