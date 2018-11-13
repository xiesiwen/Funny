package com.qingqing.project.offline.order.v3;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gallery.ui.adapter.RecyclingPagerAdapter;
import com.qingqing.api.proto.v1.TeacherProto;
import com.qingqing.api.proto.v1.Time;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.order.Order;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.dialog.CompDialog;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.badge.StrokeBadgeView;
import com.qingqing.base.view.recycler.GridLayoutMarginDividerDecoration;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.order.CalendarDateParam;
import com.qingqing.project.offline.order.CalendarMonthView;
import com.qingqing.project.offline.seltime.SelectTimeHelper;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;
import com.qingqing.project.offline.seltime.WeekDay;
import com.qingqing.qingqingbase.ui.BaseFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wangxiaxin on 2017/8/8.
 *
 * 选择具体的日期 和 具体的时间
 */

public class SelectDateTimeFragment extends BaseFragment {
    
    public static final String TAG = "SelTimeFragment";
    public static final int DEFAULT_MONTH_COUNT = 24;

    private SelectTimeParamsV3 mParams;

    private TimeBlockStatusManager mStatusManager;

    private RelativeLayout mContainer;

    private ViewPager mViewPager;
    private DateAdapter calendarAdapter;
    private ArrayList<CalendarDateParam> dateParamList;
    private CalendarDateParam selectDayParam;
    private ArrayList<CalendarDateParam> indDayParamList;

    private ImageView imagePreviousMonth, imageNextMonth;
    
    private TextView tvMonthTitle;

    private TextView tvSelectDate;

    private ImageView checkImageView;

    private ViewGroup blockLayout, bottomLayout;

    private RecyclerView blockRecyclerView;

    private TimeBlockAdapter mTimeBlockAdapter;

    private View emptyLayout;

    // bottom simple
    private Button mBtnDone;
    // end

    // bottom optional
    private ImageView imgOptional;

    private StrokeBadgeView badgeView;

    private TextView mTvOptionalTotalCount, mTvOptionalSelectedCount;

    private Button mBtnOptionalSubmit;
    
    private OptionalTimeResultHolder mOptionalTimeResultHolder;
    // end

    private Date mSelectedDate;

    private int mSelectBlock = TimeBlockStatusManager.INVALID_BLOCK;
    
    private CompDialog exitConfirmDialog;

    private ClickListener listener;

