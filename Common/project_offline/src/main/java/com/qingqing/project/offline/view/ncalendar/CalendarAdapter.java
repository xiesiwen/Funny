package com.qingqing.project.offline.view.ncalendar;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.pager.PagedAdapter;
import com.qingqing.base.view.ptr.OnRefreshListener;
import com.qingqing.base.view.ptr.PtrBase;
import com.qingqing.base.view.ptr.PtrListView;
import com.qingqing.project.offline.calendar.CalendarController;
import com.qingqing.project.offline.calendar.CalendarDay;
import com.qingqing.project.offline.calendar.CalendarMonth;
import com.qingqing.project.offline.calendar.CalendarViewType;
import com.qingqing.project.offline.calendar.CalendarWeek;
import com.qingqing.project.offline.calendar.Day;
import com.qingqing.project.offline.course.CourseCalendarDay;
import com.qingqing.project.offline.order.CalendarDateParam;
import com.qingqing.project.offline.order.CalendarMonthViewV2;
import com.qingqing.project.offline.view.calendar.CourseDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qingqing.project.offline.R;

import static com.qingqing.project.offline.calendar.CalendarViewType.MONTH;
import static com.qingqing.project.offline.calendar.CalendarViewType.WEEK;


/**
 * Created by huangming on 2017/1/19.
 */

public abstract class CalendarAdapter<T> {

    public static final int KEY_VIEW_TYPE = com.qingqing.project.offline.R.id.key_calendar_view_type;
    public static final int KEY_PAGE_INDEX = com.qingqing.project.offline.R.id.key_calendar_page_index;

    private Map<Day, CourseCalendarDay.Status> mStatusCache;
    private Map<Day, Double> mCountCache = new HashMap<>();
    private Map<Day, List<T>> mDataCache = new HashMap<>();
    private Map<Day, String> mTagCache = new HashMap<>();

    protected   Context mContext;
    private LayoutInflater mInflater;
    private final CalendarController mController;

    private final PagedAdapter mDayPagedAdapter;
    private final PagedAdapter mWeekAdapter;
    private final PagedAdapter mMonthAdapter;

    private com.qingqing.project.offline.calendar.Day mSelectedDay;
    private CalendarViewType mSelectedViewType;
    private int mSelectedWeekIndexInMonth = -1;
    private int mSelectedMonthIndex = -1;

    private final PresenterView<T> mPresenterView;
    private ArrayList<CalendarDateParam> monthParamList;
    private ArrayList<CalendarDateParam> weekParamList;
    private CalendarDateParam selectDayParam;
    /**
     * 有课 key : 19900101 value : milliseconds
     */
    private SparseArray<Long> mHasClassTimes = new SparseArray<>();

    /**
     * 有课未结 key : 19900101 value : milliseconds
     */
    private SparseArray<Long> mHasClassAttentionTimes = new SparseArray<>();

    private SparseArray<Long> mHasClassFinishedTimes = new SparseArray<>();

    private SparseArray<Long> mNeedAppraiseTimes = new SparseArray<>();

