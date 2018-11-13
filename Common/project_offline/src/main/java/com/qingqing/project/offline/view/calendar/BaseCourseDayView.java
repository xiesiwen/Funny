package com.qingqing.project.offline.view.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qingqing.project.offline.course.CourseCalendarDay;
import com.qingqing.qingqingbase.R;

/**
 * Created by huangming on 2017/1/20.
 */

public class BaseCourseDayView extends RelativeLayout {

    protected TextView mDateView;
    protected TextView mStatusView;
    protected CourseCalendarDay mCourseDay;

    public BaseCourseDayView(Context context) {
        super(context);
    }

    public BaseCourseDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseCourseDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDateView = (TextView) findViewById(R.id.tv_course_day_date);
        mStatusView = (TextView) findViewById(R.id.tv_course_day_status);
    }

    protected void onDayChanged(CourseCalendarDay courseDay) {
    }

    public void notifyDayChanged(CourseCalendarDay courseDay) {
        if (!courseDay.equals(mCourseDay)) {
            mCourseDay = courseDay;
            onDayChanged(courseDay);
        }
    }

    public CourseCalendarDay getCourseDay() {
        return mCourseDay;
    }
}
