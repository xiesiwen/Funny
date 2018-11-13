package com.qingqing.base.config;

import java.util.HashMap;

import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.qingqingbase.R;


/**
 * 逻辑控制类
 * */
public final class LogicConfig {

    public static final int SMS_REQUEST_INTERVAL = 60;// 验证码获取请求的时间间隔为60s
    public static final int PAY_REQUEST_INTERVAL = 60 * 60 * 2;// 当前时间距离订单过期时间2小时内才显示倒计时
    
    public static final String ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone";
    
    public static final int MOBILE_LEN = 11;// 手机号长度
    public static final int PASSWORD_LEN = 20;// 密码最大长度
    public static final int VERIFY_CODE_LEN = 6;// 验证码长度
    
    public static final int HEAD_DEFAULT_WIDTH = 720;// 默认头像宽度
    public static final int HEAD_DEFAULT_HEIGHT = 720;// 默认头像高度
    
    public static final int HEAD_DEFAULT_SMALL_WIDTH = 200;// 默认小头像宽度
    public static final int HEAD_DEFAULT_SMALL_HEIGHT = 200;// 默认小头像高度
    
    public static final int DEFAULT_CROP_IMG_WIDTH = 200;// 默认缩略图的宽度
    public static final int DEFAULT_CROP_IMG_HEIGHT = 200;// 默认缩略图的高度
    
    public static final int BIG_CROP_IMG_WIDTH = 400;// 默认缩略图的宽度
    public static final int BIG_CROP_IMG_HEIGHT = 400;// 默认缩略图的高度
    
    public static final long INTERVAL_UPDATE_CONTACTS = 24 * 60 * 60 * 1000;// 更新联系人间隔
                                                                            // 24小时
    
    public static final int AUDIO_RECORD_COUNTDOWN_SECS = 5 * 60;
    private static final int DEFAULT_FORMAT_DOT_DECIMAL_LENGTH = 2; // 截取小数点显示，默认的位数

    public static final String TEST_DEFAULT_ENV = "tst";

    /** 根据规则，分别为没有小数，1位小数，2位小数 */
    public static String getFormatDotString(String str) {
        
        try {
            double dotNum = Double.parseDouble(str);
            return getFormatDotString(dotNum);
        } catch (Exception e) {
            return "";
        }
    }
    
    public static String getFormatDotString(float dotNum) {
        return getFormatDotString((double) dotNum);
    }
    
    public static String getFormatDotString(double dotNum) {
        return getFormatDotString(dotNum, DEFAULT_FORMAT_DOT_DECIMAL_LENGTH);
    }

    public static String getFormatDotString(double dotNum, int decimalLength) {
        String string = String.valueOf(dotNum).toUpperCase();
        if (decimalLength < 0) {
            decimalLength = 0;
        }
        string = reviseDoubleString(string);
        if (!string.contains(".")) {
            return string;
        }
        int targetStringLength = string.indexOf(".") + decimalLength + 1;
        if (targetStringLength > string.length()) {
            targetStringLength = string.length();
        }

        string = string.substring(0, targetStringLength);

        while (string.endsWith("0")) {
            string = string.substring(0, string.length() - 1);
        }

        if (string.endsWith(".")) {
            string = string.substring(0, string.length() - 1);
        }

        return string;
    }

    private static String reviseDoubleString(String string) {
        if (!string.contains("E") || !string.contains(".")) {
            return string;
        } else {
            String front = string.substring(0, string.indexOf("."));
            String middle = string.substring(string.indexOf(".") + 1, string.indexOf("E"));
            String end = string.substring(string.indexOf("E") + 1, string.length());
            StringBuffer stringBuffer = new StringBuffer(front);
            try {
                int count = Integer.parseInt(end);
                int length = middle.length();
                int i = 0;
                for (; i < count; i++) {
                    if (i < length) {
                        stringBuffer.append(middle.charAt(i));
                    } else {
                        stringBuffer.append("0");
                    }
                }
                if (i < length) {
                    stringBuffer.append(".").append(middle.substring(i, length));
                }
            } catch (Exception e) {
                return string;
            }
            return stringBuffer.toString();
        }
    }

    public static boolean isMDStatusBarStyleValid(){
        return BaseData.getClientType() == AppCommon.AppType.qingqing_teacher
                ||BaseData.getClientType()==AppCommon.AppType.qingqing_student
                ||BaseData.getClientType()==AppCommon.AppType.qingqing_ta;
    }



    public static String sectionTOChinese(int section, String chineseNum) {
        String sectionChinese = new String();//小节部分用独立函数操作
        int unitPos = 0;//小节内部的权值计数器
        boolean zero = true;//小节内部的制零判断，每个小节内只能出现一个零
        while (section > 0) {
            int v = section % 10;//取当前最末位的值
            if (v == 0) {
                if (!zero) {
                    zero = true;//需要补零的操作，确保对连续多个零只是输出一个
                    chineseNum = Tool.chnNumChar[0] + chineseNum;
                }
            } else {
                zero = false;//有非零的数字，就把制零开关打开
                sectionChinese = Tool.chnNumChar[v];//对应中文数字位
                sectionChinese = sectionChinese + Tool.chnUnitChar[unitPos];//对应中文权位
                chineseNum = sectionChinese + chineseNum;
            }
            unitPos++;
            section = section / 10;
        }

        return chineseNum;
    }

    public static class Tool {
        //数字位
        public static String[] chnNumChar = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        public static char[] chnNumChinese = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};
        //节权位
        public static String[] chnUnitSection = {"", "万", "亿", "万亿"};
        //权位
        public static String[] chnUnitChar = {"", "十", "百", "千"};
        public static HashMap intList = new HashMap();

        static {
            for (int i = 0; i < chnNumChar.length; i++) {
                intList.put(chnNumChinese[i], i);
            }

            intList.put('十', 10);
            intList.put('百', 100);
            intList.put('千', 1000);
        }

    }

}
