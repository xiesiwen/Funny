package com.qingqing.base.view;

/**
 * Created by tangyutian on 2016/7/7.
 * 先给数据{@link com.qingqing.base.view.PPTView#setData(java.lang.String[])}
 * 先设置显示模式，{@link com.qingqing.base.view.PPTView#FULL_SCREEN_MODE}全屏模式以及{@link com.qingqing.base.view.PPTView#LITTLE_SCREEN_MODE}小屏模式
 * 然后设置是否专家模式{@link com.qingqing.base.view.PPTView#setAdmin(boolean)}(有没有同步翻页),不是专家模式需要设置可以翻到的最大页数{@link com.qingqing.base.view.PPTView#setMaxPage(int)}
 * 设置放大、缩小、返回、同步翻页的Listener{@link com.qingqing.base.view.PPTView#setPPTViewListener(PPTView.PPTViewListener)}
 * 放大缩小只加了回调并没有写切换显示模式，需要手动调用，可以直接{@link com.qingqing.base.view.PPTView#toggleMode()}
 */

import java.util.ArrayList;
import java.util.List;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.pager.ImagePage;
import com.qingqing.base.view.pager.PPTViewPagerAdapter;
import com.qingqing.base.view.pager.Page;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PPTView extends LinearLayout implements View.OnTouchListener {
    private static final int PAGE_NUMBER_TOAST = 1;
    private static final int OTHER_TOAST = 0;
    private boolean isAdmin = false;
    private long downTime;
    private float downX;
    private float downY;
    private boolean syncPageMode = true;
    private int viewPagerScrollState =0;
    private float scrollTouchX;

    // 因为ViewPager一定会消费时间所以只能这么干了
    private boolean hasTouched = false;

    private boolean isShowingController = false;
    private ViewPager mViewPager;
    private List<Page> mPageList;
    private TextView mTvPageNumberToast;
    private TextView mPageNumberText;
    private View mExpandView;
    private View mPptContainer;
    private View mControllerLayout;
    private View mBackView;
    private View mEnlargeView;
    private View mCollapseView;
    private ImageView mIvSyncPage;
    private PPTViewPagerAdapter mAdapter;
    private PPTViewListener mListener;

    private int mSoftKeyboardHeight;
    private int mScreenHeight;
    private int mRootPortraitMaxHeight;
    private boolean mSoftKeyboardOpened = false;

    private final static float ASPECT_RATIO = ((float) 16) / 9;

    private float mAspectRatio = ASPECT_RATIO;
    private int mScreenOrientation;

    private Runnable dismissPageToast = new Runnable() {
        @Override
        public void run() {
            mTvPageNumberToast.setVisibility(View.INVISIBLE);
            if (isShowingController){
                mPageNumberText.setVisibility(VISIBLE);
            }
        }
    };
    private Runnable autoDismissController = new Runnable() {
        @Override
        public void run() {
            if (isShowingController){
                dismissController();
            }
        }
    };

    public PPTView(Context context) {
        this(context, null);
    }

    public PPTView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_ppt, this);
        initViews(this);

        Configuration configuration = getResources().getConfiguration();
        setScreenOrientation(configuration != null ? configuration.orientation : Configuration.ORIENTATION_PORTRAIT);
    }

    private void initViews(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.ppt_viewpager);

        mExpandView = view.findViewById(R.id.view_expand_ppt);
        mExpandView.setOnTouchListener(this);
        mPptContainer = view.findViewById(R.id.container_ppt);
        mTvPageNumberToast = (TextView) view.findViewById(R.id.tv_page_number_toast);
        mPageNumberText = (TextView) view.findViewById(R.id.tv_page_number_text);
        mControllerLayout = view.findViewById(R.id.rl_controller);
        mBackView = view.findViewById(R.id.back);
        mBackView.setOnTouchListener(this);
        mEnlargeView = view.findViewById(R.id.iv_enlarge);
        mEnlargeView.setOnTouchListener(this);
        mCollapseView = view.findViewById(R.id.iv_collapse);
        mCollapseView.setOnTouchListener(this);
        mIvSyncPage = (ImageView) view.findViewById(R.id.iv_sync_page);
        mIvSyncPage.setOnTouchListener(this);
        
        view.findViewById(R.id.back).setOnTouchListener(this);

        // testPage();
        ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
