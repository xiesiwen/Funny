package com.qingqing.project.offline.course;

import android.content.Context;

import com.qingqing.project.offline.calendar.CalendarController;
import com.qingqing.project.offline.view.ncalendar.CalendarAdapter;


/**
 * Created by huangming on 2017/1/20.
 */

public class CourseCalendarAdapter<T> extends CalendarAdapter<T> {

    public CourseCalendarAdapter(Context context, PresenterView<T> presenterView) {
        super(context, CalendarController.getDefault(), presenterView);
    }

    @Override
    public int getCellWidth() {
        return mContext.getResources().getDimensionPixelSize(com.qingqing.project.offline.R.dimen.dimen_48);
    }

    @Override
    public int getCellHeight() {
        return mContext.getResources().getDimensionPixelSize(com.qingqing.project.offline.R.dimen.dimen_48);
    }

    @Override
    public int getCellWidthGap() {
        return 0;
    }

    @Override
    public int getCellHeightGap() {
        return 0;
    }

    @Override
    public DayCellViewHolder onCreateDayCellViewHolder() {
        return null;
    }
}
