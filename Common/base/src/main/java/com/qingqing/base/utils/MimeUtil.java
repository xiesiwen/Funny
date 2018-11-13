package com.qingqing.base.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

/**
 * Utilities for dealing with MIME types. MIME types based on
 * android.webkit.MimeTypeMap.
 */
public final class MimeUtil {
    
    private static final Map<String, String> sExtensionMinusMap = new HashMap<String, String>();
    private static final Map<String, String> sExtensionPlusMap = new HashMap<String, String>();
    
    /**
     * 增加后缀类型
     * */
    public static void addExtension(String extension, String mimeType) {
        if (!sExtensionPlusMap.containsKey(extension))
            sExtensionPlusMap.put(extension, mimeType);
        if (sExtensionMinusMap.containsKey(extension))
            sExtensionMinusMap.remove(extension);
    }
    
    /**
     * 删减后缀类型，必须是android.webkit.MimeTypeMap中包含的extension类型
     * */
    public static void deleteExtension(String extension, String mimeType) {
        if (!sExtensionMinusMap.containsKey(extension))
            sExtensionMinusMap.put(extension, mimeType);
        if (sExtensionPlusMap.containsKey(extension))
            sExtensionPlusMap.remove(extension);
    }
    
    /**
     * 判断当前是否包含指定的MIME类型
     * 
     * @param mimeType
     *            A MIME type (i.e. text/plain)
     * @return True if there is a mimeType entry in the map.
     */
    public static boolean hasMimeType(String mimeType) {
        
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        
        // 如果白名单中有此mimetype，则可以直接认定包含此mimetype
        if (sExtensionPlusMap.containsValue(mimeType))
            return true;
        
        if (sExtensionMinusMap.containsValue(mimeType))
            return false;
        
        return MimeTypeMap.getSingleton().hasMimeType(mimeType);
    }
    
    /**
     * Returns the MIME type for the given extension.
     * 
     * @param extension
     *            A file extension without the leading '.'
     * @return The MIME type for the given extension or null iff there is none.
     */
    public static String guessMimeTypeFromExtension(String extension) {
        
        if (TextUtils.isEmpty(extension)) {
            return null;
        }
        
        String mimeType = sExtensionPlusMap.get(extension);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        
        return mimeType;
    }
    
    /**
     * Returns true if the given extension has a registered MIME type.
     * 
     * @param extension
     *            A file extension without the leading '.'
     * @return True iff there is an extension entry in the map.
     */
    public static boolean hasExtension(String extension) {
        if (TextUtils.isEmpty(extension)) {
            return false;
        }
        
        if (sExtensionMinusMap.containsKey(extension))
            return false;
        
        if (sExtensionPlusMap.containsKey(extension))
            return true;
        
        return MimeTypeMap.getSingleton().hasExtension(extension);
    }
    
    /**
     * Returns the registered extension for the given MIME type. Note that some
     * MIME types map to multiple extensions. This call will return the most
     * common extension for the given MIME type.
     * 
     * @param mimeType
     *            A MIME type (i.e. text/plain)
     * @return The extension for the given MIME type or null iff there is none.
     */
    public static String guessExtensionFromMimeType(String mimeType) {
        
        if (TextUtils.isEmpty(mimeType)) {
            return null;
        }
        
        if (sExtensionPlusMap.containsValue(mimeType)) {
            for (Entry<String, String> entry : sExtensionPlusMap.entrySet()) {
                if (mimeType.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }
    
    public static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1) {
            return "*/*";
        }
        
        String ext = StringUtil.toLowerCase(filePath.substring(dotPosition + 1,
                filePath.length()));
        ext = ext.substring(0, !ext.contains("?") ? ext.length() : ext.indexOf("?"));
        String mimeType = MimeUtil.guessMimeTypeFromExtension(ext);
        // if (ext.equals("mtz")) {
        // mimeType = "application/miui-mtz";
        // }
        
        return mimeType != null ? mimeType : "*/*";
    }
}
