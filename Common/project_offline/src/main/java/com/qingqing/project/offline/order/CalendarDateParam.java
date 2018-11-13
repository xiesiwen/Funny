package com.qingqing.project.offline.order;

import java.util.Calendar;

/**
 * Created by wangxiaxin on 2017/8/8.
 *
 * 选时间的日历数据
 */
public class CalendarDateParam {
    public int year;
    public int month;
    public int day;

    public CalendarDateParam(int year, int month) {
        this.year = year;
        this.month = month;
        this.day = 1;
    }

    public CalendarDateParam(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public CalendarDateParam nextMonth() {
        if (month == Calendar.DECEMBER) {
            return new CalendarDateParam(year + 1, Calendar.JANUARY);
        }
        else {
            return new CalendarDateParam(year, month + 1);
        }
    }
    public CalendarDateParam nextWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        currentDay += 7 ;
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.DAY_OF_YEAR,currentDay);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new CalendarDateParam(year, month , day);
    }
}
