package com.qingqing.project.offline.view.calendar;

import java.util.List;
import android.content.Context;

/**
 * @author huangming
 * @date 2015-7-27
 */
public abstract class MonthPagerAdapter extends DayPagerAdapter {
    
    public MonthPagerAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return CalendarController.MONTH_COUNT;
    }

    public List<Day> createDays(int position) {
        return CalendarController.getMonthDaysByIndex(position);
    }
    
}
