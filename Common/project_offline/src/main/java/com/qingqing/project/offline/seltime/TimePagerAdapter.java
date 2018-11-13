package com.qingqing.project.offline.seltime;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.qingqing.base.log.Logger;

/**
 * 时间页面adapter，控制滑动以及缓存view
 *
 * Created by tanwei on 2015/11/27.
 */
public class TimePagerAdapter extends PagerAdapter {
    
    private static final String TAG = "timeadapter";
    
    private int count;
    
    private int mStartSelection;// 当前页面起点（会绑定设置进来的时间，根据起点和偏移计算出时间上的偏移）
    
    private int mCurrentSelection = -1;// 当前选中页面
    
    private boolean isScroll;
    
    private SelectTimeGridView[] views;
    
    private AdapterView.OnItemClickListener mItemClickListener;
    
    private Calendar mCalendar;
    
    private Date mDate;
    
    private IOnDateChangedListener mDateChangedListener;
    
    private ViewPagerChangedListener mViewPagerChangedListener;
    
    /** 默认支持支持滑动 */
    public TimePagerAdapter(Context context) {
        this(context, true);
    }
    
    /** scroll : 是否需要支持滑动 */
    public TimePagerAdapter(Context context, boolean scroll) {
        isScroll = scroll;
        if (isScroll) {
            count = WeekPagerAdapter.CACHE_VIEW_COUNT;
            mStartSelection = WeekPagerAdapter.FIRST_POSITION;
        }
        else {
            count = 1;
        }
        views = new SelectTimeGridView[count];
        mCalendar = Calendar.getInstance();
        ItemClickListener defaultItemClickListener = new ItemClickListener();
        mViewPagerChangedListener = new ViewPagerChangedListener();

        SelectTimeGridView view;
        TimeSliceAdapter adapter;
        for(int i = 0; i < count; i ++){
            view = new SelectTimeGridView(context);
            adapter = new TimeSliceAdapter(context);
            view.setAdapter(adapter);
            view.setOnItemClickListener(defaultItemClickListener);
            views[i] = view;
        }
    }
    
    /** 设置时间块点击的监听 */
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mItemClickListener = listener;
    }
    
    /** 设置滑动引起时间变化的监听 */
    public void setDateChangedListener(IOnDateChangedListener listener) {
        mDateChangedListener = listener;
    }
    
    public ViewPager.OnPageChangeListener getViewPagerChangedListener() {
        return mViewPagerChangedListener;
    }
    
    @Override
    public int getCount() {
        return isScroll ? Integer.MAX_VALUE : 1;
    }
    
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int index = position % count;
        SelectTimeGridView view = views[(index)];
        container.addView(view);
        return view;
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views[position % count]);
    }
    
    /** 设置日期 */
    public void setDate(Date date) {
        mDate = date;
        if (mCurrentSelection != -1) {
            mStartSelection = mCurrentSelection;
        }
    }
    
    // 根据上次选中position和当前选中position的偏移计算当前选中item对应的时间
    private Date getDateByPosition(int position) {
        mCalendar.setTime(mDate);
        
        // 根据起始日期和position算出当前选择的星期中的日期
        if (position - mStartSelection != 0) {
            mCalendar.add(Calendar.DAY_OF_YEAR, position - mStartSelection);
        }
        
        return mCalendar.getTime();
    }
    
    public void updateCurrentPageData(SparseIntArray array) {
        int index = mCurrentSelection % count;
        SelectTimeGridView view = views[(index)];
        ((TimeSliceAdapter) view.getAdapter()).updateData(array);
    }
    
    public SelectTimeGridView getCurrentPageView() {
        int index = mCurrentSelection % count;
        return views[(index)];
    }
    
    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(arg0, arg1, arg2, arg3);
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
            Logger.v(TAG, "onPageSelected " + position);
            if (mCurrentSelection == -1) {
                mCurrentSelection = position;
            }
            else {
                mCurrentSelection = position;
                Date date = getDateByPosition(position);
                if (mDateChangedListener != null) {
                    mDateChangedListener.onDateChanged(SelectTimeUtils.getWeekDay(date),
                            date);
                }
            }
        }
        
        @Override
        public void onPageScrollStateChanged(int state) {}
    }
}
