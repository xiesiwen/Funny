package com.qingqing.base.view;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.qingqingbase.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Wangxiaxin on 2015/8/19.
 * <p/>
 * 自定义 Toast 展示
 */
public final class ToastWrapper {
    
    private static Toast sToast = null;
    
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;
    
    private static Handler sMainThreadHdlr;
    
    private static class ShowRun implements Runnable {
        
        private CharSequence mText;
        private int mIconId = -1;
        private int mDuration;
        
        ShowRun(CharSequence text, int duration) {
            mText = text;
            mDuration = duration;
        }
        
        ShowRun(CharSequence text, int iconId, int duration) {
            this(text, duration);
            mIconId = iconId;
        }
        
        @Override
        public void run() {
            internalShow(BaseApplication.getCtx(), mText, mIconId, mDuration);
        }
    }
    
    private static void show(Context context, CharSequence text, int iconId,
            int duration) {
        if (context != null) {
            
            // 如果不在主线程，忽略
            if (!AppUtil.isMainThread()) {
                if (sMainThreadHdlr == null) {
                    sMainThreadHdlr = new Handler(Looper.getMainLooper());
                }
                
                sMainThreadHdlr.post(new ShowRun(text, iconId, duration));
            }
            else {
                internalShow(context, text, iconId, duration);
            }
        }
    }
    
    private static void internalShow(Context context, CharSequence text, int drawableId,
            int duration) {
        if (sToast != null)
            sToast.cancel();
        
        sToast = Toast.makeText(context, text, duration);
        LayoutInflater inflate = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = null;
        v = inflate.inflate(R.layout.toast_layout, null);
        
        if (v != null) {
            TextView tv = (TextView) v.findViewById(android.R.id.message);
            ImageView imageView = (ImageView) v.findViewById(android.R.id.icon);
            if (drawableId > 0) {
                if (imageView == null) {
                    Drawable drawable = context.getResources().getDrawable(drawableId);
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null,
                            null);
                }
                else {
                    imageView.setImageResource(drawableId);
                }
            }
            else if (imageView != null) {
                imageView.setVisibility(View.GONE);
            }
            sToast.setView(v);
        }
        sToast.setText(text);
        sToast.setGravity(Gravity.CENTER, 0, 0);
        sToast.show();
    }
    
    private static void show(Context context, int textResId, int iconResId,
            int duration) {
        if (context != null)
            show(context, context.getText(textResId), iconResId, duration);
    }
    
    public static void show(int textResID) {
        show(textResID, Toast.LENGTH_SHORT);
    }
    
    public static void showWithIcon(int textResID, int iconResID) {
        show(BaseApplication.getCtx(), textResID, iconResID, Toast.LENGTH_SHORT);
    }
    
    public static void show(int textResID, int duration) {
        show(BaseApplication.getCtx(), textResID, -1, duration);
    }
    
    public static void show(CharSequence text) {
        show(text, Toast.LENGTH_SHORT);
    }
    
    public static void showWithIcon(CharSequence text, int iconResID) {
        show(BaseApplication.getCtx(), text, iconResID, Toast.LENGTH_SHORT);
    }
    
    public static void show(CharSequence text, int duration) {
        show(BaseApplication.getCtx(), text, -1, duration);
    }
}
