package com.qingqing.project.offline.view.ncalendar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.qingqing.project.offline.calendar.CalendarViewType;

/**
 * Created by huangming on 2017/1/19.
 */

public class CalendarDayCellAdapter extends BaseAdapter {

    private final CalendarAdapter mRootAdapter;
    private final CalendarViewType mViewType;

    public CalendarDayCellAdapter(CalendarAdapter rootAdapter, CalendarViewType viewType) {
        this.mRootAdapter = rootAdapter;
        this.mViewType = viewType;
    }

    @Override
    public int getCount() {
        return mRootAdapter.getPageCountBy(mViewType);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CalendarViewType viewType = (CalendarViewType) parent.getTag(CalendarAdapter.KEY_VIEW_TYPE);
        int pageIndexInCalendar = (Integer) parent.getTag(CalendarAdapter.KEY_PAGE_INDEX);
        return mRootAdapter.getDayCellView(pageIndexInCalendar, viewType, convertView, parent);
    }
}
