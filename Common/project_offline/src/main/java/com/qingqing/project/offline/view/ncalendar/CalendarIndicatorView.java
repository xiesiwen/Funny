package com.qingqing.project.offline.view.ncalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by huangming on 2017/2/8.
 */

public class CalendarIndicatorView extends RelativeLayout implements DirectionIndicator {

    private int mDirection;

    public CalendarIndicatorView(Context context) {
        super(context);
    }

    public CalendarIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onIndicate(int direction) {
        if (mDirection != direction) {
            mDirection = direction;
            dispatchLevel(direction);
        }
    }

    protected void dispatchLevel(int level) {
        if (getBackground() != null) {
            getBackground().setLevel(level);
        }
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getBackground() != null) {
                child.getBackground().setLevel(level);
            }
        }

    }

}
