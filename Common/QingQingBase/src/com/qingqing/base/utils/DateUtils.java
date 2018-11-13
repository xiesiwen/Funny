package com.qingqing.base.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.qingqing.api.proto.v1.Time;
import com.qingqing.base.time.NetworkTime;

public class DateUtils {
    
    private static final long INTERVAL_IN_MILLISECONDS = 30 * 1000;


    public static final int  SECONDS_IN_DAY = 60 * 60 * 24;
    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY; //一天有多少毫秒
    public static final long MILLSECONDS_IN_ONE_HOUR = 60*60*1000L; //一小时有多少毫秒


    public static final SimpleDateFormat mdsdf= new SimpleDateFormat("MM月dd日 HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mdhmssdf= new SimpleDateFormat("MM月dd日 HH:mm:ss",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mdhmsdf= new SimpleDateFormat("MM-dd HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ymdhmSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ymdhmsSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ymdSdf = new SimpleDateFormat("yyyy-MM-dd",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ymdSdfwithDot = new SimpleDateFormat("yyyy.MM.dd",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ymdhsdf = new SimpleDateFormat("yyyy.MM.dd HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat hmSdf = new SimpleDateFormat("HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mdSdf = new SimpleDateFormat("MM月dd日",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ymdhmsfloatSdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mYearAndMonthAndDateFormat = new SimpleDateFormat("yyyy年MM月dd日",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mdwSdf = new SimpleDateFormat("MM月dd日 E",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ysdf= new SimpleDateFormat("yyyy年",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat md = new SimpleDateFormat("MM.dd",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mdwddf = new SimpleDateFormat("MM.dd E",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mdwhmSdf = new SimpleDateFormat("MM月dd日 E HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ymdSdfInChinese = new SimpleDateFormat("yyyy年MM月dd日",Locale.SIMPLIFIED_CHINESE);

    //下面是这次整理出来的大写开头是中文，小写开头是英文
    public static final SimpleDateFormat YearMonthFormat = new SimpleDateFormat("yyyy年MM月",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat yearmonthFormat = new SimpleDateFormat("yyyy.MM",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat yearandmonthanddayFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat yesterdayhourminuteFormat = new SimpleDateFormat("昨天 HH:mm", Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat monthdayFormat = new SimpleDateFormat("MM-dd",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat shortYearMonthFormat = new SimpleDateFormat("yy年M月",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat shortMonthFormat = new SimpleDateFormat("M月",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat MonthFormat = new SimpleDateFormat("MM月",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat YearMonthDayhourminuteFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat yearmonthdayhourminuteContinuousFormat = new SimpleDateFormat("yyyyMMddHHmmss",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat weekFormat = new SimpleDateFormat("E",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat YearMonthDayhourminutesecondDeadlineFormat = new SimpleDateFormat("有限期至：yyyy年MM月dd日 HH:mm:ss",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat MonthDayhourminuteReplyFormat = new SimpleDateFormat("回评时间：MM月dd日 HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat sFormat = new SimpleDateFormat("s\"",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat ssFormat = new SimpleDateFormat("ss\"",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mFormat = new SimpleDateFormat("m\'\'",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mmFormat = new SimpleDateFormat("mm\'\'",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mssFormat = new SimpleDateFormat("m\'\'ss\"",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat mmssFormat = new SimpleDateFormat("mm\'\'ss\"",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat SFormat = new SimpleDateFormat("s秒",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat SSFormat = new SimpleDateFormat("ss秒",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat MFormat = new SimpleDateFormat("m分钟",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat MMFormat = new SimpleDateFormat("mm分钟",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat MSSFormat = new SimpleDateFormat("m分钟ss秒",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat MMSSFormat = new SimpleDateFormat("mm分钟ss秒",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat minutesecondFormat = new SimpleDateFormat("mm:ss",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat weekhourminute = new SimpleDateFormat("E HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat yearmonthdayhourminutesecondEnglishFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    public static final SimpleDateFormat MonthDayShorthourminuteFormat = new SimpleDateFormat("M月d日 HH:mm",Locale.SIMPLIFIED_CHINESE);
    public static final SimpleDateFormat MonthDayShortFormat = new SimpleDateFormat("M月d日",Locale.SIMPLIFIED_CHINESE);

    public static String getTimestampString(Date messageDate) {
        // Locale curLocale =
        // HXSDKHelper.getInstance().getAppContext().getResources().getConfiguration().locale;
        //
        // String languageCode = curLocale.getLanguage();
        
        boolean isChinese = true; // languageCode.contains("zh");
        
        String format = null;
        
        long messageTime = messageDate.getTime();
        if (isSameDay(messageTime)) {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(messageDate);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            
            format = "HH:mm";
            
            if (hour > 17) {
                if (isChinese) {
                    format = "晚上 hh:mm";
                }
                
            }
            else if (hour >= 0 && hour <= 6) {
                if (isChinese) {
                    format = "凌晨 hh:mm";
                }
            }
            else if (hour > 11 && hour <= 17) {
                if (isChinese) {
                    format = "下午 hh:mm";
                }
                
            }
            else {
                if (isChinese) {
                    format = "上午 hh:mm";
                }
            }
        }
        else if (isYesterday(messageTime)) {
            if (isChinese) {
                format = "昨天 HH:mm";
            }
            else {
                format = "MM-dd HH:mm";
            }
            
        }
        else {
            if (isChinese) {
                format = "M月d日 HH:mm";
            }
            else {
                format = "MM-dd HH:mm";
            }
        }
        
        if (isChinese) {
            return new SimpleDateFormat(format, Locale.CHINA).format(messageDate);
        }
        else {
            return new SimpleDateFormat(format, Locale.US).format(messageDate);
        }
    }
    
    public static boolean isCloseEnough(long time1, long time2) {
        // long time1 = date1.getTime();
        // long time2 = date2.getTime();
        long delta = time1 - time2;
        if (delta < 0) {
            delta = -delta;
        }
        return delta < INTERVAL_IN_MILLISECONDS;
    }
    
    private static boolean isSameDay(long inputTime) {
        return false;
    }
    
    private static boolean isYesterday(long inputTime) {
        return false;
    }
    
    public static Date StringToDate(String dateStr, String formatStr) {
        DateFormat format = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    
    /**
     * 
     * @param timeLength
     *            Millisecond
     * @return
     */
    public static String toTime(int timeLength) {
        timeLength /= 1000;
        int minute = timeLength / 60;
        int hour = 0;
        if (minute >= 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        int second = timeLength % 60;
        // return String.format("%02d:%02d:%02d", hour, minute, second);
        return String.format("%02d:%02d", minute, second);
    }
    
    /**
     * 
     * @param timeLength
     *            second
     * @return
     */
    public static String toTimeBySecond(int timeLength) {
        // timeLength /= 1000;
        int minute = timeLength / 60;
        int hour = 0;
        if (minute >= 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        int second = timeLength % 60;
        // return String.format("%02d:%02d:%02d", hour, minute, second);
        return String.format("%02d:%02d", minute, second);
    }
    

    public static String getTimestampStr() {
        return Long.toString(System.currentTimeMillis());
    }

    public static boolean isUrgent(long expireTime){
        boolean isExpire = false;
        int leftSeconds = 0;
        long currentTime = NetworkTime.currentTimeMillis();
        if(expireTime <= currentTime){
            isExpire = true;
        }else{
            leftSeconds = (int) ((expireTime - currentTime) / 1000);
        }
        if(leftSeconds < 3600 * 24){
            isExpire = true;
        }
        return isExpire;

    }

    private static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
    }

    public static String getMonthAndDayAndWeek(String str) {
        Date date;
        try {
            date = ymdSdf.parse(str);
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault(),
                    Locale.CHINESE);
            calendar.setTime(date);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String week = "星期一";
            String m;
            String d;
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (Calendar.MONDAY == weekDay) {
                week = "星期一";
            } else if (Calendar.TUESDAY == weekDay) {
                week = "星期二";
            } else if (Calendar.WEDNESDAY == weekDay) {
                week = "星期三";
            } else if (Calendar.THURSDAY == weekDay) {
                week = "星期四";
            } else if (Calendar.FRIDAY == weekDay) {
                week = "星期五";
            } else if (Calendar.SATURDAY == weekDay) {
                week = "星期六";
            } else if (Calendar.SUNDAY == weekDay) {
                week = "星期日";
            }
            if (month < 10) {
                m = "0" + month;
            } else {
                m = "" + month;
            }
            if (day < 10) {
                d = "0" + day;
            } else {
                d = "" + day;
            }
            return m + "月" + d + "日" + " " + week;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getWalletCourseTime(long courseTime){
        Calendar calendar = Calendar.getInstance();
        Date currentDate = new Date(NetworkTime.currentTimeMillis());
        calendar.setTime(currentDate);
        int currentYear = calendar.get(Calendar.YEAR);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(new Date(courseTime));
        int year = calendar2.get(Calendar.YEAR);
        if (year != currentYear) {
            return ysdf.format(new Date(courseTime))+mdsdf.format(new Date(courseTime));
        }else{
            return mdsdf.format(new Date(courseTime));
        }
    }
    public static String getMonthDayWeek(String str) {
        return null;
    }

    /**
     * 通过开始时间块算时间(小时、分钟)
     *
     * @param startBlock 开始时间块
     * @return
     */
    public static Calendar getStartForHM(int startBlock) {
        Date date = new Date();
        Calendar cc = Calendar.getInstance();
        cc.setTime(date);
        cc.set(Calendar.HOUR_OF_DAY, 8);
        cc.set(Calendar.MINUTE, 0);
        cc.add(Calendar.MINUTE, startBlock * 30);
        return cc;
    }

    /**
     * 通过结束时间块算时间(小时、分钟)
     *
     * @param endBlock
     * @return
     */
    public static Calendar getEndForHM(int endBlock) {
        Date date = new Date();
        Calendar cc = Calendar.getInstance();
        cc.setTime(date);
        cc.set(Calendar.HOUR_OF_DAY, 8);
        cc.set(Calendar.MINUTE, 30);
        cc.add(Calendar.MINUTE, endBlock * 30);
        return cc;
    }
    /**
     * 通过时间和开始的时间块算时间
     *
     * @param date
     * @param startBlock
     * @return
     */
    public static Calendar getStart(long date, int startBlock) {
        Date dd = new Date(date);
        Calendar cc = Calendar.getInstance();
        cc.setTime(dd);
        cc.set(Calendar.HOUR_OF_DAY, 8);
        cc.set(Calendar.MINUTE, 0);
        cc.add(Calendar.MINUTE, startBlock * 30);
        return cc;
    }
    /**
     * 【课程时间显示规则】

     1. 本周显示：本周#  ##:##-##:##；

     2. 下周显示：下周#  ##:##-##:##；

     3. 下周之后的显示：##月##日  ##:##-##:##；

     4. 上周显示：上周#；

     5. 上周之后显示：##月##日

     6. 夸年：####年##月##日
     * @param classTime
     * @return
     */
    public static String getCourseTime(Time.TimeParam classTime, int count){
        return getCourseListTime(classTime,count);
    }
    public static String getCourseListTime(Time.TimeParam classTime, int count) {
        Date date;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            Date currentDate = new Date(NetworkTime.currentTimeMillis());
            calendar.setTime(currentDate);
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonday = getMondayOfThisWeek();
            Calendar c2 = Calendar.getInstance();
            date = ymdSdf.parse(classTime.date);
            c2.setTime(date);
            int year = c2.get(Calendar.YEAR);
            int dayOfWeek = c2.get(Calendar.DAY_OF_WEEK);
            int dayOfYear = c2.get(Calendar.DAY_OF_YEAR);
            if (year != currentYear) {
                return mYearAndMonthAndDateFormat.format(date);
            }
            else {
                String startTime = hmSdf
                        .format(getStartForHM(classTime.startBlock).getTime());
                String endTime = hmSdf
                        .format(getStartForHM(classTime.endBlock + 1).getTime());
                String total = startTime + "-" + endTime;
                if (count > 0) {
                    total = "第" + String.valueOf(count) + "次课";
                }

            }

        } catch (Exception e) {}
        return null;
    }


    /**
     * 得到本周周一
     */
    public static int getMondayOfThisWeek() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(NetworkTime.currentTimeMillis());
        int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == 0)
            day_of_week = 7;
        c.add(Calendar.DATE, -day_of_week + 1);
        int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
        return dayOfYear;
    }

    public static String getYearAndMonthAndDayAndWeek(String str) {
        Date date;
        try {
            date = ymdSdf.parse(str);
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault(),
                    Locale.CHINESE);
            int currentYear = calendar.get(Calendar.YEAR);
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String week = "周一";
            String m;
            String d;
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (Calendar.MONDAY == weekDay) {
                week = "周一";
            } else if (Calendar.TUESDAY == weekDay) {
                week = "周二";
            } else if (Calendar.WEDNESDAY == weekDay) {
                week = "周三";
            } else if (Calendar.THURSDAY == weekDay) {
                week = "周四";
            } else if (Calendar.FRIDAY == weekDay) {
                week = "周五";
            } else if (Calendar.SATURDAY == weekDay) {
                week = "周六";
            } else if (Calendar.SUNDAY == weekDay) {
                week = "周日";
            }
            if (month < 10) {
                m = "0" + month;
            } else {
                m = "" + month;
            }
            if (day < 10) {
                d = "0" + day;
            } else {
                d = "" + day;
            }
            if (year != currentYear) {
                return year + "年" + m + "月" + d + "日" + " " + week;
            } else {
                return m + "月" + d + "日" + " " + week;
            }

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getOrderTime(Time.TimeParam[] timeParams){
        String courseTimeStr = "";
        if (timeParams != null && timeParams.length > 0) {
            for (int i = 0; i < timeParams.length; i++) {
                courseTimeStr += getOrderTimeData(timeParams[i]);
            }
        }
        return courseTimeStr.trim();
    }
    public static String getOrderTimeData(Time.TimeParam timeParam) {
        return String.format("%s %s~%s\n", DateUtils.getMonthDayWeek(timeParam.date),
                DateUtils.hmSdf
                        .format(DateUtils.getStartForHM(timeParam.startBlock).getTime()),
                DateUtils.hmSdf
                        .format(DateUtils.getEndForHM(timeParam.endBlock).getTime()));
    }
    public static String getTimestampString(long var0) {
        try {
            String var1;
            long var2 = var0;
            if (isSameDay(var2)) {
                var1 = "HH:mm";
            } else if (isYesterday(var2)) {
                var1 = "昨天";
            } else {
                var1 = "yyyy-MM-dd";
            }
            return (new SimpleDateFormat(var1, Locale.CHINA)).format(var0);
        } catch (Exception e) {
            return "";
        }
    }

    /** yyyy-MM-dd 用作区分时间段的key值 */
    public static String getDateFormatYMD(final Date date) {
        return ymdSdf.format(date);
    }
}
