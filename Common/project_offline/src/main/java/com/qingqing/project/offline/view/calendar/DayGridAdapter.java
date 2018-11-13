package com.qingqing.project.offline.view.calendar;

import java.util.List;

import com.qingqing.base.view.BaseAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author huangming
 * @date 2015-7-28
 */
public class DayGridAdapter extends BaseAdapter<Day> {

    private int numColumns;
    private int numRows;

    public DayGridAdapter(Context context, List<Day> list) {
        super(context, list);
    }

    public DayGridAdapter(Context context, List<Day> list, int numColumns, int numRows) {
        super(context, list);
        this.numColumns = numColumns;
        this.numRows = numRows;
    }

    @Override
    public View createView(Context context, ViewGroup parent) {
        return new DayView(context);
    }

    @Override
    public BaseAdapter.ViewHolder<Day> createViewHolder() {
        return new DayViewHolder(numColumns, numRows);
    }

    public static class DayViewHolder extends BaseAdapter.ViewHolder<Day> {

        private int numColumns;
        private int numRows;

        DayView dayView;

        DayViewHolder(int numColumns, int numRows) {
            this.numColumns = numColumns;
            this.numRows = numRows;
        }

        @Override
        public void init(Context context, View convertView) {
            if (convertView instanceof DayView) {
                dayView = (DayView) convertView;
            }
        }

        @Override
        public void update(Context context, Day data) {
            if (dayView != null) {
                dayView.updatePosition(numColumns, numRows, position);
                dayView.update(data);
            }
        }
    }

}
