package com.qingqing.project.offline.seltime;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 时间段adapter，用于展示时间块
 */
public class TimeSliceAdapter extends BaseAdapter {
    
    private SparseIntArray status;
    
    private Context context;
    
    public TimeSliceAdapter(Context context) {
        this(context, new SparseIntArray(SelectTimeHelper.sSliceCount));
    }

    public TimeSliceAdapter(Context context, SparseIntArray array) {
        this.context = context;
        status = array;
        SelectTimeUtils.initTimeStatus(status);
    }
    
    /** 更新状态 */
    public void updateData(SparseIntArray data) {
        SelectTimeUtils.copySparseArray(data, status);
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return SelectTimeHelper.sSliceCount;
    }
    
    @Override
    public Object getItem(int arg0) {
        return status.valueAt(arg0);
    }
    
    @Override
    public long getItemId(int arg0) {
        return arg0;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        
        if (convertView == null) {
            convertView = new TimeSliceView(context);
        }
        
        ((TimeSliceView) convertView).setBlockAndStatus(status.keyAt(position),
                status.valueAt(position));
        return convertView;
    }
    
}
