package com.qingqing.base.log;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.qingqing.base.cache.DiskCacheManager;
import com.qingqing.base.data.SPManager;
import com.qingqing.base.data.SPWrapper;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.DeviceUtil;
import com.qingqing.base.utils.DisplayUtil;
import com.qingqing.base.utils.FileUtil;
import com.qingqing.base.utils.IOUtil;
import com.qingqing.base.utils.NetworkUtil;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.utils.UtilsMgr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 
 * @author db
 * 
 */
public class UtilsLog {
    
    private static final String TAG = UtilsLog.class.getName();
    
    private static final String SP_KEY_LOG_SEEK = "log_seek";
    private static final String LOG_SUFFIX = ".log";
    private static final String LOG_ZIP = ".zip";
    private static final String LOG_SYS_INFO = "systeminfo.log";
    
    private static final String CHARSET = "UTF-8";
    private File logFile = null;
    
    private static UtilsLog sInstance;
    private static final Object sLock = new Object();
    private Context mContext;
    private long mCurrentSeekPosition;
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
    
    private String mInterID = DeviceUtil.getAndroidID("unknown");
    

    private Date mLogDate = new Date();
    
    private static final int MAX_FILE_LENGTH = 10 * 1024 * 1024;
    
    private Handler mLogHandler;
    private StringBuffer mLogBuilder;
    
    private static final int MSG_LOG = 7;
    private static final int MSG_SAVE_SEEK_POSITION = 8;
    private static final String MSG_KEY_LOG_MSG = "log_msg";
    private RandomAccessFile mLogFileAccess;
    private static final int SEEK_SYNC_FREQ = 10;
    private int mCurrentSyncFreq = SEEK_SYNC_FREQ;
    private SPWrapper seekPositionSP;
    
    public static synchronized UtilsLog getInstance() {
        if (sInstance == null) {
            sInstance = new UtilsLog();
        }
        return sInstance;
    }
    
