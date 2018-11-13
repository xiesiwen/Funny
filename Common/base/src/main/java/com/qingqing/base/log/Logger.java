package com.qingqing.base.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import com.qingqing.base.data.SPManager;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.utils.UtilsMgr;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by Wangxiaxin on 2015/9/21.<br/>
 *
 * <b>Logger 输出类</b><br/>
 *
 * 级别分为：VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT<br/>
 *
 * <b>所有情况下：</b><br/>
 *
 * 1，{@link Logger#o(String)} {@link Logger#w(String)} {@link Logger#e(String)}
 * {@link Logger#wtf(String)}在输出logcat的同时，还会输出到log文件，所以请慎重使用<br/>
 * 2，对于正常的运营日志，请使用{@link Logger#o(String)} 来输出<br/>
 *
 *
 * <b>debug情况下：</b> 所有log都会输出<br/>
 * <b>release情况下：</b> {@link Logger#v(String)} {@link Logger#d(String)}
 * {@link Logger#i(String)} 不会输出<br/>
 */
public class Logger {
    
    private static final boolean sIsDebuggable;
    private static final String STACK_INFO_FORMAT = "%s: %s.%s(L:%d) ";
    private static final int sReleaseLogLevel = Log.WARN;
    private static final int STACK_INDEX = 4;
    private static final String DEFAULT_TAG = "default";
    
    static {
        sIsDebuggable = PackageUtil.isApkDebuggable();
    }
    
    public static String getExceptionMessage(Exception e) {
        return (e == null ? "empty" : e.getMessage());
    }
    
    public static int v(String msg) {
        if (couldLog(Log.VERBOSE)) {
            String stackInfo = getLineInfo(Log.VERBOSE);
            return Log.v(DEFAULT_TAG, wrapMsgWithStackInfo(msg, stackInfo));
        }
        else
            return 0;
    }
    
    public static int v(String tag, String msg) {
        if (couldLog(Log.VERBOSE)) {
            String stackInfo = getLineInfo(Log.VERBOSE);
            return Log.v(tag, wrapMsgWithStackInfo(msg, stackInfo));
        }
        else
            return 0;
    }
    
    public static int v(String msg, Throwable tr) {
        if (couldLog(Log.VERBOSE)) {
            String stackInfo = getLineInfo(Log.VERBOSE);
            return Log.v(DEFAULT_TAG, wrapMsgWithStackInfo(msg, stackInfo), tr);
        }
        else
            return 0;
    }
    
    public static int v(String tag, String msg, Throwable tr) {
        if (couldLog(Log.VERBOSE)) {
            String stackInfo = getLineInfo(Log.VERBOSE);
            return Log.v(tag, wrapMsgWithStackInfo(msg, stackInfo), tr);
        }
        else
            return 0;
    }
    
    public static int d(String msg) {
        if (couldLog(Log.DEBUG)) {
            String stackInfo = getLineInfo(Log.DEBUG);
            return Log.d(DEFAULT_TAG, wrapMsgWithStackInfo(msg, stackInfo));
        }
        else
            return 0;
    }
    
    public static int d(String tag, String msg) {
        if (couldLog(Log.DEBUG)) {
            String stackInfo = getLineInfo(Log.DEBUG);
            return Log.d(tag, wrapMsgWithStackInfo(msg, stackInfo));
        }
        else
            return 0;
    }
    
    public static int d(String msg, Throwable tr) {
        if (couldLog(Log.DEBUG)) {
            String stackInfo = getLineInfo(Log.DEBUG);
            return Log.d(DEFAULT_TAG, wrapMsgWithStackInfo(msg, stackInfo), tr);
        }
        else
            return 0;
    }
    
    public static int d(String tag, String msg, Throwable tr) {
        if (couldLog(Log.DEBUG)) {
            String stackInfo = getLineInfo(Log.DEBUG);
            return Log.d(tag, wrapMsgWithStackInfo(msg, stackInfo), tr);
        }
        else
            return 0;
    }
    
    public static int i(String msg) {
        if (couldLog(Log.INFO)) {
            String stackInfo = getLineInfo(Log.INFO);
            return Log.i(DEFAULT_TAG, wrapMsgWithStackInfo(msg, stackInfo));
        }
        else
            return 0;
    }
    
    public static int i(String tag, String msg) {
        if (couldLog(Log.INFO)) {
            String stackInfo = getLineInfo(Log.INFO);
            return Log.i(tag, wrapMsgWithStackInfo(msg, stackInfo));
        }
        else
            return 0;
    }
    
    public static int i(String msg, Throwable tr) {
        if (couldLog(Log.INFO)) {
            String stackInfo = getLineInfo(Log.INFO);
            return Log.i(DEFAULT_TAG, wrapMsgWithStackInfo(msg, stackInfo), tr);
        }
        else
            return 0;
    }
    
    public static int i(String tag, String msg, Throwable tr) {
        if (couldLog(Log.INFO)) {
            String stackInfo = getLineInfo(Log.INFO);
            return Log.i(tag, wrapMsgWithStackInfo(msg, stackInfo), tr);
        }
        else
            return 0;
    }
    
    public static int o(String msg) {
        String stackInfo = getLineInfo(Log.INFO);
        String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
        int len = Log.i(DEFAULT_TAG, wrapMsg);
        UtilsLog.getInstance().saveLogFile(DEFAULT_TAG, wrapMsg);
        return len;
    }
    
    /** 单独输出运营日志，归类于INFO */
    public static int o(String tag, String msg) {
        String stackInfo = getLineInfo(Log.INFO);
        String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
        int len = Log.i(tag, wrapMsg);
        UtilsLog.getInstance().saveLogFile(tag, wrapMsg);
        return len;
    }
    
    public static int w(String msg) {
        if (couldLog(Log.WARN)) {
            String stackInfo = getLineInfo(Log.WARN);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.w(DEFAULT_TAG, wrapMsg);
            UtilsLog.getInstance().saveLogFile(DEFAULT_TAG, wrapMsg);
            return len;
        }
        else
            return 0;
    }
    
    public static int w(String tag, String msg) {
        if (couldLog(Log.WARN)) {
            String stackInfo = getLineInfo(Log.WARN);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.w(tag, wrapMsg);
            UtilsLog.getInstance().saveLogFile(tag, wrapMsg);
            return len;
        }
        else
            return 0;
    }
    
    public static int w(String tag, String msg, Throwable tr) {
        if (couldLog(Log.WARN)) {
            String stackInfo = getLineInfo(Log.WARN);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.w(tag, wrapMsg, tr);
            UtilsLog.getInstance().saveLogFile(tag, wrapMsg + getStackTraceInfo(tr));
            return len;
        }
        else
            return 0;
    }
    
    public static int w(Throwable tr) {
        if (couldLog(Log.WARN)) {
            String stackInfo = getLineInfo(Log.WARN);
            String wrapMsg = wrapMsgWithStackInfo("", stackInfo);
            int len = Log.w(DEFAULT_TAG, wrapMsg, tr);
            UtilsLog.getInstance().saveLogFile(DEFAULT_TAG, wrapMsg,
                    getStackTraceInfo(tr));
            return len;
        }
        else
            return 0;
    }
    
    public static int w(String tag, Throwable tr) {
        if (couldLog(Log.WARN)) {
            String stackInfo = getLineInfo(Log.WARN);
            String wrapMsg = wrapMsgWithStackInfo("", stackInfo);
            int len = Log.w(tag, wrapMsg, tr);
            UtilsLog.getInstance().saveLogFile(tag, wrapMsg, getStackTraceInfo(tr));
            return len;
        }
        else
            return 0;
    }
    
    public static int e(String msg) {
        if (couldLog(Log.ERROR)) {
            String stackInfo = getLineInfo(Log.ERROR);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.e(DEFAULT_TAG, wrapMsg);
            UtilsLog.getInstance().saveLogFile(DEFAULT_TAG, wrapMsg);
            return len;
        }
        else
            return 0;
    }
    
    public static int e(String tag, String msg) {
        if (couldLog(Log.ERROR)) {
            String stackInfo = getLineInfo(Log.ERROR);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.e(tag, wrapMsg);
            UtilsLog.getInstance().saveLogFile(tag, wrapMsg);
            return len;
        }
        else
            return 0;
    }
    
    public static int e(String msg, Throwable tr) {
        if (couldLog(Log.ERROR)) {
            String stackInfo = getLineInfo(Log.ERROR);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.e(DEFAULT_TAG, wrapMsg, tr);
            UtilsLog.getInstance().saveLogFile(DEFAULT_TAG, wrapMsg,
                    getStackTraceInfo(tr));
            return len;
        }
        else
            return 0;
    }
    
    public static int e(String tag, String msg, Throwable tr) {
        if (couldLog(Log.ERROR)) {
            String stackInfo = getLineInfo(Log.ERROR);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.e(tag, wrapMsg, tr);
            UtilsLog.getInstance().saveLogFile(tag, wrapMsg, getStackTraceInfo(tr));
            return len;
        }
        else
            return 0;
    }
    
    public static int wtf(String msg) {
        if (couldLog(Log.ASSERT)) {
            String stackInfo = getLineInfo(Log.ASSERT);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.wtf(DEFAULT_TAG, wrapMsg);
            UtilsLog.getInstance().saveLogFile(DEFAULT_TAG, wrapMsg);
            return len;
        }
        else
            return 0;
    }
    
    public static int wtf(String tag, String msg) {
        if (couldLog(Log.ASSERT)) {
            String stackInfo = getLineInfo(Log.ASSERT);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.wtf(tag, wrapMsg);
            UtilsLog.getInstance().saveLogFile(tag, wrapMsg);
            return len;
        }
        else
            return 0;
    }
    
    public static int wtf(String msg, Throwable tr) {
        if (couldLog(Log.ASSERT)) {
            String stackInfo = getLineInfo(Log.ASSERT);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.wtf(DEFAULT_TAG, wrapMsg, tr);
            UtilsLog.getInstance().saveLogFile(DEFAULT_TAG, wrapMsg,
                    getStackTraceInfo(tr));
            File file = new File(SPManager.getString("Upath"), "qingqing.log");
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(getStackTraceInfo(tr).getBytes());
                fileOutputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return len;
        }
        else
            return 0;
    }
    
    public static int wtf(String tag, String msg, Throwable tr) {
        if (couldLog(Log.ASSERT)) {
            String stackInfo = getLineInfo(Log.ASSERT);
            String wrapMsg = wrapMsgWithStackInfo(msg, stackInfo);
            int len = Log.wtf(tag, wrapMsg, tr);
            UtilsLog.getInstance().saveLogFile(tag, wrapMsg, getStackTraceInfo(tr));
            return len;
        }
        else
            return 0;
    }
    
    private static String wrapMsgWithStackInfo(String msg, String stackinfo) {
        return String.format("[%s] %s", stackinfo, msg);
    }
    
    private static boolean couldLog(int level) {
        return sIsDebuggable || level >= sReleaseLogLevel;
    }
    
    private static String getStackTraceInfo(Throwable tr) {
        
        if (tr == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(tr.getMessage()).append("\n");
        StackTraceElement[] elements = tr.getStackTrace();
        for (StackTraceElement el : elements) {
            sb.append(el.getClassName()).append("--").append(el.getMethodName())
                    .append("  L:").append(el.getLineNumber()).append("\n");
        }
        return sb.toString();
    }
    
    private static String getLogLevelString(int level) {
        switch (level) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
            default:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            case Log.ASSERT:
                return "A";
        }
    }
    
    private static String getLineInfo(int level) {
        StackTraceElement ele = Thread.currentThread().getStackTrace()[STACK_INDEX];
        if (ele != null) {
            String fullClassName = ele.getClassName();
            String className = fullClassName
                    .substring(fullClassName.lastIndexOf(".") + 1);
            return String.format(Locale.CHINA, STACK_INFO_FORMAT,
                    getLogLevelString(level), className, ele.getMethodName(),
                    ele.getLineNumber());
        }
        return "";
    }
}
