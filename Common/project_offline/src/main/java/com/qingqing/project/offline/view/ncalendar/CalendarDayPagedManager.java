package com.qingqing.project.offline.view.ncalendar;

import android.content.Context;
import android.widget.ListView;

import com.qingqing.project.offline.calendar.Day;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.ptr.OnRefreshListener;
import com.qingqing.base.view.ptr.PtrListView;

import java.util.List;

/**
 * Created by huangming on 2017/2/6.
 */

public abstract class CalendarDayPagedManager<T> implements OnRefreshListener {

    void bindDayAdapter(Context context, PtrListView ptrListView, Day day, List<T> dataList) {
        ListView listView = ptrListView.getRefreshableView();
        if (!day.equals(listView.getTag()) || listView.getAdapter() == null) {
            listView.setAdapter(onCreateDayAdapter(context, dataList));
        }
    }

    public abstract BaseAdapter<T> onCreateDayAdapter(Context context, List<T> dataList);

    protected abstract void onRequestData(Day whichDay);

}