    public CalendarAdapter(Context context, CalendarController controller, PresenterView<T> presenterView) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mController = controller;
        this.mWeekAdapter = new CalendarCellPagedAdapter(this, WEEK);
        this.mMonthAdapter = new CalendarCellPagedAdapter(this, MONTH);
        this.mDayPagedAdapter = new CalendarDayPagedAdapter(this);
        this.mPresenterView = presenterView;
        prepareDateParamList();
        prepareWeekParamList();
    }

    protected PresenterView<T> getPresenterView() {
        return mPresenterView;
    }

    Context getContext() {
        return mContext;
    }

    public Day getSelectedDay() {
        return mSelectedDay;
    }

    void setSelectedDay(Day selectedDay) {
        this.mSelectedDay = selectedDay;
    }

    void setSelectedWeekIndexInMonth(int selectedWeekIndexInMonth) {
        mSelectedWeekIndexInMonth = selectedWeekIndexInMonth;
    }

    public int getSelectedWeekIndexInMonth() {
        return mSelectedWeekIndexInMonth;
    }

    void setSelectedMonthIndex(int selectedMonthIndex) {
        this.mSelectedMonthIndex = selectedMonthIndex;
    }

    public int getSelectedMonthIndex() {
        return mSelectedMonthIndex;
    }

    public CalendarViewType getSelectedViewType() {
        return mSelectedViewType;
    }

    void setSelectedViewType(CalendarViewType selectedViewType) {
        this.mSelectedViewType = selectedViewType;
    }

    public CalendarController getController() {
        return mController;
    }

    PagedAdapter getWeekAdapter() {
        return mWeekAdapter;
    }

    PagedAdapter getMonthAdapter() {
        return mMonthAdapter;
    }

    PagedAdapter getDayPagedAdapter() {
        return mDayPagedAdapter;
    }

    public void updateStatuses(Map<Day, CourseCalendarDay.Status> statusMap,Map<Day, Double> countMap) {
        mStatusCache = statusMap;
        mCountCache = countMap;
        notifyMonthDataSetChanged();
        notifyWeekDataSetChanged();
    }

    protected CourseCalendarDay.Status getStatusBy(Day day) {
        return mStatusCache != null ? mStatusCache.get(day) : CourseCalendarDay.Status.NONE;
    }

    protected Double getCourseCountBy(Day day) {
        return mCountCache != null ? (mCountCache.containsKey(day)?mCountCache.get(day):0d ): 0d;
    }

    public void updateDataListTo(Day whichDay, String nextTag, List<T> dataList) {
        mTagCache.put(whichDay, nextTag);
        List<T> oldDataList = mDataCache.get(whichDay);
        if (oldDataList == null) {
            oldDataList = new ArrayList<>();
            mDataCache.put(whichDay, oldDataList);
        }
        oldDataList.addAll(dataList);
        notifyDayPagedDataSetChanged();
    }

    public void clearDataCache() {
        mDataCache.clear();
        mTagCache.clear();
        notifyDayPagedDataSetChanged();
    }

    public void clearDataCacheFrom(Day whichDay) {
        mDataCache.remove(whichDay);
        mTagCache.remove(whichDay);
        notifyDayPagedDataSetChanged();
    }

    public void notifyDataSetChanged() {
        notifyWeekDataSetChanged();
        notifyMonthDataSetChanged();
        notifyDayPagedDataSetChanged();
    }

    void notifyWeekDataSetChanged() {
        Logger.i("CalendarAdapter", "notifyWeekDataSetChanged");
        mWeekAdapter.notifyDataSetChanged();
    }

    void notifyMonthDataSetChanged() {
        Logger.i("CalendarAdapter", "notifyMonthDataSetChanged");
        mMonthAdapter.notifyDataSetChanged();
    }

    void notifyDayPagedDataSetChanged() {
        Logger.i("CalendarAdapter", "notifyDayPagedDataSetChanged");
        mDayPagedAdapter.notifyDataSetChanged();
    }

    public int getPageCountBy(CalendarViewType viewType) {
        switch (viewType) {
            case MONTH:
                return mController.getMonthCount();
            case WEEK:
                return mController.getWeekCount();
            default:
                return 0;
        }
    }

    public int getCellCountBy(CalendarViewType viewType) {
        switch (viewType) {
            case MONTH:
                return mController.getDayCountOfMonth();
            case WEEK:
                return mController.getDayCountOfWeek();
            default:
                return 0;
        }
    }

    public int getCellCountXBy(CalendarViewType viewType) {
        switch (viewType) {
            case MONTH:
            case WEEK:
                return mController.getDayCountOfWeek();
            default:
                return 0;
        }
    }

    public int getCellCountYBy(CalendarViewType viewType) {
        switch (viewType) {
            case MONTH:
                return mController.getWeekCountOfMonth();
            case WEEK:
                return 1;
            default:
                return 0;
        }
    }


    public abstract int getCellWidth();

    public abstract int getCellHeight();

    public abstract int getCellWidthGap();

    public abstract int getCellHeightGap();

    View getDayCellView(int pageIndexInCalendar, CalendarViewType viewType, View convertView, ViewGroup parent) {
        DateViewHolder holder;
        if (convertView == null) {
            convertView = new CalendarMonthViewV2(mContext);
            holder = new DateViewHolder((CalendarMonthViewV2) convertView);
            holder.init();
            convertView.setTag(holder);
        } else {
            holder = (DateViewHolder) convertView.getTag();
        }
        CalendarMonth month = null;
        CalendarWeek week = null;
        switch (viewType) {
            case MONTH:
                month = mController.obtainMonth(pageIndexInCalendar);
                break;
            case WEEK:
                week = mController.obtainWeek(pageIndexInCalendar);
                break;
            default:
                break;
        }
        holder.update(pageIndexInCalendar,month,week,viewType);
        convertView.setId(pageIndexInCalendar);
        if(viewType == MONTH) {
            Logger.i("CalendarAdapter", "getDayCellView pageIndexInCalendar = " + pageIndexInCalendar + ", dayIndexInPage = "  + "  " + viewType.name());
        }
        return convertView;
    }

    public void refreshMonthView(Day day,CalendarMonthViewV2 viewV2,int position){
        if(day != null){
            selectDayParam.year = day.getYear();
            selectDayParam.month = day.getMonth()-1;
            selectDayParam.day = day.getDayOfMonth();
        }
        if(viewV2 != null){
            DateViewHolder holder = (DateViewHolder) viewV2.getTag();
            CalendarMonth month = mController.obtainMonth(position);
            holder.updateItemView(viewV2,position,month,null,MONTH);
        }
    }
    public void refreshWeekView(Day day,CalendarMonthViewV2 viewV2,int position){
        if(day != null){
            selectDayParam.year = day.getYear();
            selectDayParam.month = day.getMonth()-1;
            selectDayParam.day = day.getDayOfMonth();
        }
        if(viewV2 != null){
            DateViewHolder holder = (DateViewHolder) viewV2.getTag();
            CalendarWeek week = mController.obtainWeek(position);
            holder.updateItemView(viewV2,position,null,week,WEEK);
        }
    }

    View getDayPageView(int position, View pageView, ViewGroup parent) {
        Logger.i("CalendarAdapter", "position = " + position);
        if (pageView == null) {
            pageView = mPresenterView.onCreateDayPageView(mInflater, parent);

            ((PtrListView) pageView.findViewById(com.qingqing.project.offline.R.id.calendar_day_ptr_page)).getRefreshableView().setAdapter(mPresenterView.onCreateDayAdapter(mContext));
        }
        final PtrListView ptrListView = (PtrListView) pageView.findViewById(com.qingqing.project.offline.R.id.calendar_day_ptr_page);
        final ListView listView = ptrListView.getRefreshableView();
        final PtrBase ptrBase = ptrListView.getPtrBase();
        final Day day = getSelectedDay();
        ptrBase.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefreshFromStart(String tag) {
                mPresenterView.onRequestDataOfDay(day, getTagBy(day));
            }

            @Override
            public void onRefreshFromEnd(String tag) {
                mPresenterView.onRequestDataOfDay(day, getTagBy(day));
            }
        });

