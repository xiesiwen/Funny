package com.qingqing.project.offline.order;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.DisplayUtil;
import com.qingqing.project.offline.R;

import java.util.Calendar;

/**
 * Created by wangxiaxin on 2017/8/8.
 *
 * 日历中的 周标题
 */

public class CalendarWeekTitleView extends View {
    
    /** 一周几天 定值，也固定了显示列数 */
    private static final int DAYS_OF_WEEK = 7;
    
    private String[] weekContents = new String[DAYS_OF_WEEK];
    private Paint weekPaint;
    private int startDayOffset = 0;
    
    public CalendarWeekTitleView(Context context) {
        super(context);
        init(context, null);
    }
    
    public CalendarWeekTitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public CalendarWeekTitleView(Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        
        try {
            String[] defaultContents = context.getResources()
                    .getStringArray(R.array.simplest_week);
            setContent(defaultContents);
        } catch (Exception e) {
            Logger.w(e);
        }

        setStartDayOfWeek(Calendar.MONDAY);

        weekPaint = new Paint();
        weekPaint.setAntiAlias(true);
        weekPaint.setColor(Color.DKGRAY);
        // weekPaint.setTypeface(Typeface.create(mDayOfWeekTypeface,
        // Typeface.NORMAL));
        weekPaint.setStyle(Paint.Style.FILL);
        weekPaint.setTextAlign(Paint.Align.CENTER);
        // weekPaint.setFakeBoldText(true);
        
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CalendarWeekTitleView);
            setTextSize(a.getDimensionPixelSize(
                    R.styleable.CalendarWeekTitleView_android_textSize,
                    DisplayUtil.sp2px(14)));
            weekPaint.setColor(a.getColor(
                    R.styleable.CalendarWeekTitleView_android_textColor, Color.DKGRAY));
            setContent(a.getString(R.styleable.CalendarWeekTitleView_android_text));
            a.recycle();
        }
    }
    
    public CalendarWeekTitleView setTextSize(@Px int textSize) {
        if (weekPaint != null) {
            weekPaint.setTextSize(textSize);
        }
        return this;
    }
    
    /**
     * 以逗号分割的标题，必须按照 周一 ~ 周日的顺序赋值，可以使用setShowOrder来调整显示顺序
     */
    public CalendarWeekTitleView setContent(String content) {
        if (!TextUtils.isEmpty(content)) {
            final String[] splitContents = content.split(",");
            if (splitContents.length >= DAYS_OF_WEEK) {
                System.arraycopy(splitContents, 0, this.weekContents, 0, DAYS_OF_WEEK);
            }
        }
        return this;
    }
    
    /**
     * 必须保证 周一 ~ 周日的顺序
     */
    public CalendarWeekTitleView setContent(String[] contents) {
        if (contents != null && contents.length >= DAYS_OF_WEEK) {
            System.arraycopy(contents, 0, this.weekContents, 0, DAYS_OF_WEEK);
        }
        return this;
    }
    
    /**
     * 设置第一个显示的是周几
     *
     * @param startDayOfWeek
     *            {@link java.util.Calendar#SUNDAY}
     */
    public CalendarWeekTitleView setStartDayOfWeek(int startDayOfWeek) {
        if(startDayOfWeek >= Calendar.MONDAY){
            startDayOffset = startDayOfWeek - Calendar.MONDAY;
        }else{
            //Calendar.SUNDAY
            startDayOffset = DAYS_OF_WEEK - Calendar.SUNDAY;
        }

        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                Paint.FontMetricsInt fontMetrics = weekPaint.getFontMetricsInt();
                heightSize = getPaddingTop() + getPaddingBottom() + fontMetrics.bottom - fontMetrics.top;
                break;
            case MeasureSpec.EXACTLY:
            default:
                break;
        }

        setMeasuredDimension(widthSize,heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint.FontMetricsInt fontMetrics = weekPaint.getFontMetricsInt();
        int y = (getHeight() - fontMetrics.bottom - fontMetrics.top) / 2;
        int dayWidthHalf = (getWidth() - getPaddingLeft() - getPaddingRight()) / (DAYS_OF_WEEK * 2);
        
        for (int i = 0; i < DAYS_OF_WEEK; i++) {
            int dayOfWeek = (i + startDayOffset) % DAYS_OF_WEEK ;
            int x =getPaddingLeft() +  (2 * i + 1) * dayWidthHalf;
            canvas.drawText(weekContents[dayOfWeek], x,
                    y, weekPaint);
        }
    }
}
