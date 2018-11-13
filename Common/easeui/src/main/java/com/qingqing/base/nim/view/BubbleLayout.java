package com.qingqing.base.nim.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.easemob.easeui.R;

/**
 * Created by huangming on 2016/8/24.
 */
public class BubbleLayout extends RelativeLayout {
    
    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;
    
    public BubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BubbleLayout);
        final int maxWidth = a
                .getDimensionPixelSize(R.styleable.BubbleImageView_android_maxWidth, -1);
        final int maxHeight = a
                .getDimensionPixelSize(R.styleable.BubbleImageView_android_maxHeight, -1);
        a.recycle();
        setMaxWidth(maxWidth);
        setMaxHeight(maxHeight);
    }
    
    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
        requestLayout();
        invalidate();
    }
    
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        requestLayout();
        invalidate();
    }
    
    public int getMaxHeight() {
        return mMaxHeight;
    }
    
    public int getMaxWidth() {
        return mMaxWidth;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int finalWidthMeasureSpec = mMaxWidth > 0
                ? MeasureSpec.makeMeasureSpec(Math.min(mMaxWidth, widthSize), widthMode)
                : widthMeasureSpec;
        int finalHeightMeasureSpec = mMaxHeight > 0 ? MeasureSpec.makeMeasureSpec(
                Math.min(mMaxHeight, heightSize), heightMode) : heightMeasureSpec;
        
        super.onMeasure(finalWidthMeasureSpec, finalHeightMeasureSpec);
        
    }
}
