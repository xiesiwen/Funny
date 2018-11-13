package com.qingqing.project.offline.seltime;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.log.Logger;

/**
 * 时间选择界面的星期显示adapter,支持左右滑动，缓存5个子view
 * <p/>
 * Created by tanwei on 2015/10/19.
 */
public class WeekPagerAdapter extends PagerAdapter {
    
    public static final int CACHE_VIEW_COUNT = 5;
    public static final int FIRST_POSITION = 1000 * 1000;
    private static final String TAG = "weekadapter";
    private SelectTimeWeekItemView[] mWeekViewArray;
    
    private Context mContext;
    
    private Date mDate;
    
    private int mStartSelection;// 当前页面起点（会绑定设置进来的时间，根据起点和偏移计算出时间上的偏移）
    
    private int mCurrentSelection = -1;// 当前选中页面
    
    private int mLastWeekdayIndex = -1;
    
    private DateChangedListener mDefaultListener;
    
    private IOnDateChangedListener mDateChangedListener;
    
    private Calendar mCalendar;
    
    private ViewPagerChangedListener mViewPagerChangedListener;

    public WeekPagerAdapter(Context context, IOnDateChangedListener listener) {
        this(context, listener, new Date(NetworkTime.currentTimeMillis()));
    }
    
    public WeekPagerAdapter(Context context, IOnDateChangedListener listener, Date date) {
        mContext = context;
        mDateChangedListener = listener;
        mDefaultListener = new DateChangedListener();
        mDate = date;
        mLastWeekdayIndex = SelectTimeUtils.getWeekDay(date).value();
        
        mStartSelection = FIRST_POSITION;
        mCalendar = Calendar.getInstance();
        
        mViewPagerChangedListener = new ViewPagerChangedListener();
        
        initView();
    }

    private void initView() {
        mWeekViewArray = new SelectTimeWeekItemView[CACHE_VIEW_COUNT];
        for (int i = 0; i < CACHE_VIEW_COUNT; i++) {
            SelectTimeWeekItemView view = new SelectTimeWeekItemView(mContext);
            view.setDateChangedListener(mDefaultListener);
            mWeekViewArray[i] = view;
        }
    }
    
    public ViewPager.OnPageChangeListener getViewPagerChangedListener() {
        return mViewPagerChangedListener;
    }
    
    /** 设置时间 */
    public void setDate(Date date) {
        Logger.v(TAG, "setDate " + SelectTimeUtils.formatDateForLog(date));
        mDate = date;
        mStartSelection = mCurrentSelection;
        mLastWeekdayIndex = SelectTimeUtils.getWeekDay(date).value();
        SelectTimeWeekItemView weekView = mWeekViewArray[mStartSelection % CACHE_VIEW_COUNT];
        weekView.setDateAndWeek(getDateByPosition(mStartSelection), mLastWeekdayIndex);
        weekView.setWeekdaySelected(mLastWeekdayIndex);
    }
    
    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    
    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        
        final SelectTimeWeekItemView view = mWeekViewArray[position % CACHE_VIEW_COUNT];
        fillDate(view, position);
        container.addView(view);
        return view;
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mWeekViewArray[position % CACHE_VIEW_COUNT]);
    }
    
    // 填充日期
    private void fillDate(SelectTimeWeekItemView view, int position) {
        
        if (mLastWeekdayIndex != -1) {
            view.setDateAndWeek(getDateByPosition(position), mLastWeekdayIndex);
        }
        else {
            view.setDate(getDateByPosition(position));
        }
    }
    
    private Date getDateByPosition(int position) {
        mCalendar.setTime(mDate);
        
        // 根据起始日期和position算出当前选择的星期中的日期
        if (position - mStartSelection != 0) {
            mCalendar.add(Calendar.WEEK_OF_YEAR, position - mStartSelection);
        }
        // 根据当前星期中的日期和上次选择的星期下标算出具体日期
        if (mLastWeekdayIndex != -1) {
            
            return SelectTimeUtils.getDateByIndex(mCalendar.getTime(), mLastWeekdayIndex);
        }
        else {
            return mCalendar.getTime();
        }
        
    }
    
    /** 滑动翻页后在此发起日期变更回调 */
    private void onPageSelected(int position) {
        Date date = getDateByPosition(position);

        Logger.v(TAG, "onPageSelected " + position + ", date  " + SelectTimeUtils.formatDateForLog(date));

        mWeekViewArray[position % CACHE_VIEW_COUNT].setDate(date);

        if (mDateChangedListener != null) {
            mDateChangedListener.onDateChanged(SelectTimeUtils.getWeekDay(date),
                    date);
        }
    }
    
    /** 点击星期不同的按钮后在此发起日期变更回调 */
    private class DateChangedListener implements IOnDateChangedListener {
        @Override
        public void onDateChanged(WeekDay weekDay, Date date) {
            mLastWeekdayIndex = weekDay.value();
            if (mDateChangedListener != null) {
                mDateChangedListener.onDateChanged(weekDay, date);
            }
        }
    }
    
    private class ViewPagerChangedListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
            
        }
        
        @Override
        public void onPageSelected(int position) {
            Logger.v(TAG, "pager onPageSelected " + position);
            if (mCurrentSelection == -1) {
                mCurrentSelection = position;
            }
            else {
                mCurrentSelection = position;
                WeekPagerAdapter.this.onPageSelected(position);
            }
            
        }
        
        @Override
        public void onPageScrollStateChanged(int state) {}
    }
}
