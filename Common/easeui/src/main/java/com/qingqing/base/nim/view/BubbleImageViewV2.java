package com.qingqing.base.nim.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.easemob.easeui.R;
import com.qingqing.base.view.AsyncImageViewV2;

/**
 * Created by huangming on 2016/9/5.
 */
public class BubbleImageViewV2 extends AsyncImageViewV2 {
    
    public final static int DIRECTION_LEFT = 0x0;
    public final static int DIRECTION_RIGHT = 0x1;
    
    private float mRadius;
    private float mOffset;
    
    private float mArrowTop;
    private float mArrowBottom;
    private float mArrowOffset;
    private int mDirection = DIRECTION_RIGHT;
    private int mRequiredWidth;
    private int mRequiredHeight;
    
    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;
    private int mMinWidth = 0;
    private int mMinHeight = 0;
    
    public BubbleImageViewV2(Context context) {
        this(context, null);
    }
    
    public BubbleImageViewV2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public BubbleImageViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6,
                getResources().getDisplayMetrics());
        mOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6,
                getResources().getDisplayMetrics());
        mArrowTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12,
                getResources().getDisplayMetrics());
        mArrowBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18,
                getResources().getDisplayMetrics());
        mArrowOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                getResources().getDisplayMetrics());
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BubbleImageViewV2);
        
        setMaxWidth(a.getDimensionPixelSize(
                R.styleable.BubbleImageViewV2_android_maxWidth, Integer.MAX_VALUE));
        setMinimumWidth(a.getDimensionPixelSize(
                R.styleable.BubbleImageViewV2_android_minWidth, 0));
        setMaxHeight(a.getDimensionPixelSize(
                R.styleable.BubbleImageViewV2_android_maxHeight, Integer.MAX_VALUE));
        setMinimumHeight(a.getDimensionPixelSize(
                R.styleable.BubbleImageViewV2_android_minHeight, 0));
        
        mDirection = a.getInteger(R.styleable.BubbleImageViewV2_direction,
                DIRECTION_LEFT);
        a.recycle();
        
        setImageLoadedListener(new AsyncImageLoadedListener() {
            @Override
            public void onLoadComplete(String imageUri, View view, Bitmap loadedImage) {
                if (!isRequiredSizeValid() && loadedImage != null) {
                    setRequiredSize(loadedImage.getWidth(), loadedImage.getHeight());
                }
            }
        });
        
    }
    
    @Override
    public void setMaxHeight(int maxHeight) {
        super.setMaxHeight(maxHeight);
        mMaxHeight = maxHeight;
    }
    
    @Override
    public void setMinimumHeight(int minHeight) {
        super.setMinimumHeight(minHeight);
        mMinHeight = minHeight;
    }
    
    @Override
    public void setMaxWidth(int maxWidth) {
        super.setMaxWidth(maxWidth);
        mMaxWidth = maxWidth;
    }
    
    @Override
    public void setMinimumWidth(int minWidth) {
        super.setMinimumWidth(minWidth);
        mMinWidth = minWidth;
    }
    
    @Override
    public int getMaxHeight() {
        return mMaxHeight;
    }
    
    @Override
    public int getMaxWidth() {
        return mMaxWidth;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isMinimumSizeValid() && isMaximumSizeValid() && isRequiredSizeValid()) {
            if (mMinWidth <= mRequiredWidth && mRequiredWidth <= mMaxWidth
                    && mMinHeight <= mRequiredHeight && mRequiredHeight <= mMaxHeight) {
                setMeasuredDimension(mRequiredWidth, mRequiredHeight);
            }
            else {
                float aspect = ((float) mRequiredWidth) / mRequiredHeight;
                
                int newWidth = mRequiredWidth;
                int newHeight = mRequiredHeight;
                
                // 等比例显示，有最小宽高限制，大于最大限制时截取
                if (newHeight > mMaxHeight) {
                    newHeight = mMaxHeight;
                    newWidth = (int) (newHeight * aspect);
                }
                if (newWidth > mMaxWidth) {
                    newWidth = mMaxWidth;
                    newHeight = (int) (newWidth / aspect);
                }
                
                if (newHeight < mMinHeight) {
                    newHeight = mMinHeight;
                    newWidth = (int) (newHeight * aspect);
                }
                if (newWidth < mMinWidth) {
                    newWidth = mMinWidth;
                    newHeight = (int) (newWidth / aspect);
                }
                
                setMeasuredDimension(Math.min(newWidth, mMaxWidth),
                        Math.min(newHeight, mMaxHeight));
            }
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            super.onDraw(canvas);
            return;
        }
        int width = getWidth();
        int height = getHeight();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG));
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        final float offset = mOffset;
        float rectWidth = width - offset;
        float rectHeight = height;
        float rectLeft = isDirectionLeft() ? offset : 0;
        float rectTop = 0;
        Path path = new Path();
        path.moveTo(rectLeft + mRadius, rectTop);
        path.lineTo(rectLeft + rectWidth - mRadius, rectTop);
        RectF rect1 = new RectF(rectLeft + rectWidth - mRadius, rectTop,
                rectLeft + rectWidth, rectTop + mRadius);
        path.arcTo(rect1, 270, 90);
        if (!isDirectionLeft()) {
            path.lineTo(rectLeft + rectWidth, rectTop + mArrowTop);
            path.lineTo(rectLeft + rectWidth + offset,
                    rectTop + mArrowTop - mArrowOffset);
            path.lineTo(rectLeft + rectWidth, rectTop + mArrowBottom);
        }
        
        path.lineTo(rectLeft + rectWidth, rectTop + rectHeight - mRadius);
        RectF rect2 = new RectF(rectLeft + rectWidth - mRadius,
                rectTop + rectHeight - mRadius, rectLeft + rectWidth,
                rectTop + rectHeight);
        path.arcTo(rect2, 0, 90);
        path.lineTo(rectLeft + mRadius, rectTop + rectHeight);
        RectF rect3 = new RectF(rectLeft, rectTop + rectHeight - mRadius,
                rectLeft + mRadius, rectTop + rectHeight);
        path.arcTo(rect3, 90, 90);
        if (isDirectionLeft()) {
            path.lineTo(rectLeft, rectTop + mArrowBottom);
            path.lineTo(0, rectTop + mArrowTop - mArrowOffset);
            path.lineTo(rectLeft, rectTop + mArrowTop);
        }
        path.lineTo(rectLeft, rectTop + mRadius);
        RectF rect4 = new RectF(rectLeft, rectTop, rectLeft + mRadius, rectTop + mRadius);
        path.arcTo(rect4, 180, 90);
        
        Drawable d = getDrawable();
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.clipPath(path);
        d.setBounds(0, 0, width, height);
        d.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
    
    public void setRequiredSize(int requiredWidth, int requiredHeight) {
        if (requiredWidth != mRequiredWidth || requiredHeight != mRequiredHeight) {
            mRequiredWidth = requiredWidth;
            mRequiredHeight = requiredHeight;
            invalidate();
            requestLayout();
        }
    }
    
    boolean isMinimumSizeValid() {
        return getMinimumWidth() >= 0 && getMinimumHeight() >= 0;
    }
    
    boolean isMaximumSizeValid() {
        return getMaxWidth() > 0 && getMaxHeight() > 0;
    }
    
    boolean isRequiredSizeValid() {
        return mRequiredWidth > 0 && mRequiredHeight > 0;
    }
    
    public boolean isDirectionLeft() {
        return mDirection == DIRECTION_LEFT;
    }
    
}