    private ProtoListener responseListener;// 调课请求时间占用的回调
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = savedInstanceState;
        }

        if (bundle != null) {
            mParams = bundle.getParcelable(BaseParamKeys.PARAM_PARCEL_SELECT_TIME);
        }
        Logger.v(TAG, mParams != null ? mParams.toString() : "param null");

        mStatusManager = new TimeBlockStatusManager(mParams.getBlockLength());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_date_time_v3, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateTitle();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mContainer = (RelativeLayout) view
                .findViewById(R.id.fragment_sel_date_time_container);

        listener = new ClickListener();

        // month controller
        imagePreviousMonth = (ImageView) view
                .findViewById(R.id.fragment_sel_date_time_pre_month);
        imageNextMonth = (ImageView) view
                .findViewById(R.id.fragment_sel_date_time_next_month);
        imagePreviousMonth.setOnClickListener(listener);
        imageNextMonth.setOnClickListener(listener);
        
        tvMonthTitle = (TextView) view
                .findViewById(R.id.fragment_sel_date_time_month_title);

        // date calendar
        int selectPos = prepareDateParamList();

        mViewPager = (ViewPager) view.findViewById(R.id.fragment_sel_date_time_view_pager);
        calendarAdapter = new DateAdapter();
        mViewPager.setAdapter(calendarAdapter);
        mViewPager.setCurrentItem(selectPos);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateCalendar();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        CalendarDateParam dateParam = dateParamList.get(selectPos);
        updateMonthTitle(dateParam.year, dateParam.month);

        // time block
        blockLayout = (ViewGroup) view.findViewById(R.id.fragment_sel_date_time_block_layout);
        emptyLayout = view.findViewById(R.id.fragment_sel_date_time_empty_layout);
        tvSelectDate = (TextView) blockLayout
                .findViewById(R.id.fragment_sel_date_time_tv_select_date);
        initTimeBlock();
        
        // bottom
        bottomLayout = (ViewGroup) view
                .findViewById(R.id.fragment_sel_date_time_bottom_layout);
        initBottomLayout(
                (ViewStub) view.findViewById(R.id.fragment_sel_date_time_view_stub));
    }

    private void initTimeBlock() {
        blockRecyclerView = (RecyclerView) blockLayout
                .findViewById(R.id.fragment_sel_date_time_block_recycler);
        blockRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 6));
        mTimeBlockAdapter = new TimeBlockAdapter(getActivity());
        blockRecyclerView.setAdapter(mTimeBlockAdapter);
        mTimeBlockAdapter
                .setBlockClickListener(new TimeBlockAdapter.ITimeBlockClickListener() {
                    @Override
                    public void onTimeBlockClick(final int block, int status) {
                        Logger.v(TAG, "onTimeBlockClick block : " + block + ", status : "
                                + status);
                        
                        boolean busy = false;
                        if ((status & TimeBlockStatusManager.STATUS_MASK_TEACHER_BUSY) > 0) {
                            busy = true;
                        }
                        status &= TimeBlockStatusManager.STATUS_FILTER;

                        switch (status) {
                            case TimeBlockStatusManager.STATUS_INIT:
                                if (mStatusManager.checkBlockEnough(block)) {
                                    
                                    if (busy) {
                                        
                                        OrderDialogUtil.showTeacherTimeBusyDialog(
                                                getActivity(),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialog,
                                                            int which) {
                                                        selectBlock(block);
                                                    }
                                                }, null);
                                    }
                                    else {
                                        selectBlock(block);
                                    }
                                }
                                else {
                                    ToastWrapper.showWithIcon(
                                            R.string.text_toast_time_not_enough,
                                            R.drawable.icon_toast_wrong);
                                }
                                break;
                            
                            case TimeBlockStatusManager.STATUS_OTHER_SELECTED:
                                ToastWrapper.showWithIcon(R.string.text_toast_time_used,
                                        R.drawable.icon_toast_wrong);
                                break;
                        }
                    }
                });
        blockRecyclerView.addItemDecoration(new GridLayoutMarginDividerDecoration(6,
                getResources().getDimensionPixelOffset(R.dimen.dimen_12)));
        
        checkImageView = (ImageView) blockLayout
                .findViewById(R.id.fragment_sel_date_time_check_img);
        blockLayout.findViewById(R.id.fragment_sel_date_time_tv_interval_half_hour)
                .setOnClickListener(listener);
        checkImageView.setOnClickListener(listener);
        mStatusManager.setIntervalChangedListener(
                new TimeBlockStatusManager.IIntervalChangedListener() {
                    @Override
                    public void intervalChanged(boolean halfHour, boolean locked) {
                        if (locked) {
                            checkImageView.setImageResource(
                                    R.drawable.icon_time_half_hour_lock);
                            
                            // 如果没有选中，则需要刷新ui
                            if (!mStatusManager.isIntervalHalfHour()) {
                                mStatusManager.refreshIntervalByHalfHour();
                                mTimeBlockAdapter
                                        .updateBlockStatus(mStatusManager.getStatusMap(), false);
                            }
                            
                        }
                        else if (halfHour) {
                            checkImageView.setImageResource(
                                    R.drawable.icon_time_half_hour_orange);
                        }
                        else {
                            checkImageView.setImageResource(
                                    R.drawable.icon_time_half_hour_gray);
                            
                        }
                    }
                });
    }
    
    private void initBottomLayout(ViewStub stub) {

        if (mParams.isOptional()) {
            bottomLayout.removeAllViews();

            View optionalLayout = LayoutInflater.from(getActivity())
                    .inflate(R.layout.layout_sel_optional_time_bottom, bottomLayout);
            imgOptional = (ImageView) optionalLayout
                    .findViewById(R.id.layout_sel_optional_time_img);
            badgeView = (StrokeBadgeView) optionalLayout
                    .findViewById(R.id.layout_sel_optional_time_badge_view);
            mTvOptionalTotalCount = (TextView) optionalLayout
                    .findViewById(R.id.layout_sel_optional_time_total_count);
            mTvOptionalSelectedCount = (TextView) optionalLayout
                    .findViewById(R.id.layout_sel_optional_time_sel_count);
            mBtnOptionalSubmit = (Button) optionalLayout
                    .findViewById(R.id.layout_sel_optional_time_btn_submit);

            optionalLayout.findViewById(R.id.layout_sel_optional_time_count_layout)
                    .setOnClickListener(listener);
            mBtnOptionalSubmit.setOnClickListener(listener);

            // init result layout
            stub.setLayoutResource(R.layout.layout_optional_time_result);
            View resultLayout = stub.inflate();
            mOptionalTimeResultHolder = new OptionalTimeResultHolder(getActivity(),
                    resultLayout, mParams.getTimeList());
            mOptionalTimeResultHolder.setTimeChangedListener(
                    new OptionalTimeResultHolder.ITimeChangedListener() {
                        @Override
                        public void timeChanged() {
                            updateOptionalBottom();

                            mStatusManager.refreshSelectedTime(mParams.getTimeList(),
                                    true);
                            mTimeBlockAdapter
                                    .updateBlockStatus(mStatusManager.getStatusMap(), false);

                            refreshIndDays();

                            updateCalendar();
                        }
                    });

            mOptionalTimeResultHolder.refresh();

            updateOptionalBottom();

        }
        else {
            mBtnDone = (Button) bottomLayout
                    .findViewById(R.id.fragment_sel_date_time_bottom_btn_commit);
            mBtnDone.setOnClickListener(listener);

            updateTitle();

            if(mParams.getTimeList().size() > 0){
                mStatusManager.refreshSelectedTime(mParams.getTimeList());
            }

            // 检查寒假包时间限制
            TimeSlice selectedTime = mParams.getSelectedTime();
            if (selectedTime != null) {
                Date startDate = selectedTime.getStartDate();
                if (!checkTimeLimited(startDate.getTime())) {
                    updateDateChanged(startDate);
                    selectBlock(selectedTime.getStart());
                }
            }
            else if (mParams.getSelectDateTime() > 0) {
                if (!checkTimeLimited(mParams.getSelectDateTime())) {
                    updateDateChanged(new Date(mParams.getSelectDateTime()));
                }
            }
        }
    }

    // 刷新标题
    private void updateTitle() {
        if (mParams.isOptional()) {
            int odd = mParams.getCount() - mParams.getTimeList().size();
            if (odd == 0) {
                setTitle(R.string.text_title_optional_time_filled);
            }
            else {
                setTitle(getString(R.string.text_title_optional_time_odd, odd));
            }
        }
        else if (mParams.getSpecifiedWeek() > 0) {
            setTitle(getString(R.string.text_title_circle_time_with_week,
                    WeekDay.getWeekStringSimple(SelectTimeUtils
                            .calendarWeekToWeekDay(mParams.getSpecifiedWeek()))));
        }
        else {
            if (mParams.getDatePosition() > 0) {
                setTitle(getString(R.string.text_title_modify_time_with_index,
                        mParams.getDatePosition()));
            }
            else {
                setTitle(R.string.text_title_reset_time);
            }
        }
        
    }

    // 更新底部购物车信息
    private void updateOptionalBottom() {

        int count = mParams.getTimeList().size();
        if (count > 0) {
            imgOptional.setImageResource(R.drawable.icon_buy_list_ora);
        }
        else {
            imgOptional.setImageResource(R.drawable.icon_buy_list_gra);
        }
        badgeView.setBadgeCount(count);
        
        mTvOptionalTotalCount.setText(
                getString(R.string.text_sel_time_total_count, mParams.getCount()));

        if (count == mParams.getCount()) {
            mBtnOptionalSubmit.setEnabled(true);
            mTvOptionalSelectedCount.setVisibility(View.GONE);
        }
        else {
            mTvOptionalSelectedCount.setVisibility(View.VISIBLE);
            mTvOptionalSelectedCount
                    .setText(getString(R.string.text_sel_time_selected_count, count));
            mBtnOptionalSubmit.setEnabled(false);
        }

        updateTitle();
    }

    private boolean checkTimeLimited(long time) {
        return checkTimeLimited(time, true);
    }
    
    // 检查时间是否被限制
    private boolean checkTimeLimited(long time, boolean showDialog) {
        if (mParams.isTimeLimited()) {
            
            // 清除当前时间（否则下面的代码覆盖不到时、分、秒）
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            time = calendar.getTimeInMillis();
            
            if (mParams.getLimitTimeStart() > time || time > mParams.getLimitTimeEnd()) {
                if (showDialog) {
                    String message = getString(R.string.text_winter_pack_time_limited,
                            DateUtils.mYearAndMonthAndDateFormat
                                    .format(new Date(mParams.getLimitTimeStart())),
                            DateUtils.mYearAndMonthAndDateFormat
                                    .format(new Date(mParams.getLimitTimeEnd())));
                    OrderDialogUtil.showDialog(getActivity(), message);
                }
                return true;
            }
        }
        
        return false;
    }
    
    // 选中block后的处理逻辑
    private void selectBlock(int block){
        // 时间超限
        if (checkTimeLimited(mSelectedDate.getTime())) {
            return;
        }

        if (mParams.isOptional()) {

            if(mParams.getTimeList().size() >= mParams.getCount()){
                // 不能继续选择
                ToastWrapper.showWithIcon(
                        getString(
                                R.string.text_toast_optional_time_satisfied,
                                mParams.getCount()),
                        R.drawable.icon_toast_wrong);
                return;
            }

            // 生成时间
            TimeSlice timeSlice = SelectTimeUtils
                    .getTimeSliceMulti(block,
                            block
                                    + mParams.getBlockLength()
                                    - 1,
                            mSelectedDate);
            mParams.getTimeList().add(timeSlice);

            mStatusManager.refreshSelectedTime(mParams.getTimeList(), true);
            mTimeBlockAdapter.updateBlockStatus(mStatusManager.getStatusMap(), false);

            updateOptionalBottom();

            refreshIndDays();

            updateCalendar();

            showAnimation(blockRecyclerView
                    .getChildAt(mTimeBlockAdapter.getPositionOfBlock(block)));
        }
        else {

            mSelectBlock = block;

            mTimeBlockAdapter.setSelectedTimeBlock(block);
            mStatusManager.setSelected(block);

            mBtnDone.setEnabled(true);

            // 刷新具体时间到date title
            String date = getDate(mSelectedDate);
            String start = SelectTimeUtils.getHMbyBlock(mSelectBlock);
            String end = SelectTimeUtils.getHMbyBlock(mSelectBlock + mParams.getBlockLength());
            tvSelectDate.setText(date + "  " + start + "-" + end);
        }
    }

    // 根据是否跨年显示不同的格式
    private String getDate(Date time) {
        String date;
        if (SelectTimeUtils.isTimeInCurrentYear(time)) {
            date = DateUtils.mdSdf.format(mSelectedDate);
        }
        else {
            date = new SimpleDateFormat(SelectTimeUtils.FORMAT_YEAR_MONTH_DAY_2,
                    Locale.CHINA).format(mSelectedDate);
        }
        
        return date;
    }

    private void updateBlockStatus() {
        SparseIntArray statusMap = mStatusManager.getStatusMap();
        if (statusMap.size() > 0) {
            blockLayout.setVisibility(View.VISIBLE);
            mTimeBlockAdapter.updateBlockStatus(statusMap, true);
            emptyLayout.setVisibility(View.GONE);
        }
        else {
            blockLayout.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }

    // 日期更新后刷新ui以及其他数据
    private void updateDateChanged(Date date) {

        if (!mParams.isOptional()) {
            mBtnDone.setEnabled(false);
        }
        
        mSelectBlock = TimeBlockStatusManager.INVALID_BLOCK;
        mSelectedDate = date;
        
        tvSelectDate.setText(getDate(mSelectedDate));
        
        mStatusManager.refreshDate(mSelectedDate.getTime());
        
        updateBlockStatus();
        
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                checkTeacherTimeStatus();
                break;
            
            case AppCommon.AppType.qingqing_teacher:
                // 老师端调课
                if (!TextUtils.isEmpty(mParams.getOrderCourseId())) {
                    getOccupiedTimeBlock();
                }
                break;
        }
    }
    
    private int prepareDateParamList() {
        
        Calendar todayCalendar = Calendar.getInstance();
        
        dateParamList = new ArrayList<>();
        int count = DEFAULT_MONTH_COUNT;
        long startTime = NetworkTime.currentTimeMillis();
        long selectDate = 0;
        if (!checkTimeLimited(mParams.getSelectDateTime(), false)) {
            selectDate = mParams.getSelectDateTime();
        }
        todayCalendar.setTimeInMillis(startTime);
        selectDayParam = new CalendarDateParam(todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.get(Calendar.DAY_OF_MONTH));
        // 自定义选择时间不需要默认选中
        if (mParams.isOptional()) {
            selectDayParam.day = -1;
        }
        
        final int startYear = todayCalendar.get(Calendar.YEAR);
        final int startMonth = todayCalendar.get(Calendar.MONTH);
        
        // 设置选中日期
        if (selectDate > 0) {
            todayCalendar.setTimeInMillis(selectDate);
            selectDayParam.year = todayCalendar.get(Calendar.YEAR);
            selectDayParam.month = todayCalendar.get(Calendar.MONTH);
            selectDayParam.day = todayCalendar.get(Calendar.DAY_OF_MONTH);
        }
        
        // 组装数据
        int selectPos = -1;
        CalendarDateParam startCalendarParam = new CalendarDateParam(startYear,
                startMonth);
        dateParamList.add(startCalendarParam);
        if (selectDayParam.year == startCalendarParam.year
                && selectDayParam.month == startCalendarParam.month) {
            selectPos = 0;
        }
        
        while (count > 1) {
            startCalendarParam = startCalendarParam.nextMonth();
            dateParamList.add(startCalendarParam);
            if (selectDayParam.year == startCalendarParam.year
                    && selectDayParam.month == startCalendarParam.month) {
                selectPos = dateParamList.indexOf(startCalendarParam);
            }
            --count;
        }
        
        refreshIndDays();
        
        return selectPos;
    }

    private void refreshIndDays() {
        ArrayList<TimeSlice> timeList = mParams.getTimeList();
        if (!timeList.isEmpty()) {
            if (indDayParamList == null) {
                indDayParamList = new ArrayList<>(timeList.size());
            }
            else {
                indDayParamList.clear();
            }
            Calendar tmpCalendar = Calendar.getInstance();
            for (TimeSlice time : timeList) {
                tmpCalendar.setTime(time.getStartDate());
                indDayParamList.add(new CalendarDateParam(tmpCalendar.get(Calendar.YEAR),
                        tmpCalendar.get(Calendar.MONTH),
                        tmpCalendar.get(Calendar.DAY_OF_MONTH)));
            }
        }
        else if (indDayParamList != null) {
            indDayParamList.clear();
        }
    }

    // 刷新日历状态
    private void updateCalendar() {
        int position = mViewPager.getCurrentItem();
//        View itemView = calendarAdapter.getCurrentItemView();
        View itemView = mViewPager.findViewById(position);
        CalendarDateParam dateParam = dateParamList.get(position);
        if (itemView instanceof CalendarMonthView) {
            
            DateViewHolder holder = (DateViewHolder) itemView.getTag();
            holder.update(dateParam);
        }
        
        updateMonthTitle(dateParam.year, dateParam.month);
    }

    // 更新月份标题
    private void updateMonthTitle(int year, int month) {
        int position = mViewPager.getCurrentItem();
        tvMonthTitle.setText(getMonthTitle(year, month));
        if (position <= 0) {
            imagePreviousMonth.setVisibility(View.GONE);
        }
        else {
            imagePreviousMonth.setVisibility(View.VISIBLE);
        }
        if (position >= calendarAdapter.getCount() - 1) {
            imageNextMonth.setVisibility(View.GONE);
        }
        else {
            imageNextMonth.setVisibility(View.VISIBLE);
        }
    }

    // 获取老师授课时间状态
    private void checkTeacherTimeStatus() {
        Order.CheckTimeBlockStatusRequest request = new Order.CheckTimeBlockStatusRequest();
        request.date = mSelectedDate.getTime();
        request.qingqingTeacherId = mParams.getTeacherId();
        
        newProtoReq(CommonUrl.CHECK_TEACHER_TIME_STATUS.url()).setSendMsg(request).setReqTag(mSelectedDate.getTime())
                .setRspListener(new ProtoListener(
                        TeacherProto.CheckTeacherTimeBlockStatusResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        if (!couldOperateUI()
                                || (long)requestTag() != mSelectedDate.getTime()) {
                            Logger.v(TAG, "ignore request tag : " + requestTag());
                            return;
                        }
                        mStatusManager.refreshTeacherBusy(
                                ((TeacherProto.CheckTeacherTimeBlockStatusResponse) result).busyBlocks);
                        mTimeBlockAdapter.updateBlockStatus(mStatusManager.getStatusMap(), false);
                    }

                }).reqSilent();
    }

    // 获取调课时家长和老师的时间占用
    private void getOccupiedTimeBlock() {
        
        if (responseListener == null) {
            
            responseListener = new ProtoListener(
                    Order.GroupTimeConflictDetectSeparatedResponse.class) {
                @Override
                public void onDealResult(Object result) {
                    Order.GroupTimeConflictDetectSeparatedResponse response = (Order.GroupTimeConflictDetectSeparatedResponse) result;
                    if (couldOperateUI()) {
                        mStatusManager.refreshOccupiedBlock(response.conflicts);
                        mTimeBlockAdapter.updateBlockStatus(mStatusManager.getStatusMap(), false);
                    }
                }
                
                @Override
                public void onDealError(HttpError error, boolean isParseOK, int errorCode,
                        Object result) {}
            };
        }
        
        String date = SelectTimeUtils.getDateFormatYMD(mSelectedDate);
        Order.GroupTimeConflictDetectRequest request = new Order.GroupTimeConflictDetectRequest();
        request.qingqingTeacherId = mParams.getTeacherId();
        ArrayList<String> studentIds = mParams.getStudentIds();
        if (studentIds != null && studentIds.size() > 0) {
            request.qingqingStudentIds = studentIds
                    .toArray(new String[studentIds.size()]);
        }
        
        request.qingqingGroupOrderCourseId = mParams.getOrderCourseId();
        Time.TimeParam time = new Time.TimeParam();
        time.date = date;
        time.startBlock = SelectTimeHelper.sSliceStartBlock;
        time.endBlock = SelectTimeHelper.sSliceEndBlock;
        
        request.timeParams = new Time.TimeParam[] { time };
        newProtoReq(CommonUrl.GET_TIME_BLOCK_STATUS_FOR_GROUP_URL.url())
                .setSendMsg(request).setRspListener(responseListener).req();
    }

    private void showAnimation(View view) {
        final float[] mCurrentPosition = new float[2];
        // 一、创造出执行动画的主题---imageview
        final ImageView goods = new ImageView(getActivity());
        goods.setImageResource(R.drawable.shape_animation_red_point);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.addView(goods, params);
        
        // 二、计算动画开始/结束点的坐标的准备工作
        // 得到父布局的起始点坐标（用于辅助计算动画开始/结束时的点的坐标）
        int[] parentLocation = new int[2];
        mContainer.getLocationInWindow(parentLocation);
        
        // 得到商品图片的坐标（用于计算动画开始的坐标）
        int startLoc[] = new int[2];
        view.getLocationInWindow(startLoc);
        
        // 得到购物车图片的坐标(用于计算动画结束后的坐标)
        int endLoc[] = new int[2];
        imgOptional.getLocationInWindow(endLoc);
        
        // 三、正式开始计算动画开始/结束的坐标
        // 开始掉落的商品的起始点：商品起始点-父布局起始点+该商品图片的一半
        float startX = startLoc[0] - parentLocation[0] + view.getWidth() / 2;
        float startY = startLoc[1] - parentLocation[1] + view.getHeight() / 2;
        
        // 商品掉落后的终点坐标：购物车起始点-父布局起始点+购物车图片的1/5
        float toX = endLoc[0] - parentLocation[0] + imgOptional.getWidth() / 5;
        float toY = endLoc[1] - parentLocation[1];
        
        // 四、计算中间动画的插值坐标（贝塞尔曲线）（其实就是用贝塞尔曲线来完成起终点的过程）
        // 开始绘制贝塞尔曲线
        Path path = new Path();
        // 移动到起始点（贝塞尔曲线的起点）
        path.moveTo(startX, startY);
        // 使用二次萨贝尔曲线：注意第一个起始坐标越大，贝塞尔曲线的横向距离就会越大，一般按照下面的式子取即可
        path.quadTo((startX + toX) / 2, startY, toX, toY);
        // mPathMeasure用来计算贝塞尔曲线的曲线长度和贝塞尔曲线中间插值的坐标，
        // 如果是true，path会形成一个闭环
        final PathMeasure mPathMeasure = new PathMeasure(path, false);
        
        // 属性动画实现（从0到贝塞尔曲线的长度之间进行插值计算，获取中间过程的距离值）
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 当插值计算进行时，获取中间的每个值，
                // 这里这个值是中间过程中的曲线长度（下面根据这个值来得出中间点的坐标值）
                float value = (Float) animation.getAnimatedValue();
                // 获取当前点坐标封装到mCurrentPosition
                // boolean getPosTan(float distance, float[] pos, float[] tan) ：
                // 传入一个距离distance(0<=distance<=getLength())，然后会计算当前距
                // 离的坐标点和切线，pos会自动填充上坐标，这个方法很重要。
                mPathMeasure.getPosTan(value, mCurrentPosition, null);// mCurrentPosition此时就是中间距离点的坐标值
                // 移动的动画图片的坐标设置为该中间点的坐标
                goods.setTranslationX(mCurrentPosition[0]);
                goods.setTranslationY(mCurrentPosition[1]);
            }
        });
        // 五、 开始执行动画
        valueAnimator.start();
        
        // 六、动画结束后的处理
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                mContainer.removeView(goods);
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animator animation) {
                
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        
        if (mParams.isOptional() && mParams.getTimeList().size() > 0) {
            showExitConfirmDialog();
            return true;
        }
        
        mParams.setOptional(false);
        if (mParams.getSpecifiedWeek() > 0) {
            mParams.setSpecifiedWeek(0);
        }

        return super.onBackPressed();
    }

    private void showExitConfirmDialog() {
        
        if (exitConfirmDialog == null) {
            
            exitConfirmDialog = OrderDialogUtil.showDialog(getActivity(),
                    getString(R.string.text_dialog_exit_confirm_title),
                    getString(R.string.text_clear),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mParams.getTimeList().clear();
                            getActivity().onBackPressed();
                        }
                    }, getString(R.string.cancel));
        }
        else {
            exitConfirmDialog.show();
        }
    }
    
    private class DateAdapter extends RecyclingPagerAdapter {
        
        @Override
        public int getCount() {
            return dateParamList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            
            DateViewHolder holder;
            if (convertView == null) {
                convertView = new CalendarMonthView(getContext());
                holder = new DateViewHolder((CalendarMonthView) convertView);
                holder.init();
                convertView.setTag(holder);
            }
            else {
                holder = (DateViewHolder) convertView.getTag();
            }
            
            holder.update(dateParamList.get(position));
            convertView.setId(position);
            return convertView;
        }

    }

    private class DateViewHolder {

        private CalendarMonthView view;

        DateViewHolder(CalendarMonthView view) {
            this.view = view;
        }

        public void init() {
            // 这里设置显示样式
            CalendarMonthView.ShowParam showParam = new CalendarMonthView.ShowParam.Builder()
                    .setShowWeekTitle(false).setShowPrevAndNextDays(false)
                    .setShowStartDayOfWeek(Calendar.MONDAY)
                    .setDateColor(getResources().getColor(R.color.gray_dark_deep))
                    .setMonthTitleHeight(0)
                    .setRowHeight(getResources().getDimensionPixelSize(R.dimen.dimen_40))
                    .setDateFontSize(
                            getResources().getDimensionPixelSize(R.dimen.font_size_16))
                    .setTitleColor(getResources().getColor(R.color.gray_dark_deep))
                    .setDateSelectBgColor(getResources().getColor(R.color.accent_orange))
                    .setDateSelectBgShape(CalendarMonthView.ShowParam.BgShape.ROUND_RECT)
//                    .setDateTodayColor(getResources().getColor(R.color.accent_orange))
                    .setDateTodayDrawable(getResources().getDrawable(R.drawable.icon_time_today))
                    .setDateIndicationColor(
                            getResources().getColor(R.color.accent_orange))
                    .setDatePassedColor(getResources().getColor(R.color.gray))
                    .build();
            view.setShowParam(showParam).setOnDaySelectCallback(daySelectCallback);
        }

        public void update(CalendarDateParam data) {
            CalendarMonthView.DateParam.Builder dateParamBuilder = new CalendarMonthView.DateParam.Builder()
                    .setYear(data.year).setMonth(data.month)
                    .setSpecifiedWeek(mParams.getSpecifiedWeek());

            if (data.year != selectDayParam.year || data.month != selectDayParam.month) {
                dateParamBuilder.setSelectDate(-1);
            }
            else {
                dateParamBuilder.setSelectDate(selectDayParam.day);
            }

            if (indDayParamList != null) {
                for (CalendarDateParam indParam : indDayParamList) {
                    if (indParam.year == data.year && indParam.month == data.month) {
                        dateParamBuilder.addIndDay(indParam.day);
                    }
                }
            }
            view.setDateParam(dateParamBuilder.build());
        }

    }

    private CalendarMonthView.DaySelectCallback daySelectCallback = new CalendarMonthView.DaySelectCallback() {
        
        @Override
        public void onDaySelected(int year, int month, int day) {
            
            if (selectDayParam.year != year || selectDayParam.month != month
                    || selectDayParam.day != day) {
                
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                
                if (checkTimeLimited(calendar.getTimeInMillis())) {
                    updateCalendar();
                    return;
                }
                selectDayParam.year = year;
                selectDayParam.month = month;
                selectDayParam.day = day;
                calendarAdapter.notifyDataSetChanged();
                
                updateDateChanged(calendar.getTime());
            }
        }
    };

