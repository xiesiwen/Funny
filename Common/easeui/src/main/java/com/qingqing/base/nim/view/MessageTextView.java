package com.qingqing.base.nim.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by huangming on 2016/3/29.
 * 处理BubbleLayout和TextView长按事件冲突的问题
 * TextView中包含对电话的link，所以onTouchEvent中一直返回true
 */
public class MessageTextView extends AppCompatTextView {

    private boolean mHasPerformedLongPress;

    public MessageTextView(Context context) {
        super(context);
    }

    public MessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLongClickable()) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                mHasPerformedLongPress = false;
            } else if (action == MotionEvent.ACTION_UP && mHasPerformedLongPress) {
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performLongClick() {
        mHasPerformedLongPress = super.performLongClick();
        return mHasPerformedLongPress;
    }
}