    private UtilsLog() {
        mLogBuilder = new StringBuffer();
        seekPositionSP = new SPWrapper("seek_position");
        HandlerThread mLogThread = new HandlerThread("LoggerWriter");
        mLogThread.start();
        mLogHandler = new Handler(mLogThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LOG:
                        Bundle bd = msg.getData();
                        if (bd != null) {
                            saveLogFileInternal(bd.getString(MSG_KEY_LOG_MSG));
                        }
                        break;
                    case MSG_SAVE_SEEK_POSITION:
                        break;
                }
                
            }
        };
        setContent(UtilsMgr.getCtx());
    }
    
    private void setContent(Context ctx) {
        mContext = ctx;
        try {
            File logDir = getExternalLogDir();
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            logFile = new File(logDir, mInterID + LOG_SUFFIX);
            mLogFileAccess = new RandomAccessFile(logFile, "rw");
            mCurrentSeekPosition = seekPositionSP.getLong(SP_KEY_LOG_SEEK, 0);
            Logger.d(TAG, "size=" + logFile.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获得基本目录
     * 
     * @return File
     */
    private File getExternalLogDir() {
        File externalRootDir = FileUtil.getExternalRootFile();
        if(externalRootDir != null){
            File dir = new File(externalRootDir,"qingqing/" + PackageUtil.getPackageName());
            dir.mkdirs();
            return dir;
        }else{
            return DiskCacheManager.instance().dir(DiskCacheManager.TYPE_LOG);
        }
    }
    
    /**
     * 获得上传基本log
     * 
     * @return File
     */
    public File getUploadLogFile() {
        File dir = getExternalLogDir();
        File logFile = new File(dir, mInterID + LOG_SUFFIX);
        if (logFile.exists()) {
            return logFile;
        }
        else {
            return null;
        }
    }
    
    /**
     * 获得系统信息log
     * 
     * @return File
     */
    public File getSystemInfoFile() {
        File dir = getExternalLogDir();
        File logFile = new File(dir, LOG_SYS_INFO);
        if (logFile.exists()) {
            return logFile;
        }
        else {
            return null;
        }
    }
    
    /**
     * 获得上传zip
     * 
     * @return File
     */
    public File getZipFile() {
        File dir = getExternalLogDir();
        File logFile = new File(dir, mInterID + LOG_ZIP);
        if (logFile.exists()) {
            logFile.delete();
        }
            return logFile;
    }
    
    /**
     * 删除上传文件 zip
     * 
     * @return boolean
     */
    
    public boolean deleteUploadLogFileForZip() {
        File dir = mContext.getFilesDir();
        File logFile = new File(dir, mInterID + LOG_ZIP);
        return logFile.delete();
    }
    
    public boolean deleteUploadLogFile() {
        File dir = mContext.getFilesDir();
        File logFile = new File(dir, mInterID + LOG_SUFFIX);
        return logFile.delete();
    }
    
    private String buildLogs(String stackInfo, String modeMsg, String msg) {
        
        mLogBuilder.setLength(0);
        mLogDate.setTime(NetworkTime.currentTimeMillis());
        mLogBuilder
                .append("[")
                .append(mDateFormat.format(mLogDate))
                .append("]")
                .append(String.format(Locale.CHINA, "[tid:%d] ", Thread
                        .currentThread().getId())).append(stackInfo);
        
        if (!TextUtils.isEmpty(modeMsg)) {
            mLogBuilder.append(" ").append(modeMsg);
        }
        
        mLogBuilder.append(" ").append(msg).append("\n\n");
        return mLogBuilder.toString();
    }
    
    public boolean saveLogFile(String stackInfo, String logString) {
        return saveLogFile(stackInfo, null, logString);
    }
    
    public boolean saveLogFile(String stackInfo, String modeMsg, String logString) {
        synchronized (sLock) {
            Message msg = Message.obtain(mLogHandler);
            msg.what = MSG_LOG;
            Bundle bundle = new Bundle();
            bundle.putString(MSG_KEY_LOG_MSG, buildLogs(stackInfo, modeMsg, logString));
            msg.setData(bundle);
            msg.sendToTarget();
            return true;
        }
    }
    
    /**
     * log记录
     */
    private boolean saveLogFileInternal(String logString) {
        
        long fileLength;
        try {
            fileLength = mLogFileAccess.length();
        } catch (Exception e) {
            fileLength = -1;
        }
        
        if (fileLength < 0)
            return false;
        
        long seekPos;
        if (fileLength < MAX_FILE_LENGTH) {
            seekPos = fileLength;
        }
        else {
            seekPos = mCurrentSeekPosition;
        }
        
        try {
            mLogFileAccess.seek(seekPos);
            mLogFileAccess.write(logString.getBytes(CHARSET));
            if (fileLength >= MAX_FILE_LENGTH) {
                mCurrentSeekPosition = mLogFileAccess.getFilePointer();
//                if (mCurrentSyncFreq > 0) {
//                    --mCurrentSyncFreq;
//                }
//                else {
//                    mCurrentSyncFreq = SEEK_SYNC_FREQ;
                    seekPositionSP.put(SP_KEY_LOG_SEEK, mCurrentSeekPosition);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    /**
     * 系統信息log记录
     */
    public boolean saveSysLogFile(String logString) {

        FileOutputStream fos = null;
        try {
            File logDir = getExternalLogDir();
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            File logFile = new File(logDir, LOG_SYS_INFO);
            Logger.d(TAG, logFile.getPath());
            
            fos = new FileOutputStream(logFile);
            fos.write(logString.getBytes(CHARSET));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            IOUtil.closeQuietly(fos);
        }
        return true;
    }

    public static String buildSystemInfo() {
        return "\n" + "#-------system info-------" + "\n" + "version-name:"
                + PackageUtil.getVersionName() + "\n" + "version-code:"
                + PackageUtil.getVersionCode() + "\n" + "system-version:"
                + DeviceUtil.getBuildVersion() + "\n" + "model:" + DeviceUtil.getModel()
                + "\n" + "density:" + DisplayUtil.getScreenDensity() + "\n" + "imei:"
                + DeviceUtil.getDeviceID() + "\n" + "screen-height:"
                + DisplayUtil.getScreenHeight() + "\n" + "screen-width:"
                + DisplayUtil.getScreenWidth() + "\n" + "unique-code:"
                + DeviceUtil.getIdentification() + "\n" + "mobile:"
                + DeviceUtil.getLine1Number() + "\n" + "imsi:"
                + DeviceUtil.getIMSI() + "\n" + "isWifi:"
                + NetworkUtil.isWifiConnected() + "\n";
    }
}
