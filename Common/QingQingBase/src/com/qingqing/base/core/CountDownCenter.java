package com.qingqing.base.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.qingqing.base.log.Logger;

import android.text.TextUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Wangxiaxin on 2015/8/26.
 *
 * 倒计时任务处理中心 倒计时以秒为单位
 */
public final class CountDownCenter {
    
    /**
     * 倒计时任务的监听接口
     */
    public interface CountDownListener {
        void onCountDown(String tag, int leftCount);
    }
    
    private class CountDownTask {
        Disposable disposable;
        CountDownListener listener;
        int count;
        int oriCount;
        boolean isRepeat;// 是否是循环任务
    }
    
    private static CountDownCenter sInstance;
    private ConcurrentHashMap<String, CountDownTask> mCountDownTaskMap;

    private static final String TAG = "CountDownCenter";
    
    public static CountDownCenter INSTANCE() {
        if (sInstance == null) {
            synchronized (CountDownCenter.class) {
                if (sInstance == null) {
                    sInstance = new CountDownCenter();
                }
            }
        }
        return sInstance;
    }
    
    private CountDownCenter() {
        mCountDownTaskMap = new ConcurrentHashMap<String, CountDownTask>();
    }

    public void addSystemTask(final Runnable r, long delay) {
        if (r == null)
            return;

        Observable.timer(delay,TimeUnit.SECONDS,Schedulers.computation())
                .flatMap(new Function<Long, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull Long aLong) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<Integer>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                                r.run();
                                e.onComplete();
                            }
                        });
                    }
                }).subscribe();
    }
    
    /** 判断当前tag是否处于倒计时中 */
    public boolean isDuringCountDown(String tag) {
        CountDownTask task = mCountDownTaskMap.get(tag);
        return (task != null && task.count > 0);
    }
    
    /**
     *
     * 获取指定tag的倒计时任务的剩余秒数
     */
    public synchronized int getLeftCount(String tag) {
        if (mCountDownTaskMap.containsKey(tag)) {
            return mCountDownTaskMap.get(tag).count;
        }
        return 0;
    }
    
    /**
     * 增加一个倒计时任务
     *
     */
    public void addTask(String tag, int initCount, CountDownListener listener) {
        addTask(tag, initCount, listener, false);
    }
    
    /**
     * 增加一个倒计时任务
     *
     * @param forceReset
     *            是否需要强制重置
     * @param tag
     *            任务的名称（保证全局唯一）
     * @param initCount
     *            初始倒计时数
     * @param listener
     *            倒计时监听
     *
     */
    public void addTask(String tag, int initCount, CountDownListener listener,
            boolean forceReset) {
        addTask(tag, initCount, listener, forceReset, false);
    }
    
    public void addRepeatTask(String tag, int interval, CountDownListener listener) {
        addTask(tag, interval, listener, false, true);
    }
    
    public void addTask(String tag, int initCount, CountDownListener listener,
            boolean forceReset, boolean isRepeat) {
        
        Logger.v(TAG, "addTask:  tag=" + tag + "  initSeconds=" + initCount
                + "  listener=" + listener + "  reset=" + forceReset);

        synchronized (this){
            if (mCountDownTaskMap.get(tag)!=null){
                if (forceReset){
                    if (mCountDownTaskMap.get(tag).disposable !=null) {
                        mCountDownTaskMap.get(tag).disposable.dispose();
                    }
                    mCountDownTaskMap.remove(tag);
                    createObservable(tag,initCount,listener,isRepeat);
                }
            }else{
                createObservable(tag,initCount,listener,isRepeat);
            }
        }
    }

    private void createObservable(final String tag, int initCount,CountDownListener listener,boolean isRepeat){
        Observable<Long> observable = Observable.intervalRange(0,initCount+1,0,1, TimeUnit.SECONDS,Schedulers.computation());
        if (isRepeat){
            observable = observable.repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                @Override
                public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                    return objectObservable.flatMap(new Function<Object, ObservableSource<?>>() {
                        @Override
                        public ObservableSource<?> apply(@NonNull Object o) throws Exception {
                            return Observable.timer(1,TimeUnit.SECONDS);
                        }
                    });
                }
            });
        }
        CountDownTask task = new CountDownTask();
        task.count = task.oriCount = initCount;
        task.listener = listener;
        task.isRepeat = isRepeat;
        task.disposable = observable.observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(@NonNull Long aLong) throws Exception {
                CountDownTask countDownTask = mCountDownTaskMap.get(tag);
                if (countDownTask != null) {
                    int count = countDownTask.oriCount - (int) (long) aLong;
                    countDownTask.count = count;
                    if (countDownTask.listener != null) {
                        countDownTask.listener.onCountDown(tag, count);
                    }
                }
            }
        }, Functions.ON_ERROR_MISSING, new Action() {
            @Override
            public void run() throws Exception {
                mCountDownTaskMap.remove(tag);
            }
        });
        mCountDownTaskMap.put(tag,task);
    }
    
    public void removeTaskListener(String tag) {
        synchronized (this) {
            if (!TextUtils.isEmpty(tag) && mCountDownTaskMap.containsKey(tag)
                    && mCountDownTaskMap.get(tag) != null) {
                CountDownTask task = mCountDownTaskMap.get(tag);
                task.listener = null;
            }
        }
    }
    
    /**
     * 取消一个定时任务
     * 
     * @param tag
     *            任务的名称（保证全局唯一）
     * 
     */
    public void cancelTask(String tag) {
        synchronized (this) {
            Logger.v(TAG, "cancelTask:  tag=" + tag);
            if (mCountDownTaskMap.containsKey(tag)) {
                Logger.v(TAG, "cancelTask:  tag=" + tag + " ok");
                Disposable disposable = mCountDownTaskMap.get(tag).disposable;
                if (disposable!=null){
                    disposable.dispose();
                }
                mCountDownTaskMap.remove(tag);
            }
        }
    }
    
    /**
     * 取消指定倒计时任务的监听
     *
     * @param tag
     *            任务的tag
     */
    public void cancelTaskListen(String tag) {
        if (mCountDownTaskMap.containsKey(tag)) {
            CountDownTask task = mCountDownTaskMap.get(tag);
            task.listener = null;
        }
    }
    
    /**
     * 更新一个已存在的倒计时任务的监听
     * 
     * @param tag
     *            任务的tag
     * @param listener
     *            想要设置的监听
     */
    public void setTaskListener(String tag, CountDownListener listener) {
        if (mCountDownTaskMap.containsKey(tag)) {
            CountDownTask task = mCountDownTaskMap.get(tag);
            task.listener = listener;
        }
    }
}
