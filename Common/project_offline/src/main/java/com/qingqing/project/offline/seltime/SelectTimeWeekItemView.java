package com.qingqing.project.offline.seltime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.log.Logger;
import com.qingqing.project.offline.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 展示单周SIZE天日期的view
 *
 * Created by tanwei on 2015/11/26.
 */
public class SelectTimeWeekItemView extends LinearLayout {

    private static final String TAG = "weekitem";

    private static final int SIZE = 7;
    
    private int selectedResId, defaultResId;// 选中、默认状态的背景资源

    private int defaultTextColor,selectedTextColor;// 选中、默认状态的文字颜色
            
    private List<TextView> mDateTvList;
    
    private int mLastSelection;
    
    private Date mDate;
    
    private IOnDateChangedListener mDateChangedListener;// 日期变更事件监听
    
    public SelectTimeWeekItemView(Context context) {
        super(context);
        init(context);
    }
    
    public SelectTimeWeekItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        defaultResId = R.color.white;
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                selectedResId = R.drawable.shape_circle_point_green;
                break;
            case AppCommon.AppType.qingqing_teacher:
                selectedResId = R.drawable.shape_circle_point_blue;
                break;
            case AppCommon.AppType.qingqing_ta:
                selectedResId = R.drawable.shape_circle_point_primary_orange;
                break;
        }

        defaultTextColor = getResources().getColor(R.color.black);
        selectedTextColor = getResources().getColor(R.color.white);

        ClickListener mClickListener = new ClickListener();
        inflate(context, R.layout.view_seltime_week_item, this);
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.color.white);
        
        mDateTvList = new ArrayList<TextView>(SIZE);
        mDateTvList.add((TextView) findViewById(R.id.select_time_week_date_monday));
        mDateTvList.add((TextView) findViewById(R.id.select_time_week_date_tuesday));
        mDateTvList.add((TextView) findViewById(R.id.select_time_week_date_wednesday));
        mDateTvList.add((TextView) findViewById(R.id.select_time_week_date_thursday));
        mDateTvList.add((TextView) findViewById(R.id.select_time_week_date_friday));
        mDateTvList.add((TextView) findViewById(R.id.select_time_week_date_saturday));
        mDateTvList.add((TextView) findViewById(R.id.select_time_week_date_sunday));
        
        for (int i = 0; i < SIZE; i++) {
            View parent = (View)mDateTvList.get(i).getParent();
            parent.setTag(i);
            parent.setOnClickListener(mClickListener);
        }
    }
    
    /** 设置监听 */
    public void setDateChangedListener(IOnDateChangedListener listener) {
        mDateChangedListener = listener;
    }
    
    /** 设置时间 */
    public void setDate(Date date) {
        setDateAndWeek(date, SelectTimeUtils.getWeekDay(date).value());
    }
    
    /** 设置时间和星期 */
    public void setDateAndWeek(Date date, int week) {
        mDate = date;
        mLastSelection = week;
        Logger.v(TAG, "setDateAndWeek  week == " + week + ", date = " + SelectTimeUtils.formatDateForLog(date));
        Date monday = SelectTimeUtils.getMondayOfWeek(date);
        for (int i = 0; i < SIZE; i++) {
            TextView tv = mDateTvList.get(i);
            int dayOfMonth = SelectTimeUtils.getDayOfMonth(SelectTimeUtils.addDay(monday,
                    i));
            tv.setText(getResources().getString(R.string.format_number_length_2, dayOfMonth));
        }
        
        setWeekdaySelected(mLastSelection);
    }
    
    /** 设置选中项 */
    public void setWeekdaySelected(int index) {
        for (int i = 0; i < SIZE; i++) {
            TextView view = mDateTvList.get(i);
            view.setBackgroundResource(defaultResId);
            view.setTextColor(defaultTextColor);
        }
        mDateTvList.get(index).setBackgroundResource(selectedResId);
        mDateTvList.get(index).setTextColor(selectedTextColor);
        mLastSelection = index;
    }
    
    private class ClickListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            
            int index = (Integer) arg0.getTag();
            
            if (index != mLastSelection) {
                setWeekdaySelected(index);
                if (mDateChangedListener != null) {
                    mDateChangedListener.onDateChanged(WeekDay.valueOf(index),
                            SelectTimeUtils.getDateByIndex(mDate, mLastSelection));
                }
            }
        }
    }
    
}
