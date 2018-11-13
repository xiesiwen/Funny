package com.qingqing.base.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.qingqing.base.ui.ActivityStack;

public final class UIUtil {
    
    /**
     * 展开软键盘
     */
    public static void toggleInputManager() {
        InputMethodManager inputMethodManager = (InputMethodManager) UtilsMgr.getCtx()
                .getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    public static void openInputBoard(EditText editText) {
        if (editText != null) {
            // 设置可获得焦点
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            // 请求获得焦点
            editText.requestFocus();
            // 调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) editText.getContext()
                    .getApplicationContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(editText, 0);
        }
    }
    
    /**
     * 隐藏软键盘
     */
    public static void closeInputManager(EditText editText) {
        
        if (editText == null)
            return;
        
        InputMethodManager inputMethodManager = (InputMethodManager) UtilsMgr.getCtx()
                .getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
    
    public static void closeInputManager(Activity activity) {
        
        if (activity == null)
            return;
        
        /* 隐藏软键盘 */
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        // if (inputMethodManager != null && activity.getCurrentFocus() != null
        // && inputMethodManager.isActive()) {
        // // 获取当前输入焦点的edittext
        // inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
        // .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        // }
        
        if (inputMethodManager != null && activity.getWindow()
                .getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        
    }
    
    public static void closeInputManager(Context context) {
        
        if (context == null)
            return;
        
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && inputMethodManager.isActive()) {
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    public static void closeInputManager(Fragment fragment) {
        
        if (fragment == null)
            return;
        
        closeInputManager(fragment.getActivity());
    }
    
    private static long sExitTime;
    
    /**
     * 按两次，返回桌面
     * 
     * @return false 标识不符合，需要自行提示用户 true 表示可以返回到桌面
     */
    public static boolean doubleClickQuitApp() {
        if ((System.currentTimeMillis() - sExitTime) > 2000) {
            sExitTime = System.currentTimeMillis();
            return false;
        }
        else {
            ActivityStack.popAll();
            return true;
        }
    }
    
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null)
            return;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        
        int count = listAdapter.getCount();
        if (count > 0) {
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(0, 0);
            params.height = listItem.getMeasuredHeight() * count
                    + (listView.getDividerHeight() * (count - 1));
        }
        else {
            params.height = 0;
        }
        
        listView.setLayoutParams(params);
    }
    
    /**
     * 隐藏软键盘
     */
    public static boolean hideSoftInput(Fragment fragment) {
        return fragment != null && hideSoftInput(fragment.getActivity());
        
    }
    
    /**
     * 隐藏软键盘
     */
    public static boolean hideSoftInput(Context context) {
        return (context instanceof Activity) && hideSoftInput((Activity) context);
    }
    
    /**
     * 隐藏软键盘
     */
    public static boolean hideSoftInput(Activity activity) {
        if (activity == null) {
            return false;
        }
        InputMethodManager im = (InputMethodManager) activity.getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null && activity.getCurrentFocus() != null) {
            return im.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        
        return false;
    }
    
    /**
     * 显示软键盘
     */
    public static boolean showSoftInput(Fragment fragment) {
        return fragment != null && showSoftInput(fragment.getActivity());
        
    }
    
    /**
     * 显示软键盘
     */
    public static boolean showSoftInput(Context context) {
        return (context instanceof Activity) && showSoftInput((Activity) context);
    }
    
    /**
     * 显示软键盘
     */
    public static boolean showSoftInput(Activity activity) {
        if (activity == null) {
            return false;
        }
        InputMethodManager im = (InputMethodManager) activity.getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null && activity.getCurrentFocus() != null) {
            try {
                im.showSoftInputFromInputMethod(
                        activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * 显示软键盘
     */
    public static boolean showSoftInput(View view) {
        if (view == null || view.getContext() == null) {
            return false;
        }
        InputMethodManager im = (InputMethodManager) view.getContext()
                .getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null) {
            return im.showSoftInput(view, 0);
        }
        return false;
    }

    public static boolean isViewVisable(View v){
        return v != null && v.getVisibility() == View.VISIBLE;
    }
}
