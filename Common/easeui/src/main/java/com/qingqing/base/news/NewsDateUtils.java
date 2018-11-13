package com.qingqing.base.news;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 消息时间格式
 *
 * Created by huangming on 2016/12/28.
 */

public class NewsDateUtils {

    private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;

    private NewsDateUtils() {

    }

    /**
     * 昨天以前的日期返回yyyy年MM月dd日格式
     *
     * @param time time
     * @return
     */
    public static String getDateText(long time) {
        if (time <= 0) {
            return "";
        }
        SimpleDateFormat format;
        Date date = new Date(time);
        int diff = diffDaysFromNow(time);
        if (diff == 0) {
            format = DateUtils.hmSdf;
        }
        else if (diff == -1) {
            format = DateUtils.yesterdayhourminuteFormat;
        }
        else {
            format = DateUtils.mYearAndMonthAndDateFormat;
        }
        return format.format(date);
    }

    private static int diffDaysFromNow(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Calendar calendarCurrent = Calendar.getInstance();
        calendarCurrent.setTimeInMillis(NetworkTime.currentTimeMillis());
        
        return calendar.get(Calendar.DAY_OF_YEAR)
                - calendarCurrent.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 消息时间格式调整：
     * 当天消息：##:## （小时：分钟)
     * 前一天消息：昨天
     * 本周消息：周#（周一、周二、周三、周四、周五、周六、周日）
     * 今年的消息：##-##（月-日）
     * 今年之前的消息：####-##-##（年-月-日）
     *
     * @param time time
     *
     * @return
     */
    public static String getDateTextNew(long time) {

        if (time <= 0) {
            return "";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Calendar calendarCurrent = Calendar.getInstance();
        calendarCurrent.setTimeInMillis(NetworkTime.currentTimeMillis());

        // 指定时间和当前时间的天数差（注意非今天是负数）
        int diffDays = calendar.get(Calendar.DAY_OF_YEAR)
                - calendarCurrent.get(Calendar.DAY_OF_YEAR);
        // 指定时间和当前时间的周数差（注意非本周是负数）
        int diffWeeks = calendar.get(Calendar.WEEK_OF_YEAR)
                - calendarCurrent.get(Calendar.WEEK_OF_YEAR);
        // 指定时间和当前时间的年数数差（注意非今年是负数）
        int diffYears = calendar.get(Calendar.YEAR) - calendarCurrent.get(Calendar.YEAR);

        String text;
        Date date = new Date(time);
        if (diffYears == 0) {
            switch (diffDays) {
                case 0:
                    text = DateUtils.hmSdf.format(date);
                    break;

                case -1:
                    text = BaseApplication.getCtx().getString(
                            com.qingqing.qingqingbase.R.string.text_time_yesterday);
                    break;

                default:

                    if (diffWeeks == 0) {
                        int week = calendar.get(Calendar.DAY_OF_WEEK);
                        String[] weekStrs = BaseApplication.getCtx().getResources()
                                .getStringArray(
                                        com.qingqing.qingqingbase.R.array.week_array);

                        int index = week - 2;
                        if (index < 0) {
                            index += 7;
                        }
                        text = weekStrs[index];
                    }
                    else {
                        text = DateUtils.monthdayFormat.format(date);
                    }

                    break;
            }
        }
        else {
            text = DateUtils.ymdSdf.format(date);
        }

        return text;
    }
}
