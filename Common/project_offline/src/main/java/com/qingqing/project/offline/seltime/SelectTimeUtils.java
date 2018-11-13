package com.qingqing.project.offline.seltime;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.qingqing.api.proto.v1.Time;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.project.offline.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 封装部分对时间段和日期的操作
 *
 * <b>区别：block表示时间块下标，index表示展示时间块ui的view的下标</b>
 *
 * @author tanwei
 * 
 */
public class SelectTimeUtils {

    /** block=0对应的起点小时 */
    public static final int START_HOUR = 8;

    // ---------选择时间使用场景
    /** 智能匹配试听 */
    public static final int SCENE_TYPE_MATCH_AUDITION = -1;
    /** 试听 */
    public static final int SCENE_TYPE_AUDITION = 1;
    /** 智能匹配 */
    public static final int SCENE_TYPE_MATCH = 2;
    /** 找老师下单 */
    public static final int SCENE_TYPE_ORDER = 3;
    /** 续课 */
    public static final int SCENE_TYPE_RENEW_ORDER = 4;
    /** 调课 */
    public static final int SCENE_TYPE_CHANGE_ORDER = 5;
    /** 老师开课 */
    public static final int SCENE_TYPE_SET_TEACHER_TIME = 6;
    // ---------end

    /** yyyy-MM-dd */
    public static final String FORMAT_KEY = "yyyy-MM-dd";
    
    /** 07月07日 */
    public static final String FORMAT_MONTH_DAY = "MM月dd日";
    /** title时间格式 *2015年6月1号* */
    public static final String FORMAT_YEAR_MONTH_DAY = "yyyy年M月d日";
    /** title时间格式 *2015年06月01号* */
    public static final String FORMAT_YEAR_MONTH_DAY_2 = "yyyy年MM月dd日";
    /** 已选择的时间格式 */
    public static final String FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE = "yy年MM月d日HH:mm";
    public static final String FORMAT_HOUR_MINUTE = "HH:mm";

    /** 已选择的正式课时间格式 */
    private static final String FORMAT_SELECTED_TIME_FORMAL = "%s至%s 正课%.1f小时";
    /** 已选择的试听课时间格式 */
    private static final String FORMAT_SELECTED_TIME_AUDITION = "%s至%s 试听课%.1f小时";
    private static final String FORMAT_TIME_SLICE_CONTENT_SINGLE = "%s\n至\n%s";
    private static final String FORMAT_INDEX_HOUR_MINUTE = "%02d:%02d";
    private static Calendar cal = Calendar.getInstance();
    
    private static SimpleDateFormat ymdTitle;
    
    private static SimpleDateFormat ymd;
    
    private static SimpleDateFormat ymdhm;
    
    private static SimpleDateFormat hm;
    
    private static SimpleDateFormat md;
    
    private static SimpleDateFormat yyyymmdd;
    
    public static String formatDateForLog(final Date date) {
        if (ymdhm == null) {
            ymdhm = new SimpleDateFormat(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE, Locale.CHINA);
        }
        
        return ymdhm.format(date);
    }
    
    public static String formatDateForLog(final long date) {
        return formatDateForLog(new Date(date));
    }
    
    /** 根据时间段下标获取时间间隔字符串，形如：08:00至08:30 */
    public static String getSliceContentByBlock(int block) {
        String content;

        int startBlock = block + START_HOUR * 2;
        int startHour = startBlock / 2;

        String start;
        String end;
        
        if (startBlock % 2 == 0) {
            start = String.format(FORMAT_INDEX_HOUR_MINUTE, startHour, 0);
            end = String.format(FORMAT_INDEX_HOUR_MINUTE, startHour, 30);
        }
        else {
            start = String.format(FORMAT_INDEX_HOUR_MINUTE, startHour, 30);
            end = String.format(FORMAT_INDEX_HOUR_MINUTE, startHour + 1, 0);
        }
        
        content = String.format(FORMAT_TIME_SLICE_CONTENT_SINGLE, start, end);
        
        return content;
    }
    
