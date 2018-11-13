package com.qingqing.base.utils;

import android.text.TextUtils;

import com.google.protobuf.nano.MessageNano;
import com.qingqing.base.data.SPWrapper;
import com.qingqing.base.log.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Created by Wangxiaxin on 2016/3/4.
 * 
 * 用于ProtoBuffer 的通用处理
 */
public class PBUtil {
    
    public static void saveMsg(SPWrapper sp, String key, MessageNano msg) {
        byte[] infoBytes = MessageNano.toByteArray(msg);
        String infoString = StringUtil.bytesToHexString(infoBytes);
        if (!TextUtils.isEmpty(infoString)) {
            sp.putEncryptString(key, infoString);
        }
    }
    
    /**
     * 用于存储 pb 自定义类型数组的数据
     */
    public static void saveMsgArray(SPWrapper sp, String key, MessageNano[] msg) {
        if (msg == null || msg.length <= 0) {
            return;
        }
        
        String infoString = "";
        for (int i = 0; i < msg.length; i++) {
            byte[] infoBytes = MessageNano.toByteArray(msg[i]);
            infoString += StringUtil.bytesToHexString(infoBytes) + "\n";
        }
        if (!TextUtils.isEmpty(infoString)) {
            sp.putEncryptString(key, infoString);
        }
    }
    
    public static MessageNano loadMsg(SPWrapper sp, String key,
            Class<? extends MessageNano> msgClazz) {
        
        MessageNano msg = null;
        String localString = sp.getEncryptString(key, "");
        if (!TextUtils.isEmpty(localString)) {
            byte[] userInfoBytes = StringUtil.hexStringToBytes(localString);
            if (userInfoBytes != null) {
                try {
                    /*
                     * Method parseMethod = msgClazz.getMethod("mergeFrom",
                     * msgClazz,byte[].class); msg = (MessageNano)
                     * parseMethod.invoke(null,msgClazz.newInstance(), userInfoBytes);
                     */
                    msg = MessageNano.mergeFrom(msgClazz.newInstance(), userInfoBytes);
                } catch (Exception e) {
                    Logger.w(e);
                }
            }
        }
        
        if (msg == null) {
            try {
                msg = msgClazz.newInstance();
            } catch (Exception e) {
                Logger.w(e);
            }
        }
        return msg;
    }
    
    /**
     * 读取 pb 自定义类型数组的数据
     * 
     * @return 返回值可以强制转换为数组（如 (AppConfig[])），无结果时返回可强转的 size 为 0 的数组
     */
    public static MessageNano[] loadMsgArray(SPWrapper sp, String key,
            Class<? extends MessageNano> msgClazz) {
        String localString = sp.getEncryptString(key, "");
        if (!TextUtils.isEmpty(localString)) {
            String[] strings = localString.split("\n");
            
            MessageNano[] messageArray = (MessageNano[]) Array.newInstance(msgClazz,
                    strings.length);
            for (int i = 0; i < strings.length; i++) {
                byte[] infoBytes = StringUtil.hexStringToBytes(strings[i]);
                if (infoBytes != null) {
                    try {
                        /*
                         * Method parseMethod = msgClazz.getMethod("mergeFrom",
                         * msgClazz,byte[].class); msg = (MessageNano)
                         * parseMethod.invoke(null,msgClazz.newInstance(), userInfoBytes);
                         */
                        
                        messageArray[i] = MessageNano.mergeFrom(msgClazz.newInstance(),
                                infoBytes);
                    } catch (Exception e) {
                        Logger.w(e);
                    }
                }
            }
            
            return messageArray;
        }
        
        return (MessageNano[]) Array.newInstance(msgClazz, 0);
    }
    
    public static boolean value(Boolean b) {
        return b != null && b;
    }
    
    public static int value(Integer i, int defaultValue) {
        if (i != null) {
            return i;
        }
        else {
            return defaultValue;
        }
    }
    
    public static int value(Integer i) {
        return value(i, 0);
    }
    
    public static long value(Long i, long defaultValue) {
        if (i != null) {
            return i;
        }
        else {
            return defaultValue;
        }
    }
    
    public static long value(Long i) {
        return value(i, 0);
    }
    
    public static double value(Double d, double defaultValue) {
        if (d != null) {
            return d;
        }
        else {
            return defaultValue;
        }
    }
    
    public static double value(Double d) {
        return value(d, 0);
    }
    
    private static String getHasFieldName(String fieldName) {
        if (TextUtils.isEmpty(fieldName))
            return fieldName;
        
        char firstChar = fieldName.charAt(0);
        firstChar -= 32;
        StringBuilder sb = new StringBuilder(fieldName);
        sb.deleteCharAt(0).insert(0, firstChar).insert(0, "has");
        return sb.toString();
    }
    
    public static void set(MessageNano msg, String fieldName, Object value) {
        try {
            Field field = msg.getClass().getField(fieldName);
            field.set(msg, value);
            String hasFieldName = getHasFieldName(fieldName);
            field = msg.getClass().getField(hasFieldName);
            field.setBoolean(msg, true);
        } catch (Exception e) {
            Logger.w(e);
        }
    }
}
