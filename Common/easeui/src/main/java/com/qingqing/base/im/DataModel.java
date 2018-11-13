package com.qingqing.base.im;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2015/12/23.
 */
public abstract class DataModel {

    protected boolean isInit;

    protected boolean isSyncing;
    protected boolean isSynced;

    private DataRequestListener requestListener;

    private List<DataSyncListener> syncListeners = new ArrayList<DataSyncListener>();

    protected static Context sContext;

    protected static HandlerThread sWorkerThread = new HandlerThread("Work");

    static {
        sWorkerThread.start();
    }

    protected static Handler sWorkerHandler = new Handler(sWorkerThread.getLooper());

    protected static Handler sMainHandler;

    void init() {
        isInit = true;
    }

    DataModel(Context context) {
        sContext = context.getApplicationContext();
        if (sMainHandler == null) {
            sMainHandler = new Handler(context.getApplicationContext().getMainLooper());
        }
    }


    protected void runOnMainThread(Runnable r) {
        if (Thread.currentThread().getId() != sMainHandler.getLooper().getThread().getId()) {
            sMainHandler.post(r);
        } else {
            r.run();
        }
    }

    protected void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() != android.os.Process.myTid()) {
            sWorkerHandler.post(r);
        } else {
            r.run();
        }
    }

    public void setRequestListener(DataRequestListener l) {
        requestListener = l;
    }

    public void addSyncListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (!syncListeners.contains(listener)) {
            syncListeners.add(listener);
        }
    }

    public void removeSyncListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (syncListeners.contains(listener)) {
            syncListeners.remove(listener);
        }
    }

    public void notifySyncListener(final boolean success) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setSynced(success);
                for (DataSyncListener listener : syncListeners) {
                    listener.onSyncComplete(success);
                }
            }
        });
    }

    public void setSyncing(boolean isSyncing) {
        this.isSyncing = isSyncing;
    }

    public void setSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public boolean isSyncing() {
        return isSyncing;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void asyncFetchData() {
        setSynced(false);
        if (requestListener != null) {
            requestListener.onRequest();
        }
    }

    public void asyncFetchData(Object... parameters) {
        if (requestListener != null) {
            requestListener.onRequest(parameters);
        }
    }

    void reset() {
        setSyncing(false);
        setSynced(false);
        syncListeners.clear();
        isInit = false;
    }

}
