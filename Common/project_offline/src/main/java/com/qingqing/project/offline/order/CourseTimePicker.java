package com.qingqing.project.offline.order;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.picker.PickerView;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wangxiaxin on 2016/9/20.
 *
 * 选择具体的时间
 */
public class CourseTimePicker extends LinearLayout {
    
    private PickerView mTimePicker;
    private List<Integer> mTimeBlocks = new ArrayList<>();
    private static SimpleDateFormat sHourFormat = new SimpleDateFormat(
            SelectTimeUtils.FORMAT_HOUR_MINUTE);
    private TimeBlockAdapter timeBlockAdapter;
    
    private static int DEFAULT_MIN_START_BLOCK = -4;
    private static int DEFAULT_MAX_END_BLOCK = 31;
    
    private PickerView.OnPickerSelectedListener mTimeSelectedListener = new PickerView.OnPickerSelectedListener() {
        
        @Override
        public void onPickerSelected(int position) {}
    };
    
    public CourseTimePicker(Context context) {
        this(context, null);
    }
    
    public CourseTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(HORIZONTAL);
        final LayoutInflater inflater = LayoutInflater.from(context);
        LayoutParams lp;

        lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        addView(inflater.inflate(R.layout.view_class_time_picker, this, false), lp);

        mTimePicker = (PickerView) inflater.inflate(R.layout.view_class_time_picker, this,
                false);
        lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 2);
        addView(mTimePicker, lp);
        mTimePicker.setOnPickerSelectedListener(mTimeSelectedListener);
        setBackgroundResource(R.color.white);

        lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        addView(inflater.inflate(R.layout.view_class_time_picker, this, false), lp);
    }
    
    public void setBlocks(Calendar calendar, int courseBlocks) {
        
        mTimeBlocks.clear();
        int startBlock = SelectTimeUtils.getNextAvailableBlock(calendar.getTime(),
                NetworkTime.currentTimeMillis());
        for (int i = startBlock; i <= (DEFAULT_MAX_END_BLOCK - courseBlocks + 1); i++) {
            mTimeBlocks.add(i);
        }
        
        if (timeBlockAdapter == null) {
            timeBlockAdapter = new TimeBlockAdapter(getContext(), mTimeBlocks);
            mTimePicker.setAdapter(timeBlockAdapter);
        }
        else {
            mTimePicker.notifyDataChange();
        }

        if(mTimeBlocks.isEmpty()){
            ToastWrapper.show(R.string.text_no_time_blocks);
        }
    }
    
    public int getSelectBlock() {

        if(mTimeBlocks.isEmpty()){
            return Integer.MIN_VALUE;
        }

        return mTimeBlocks.get(mTimePicker.getCurrentItem());
    }
    
    class TimeBlockAdapter extends BaseAdapter<Integer> {
        
        public TimeBlockAdapter(Context context, List<Integer> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(
                    com.qingqing.qingqingbase.R.layout.item_date_picker, parent, false);
        }
        
        @Override
        public ViewHolder<Integer> createViewHolder() {
            return new TimeBlockHolder();
        }

        class TimeBlockHolder extends ViewHolder<Integer> {
            
            TextView textView;
            
            @Override
            public void init(Context context, View convertView) {
                textView = (TextView) convertView;
            }
            
            @Override
            public void update(Context context, Integer data) {
                textView.setText(sHourFormat
                        .format(SelectTimeUtils.getTimeByBlock(data).getTime()));
            }
        }
    }
}
