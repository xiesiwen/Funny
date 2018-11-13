package com.qingqing.project.offline.seltime;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import com.qingqing.base.log.Logger;
import com.qingqing.qingqingbase.R;

/**
 * 有分割线的GridView（支持在ScrollView里滑动）
 *
 * Created by tanwei on 2016/8/23.
 */
public class DividerLineGridView extends GridView {

    private int numColumns;

    private Drawable mDividerDrawable;
    private int mDividerWidth;
    private int mDividerHeight;

    public DividerLineGridView(Context context) {
        this(context, null);
    }

    public DividerLineGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DividerLineGridView);
        mDividerDrawable = a.getDrawable(R.styleable.DividerLineGridView_android_divider);
        mDividerWidth = a
                .getDimensionPixelSize(R.styleable.DividerLineGridView_dividerWidth, 0);
        mDividerHeight = a.getDimensionPixelSize(
                R.styleable.DividerLineGridView_android_dividerHeight, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            numColumns = getNumColumns();
        }
        else {
            numColumns = 4;// 需要设置
        }

        if (numColumns <= 0) {
            Logger.v("getNumColumns failed " + numColumns);
            numColumns = 4;
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int count = getCount();
        if (count > 0) {
            
            if (mDividerHeight > 0) {
                for (int i = 0; i < count; i += numColumns) {
                    View child = getChildAt(i);
                    if (child != null) {
                        int bottom = child.getBottom();
//                        Logger.v("draw line below " + i);
                        
                        if (mDividerHeight < getHeight() - getPaddingBottom() - bottom) {
                            mDividerDrawable.setBounds(0, bottom - mDividerHeight,
                                    getWidth(), bottom);
                        }
                        else {
                            mDividerDrawable.setBounds(0, bottom, getWidth(),
                                    bottom + mDividerHeight);
                        }
                        mDividerDrawable.draw(canvas);
                    }
                    else {
                        Logger.v("draw", "getChildAt " + i + " null");
                    }
                }
            }
            
            if (mDividerWidth > 0) {
                for (int i = 0; i < numColumns; i++) {
                    View child = getChildAt(i);
                    if (child != null) {
                        int right = child.getRight();
//                        Logger.v("draw line right " + i);
                        mDividerDrawable.setBounds(right, 0, right + mDividerWidth,
                                getHeight());
                        mDividerDrawable.draw(canvas);
                    }
                    else {
                        Logger.v("draw", "getChildAt " + i + " null");
                    }
                }
            }
        }
    }
}
