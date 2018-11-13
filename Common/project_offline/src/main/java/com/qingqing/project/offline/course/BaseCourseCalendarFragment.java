package com.qingqing.project.offline.course;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.data.SPManager;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.fragment.PtrLayoutFragment;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.TimeUtil;
import com.qingqing.base.view.pager.PagedView;
import com.qingqing.base.view.picker.DatePicker;
import com.qingqing.project.offline.calendar.CalendarDay;
import com.qingqing.project.offline.calendar.CalendarMonth;
import com.qingqing.project.offline.calendar.CalendarViewType;
import com.qingqing.project.offline.calendar.Day;
import com.qingqing.project.offline.view.ncalendar.CalendarWidget;
import com.qingqing.project.offline.view.ncalendar.CalendarWorkspace;
import com.qingqing.qingqingbase.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by huangming on 2017/1/18.
 */

public abstract class BaseCourseCalendarFragment<T> extends PtrLayoutFragment implements View.OnClickListener, CourseCalendarAdapter.PresenterView {

    private static final String TAG = BaseCourseCalendarFragment.class.getSimpleName();

    protected TextView mSelectedDayTextView;
    protected View mBackToTodayView;
    protected CourseCalendarAdapter<T> mAdapter;
    protected CalendarWidget mCalendarWidget;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.view_switch_day).setOnClickListener(this);
        mBackToTodayView = view.findViewById(R.id.view_back_to_today);
        mBackToTodayView.setOnClickListener(this);
        mSelectedDayTextView = (TextView) view.findViewById(R.id.tv_calendar_selected_day);
        mAdapter = onCreateCourseCalendarAdapter();

        CalendarWorkspace workspace = (CalendarWorkspace) view.findViewById(R.id.calendar_workspace);
        mCalendarWidget = workspace.getCalendarWidget();
        mCalendarWidget.setDayPagedView((PagedView) view.findViewById(R.id.calendar_day_paged_view));
        workspace.setAdapter(mAdapter);
        mCalendarWidget.setOnCalendarChangeListener(new CalendarWidget.OnCalendarChangeListener() {
            @Override
            public void onDaySelected(Day day) {
                BaseCourseCalendarFragment.this.onDaySelected(day);
            }

            @Override
            public void onMonthSelected(CalendarMonth month) {
                Logger.i(TAG, "onMonthSelected : " + month.getYear() + "-" + month.getMonthOfYear());
                onRequestStatuses();
            }

            @Override
            public void onViewTypeSelected(CalendarViewType viewType) {
                Logger.i(TAG, "onViewTypeSelected : " + viewType);
                SPManager.put(getSavedViewTypeKey(), viewType.name());
            }
        });

        mCalendarWidget.selectViewType(getSavedViewType());
        selectToday();
    }

    private CalendarViewType getSavedViewType() {
        return CalendarViewType.valueOf(SPManager.getString(getSavedViewTypeKey(), CalendarViewType.WEEK.name()));
    }

    protected String getSavedViewTypeKey() {
        return "course_calendar_view_type";
    }

    protected abstract CourseCalendarAdapter<T> onCreateCourseCalendarAdapter();

    protected abstract int getLayoutResId();

    protected abstract int getSwitchDayDialogTheme();

    protected abstract void onRequestStatuses();

    protected void onStatusesRequestSuccess() {
        finishRefresh(true);
        Logger.i(TAG, "onStatusesRequestSuccess");
    }

    protected void onStatusesRequestFailed(Throwable e) {
        finishRefresh(false);
        Logger.e(TAG, "onStatusesRequestFailed", e);
    }

    protected void onStatusesRequested(Map<Day, CourseCalendarDay.Status> statusMap,Map<Day, Double> countMap) {
        Logger.i(TAG, "onStatusesRequested");
        mAdapter.updateStatuses(statusMap,countMap);
    }

    protected void notifyStatusesRequestSuccess() {
        if (couldOperateUI()) {
            onStatusesRequestSuccess();
        }
    }

    protected void notifyStatusesRequestFailed(Throwable error) {
        if (couldOperateUI()) {
            onStatusesRequestFailed(error);
        }
    }

    protected void notifyStatusesRequested(Map<Day, CourseCalendarDay.Status> statusMap,Map<Day, Double> countMap) {
        if (couldOperateUI()) {
            onStatusesRequested(statusMap,countMap);
        }
    }

    @Override
    public void onClickDayCellView(CalendarDay calendarDay) {
        selectDay(calendarDay.getDay());
    }

    @Override
    public void onClickDay(Day day) {
        selectDay(day);
    }

    @Override
    public void fetchData(String tag) {
        onRequestStatuses();
        mAdapter.clearDataCache();
        onRequestDataOfDay(getSelectedDay(), mAdapter.getTagBy(getSelectedDay()));
    }

    @Override
    public void onRequestDataOfDay(Day day, String tag) {

    }

    private void onDaySelected(Day day) {
        Logger.i(TAG, "onDaySelected : " + day);
        mSelectedDayTextView.setText(DateUtils.mYearAndMonthAndDateFormat.format(new Date(day.getTimeMillis())));
        boolean isToday = TimeUtil.isToday(day.getTimeMillis());
        mBackToTodayView.setVisibility(isToday ? View.GONE : View.VISIBLE);

        onRequestDataOfDay(day, mAdapter.getTagBy(day));
    }

    public Day getSelectedDay() {
        return mAdapter.getSelectedDay();
    }

    private void selectDay(Day day) {
        Logger.i(TAG, "selectDay: " + day);
        mCalendarWidget.selectDay(day);
    }

    private void selectToday() {
        Logger.i(TAG, "selectToday : " + DateUtils.mYearAndMonthAndDateFormat.format(new Date(NetworkTime.currentTimeMillis())));
        selectDay(Day.create(NetworkTime.currentTimeMillis()));
    }

    private void switchDay() {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH);
        int currentYear = c.get(Calendar.YEAR);
        final DatePicker datePicker = new DatePicker(getActivity());
        datePicker.setDate(currentYear, currentMonth + 1, 1, mAdapter.getController().getMinYear(), mAdapter.getController().getMaxYear());
        datePicker.setDateVisible(true, true, false);
        new CompatDialog.Builder(getActivity(), getSwitchDayDialogTheme())
                .setCustomView(datePicker)
                // 必须设置：设置dialog在底部
                .setWindowGravity(Gravity.BOTTOM)
                // 必须设置：设置dialog全屏显示
                .setWindowFullScreen(true)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,
                                                int i) {
                                dialogInterface.dismiss();
                                int year = datePicker.getYear();
                                int month = datePicker.getMonth();
                                Log.i(TAG, "ChangeDate : " + year + "-" + month);
                                final Day selectedDay = getSelectedDay();
                                if (selectedDay == null || (selectedDay.getYear() == year && selectedDay.getMonth() == month)) {
                                    return;
                                }
                                Day newDay = Day.create(year, month, 1);
                                selectDay(newDay);
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.view_back_to_today) {
            selectToday();
        } else if (id == R.id.view_switch_day) {
            switchDay();
        }
    }

}
