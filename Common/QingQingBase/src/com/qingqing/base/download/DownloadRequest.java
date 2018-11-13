package com.qingqing.base.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

import com.qingqing.base.http.HttpCallback;
import com.qingqing.base.http.HttpManager;
import com.qingqing.base.http.HttpMethod;
import com.qingqing.base.http.HttpRequest;
import com.qingqing.base.http.HttpRequestBuilder;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.DeviceUtil;
import com.qingqing.base.utils.ExecUtil;
import com.qingqing.base.utils.NetworkUtil;

/**
 * Created by wangxiaxin on 2017/12/27.
 *
 * 下载请求
 */
public class DownloadRequest {
    
    private final static String TAG = "DownloadRequest";
    
    public final static int ERROR_NONE = 0;
    public final static int ERROR_SD_NO_MEMORY = 1;
    public final static int ERROR_BLOCK_INTERNET = 2;
    public final static int ERROR_UNKNOWN = 3;
    public final static int ERROR_REQ_ERROR = 4;
    public final static int ERROR_IO_ERROR = 5;
    
    private final static int TIME_OUT = 10000;
    private final static int BUFFER_SIZE = 1024 * 8;
    private final static String DOWNLOAD_SUFFIX = ".tmp";
    
    private File mFile;
    private File mTmpFile;
    private String mUrl;
    private Throwable mException;
    private RandomAccessFile mOutputStream;
    private DownloadListener mListener;
    private long mDownloadSize;
    private long mPreviousFileSize;
    private long mTotalSize;
    private long mDownloadPercent;// 100% is 100
    private long mNetworkSpeed; // 网速
    private long mPreviousTime;
    private long mTotalTime;
    private int mErrStatusCode = ERROR_NONE;
    private boolean mInterrupt = false;
    private boolean mIsUpgradeSelf = false;
    private HttpRequest realRequest;
    
    private final class ProgressReportingRandomAccessFile extends RandomAccessFile {
        private int mProgress = 0;
        
