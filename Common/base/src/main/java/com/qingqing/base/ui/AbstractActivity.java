package com.qingqing.base.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.qingqing.base.R;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.UIUtil;

import java.lang.ref.WeakReference;

/**
 * @author huangming date 2015-6-26
 */
public abstract class AbstractActivity extends AppCompatActivity implements IActivityFunc {
    protected FragmentAssist mFragAssist;
    private boolean mCouldOperateUI;
    protected UIHandler mUIHandler;
    private static final String TAG = "AbstractActivity";
    protected String mTag;
    protected String mReqTag;
    private View mStatusBarView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReqTag = mTag + System.currentTimeMillis();
        ActivityStack.pushActivity(this);
        mFragAssist = new FragmentAssist(this);
        mCouldOperateUI = true;
        mUIHandler = new UIHandler(this);
        Logger.o(TAG, mTag + ".onCreate");
    }

    public void setFragGroupID(int id) {
        mFragAssist.setGroupID(id);
    }
    

    @Override
    protected void onDestroy() {
        Logger.o(TAG, mTag + ".onDestroy");
        super.onDestroy();
        mUIHandler.removeCallbacksAndMessages(null);// delete all msgs and
        // callbacks
        ActivityStack.pop(this);
        mCouldOperateUI = false;
    }
    
    @Override
    protected void onPause() {
        Logger.o(TAG, mTag + ".onPause");
        super.onPause();
    }
    
    @Override
    protected void onStart() {
        Logger.o(TAG, mTag + ".onStart");
        super.onStart();
    }
    
    @Override
    protected void onResume() {
        Logger.o(TAG, mTag + ".onResume");
        super.onResume();
    }
    
    @Override
    public void onAttachedToWindow() {
        Logger.o(TAG, mTag + ".onAttachedToWindow");
        super.onAttachedToWindow();
    }
    
    @Override
    public void onDetachedFromWindow() {
        Logger.o(TAG, mTag + ".onDetachedFromWindow");
        super.onDetachedFromWindow();
    }
    
    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        mTag = getClass().getSimpleName();
        Logger.o(TAG, mTag + ".onApplyThemeResource");
        super.onApplyThemeResource(theme, resid, first);
    }
    
    @Override
    protected void onStop() {
        Logger.o(TAG, mTag + ".onStop");
        UIUtil.closeInputManager(this);
        super.onStop();
        // if (!AppUtil.isAppForeground()) {
        // // app 进入后台 全局变量 记录当前已经进入后台
        // AccountManager.INSTANCE().isActive = false;
        // UserBehaviorLogManager.INSTANCE()
        // .saveEventLog(StatisticalDataConstants.LOG_CLOSE_APP);
        // }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.o(TAG, mTag + ".onSaveInstanceState");
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Logger.o(TAG, mTag + ".onRestoreInstanceState");
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.o(TAG, mTag + ".onRestart");
    }
    
    protected boolean onPropChanged(int i) {
        return false;
    }
    
    public boolean couldOperateUI() {
        return mCouldOperateUI;
    }
    
    @Override
    public void onBackPressed() {
        
        boolean ret = false;
        AbstractFragment frag = mFragAssist.getTopFragment();
        if (frag != null) {
            ret = frag.onBackPressed();
        }
        
        if (!ret) {
            final int backEntryCount = mFragAssist.getBackStackCount();
            if (backEntryCount > 0) {
                mFragAssist.pop();
            }
            else {
                onPreActivityBackPressed();
                super.onBackPressed();
            }
        }
    }
    
    protected void onPreActivityBackPressed() {
        
    }
    
    public boolean sendEmptyMessage(int what) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.sendEmptyMessage(what);
    }
    
    public boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.sendEmptyMessageAtTime(what, uptimeMillis);
    }
    
    public boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.sendEmptyMessageDelayed(what, delayMillis);
    }
    
    public boolean sendMessage(Message msg) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.sendMessage(msg);
    }
    
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.sendMessageAtTime(msg, uptimeMillis);
    }
    
    public boolean sendMessageDelayed(Message msg, long delayMillis) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.sendMessageDelayed(msg, delayMillis);
    }
    
    public boolean post(Runnable r) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.post(r);
    }
    
    public boolean postAtFrontOfQueue(Runnable r) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.postAtFrontOfQueue(r);
    }
    
    public boolean postAtTime(Runnable r, long uptimeMillis) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.postAtTime(r, uptimeMillis);
    }
    
    public boolean postDelayed(Runnable r, long delayMillis) {
        Handler hdlr = getUIHandler();
        return couldOperateUI() && hdlr.postDelayed(r, delayMillis);
    }
    
    public void removeCallbacks(Runnable runnable) {
        Handler handler = getUIHandler();
        if (handler != null) {
            if (couldOperateUI()) {
                handler.removeCallbacks(runnable);
            }
        }
    }
    
    public void removeMessages(int what) {
        Handler hdlr = getUIHandler();
        if (couldOperateUI()) {
            hdlr.removeMessages(what);
        }
    }
    
    public UIHandler getUIHandler() {
        return mUIHandler;
    }
    
    public boolean isUIHandlerValid() {
        return mUIHandler.isValid();
    }
    
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            onSetStatusBarMode(); // 默认使用着色模式，即4.4-5.0添加View，5.0以上设置状态栏颜色的方式
        }
    }
    
    /**
     * 在setContentView之后会被调用，子类如果需要进入全屏模式，可以复写这个方法，调用全屏的实现。
     */
    public void onSetStatusBarMode() {
        setStatusBarColor(R.color.transparent_dark, true);
    }
    
    public void setFullScreen() {
        // 4.4 以下不支持
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return;
        moveContentViewUpwards();
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (mStatusBarView != null) {
                mStatusBarView.setBackgroundColor(
                        ContextCompat.getColor(this, android.R.color.transparent));
            }
        }
    }
    
    /**
     * 对外提供的设置statusBar颜色的方法
     * 
     * @param statusBarColor
     * @param moveContentViewDownwards
     *            是否选择fitSystemWindows,即是否不让ContentView 占据状态栏空间
     */
    public void setStatusBarColor(@ColorRes int statusBarColor,
            boolean moveContentViewDownwards) {
        
        // 4.4 以下不支持
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return;
        ViewGroup contentView = (ViewGroup) ((ViewGroup) findViewById(
                android.R.id.content)).getChildAt(0);
        if (moveContentViewDownwards) {
            if (contentView != null && !contentView.getFitsSystemWindows()) {
                contentView.setFitsSystemWindows(true);
            }
        }
        @ColorInt
        int colorValue = ContextCompat.getColor(this, statusBarColor);
        final boolean needDarkText = isStatusBarBgColorNeedDarkText(colorValue);
        final boolean supportSetText = autoSetStatusBarTextColor(needDarkText);
        if (needDarkText && !supportSetText) {
            // VIVO手机上，字体颜色自动变化
            boolean couldIgnore = false;
            // if (DeviceUtil.isVivo()) {
            // couldIgnore = true;
            // }
            if (!couldIgnore) {
                colorValue = ContextCompat.getColor(this, R.color.transparent_dark);
            }
        }
        if (contentView != null
                && contentView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 5.0以上使用系统提供的方法
                window.addFlags(
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.setStatusBarColor(colorValue);
                if ((window.getAttributes().softInputMode
                        & WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) > 0) {
                    AndroidBug5497Workaround.assistActivity(this);
                }
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                ViewGroup decorView = (ViewGroup) window.getDecorView();
                if (mStatusBarView == null) {
                    mStatusBarView = new View(this);
                    mStatusBarView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            AppUtil.getStatusBarHeight()));
                    mStatusBarView.setBackgroundColor(colorValue);
                    decorView.addView(mStatusBarView);
                }
                else {
                    mStatusBarView.setBackgroundColor(colorValue);
                }
            }
        }
        AppUtil.setStatusBarTextDarkOnMarshMallow(this, needDarkText);
    }
    
    /**
     * 请不要在xml中根布局中添加fitSystemWindows = true ，否则该方法无效
     */
    protected void moveContentViewDownWards() {
        ViewGroup contentview = (ViewGroup) ((ViewGroup) findViewById(
                android.R.id.content)).getChildAt(0);
        if (contentview != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentview
                    .getLayoutParams();
            params.setMargins(params.leftMargin, AppUtil.getStatusBarHeight(),
                    params.rightMargin, params.bottomMargin);
            contentview.setLayoutParams(params);
        }
    }
    
    /**
     * 请不要在xml中根布局中添加fitSystemWindows = true ，否则该方法无效
     */
    protected void moveContentViewUpwards() {
        ViewGroup contentView = (ViewGroup) ((ViewGroup) findViewById(
                android.R.id.content)).getChildAt(0);
        if (contentView != null) {
            if (contentView.getTop() == AppUtil.getStatusBarHeight()) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView
                        .getLayoutParams();
                if (params.topMargin == AppUtil.getStatusBarHeight()) {
                    params.setMargins(params.leftMargin, 0, params.rightMargin,
                            params.bottomMargin);
                    contentView.setLayoutParams(params);
                }
            }
        }
    }
    
    /**
     * 设置状态栏字体颜色，Dark或light, 只在部分手机上有效 这里面需要注意 ， MiUi安卓6.0手机 Android
     * 6.0的方法并不能改变状态栏颜色，需要使用MiUi的方法
     */
    public boolean setStatusBarTextColor(boolean isDark) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && (AppUtil.setMiuiStatusBarDarkMode(this, isDark)
                        || AppUtil.setMeizuStatusBarDarkIcon(this, isDark)
                        || AppUtil.setStatusBarTextDarkOnMarshMallow(this, isDark));
    }
    
    private boolean isStatusBarBgColorNeedDarkText(int bgColor) {
        int red = Color.red(bgColor), blue = Color.blue(bgColor),
                green = Color.green(bgColor);
        // 三个都大于0xcc 或者 一个为0两个大于0xcc
        int zeroCount = 0, ccCount = 0;
        if (red >= 0xcc)
            ++ccCount;
        if (blue >= 0xcc)
            ++ccCount;
        if (green >= 0xcc)
            ++ccCount;
        
        if (red == 0)
            ++zeroCount;
        if (blue == 0)
            ++zeroCount;
        if (green == 0)
            ++zeroCount;
        
        return ccCount == 3 || (ccCount == 2 && zeroCount == 1);
    }
    
    /**
     * 自动设置状态栏的字体颜色模式
     *
     * @return 是否设置成功
     * @param needDark
     *            是否需要设置状态栏字体颜色为黑色
     */
    private boolean autoSetStatusBarTextColor(boolean needDark) {
        return setStatusBarTextColor(needDark);
    }
    
    /**
     * 子类可以重写此方法，达到使用handler的效果
     */
    @Override
    public boolean onHandlerUIMsg(Message msg) {
        return true;
    }
    
    private static class UIHandler extends Handler {
        
        private WeakReference<Context> mCtxRef;
        
        public UIHandler(Context ctx) {
            mCtxRef = new WeakReference<>(ctx);
        }
        
        boolean isValid() {
            return mCtxRef.get() != null;
        }
        
        @Override
        public void handleMessage(Message msg) {
            
            if (mCtxRef.get() == null) {
                return;
            }
            
            AbstractActivity activity = (AbstractActivity) mCtxRef.get();
            if (activity != null) {
                boolean ret = false;
                if (activity.mFragAssist != null) {
                    AbstractFragment frag = activity.mFragAssist.getTopFragment();
                    if (frag != null && frag.couldOperateUI()) {
                        ret = frag.onHandlerUIMsg(msg);
                    }
                }
                
                if (!ret) {
                    activity.onHandlerUIMsg(msg);
                }
            }
        }
    }
    
    private static class AndroidBug5497Workaround {
        
        // For more information, see
        // https://code.google.com/p/android/issues/detail?id=5497
        // To use this class, simply invoke assistActivity() on an Activity that
        // already has its content view set.
        
        static void assistActivity(Activity activity) {
            new AndroidBug5497Workaround(activity);
        }
        
        private View mChildOfContent;
        private int usableHeightPrevious;
        private FrameLayout.LayoutParams frameLayoutParams;
        
        private AndroidBug5497Workaround(Activity activity) {
            FrameLayout content = (FrameLayout) activity
                    .findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            possiblyResizeChildOfContent();
                        }
                    });
            frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent
                    .getLayoutParams();
        }
        
        private void possiblyResizeChildOfContent() {
            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != usableHeightPrevious) {
                int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
                int heightDifference = usableHeightSansKeyboard - usableHeightNow;
                if (heightDifference > (usableHeightSansKeyboard / 4)) {
                    // keyboard probably just became visible
                    frameLayoutParams.height = usableHeightSansKeyboard
                            - heightDifference;
                }
                else {
                    // keyboard probably just became hidden
                    frameLayoutParams.height = usableHeightSansKeyboard;
                }
                mChildOfContent.requestLayout();
                usableHeightPrevious = usableHeightNow;
            }
        }
        
        private int computeUsableHeight() {
            Rect r = new Rect();
            mChildOfContent.getWindowVisibleDisplayFrame(r);
            return (r.bottom - r.top);// 全屏模式下： return r.bottom
        }
        
    }
}
