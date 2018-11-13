package com.qingqing.project.offline.course;

import com.qingqing.project.offline.calendar.CalendarDay;
import com.qingqing.project.offline.calendar.CalendarViewType;
import com.qingqing.base.utils.Objects;

/**
 * Created by huangming on 2017/1/18.
 */

public class CourseCalendarDay {

    private final CalendarDay calendarDay;
    private final CalendarViewType calendarViewType;
    private final Status status;
    private final boolean selected;
    private double courseCount = 0d;

    public CourseCalendarDay(CalendarDay calendarDay, CalendarViewType calendarViewType, Status status, boolean selected,double courseCount) {
        this.calendarDay = calendarDay;
        this.calendarViewType = calendarViewType;
        this.status = status;
        this.selected = selected;
        this.courseCount = courseCount;
    }

    public CalendarDay getCalendarDay() {
        return calendarDay;
    }

    public CalendarViewType getCalendarViewType() {
        return calendarViewType;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSelected() {
        return selected;
    }

    public double getCourseCount(){
        return courseCount;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof CourseCalendarDay) {
            CourseCalendarDay other = (CourseCalendarDay) o;
            return Objects.equals(this.calendarDay, other.calendarDay)
                    && Objects.equals(this.calendarViewType, other.calendarViewType)
                    && Objects.equals(this.status, other.status)
                    && Objects.equals(this.courseCount, other.courseCount)
                    && Objects.equals(this.selected, other.selected);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(calendarDay, calendarViewType, status, selected,courseCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CourseCalendarDay(");
        if (calendarDay != null) {
            sb.append("\n").append(calendarDay);
        }
        if (calendarViewType != null) {
            sb.append("\ncalendarViewType=").append(calendarViewType);
        }
        if (status != null) {
            sb.append("\nstatus=").append(status);
        }
        sb.append("\n").append(selected);
        sb.append("\n)");
        return sb.toString();
    }

    public enum Status {

        NONE,
        /**
         * 有课
         */
        HAS_CLASS,
        /**
         * 有课，未结束
         */
        HAS_CLASS_NO_END,
        /**
         * 有课，已完成
         */
        HAS_CLASS_COMPLETE,

    }
}