//                Logger.d("scroll"+positionOffset);
                if (positionOffsetPixels<=0&&viewPagerScrollState==ViewPager.SCROLL_STATE_DRAGGING&&position==mAdapter.getCount()-1&&scrollTouchX<=downX) {
                    if (mPageList != null) {
                        if (!isAdmin && getCurrentPage() < mPageList.size()-1) {
//                            showToast(PPTView.this.getContext().getString(R.string.lecture_not_allowed_next_page),OTHER_TOAST);
                            //TODO 可能有用
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                syncPageNumber();
                if (isAdmin && syncPageMode && mListener != null) {
                    mListener.onSyncPage(position);
                }
                else {
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                        UserBehaviorLogManager.INSTANCE().saveClickLog(
                                StatisticalDataConstants.LOG_PAGE_LECTURE_ROOM,
                                StatisticalDataConstants.CLICK_LECTURE_PAGE_TURNING);
                    }
                    else if (BaseData
                            .getClientType() == AppCommon.AppType.qingqing_teacher) {
                        UserBehaviorLogManager.INSTANCE().saveClickLog(
                                StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                                StatisticalDataConstants.CLICK_LECTURE_PAGE_TURNING);
                    }
                }
            }
            
            @Override
            public void onPageScrollStateChanged(int state) {
                viewPagerScrollState = state;
            }
        };
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        showController();
        postDelayed(autoDismissController,3000L);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setScreenOrientation(newConfig != null ? newConfig.orientation : Configuration.ORIENTATION_PORTRAIT);
    }

    /**
     * 设置视频的宽高比:默认16:9
     */
    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio > 0f && mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
        }
    }

    protected void setScreenOrientation(int screenOrientation) {
        mScreenOrientation = screenOrientation;
        onScreenOrientationChanged();
        requestLayout();
    }

    /**
     * 横竖屏切换
     */
    public void toggleScreenOrientation() {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            boolean isPrePortrait = getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT;
            activity.setRequestedOrientation(isPrePortrait ?
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private int getScreenOrientation() {
        return mScreenOrientation;
    }

    protected void onScreenOrientationChanged() {

        syncPageNumber();

        boolean isPortrait = isPortrait();
        if (!isPortrait) {
            mPptContainer.setVisibility(VISIBLE);
            mExpandView.setVisibility(GONE);
        }

        mBackView.setVisibility(isPortrait ? GONE : VISIBLE);
        mEnlargeView.setVisibility(isPortrait ? VISIBLE : GONE);
        mCollapseView.setVisibility(isPortrait ? VISIBLE : GONE);

        if (getContext() instanceof BaseActionBarActivity) {
            BaseActionBarActivity actionBarActivity = (BaseActionBarActivity) getContext();
            if (isPortrait) {
                actionBarActivity.showActionBar();
            } else {
                actionBarActivity.hideActionBar();
            }
        }

        if (isPortrait) {
            displayPortraitScreen();
        } else {
            UIUtil.hideSoftInput(getContext());
            displayLandscapeScreen();
        }

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mSoftKeyboardHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, isPortrait ? 150 : 100, dm);
        mScreenHeight = dm.heightPixels;
    }

    /**
     * 横屏时,显示全屏
     */
    private void displayLandscapeScreen() {
        final View decorView = getDecorView();

        if (decorView != null && Build.VERSION.SDK_INT >= 11) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void displayPortraitScreen() {
        final View decorView = getDecorView();
        if (decorView != null && Build.VERSION.SDK_INT >= 11) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private View getDecorView() {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            return activity.getWindow() != null ? activity.getWindow().getDecorView() : null;
        }
        return null;
    }

    private void expandPPTView() {
        mExpandView.setVisibility(GONE);
        mPptContainer.setVisibility(VISIBLE);
        if (mListener != null) {
            mListener.onExpandChanged(true);
        }
    }

    private void collapsePPTView() {
        mExpandView.setVisibility(VISIBLE);
        mPptContainer.setVisibility(GONE);
        if (mListener != null) {
            mListener.onExpandChanged(false);
        }
    }

    boolean isExpanded() {
        return mPptContainer.getVisibility() == VISIBLE;
    }


    public boolean isPortrait() {
        return getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT;
    }

    public synchronized void showController() {
        mControllerLayout.setVisibility(View.VISIBLE);
        isShowingController = true;
        mPageNumberText.setVisibility(mTvPageNumberToast.getVisibility()==VISIBLE?GONE:VISIBLE);

    }

    public synchronized void dismissController() {
        mControllerLayout.setVisibility(GONE);
        isShowingController = false;
        mPageNumberText.setVisibility(GONE);
    }

    public synchronized void toggleShowingController() {
        if (isShowingController) {
            dismissController();
        } else {
            showController();
        }
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
            UserBehaviorLogManager.INSTANCE().saveClickLog(
                    StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                    StatisticalDataConstants.CLICK_LECTURE_PPT_CLICK);
        }
    }
    
    public synchronized void showSyncPageIfNeeded() {
        mIvSyncPage.setVisibility(isAdmin ? VISIBLE : GONE);
        if (syncPageMode) {
            mIvSyncPage.setImageResource(R.drawable.icon_fanye_select);
        } else {
            mIvSyncPage.setImageResource(R.drawable.icon_fanye);
        }
    }

    public synchronized void setSyncPageMode(boolean mode) {
        if (mode) {
            mIvSyncPage.setImageResource(R.drawable.icon_fanye_select);
            showToast(getResources().getString(R.string.sync_page_mode_on),OTHER_TOAST);
        } else {
            mIvSyncPage.setImageResource(R.drawable.icon_fanye);
            showToast(getResources().getString(R.string.sync_page_mode_off),OTHER_TOAST);
        }
        syncPageMode = mode;

        if (mListener != null) {
            mListener.onSyncPageModeChanged(syncPageMode);
        }
    }

    /**
     * 同步翻页
     */
    public synchronized void toggleSyncPageMode() {
        setSyncPageMode(!syncPageMode);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hasTouched = true;
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                hasTouched = false;
                onClick(v);
                return true;
        }
        return false;
    }

    private void onClick(View v) {

        int id = v.getId();
        if(id == R.id.view_expand_ppt) {
            if(!mSoftKeyboardOpened) {
                expandPPTView();
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                    UserBehaviorLogManager.INSTANCE().saveClickLog(
                            StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                            StatisticalDataConstants.CLICK_LECTURE_PPT_UNFOLD);
                }
            }
        }
        else if (id == R.id.iv_enlarge || id == R.id.back) {
            toggleScreenOrientation();
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                UserBehaviorLogManager.INSTANCE().saveClickLog(
                        StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                        (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT)
                                ? StatisticalDataConstants.CLICK_LECTURE_PPT_MAGNIFY
                                : StatisticalDataConstants.CLICK_LECTURE_PPT_RETURN);
            }
        }
        else if (id == R.id.iv_collapse) {
            collapsePPTView();
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                UserBehaviorLogManager.INSTANCE().saveClickLog(
                        StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                        StatisticalDataConstants.CLICK_LECTURE_PPT_STOP);
            }
        }
        else if (id == R.id.iv_sync_page) {
            toggleSyncPageMode();
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Logger.d(ev.toString());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTime = SystemClock.currentThreadTimeMillis();
                downX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (SystemClock.currentThreadTimeMillis() - downTime < 500
                        && Math.abs(ev.getX() - downX) < 10
                        && Math.abs(ev.getY() - downY) < 10 && !hasTouched && !mSoftKeyboardOpened) {
                    toggleShowingController();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                scrollTouchX = ev.getX();
                break;
        }
        if (getHandler()!=null){
            getHandler().removeCallbacks(autoDismissController);
            getHandler().postDelayed(autoDismissController,3000L);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setMaxPage(int page) {
        if(mAdapter != null) {
            mAdapter.setMaxPage(page);
            syncPageNumber();
        }
    }

    public int getMaxPage() {
        return mAdapter != null ? mAdapter.getMaxPage() : 0;
    }

    public int getPPTDisplayedSize() {
        return mAdapter != null ? mAdapter.getCount() : 0;
    }

    private void syncPageNumber() {
        if (!isShowingController) {
            showToast(getContext().getString(R.string.page_number,
                    (getCurrentPage() + 1)), PAGE_NUMBER_TOAST);
        }
        mPageNumberText.setText((getCurrentPage() + 1)
                + "/"
                + (mViewPager.getAdapter() == null ? 0 : ((PPTViewPagerAdapter)(mViewPager.getAdapter()))
                .getCount()));
    }

    private void showToast(String text,int type) {
        if (getHandler() != null) {
            switch (type){
                case PAGE_NUMBER_TOAST:
                    if (isShowingController){
                        mPageNumberText.setVisibility(GONE);
                    }
                    break;
            }
            mTvPageNumberToast.setText(text);
            mTvPageNumberToast.setVisibility(View.VISIBLE);
            getHandler().removeCallbacks(dismissPageToast);
            getHandler().postDelayed(dismissPageToast, 1500);
        }
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
        if(mAdapter != null){
            mAdapter.setCanViewAll(admin);
            showSyncPageIfNeeded();
            syncPageNumber();
        }
    }

    public void setCurrentPage(int pageIndex) {
        mViewPager.setCurrentItem(pageIndex);
    }

    public int getCurrentPage() {
        return mViewPager.getCurrentItem();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final boolean isPortrait = isPortrait();

        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = widthSpecSize;
        int height = heightSpecSize;
        int widthMode = widthSpecMode;
        int heightMode = heightSpecMode;

        LayoutParams lp = (LayoutParams) mPptContainer.getLayoutParams();
        lp.width = widthSpecSize;
        if (!isPortrait) {
            height = heightSpecSize;
            heightMode = MeasureSpec.EXACTLY;
            lp.height = heightSpecSize;
        } else {
            lp.height = (int) (lp.width / mAspectRatio);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), MeasureSpec.makeMeasureSpec(height, heightMode));

        postPptFollowSoftInputVisibility(heightSpecSize);
    }

    private void postPptFollowSoftInputVisibility(final int rootHeightSize) {
        if(isPortrait()) {
            post(new Runnable() {
                @Override
                public void run() {
                    mRootPortraitMaxHeight = Math.max(rootHeightSize, mRootPortraitMaxHeight);
                    if(mRootPortraitMaxHeight > 0 && mRootPortraitMaxHeight <= mScreenHeight && rootHeightSize > 0 && rootHeightSize <= mRootPortraitMaxHeight) {
                        //软键盘弹出
                        if(rootHeightSize + mSoftKeyboardHeight < mRootPortraitMaxHeight) {
                            mSoftKeyboardOpened = true;
                            if(isExpanded()) {
                                collapsePPTView();
                            }
                        } else {
                            if(mSoftKeyboardOpened && !isExpanded()) {
                                expandPPTView();
                            }
                            mSoftKeyboardOpened = false;
                        }
                    }
                }
            });
        } else {
            mSoftKeyboardOpened = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPageList != null) {
            mPageList.clear();
        }
    }

    public void setData(String[] urls) {
        List<Page> pages = new ArrayList<>(urls.length);
        for (int i = 0; i < urls.length; i++) {
            pages.add(new ImagePage(urls[i], R.drawable.bg_item_lecture_list_default));
        }
        mAdapter = new PPTViewPagerAdapter(pages);
        mViewPager.setAdapter(mAdapter);
        mPageList = pages;
        syncPageNumber();
    }

    public void setImageUrlBy(int index, String imageUrl) {
        if (mPageList != null && index >= 0 && index < mPageList.size()) {
            ImagePage page = (ImagePage) mPageList.get(index);
            page.setImgUrl(imageUrl);
            if (mViewPager.getContext() != null) {
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    public void setPPTViewListener(PPTViewListener listener) {
        mListener = listener;
    }

    public interface PPTViewListener {

        void onExpandChanged(boolean expanded);

        void onSyncPage(int pageIndex);

        void onSyncPageModeChanged(boolean mode);

    }

}
