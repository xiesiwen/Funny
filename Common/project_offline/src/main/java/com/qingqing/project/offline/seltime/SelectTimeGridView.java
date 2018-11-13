package com.qingqing.project.offline.seltime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.GridView;

import com.qingqing.project.offline.R;


/**
 * 课时选择 显示时间段的GridView，布局于ScrollView，不可滑动
 * 
 * @author tanwei
 */

public class SelectTimeGridView extends GridView {
    
    public SelectTimeGridView(Context context) {
        super(context);
        init();
    }
    
    public SelectTimeGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init();
    }
    
    // 设置ui属性
    private void init() {
        setGravity(Gravity.CENTER);
        int dimen = getResources().getDimensionPixelOffset(R.dimen.dimen_6);
        setHorizontalSpacing(dimen);
        setVerticalSpacing(dimen);
        setStretchMode(STRETCH_COLUMN_WIDTH);
        setNumColumns(6);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        
        // 禁止滑动
        return ev.getAction() == MotionEvent.ACTION_MOVE || super.dispatchTouchEvent(ev);
    }
    
}
