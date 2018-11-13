package com.qingqing.project.offline.view.ncalendar;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.qingqing.base.view.pager.PagedView;

/**
 * Created by huangming on 2017/1/6.
 */

class CalendarContentLayout extends LinearLayout implements CalendarNestedScrollingChild {

    private CalendarWidget mCalendarWidget;
    private CalendarNestedScrollingParent mScrollingParent;

    CalendarContentLayout(Context context, CalendarNestedScrollingParent scrollingParent) {
        super(context);
        setOrientation(VERTICAL);
        mScrollingParent = scrollingParent;

        scrollingParent.setNestedScrollingChild(this);
    }

    CalendarWidget getCalendarWidget() {
        return mCalendarWidget;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        int childCount = getChildCount();
        int rearrangedIndex = index;
        if (child instanceof CalendarWidget) {
            if (childCount > 0) {
                rearrangedIndex = 0;
            }
            if (mCalendarWidget == null) {
                mCalendarWidget = (CalendarWidget) child;
                mScrollingParent.setOnScrollChangeListener(mCalendarWidget.getOnScrollChangeListener());
            } else {
                throw new IllegalStateException("Multiple CalendarWidget");
            }
        } else {
            if (mCalendarWidget != null) {
                if (index >= 0) {
                    rearrangedIndex = index + 1;
                }
            }

            if (child instanceof DirectionIndicator) {
                mScrollingParent.setScrollingDirectionIndicator((DirectionIndicator) child);
            }
        }
        super.addView(child, rearrangedIndex, params);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (mCalendarWidget != null) {
            measureChild(mCalendarWidget, widthMeasureSpec, heightMeasureSpec);
            int minVisibleHeightOfWidget = mCalendarWidget.getMinVisibleMeasuredHeight();
            int maxVisibleHeightOfWidget = mCalendarWidget.getMaxVisibleMeasuredHeight();
            if (minVisibleHeightOfWidget < heightSize && minVisibleHeightOfWidget < maxVisibleHeightOfWidget) {
                super.onMeasure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(heightSize - minVisibleHeightOfWidget + maxVisibleHeightOfWidget, MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
            }
        } else {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
        }
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return !isListOnTop();
    }

    private boolean isListOnTop() {
        AdapterView adapterView = getAdapterViewFrom(mCalendarWidget.getDayPagedView());
        if (adapterView == null) {
            return true;
        }
        int count = adapterView.getCount();
        if (count <= 0) {
            return true;
        }
        if (adapterView != null && adapterView.getVisibility() == VISIBLE) {
            int firstVisiblePosition = adapterView.getFirstVisiblePosition();
            View firstView = adapterView.getChildAt(0);
            int listPaddingTop = adapterView.getPaddingTop();
            return firstVisiblePosition == 0 && firstView != null
                    && firstView.getTop() == listPaddingTop;
        }
        return true;
    }

    private AdapterView getAdapterViewFrom(PagedView pagedView) {
        if (pagedView == null) {
            return null;
        }
        View currentView = pagedView.getCurrentVisibleView();
        if (currentView instanceof ViewGroup) {
            return getChildAdapterView((ViewGroup) currentView);
        }
        return null;
    }

    private AdapterView getChildAdapterView(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof AdapterView) {
                return (AdapterView) child;
            } else if (child instanceof ViewGroup) {
                View adapterView = getChildAdapterView((ViewGroup) child);
                if (adapterView instanceof AdapterView) {
                    return (AdapterView) adapterView;
                }
            }
        }
        return null;
    }
}
