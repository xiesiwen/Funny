package com.qingqing.base.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by huangming on 2016/9/2.
 */
public class ToggleScreenLinearLayout extends LinearLayout {
    
    private static final String TAG = "ToggleScreenLinearLayout";
    
    private int mScreenOrientation;
    
    public ToggleScreenLinearLayout(Context context) {
        super(context);
        init();
    }
    
    public ToggleScreenLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ToggleScreenLinearLayout(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        Configuration configuration = getResources().getConfiguration();
        setScreenOrientation(configuration != null ? configuration.orientation
                : Configuration.ORIENTATION_PORTRAIT);
    }
    
    public int getScreenOrientation() {
        return mScreenOrientation;
    }
    
    protected void setScreenOrientation(int screenOrientation) {
        mScreenOrientation = screenOrientation;
        requestLayout();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() > 0 && isScreenLandscape()) {
            View lastChild = getChildAt(getChildCount() - 1);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) lastChild
                    .getLayoutParams();
            int childWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                    lp.width);
            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                    lp.height);
            lastChild.measure(childWidthSpec, childHeightSpec);
        }
    }
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setScreenOrientation(newConfig != null ? newConfig.orientation
                : Configuration.ORIENTATION_PORTRAIT);
    }
    
    private boolean isScreenLandscape() {
        return getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE;
    }
}
