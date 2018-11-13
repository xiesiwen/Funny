package com.qingqing.project.offline.calendar;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by huangming on 2017/2/3.
 */

public class Day {

    private final int key;
    private final int year;
    private final int month;
    private final int dayOfMonth;
    private final int dayOfWeek;
    private final long timeMillis;

    private Day(int year, int month, int dayOfMonth, int dayOfWeek, long timeMillis) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.timeMillis = timeMillis;
        this.key = year * 10000 + month * 100 + dayOfMonth;
    }

    public Day(Day other) {
        this.year = other.year;
        this.month = other.month;
        this.dayOfMonth = other.dayOfMonth;
        this.dayOfWeek = other.dayOfWeek;
        this.timeMillis = other.timeMillis;
        this.key = other.key;
    }

    public int getKey() {
        return key;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Day) {
            Day other = (Day) o;
            return other.year == this.year && other.month == this.month && other.dayOfMonth == this.dayOfMonth;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + year;
        result = 37 * result + month;
        result = 37 * result + dayOfMonth;
        return result;
    }

    @Override
    public String toString() {
        return "Day(" + year + "-" + month + "-" + dayOfMonth + ")";
    }

    public static Day create(int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return new Day(year, month, dayOfMonth, c.get(Calendar.DAY_OF_WEEK), c.getTimeInMillis());
    }

    public static Day create(long timeMillis) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.clear();
        c.setTimeInMillis(timeMillis);
        return new Day(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_WEEK), timeMillis);
    }

    public static Day create(Day other) {
        return new Day(other);
    }

}
