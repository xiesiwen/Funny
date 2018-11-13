package com.qingqing.project.offline.view.picker;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.picker.PickerView;
import com.qingqing.project.offline.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by huangming on 2016/1/21.
 * <p/>
 * 上课时间选择
 * timeBlock:8点为0,0点为-16,24点为32
 */
public class ClassTimePicker extends LinearLayout {

    private static final String TAG = "ClassTimePicker";

    /**
     * 两个小时block间隔
     */
    private static final int BLOCK_MAX_INTERVAL = 4;
    private static final int BLOCK_INTERVAL = BaseData.getClientType() == AppCommon.AppType.qingqing_student ? 4 : 2;
    private static final int BLOCK_MIN = -16;
    private static final int BLOCK_MAX = 32;
    private static final int BLOCK_START_MAX = BLOCK_MAX - BLOCK_MAX_INTERVAL;

    private static final int WEEK_START = 0;
    private static final int WEEK_END = 6;

    private PickerView mWeekPicker;
    private PickerView mStartTimePicker;
    private PickerView mEndTimePicker;

    private List<Integer> mWeekDatas = new ArrayList<Integer>();
    private List<Integer> mStartTimeDatas = new ArrayList<Integer>();
    private List<Integer> mEndTimeDatas = new ArrayList<Integer>();

    private boolean mFirst = true;

    public ClassTimePicker(Context context) {
        this(context, null);
    }

    public ClassTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        final LayoutInflater inflater = LayoutInflater.from(context);
        LayoutParams lp;

        mWeekPicker = (PickerView) inflater.inflate(R.layout.view_class_time_picker, this,
                false);
        lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        addView(mWeekPicker, lp);
        mWeekPicker.setOnPickerSelectedListener(mWeekSelectedListener);

        mStartTimePicker = (PickerView) inflater.inflate(R.layout.view_class_time_picker, this,
                false);
        lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        addView(mStartTimePicker, lp);
        mStartTimePicker.setOnPickerSelectedListener(mStartTimeSelectedListener);

        mEndTimePicker = (PickerView) inflater.inflate(R.layout.view_class_time_picker, this, false);
        lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        addView(mEndTimePicker, lp);
        mEndTimePicker.setOnPickerSelectedListener(mEndTimeSelectedListener);

        setBackgroundColor(getResources().getColor(R.color.white));

