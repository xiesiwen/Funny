package com.qingqing.base.utils;

import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Wangxiaxin on 2015/8/20.
 * 
 * 字符串的 工具类
 * 
 */
public final class StringUtil {
    
    private static final String PATTERN_EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
    private static final String PATTERN_CHAR_NUMBER = "[a-zA-Z0-9]+";
    private static final String PATTERN_NUMBER = "[0-9]+";
    private static final String PATTERN_CHAR = "[a-zA-Z]+";
    private static final String PATTERN_CHINESE = "[\u4E00-\u9FA5]+";
    private static final String PATTERN_CHINESE_ENGLISH = "[a-zA-Z\u4E00-\u9FA5]+";
    private static final String PATTERN_CHINESE_ENGLISH_NUMBER = "[0-9a-zA-Z\u4E00-\u9FA5]+";
    private static final String PATTERN_CHINESE_ENGLISH_SPACE_DOT = "[a-zA-Z\u4E00-\u9FA5. ]+";
    private static final String PATTERN_PASSWORD = "[a-zA-Z0-9]+";
    private static final String PATTERN_MOBILE = /* "[1][3-8]\\\\d{9}" */"^((\\+{0,1}86){0,1})1[0-9]{10}";
    private static final String PATTERN_CITIZEN_CARD = "^\\d{15}(\\d{2}[0-9xX])?$";// 身份证
    private static final String PATTERN_HTTP_URL = "^http://[a-zA-Z0-9./\\\\s]";
    private static final String PATTERN_CAR_NUMBER = "[\u4e00-\u9fa5]{1}[A-Z]{1}[A-Z_0-9]{5}";
    private static final String PATTERN_NUBMER_DOT = "[0-9.]+";
    private static final char[] NUMBER_TEXT = { '零', '一', '二', '三', '四', '五', '六', '七',
            '八', '九' };
    private static final char[] NUMBER = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9' };
    private static final char[] NUMBER_UNIT = { '个', '十', '百', '千', '万' };

    public static final String PATTERN_URL = "((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";
    public static final String PATTERN_URL_WITH_OR_WITHOUT_HTTP = "(https?://|www)[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
    // 必须以"http://","http://"或者"www"这三者之一开头


    /**
     * 检验是否是合法邮箱
     */
    public static boolean checkEmail(String string) {
        return checkPattern(string, PATTERN_EMAIL);
    }
    
    /**
     * 检验是否是字母+数字
     */
    public static boolean checkCharAndNumber(String string) {
        return checkPattern(string, PATTERN_CHAR_NUMBER);
    }
    
    /**
     * 检验是否是数字
     */
    public static boolean checkNumber(String string) {
        return checkPattern(string, PATTERN_NUMBER);
    }
    
    public static boolean checkChineseEnglish(String string) {
        return checkPattern(string, PATTERN_CHINESE_ENGLISH);
    }
    
    public static boolean checkChineseEnglishNumber(String string) {
        return checkPattern(string, PATTERN_CHINESE_ENGLISH_NUMBER);
    }
    
    public static boolean checkChineseEnglishSpaceDot(String string) {
        return checkPattern(string, PATTERN_CHINESE_ENGLISH_SPACE_DOT);
    }
    
    /**
     * 检验是否是密码
     */
    public static boolean checkPassword(String string) {
        return checkPattern(string, PATTERN_PASSWORD);
    }
    
    /**
     * 检验是否是手机号
     */
    public static boolean checkMobilePhone(String string) {
        return checkPattern(string, PATTERN_MOBILE);
    }
    
    public static boolean checkCarNumber(String string) {
        // 车牌号格式：汉字 + A-Z + 5位A-Z或0-9
        // （只包括了普通车牌号，教练车和部分部队车等车牌号不包括在内）
        return checkPattern(string, PATTERN_CAR_NUMBER);
    }
    
    public static boolean checkNumberDot(String string) {
        return checkPattern(string, PATTERN_NUBMER_DOT);
    }
    
