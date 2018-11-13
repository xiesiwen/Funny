package com.qingqing.base.download;

public interface DownloadListener {
    void onUpdateProcess(DownloadRequest request); // 更新进度
    
    void onFinishDownload(DownloadRequest request); // 完成下载
    
    void onPreDownload(DownloadRequest request); // 准备下载
    
    void onErrorDownload(DownloadRequest request, int error, Throwable exception); // 下载错误
}
