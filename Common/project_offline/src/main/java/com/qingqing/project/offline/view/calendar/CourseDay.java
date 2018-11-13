package com.qingqing.project.offline.view.calendar;

import java.util.Date;

import com.qingqing.project.offline.view.calendar.Day;

/**
 * @author huangming
 * @date 2015-7-29
 */
public class CourseDay extends Day {
    
    /** 初始状态 */
    public static final int FLAG_NONE = 0x00000000;
    
    /** 有课 */
    public static final int FLAG_HAS_CLASS = 0x00000001;
    
    /** 有课未结:需要提醒 */
    public static final int FLAG_HAS_CLASS_ATTENTIONS = 0x00000002;

    /** 周 */
    public static final int FLAG_IN_WEEK = 0x00000004;
    
    /** 月 */
    public static final int FLAG_IN_MONTH = 0x00000008;

    /** 有课完成 */
    public static final int FLAG_HAS_CLASS_COMPLETE = 0x00000010;

    /** 需要老师回评 */
    public static final int FLAG_NEED_TEACHER_APPRAISE = 0x00000020;
    
    private Day mSelectedDay;
    
    private int mFlag = FLAG_NONE;
    
    public CourseDay(Day day) {
        super(day);
    }
    
    public CourseDay(Date date) {
        super(date);
    }
    
    public CourseDay(int year, int month, int dayOfMonth) {
        super(year, month, dayOfMonth);
    }
    
    public CourseDay(long milliseconds) {
        super(milliseconds);
    }
    
    public void setSelectedDay(Day day) {
        mSelectedDay = day;
    }
    
    @Override
    public boolean isSelected() {
        return mSelectedDay != null && mSelectedDay.getYear() == getYear()
                && mSelectedDay.getMonth() == getMonth()
                && mSelectedDay.getDayOfMonth() == getDayOfMonth();
    }
    
    public void setFlag(int flag) {
        mFlag = flag;
    }
    
    public int getFlag() {
        return mFlag;
    }
    
    @Override
    public void destory() {
        super.destory();
        mSelectedDay = null;
    }
    
}