//    private CalendarMonthView.MonthTitleStringGenerator monthTitleStringGenerator = new CalendarMonthView.MonthTitleStringGenerator() {
//
//        @Override
//        public String generateMonthTitleString(CalendarMonthView.DateParam dateParam) {
//
//            // 自定义title的显示
//            return getMonthTitle(dateParam.getYear(), dateParam.getMonth());
//        }
//    };

    // month为Calendar的MONTH字段表示的范围
    private String getMonthTitle(int year, int month) {
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(NetworkTime.currentTimeMillis());
        
        int currentYear = calendar.get(Calendar.YEAR);

        calendar.clear();
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        if (year != currentYear) {
            return DateUtils.YearMonthFormat.format(calendar.getTime());
        }
        else {
            return DateUtils.MonthFormat.format(calendar.getTime());
        }
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.fragment_sel_date_time_bottom_btn_commit) {
                // 单次选择时间，点完成后返回结果
                if (mParams.getSpecifiedWeek() > 0) {
                    mParams.setSpecifiedWeek(0);
                }
                if (mSelectedDate != null
                        && mSelectBlock >= SelectTimeHelper.sSliceStartBlock) {
                    TimeSlice timeSlice = SelectTimeUtils.getTimeSliceMulti(mSelectBlock,
                            mSelectBlock + mParams.getBlockLength() - 1, mSelectedDate);
                    mParams.getTimeList().clear();
                    mParams.getTimeList().add(timeSlice);
                    if (mFragListener instanceof SelectTimeFragListener) {
                        ((SelectTimeFragListener) mFragListener).done(mParams);
                    }
                }
            }
            else if (id == R.id.fragment_sel_date_time_check_img
                    || id == R.id.fragment_sel_date_time_tv_interval_half_hour) {
                if (mStatusManager.isLocked()) {
                    return;
                }
                mStatusManager.refreshIntervalByHalfHour();
                
                mTimeBlockAdapter.updateBlockStatus(mStatusManager.getStatusMap(), false);
            }
            else if (id == R.id.layout_sel_optional_time_count_layout) {
                if (mParams.getTimeList().isEmpty()
                        || mOptionalTimeResultHolder.isShowing()) {
                    return;
                }
                mOptionalTimeResultHolder.refresh();
            }
            else if (id == R.id.layout_sel_optional_time_btn_submit) {
                if (mFragListener instanceof SelectTimeFragListener) {
                    ((SelectTimeFragListener) mFragListener).done(mParams);
                }
            }
            else if (id == R.id.fragment_sel_date_time_pre_month) {
                int currentItem = mViewPager.getCurrentItem();
                if (currentItem <= 0) {
                    return;
                }
                mViewPager.setCurrentItem(currentItem - 1);
            }
            else if (id == R.id.fragment_sel_date_time_next_month) {
                int currentItem = mViewPager.getCurrentItem();
                if (currentItem >= calendarAdapter.getCount() - 1) {
                    return;
                }
                mViewPager.setCurrentItem(currentItem + 1);
            }
        }
    }
}