    /** 根据连续多个时间段获取时间间隔字符串，形如：yy年MM月d日HH:mm至HH:mm 正课2小时 */
    public static String getContentByTimeSlice(boolean isFree, TimeSlice time) {
        
        if (ymdhm == null) {
            ymdhm = new SimpleDateFormat(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE, Locale.CHINA);
        }
        if (hm == null) {
            hm = new SimpleDateFormat(FORMAT_HOUR_MINUTE, Locale.CHINA);
        }
        
        String content;
        
        String start = ymdhm.format(time.getStartDate());
        String end = hm.format(time.getEndDate());
        
        float hour = time.getTimeInHour();
        
        if (isFree) {
            content = String.format(FORMAT_SELECTED_TIME_AUDITION, start, end, hour);
        }
        else {
            content = String.format(FORMAT_SELECTED_TIME_FORMAL, start, end, hour);
        }
        
        return replaceDateFormatTo24(content);
    }
    
    /** 获取星期 */
    public static WeekDay getWeekDay(final Date date) {
        cal.clear();
        cal.setTime(date);
        int week = cal.get(Calendar.DAY_OF_WEEK) - 2;
        if (week < 0) {
            week += 7;
        }
        return WeekDay.valueOf(week);
    }

    /** 将WeekDay对应的week转化为Calendar对应的week */
    public static int weekDayToCalendarWeek(int weekday) {
        int week = weekday + 2;
        if (week > 7) {
            week -= 7;
        }

        return week;
    }

    /** 将Calendar对应的week转化为WeekDay对应的week */
    public static int calendarWeekToWeekDay(int week) {
        
        int weekday = week - 2;
        if (weekday < 0) {
            weekday += 7;
        }
        return weekday;
    }
    
    /** 获取 天 */
    public static int getDayOfMonth(final Date date) {
        cal.clear();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
    
    /** 获取日期所在星期的星期一 */
    public static Date getMondayOfWeek(final Date date) {
        cal.clear();
        cal.setTime(date);
        int week = cal.get(Calendar.DAY_OF_WEEK) - 2;
        if (week < 0) {
            week += 7;
        }
        if (week > 0) {
            
            cal.add(Calendar.DAY_OF_MONTH, 0 - week);
            return cal.getTime();
        }
        else {
            return date;
        }
    }

    /** 增加时间块代表长度：半个小时 */
    public static Date addSlice(final Date date) {
        cal.clear();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, 30);
        return cal.getTime();
    }
    