    private static boolean checkPattern(String string, String pattern) {
        Pattern _pattern = Pattern.compile(pattern);
        Matcher matcher = _pattern.matcher(string);
        return matcher.matches();
    }
    
    private static String filterPattern(String string, String pattern) {
        // Pattern _pattern = Pattern.compile(pattern);
        // Matcher matcher = _pattern.matcher(string);
        // return matcher.replaceAll("").trim();
        return string.replaceAll(pattern, "");
    }
    
    public static boolean containChinese(String str) {
        Pattern p = Pattern.compile(PATTERN_CHINESE);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
    
    private static boolean isUtf8GraphChar(char codePoint) {
        return ((codePoint >= 0x245F) && (codePoint <= 0x25E5))// utf8图形字符
                || ((codePoint >= 0x2600) && (codePoint <= 0x26FF));// utf8图形字符
    }
    
    public static boolean containUtf8GraphChar(String string) {
        int len = string.length();
        for (int i = 0; i < len; i++) {
            char codePoint = string.charAt(i);
            if (isUtf8GraphChar(codePoint)) { // 如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断是否是Emoji
     *
     * @param codePoint
     *            比较的单个字符
     * @return
     */
    private static boolean isEmoji(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA)
                || (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
                || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }
    
    public static boolean containEmoji(String string) {
        int len = string.length();
        for (int i = 0; i < len; i++) {
            char codePoint = string.charAt(i);
            if (!isEmoji(codePoint)) { // 如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }
    
    /**
     * desc:将数组转为16进制
     *
     * @param bArray
     * @return modified:
     */
    public static String bytesToHexString(byte[] bArray) {
        if (bArray == null) {
            return null;
        }
        if (bArray.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(toUpperCase(sTemp));
        }
        return sb.toString();
    }
    
    /**
     * desc:将16进制的数据转为数组
     * <p>
     * 创建人：聂旭阳 , 2014-5-25 上午11:08:33
     * </p>
     *
     * @param data
     * @return modified:
     */
    public static byte[] hexStringToBytes(String data) {
        String hexString = toUpperCase(data).trim();
        if (hexString.length() % 2 != 0) {
            return null;
        }
        byte[] retData = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i++) {
            int int_ch; // 两位16进制数转化后的10进制数
            char hex_char1 = hexString.charAt(i); // //两位16进制数中的第一位(高位*16)
            int int_ch1;
            if (hex_char1 >= '0' && hex_char1 <= '9')
                int_ch1 = (hex_char1 - 48) * 16; // // 0 的Ascll - 48
            else if (hex_char1 >= 'A' && hex_char1 <= 'F')
                int_ch1 = (hex_char1 - 55) * 16; // // A 的Ascll - 65
            else
                return null;
            i++;
            char hex_char2 = hexString.charAt(i); // /两位16进制数中的第二位(低位)
            int int_ch2;
            if (hex_char2 >= '0' && hex_char2 <= '9')
                int_ch2 = (hex_char2 - 48); // // 0 的Ascll - 48
            else if (hex_char2 >= 'A' && hex_char2 <= 'F')
                int_ch2 = hex_char2 - 55; // // A 的Ascll - 65
            else
                return null;
            int_ch = int_ch1 + int_ch2;
            retData[i / 2] = (byte) int_ch;// 将转化后的数放入Byte里
        }
        return retData;
    }
    
    /**
     * Get utf8 byte array.
     *
     * @param str
     * @return array of NULL if error was found
     */
    public static byte[] getUTF8Bytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static String toDBCStr(String oriStr) {
        char[] c = oriStr.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }
    
    public static String toLowerCase(String inString) {
        return inString.toLowerCase(Locale.ENGLISH);
    }
    
    public static String toUpperCase(String inString) {
        return inString.toUpperCase(Locale.ENGLISH);
    }
    
    public static String formatString(String format, Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }
    
    /**
     * 输出形如 400-1112-3320 的样式
     * <p/>
     * 3-4-4-4 格式
     */
    public static String getPhoneFormatString(String string) {
        if (TextUtils.isEmpty(string) || string.length() <= 3)
            return string;
        
        final int length = string.length();
        StringBuilder sb = new StringBuilder(string.substring(0, 3));
        
        int count = (length - 3) / 4;
        if ((length - 3) % 4 > 0)
            ++count;
        int start, end;
        
        for (int i = 0; i < count; i++) {
            sb.append("-");
            start = 3 + i * 4;
            end = Math.min(start + 4, length);
            sb.append(string.substring(start, end));
        }
        
        return sb.toString();
    }
    
    /**
     * 输入数字输出汉字 12345->一万两千三百四十五 只支持正数，最多五位数
     * 
     * @param num
     * @return
     */
    public static String intToString(int num) {
        Integer integer = num;
        char[] ints = integer.toString().toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        boolean hasText = false;
        int zeroCount = 0;
        for (int i = 0; i < ints.length; i++) {
            if (ints[i] != NUMBER[0]) {
                zeroCount = 0;
                if (ints[i] != NUMBER[1] || i != ints.length - 2) {
                    stringBuilder.append(
                            NUMBER_TEXT[Integer.parseInt(String.valueOf(ints[i]))]);
                }
                if (i != ints.length - 1) {
                    stringBuilder.append(NUMBER_UNIT[ints.length - i - 1]);
                }
                hasText = true;
            }
            else {
                if (hasText) {
                    zeroCount++;
                    if (zeroCount == 1 && i != ints.length - 1) {
                        stringBuilder.append(NUMBER_TEXT[0]);
                    }
                    if (i == ints.length - 1 && zeroCount > 1) {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                }
            }
        }
        if (ints.length == 1 && ints[0] == NUMBER[0]) {
            stringBuilder.append(NUMBER_TEXT[0]);
        }
        
        return stringBuilder.toString();
    }
    
    public static String getMoneyFormatString(Double money) {
        String moneyStr = "";
        DecimalFormat format = new DecimalFormat("#,##0.00");
        
        if (money == null) {
            money = 0.00;
        }
        moneyStr = format.format(money);
        if (money <= 0) {
            moneyStr = "0.00";
        }
        return moneyStr;
    }

    public static String formatDecimal(double decimal) {
        DecimalFormat format = new DecimalFormat("#,##0.00");
        return format.format(decimal);
    }
    
    /**
     * 获取中间四位为 * 的电话号码
     */
    public static String getHiddenPhoneNumber(String phone) {
        String hidden = "";
        if (phone.length() > 4) {
            hidden = phone.substring(0, 3);
            if (phone.length() > 7) {
                hidden += "****" + phone.substring(7, phone.length());
                
            }
            else {
                for (int j = 0; j < phone.length() - 3; j++) {
                    hidden += "*";
                }
            }
        }
        else {
            hidden = phone;
        }
        
        return hidden;
    }
    
    public static class StarReplaceNumberTransformationMethod
            extends ReplacementTransformationMethod {
        
        private static char[] ORIGINAL = new char[] { '0', '1', '2', '3', '4', '5', '6',
                '7', '8', '9', '.' };
        private static char[] REPLACEMENT = new char[] { '*', '*', '*', '*', '*', '*',
                '*', '*', '*', '*', '*' };
        
        private StarReplaceNumberTransformationMethod() {}
        
        @Override
        protected char[] getOriginal() {
            return ORIGINAL;
        }
        
        @Override
        protected char[] getReplacement() {
            return REPLACEMENT;
        }
        
        public static StarReplaceNumberTransformationMethod getInstance() {
            if (sInstance != null)
                return sInstance;
            
            sInstance = new StarReplaceNumberTransformationMethod();
            return sInstance;
        }
        
        private static StarReplaceNumberTransformationMethod sInstance;
    }
    
}
