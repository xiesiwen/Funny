package com.qingqing.project.offline.order;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.qingqing.api.proto.v1.CommentTypeEnum;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.CalendarUtil;
import com.qingqing.base.utils.DisplayUtil;
import com.qingqing.base.utils.TimeUtil;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.calendar.CalendarDay;
import com.qingqing.project.offline.calendar.CalendarMonth;
import com.qingqing.project.offline.calendar.CalendarViewType;
import com.qingqing.project.offline.course.CourseCalendarDay;
import com.qingqing.project.offline.view.calendar.CalendarController;
import com.qingqing.project.offline.view.calendar.CourseDay;
import com.qingqing.project.offline.view.calendar.Day;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Created by wangxiaxin on 2016/9/18.<br>
 * <br>
 * 日历的月视图<br>
 * 显示相关的配置 请看 {@link CalendarMonthViewV2.ShowParam}<br>
 * 日期相关的配置 请看 {@link CalendarMonthViewV2.DateParam}
 */
public class CalendarMonthViewV2 extends View {

    public interface DaySelectCallback {
        void onDaySelected(int year, int month, int day);
    }

    /** 一周几天 定值，也固定了显示列数 */
    private static final int DAYS_OF_WEEK = 7;

    /** 当月需要显示几行 */
    private int rowCount;

    /** 当前显示的周 */
    private int weekIndex;

    private ShowParam showParam;
    private DateParam dateParam;

    private Paint monthPaint;
    private Paint weekPaint;
    private Paint dayDisablePaint;
    private Paint statusPaint;
    private Paint dayPaint;
    private Paint selectDayBgPaint;
    private Paint testBgPaint;
    private Paint dayIndPaint;

    private Calendar todayCalendar;
    private int width;
    private DaySelectCallback selectCallback;
    List<Day> weekDaysByIndex;

    public CalendarMonthViewV2(Context context) {
        this(context, null);
    }