    /** 根据日期计算出星期的下标对应的日期 */
    public static Date getDateByIndex(final Date date, int index) {
        int week = SelectTimeUtils.getWeekDay(date).value();
        int delt = index - week;
        cal.clear();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, delt);
        return cal.getTime();
    }

    /** 获取指定日期之后的指定星期对应的日期（含指定日期） */
    public static Date getNextDateByIndex(Date date, int week){
        Date nextDate = getDateByIndex(date, week);
        if(diffDaysOf2Dates(date, nextDate) == 1){
            cal.clear();
            cal.setTime(nextDate);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            return cal.getTime();
        }else{
            return nextDate;
        }
    }

    /** 获取指定日期之后的指定星期对应的日期(不含指定的日期) */
    public static Date getNextDateByIndexExclusive(Date date, int week) {
        Date nextDate = getDateByIndex(date, week);
        if (diffDaysOf2Dates(date, nextDate) == -1) {
            return nextDate;
        }
        else {
            cal.clear();
            cal.setTime(nextDate);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            return cal.getTime();
        }
    }
    
    public static Date addDay(final Date date, int value) {
        cal.clear();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, value);
        return cal.getTime();
    }
    
    /** 展示日期 形如2015年6月1号 */
    public static String getTimeTitle(final Date date) {
        
        if (ymdTitle == null) {
            ymdTitle = new SimpleDateFormat(FORMAT_YEAR_MONTH_DAY, Locale.CHINA);
        }
        
        return ymdTitle.format(date);
    }
    
    /** yyyy-MM-dd 用作区分时间段的key值 */
    public static String getDateFormatYMD(final Date date) {
        if (ymd == null) {
            ymd = new SimpleDateFormat(FORMAT_KEY, Locale.CHINA);
        }
        
        return ymd.format(date);
    }

    public static Calendar getTimeByBlock(int block) {
        Date date = new Date();
        Calendar cc = Calendar.getInstance();
        cc.setTime(date);
        cc.set(Calendar.HOUR_OF_DAY, 8);
        cc.set(Calendar.MINUTE, 0);
        cc.add(Calendar.MINUTE, block * 30);
        return cc;
    }

    /** 将格式化日期yyyy-MM-dd 解析为具体的Date */
    public static Date parseDateByFormatYMD(String dateString){
        if (ymd == null) {
            ymd = new SimpleDateFormat(FORMAT_KEY, Locale.CHINA);
        }

        Date date = null;
        if (!TextUtils.isEmpty(dateString)) {

            try {
                return ymd.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(date == null){
            date = new Date(NetworkTime.currentTimeMillis());
        }

        return date;
    }

    /** TimeParam --> TimeSlice */
    public static TimeSlice parseToTimeSlice(Time.TimeParam param) {
        Date date = SelectTimeUtils.parseDateByFormatYMD(param.date);
        return SelectTimeUtils.getTimeSliceMulti(param.startBlock, param.endBlock, date);
    }
    
    /** 比较2个日期是否为同一天 */
    public static boolean isDateEquals(final Date date1, final long date2) {
        
        return diffDaysOf2Dates(date1, date2) == 0;
    }
    
    /** 比较2个日期是否为同一天 */
    public static boolean isDateEquals(final Date date1, final Date date2) {
        
        return diffDaysOf2Dates(date1, date2) == 0;
    }
    
    /** 根据下标获取当天多个时间段（TimeSlice） */
    public static TimeSlice getTimeSliceMulti(final int start, final int end,
            final Date date) {
        cal.clear();
        cal.setTime(date);
        int startBlock = start + START_HOUR * 2;
        int endBlock = end + START_HOUR * 2;
        int startHour = startBlock / 2;
        int endHour = endBlock / 2;
        
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, startHour);
        if (startBlock % 2 == 0) {
            cal.set(Calendar.MINUTE, 0);
        }
        else {
            cal.set(Calendar.MINUTE, 30);
        }
        Date startDate = cal.getTime();
        
        cal.set(Calendar.HOUR_OF_DAY, endHour);
        if (endBlock % 2 == 0) {
            cal.set(Calendar.MINUTE, 0);
        }
        else {
            cal.set(Calendar.MINUTE, 30);
        }
        cal.add(Calendar.MINUTE, 30);// 结束时间需要加上时间段的长度
        
        return new TimeSlice(start, end, startDate, cal.getTime());
    }
    
    /** 根据当前时间获取相邻的下一个时间段下标 */
    public static int getNextAvailableBlock(final Date date, long time) {
        
        int block;
        
        int diff = diffDaysOf2Dates(date, time);
        
        if (diff == 0) {
            
            cal.setTimeInMillis(time);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);

            block = hour * 2;

            if (min > 30) {
                block += 2;
            }
            else {
                block += 1;
            }

            block -= START_HOUR * 2;
            
            if (block <= SelectTimeHelper.sSliceStartBlock) {
                block = SelectTimeHelper.sSliceStartBlock;
            }

        }
        else if (diff == 1) {
            block = SelectTimeHelper.sSliceStartBlock;
        }
        else {
            block = SelectTimeHelper.sSliceEndBlock + 1;
        }
        
        
        return block;
    }
    
    /** 比较2个日期相差的天数，1表示date1比date2晚一天以上，-1表示早一天以上, 0表示同一天 */
    public static int diffDaysOf2Dates(final Date date, long time) {
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time);

        return diffDays(cal1, cal2);
    }
    
    /** 比较2个日期相差的天数，1表示date1比date2晚一天以上，-1表示早一天以上, 0表示同一天 */
    public static int diffDaysOf2Dates(final Date date1, final Date date2) {
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        
        return diffDays(cal1, cal2);
    }
    
    private static int diffDays(final Calendar cal1, final Calendar cal2) {
        
        int diff;
        
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 == year2) {
            int day1 = cal1.get(Calendar.DAY_OF_YEAR);
            int day2 = cal2.get(Calendar.DAY_OF_YEAR);
            if (day1 == day2) {
                diff = 0;
            }
            else if (day1 > day2) {
                diff = 1;
            }
            else {
                diff = -1;
            }
        }
        else if (year1 > year2) {
            diff = 1;
        }
        else {
            diff = -1;
        }
        
        return diff;
    }
    
    /** 给定时间在当前日期是否有可用时间段 */
    public static boolean isTimeAvailable(Date date, long time) {
        cal.clear();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.HOUR_OF_DAY, SelectTimeHelper.sSliceEndHour - 1);

        return cal.getTimeInMillis() > time;
    }
    
    /** 根据连续多个时间段获取时间间隔字符串，形如：MM月d日 周二 HH:mm-HH:mm */
    public static String getContentByTimeSlice(TimeSlice time) {
        
        if (md == null) {
            md = new SimpleDateFormat(FORMAT_MONTH_DAY, Locale.CHINA);
        }
        if (hm == null) {
            hm = new SimpleDateFormat(FORMAT_HOUR_MINUTE, Locale.CHINA);
        }
        if (yyyymmdd == null) {
            yyyymmdd = new SimpleDateFormat(FORMAT_YEAR_MONTH_DAY_2, Locale.CHINA);
        }

        // 5.9.6新增：不是今年显示年份
        String date;
        if (isTimeInCurrentYear(time.getStartDate())) {
            date = md.format(time.getStartDate());
        }
        else {
            date = yyyymmdd.format(time.getStartDate());
        }
        
        StringBuilder sb = new StringBuilder(date);
        sb.append(" ");
        sb.append(WeekDay.getWeekStringSimple(getWeekDay(time.getStartDate()).value()));
        sb.append(" ");
        sb.append(hm.format(time.getStartDate()));
        sb.append("-");
        sb.append(hm.format(time.getEndDate()));
        
        return replaceDateFormatTo24(sb.toString());
    }

    /** 判断指定时间是否在今年 */
    public static boolean isTimeInCurrentYear(Date date) {
        
        return isTimeInSameYear(NetworkTime.currentTimeMillis(), date);
    }
    
    /** 判断指定时间是否在同一年 */
    public static boolean isTimeInSameYear(long currentTimeMills, Date date) {
        
        cal.clear();
        cal.setTime(date);
        
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(currentTimeMills);
        return cal.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR);
    }

    /** 上课时间 */
    public static String getClassTimeByTimeSlice(TimeSlice time) {

        if (md == null) {
            md = new SimpleDateFormat(FORMAT_MONTH_DAY, Locale.CHINA);
        }
        if (hm == null) {
            hm = new SimpleDateFormat(FORMAT_HOUR_MINUTE, Locale.CHINA);
        }

        String date = md.format(time.getStartDate());

        StringBuilder sb = new StringBuilder(date);
        sb.append(" ");
        sb.append(hm.format(time.getStartDate()));
        sb.append("-");
        sb.append(hm.format(time.getEndDate()));

        return replaceDateFormatTo24(sb.toString());
    }
    /** 将00:00改为24:00 */
    public static String replaceDateFormatTo24(String dateFormat) {
        return dateFormat.replace("00:00", "24:00");
    }

    /** 初始化时间块状态SparseIntArray */
    public static void initTimeStatus(SparseIntArray array) {
        fillTimeWithStatus(array, TimeSliceView.STATUS_INVALID);
    }

    /** 以指定状态填充时间块状态SparseIntArray */
    public static void fillTimeWithStatus(SparseIntArray array, int status) {
        int start = SelectTimeHelper.sSliceStartBlock;
        int end = SelectTimeHelper.sSliceCount;
        for (int i = 0; i < end; i++) {
            array.put(start + i, status);
        }
    }

    /** 复制SparseIntArray */
    public static void copySparseArray(SparseIntArray src, SparseIntArray dst) {
        int size = SelectTimeHelper.sSliceCount;
        for (int i = 0; i < size; i++) {
            dst.put(src.keyAt(i), src.valueAt(i));
        }
    }

    /** 将时间点11:30转换成block */
    public static int parseTimeToBlock(String time){
        String[] hm = time.split(":");
        int hour = Integer.parseInt(hm[0]);
        int min = Integer.parseInt(hm[1]);

        int block = (hour - START_HOUR) * 2;

        if (min == 30) {
            block += 1;
        }

        return block;
    }

    /** 将block转换成时间点11:30 */
    public static String getHMbyBlock(int block){
        return getHMbyBlock(block, true);
    }

    /** 将block转换成时间点11:30, isStart表示获取起点时间 */
    public static String getHMbyBlock(int block, boolean isStart) {
        int startBlock = block + START_HOUR * 2;
        int startHour = startBlock / 2;
        
        if (isStart) {
            String start;
            if (startBlock % 2 == 0) {
                start = String.format(FORMAT_INDEX_HOUR_MINUTE, startHour, 0);
            }
            else {
                start = String.format(FORMAT_INDEX_HOUR_MINUTE, startHour, 30);
            }
            
            return start;
        }
        else {
            String end;
            
            if (startBlock % 2 == 0) {
                end = String.format(FORMAT_INDEX_HOUR_MINUTE, startHour, 30);
            }
            else {
                end = String.format(FORMAT_INDEX_HOUR_MINUTE, startHour + 1, 0);
            }
            
            return end;
        }
        
    }

    /**
     * 返回形如 “周一上午” “周三晚上”之类的字串
     *
     * @param week 星期 0表示星期一 往后类推
     * @param timeRange {@linkplain Time.DayTimeRange}
     * @return
     */
    public static String getContentByNewTime(Context context, int week, int timeRange) {
        String weekStr = WeekDay.getWeekStringSimple(week);
        String timeRangeStr = getContentByTimeRange(context, timeRange);
        
        return weekStr + timeRangeStr;
    }

    /**
     * 返回形如 “上午” “下午” “晚上”之类的字串
     * 
     * @param timeRange
     *            {@linkplain Time.DayTimeRange}
     * @return
     */
    public static String getContentByTimeRange(Context context, int timeRange) {
        switch (timeRange) {
            case Time.DayTimeRange.morning_day_time_range:
                return context.getString(R.string.text_time_range_morning);
            
            case Time.DayTimeRange.afternoon_day_time_range:
                return context.getString(R.string.text_time_range_afternoon);
            case Time.DayTimeRange.evening_day_time_range:
                return context.getString(R.string.text_time_range_evening);
        }
        
        return "";
    }

    /** 判断2个时间是否冲突（重叠） */
    public static boolean checkTimeConflict(TimeSlice time1, TimeSlice time2) {
        final long start1 = time1.getStartDate().getTime();
        final long end1 = time1.getEndDate().getTime();
        final long start2 = time2.getStartDate().getTime();
        final long end2 = time2.getEndDate().getTime();

        return (start1 > start2 && start1 < end2) || (end1 > start2 && end1 < end2)
                || (start1 <= start2 && end1 >= end2);
    }

    /** TimeSlice --> TimeParam */
    public static Time.TimeParam createTimeParamFromTimeSlice(TimeSlice time) {
        Time.TimeParam param = new Time.TimeParam();
        param.date = getDateFormatYMD(time.getStartDate());
        param.startBlock = time.getStart();
        param.endBlock = time.getEnd();
        
        return param;
    }
}
