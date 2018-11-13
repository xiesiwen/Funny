package com.qingqing.project.offline.calendar;

import android.util.SparseArray;

/**
 * Created by huangming on 2017/1/19.
 */

public class CalendarMonth {

    private final CalendarController controller;
    private final int firstDayIndexInCalendar;
    private final int dayCountOfMonth;

    private final int realFirstDayIndexInCalendar;
    private final int realDayCountOfMonth;
    private final SparseArray<CalendarDay> dayCache;

    private final int year;
    private final int monthOfYear;

    CalendarMonth(CalendarController controller, long firstDayMillis, int dayCountOfMonth, long realFirstDayMillis, int realDayCountOfMonth, int year, int monthOfYear) {
        this.controller = controller;
        this.dayCountOfMonth = dayCountOfMonth;
        this.year = year;
        this.monthOfYear = monthOfYear;
        this.firstDayIndexInCalendar = controller.getDayIndexInCalendar(firstDayMillis);

        this.realDayCountOfMonth = realDayCountOfMonth;
        this.realFirstDayIndexInCalendar = controller.getDayIndexInCalendar(realFirstDayMillis);

        this.dayCache = new SparseArray<>();

    }

    public int getYear() {
        return year;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }

    public int getRealFirstDayIndexInCalendar() {
        return realFirstDayIndexInCalendar;
    }

    public boolean isDayInRealMonth(long timeMillis) {
        int dayIndexInCalendar = controller.getDayIndexInCalendar(timeMillis);
        return dayIndexInCalendar >= realFirstDayIndexInCalendar && dayIndexInCalendar < realFirstDayIndexInCalendar + realDayCountOfMonth;

    }

    private int getDayIndexInCalendar(int dayIndexInMonth) {
        if (dayIndexInMonth >= dayCountOfMonth) {
            throw new IllegalArgumentException("dayIndexInMonth(" + dayIndexInMonth + "), dayCountOfMonth(" + dayCountOfMonth + ")");
        }
        return dayIndexInMonth + firstDayIndexInCalendar;
    }

    public int getWeekIndex(long timeMillis) {
        int dayIndexInCalendar = controller.getDayIndexInCalendar(timeMillis);
        return (dayIndexInCalendar - firstDayIndexInCalendar) / controller.getDayCountOfWeek();
    }

    public CalendarDay getDayBy(int dayIndexInMonth) {
        CalendarDay cacheDay = dayCache.get(dayIndexInMonth);
        if (cacheDay == null) {
            Day day = controller.obtainDay(getDayIndexInCalendar(dayIndexInMonth));
            cacheDay = CalendarDay.createMonthViewDay(this, day);
            dayCache.put(dayIndexInMonth, cacheDay);
        }
        return cacheDay;
    }

}