        mStartTimePicker.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (!mFirst) {
                        setEndTimePicker();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    /**
     * 设置初始值
     *
     * @param startTimeBlock
     * @param endTimeBlock
     */
    public void setInitialValue(int startTimeBlock, int endTimeBlock) {
        setInitialValue(-1, startTimeBlock, endTimeBlock);
    }

    /**
     * 设置初始值
     *
     * @param excludedWeek   剔除某一个week, -1表示不剔除，week值：0到6
     * @param startTimeBlock
     * @param endTimeBlock
     */
    public void setInitialValue(int excludedWeek, int startTimeBlock, int endTimeBlock) {
        if (startTimeBlock < BLOCK_MIN || startTimeBlock > BLOCK_START_MAX) {
            throw new RuntimeException("startTimeBlock < " + BLOCK_MIN + " || startTimeBlock > " + BLOCK_START_MAX);
        }

        if (endTimeBlock < BLOCK_MIN || endTimeBlock > BLOCK_MAX) {
            throw new RuntimeException("endTimeBlock < " + BLOCK_MIN + " || endTimeBlock > " + BLOCK_MAX);
        }

        if (endTimeBlock - startTimeBlock < BLOCK_INTERVAL) {
            throw new RuntimeException("endTimeBlock - startTimeBlock < " + BLOCK_INTERVAL);
        }

        if (excludedWeek < WEEK_START || excludedWeek > WEEK_END) {
            Log.i(TAG, "excludedWeek < WEEK_START || excludedWeek > WEEK_END");
        }

        for (int i = WEEK_START; i <= WEEK_END; i++) {
            if (i != excludedWeek) {
                mWeekDatas.add(i);
            }
        }
        mWeekPicker.setAdapter(new WeekAdapter(getContext(), mWeekDatas));

        for (int i = startTimeBlock; i <= endTimeBlock - BLOCK_INTERVAL && i <= BLOCK_START_MAX; i++) {
            mStartTimeDatas.add(i);
        }

        mStartTimePicker.setAdapter(new TimeBlockAdapter(getContext(), mStartTimeDatas));

        mEndTimePicker.setAdapter(new TimeBlockAdapter(getContext(), mEndTimeDatas));
        setEndTimePicker();
    }

    public void setEndTimePicker() {
        mEndTimeDatas.clear();

        int curItem = mStartTimePicker.getCurrentItem();
        if (curItem >= 0 && curItem < mStartTimeDatas.size()) {
            mEndTimeDatas.clear();
            int endBlock = mStartTimeDatas.get(mStartTimeDatas.size() - 1);
            int curStartBlock = mStartTimeDatas.get(curItem);
            for (int i = curStartBlock + BLOCK_INTERVAL; i <= endBlock + BLOCK_MAX_INTERVAL; i++) {
                mEndTimeDatas.add(i);
            }
        }
        mEndTimePicker.notifyDataChange();
        if (!mFirst) {

            // 默认选中2小时，因为老师端和ta端支持1小时，默认选中需要+2
            mEndTimePicker.setCurrentItem(BLOCK_MAX_INTERVAL - BLOCK_INTERVAL, false);
        }
    }

    /**
     * 设置选中值
     */
    public void setSelectedValue(int selectedStartTime, int selectedEndTime) {
        setSelectedValue(-1, selectedStartTime, selectedEndTime);
    }

    /**
     * 设置选中值
     *
     * @param selectedWeek
     * @param selectedStartTime
     * @param selectedEndTime
     */
    public void setSelectedValue(int selectedWeek, int selectedStartTime, int selectedEndTime) {
        if (mWeekDatas.contains(selectedWeek)) {
            mWeekPicker.setCurrentItem(mWeekDatas.indexOf(selectedWeek));
        } else {
            Log.w(TAG, "not contains week = " + selectedWeek);
        }

        if (mStartTimeDatas.contains(selectedStartTime)) {
            mStartTimePicker.setCurrentItem(mStartTimeDatas.indexOf(selectedStartTime));
        } else {
            Log.w(TAG, "not contains selectedStartTime = " + selectedStartTime);
        }

        mEndTimeDatas.clear();

        int curItem = mStartTimeDatas.indexOf(selectedStartTime);
        if (curItem >= 0 && curItem < mStartTimeDatas.size()) {
            mEndTimeDatas.clear();
            int endBlock = mStartTimeDatas.get(mStartTimeDatas.size() - 1);
            int curStartBlock = mStartTimeDatas.get(curItem);
            for (int i = curStartBlock + BLOCK_INTERVAL; i <= endBlock + BLOCK_MAX_INTERVAL; i++) {
                mEndTimeDatas.add(i);
            }
        }
        mEndTimePicker.notifyDataChange();

        if (mEndTimeDatas.contains(selectedEndTime)) {
            mEndTimePicker.setCurrentItem(mEndTimeDatas.indexOf(selectedEndTime));
        } else {
            Log.w(TAG, "not contains selectedEndTime = " + selectedEndTime);
        }
    }

    public int getSelectedWeek() {
        int currentItem = mWeekPicker.getCurrentItem();
        if (currentItem >= 0 && currentItem < mWeekDatas.size()) {
            return mWeekDatas.get(currentItem);
        }
        return -1;
    }

    public int getSelectedStartTime() {
        int currentItem = mStartTimePicker.getCurrentItem();
        if (currentItem >= 0 && currentItem < mStartTimeDatas.size()) {
            return mStartTimeDatas.get(currentItem);
        }
        return Integer.MAX_VALUE;
    }

    public int getSelectedEndTime() {
        int currentItem = mEndTimePicker.getCurrentItem();
        if (currentItem >= 0 && currentItem < mEndTimeDatas.size()) {
            return mEndTimeDatas.get(currentItem);
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mFirst = false;
        return super.dispatchTouchEvent(ev);
    }

    private PickerView.OnPickerSelectedListener mWeekSelectedListener = new PickerView.OnPickerSelectedListener() {

        @Override
        public void onPickerSelected(int position) {
        }
    };

    private PickerView.OnPickerSelectedListener mStartTimeSelectedListener = new PickerView.OnPickerSelectedListener() {

        @Override
        public void onPickerSelected(int position) {
        }
    };

    private PickerView.OnPickerSelectedListener mEndTimeSelectedListener = new PickerView.OnPickerSelectedListener() {

        @Override
        public void onPickerSelected(int position) {
        }
    };

    static class WeekAdapter extends BaseAdapter<Integer> {

        String[] mWeekArray;

        public WeekAdapter(Context context, List<Integer> list) {
            super(context, list);
            mWeekArray = context.getResources().getStringArray(R.array.week_array);
        }

        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_date_picker,
                    parent, false);
        }

        @Override
        public BaseAdapter.ViewHolder<Integer> createViewHolder() {
            return new WeekHolder();
        }

        class WeekHolder extends BaseAdapter.ViewHolder<Integer> {

            TextView textView;

            @Override
            public void init(Context context, View convertView) {
                textView = (TextView) convertView;
            }

            @Override
            public void update(Context context, Integer data) {
                textView.setText(getWeekText(data));
            }
        }

        public String getWeekText(int index) {
            int length = mWeekArray != null ? mWeekArray.length : 0;
            if (index >= 0 && index < length) {
                return mWeekArray[index];
            }
            return "";
        }

    }

    static class TimeBlockAdapter extends BaseAdapter<Integer> {

        public TimeBlockAdapter(Context context, List<Integer> list) {
            super(context, list);
        }

        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_date_picker,
                    parent, false);
        }

        @Override
        public BaseAdapter.ViewHolder<Integer> createViewHolder() {
            return new TimeBlockHolder();
        }

        static class TimeBlockHolder extends BaseAdapter.ViewHolder<Integer> {

            TextView textView;

            @Override
            public void init(Context context, View convertView) {
                textView = (TextView) convertView;
            }

            @Override
            public void update(Context context, Integer data) {
                textView.setText(DateUtils.hmSdf.format(getTimeByBlock(data).getTime()));
            }
        }

    }

    public static Calendar getTimeByBlock(int block) {
        Date date = new Date();
        Calendar cc = Calendar.getInstance();
        cc.setTime(date);
        cc.set(Calendar.HOUR_OF_DAY, 8);
        cc.set(Calendar.MINUTE, 0);
        cc.add(Calendar.MINUTE, block * 30);
        return cc;
    }

}
