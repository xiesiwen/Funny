package com.qingqing.project.offline.calendar;

import android.util.SparseArray;

/**
 * Created by huangming on 2017/1/19.
 */

public class CalendarWeek {

    private final CalendarController controller;
    private final int firstDayIndexInCalendar;
    private final int dayCountOfWeek;
    private final SparseArray<CalendarDay> dayCache;

    public CalendarWeek(CalendarController controller, long firstDayMillis, int dayCountOfWeek) {
        this.controller = controller;
        this.firstDayIndexInCalendar = controller.getDayIndexInCalendar(firstDayMillis);
        this.dayCountOfWeek = dayCountOfWeek;

        this.dayCache = new SparseArray<>(dayCountOfWeek);
    }

    public int getDayIndexInCalendar(int dayIndexInWeek) {
        if (dayIndexInWeek >= dayCountOfWeek) {
            throw new IllegalArgumentException("dayIndexInWeek(" + dayIndexInWeek + "), dayCountOfWeek(" + dayCountOfWeek + ")");
        }
        return dayIndexInWeek + firstDayIndexInCalendar;
    }

    public int getFirstDayIndexInCalendar() {
        return firstDayIndexInCalendar;
    }

    public int getLastDayIndexInCalendar() {
        return firstDayIndexInCalendar + dayCountOfWeek - 1;
    }

    public CalendarDay getDayBy(int dayIndexInWeek) {
        CalendarDay calendarDay = dayCache.get(dayIndexInWeek);
        if (calendarDay == null) {
            Day day = controller.obtainDay(getDayIndexInCalendar(dayIndexInWeek));
            calendarDay = CalendarDay.createWeekViewDay(this, day);
            dayCache.put(dayIndexInWeek, calendarDay);
        }
        return calendarDay;
    }

}
