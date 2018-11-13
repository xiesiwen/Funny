package com.qingqing.project.offline.view.picker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.picker.PickerView;
import com.qingqing.project.offline.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择时长（单位：小时）
 *
 * Created by tanwei on 2016/9/18.
 */
public class TimeLengthPicker extends LinearLayout {
    
    public static final float STEP = 0.5f;
    
    private PickerView mPickerView;
    
    private TimeLengthAdapter mAdapter;
    
    private float mHours, mMinHours;
    
    private ArrayList<Float> mLengthList;
    
    public TimeLengthPicker(Context context) {
        this(context, null);
    }
    
    public TimeLengthPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        final LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout.LayoutParams lp;

        lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        addView(inflater.inflate(R.layout.view_date_picker, this, false), lp);
        
        mPickerView = (PickerView) inflater.inflate(R.layout.view_date_picker, this,
                false);
        lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        addView(mPickerView, lp);
        mPickerView.setOnPickerSelectedListener(mPickerSelectedListener);
        setBackgroundColor(getResources().getColor(R.color.white));

        lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        addView(inflater.inflate(R.layout.view_date_picker, this, false), lp);
        
        mLengthList = new ArrayList<>();
    }
    
    /**
     * 设置初始化参数
     *
     * @param min
     *            最小时长 默认选中
     * @param max
     *            最大时长
     */
    public void setParam(float min, float max) {
        setParam(min, max, min);
    }
    
    /**
     * 设置初始化参数
     *
     * @param min
     *            最小时长
     * @param max
     *            最大时长
     * @param sel
     *            当前选中
     */
    public void setParam(float min, float max, float sel) {
        setParam(min, max, sel, STEP);
    }
    
    /**
     * 设置初始化参数
     *
     * @param min
     *            最小时长
     * @param max
     *            最大时长
     * @param sel
     *            当前选中
     * @param step
     *            步进
     */
    public void setParam(float min, float max, float sel, float step) {

        mHours = sel;
        mMinHours = min;

        if (mAdapter == null) {
            mAdapter = new TimeLengthAdapter(getContext(), mLengthList);
            mPickerView.setAdapter(mAdapter);
        }
        else {
            mLengthList.clear();
        }

        float index = min;
        do {
            mLengthList.add(index);
            index += step;
        } while (index <= max);
        mPickerView.setCurrentItem((int) ((sel - min) / step));
        mAdapter.notifyDataSetChanged();
    }

    /** 获取当前选中的时长 */
    public float getTimeLength() {
        return mHours;
    }
    
    private PickerView.OnPickerSelectedListener mPickerSelectedListener = new PickerView.OnPickerSelectedListener() {
        
        @Override
        public void onPickerSelected(int position) {
            mHours = position * STEP + mMinHours;
        }
    };
    
    class TimeLengthAdapter extends BaseAdapter<Float> {
        
        public TimeLengthAdapter(Context context, List<Float> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_date_picker, parent,
                    false);
        }
        
        @Override
        public ViewHolder<Float> createViewHolder() {
            return new DateHolder();
        }
        
        class DateHolder extends ViewHolder<Float> {
            
            TextView textView;
            
            @Override
            public void init(Context context, View convertView) {
                textView = (TextView) convertView;
            }
            
            @Override
            public void update(Context context, Float data) {
                textView.setText(
                        getResources().getString(R.string.text_format_hours, LogicConfig.getFormatDotString(data)));
            }
        }
    }
}
