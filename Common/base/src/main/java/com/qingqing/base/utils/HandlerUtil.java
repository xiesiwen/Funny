package com.qingqing.base.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by huangming on 2016/8/18.
 *
 *  请使用{@link ExecUtil}
 */
@Deprecated
public class HandlerUtil {
    
    private static Handler sUIHandler = new Handler(Looper.getMainLooper());
    
    private HandlerUtil() {}
    
    public static Handler ui() {
        return sUIHandler;
    }
    
    public static void runOnUiThread(final Runnable r) {
        if (Thread.currentThread() == sUIHandler.getLooper().getThread()) {
            r.run();
        }
        else {
            sUIHandler.post(r);
        }
    }

    public static void runOnUiThreadDealy(final Runnable r,final long delayMillis) {
        if (Thread.currentThread() == sUIHandler.getLooper().getThread()) {
            r.run();
        }
        else {
            sUIHandler.postDelayed(r,delayMillis);
        }
    }
    
    public static void runOnWorkThread(final Handler workHandler, final Runnable r) {
        if (Thread.currentThread() == workHandler.getLooper().getThread()) {
            r.run();
        }
        else {
            workHandler.post(r);
        }
    }
}
