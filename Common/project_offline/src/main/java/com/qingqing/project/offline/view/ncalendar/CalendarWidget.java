package com.qingqing.project.offline.view.ncalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.project.offline.calendar.CalendarController;
import com.qingqing.project.offline.calendar.CalendarMonth;
import com.qingqing.project.offline.calendar.CalendarViewType;
import com.qingqing.project.offline.calendar.CalendarWeek;
import com.qingqing.project.offline.calendar.Day;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.pager.PagedView;
import com.qingqing.project.offline.order.CalendarMonthViewV2;

/**
 * Created by huangming on 2017/1/18.
 */

public class CalendarWidget extends FrameLayout {

    private static final String TAG = CalendarWidget.class.getSimpleName();

    private PagedView mWeekView;
    private PagedView mMonthView;
    private PagedView mDayPagedView;
    private FrameLayout mMonthScrollContainer;

    private CalendarAdapter mAdapter;
    private OnCalendarChangeListener mOnCalendarChangeListener;
    private boolean mIsFirst = true;
    private boolean mIsFirstDayList = true;

    public CalendarWidget(Context context) {
        this(context, null);
    }

    public CalendarWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mWeekView = new PagedView(context);
        mWeekView.setOnPageChangeListener(new PagedView.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Logger.i(TAG, "mWeekView onPageSelected : " + position);
                final CalendarController controller = getController();
                final Day selectedDay = getSelectedDay();
                if (isWeekViewType() && controller != null) {
                    CalendarWeek week = controller.obtainWeek(position);
                    if (week == null) {
                        return;
                    }
                    CalendarWeek selectedWeek = selectedDay != null ? controller.getWeekBy(selectedDay.getTimeMillis()) : null;
                    if (selectedWeek != week) {
                        int realFirstDayIndexOfWeek = week.getFirstDayIndexInCalendar();
                        Day firstDayOfWeek = controller.obtainDay(realFirstDayIndexOfWeek);
                        if (controller.isDayInRealRange(firstDayOfWeek.getTimeMillis())) {
                            selectDay(firstDayOfWeek);
                        } else {
                            Day lastDayOfWeek = controller.obtainDay(week.getLastDayIndexInCalendar());
                            CalendarMonth month = controller.getMonthBy(lastDayOfWeek.getTimeMillis());
                            if (month != null) {
                                int realFirstDayIndexOfMonth = month.getRealFirstDayIndexInCalendar();
                                selectDay(controller.obtainDay(realFirstDayIndexOfMonth));
                            }
                        }
                    }
                }

            }
        });
        LayoutParams lp = generateDefaultLayoutParams();
        lp.gravity = Gravity.BOTTOM;
        addView(mWeekView, lp);

        mMonthView = new PagedView(context);
        mMonthView.setOnPageChangeListener(new PagedView.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Logger.i(TAG, "mMonthView onPageSelected : " + position);
                final CalendarController controller = getController();
                final Day selectedDay = getSelectedDay();
                if (isMonthViewType() && controller != null) {
                    if(mIsFirst){
                        mIsFirst = false;
                        selectDay(Day.create(NetworkTime.currentTimeMillis()));
                    }else{
                        CalendarMonth month = controller.obtainMonth(position);
                        if (month == null) {
                            return;
                        }
                        CalendarMonth selectedMonth = selectedDay != null ? controller.getMonthBy(selectedDay.getTimeMillis()) : null;
                        if (selectedMonth != month) {
                            int realFirstDayIndex = month.getRealFirstDayIndexInCalendar();
                            selectDay(controller.obtainDay(realFirstDayIndex));
                        }
                    }
                }
            }
        });
        mMonthScrollContainer = new FrameLayout(context);
        mMonthScrollContainer.addView(mMonthView);
        addView(mMonthScrollContainer);
    }

    public void setOnCalendarChangeListener(OnCalendarChangeListener l) {
        this.mOnCalendarChangeListener = l;
    }

    PagedView getDayPagedView() {
        return mDayPagedView;
    }

    public void setAdapter(CalendarAdapter adapter) {
        mAdapter = adapter;
        mWeekView.setAdapter(adapter.getWeekAdapter());
        mMonthView.setAdapter(adapter.getMonthAdapter());
        if (mDayPagedView != null) {
            mDayPagedView.setAdapter(adapter.getDayPagedAdapter());
        }
    }

    public void setDayPagedView(final PagedView dayPagedView) {
        if (mDayPagedView != null) {
            mDayPagedView.setOnPageChangeListener(null);
        }
        this.mDayPagedView = dayPagedView;
        if (mDayPagedView != null) {
            mDayPagedView.setOnPageChangeListener(new PagedView.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    Logger.i(TAG, "mDayPagedView onPageSelected : " + position);
                    final CalendarController controller = getController();
                    if (controller != null) {
                        if(mIsFirstDayList){
                            mIsFirstDayList = false;
                            selectDay(Day.create(NetworkTime.currentTimeMillis()));
                        }else{
                            Day selectedDay = controller.getDayByIndexOfRealCalendar(position);
                            if (selectedDay != null) {
                                selectDay(selectedDay);
                            }
                        }
                    }
                }
            });
        }
    }

    private CalendarController getController() {
        return mAdapter != null ? mAdapter.getController() : null;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public CalendarViewType getSelectedViewType() {
        return mAdapter != null ? mAdapter.getSelectedViewType() : null;
    }

    private boolean isWeekViewType() {
        return getSelectedViewType() == CalendarViewType.WEEK;
    }

    private boolean isMonthViewType() {
        return getSelectedViewType() == CalendarViewType.MONTH;
    }

    public void selectViewType(CalendarViewType viewType) {
        Logger.i("OnScrollChangeListener viewType="+viewType);
        if (mAdapter != null) {
            if (getSelectedViewType() != viewType) {
                mAdapter.setSelectedViewType(viewType);
                if (mOnCalendarChangeListener != null) {
                    mOnCalendarChangeListener.onViewTypeSelected(viewType);
                }
                mWeekView.setVisibility(viewType == CalendarViewType.WEEK ? VISIBLE : INVISIBLE);
                mMonthScrollContainer.setVisibility(viewType == CalendarViewType.MONTH ? VISIBLE : INVISIBLE);
                mMonthScrollContainer.postInvalidateOnAnimation();
                mWeekView.postInvalidateOnAnimation();
            }
        }
    }

    private Day getSelectedDay() {
        return mAdapter != null ? mAdapter.getSelectedDay() : null;
    }

    public void selectDay(Day day) {
        final CalendarController controller = getController();
        if (mAdapter != null && controller != null && controller.isDayInRealRange(day.getTimeMillis())) {
            if (!day.equals(getSelectedDay())) {
                mAdapter.setSelectedDay(day);
                if (mOnCalendarChangeListener != null) {
                    mOnCalendarChangeListener.onDaySelected(day);
                }
                int monthIndex = controller.getMonthIndexInCalendar(day.getTimeMillis());
                int weekIndex = controller.getWeekIndexInCalendar(day.getTimeMillis());
                CalendarMonth month = controller.obtainMonth(monthIndex);
                if (monthIndex != mAdapter.getSelectedMonthIndex()) {
                    mAdapter.setSelectedMonthIndex(monthIndex);
                    if (mOnCalendarChangeListener != null && month != null) {
                        mOnCalendarChangeListener.onMonthSelected(month);
                    }
                }
                if (month != null) {
                    int weekIndexInMonth = month.getWeekIndex(day.getTimeMillis());
                    if (weekIndexInMonth != mAdapter.getSelectedWeekIndexInMonth()) {
                        mAdapter.setSelectedWeekIndexInMonth(weekIndexInMonth);
                    }
                }
                mDayPagedView.scrollToPosition(controller.getDayIndexInRealCalendar(day.getTimeMillis()));
                mWeekView.scrollToPosition(weekIndex);
                mMonthView.scrollToPosition(monthIndex);
                mAdapter.notifyDataSetChanged();
                mAdapter.refreshMonthView(day, (CalendarMonthViewV2) mMonthView.getCurrentVisibleView(),mMonthView.getCurrentPosition());
                mAdapter.refreshWeekView(day,(CalendarMonthViewV2) mWeekView.getCurrentVisibleView(),mWeekView.getCurrentPosition());
            } else {
                Logger.w(TAG, "select same day : " + day);
            }
        } else {
            Logger.w(TAG, day + " is not in range");
        }
    }



    int getMinVisibleMeasuredHeight() {
        return mWeekView.getMeasuredHeight();
    }

    int getMaxVisibleMeasuredHeight() {
        return mMonthView.getMeasuredHeight();
    }

    int getMinVisibleHeight() {
        return mWeekView.getHeight();
    }

    int getMaxVisibleHeight() {
        return mMonthView.getHeight();
    }

    CalendarNestedScrollingParent.OnScrollChangeListener getOnScrollChangeListener() {
        return mScrollChangeListener;
    }

    private CalendarNestedScrollingParent.OnScrollChangeListener mScrollChangeListener = new CalendarNestedScrollingParent.OnScrollChangeListener() {
        @Override
        public void onScrollChanged(int y, int oldY) {
            int maxVisibleHeight = getMaxVisibleHeight();
            int minVisibleHeight = getMinVisibleHeight();
            Logger.i("OnScrollChangeListener maxVisibleHeight="+maxVisibleHeight+"minVisibleHeight="+minVisibleHeight+"y="+y);
            if (maxVisibleHeight > minVisibleHeight) {
                boolean isWeek = y ==maxVisibleHeight - minVisibleHeight || y ==maxVisibleHeight - minVisibleHeight-1 || y ==maxVisibleHeight - minVisibleHeight-2 || y ==maxVisibleHeight - minVisibleHeight-3;
                selectViewType(isWeek ? CalendarViewType.WEEK : CalendarViewType.MONTH);
                if (mAdapter != null) {
                    final int selectedWeekIndexInMonth = mAdapter.getSelectedWeekIndexInMonth();
                    int weekCountOfMonth = mAdapter.getCellCountYBy(CalendarViewType.MONTH);
                    if (selectedWeekIndexInMonth >= 0 && selectedWeekIndexInMonth < weekCountOfMonth) {
                        int targetScrollY;
                        final int cellHeight = mAdapter.getCellHeight();
                        final int cellHeightGap = mAdapter.getCellHeightGap();
                        int selectedCellTop = selectedWeekIndexInMonth * (cellHeight + cellHeightGap);
                        if (selectedCellTop - y < 0) {
                            targetScrollY = selectedCellTop - y;
                        } else {
                            targetScrollY = 0;
                        }
                        if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta && isWeek){
                        }else{
                            mMonthScrollContainer.scrollTo(0, targetScrollY);
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), Math.max(mMonthScrollContainer.getMeasuredHeight(), 0));

    }

    public interface OnCalendarChangeListener {
        void onDaySelected(Day day);

        void onMonthSelected(CalendarMonth month);

        void onViewTypeSelected(CalendarViewType viewType);
    }

}
