package com.qingqing.project.offline.view.calendar;

import java.util.List;
import android.content.Context;

/**
 * @author huangming
 * @date 2015-7-27
 */
public abstract class WeekPagerAdapter extends DayPagerAdapter {

    public WeekPagerAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return CalendarController.WEEK_COUNT;
    }

    public List<Day> createDays(int position) {
        return CalendarController.getWeekDaysByIndex(position);
    }
    
}
