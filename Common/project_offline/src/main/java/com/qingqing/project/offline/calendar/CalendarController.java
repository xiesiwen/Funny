package com.qingqing.project.offline.calendar;

import android.util.SparseArray;

import com.qingqing.base.utils.TimeUtil;

/**
 * Created by huangming on 2017/1/18.
 */

public class CalendarController {

    private SparseArray<Day> mDayCache = new SparseArray<>();
    private SparseArray<CalendarWeek> mWeekCache = new SparseArray<>();
    private SparseArray<CalendarMonth> mMonthCache = new SparseArray<>();

    private final int mMinYear, mMinMonth, mMaxYear, mMaxMonth;

    private int mMonthCount, mWeekCount, mRealDayCount;

    private final long mFirstDayMillis;

    private final int mRealFirstDayIndex;
    private final long mRealFirstDayMillis;

    private final int WEEK_COUNT_OF_MONTH = 6;

    public CalendarController(int minYear, int minMonth, int maxYear, int maxMonth) {

        if (minMonth <= 0 || minMonth > TimeUtil.MONTH_COUNT_OF_YEAR) {
            throw new IllegalArgumentException("illegal minMonth(" + minMonth + "), must be between 1 and 12.");
        }

        if (maxMonth <= 0 || maxMonth > TimeUtil.MONTH_COUNT_OF_YEAR) {
            throw new IllegalArgumentException("illegal maxMonth(" + maxMonth + "), must be between 1 and 12.");
        }

        if (minYear > maxYear) {
            throw new IllegalArgumentException("minYear(" + minYear + ") must be less than or equal to maxYear(" + maxYear + ").");
        }

        if (minYear == maxYear && minMonth > maxMonth) {
            throw new IllegalArgumentException("minMonth(" + minMonth + ") must be less than or equal to maxMonth(" + maxMonth + ") , when minYear(" + minYear + ") be equal to maxYear(" + maxYear + ").");
        }

        mMinYear = minYear;
        mMinMonth = minMonth;
        mMaxYear = maxYear;
        mMaxMonth = maxMonth;

        mFirstDayMillis = TimeUtil.getFirstDayMillisOfMonth(minYear, minMonth);

        mRealFirstDayMillis = TimeUtil.getRealFirstDayMillisOfMonth(minYear, minMonth);
        mRealFirstDayIndex = getDayIndexInCalendar(mRealFirstDayMillis);
    }

    public int getMinYear() {
        return mMinYear;
    }

    public int getMinMonth() {
        return mMinMonth;
    }

    public int getMaxYear() {
        return mMaxYear;
    }

    public int getMaxMonth() {
        return mMaxMonth;
    }

    public int getWeekCountOfMonth() {
        return WEEK_COUNT_OF_MONTH;
    }

    public int getDayCountOfWeek() {
        return TimeUtil.DAY_COUNT_OF_WEEK;
    }

    public int getDayCountOfMonth() {
        return getWeekCountOfMonth() * getDayCountOfWeek();
    }


    public long getFirstDayMillis() {
        return mFirstDayMillis;
    }

    public long getRealFirstDayMillis() {
        return mRealFirstDayMillis;
    }

    public int getMonthCount() {
        if (mMonthCount <= 0) {
            mMonthCount = (mMaxYear - mMinYear) * TimeUtil.MONTH_COUNT_OF_YEAR + mMaxMonth - mMinMonth + 1;
        }
        return mMonthCount;
    }

    public int getWeekCount() {
        if (mWeekCount <= 0) {
            mWeekCount = TimeUtil.getTotalWeekCount(mMinYear, mMinMonth, mMaxYear, mMaxMonth);
        }
        return mWeekCount;
    }

    public Day getDayByIndexOfRealCalendar(int index) {
        if (index >= 0 && index < getRealDayCount()) {
            return obtainDay(index + mRealFirstDayIndex);
        }
        return null;
    }

    public Day obtainDay(int index) {
        Day cacheDay = mDayCache.get(index);
        if (cacheDay == null) {
            cacheDay = Day.create(mFirstDayMillis + index * TimeUtil.ONE_DAY);
            mDayCache.put(index, cacheDay);
        }
        return cacheDay;
    }

    public CalendarWeek obtainWeek(int index) {
        if (index < 0 || index >= getWeekCount()) {
            return null;
        }
        CalendarWeek cacheWeek = mWeekCache.get(index);
        if (cacheWeek == null) {
            cacheWeek = new CalendarWeek(this, TimeUtil.getFirstDayMillisByWeekIndex(mFirstDayMillis, index), getDayCountOfWeek());
            mWeekCache.put(index, cacheWeek);
        }
        return cacheWeek;
    }

    public CalendarMonth obtainMonth(int index) {
        if (index < 0 || index >= getMonthCount()) {
            return null;
        }
        CalendarMonth cacheMonth = mMonthCache.get(index);
        if (cacheMonth == null) {
            int year = TimeUtil.getYearByMonthIndex(mMinYear, mMinMonth, index);
            int month = TimeUtil.getMonthByMonthIndex(mMinMonth, index);
            long firstDayMillis = TimeUtil.getFirstDayMillisOfMonth(year, month);
            long realFirstDayMillis = TimeUtil.getRealFirstDayMillisOfMonth(year, month);
            int realDayCountOfMonth = TimeUtil.getRealDayCountOfMonth(year, month);
            cacheMonth = new CalendarMonth(this, firstDayMillis, getDayCountOfMonth(), realFirstDayMillis, realDayCountOfMonth, year, month);
            mMonthCache.put(index, cacheMonth);
        }
        return cacheMonth;
    }

    int getDayIndexInCalendar(long timeMillis) {
        return (int) ((timeMillis - mFirstDayMillis) / TimeUtil.ONE_DAY);
    }

    public int getDayIndexInRealCalendar(long timeMillis) {
        return (int) ((timeMillis - mRealFirstDayMillis) / TimeUtil.ONE_DAY);
    }

    public CalendarMonth getMonthBy(long timeMillis) {
        int monthIndex = getMonthIndexInCalendar(timeMillis);
        return obtainMonth(monthIndex);
    }

    public int getMonthIndexInCalendar(long timeMillis) {
        return TimeUtil.getMonthIndexBy(mMinYear, mMinMonth, timeMillis);
    }

    public CalendarWeek getWeekBy(long timeMillis) {
        int weekIndex = getWeekIndexInCalendar(timeMillis);
        return obtainWeek(weekIndex);
    }

    public int getWeekIndexInCalendar(long timeMillis) {
        return TimeUtil.getWeekIndexBy(mFirstDayMillis, timeMillis);
    }

    public boolean isDayInRealRange(long timeMillis) {
        int monthIndex = getMonthIndexInCalendar(timeMillis);
        return monthIndex >= 0 && monthIndex < getMonthCount();
    }

    public int getRealDayCount() {
        if (mRealDayCount <= 0) {
            mRealDayCount = TimeUtil.getTotalDays(mMinYear, mMinMonth, mMaxYear, mMaxMonth);
        }
        return mRealDayCount;
    }

    public int getRealFirstDayIndex() {
        return mRealFirstDayIndex;
    }

    public static CalendarController getDefault() {
        return new CalendarController(2010, 1, 2030, 12);
    }

}
