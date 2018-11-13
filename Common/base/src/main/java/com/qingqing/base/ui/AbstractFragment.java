package com.qingqing.base.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.UIUtil;

/**
 *
 * 支持 M-V-VM 模式开始，用于后续取代AbsFragment
 *
 *
 * 特性： 1，自带了一个属性监听回调
 */
public abstract class AbstractFragment extends Fragment implements IActivityFunc {
    
    protected FragListener mFragListener;
    protected boolean mNeedNotifyOnStart = true;
    protected boolean mNeedNotifyOnStop = true;
    private static final String TAG = "AbstractFragment";
    protected String mTag;
    protected String mReqTag;


    public interface FragListener {
        // 当前Fragment开始显示
        void onStart();
        
        // 当前Fragment开始不显示
        void onStop();
    }
    
    public AbstractFragment setFragListener(FragListener listener) {
        mFragListener = listener;
        return this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mTag = getClass().getSimpleName();
        mReqTag = mTag + System.currentTimeMillis();
        Logger.o(TAG, mTag + " onCreate");
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        Logger.o(TAG, mTag + " onStart");
        super.onStart();
        if (mFragListener != null && mNeedNotifyOnStart) {
            mFragListener.onStart();
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable  Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    
    @Override
    public void onDestroy() {
        Logger.o(TAG, mTag + " onDestroy");
        super.onDestroy();
    }
    
    @Override
    public void onDestroyView() {
        Logger.o(TAG, mTag + " onDestroyView");
        super.onDestroyView();
    }
    
    @Override
    public void onDetach() {
        Logger.o(TAG, mTag + " onDetach");
        super.onDetach();
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        Logger.o(TAG, mTag + " onHiddenChanged  " + hidden);
        super.onHiddenChanged(hidden);
    }
    
    @Override
    public void onPause() {
        Logger.o(TAG, mTag + " onPause");
        super.onPause();
    }
    
    @Override
    public void onResume() {
        Logger.o(TAG, mTag + " onResume");
        super.onResume();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Logger.o(TAG, mTag + " onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onStop() {
        Logger.o(TAG, mTag + " onStop");
        if (mFragListener != null && mNeedNotifyOnStop) {
            mFragListener.onStop();
        }
        UIUtil.closeInputManager(this);
        super.onStop();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Logger.o(TAG, mTag + " onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Logger.o(TAG, mTag + " onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
    }
    
    public boolean couldOperateUI() {
        return (getActivity() != null) && isAdded() && !isDetached();
    }
    
    /**
     * 发起销毁自己的请求，随后会调用到自己的{@link AbstractFragment#onBackPressed()}
     */
    public void finish() {
        if (couldOperateUI()) {
            getActivity().onBackPressed();
        }
    }
    
    /**
     * 针对返回键的处理，正常情况下，返回false 交给activity处理
     * <p/>
     * 遇到特殊情况，需要拦截返回键或者其他操作的时候，可以重写该方法
     */
    public boolean onBackPressed() {
        return false;
    }
    
    private Handler getActivityHandler() {
        Activity activity = getActivity();
        if (activity instanceof AbstractActivity) {
            return ((AbstractActivity) activity).getUIHandler();
        }
        
        return null;
    }
    
    public boolean sendEmptyMessage(int what) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null && handler.sendEmptyMessage(what);
    }
    
    public boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null
                && handler.sendEmptyMessageAtTime(what, uptimeMillis);
    }
    
    public boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null
                && handler.sendEmptyMessageDelayed(what, delayMillis);
    }
    
    public boolean sendMessage(Message msg) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null && handler.sendMessage(msg);
    }
    
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null
                && handler.sendMessageAtTime(msg, uptimeMillis);
    }
    
    public boolean sendMessageDelayed(Message msg, long delayMillis) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null
                && handler.sendMessageDelayed(msg, delayMillis);
    }
    
    public boolean post(Runnable r) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null && handler.post(r);
    }
    
    public boolean postAtFrontOfQueue(Runnable r) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null && handler.postAtFrontOfQueue(r);
    }
    
    public boolean postAtTime(Runnable r, long uptimeMillis) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null && handler.postAtTime(r, uptimeMillis);
    }
    
    public boolean postDelayed(Runnable r, long delayMillis) {
        Handler handler = getActivityHandler();
        return couldOperateUI() && handler != null && handler.postDelayed(r, delayMillis);
    }
    
    public void removeCallbacks(Runnable runnable) {
        Handler handler = getActivityHandler();
        if (couldOperateUI() && handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
    
    public void removeMessages(int what) {
        Handler handler = getActivityHandler();
        if (couldOperateUI() && handler != null) {
            handler.removeMessages(what);
        }
    }
    
    /**
     * 处理完成Message，需要返回true
     */
    @Override
    public boolean onHandlerUIMsg(Message msg) {
        return !couldOperateUI();
    }
}
