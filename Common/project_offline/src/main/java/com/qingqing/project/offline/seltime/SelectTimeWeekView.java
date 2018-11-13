package com.qingqing.project.offline.seltime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.pager.WrapHeightViewPager;
import com.qingqing.project.offline.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 星期选择（包括星期、对应日期，日期支持滑动）
 * 
 * @author tanwei
 * 
 */
public class SelectTimeWeekView extends LinearLayout {

    private static final int SIZE = 7;
    
    private static final String TAG = "weekview";
    
    private ClickListener mClickListener;
    
    private int mLastSelection;// 选中日期下标

    private int selectedResId, defaultResId;// 选中、默认状态的背景资源

    private int defaultTextColor,selectedTextColor;// 选中、默认状态的文字颜色
    
    private List<TextView> mIndicatorTvList;
    
    private WrapHeightViewPager mWeekViewPager;// 日期滑动控件
    
    private WeekPagerAdapter mAdapter;
    
    private Date mDate;// 选中日期
    
    private IOnDateChangedListener mDateChangedListener;
    
    private boolean isShowWeekOnly;// 是否仅显示星期
    
    public SelectTimeWeekView(Context context) {
        super(context);
        init(context);
    }
    
    public SelectTimeWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        mClickListener = new ClickListener();
        inflate(context, R.layout.view_seltime_week, this);
        setOrientation(VERTICAL);

        defaultResId = R.color.white;
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                selectedResId = R.drawable.shape_circle_point_green;
                break;
            case AppCommon.AppType.qingqing_teacher:
                selectedResId = R.drawable.shape_circle_point_blue;
                break;
            case AppCommon.AppType.qingqing_ta:
                break;
        }

        defaultTextColor = getResources().getColor(R.color.black);
        selectedTextColor = getResources().getColor(R.color.white);

        mIndicatorTvList = new ArrayList<TextView>(SIZE);
        mIndicatorTvList.add((TextView) findViewById(R.id.select_time_week_index_monday));
        mIndicatorTvList
                .add((TextView) findViewById(R.id.select_time_week_index_tuesday));
        mIndicatorTvList
                .add((TextView) findViewById(R.id.select_time_week_index_wednesday));
        mIndicatorTvList
                .add((TextView) findViewById(R.id.select_time_week_index_thursday));
        mIndicatorTvList.add((TextView) findViewById(R.id.select_time_week_index_friday));
        mIndicatorTvList
                .add((TextView) findViewById(R.id.select_time_week_index_saturday));
        mIndicatorTvList.add((TextView) findViewById(R.id.select_time_week_index_sunday));
        
        mWeekViewPager = (WrapHeightViewPager) findViewById(R.id.select_time_week_viewpager);
        
        for (int i = 0; i < SIZE; i++) {
            TextView textView = mIndicatorTvList.get(i);
            View parent = (View)textView.getParent();
            parent.setTag(i);
            parent.setOnClickListener(mClickListener);
        }

    }

    /** 设置子view未选中/选中背景 */
    public void setItemBackgroundResId(int defaultRes, int selectedRes) {
        defaultResId = defaultRes;
        selectedResId = selectedRes;

    }

    /** 设置子view未选中/选中文字颜色 */
    public void setItemTextColor(int defaultColor, int selectedColor) {
        defaultTextColor = defaultColor;
        selectedTextColor = selectedColor;
    }
    
    /** 设置监听，如果支持滑动，则初始化WeekPagerAdapter */
    public void setDateChangedListener(IOnDateChangedListener listener) {
        mDateChangedListener = listener;
        if (!isShowWeekOnly) {
            mAdapter = new WeekPagerAdapter(getContext(), mDateChangedListener);
            mWeekViewPager.setAdapter(mAdapter);
            mWeekViewPager
                    .setOnPageChangeListener(mAdapter.getViewPagerChangedListener());
            mWeekViewPager.setCurrentItem(WeekPagerAdapter.FIRST_POSITION);
        }
    }
    
    /** 只显示星期，且不可滑动 */
    public void showWeekOnly() {
        isShowWeekOnly = true;
        mWeekViewPager.setVisibility(View.GONE);
        setWeekdaySelected(mLastSelection);
    }
    
    /** 设置时间 */
    public void setDate(Date date) {
        setDate(date, false);
    }
    
    /** 设置时间和星期 */
    public void setDate(Date date, boolean callback) {
        int week = SelectTimeUtils.getWeekDay(date).value();
        mDate = date;
        mLastSelection = week;
        Logger.v(TAG, "setDate  week == " + week + ", date = " + SelectTimeUtils.formatDateForLog(date));
        if (isShowWeekOnly) {
            setWeekdaySelected(week);
        }
        else {
            mAdapter.setDate(date);
        }
        if (callback && mDateChangedListener != null) {
            mDateChangedListener.onDateChanged(WeekDay.valueOf(week),
                    getCurrentDate());
        }
    }
    
    // 设置选中状态（仅限于星期模式）
    private void setWeekdaySelected(int index) {

        mLastSelection = index;

        for (int i = 0; i < SIZE; i++) {
            TextView view = mIndicatorTvList.get(i);
            view.setBackgroundResource(defaultResId);
            view.setTextColor(defaultTextColor);
        }

        mIndicatorTvList.get(mLastSelection).setBackgroundResource(selectedResId);
        mIndicatorTvList.get(mLastSelection).setTextColor(selectedTextColor);
    }
    
    /** 获取当前选中的日期 */
    public Date getCurrentDate() {
        return SelectTimeUtils.getDateByIndex(mDate, mLastSelection);
    }

    private class ClickListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            
            int index = (Integer) arg0.getTag();
            // 设置选中状态（仅限于星期模式）
            if (isShowWeekOnly && index != mLastSelection) {
                setWeekdaySelected(index);
                if (mDateChangedListener != null) {
                    mDateChangedListener.onDateChanged(WeekDay.valueOf(index),
                            getCurrentDate());
                }
            }
        }
    }
    
}
