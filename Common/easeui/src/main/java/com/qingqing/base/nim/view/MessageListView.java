package com.qingqing.base.nim.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;

import com.qingqing.base.log.Logger;
import com.qingqing.base.view.NoAnimPtrListView;

/**
 * Created by huangming on 2016/9/9.
 */
public class MessageListView extends NoAnimPtrListView {
    
    private static final String TAG = "MessageListView";
    private static final int MSG_SELECT_LAST_ITEM = 2841;
    private static final int MSG_FORCE_SELECT_LAST_ITEM = 2842;
    
    private OnScrollListener mExternalScrollListener;
    
    private boolean mForceLastItemVisible;
    private boolean mPendingLastItemVisible;
    private Handler mUIHandler;
    
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private int mOldScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    
    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    private int mTotalItemCount;
    
    private boolean mTouched;
    
    public MessageListView(Context context) {
        super(context);
        init();
    }
    
    public MessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public MessageListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setInternalOnScrollListener();
        mUIHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int what = msg.what;
                switch (what) {
                    case MSG_SELECT_LAST_ITEM:
                        if (getScrollState() == OnScrollListener.SCROLL_STATE_FLING
                                && getOldScrollState() == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            Logger.i(TAG, "handleMessage : scroll from touch to fling");
                            return;
                        }
                        
                        if (getScrollState() == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            Logger.i(TAG, "handleMessage : scroll touch");
                            return;
                        }
                        
                        if (isLastItemVisible()) {
                            scrollToLastItem();
                        }
                        break;
                    case MSG_FORCE_SELECT_LAST_ITEM:
                        scrollToLastItem();
                        break;
                    default:
                        break;
                }
            }
        };
    }
    
    private void setScrollState(int scrollState) {
        this.mScrollState = scrollState;
    }
    
    private int getScrollState() {
        return mScrollState;
    }
    
    private void setOldScrollState(int mOldScrollState) {
        this.mOldScrollState = mOldScrollState;
    }
    
    private int getOldScrollState() {
        return mOldScrollState;
    }
    
    private void setFirstVisibleItem(int firstVisibleItem) {
        this.mFirstVisibleItem = firstVisibleItem;
    }
    
    private int getFirstVisibleItem() {
        return mFirstVisibleItem;
    }
    
    private void setVisibleItemCount(int visibleItemCount) {
        this.mVisibleItemCount = visibleItemCount;
    }
    
    private void setPendingLastItemVisible(boolean pendingLastItemVisible) {
        this.mPendingLastItemVisible = pendingLastItemVisible;
    }
    
    private boolean isPendingLastItemVisible() {
        return mPendingLastItemVisible;
    }
    
    private int getVisibleItemCount() {
        return mVisibleItemCount;
    }
    
    private int getTotalItemCount() {
        return mTotalItemCount;
    }
    
    private void setTotalItemCount(int totalItemCount) {
        this.mTotalItemCount = totalItemCount;
    }
    
    private Handler getUIHandler() {
        return mUIHandler;
    }
    
    public boolean isTouched() {
        return mTouched;
    }
    
    public void setTouched(boolean touched) {
        this.mTouched = touched;
    }
    
    private OnScrollListener mInternalScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            setOldScrollState(getScrollState());
            setScrollState(scrollState);
            if (getExternalScrollListener() != null) {
                getExternalScrollListener().onScrollStateChanged(view, scrollState);
            }
        }
        
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            setFirstVisibleItem(firstVisibleItem);
            setVisibleItemCount(visibleItemCount);
            setTotalItemCount(totalItemCount);
            
            if (getExternalScrollListener() != null) {
                getExternalScrollListener().onScroll(view, firstVisibleItem,
                        visibleItemCount, totalItemCount);
            }
            
            if (isTouched()) {
                Logger.i(TAG, "onScroll : touch");
                return;
            }
            
            if (isLastItemVisible()) {
                setForceLastItemVisible(false);
                setPendingLastItemVisible(true);
            }
            else if ((isForceLastItemVisible() || isPendingLastItemVisible())
                    && totalItemCount > 0
                    && getScrollState() != OnScrollListener.SCROLL_STATE_FLING) {
                postForceScrollToLastItem();
            }
        }
    };
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result = super.dispatchTouchEvent(ev);
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTouched(true);
                setPendingLastItemVisible(false);
                break;
            case MotionEvent.ACTION_MOVE:
                setTouched(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setTouched(false);
                break;
            default:
                break;
        }
        return result;
    }
    
    private boolean isLastItemVisible() {
        return getTotalItemCount() > 0
                && getFirstVisibleItem() + getVisibleItemCount() >= getTotalItemCount();
    }
    
    public boolean isForceLastItemVisible() {
        return mForceLastItemVisible;
    }
    
    public void setForceLastItemVisible(boolean forceLastItemVisible) {
        this.mForceLastItemVisible = forceLastItemVisible;
    }
    
    private OnScrollListener getExternalScrollListener() {
        return mExternalScrollListener;
    }
    
    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mExternalScrollListener = l;
        setInternalOnScrollListener();
    }
    
    private void setInternalOnScrollListener() {
        super.setOnScrollListener(mInternalScrollListener);
    }
    
    public void selectListLast() {
        postScrollToLastItem();
    }
    
    public void forceSelectListLast() {
        setForceLastItemVisible(true);
        postForceScrollToLastItem();
    }
    
    private void postScrollToLastItem() {
        if (!getUIHandler().hasMessages(MSG_SELECT_LAST_ITEM)) {
            getUIHandler().sendEmptyMessage(MSG_SELECT_LAST_ITEM);
        }
    }
    
    private void postForceScrollToLastItem() {
        getUIHandler().removeMessages(MSG_SELECT_LAST_ITEM);
        if (!getUIHandler().hasMessages(MSG_FORCE_SELECT_LAST_ITEM)) {
            getUIHandler().sendEmptyMessage(MSG_FORCE_SELECT_LAST_ITEM);
        }
    }
    
    private void scrollToLastItem() {
        smoothScrollBy(Integer.MAX_VALUE, 0);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getUIHandler().removeMessages(MSG_SELECT_LAST_ITEM);
        getUIHandler().removeMessages(MSG_FORCE_SELECT_LAST_ITEM);
    }
}
