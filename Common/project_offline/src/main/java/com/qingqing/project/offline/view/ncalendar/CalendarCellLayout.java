package com.qingqing.project.offline.view.ncalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * Created by huangming on 2017/1/19.
 */
public class CalendarCellLayout extends GridView {

    private int mCountX;
    private int mCountY;

    private int mCellWidth;
    private int mCellHeight;

    private int mOriginalCellWidth;
    private int mOriginalCellHeight;

    public CalendarCellLayout(Context context) {
        super(context);
    }

    public CalendarCellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarCellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setGridSize(int countX, int countY) {
        if (countX > 0 && countY > 0) {
            if (countX != mCountX || countY != mCountY) {
                mCountX = countX;
                mCountY = countY;
                setNumColumns(countX);
                requestLayout();
            }
        }
    }

    public void setCellDimensions(int cellWidth, int cellHeight, int widthGap, int heightGap) {
        setHorizontalSpacing(Math.max(0, widthGap));
        setVerticalSpacing(Math.max(0, heightGap));
        if (cellWidth != mOriginalCellWidth || cellHeight != mOriginalCellHeight) {
            mOriginalCellWidth = mCellWidth = cellWidth;
            mOriginalCellHeight = mCellHeight = cellHeight;
            requestLayout();
        }
    }

    public void setGapDrawable() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED && mCellWidth <= 0) {
            throw new RuntimeException("CalendarGridView cannot have Width UNSPECIFIED dimensions and mCellWidth = 0");
        }

        if (heightSpecMode == MeasureSpec.UNSPECIFIED && mCellHeight <= 0) {
            throw new RuntimeException("CalendarGridView cannot have Height UNSPECIFIED dimensions and mCellHeight = 0");
        }

        if (mCountX > 0 && mCountY > 0) {
            int newWidth = widthSpecSize;
            int newHeight = heightSpecSize;

            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();

            int numWidthGaps = mCountX - 1;
            int numHeightGaps = mCountY - 1;

            int widthGap = getHorizontalSpacing();
            int heightGap = getVerticalSpacing();

            if (mOriginalCellWidth > 0) {
                newWidth = paddingLeft + paddingRight + widthGap * numWidthGaps + mCellWidth * mCountX;
            } else {
                mCellWidth = (widthSpecSize - paddingLeft - paddingRight - numWidthGaps * widthGap) / mCountX;
            }
            mCellWidth = Math.max(0, mCellWidth);

            if (mOriginalCellHeight > 0) {
                newHeight = paddingTop + paddingBottom + heightGap * numHeightGaps + mCellHeight * mCountY;
            } else {
                mCellHeight = (heightSpecSize - paddingTop - paddingBottom - numHeightGaps * heightGap) / mCountY;
            }
            mCellHeight = Math.max(0, mCellHeight);
            super.onMeasure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
