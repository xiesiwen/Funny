package com.qingqing.base.data;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.qingqing.base.log.Logger;

/**
 * 处理数据库操作，执行结果回调
 */
public abstract class DBProcessTask<T> implements Runnable {
    protected Handler mHandler;
    
    protected Callback<T> callback;
    
    protected SQLiteDatabase db;
    
    public DBProcessTask(Callback<T> cb) {
        callback = cb;
    }
    
    /**
     * 初始化工作环境，通过{@linkplain com.qingqing.base.data.AbsDBDao
     * AbsDBDao}添加到任务队列由{@linkplain com.qingqing.base.data.AbsDBDao#execTask(DBProcessTask)
     * AbsDBDao.execTask()}执行
     * 
     * @param db
     *            不可为空
     * @param handler
     *            如果为空，表示不需要回调
     */
    public void initEnv(SQLiteDatabase db, Handler handler) {
        this.db = db;
        this.mHandler = handler;
    }
    
    /** 实现查询和对象构建 */
    protected abstract T doJob();
    
    @Override
    public void run() {
        if (db == null) {
            Logger.e("db has not been init");
            return;
        }
        
        // 执行数据库操作
        final T result = doJob();
        
        if (callback == null || mHandler == null) {
            return;
        }
        
        mHandler.post(new Runnable() {
            
            @Override
            public void run() {
                callback.done(result);
            }
        });
    }
    
    /** 数据库访问结果回调接口 */
    public interface Callback<T> {
        void done(T result);
        
        /** 该方法暂未回调 */
        void error(Exception e);
    }
    
}