    public CalendarMonthViewV2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarMonthViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        Log.i("xxx", "new this is " + hashCode());
    }
    
    /** 设置相关参数 */
    public CalendarMonthViewV2 setShowParam(ShowParam param) {
        showParam = param;
        Log.i("xxx", "onset this is " + hashCode() + " class name is " + " showp is " + showParam.hashCode());
        preparePaint();
        if (todayCalendar == null) {
            todayCalendar = Calendar.getInstance();
        }
        return this;
    }
    
    public CalendarMonthViewV2 setDateParam(DateParam param,int weekPosition) {
        dateParam = param;
        rowCount = param.rowCount;
        weekIndex = weekPosition;
        requestLayout();
        return this;
    }
    
    public CalendarMonthViewV2 setOnDaySelectCallback(DaySelectCallback callback) {
        selectCallback = callback;
        return this;
    }
    
    /** 准备画笔 */
    private void preparePaint() {
        monthPaint = new Paint();
//        monthPaint.setFakeBoldText(true);
        monthPaint.setAntiAlias(true);
        monthPaint.setTextSize(showParam.titleFontSize);
        // monthPaint.setTypeface(Typeface.create(mMonthTitleTypeface,
        // Typeface.BOLD));
        monthPaint.setColor(showParam.titleColor);
        monthPaint.setTextAlign(Paint.Align.LEFT);
        monthPaint.setStyle(Paint.Style.FILL);
        
        weekPaint = new Paint();
        weekPaint.setAntiAlias(true);
        weekPaint.setTextSize(showParam.weekFontSize);
        weekPaint.setColor(showParam.weekColor);
        // weekPaint.setTypeface(Typeface.create(mDayOfWeekTypeface,
        // Typeface.NORMAL));
        weekPaint.setStyle(Paint.Style.FILL);
        weekPaint.setTextAlign(Paint.Align.CENTER);
        weekPaint.setFakeBoldText(true);
        
        dayPaint = new Paint();
        dayPaint.setAntiAlias(true);
        dayPaint.setTextSize(showParam.dateFontSize);
        dayPaint.setStyle(Paint.Style.FILL);
        dayPaint.setTextAlign(Paint.Align.CENTER);
        dayPaint.setFakeBoldText(false);
        
        dayDisablePaint = new Paint();
        dayDisablePaint.setAntiAlias(true);
        dayDisablePaint.setTextSize(showParam.dataUnSelectFontSize);
        dayDisablePaint.setColor(showParam.dateUnSelectColor);
        dayDisablePaint.setStyle(Paint.Style.FILL);
        dayDisablePaint.setTextAlign(Paint.Align.CENTER);
        dayDisablePaint.setFakeBoldText(false);
        


        statusPaint = new Paint();
        statusPaint.setAntiAlias(true);
        statusPaint.setTextSize(showParam.dataStatusFontSize);
        statusPaint.setColor(showParam.dateUnSelectColor);
        statusPaint.setStyle(Paint.Style.FILL);
        statusPaint.setTextAlign(Paint.Align.CENTER);
        statusPaint.setFakeBoldText(false);

        selectDayBgPaint = new Paint();
        selectDayBgPaint.setFakeBoldText(true);
        selectDayBgPaint.setAntiAlias(true);
        selectDayBgPaint.setColor(showParam.dateSelectBgColor);
        // selectDayBgPaint.setTextAlign(Paint.Align.CENTER);
        selectDayBgPaint.setStyle(Paint.Style.FILL);
        // selectDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);
        
        dayIndPaint = new Paint();
        dayIndPaint.setAntiAlias(true);
        dayIndPaint.setColor(showParam.dateIndColor);
        dayIndPaint.setStyle(Paint.Style.FILL);
        
        testBgPaint = new Paint();
        testBgPaint.setStyle(Paint.Style.FILL);
        testBgPaint.setAntiAlias(true);
        testBgPaint.setColor(Color.parseColor("#ff0000"));
        
        if (showParam.dateTodayDrawable != null) {
            showParam.dateTodayDrawable.setBounds(0, 0,
                    showParam.dateTodayDrawable.getIntrinsicWidth(),
                    showParam.dateTodayDrawable.getIntrinsicHeight());
        }
    }
    
    private int findDayOffset() {
        int dayOfWeek = dateParam.dayOfWeekStart;
        if (dayOfWeek < showParam.showStartDayOfWeek) {
            dayOfWeek += DAYS_OF_WEEK;
        }
        
        return dayOfWeek - showParam.showStartDayOfWeek;
    }
    
    private int calcRowCount() {
        int offset = findDayOffset();
        int dividend = (offset + dateParam.days) / DAYS_OF_WEEK;
        int remainder = (offset + dateParam.days) % DAYS_OF_WEEK;
        return (dividend + (remainder > 0 ? 1 : 0));
    }
    
    private int dayByTouch(float x, float y) {
        
        if (y <= showParam.titleHeight())
            return 0;
        
        final int dayOffset = findDayOffset();
        final int cellWidth = width / DAYS_OF_WEEK;
        final int cellHeight = showParam.rowHeight;
        int day = ((int) y - showParam.titleHeight()) / cellHeight * DAYS_OF_WEEK
                + ((int) x) / cellWidth + 1;
        if (day > dayOffset && day <= dayOffset + dateParam.days) {
            return day - dayOffset;
        }
        else {
            if(day <= dayOffset){
                //上个月的格子
                int daysPrevMonth =dateParam.daysPrevMonth;
                dateParam.month -= 1;
               return daysPrevMonth - (dayOffset-day);
            }else{
                //下个月
                if(day > dayOffset + dateParam.days){
                    dateParam.month += 1;
                    return day - dayOffset - dateParam.days;
                }else{
                    return 0;
                }
            }
        }
    }

    private int dayByTouch(float x) {
        final int cellWidth = width / DAYS_OF_WEEK;
        int dayIndex = ((int) x) / cellWidth;
        Day day;
        //fix crash 2312
        if(dayIndex >= weekDaysByIndex.size()){
            day = weekDaysByIndex.get(weekDaysByIndex.size()-1);
        }else{
            day = weekDaysByIndex.get(dayIndex);
        }

        dateParam.year = day.getYear();
        dateParam.month = day.getMonth() - 1;
//        dateParam.days = day.getDayOfMonth();
        if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta && dateParam.courseCalendarDays != null){
            com.qingqing.project.offline.calendar.Day day1 = dateParam.courseCalendarDays.get(dayIndex).getCalendarDay().getDay();
            dateParam.year = day1.getYear();
            dateParam.month = day1.getMonth() - 1;
            return day1.getDayOfMonth();
        }else{
            return day.getDayOfMonth();
        }

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int day = 0;
            if(rowCount == 1){
                day = dayByTouch(event.getX());
            }else{
                day = dayByTouch(event.getX(), event.getY());
            }
            if (day > 0) {
                dateParam.selectDate = day;
                invalidate();
                if (selectCallback != null) {
                    selectCallback.onDaySelected(dateParam.year, dateParam.month, day);
                }
            }
        }
        return true;
    }

    private boolean isToday(int year, int month, int day) {
        return todayCalendar.get(Calendar.YEAR) == year
                && todayCalendar.get(Calendar.MONTH) == month
                && todayCalendar.get(Calendar.DAY_OF_MONTH) == day;
    }
    
    private boolean isPastedDay(int year, int month, int day) {
        return todayCalendar.get(Calendar.YEAR) == year
                && todayCalendar.get(Calendar.MONTH) == month
                && day < todayCalendar.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isSpecifiedWeek(int year, int month, int day, int exactWeek) {
        if (exactWeek <= 0 || exactWeek > 7) {
            return true;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        return week == exactWeek;
    }

    private void drawWeekDays(Canvas canvas) {
        int x, y;
        // int paddingDay = (width/* - 2 * mPadding */) / (2 * DAYS_OF_WEEK);
        final int dayOffset = findDayOffset();
        final int cellWidth = width / DAYS_OF_WEEK;
        final int halfCellWidth = cellWidth / 2;
        int top, bottom;
        int day;

        todayCalendar.setTimeInMillis(NetworkTime.currentTimeMillis());


        Paint.FontMetricsInt fontMetrics = monthPaint.getFontMetricsInt();
        weekDaysByIndex = CalendarController.getWeekDaysByIndex(weekIndex);

        for (int i = 0; i < DAYS_OF_WEEK; i++) {
            String dateText = "";
            top = showParam.titleHeight() + (i / DAYS_OF_WEEK) * showParam.rowHeight;
            bottom = top + showParam.rowHeight;

            y = (top + bottom - fontMetrics.bottom - fontMetrics.top) / 2;

            x = (i % DAYS_OF_WEEK) * cellWidth + halfCellWidth;

            Day weekDay = weekDaysByIndex.get(i);
            day = weekDay.getDayOfMonth();
            int month = 0;
            if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta && dateParam.courseCalendarDays != null){
                day = dateParam.courseCalendarDays.get(i).getCalendarDay().getDay().getDayOfMonth();
                month = dateParam.courseCalendarDays.get(i).getCalendarDay().getDay().getMonth();
            }else{
                month = weekDay.getMonth();
            }
            if(month -1 ==  dateParam.month){
                  // 当月的天
                  boolean todayNotSelected = false;
                  if (dateParam.isSelectDay(dateParam.year, dateParam.month, day)) {

                      switch (showParam.dateSelectBgShape) {
                          case CIRCLE:
                              canvas.drawCircle(x, (top + bottom) >> 1,
                                      showParam.rowHeight/3-DisplayUtil.dp2px(4), selectDayBgPaint);
                              break;
                          case ROUND_RECT:
                              // 缩小半径，取高度*0.4或者宽度*0.3的较大值
                              int rectRadius = Math.max((int) (cellWidth * 0.3),
                                      (int) ((bottom - top) * 0.4));
                              int centerY = (bottom + top) / 2;
                              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                  canvas.drawRoundRect(x - rectRadius, centerY - rectRadius,
                                          x + rectRadius, centerY + rectRadius, 6, 6,
                                          selectDayBgPaint);
                              }
                              else {
                                  canvas.drawRect(x - rectRadius, centerY - rectRadius,
                                          x + rectRadius, centerY + rectRadius,
                                          selectDayBgPaint);
                              }
                              break;
                          case RECT:
                              canvas.drawRect(x - halfCellWidth, top, x + halfCellWidth, bottom,
                                      selectDayBgPaint);
                              break;
                      }

                      dayPaint.setFakeBoldText(true);
                      dayPaint.setColor(showParam.dateSelectColor);
                  }
                  else if (isToday(dateParam.year, dateParam.month, day)) {
                      dayPaint.setFakeBoldText(true);
                      dayPaint.setColor(showParam.dateTodayColor);
                      todayNotSelected = true;
                  }
                  else if (isPastedDay(dateParam.year, dateParam.month, day)) {
                      dayPaint.setFakeBoldText(false);
                      dayPaint.setColor(showParam.datePassedColor);
                  }
                  // check if matches specified week @5.9.6
                  else if(!isSpecifiedWeek(dateParam.year, dateParam.month, day, dateParam.specifiedWeek)){
                      dayPaint.setFakeBoldText(false);
                      dayPaint.setColor(showParam.datePassedColor);
                  }
                  else {
                      dayPaint.setFakeBoldText(false);
                      dayPaint.setColor(showParam.dateColor);
                  }

                  if (todayNotSelected && showParam.dateTodayDrawable != null) {
                      int drawableHalfWidth = showParam.rowHeight/3-DisplayUtil.dp2px(4);
                      int drawableHalfHeight = showParam.rowHeight/3-DisplayUtil.dp2px(4);
                      Rect dst = new Rect(x - drawableHalfWidth,
                              top + showParam.rowHeight / 2 - drawableHalfHeight,
                              x + drawableHalfWidth,
                              top + showParam.rowHeight / 2 + drawableHalfHeight);

                      showParam.dateTodayDrawable.setBounds(dst);
                      showParam.dateTodayDrawable.draw(canvas);
                      canvas.drawText(String.format(Locale.CHINA, "%d", day), x, y,
                              dayPaint);
                  }
                  else {
                      canvas.drawText(String.format(Locale.CHINA, "%d", day), x, y,
                              dayPaint);
                  }
                if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta){
                    drawTaStatus(dateText,i,canvas,x,y);
                }else{
                    drawStatus(dateText,i,canvas,x,y);
                }
                  dayDisablePaint.setColor(getResources().getColor(R.color.gray));
                  canvas.drawLine(x-showParam.rowHeight/2-DisplayUtil.dp2px(10),y + DisplayUtil.dp2px(25),x+showParam.rowHeight-DisplayUtil.dp2px(25),y + DisplayUtil.dp2px(25),dayDisablePaint);

              }else{
                if (showParam.showPreAndNextDays) {
                    canvas.drawText(String.format(Locale.CHINA, "%d", day), x, y,
                            dayDisablePaint);
                }
                if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta){
                    drawTaStatus(dateText,i,canvas,x,y);
                }else{
                    drawStatus(dateText,i,canvas,x,y);
                }
                dayDisablePaint.setColor(getResources().getColor(R.color.gray));
                canvas.drawLine(x-showParam.rowHeight/2-DisplayUtil.dp2px(10),y + DisplayUtil.dp2px(25),x+showParam.rowHeight-DisplayUtil.dp2px(25),y + DisplayUtil.dp2px(25),dayDisablePaint);
            }
        }
    }
    private void drawDays(Canvas canvas) {
        int x, y;
        // int paddingDay = (width/* - 2 * mPadding */) / (2 * DAYS_OF_WEEK);
        final int dayOffset = findDayOffset();
        final int cellWidth = width / DAYS_OF_WEEK;
        final int halfCellWidth = cellWidth / 2;
        int top, bottom;
        int day;
        
        todayCalendar.setTimeInMillis(NetworkTime.currentTimeMillis());
        
        // final int[] colors =
        // {Color.BLUE,Color.YELLOW,Color.CYAN,Color.MAGENTA,Color.RED,Color.GREEN,Color.BLUE};
        
        Paint.FontMetricsInt fontMetrics = monthPaint.getFontMetricsInt();

        for (int i = 0; i < rowCount * DAYS_OF_WEEK; i++) {
            String dateText = "";
            top = showParam.titleHeight() + (i / DAYS_OF_WEEK) * showParam.rowHeight;
            bottom = top + showParam.rowHeight;
            
            y = (top + bottom - fontMetrics.bottom - fontMetrics.top) / 2;

            x = (i % DAYS_OF_WEEK) * cellWidth + halfCellWidth;

//            if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta){
//                testBgPaint.setColor(getResources().getColor(R.color.white));
//                canvas.drawRect(0, showParam.titleHeight() + (i / DAYS_OF_WEEK) *
//                                showParam.rowHeight, cellWidth,
//                        showParam.titleHeight() + (i / DAYS_OF_WEEK) *
//                                showParam.rowHeight + showParam.rowHeight, testBgPaint);
//            }

            if (i < dayOffset) {
                // 前一个月的几天
                if (showParam.showPreAndNextDays) {
                    day = dateParam.daysPrevMonth - (dayOffset - i) + 1;
                    canvas.drawText(String.format(Locale.CHINA, "%d", day), x, y,
                            dayDisablePaint);
                }
                if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta){
                    drawTaStatus(dateText,i,canvas,x,y);
                }else{
                    drawStatus(dateText,i,canvas,x,y);
                }
                dayDisablePaint.setColor(getResources().getColor(R.color.gray));
                canvas.drawLine(x-showParam.rowHeight/2-DisplayUtil.dp2px(10),y + DisplayUtil.dp2px(25),x+showParam.rowHeight-DisplayUtil.dp2px(25),y + DisplayUtil.dp2px(25),dayDisablePaint);
            }
            else if (i >= (dateParam.days + dayOffset)) {
                // 后一个月的前几天
                if (showParam.showPreAndNextDays) {
                    day = i + 1 - (dateParam.days + dayOffset);
                    canvas.drawText(String.format(Locale.CHINA, "%d", day), x, y,
                            dayDisablePaint);
                }
                if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta){
                    drawTaStatus(dateText,i,canvas,x,y);
                }else{
                    drawStatus(dateText,i,canvas,x,y);
                }
                dayDisablePaint.setColor(getResources().getColor(R.color.gray));
                canvas.drawLine(x-showParam.rowHeight/2-DisplayUtil.dp2px(10),y + DisplayUtil.dp2px(25),x+showParam.rowHeight-DisplayUtil.dp2px(25),y + DisplayUtil.dp2px(25),dayDisablePaint);
            }
            else {
                // 当月的天
                day = i + 1 - dayOffset;
                boolean todayNotSelected = false;
                if (dateParam.isSelectDay(dateParam.year, dateParam.month, day)) {
                    
                    switch (showParam.dateSelectBgShape) {
                        case CIRCLE:
                            canvas.drawCircle(x, (top + bottom) >> 1,
                                    showParam.rowHeight/3-DisplayUtil.dp2px(4), selectDayBgPaint);
                            break;
                        case ROUND_RECT:
                            // 缩小半径，取高度*0.4或者宽度*0.3的较大值
                            int rectRadius = Math.max((int) (cellWidth * 0.3),
                                    (int) ((bottom - top) * 0.4));
                            int centerY = (bottom + top) / 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                canvas.drawRoundRect(x - rectRadius, centerY - rectRadius,
                                        x + rectRadius, centerY + rectRadius, 6, 6,
                                        selectDayBgPaint);
                            }
                            else {
                                canvas.drawRect(x - rectRadius, centerY - rectRadius,
                                        x + rectRadius, centerY + rectRadius,
                                        selectDayBgPaint);
                            }
                            break;
                        case RECT:
                            canvas.drawRect(x - halfCellWidth, top, x + halfCellWidth, bottom,
                                    selectDayBgPaint);
                            break;
                    }
                    
                    dayPaint.setFakeBoldText(true);
                    dayPaint.setColor(showParam.dateSelectColor);
                }
                else if (isToday(dateParam.year, dateParam.month, day)) {
                    dayPaint.setFakeBoldText(true);
                    dayPaint.setColor(showParam.dateTodayColor);
                    todayNotSelected = true;
                }
                else if (isPastedDay(dateParam.year, dateParam.month, day)) {
                    dayPaint.setFakeBoldText(false);
                    dayPaint.setColor(showParam.datePassedColor);
                }
                // check if matches specified week @5.9.6
                else if(!isSpecifiedWeek(dateParam.year, dateParam.month, day, dateParam.specifiedWeek)){
                    dayPaint.setFakeBoldText(false);
                    dayPaint.setColor(showParam.datePassedColor);
                }
                else {
                    dayPaint.setFakeBoldText(false);
                    dayPaint.setColor(showParam.dateColor);
                }

                final int theDay = i + 1 - dayOffset;
                if (todayNotSelected && showParam.dateTodayDrawable != null) {
                    int drawableHalfWidth = showParam.rowHeight/3-DisplayUtil.dp2px(4);
                    int drawableHalfHeight = showParam.rowHeight/3-DisplayUtil.dp2px(4);
                    Rect dst = new Rect(x - drawableHalfWidth,
                            top + showParam.rowHeight / 2 - drawableHalfHeight,
                            x + drawableHalfWidth,
                            top + showParam.rowHeight / 2 + drawableHalfHeight);

                    showParam.dateTodayDrawable.setBounds(dst);
                    showParam.dateTodayDrawable.draw(canvas);
                    canvas.drawText(String.format(Locale.CHINA, "%d", theDay), x, y,
                            dayPaint);
                }
                else {
                    canvas.drawText(String.format(Locale.CHINA, "%d", theDay), x, y,
                            dayPaint);
                }
                if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta){
                    drawTaStatus(dateText,i,canvas,x,y);
                }else{
                    drawStatus(dateText,i,canvas,x,y);
                }
                dayDisablePaint.setColor(getResources().getColor(R.color.gray));
                canvas.drawLine(x-showParam.rowHeight/2-DisplayUtil.dp2px(10),y + DisplayUtil.dp2px(25),x+showParam.rowHeight-DisplayUtil.dp2px(25),y + DisplayUtil.dp2px(25),dayDisablePaint);
            }
        }
    }

    private void drawStatus(String dateText,int position,Canvas canvas,int x,int y){
        if(dateParam.courseDays!= null){
            CourseDay itemDay = dateParam.courseDays.get(position);

            final int flag = itemDay.getFlag();

            boolean hasClass = (flag & CourseDay.FLAG_HAS_CLASS) != 0;

            boolean isRemain = (flag & CourseDay.FLAG_HAS_CLASS_ATTENTIONS) != 0;

            boolean isFinished = (flag & CourseDay.FLAG_HAS_CLASS_COMPLETE) != 0;

            Day today = new Day(NetworkTime.currentTimeMillis());

            boolean isToday = Day.isSameDay(itemDay, today);

            boolean isAfterToday = Day.isAfterToday(itemDay, today);

            int hasClassTextColor = R.color.gray;

            if (isRemain) {
                hasClassTextColor = R.color.accent_red_light;
                dateText = "未结";
            }
            else if (isFinished) {
                dateText = "已结";
            }
            else if (hasClass) {
                if(isToday || isAfterToday){
                    hasClassTextColor = R.color.primary_blue;
                }
                dateText = "有课";
            }
            if (!TextUtils.isEmpty(dateText)) {
                statusPaint.setColor(getResources().getColor(hasClassTextColor));
                canvas.drawText(dateText, x,
                        y + (showParam.dateFontSize >> 1) + DisplayUtil.dp2px(12),
                        statusPaint);
            }
        }
    }
    private void drawTaStatus(String dateText,int position,Canvas canvas,int x,int y){
        if(dateParam.courseCalendarDays!= null){
            CourseCalendarDay courseDay = dateParam.courseCalendarDays.get(position);

            final CalendarDay calendarDay = courseDay.getCalendarDay();
            final com.qingqing.project.offline.calendar.Day day = calendarDay.getDay();
            final CalendarViewType viewType = courseDay.getCalendarViewType();
            final boolean isSelected = courseDay.isSelected();
            final CourseCalendarDay.Status status = courseDay.getStatus();
            final boolean isToday = TimeUtil.isToday(day.getTimeMillis());
            final boolean isAfterToday = TimeUtil.isAfterToday(day.getTimeMillis());

//            int dateColorResId;
//            int dateBgResId;
            int statusColorResId;
            String statusText;


//            if (isSelected) {
//                dateBgResId = R.drawable.bg_course_calendar_day_selected;
//            } else if (isToday) {
//                dateBgResId = R.drawable.bg_course_calendar_today;
//            } else {
//                dateBgResId = R.drawable.bg_course_calendar_day_normal;
//            }
//
//            if (isSelected) {
//                dateColorResId = R.color.white;
//            } else if (isToday) {
//                dateColorResId = R.color.primary_orange;
//            } else if (isAfterToday) {
//                dateColorResId = R.color.black_light;
//            } else {
//                dateColorResId = R.color.gray;
//            }

            if (isToday || isAfterToday) {
                statusColorResId = R.color.primary_orange;
            } else {
                statusColorResId = R.color.gray;
            }

            if (status == CourseCalendarDay.Status.HAS_CLASS) {
                if(courseDay.getCourseCount() > 0){
                    statusText = LogicConfig.getFormatDotString(courseDay.getCourseCount())+"课时";
                }else{
                    statusText = "";
                }
            } else {
                statusText = "";
            }

            //月视图
            if (viewType == CalendarViewType.MONTH) {
                final CalendarMonth month = calendarDay.getMonth();
                //月视图中不在当月的特殊处理
                if (!month.isDayInRealMonth(day.getTimeMillis())) {
//                    if (!isSelected && !isToday) {
//                        dateColorResId = R.color.gray;
//                    }
                    statusColorResId = R.color.gray;
                }
            }
            dateText = statusText;
            if (!TextUtils.isEmpty(dateText)) {
                statusPaint.setColor(getResources().getColor(statusColorResId));
                canvas.drawText(dateText, x,
                        y + (showParam.dateFontSize >> 1) + DisplayUtil.dp2px(12),
                        statusPaint);
            }
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //drawMonthTitle(canvas);
       // drawWeekTitle(canvas);
        if(rowCount == 1){
            drawWeekDays(canvas);
        }else{
            drawDays(canvas);
        }

    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i("xxx", "onmeasure this is " + hashCode() + " showp is " + (showParam == null ? "null" : showParam.hashCode()));
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                showParam.rowHeight * rowCount + showParam.titleHeight());
    }
    
    public static class DateParam {
        private int year;
        private int month;
        private int yearOfPrevMonth;
        private int prevMonth;
        private int selectDate;
        private int days;
        private int daysPrevMonth;
        /** 当月的第一天是周几 */
        private int dayOfWeekStart;
        // /** 当月的最后一天是周几 */
        // private int dayOfWeekEnd;
        private Calendar calendar;
        private ArrayList<Integer> indicationDays;// 需要提示显示的天
        private ArrayList<CourseDay> courseDays;//日历day
        private ArrayList<CourseCalendarDay> courseCalendarDays;//日历taday
        private int rowCount;

        private int specifiedWeek;// 指定可以选择的星期，其他星期不可选
        
        private DateParam(Builder builder) {
            
            calendar = Calendar.getInstance();
            if (builder.year > 0) {
                calendar.set(Calendar.YEAR, builder.year);
            }
            if (builder.month >= 0) {
                calendar.set(Calendar.MONTH, builder.month);
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            days = CalendarUtil.daysOfMonth(year, month);
            if (month == Calendar.JANUARY) {
                yearOfPrevMonth = year - 1;
                prevMonth = Calendar.DECEMBER;
            }
            else {
                yearOfPrevMonth = year;
                prevMonth = month - 1;
            }
            
            daysPrevMonth = CalendarUtil.daysOfMonth(yearOfPrevMonth, prevMonth);
            dayOfWeekStart = calendar.get(Calendar.DAY_OF_WEEK);
            // dayOfWeekEnd = calendar.get(Calendar.DAY_OF_WEEK);
            if (builder.selectDate > 0) {
                selectDate = builder.selectDate;
            }

            if(builder.monthStatus != null){
                courseDays = new ArrayList<>();
                courseDays.addAll(builder.monthStatus);
            }
            if(builder.monthStatusTA != null){
                courseCalendarDays = new ArrayList<>();
                courseCalendarDays.addAll(builder.monthStatusTA);
            }
            if(builder.rowCount > 0){
                rowCount = builder.rowCount;
            }
            specifiedWeek = builder.specifiedWeek;
        }
        
        boolean isSelectDay(long year, long month, long day) {
            return year == this.year && month == this.month && day == selectDate;
        }
        
        public static class Builder {
            
            private int year = -1;
            private int month = -1;
            private int selectDate = -1;
            private ArrayList<CourseDay> monthStatus;
            private ArrayList<CourseCalendarDay> monthStatusTA;
            private int rowCount = 6;
            private int specifiedWeek;
            
            public Builder() {}
            public Builder setRowCount(int rowCount) {
                this.rowCount = rowCount;
                return this;
            }
            public Builder setYear(int year) {
                this.year = year;
                return this;
            }
            
            public Builder setMonth(int month) {
                this.month = month;
                return this;
            }
            
            public Builder setSelectDate(int date) {
                this.selectDate = date;
                return this;
            }

            public Builder setSpecifiedWeek(int week) {
                this.specifiedWeek = week;
                return this;
            }

            public Builder addMonthDay(ArrayList<CourseDay> days){
                if(monthStatus == null){
                    monthStatus = new ArrayList<>();
                }else{
                    monthStatus.clear();
                }
                monthStatus.addAll(days);
                return this;
            }
            public Builder addMonthDayStatus(ArrayList<CourseCalendarDay> days){
                if(monthStatusTA == null){
                    monthStatusTA = new ArrayList<>();
                }else{
                    monthStatusTA.clear();
                }
                monthStatusTA.addAll(days);
                return this;
            }
            public DateParam build() {
                return new DateParam(this);
            }
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }
    }
    
    public static class ShowParam {
        
        public enum BgShape {
            CIRCLE, ROUND_RECT, RECT
        }
        
        private int monthTitleHeight;
        private int rowHeight;
        private int showStartDayOfWeek;
        private int bgColor;
        
        private int titleColor;
        private int titleFontSize;
        
        private int weekColor;
        private int weekFontSize;
        
        private int dateColor;
        private int datePassedColor;
        private int dateTodayColor;
        private Drawable dateTodayDrawable;
        private int dateFontSize;
        
        private int dateUnSelectColor;
        private int dataUnSelectFontSize;
        private int dataStatusFontSize;
        private int dateSelectColor;
        private int dateSelectBgColor;
        private BgShape dateSelectBgShape;
        private boolean showWeekTitle;
        private boolean showPreAndNextDays;
        private int dateIndColor;

        private ShowParam(Builder builder) {
            monthTitleHeight = builder.monthTitleHeight;
            rowHeight = builder.rowHeight;
            showStartDayOfWeek = builder.showStartDayOfWeek;
            bgColor = builder.bgColor;
            titleColor = builder.titleColor;
            titleFontSize = builder.titleFontSize;
            weekColor = builder.weekColor;
            weekFontSize = builder.weekFontSize;
            dateColor = builder.dateColor;
            dateTodayColor = builder.dateTodayColor;
            dateTodayDrawable = builder.dateTodayDrawable;
            datePassedColor = builder.datePassedColor;
            dateFontSize = builder.dateFontSize;
            dateUnSelectColor = builder.dateUnSelectColor;
            dataUnSelectFontSize = builder.dataUnSelectFontSize;
            dataStatusFontSize = builder.dataStatusFontSize;
            dateSelectColor = builder.dateSelectColor;
            dateSelectBgColor = builder.dateSelectBgColor;
            dateSelectBgShape = builder.dateSelectBgShape;
            showWeekTitle = builder.showWeekTitle;
            showPreAndNextDays = builder.showPreAndNextDays;
            dateIndColor = builder.dateIndColor;
        }
        
        public int titleHeight() {
            int height = monthTitleHeight();
            if (showWeekTitle) {
                height += weekFontSize;
            }
            
            return height;
        }
        
        public int monthTitleHeight() {
            return monthTitleHeight;
        }
        
        public static class Builder {
            
            private int monthTitleHeight = DisplayUtil.dp2px(24);
            private int rowHeight = DisplayUtil.dp2px(35);
            
            private int showStartDayOfWeek = Calendar.SUNDAY;
            private int bgColor = Color.TRANSPARENT;
            
            private int titleColor = Color.DKGRAY;
            private int titleFontSize = DisplayUtil.dp2px(14);
            
            private int weekColor = Color.GRAY;
            private int weekFontSize = DisplayUtil.dp2px(12);
            
            private int dateColor = Color.GRAY;
            private int datePassedColor = dateColor;
            private int dateTodayColor = Color.YELLOW;
            private Drawable dateTodayDrawable;
            private int dateFontSize = DisplayUtil.dp2px(12);
            
            private int dateUnSelectColor = Color.LTGRAY;
            private int dataUnSelectFontSize = DisplayUtil.dp2px(12);

            private int dataStatusFontSize = DisplayUtil.dp2px(10);
            private int dateSelectColor = Color.WHITE;
            private int dateSelectBgColor = Color.CYAN;
            private BgShape dateSelectBgShape = BgShape.CIRCLE;
            
            private int dateIndColor = Color.DKGRAY;
            
            private boolean showWeekTitle = true;
            private boolean showPreAndNextDays = true;

            public Builder() {}
            
            public Builder setMonthTitleHeight(int monthTitleHeight) {
                this.monthTitleHeight = monthTitleHeight;
                return this;
            }
            
            public Builder setRowHeight(int rowHeight) {
                this.rowHeight = rowHeight;
                return this;
            }
            
            public Builder setShowStartDayOfWeek(int showStartDayOfWeek) {
                this.showStartDayOfWeek = showStartDayOfWeek;
                return this;
            }
            
            public Builder setBgColor(int bgColor) {
                this.bgColor = bgColor;
                return this;
            }
            
            public Builder setTitleColor(int titleColor) {
                this.titleColor = titleColor;
                return this;
            }
            
            public Builder setTitleFontSize(int sizePx) {
                this.titleFontSize = sizePx;
                return this;
            }
            
            public Builder setWeekColor(int weekColor) {
                this.weekColor = weekColor;
                return this;
            }
            
            public Builder setWeekFontSize(int sizePx) {
                this.weekFontSize = sizePx;
                return this;
            }
            
            public Builder setDateColor(int dateColor) {
                this.dateColor = dateColor;
                return this;
            }
            
            public Builder setDateTodayColor(int dateColor) {
                this.dateTodayColor = dateColor;
                return this;
            }

            public Builder setDateTodayDrawable(Drawable drawable) {
                this.dateTodayDrawable = drawable;
                return this;
            }
            
            public Builder setDatePassedColor(int dateColor) {
                this.datePassedColor = dateColor;
                return this;
            }
            
            public Builder setDateFontSize(int sizePx) {
                this.dateFontSize = sizePx;
                return this;
            }
            
            public Builder setDateUnSelectColor(int dateColor) {
                this.dateUnSelectColor = dateColor;
                return this;
            }
            
            public Builder setDateUnSelectFontSize(int sizePx) {
                this.dataUnSelectFontSize = sizePx;
                return this;
            }
            
            public Builder setDateSelectColor(int dateSelectColor) {
                this.dateSelectColor = dateSelectColor;
                return this;
            }
            
            public Builder setDateSelectBgColor(int dateSelectBgColor) {
                this.dateSelectBgColor = dateSelectBgColor;
                return this;
            }
            
            public Builder setDateSelectBgShape(BgShape shape) {
                this.dateSelectBgShape = shape;
                return this;
            }
            
            public Builder setShowWeekTitle(boolean show) {
                this.showWeekTitle = show;
                return this;
            }
            
            public Builder setShowPrevAndNextDays(boolean show) {
                this.showPreAndNextDays = show;
                return this;
            }
            
            public Builder setDateIndicationColor(int color) {
                this.dateIndColor = color;
                return this;
            }

            
            public ShowParam build() {
                return new ShowParam(this);
            }
        }
    }
}