//        if (day != null && !day.equals(listView.getTag())) {
//            ptrBase.finishRefresh(false);
//        }
        ptrBase.finishRefresh(false);
        ListAdapter listAdapter;
        if (listView.getAdapter() instanceof HeaderViewListAdapter) {
            listAdapter = ((HeaderViewListAdapter) listView.getAdapter()).getWrappedAdapter();
        } else {
            listAdapter = listView.getAdapter();
        }
        ((BaseAdapter) listAdapter).updateDataList(mDataCache.get(day));
        String tag = getTagBy(day);
        if (!tag.equals(ptrBase.getNextTag())) {
            ptrBase.setNextTag(getTagBy(day));
        }
        listView.setTag(day);
        return pageView;
    }
    private CalendarMonthViewV2.DaySelectCallback daySelectCallback = new CalendarMonthViewV2.DaySelectCallback() {
        @Override
        public void onDaySelected(int year, int month, int day) {
            selectDayParam.year = year;
            selectDayParam.month = month;
            selectDayParam.day = day;
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            getPresenterView().onClickDay(Day.create(calendar.getTime().getTime()));
        }
    };
    private class DateViewHolder {

        private CalendarMonthViewV2 view;

        DateViewHolder(CalendarMonthViewV2 view) {
            this.view = view;
        }

        public void init() {
            // 这里设置显示样式
            CalendarMonthViewV2.ShowParam showParam = new CalendarMonthViewV2.ShowParam.Builder()
                    .setShowWeekTitle(false).setShowPrevAndNextDays(true)
                    .setShowStartDayOfWeek(Calendar.MONDAY)
                    .setDateColor(mContext.getResources().getColor(com.qingqing.project.offline.R.color.gray_dark_deep))
                    .setMonthTitleHeight(
                            mContext.getResources().getDimensionPixelSize(com.qingqing.project.offline.R.dimen.dimen_1))
                    .setRowHeight(mContext.getResources().getDimensionPixelSize(com.qingqing.project.offline.R.dimen.dimen_48))
                    .setDateFontSize(
                            mContext.getResources().getDimensionPixelSize(com.qingqing.project.offline.R.dimen.font_size_12))
                    .setTitleColor(mContext.getResources().getColor(com.qingqing.project.offline.R.color.transparent))
                    .setDateSelectBgColor(mContext.getResources().getColor(com.qingqing.project.offline.R.color.primary_orange))
                    .setDateSelectBgShape(CalendarMonthViewV2.ShowParam.BgShape.CIRCLE)
                    .setDateTodayColor(mContext.getResources().getColor(com.qingqing.project.offline.R.color.primary_orange))
                    .setDateTodayDrawable(mContext.getResources().getDrawable(com.qingqing.project.offline.R.drawable.bg_course_today_text))
                    .setDateIndicationColor(
                            mContext.getResources().getColor(com.qingqing.project.offline.R.color.primary_orange))
                    .setDatePassedColor(mContext.getResources().getColor(com.qingqing.project.offline.R.color.gray))
                    .build();
            view.setShowParam(showParam).setOnDaySelectCallback(daySelectCallback);
        }

        public void update(int position,CalendarMonth month,CalendarWeek week,CalendarViewType viewType) {
            List<CourseCalendarDay> courseDays = new ArrayList<CourseCalendarDay>();
            CalendarDateParam data = null;
            if(viewType == MONTH){
                data = monthParamList.get(position);
            }else{
                data = weekParamList.get(position);
            }
            CalendarMonthViewV2.DateParam.Builder dateParamBuilder = new CalendarMonthViewV2.DateParam.Builder()
                    .setYear(data.year).setMonth(data.month)
                    .setSpecifiedWeek(0);

            if (data.year != selectDayParam.year || data.month != selectDayParam.month) {
                dateParamBuilder.setSelectDate(-1);
            }
            else {
                dateParamBuilder.setSelectDate(selectDayParam.day);
            }
            view.setId(position);
            switch (viewType) {
                case MONTH:
                    courseDays.clear();
                    List<com.qingqing.project.offline.view.calendar.Day> indDayParamList = com.qingqing.project.offline.view.calendar.CalendarController.getMonthDaysByIndex(position);
                    for(int i=0;i<indDayParamList.size();i++){
                        CalendarDay calendarDay = month.getDayBy(i);
                        CourseCalendarDay courseCalendarDay = new CourseCalendarDay(calendarDay, viewType, getStatusBy(calendarDay.getDay()), calendarDay.getDay().equals(getSelectedDay()),getCourseCountBy(calendarDay.getDay()));
                        courseDays.add(courseCalendarDay);
                    }
                    dateParamBuilder.setRowCount(6);
                    dateParamBuilder.addMonthDayStatus((ArrayList<CourseCalendarDay>) courseDays);
                    view.setDateParam(dateParamBuilder.build(),0);
                    break;
                case WEEK:
                    courseDays.clear();
                    List<com.qingqing.project.offline.view.calendar.Day> daysList = com.qingqing.project.offline.view.calendar.CalendarController.getWeekDaysByIndex(position);
                    for(int i=0;i<daysList.size();i++){
                        CalendarDay calendarDay = week.getDayBy(i);
                        CourseCalendarDay courseCalendarDay = new CourseCalendarDay(calendarDay, viewType, getStatusBy(calendarDay.getDay()), calendarDay.getDay().equals(getSelectedDay()),getCourseCountBy(calendarDay.getDay()));
                        courseDays.add(courseCalendarDay);
                    }
                    dateParamBuilder.setRowCount(1);
                    dateParamBuilder.addMonthDayStatus((ArrayList<CourseCalendarDay>) courseDays);
                    view.setDateParam(dateParamBuilder.build(),position);
                    break;
                default:
                    break;
            }
        }

        public void updateItemView(CalendarMonthViewV2 view,int position,CalendarMonth month,CalendarWeek week,CalendarViewType viewType){
            if(view == null){
                return;
            }
            List<CourseCalendarDay> courseDays = new ArrayList<CourseCalendarDay>();
            CalendarDateParam data = null;
            if(viewType == MONTH){
                data = monthParamList.get(position);
            }else{
                data = weekParamList.get(position);
            }
            CalendarMonthViewV2.DateParam.Builder dateParamBuilder = new CalendarMonthViewV2.DateParam.Builder()
                    .setYear(data.year).setMonth(data.month)
                    .setSpecifiedWeek(0);

            if (data.year != selectDayParam.year || data.month != selectDayParam.month) {
                dateParamBuilder.setSelectDate(-1);
            }
            else {
                dateParamBuilder.setSelectDate(selectDayParam.day);
            }
            view.setId(position);

            switch (viewType) {
                case MONTH:
                    courseDays.clear();
                    List<com.qingqing.project.offline.view.calendar.Day> indDayParamList = com.qingqing.project.offline.view.calendar.CalendarController.getMonthDaysByIndex(position);
                    if(month != null){
                        for(int i=0;i<indDayParamList.size();i++){
                            CalendarDay calendarDay = month.getDayBy(i);
                            CourseCalendarDay courseCalendarDay = new CourseCalendarDay(calendarDay, viewType, getStatusBy(calendarDay.getDay()), calendarDay.getDay().equals(getSelectedDay()),getCourseCountBy(calendarDay.getDay()));
                            courseDays.add(courseCalendarDay);
                        }
                    }
                    dateParamBuilder.setRowCount(6);
                    dateParamBuilder.addMonthDayStatus((ArrayList<CourseCalendarDay>) courseDays);
                    view.setDateParam(dateParamBuilder.build(),0).setOnDaySelectCallback(daySelectCallback);
                    break;
                case WEEK:
                    courseDays.clear();
                    List<com.qingqing.project.offline.view.calendar.Day> daysList = com.qingqing.project.offline.view.calendar.CalendarController.getWeekDaysByIndex(position);
                    if(week != null){
                        for(int i=0;i<daysList.size();i++){
                            CalendarDay calendarDay = week.getDayBy(i);
                            CourseCalendarDay courseCalendarDay = new CourseCalendarDay(calendarDay, viewType, getStatusBy(calendarDay.getDay()), calendarDay.getDay().equals(getSelectedDay()),getCourseCountBy(calendarDay.getDay()));
                            courseDays.add(courseCalendarDay);
                        }
                    }
                    dateParamBuilder.setRowCount(1);
                    dateParamBuilder.addMonthDayStatus((ArrayList<CourseCalendarDay>) courseDays);
                    view.setDateParam(dateParamBuilder.build(),position).setOnDaySelectCallback(daySelectCallback);
                    break;
                default:
                    break;
            }
        }

    }

    private int prepareDateParamList() {

        Calendar todayCalendar = Calendar.getInstance();

        monthParamList = new ArrayList<>();
        int count = getPageCountBy(MONTH);
        long startTime = mController.getRealFirstDayMillis();
        todayCalendar.setTimeInMillis(startTime);
        selectDayParam = new CalendarDateParam(todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.get(Calendar.DAY_OF_MONTH));

        final int startYear = todayCalendar.get(Calendar.YEAR);
        final int startMonth = todayCalendar.get(Calendar.MONTH);
//        long selectDate = 0;
//        if(startTime != NetworkTime.currentTimeMillis()){
//            selectDate = NetworkTime.currentTimeMillis();
//        }
//        //设置选中日期
//        if (selectDate > 0) {
//            todayCalendar.setTimeInMillis(selectDate);
//            selectDayParam.year = todayCalendar.get(Calendar.YEAR);
//            selectDayParam.month = todayCalendar.get(Calendar.MONTH);
//            selectDayParam.day = todayCalendar.get(Calendar.DAY_OF_MONTH);
//        }

        // 组装数据
        int selectPos = -1;
        CalendarDateParam startCalendarParam = new CalendarDateParam(startYear,
                startMonth);
        monthParamList.add(startCalendarParam);
        if (selectDayParam.year == startCalendarParam.year
                && selectDayParam.month == startCalendarParam.month) {
            selectPos = 0;
        }

        while (count > 1) {
            startCalendarParam = startCalendarParam.nextMonth();
            monthParamList.add(startCalendarParam);
            if (selectDayParam.year == startCalendarParam.year
                    && selectDayParam.month == startCalendarParam.month) {
                selectPos = monthParamList.indexOf(startCalendarParam);
            }
            --count;
        }
        return selectPos;
    }
    private int prepareWeekParamList() {

        Calendar todayCalendar = Calendar.getInstance();

        weekParamList = new ArrayList<>();

        int count = getPageCountBy(WEEK);
        long startTime = mController.getRealFirstDayMillis();
        todayCalendar.setTimeInMillis(startTime);
        selectDayParam = new CalendarDateParam(todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.get(Calendar.DAY_OF_MONTH));

        final int startYear = todayCalendar.get(Calendar.YEAR);
        final int startMonth = todayCalendar.get(Calendar.MONTH);
        final int startDay = todayCalendar.get(Calendar.DAY_OF_YEAR);

        // 组装数据
        int selectPos = -1;
        CalendarDateParam startCalendarParam = new CalendarDateParam(startYear,
                startMonth,startDay);
        weekParamList.add(startCalendarParam);
        if (selectDayParam.year == startCalendarParam.year
                && selectDayParam.month == startCalendarParam.month) {
            selectPos = 0;
        }

        while (count > 1) {
            startCalendarParam = startCalendarParam.nextWeek();
            weekParamList.add(startCalendarParam);
            if (selectDayParam.year == startCalendarParam.year
                    && selectDayParam.month == startCalendarParam.month) {
                selectPos = monthParamList.indexOf(startCalendarParam);
            }
            --count;
        }
        return selectPos;
    }


    public String getTagBy(Day day) {
        return mTagCache.containsKey(day) ? mTagCache.get(day) : "";
    }

    public abstract DayCellViewHolder onCreateDayCellViewHolder();

    public static abstract class DayCellViewHolder {
        public abstract CalendarMonthViewV2 onDayCellViewCreated(ViewGroup container, int position);

        public abstract void onUpdateDayCell(CalendarMonthViewV2 view, int position,CalendarDay day, CalendarViewType viewType);
    }

    public interface PresenterView<T> {
        View onCreateDayCellView(LayoutInflater inflater, ViewGroup container);

        void onClickDayCellView(CalendarDay calendarDay);

        void onClickDay(Day day);

        View onCreateDayPageView(LayoutInflater inflater, ViewGroup container);

        void onRequestDataOfDay(Day day, String tag);

        BaseAdapter<T> onCreateDayAdapter(Context context);

    }

}