        ProgressReportingRandomAccessFile(File file, String mode)
                throws FileNotFoundException {
            super(file, mode);
        }
        
        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
            super.write(buffer, offset, count);
            mProgress += count;
            publishProgress(mProgress);
        }
    }
    
    public DownloadRequest(String url, String dirPath) throws MalformedURLException {
        this(url, dirPath, null);
    }
    
    public DownloadRequest(String url, String dirPath, DownloadListener listener)
            throws MalformedURLException {
        URL _url = new URL(url);
        String fileName = new File(_url.getFile()).getName();
        File file = new File(dirPath, fileName);
        init(url, file, listener);
    }
    
    public DownloadRequest(String url, File file, DownloadListener listener) {
        init(url, file, listener);
    }
    
    private void init(String url, File file, DownloadListener listener) {
        mUrl = url;
        this.mListener = listener;
        mFile = file;
        mTmpFile = new File(file.getParentFile(), file.getName() + DOWNLOAD_SUFFIX);
    }
    
    public DownloadRequest setSelfUpgrade() {
        mIsUpgradeSelf = true;
        return this;
    }
    
    public boolean isSelfUpgrade() {
        return mIsUpgradeSelf;
    }
    
    public String getUrl() {
        return mUrl;
    }
    
    public long getDownloadPercent() {
        return mDownloadPercent;
    }
    
    public long getDownloadSize() {
        return mDownloadSize + mPreviousFileSize;
    }
    
    public long getTotalSize() {
        return mTotalSize;
    }
    
    public long getDownloadSpeed() {
        return this.mNetworkSpeed;
    }
    
    public long getTotalTime() {
        return this.mTotalTime;
    }
    
    public File getDownloadFile() {
        return mFile;
    }
    
    public void execute() {
        mPreviousTime = System.currentTimeMillis();
        notifyPreDownload();
        try {
            download();
        } catch (Exception e) {
            mException = e;
            if (mErrStatusCode <= ERROR_NONE)
                mErrStatusCode = ERROR_UNKNOWN;
        }
    }
    
    public void cancel() {
        mInterrupt = true;
        if (realRequest != null && !realRequest.isCanceled()) {
            realRequest.cancel();
        }
    }
    
    private void publishProgress(Integer... progress) {
        if (progress.length > 1) {
            mTotalSize = progress[1];
            if (mTotalSize == -1) {
                mErrStatusCode = ERROR_UNKNOWN;
                notifyErrorDownload();
            }
            // else {
            //
            // }
        }
        else {
            mTotalTime = System.currentTimeMillis() - mPreviousTime;
            mDownloadSize = progress[0];
            mDownloadPercent = (mDownloadSize + mPreviousFileSize) * 100 / mTotalSize;
            mNetworkSpeed = mDownloadSize / mTotalTime;
            Logger.v(TAG, "publishProgress : percent=" + mDownloadPercent + "  size="
                    + mDownloadSize + "  speed=" + mNetworkSpeed);
            notifyUpdateProcess();
        }
    }
    
    private void download() {
        
        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        realRequest = requestBuilder.url(mUrl).client(HttpManager.HttpClient.UD).method(HttpMethod.GET).build();
        realRequest.async(false, new HttpCallback() {
            @Override
            public void onError(HttpRequest request) {
                Logger.w(TAG, "thread=" + Thread.currentThread().getId()
                        + "  download  :  " + mUrl + "   get content length failed");
                mErrStatusCode = ERROR_REQ_ERROR;
                notifyErrorDownload();
            }
            
            @Override
            public void onResponse(HttpRequest request) {
                mTotalSize = request.responseBody().contentLength();
                Logger.v(TAG, "thread=" + Thread.currentThread().getId()
                        + "  mTotalSize: " + mTotalSize);
                if (mTotalSize > 0 && mFile.exists() && mTotalSize == mFile.length()) {
                    Logger.v(TAG, "Output file already exists. Skipping download.");
                    request.closeResponse();
                    notifyFinishDownload();
                    return;
                }
                
                // 临时文件大于目标大小
                if (mTotalSize > 0 && mTmpFile.exists()
                        && mTotalSize < mTmpFile.length()) {
                    mTmpFile.delete();
                }
                
                if (mTmpFile.exists() && mTmpFile.length() > 0)
                    requestBuilder.addHeader("Range", "bytes=" + mTmpFile.length() + "-");
                
                mPreviousFileSize = mTmpFile.length();
                Logger.v(TAG, "File is not complete, download now.");
                Logger.v(TAG,
                        "File length:" + mTmpFile.length() + " mTotalSize:" + mTotalSize);
                request.cancel();
                realRequest = requestBuilder.setResponseStreamMode(true).build();
                realRequest.async(false, new HttpCallback() {
                    @Override
                    public void onError(HttpRequest request) {
                        mErrStatusCode = ERROR_REQ_ERROR;
                        notifyErrorDownload();
                    }
                    
                    @Override
                    public void onResponse(HttpRequest request) {
                        
                        long storage = DeviceUtil.getAvailableStorage(mTmpFile);
                        Logger.i(TAG, "storage:" + storage + " mTotalSize:" + mTotalSize);
                        if (mTotalSize > 0 && mTotalSize - mTmpFile.length() > storage) {
                            mErrStatusCode = ERROR_SD_NO_MEMORY;
                            mException = new RuntimeException("no memory");
                            notifyErrorDownload();
                            return;
                        }
                        
                        try {
                            mOutputStream = new ProgressReportingRandomAccessFile(
                                    mTmpFile, "rw");
                        } catch (FileNotFoundException e) {
                            Logger.v(TAG, "OutputStream Error");
                            mException = e;
                            mErrStatusCode = ERROR_IO_ERROR;
                            notifyErrorDownload();
                            return;
                        }
                        
                        publishProgress(0, (int) mTotalSize);
                        InputStream input = request.responseBody().stream();
                        try {
                            int bytesCopied = copy(input, mOutputStream);
                            if ((mPreviousFileSize + bytesCopied) != mTotalSize
                                    && mTotalSize != -1 && !mInterrupt) {
                                mException = new IOException("Download incomplete: "
                                        + bytesCopied + " != " + mTotalSize);
                                mErrStatusCode = ERROR_UNKNOWN;
                                notifyErrorDownload();
                                return;
                            }
                            mOutputStream.close();
                        } catch (Exception e) {
                            mException = e;
                            mErrStatusCode = ERROR_BLOCK_INTERNET;
                            notifyErrorDownload();
                            return;
                        }
                        
                        // 下载成功
                        if (mTmpFile.exists() && mTotalSize == mTmpFile.length()) {
                            if (mFile.exists())
                                mFile.delete();
                            mTmpFile.renameTo(mFile);
                            notifyFinishDownload();
                            Logger.v(TAG, "Download completed successfully.");
                        }
                        else {
                            mTmpFile.delete();
                            mErrStatusCode = ERROR_UNKNOWN;
                            notifyErrorDownload();
                        }
                    }
                });
            }
        });
    }
    
    public int copy(InputStream input, RandomAccessFile out) throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];
        
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        Logger.v(TAG, "length" + out.length());
        out.seek(out.length());
        
        int count = 0, n;
        long errorBlockTimePreviousTime = -1, expireTime;
        try {
            while (!mInterrupt) {
                n = in.read(buffer, 0, BUFFER_SIZE);
                if (n == -1) {
                    break;
                }
                
                out.write(buffer, 0, n);
                
                count += n;
                if (!NetworkUtil.isNetworkAvailable()) {
                    mInterrupt = true;
                    mErrStatusCode = ERROR_BLOCK_INTERNET;
                    throw new RuntimeException("no network");
                }
                
                if (mNetworkSpeed == 0) {
                    if (errorBlockTimePreviousTime > 0) {
                        expireTime = System.currentTimeMillis()
                                - errorBlockTimePreviousTime;
                        if (expireTime > TIME_OUT) {
                            mErrStatusCode = ERROR_BLOCK_INTERNET;
                            mInterrupt = true;
                        }
                    }
                    else {
                        errorBlockTimePreviousTime = System.currentTimeMillis();
                    }
                }
                else {
                    errorBlockTimePreviousTime = -1;
                }
            }
        } catch (Exception e) {
            Logger.w(e);
            throw e;
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                mErrStatusCode = ERROR_UNKNOWN;
                Logger.e(TAG, e.getMessage(), e);
            }
            try {
                in.close();
            } catch (IOException e) {
                mErrStatusCode = ERROR_UNKNOWN;
                Logger.e(TAG, e.getMessage(), e);
            }
        }
        return count;
    }
    
    private void notifyPreDownload() {
        ExecUtil.executeUI(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onPreDownload(DownloadRequest.this);
                }
            }
        });
    }
    
    private void notifyUpdateProcess() {
        ExecUtil.executeUI(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onUpdateProcess(DownloadRequest.this);
                }
            }
        });
    }
    
    private void notifyFinishDownload() {
        ExecUtil.executeUI(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onFinishDownload(DownloadRequest.this);
                }
            }
        });
    }
    
    private void notifyErrorDownload() {
        ExecUtil.executeUI(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onErrorDownload(DownloadRequest.this, mErrStatusCode,
                            mException);
                }
            }
        });
    }
}
