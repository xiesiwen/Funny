package com.qingqing.base.utils;

import com.qingqing.base.time.NetworkTime;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Wangxiaxin on 2016/3/4.
 * 
 * 与时间&日期 相关的辅助方法
 */
public class TimeUtil {

    public static final long ONE_MIN = 60 * 1000;
    public static final long ONE_HOUR = 60 * ONE_MIN;
    public static final long EIGHT_HOURS = 8 * ONE_HOUR;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_WEEK = 7 * ONE_DAY;

    public static final int MONTH_COUNT_OF_YEAR = 12;
    public static final int DAY_COUNT_OF_WEEK = 7;

    /**
     * 获取指定时间到当前时间的 分钟数
     */
    public static long getCountInMinutes(long time) {
        return getCountInMinutes(time, NetworkTime.currentTimeMillis());
    }
    
    /**
     * 获取两个时间之间相差的分钟数
     */
    public static long getCountInMinutes(long time1, long time2) {
        long interval = Math.abs(time1 - time2);
        return interval / (ONE_MIN);
    }

    /**
     * 计算两个时间戳相差的时 分 秒
     * @return long(时 分 秒)
     */
    public static long[] getTimeReaming(long time1, long time2){
        long[] time = new long[3];
        long interval = time1 - time2;
        time[0] = interval / ONE_HOUR;
        interval %= ONE_HOUR;
        time[1] = interval / ONE_MIN;
        interval %= ONE_MIN;
        time[2] = interval / 1000;
        return time;
    }

    public static int getTotalWeekCount(int minYear, int minMonth, int currentYear,
            int currentMonth) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(currentYear, currentMonth - 1, 1);
        return getTotalWeekCount(minYear, minMonth, currentYear, currentMonth,
                c.getActualMaximum(Calendar.DAY_OF_MONTH));
    }
    
    public static int getTotalWeekCount(int minYear, int minMonth, int currentYear,
            int currentMonth, int currentDayOfMonth) {
        int totalWeeks = 0;
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(minYear, minMonth - 1, 1);
        long startTime = c.getTimeInMillis();
        int startDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int startRealDayOfWeek = getRealDayOfWeek(startDayOfWeek);
        
        c.clear();
        c.set(currentYear, currentMonth - 1, currentDayOfMonth);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        long currentTime = c.getTimeInMillis();
        if (currentTime >= startTime) {
            int totalDays = (int) ((currentTime - startTime) / ONE_DAY);
            int weeks = totalDays / DAY_COUNT_OF_WEEK;
            // 求余
            int modDays = totalDays % DAY_COUNT_OF_WEEK;
            totalWeeks = weeks + 1
                    + ((modDays + startRealDayOfWeek > DAY_COUNT_OF_WEEK) ? 1 : 0);
        }
        return totalWeeks;
    }
    
    public static long getFirstDayMillisOfMonth(int year, int month) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        long timeMillis = c.getTimeInMillis();
        int dayOfFirstWeek = c.get(Calendar.DAY_OF_WEEK);
        int realDayOfFirstWeek = getRealDayOfWeek(dayOfFirstWeek);
        return timeMillis + (1 - realDayOfFirstWeek) * ONE_DAY;
    }

    public static long getRealFirstDayMillisOfMonth(int year, int month) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    public static int getRealDayCountOfMonth(int year, int month) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static long getFirstDayMillisByWeekIndex(long firstDayMillisOfMinWeek, int index) {
        return firstDayMillisOfMinWeek + index * ONE_WEEK;
    }
    
    private static int getRealDayOfWeek(int dayOfWeek) {
        int reelDayIndex = dayOfWeek - 1;
        return reelDayIndex >= 1 ? reelDayIndex : 7;
    }

    public static int getYearByMonthIndex(int minYear, int minMonth, int index) {
        return minYear + (minMonth + index - 1) / MONTH_COUNT_OF_YEAR;
    }

    public static int getMonthByMonthIndex(int minMonth, int index) {
        return (minMonth + index - 1) % MONTH_COUNT_OF_YEAR + 1;
    }

    public static int getMonthIndexBy(int minYear, int minMonth, long timeMillis) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.setTimeInMillis(timeMillis);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        return (year - minYear) * MONTH_COUNT_OF_YEAR + month - minMonth;
    }

    public static int getWeekIndexBy(long startTimeMillis, long timeMillis) {
        return (int) ((timeMillis - startTimeMillis) / ONE_WEEK);
    }
    
    public static boolean isSameDay(long timeMillis1, long timeMillis2) {
        return (timeMillis1 + EIGHT_HOURS) / ONE_DAY == (timeMillis2 + EIGHT_HOURS)
                / ONE_DAY;
    }
    
    public static boolean isToday(long timeMillis) {
        return (timeMillis + EIGHT_HOURS)
                / ONE_DAY == (NetworkTime.currentTimeMillis() + EIGHT_HOURS) / ONE_DAY;
    }
    
    public static boolean isAfterToday(long timeMillis) {
        return (timeMillis + EIGHT_HOURS)
                / ONE_DAY > (NetworkTime.currentTimeMillis() + EIGHT_HOURS) / ONE_DAY;
    }
    
    public static long getTimeInMillis(int year, int month, int day) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        return c.getTimeInMillis();
    }
    
    public static long getTimeInMillis(int year, int month, int day, int field,
            int value) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.add(field, value);
        return c.getTimeInMillis();
    }
    
    public static int getTotalDays(int fromYear, int formMonth, int toYear, int toMonth) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(Calendar.YEAR, fromYear);
        c.set(Calendar.MONTH, formMonth - 1);
        long formTimeMillis = c.getTimeInMillis();
        
        c.clear();
        c.set(Calendar.YEAR, toYear);
        c.set(Calendar.MONTH, toMonth - 1);
        long toTimeMillis = c.getTimeInMillis();
        
        return (int) ((toTimeMillis - formTimeMillis) / ONE_DAY + 1);
    }

    /**
     * 获取某个月有多少天
     *
     * @param month
     *            {@link Calendar#JANUARY} ~ {@link Calendar#DECEMBER}
     * @param year
     *            such as 2016
     */
    public static int daysOfMonth(int year,int month) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                return ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) ? 29
                        : 28;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }
}
