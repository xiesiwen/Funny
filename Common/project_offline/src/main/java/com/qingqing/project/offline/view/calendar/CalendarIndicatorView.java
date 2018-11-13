package com.qingqing.project.offline.view.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.qingqing.project.offline.R;


/**
 * Created by huangming on 2016/12/9.
 */

public class CalendarIndicatorView extends RelativeLayout {

    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_DOWN = 1;

    private int mDirection = DIRECTION_UP;

    private View mViewIndicator;

    public CalendarIndicatorView(Context context) {
        super(context);
    }

    public CalendarIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setIndicatorDirection(int direction) {
        if (mDirection != direction) {
            mDirection = direction;
            onIndicatorDirectionChanged(direction);
        }
    }

    protected void onIndicatorDirectionChanged(int direction) {
        dispatchLevel(direction);
    }

    protected void dispatchLevel(int level) {
        if (getBackground() != null) {
            getBackground().setLevel(level);
        }
        if(mViewIndicator != null && mViewIndicator.getBackground() !=null){
            mViewIndicator.getBackground().setLevel(level);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViewIndicator = findViewById(R.id.v_indicator);
    }
}
