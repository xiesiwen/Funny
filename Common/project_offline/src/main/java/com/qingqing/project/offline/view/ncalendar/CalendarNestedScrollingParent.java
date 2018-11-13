package com.qingqing.project.offline.view.ncalendar;

/**
 * Created by huangming on 2017/2/5.
 */

interface CalendarNestedScrollingParent {

    void scrollToPullDown();

    void scrollToPullUp();

    void setScrollingDirectionIndicator(DirectionIndicator indicator);

    void setOnScrollChangeListener(OnScrollChangeListener l);

    void setNestedScrollingChild(CalendarNestedScrollingChild scrollingChild);

    int getScrollY();

    interface OnScrollChangeListener {
        void onScrollChanged(int y, int oldY);
    }
}
