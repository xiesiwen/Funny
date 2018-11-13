package com.qingqing.project.offline.order.v3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.view.TagLayout;
import com.qingqing.base.view.recycler.VerticalDividerDecoration;
import com.qingqing.base.view.text.SquareTextView;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;
import com.qingqing.project.offline.seltime.WeekDay;
import com.qingqing.qingqingbase.ui.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * 选择时间周期界面
 *
 * Created by tanwei on 2017/8/7.
 */

public class SelectTimeCircleFragment extends BaseFragment {

    public static final String TAG = "timeCircle";

    private static final int REQUEST_CODE_SEL_TIME = 1;

    private SparseArray<TimeSlice> mCircleList;
    
    private TextView tvCount;
    
    private TagLayout mWeeksTagLayout;
    
    private RecyclerView mDateRecyclerView;
    
    private CircleTimeAdapter mAdapter;

    private Button mBtnGenerateTime;
    
    private SelectTimeParamsV3 mParams;

    private int clickIndex;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCircleList = new SparseArray<>();
        if (getArguments() != null) {
            mParams = getArguments()
                    .getParcelable(BaseParamKeys.PARAM_PARCEL_SELECT_TIME);
        }
        else {
            getActivity().finish();
        }
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_select_time_circle, container,
                false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.text_sel_course_time);

        // week title span
        TextView tvWeekTitle = (TextView) view
                .findViewById(R.id.fragment_order_select_time_circle_week_title);
        String title = getString(R.string.text_sel_time_circle);
        String tip = getString(R.string.text_sel_time_multiple);
        SpannableString span = new SpannableString(title + tip);
        span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.gray_dark)),
                title.length(), span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tvWeekTitle.setText(span);
        
        tvCount = (TextView) view
                .findViewById(R.id.fragment_order_select_time_circle_count);
        
        mWeeksTagLayout = (TagLayout) view
                .findViewById(R.id.fragment_order_select_time_circle_week_tag);
        mDateRecyclerView = (RecyclerView) view
                .findViewById(R.id.fragment_order_select_time_circle_date_list);
        mBtnGenerateTime = (Button) view
                .findViewById(R.id.fragment_order_select_time_circle_btn_generate);
        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.fragment_order_select_time_circle_btn_generate) {
                    generateTime();
                }
                
            }
        };
        mBtnGenerateTime.setOnClickListener(clickListener);
        mBtnGenerateTime.setText(getString(R.string.text_sel_time_generate_list)
                + getString(R.string.text_format_total_time, mParams.getCount()));

        // add week
        int padding = getResources().getDimensionPixelOffset(R.dimen.dimen_16);
        for (int i = WeekDay.MONDAY.value(); i <= WeekDay.SUNDAY.value(); i++) {
            SquareTextView tv = new SquareTextView(getActivity());
            tv.setPadding(0, padding, 0, padding);
            tv.setGravity(Gravity.CENTER);
            tv.setText(WeekDay.getWeekStringSimple(i));
            tv.setBackgroundResource(R.drawable.selector_sel_time_circle);
            tv.setTextColor(
                    getResources().getColorStateList(R.color.selector_time_text_color));
            mWeeksTagLayout.addTag(i, tv);
        }
        mWeeksTagLayout.setOnTagSelectedListener(new TagLayout.OnTagSelectedListener() {
            @Override
            public void onTagSelectedChange(Object tag, boolean selected) {
                int week = (int) tag;
                Logger.v(TAG, "week : " + week + ", selected : " + selected);
                
                boolean complete = true;// 周期是否完成
                if (selected) {
                    mCircleList.append(week, null);
                    int position = mCircleList.indexOfKey(week);
                    mAdapter.notifyItemInserted(position);
                    complete = false;
                }
                else {
                    int position = mCircleList.indexOfKey(week);
                    mCircleList.remove(week);
                    mAdapter.notifyItemRemoved(position);
                    
                    if (mCircleList.size() == 0) {
                        complete = false;

                        mParams.getTimeList().clear();
                    }
                }
                
                if (complete) {
                    complete = checkCircleComplete();
                }

                int size = mCircleList.size();
                if (size > 0) {
                    tvCount.setVisibility(View.VISIBLE);
                    tvCount.setText(getString(R.string.text_sel_time_circle_count, size));
                }
                else {
                    tvCount.setVisibility(View.GONE);
                }

                mBtnGenerateTime.setEnabled(complete);
            }
            
            @Override
            public void onTagRejectSelected() {
                
            }
        });
        
        mAdapter = new CircleTimeAdapter(getActivity(), mCircleList);
        mDateRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDateRecyclerView.setAdapter(mAdapter);
        mDateRecyclerView.addItemDecoration(
                new VerticalDividerDecoration(getActivity()).showHeaderDivider(true));

        mAdapter.setItemClickListener(new CircleTimeAdapter.ItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh, int position) {

                clickIndex = position;
                if (mParams.getTimeList().size() > 0) {
                    mParams.getTimeList().clear();
                }

                Intent intent = new Intent(getActivity(),
                        InternalSelectTimeActivity.class);

                // 设置星期
                int specifiedWeek = mCircleList.keyAt(position);
                mParams.setSpecifiedWeek(
                        SelectTimeUtils.weekDayToCalendarWeek(specifiedWeek));

                // 设置已选的时间
                TimeSlice timeSlice = mCircleList.valueAt(clickIndex);
                if (timeSlice != null) {
                    mParams.setSelectedTime(timeSlice);
                    mParams.setSelectDateTime(timeSlice.getStartDate().getTime());
                }
                else {
                    mParams.setSelectedTime(null);
                    // 计算指定的星期对应的最近的日期
                    Date date = SelectTimeUtils.getNextDateByIndex(
                            new Date(NetworkTime.currentTimeMillis()), specifiedWeek);
                    mParams.setSelectDateTime(date.getTime());
                }
                
                intent.putExtra(BaseParamKeys.PARAM_PARCEL_SELECT_TIME, mParams);
                startActivityForResult(intent, REQUEST_CODE_SEL_TIME);
            }
        });
    }

    private boolean checkCircleComplete() {
        boolean complete = true;
        for (int i = 0; i < mCircleList.size(); i++) {
            if (mCircleList.valueAt(i) == null) {
                complete = false;
                break;
            }
        }
        
        return complete;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_optional_time, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_optional_time) {
            if (mFragListener instanceof SelectTimeFragListener) {
                // clear circle list
                mCircleList.clear();
                // clear param
                mParams.clear();
                mParams.setOptional(true);
                
                ((SelectTimeFragListener) mFragListener).done(mParams);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setTitle(R.string.text_sel_course_time);
        }
        // 清空时间后删除周期
        if (!hidden && mParams.getTimeList().isEmpty()) {
            mCircleList.clear();
            mAdapter.notifyDataSetChanged();
            
            int childCount = mWeeksTagLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                mWeeksTagLayout.getChildAt(i).setSelected(false);
            }

            int size = mCircleList.size();
            if (size > 0) {
                tvCount.setVisibility(View.VISIBLE);
                tvCount.setText(getString(R.string.text_sel_time_circle_count, size));
            }
            else {
                tvCount.setVisibility(View.GONE);
            }
            mBtnGenerateTime.setEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SEL_TIME
                && data != null) {
            ArrayList<TimeSlice> list = data
                    .getParcelableArrayListExtra(BaseParamKeys.PARAM_ARRAY_LIST_SELECTED_TIME);
            
            if(list != null && list.size() > 0) {
                mCircleList.put(mCircleList.keyAt(clickIndex), list.get(0));
                mAdapter.notifyItemChanged(clickIndex);
                mBtnGenerateTime.setEnabled(checkCircleComplete());
            }else{
                Logger.w(TAG, "sel time result error");
            }
        }
    }

    // 按照星期顺序和时间顺序生成时间,优先按照时间先后顺序选择
    private void generateTime() {
        int count = mParams.getCount();
        int size = mCircleList.size();

        TimeSlice[] tempTimes = new TimeSlice[size];
        for (int i = 0; i < size; i++) {
            tempTimes[i] = mCircleList.valueAt(i);
        }

        TimeComparator comparator = new TimeComparator();
        ArrayList<TimeSlice> list = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            // 按时间先后顺序排序
            Arrays.sort(tempTimes, comparator);

            // 由于已经按时间先后顺序排序，直接取第一个
            TimeSlice timeSlice = tempTimes[0];
            list.add(timeSlice);

            // 将第一个时间加1周，替换已经选择的第一个
            Date date = timeSlice.getStartDate();
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            instance.add(Calendar.WEEK_OF_YEAR, 1);

            tempTimes[0] = SelectTimeUtils.getTimeSliceMulti(timeSlice.getStart(),
                    timeSlice.getEnd(), instance.getTime());
        }

        mParams.setTimeList(list);
        mParams.setSpecifiedWeek(0);
        if (mFragListener instanceof SelectTimeFragListener) {
            ((SelectTimeFragListener) mFragListener).done(mParams);
        }
    }
}
