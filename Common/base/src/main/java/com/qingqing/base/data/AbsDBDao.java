package com.qingqing.base.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * 数据库同步操作基类
 * 
 * @author tanwei
 *
 */
public abstract class AbsDBDao {
    
    protected static final String TAG = "DBDao";
    protected SQLiteDatabase mDB;
    private Handler mMainHandler;
    private Handler mHandler;

    /**
     * 初始化线程和handler
     *
     * @param context context
     * @param name threadName
     */
    public AbsDBDao(Context context, String name) {
        
        HandlerThread taskThread = new HandlerThread(name);
        taskThread.start();
        
        mHandler = new Handler(taskThread.getLooper());// start()之后获取looper,如果looper没创建好会被阻塞
        mMainHandler = new Handler(context.getMainLooper());
        initDB(context);
    }
    
    public static boolean isTableExists(final SQLiteDatabase database, String tablename) {
        Cursor c = null;
        try {
            c = database
                    .rawQuery(
                            String.format(
                                    "SELECT count(*) tables FROM sqlite_master WHERE type='table' AND name='%s'",
                                    tablename), null);
            
            if (c.moveToNext()) {
                if (c.getInt(c.getColumnIndex("tables")) > 0) {
                    // c.close();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
            if (c != null) {
                c.close();
            }
            
        }
        
        return false;
    }
    
    public abstract void initDB(Context context);
    
    /** 添加数据库操作任务(使用默认参数) */
    public void execTask(final DBProcessTask<?> task) {
        execTaskWithoutEnv(task, mDB, mMainHandler);
    }
    
    /** 添加数据库操作任务(由调用者提供初始化参数) */
    public void execTaskWithoutEnv(final DBProcessTask<?> task, SQLiteDatabase db,
                                   Handler handler) {
        if (db == null) {
            Log.e(TAG, "db is null");
            task.initEnv(mDB, handler);
        }
        else {
            task.initEnv(db, handler);
        }
        mHandler.post(task);
    }
    
    /**
     * 根据不同的表查询不同的字段
     */
    public boolean isContentExistsInTable(String table,
            String columnName, String value) {
        boolean result = false;
        
        Cursor cursor = null;
        try {
            cursor = mDB.query(table, null, columnName + "=?", new String[] { value },
                    null, null, null);
            if (cursor != null) {
                result = cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        
        return result;
    }
}
