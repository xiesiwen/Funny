package com.qingqing.project.offline.view.ncalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.qingqing.base.view.ptr.PtrCheckListener;
import com.qingqing.project.offline.calendar.CalendarViewType;

/**
 * Created by huangming on 2017/1/18.
 * <p>
 * 职责：滑动处理
 */

public class CalendarWorkspace extends FrameLayout implements CalendarNestedScrollingParent, PtrCheckListener {

    private static final String TAG = CalendarWorkspace.class.getSimpleName();

    private static final int DEFAULT_DURATION = 400;

    private static final int INVALID_POINTER = -1;
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

    private CalendarContentLayout mContentLayout;

    private boolean mIsBeingDragged = false;
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    private int mLastMotionX;
    private int mLastMotionY;
    private int mActivePointerId = INVALID_POINTER;
    private int mTouchSlop;
    private int mFlingDistance;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    private CalendarNestedScrollingParent.OnScrollChangeListener mScrollChangeListener;

    private DirectionIndicator mScrollingDirectionIndicator;

    private CalendarNestedScrollingChild mNestedScrollingChild;

    private CalendarAdapter mAdapter;

    private boolean mFirstLayout = true;

    public CalendarWorkspace(Context context) {
        this(context, null);
    }

    public CalendarWorkspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarWorkspace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScroller = new Scroller(getContext());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        float density = context.getResources().getDisplayMetrics().density;
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
        mContentLayout = new CalendarContentLayout(context, this);
        addViewInternal(mContentLayout);
    }

    public void setAdapter(CalendarAdapter adapter) {
        mAdapter = adapter;
        if (mContentLayout.getCalendarWidget() != null) {
            mContentLayout.getCalendarWidget().setAdapter(adapter);
        }
    }

    public void setScrollingDirectionIndicator(DirectionIndicator indicator) {
        this.mScrollingDirectionIndicator = indicator;
    }

    @Override
    public void scrollToPullUp() {
        scrollTo(0, getScrollRange());
    }

    @Override
    public void scrollToPullDown() {
        scrollTo(0, 0);
    }

    public void setOnScrollChangeListener(CalendarNestedScrollingParent.OnScrollChangeListener l) {
        this.mScrollChangeListener = l;
    }

    @Override
    public void setNestedScrollingChild(CalendarNestedScrollingChild scrollingChild) {
        mNestedScrollingChild = scrollingChild;
    }

    public CalendarWidget getCalendarWidget() {
        return mContentLayout.getCalendarWidget();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public void addView(View child) {
        mContentLayout.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        mContentLayout.addView(child, -1);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        mContentLayout.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        mContentLayout.addView(child, -1, params);
    }

    private void addViewInternal(View child) {
        super.addView(child, -1, generateDefaultLayoutParams());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChild(mContentLayout, widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        childWidthMeasureSpec = getChildMeasureSpec(
                parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight(),
                lp.width);
        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - verticalPadding),
                MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mFirstLayout && getScrollRange() > 0 && getSelectedViewType() != null) {
            mFirstLayout = false;
            int scrollY;
            if (getSelectedViewType() == CalendarViewType.MONTH) {
                scrollY = 0;
            } else {
                scrollY = getScrollRange();
            }
            scrollTo(0, scrollY);
        }
    }

    private CalendarViewType getSelectedViewType() {
        return mAdapter != null ? mAdapter.getSelectedViewType() : null;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t != oldt && mScrollChangeListener != null) {
            mScrollChangeListener.onScrollChanged(t, oldt);
        }
        if (mScrollingDirectionIndicator != null) {
            mScrollingDirectionIndicator.onIndicate(t != 0 ? DirectionIndicator.DOWNWARD_INDICATION : DirectionIndicator.UPWARD_INDICATION);
        }
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        scrollTo(scrollX, scrollY);
        postInvalidateOnAnimation();
    }

    @Override
    public void computeScroll() {
        boolean computeScrollOffset = mScroller.computeScrollOffset();
        if (computeScrollOffset) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, getScrollRange(), 0, 0, false);
            } else {
                postInvalidateOnAnimation();
            }
        }
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void determineScrollingStart(MotionEvent ev, final int x, final int y) {
        final int deltaX = x - mLastMotionX;
        final int deltaY = y - mLastMotionY;
        final int xDiff = Math.abs(deltaX);
        final int yDiff = Math.abs(deltaY);
        final int scrollY = getScrollY();
        if (yDiff >= mTouchSlop && yDiff > xDiff && ((deltaY < 0 && scrollY == 0) || (deltaY > 0 && scrollY != 0))) {
            mIsBeingDragged = true;
            mLastMotionX = x;
            mLastMotionY = y;
            initVelocityTrackerIfNotExists();
            mVelocityTracker.addMovement(ev);
            final ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0) {
            return false;
        }

        if (mNestedScrollingChild.isNestedScrollingEnabled()) {
            return false;
        }

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + activePointerId
                            + " in onInterceptTouchEvent");
                    break;
                }

                final int x = (int) ev.getX(pointerIndex);
                final int y = (int) ev.getY(pointerIndex);
                if (!mIsBeingDragged) {
                    determineScrollingStart(ev, x, y);
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                mLastMotionX = x;
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);

                if (!mScroller.isFinished()) {
                    mIsBeingDragged = true;
                    mScroller.abortAnimation();
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(ev);

        final int actionMasked = ev.getActionMasked();

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = (int) ev.getX();
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    break;
                }

                final int y = (int) ev.getY(activePointerIndex);
                final int x = (int) ev.getX(activePointerIndex);
                int deltaY = mLastMotionY - y;
                if (!mIsBeingDragged) {
                    determineScrollingStart(ev, x, y);
                }
                if (mIsBeingDragged) {
                    mLastMotionX = x;
                    mLastMotionY = y;
                    if (overScrollBy(0, deltaY, 0, getScrollY(), 0, getScrollRange(), 0, 0, true)) {
                        mVelocityTracker.clear();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                    boolean moveDown = determineMoveDown(initialVelocity);
                    if (moveDown) {
                        mScroller.startScroll(0, getScrollY(), 0, getScrollRange() - getScrollY(), DEFAULT_DURATION);
                    } else {
                        mScroller.startScroll(0, getScrollY(), 0, - getScrollY(), DEFAULT_DURATION);
                    }
                    postInvalidateOnAnimation();
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), DEFAULT_DURATION);
                    postInvalidateOnAnimation();
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionX = (int) ev.getX(index);
                mLastMotionY = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    private boolean determineMoveDown(int velocity){
        boolean result;
        int delta = Math.abs(getScrollY());
        if (delta > mFlingDistance  && Math.abs(velocity) > mMinimumVelocity) {
            result = velocity > 0 ? false : true;
        } else {
            result = delta > getCalendarWidget().getHeight() / 2;
        }
        return result;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = (int) ev.getX(newPointerIndex);
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;
        recycleVelocityTracker();
    }

    private int getScrollRange() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0, child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
        }
        return scrollRange;
    }

    @Override
    public boolean checkCanScrollUp() {
        return mIsBeingDragged || mNestedScrollingChild.isNestedScrollingEnabled() || (getScrollY() != 0);
    }

}
