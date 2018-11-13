package com.qingqing.project.offline.view.ncalendar;

import android.view.View;
import android.view.ViewGroup;
import com.qingqing.base.view.pager.PagedAdapter;

/**
 * Created by huangming on 2017/2/6.
 */

public class CalendarDayPagedAdapter extends PagedAdapter {

    private final CalendarAdapter mRootAdapter;

    public CalendarDayPagedAdapter(CalendarAdapter rootAdapter) {
        this.mRootAdapter = rootAdapter;
    }

    @Override
    public int getCount() {
        return mRootAdapter.getController().getRealDayCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mRootAdapter.getDayPageView(position, convertView, parent);
    }

}
