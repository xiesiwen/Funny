package com.qingqing.base.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

/**
 * MD5 工具类
 * 
 * @author Bruce.lu
 * 
 */

public final class MD5Util {
    
    public static String encodePackage(String packageName) {
        PackageManager mPackMgr = UtilsMgr.getCtx().getPackageManager();
        try {
            ApplicationInfo ai = mPackMgr.getApplicationInfo(packageName, 0);
            File file = new File(ai.publicSourceDir);
            return encode(file);
        } catch (NameNotFoundException e) {}
        return null;
    }
    
    public static Map<String, String> encodeDir(File file) {
        Map<String, String> encodeMD5 = null;
        if (file.exists() && file.isDirectory()) {
            encodeMD5 = new HashMap<String, String>();
            File[] childFiles = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".apk") || filename.endsWith(".APK");
                }
            });
            for (File childFile : childFiles) {
                if (childFile.isFile())
                    encodeMD5.put(childFile.getName(), encode(file));
            }
        }
        return encodeMD5;
    }
    
    /**
     * 对文件做摘要
     */
    public static String encode(File file) {
        if (!file.exists())
            return null;
        
        try {
            return encode(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String encode(InputStream is) {
        if (is == null)
            return null;
        
        DigestInputStream dis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(is, md);
            
            byte[] buf = new byte[1024 * 5];
            while (dis.read(buf, 0, 1024 * 5) != -1)
                ;
            
            byte[] md5 = md.digest();
            return toHexString(md5);
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            IOUtil.closeQuietly(dis);
        }
        
        return null;
    }
    
    /**
     * 对字节数组做MD5摘要
     * */
    public static String encode(byte[] inData) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(inData);
            byte[] md5 = md.digest();
            return toHexString(md5);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 对字符串做MD5摘要
     * 
     * @param inStr
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String encode(String inStr) {
        try {
            byte[] data = inStr.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            byte[] md5 = md.digest();
            
            return toHexString(md5);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String toHexString(byte[] in) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length; i++) {
            sb.append(Integer.toHexString((0x000000ff & in[i]) | 0xffffff00).substring(6));
        }
        return sb.toString();
    }
    
    public static final String getMD5String(String s) throws NoSuchAlgorithmException {
        byte abyte0[] = MessageDigest.getInstance("SHA-1").digest(s.getBytes());
        Formatter formatter = new Formatter();
        try {
            int i = abyte0.length;
            for (int j = 0; j < i; j++) {
                byte byte0 = abyte0[j];
                formatter.format("%02x", byte0);
            }
            return formatter.toString();
        } finally {
            formatter.close();
        }
    }
    
    private static String byte2Hex(byte b) {
        int value = (b & 0x7F) + (b < 0 ? 0x80 : 0);
        return (value < 0x10 ? "0" : "")
                + StringUtil.toLowerCase(Integer.toHexString(value));
    }
    
    public static String MD5_32(String passwd) {
        if (TextUtils.isEmpty(passwd)) {
            return null;
        }
        try {
            return MD5_32(passwd.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {}
        return null;
    }
    
    public static String MD5_32(byte[] passwd) {
        
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            
            StringBuffer strbuf = new StringBuffer();
            
            // md5.update(passwd.getBytes(), 0, passwd.length());
            md5.update(passwd);
            byte[] digest = md5.digest();
            
            for (int i = 0; i < digest.length; i++) {
                strbuf.append(byte2Hex(digest[i]));
            }
            
            return strbuf.toString();
            
        } catch (NoSuchAlgorithmException e) {}
        
        return null;
    }
    
    public static String MD5_16(String passwd) {
        return MD5_32(passwd).subSequence(8, 24).toString();
    }
    
    public static String MD5_16(byte[] passwd) {
        return MD5_32(passwd).subSequence(8, 24).toString();
    }
    
}
