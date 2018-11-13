package com.qingqing.project.offline.calendar;


import com.qingqing.base.utils.Objects;

/**
 * Created by huangming on 2017/1/18.
 */

public class CalendarDay {

    private final Day day;
    private final CalendarMonth month;
    private final CalendarWeek week;

    private CalendarDay(CalendarMonth month, CalendarWeek week, Day day) {
        this.day = day;
        this.month = month;
        this.week = week;
    }

    public Day getDay() {
        return day;
    }

    public CalendarWeek getWeek() {
        return week;
    }

    public CalendarMonth getMonth() {
        return month;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Day) {
            CalendarDay other = (CalendarDay) o;
            return Objects.equals(day, other.day) && Objects.equals(month, other.month) && Objects.equals(week, other.week);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, month, week);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CalendarDay(");
        if(day != null) {
            sb.append("\n").append(day);
        }
        if(month != null) {
            sb.append("\n").append(month);
        }
        if(week != null) {
            sb.append("\n").append(week);
        }
        return sb.toString();
    }

    public static CalendarDay createMonthViewDay(CalendarMonth month, Day day) {
        return new CalendarDay(month, null, day);
    }

    public static CalendarDay createWeekViewDay(CalendarWeek week, Day day) {
        return new CalendarDay(null, week, day);
    }

}
