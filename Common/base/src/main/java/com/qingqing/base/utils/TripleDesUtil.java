package com.qingqing.base.utils;

import java.security.InvalidParameterException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.qingqing.base.crypt.Hex;

import android.util.Base64;

/**
 * 3DES又称Triple DES，是DES加密算法的一种模式，它使用3条56位的密钥对 3DES 3DES
 * 数据进行三次加密。数据加密标准（DES）是美国的一种由来已久的加密标准，它使用对称密钥加密法，并于1981年被ANSI组织规范为ANSI
 * X.3.92。DES使用56位密钥和密码块的方法，而在密码块的方法中，文本被分成64位大小的文本块然后再进行加密。
 */
public final class TripleDesUtil {
    
    private static final String KEY[] = { "70706C6976656F6B",
            "15B9FDAEDA40F86BF71C73292516924A294FC8BA31B6E9EA",
            "29028A7698EF4C6D3D252F02F4F79D5815389DF18525D326",
            "D046E6B6A4A85EB6C44C73372A0D5DF1AE76405173B3D5EC",
            "435229C8F79831131923F18C5DE32F253E2AF2AD348C4615",
            "9B2915A72F8329A2FE6B681C8AAE1F97ABA8D9D58576AB20",
            "B3B0CD830D92CB3720A13EF4D93B1A133DA4497667F75191",
            "AD327AFB5E19D023150E382F6D3B3EB5B6319120649D31F8",
            "C42F31B008BF257067ABF115E0346E292313C746B3581FB0",
            "529B75BAE0CE2038466704A86D985E1C2557230DDF311ABC",
            "8A529D5DCE91FEE39E9EE9545DF42C3D9DEC2F767C89CEAB", };
    
    private static final String CRYPTALGORITHM = "DESede/CBC/PKCS5Padding";
    
    private static final String CODINGTYPE = "UTF-8";
    
    private static final byte DEFAULTIV[] = { 1, 2, 3, 4, 5, 6, 7, 8 };
    
    private static final String KEYALGORITHM = "DESede";
    
    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    public static String encode(String param, int keyIndex) throws Exception {
        if (keyIndex <= 0 || keyIndex >= KEY.length) {
            throw new InvalidParameterException();
        }
        
        String key = KEY[keyIndex];
        byte[] byteIV = hex2byte(KEY[0]);
        byte input[] = Hex.decode(key);
        Cipher cipher = Cipher.getInstance(CRYPTALGORITHM);
        DESedeKeySpec desKeySpec = new DESedeKeySpec(input);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivGenerator(byteIV));
        
        byte[] output = cipher.doFinal(param.getBytes(CODINGTYPE));
        String strDesEnc = new String(Base64.encode(output, Base64.DEFAULT), CODINGTYPE);
        return strDesEnc;
    }
    
    public static String encode(String param) throws Exception {
        return encode(param, 0);
    }
    
    public static String decode(String param, int keyIndex) throws Exception {
        if (keyIndex <= 0 || keyIndex >= KEY.length) {
            throw new InvalidParameterException();
        }
        String key = KEY[keyIndex];
        byte[] byteIV = hex2byte(KEY[0]);
        String reponseDecrpt = decrypt(param, key, byteIV);
        
        return reponseDecrpt;
    }
    
    public static String decode(String param) throws Exception {
        return decode(param, 0);
    }
    
    public static String toHexString(byte[] b) { // String to byte
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }
    
    private static Key keyGenerator(String keyStr) throws Exception {
        byte input[] = Hex.decode(keyStr);
        DESedeKeySpec keySpec = new DESedeKeySpec(input);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYALGORITHM);
        return keyFactory.generateSecret(keySpec);
    }
    
    private static IvParameterSpec ivGenerator(byte b[]) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(b);
        return iv;
    }
    
    private static byte[] base64Decode(String s) throws Exception {
        return Base64.decode(s, Base64.DEFAULT);
    }
    
    private static String decrypt(String strTobeDeCrypted, String strKey, byte byteIV[])
            throws Exception {
        byte input[] = base64Decode(strTobeDeCrypted);
        Key k = keyGenerator(strKey);
        IvParameterSpec ivSpec = byteIV.length != 0 ? ivGenerator(byteIV)
                : ivGenerator(DEFAULTIV);
        Cipher c = Cipher.getInstance(CRYPTALGORITHM);
        c.init(2, k, ivSpec);
        byte output[] = c.doFinal(input);
        return new String(output, CODINGTYPE);
    }
    
    private static byte[] hex2byte(String hex) throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = (Integer.valueOf(byteint)).byteValue();
        }
        return b;
    }
}
