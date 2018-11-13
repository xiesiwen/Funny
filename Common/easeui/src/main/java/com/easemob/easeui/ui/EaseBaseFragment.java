package com.easemob.easeui.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.qingqing.base.im.ChatManager;
import com.qingqing.qingqingbase.ui.BaseFragment;

public abstract class EaseBaseFragment extends BaseFragment {
    protected InputMethodManager inputMethodManager;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        checkLogin(new ChatManager.ChatCallback() {
            @Override
            public void onSuccess() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        inputMethodManager = (InputMethodManager) getActivity()
                                .getApplicationContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        
                        initView();
                        setUpView();
                    }
                });
            }
            
            @Override
            public void onProgress(int progress, String status) {
            
            }
            
            @Override
            public void onError(int code, String error) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        });
    }
    
    private void checkLogin(ChatManager.ChatCallback chatCallback) {
        if (!ChatManager.getInstance().isLoggedIn()) {
            ChatManager.getInstance().login(true, chatCallback);
        }
        else {
            if (chatCallback != null) {
                chatCallback.onSuccess();
            }
        }
    }
    
    protected void hideSoftKeyboard() {
        if (getActivity().getWindow()
                .getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(
                        getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    /**
     * 初始化控件
     */
    protected abstract void initView();
    
    /**
     * 设置属性，监听等
     */
    protected abstract void setUpView();
    
}
