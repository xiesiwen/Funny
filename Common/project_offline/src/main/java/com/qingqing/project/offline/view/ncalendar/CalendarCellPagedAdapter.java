package com.qingqing.project.offline.view.ncalendar;

import android.view.View;
import android.view.ViewGroup;

import com.qingqing.project.offline.calendar.CalendarViewType;
import com.qingqing.base.view.pager.PagedAdapter;

/**
 * Created by huangming on 2017/1/19.
 */

public class CalendarCellPagedAdapter extends PagedAdapter {

    private final CalendarAdapter mRootAdapter;
    private final CalendarViewType mViewType;

    public CalendarCellPagedAdapter(CalendarAdapter rootAdapter, CalendarViewType viewType) {
        this.mRootAdapter = rootAdapter;
        this.mViewType = viewType;
    }

    @Override
    public int getCount() {
        return mRootAdapter.getPageCountBy(mViewType);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mRootAdapter.getDayCellView(position, mViewType, convertView, parent);
    }


}
