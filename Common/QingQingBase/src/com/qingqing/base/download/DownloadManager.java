package com.qingqing.base.download;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;

import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.FileUtil;

/**
 * Created by Wangxiaxin on 2015/10/10.
 * 
 * 下载管理类
 */
public class DownloadManager {
    
    private static final String TAG = "DownloadManager";
    private static DownloadManager sInstance;
    private HashMap<String, DownloadRequest> mTaskMap;
    private HashMap<String, DownloadListener> mDownloadListenerMap;
    private static final String DOWNLOAD_DIR = "download";
    
    public static DownloadManager INSTANCE() {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager();
                }
            }
        }
        return sInstance;
    }
    
    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onUpdateProcess(DownloadRequest task) {
            DownloadListener l = mDownloadListenerMap.get(task.getUrl());
            if (l != null) {
                l.onUpdateProcess(task);
            }
        }
        
        @Override
        public void onFinishDownload(DownloadRequest task) {
            DownloadListener l = mDownloadListenerMap.get(task.getUrl());
            mTaskMap.remove(task.getUrl());
            mDownloadListenerMap.remove(task.getUrl());
            
            if (l != null) {
                l.onFinishDownload(task);
            }
        }
        
        @Override
        public void onPreDownload(DownloadRequest task) {
            DownloadListener l = mDownloadListenerMap.get(task.getUrl());
            if (l != null) {
                l.onPreDownload(task);
            }
        }
        
        @Override
        public void onErrorDownload(DownloadRequest task, int error,
                Throwable exception) {
            DownloadListener l = mDownloadListenerMap.get(task.getUrl());
            mTaskMap.remove(task.getUrl());
            mDownloadListenerMap.remove(task.getUrl());
            
            if (l != null) {
                l.onErrorDownload(task, error, exception);
            }
        }
    };
    
    private DownloadManager() {
        mTaskMap = new HashMap<>();
        mDownloadListenerMap = new HashMap<>();
    }
    
    /**
     * 判断指定的URL下载任务是否已存在
     */
    public boolean isTaskExist(String url) {
        return mTaskMap.containsKey(url);
    }
    
    public File getDirFile() {
        File rootDir = FileUtil.getExternalRootCacheFile();
        File dir = new File(rootDir, DOWNLOAD_DIR);
        if (!dir.exists())
            dir.mkdirs();
        
        return dir;
    }
    
    /**
     * 获取下载目录
     */
    public String getDirName() {
        return getDirFile().getAbsolutePath();
    }
    
    /**
     * 清理下载目录
     */
    public void clearDir() {
        FileUtil.clearDir(getDirFile());
    }
    
    /**
     * 停止下载任务
     */
    public boolean stopTask(String url) {
        if (!isTaskExist(url))
            return false;
        
        DownloadRequest task = mTaskMap.get(url);
        task.cancel();
        mTaskMap.remove(url);
        return true;
    }
    
    /**
     * 尝试停止已开始的自升级任务
     */
    public boolean tryStopSelfUpgradeTask() {
        for (Object o : mTaskMap.entrySet()) {
            HashMap.Entry entry = (HashMap.Entry) o;
            String url = (String) entry.getKey();
            DownloadRequest task = (DownloadRequest) entry.getValue();
            if (task.isSelfUpgrade()) {
                task.cancel();
                mTaskMap.remove(url);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 开始下载任务，使用默认存储路径
     */
    public boolean startTask(String url, DownloadListener listener) {
        try {
            DownloadRequest task = new DownloadRequest(url, getDirName(),
                    mDownloadListener);
            return startTask(task, url, listener);
        } catch (MalformedURLException e) {
            Logger.w(TAG, e);
            return false;
        }
    }
    
    /**
     * 开始下载任务，使用指定的存储路径 since 5.5.0
     */
    public boolean startTask(String url, File targetFile, DownloadListener listener) {
        DownloadRequest task = new DownloadRequest(url, targetFile, mDownloadListener);
        return startTask(task, url, listener);
    }
    
    private boolean startTask(DownloadRequest task, String url,
            DownloadListener listener) {
        if (isTaskExist(url)) {
            mDownloadListenerMap.put(url, listener);
            return false;
        }
        
        task.execute();
        mTaskMap.put(url, task);
        mDownloadListenerMap.put(url, listener);
        return true;
    }
    
    /**
     * 开始下载自更新包
     */
    public boolean startUpgrade(String url, DownloadListener listener) {
        if (isTaskExist(url)) {
            mDownloadListenerMap.put(url, listener);
            return false;
        }
        
        try {
            DownloadRequest task = new DownloadRequest(url, getDirName(),
                    mDownloadListener).setSelfUpgrade();
            task.execute();
            mTaskMap.put(url, task);
            mDownloadListenerMap.put(url, listener);
            return true;
        } catch (MalformedURLException e) {
            Logger.w(TAG, e);
            return false;
        }
    }
}
