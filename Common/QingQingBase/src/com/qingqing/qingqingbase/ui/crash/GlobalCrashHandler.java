package com.qingqing.qingqingbase.ui.crash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import com.qingqing.base.data.BaseData;

import android.os.Environment;

/**
 * UncaughtExceptionHandler：线程未捕获异常控制器是用来处理未捕获异常的。 如果程序出现了未捕获异常默认情况下则会出现强行关闭对话框
 * 实现该接口并注册为程序中的默认未捕获异常处理 这样当未捕获异常发生时，就可以做些异常处理操作 例如：收集异常信息，发送错误报告 等。
 * 
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 */
public enum GlobalCrashHandler implements UncaughtExceptionHandler {
    
    INSTANCE;
    
    private static final String TAG = "GlobalCrashHandler";
    
    /** 系统默认的UncaughtException处理类 */
    private UncaughtExceptionHandler mDefaultHandler;
    private static final long QUICK_CRASH_ELAPSE = 10 * 1000;
    public static final int MAX_CRASH_COUNT = 5;
    
    private static final String DALVIK_XPOSED_CRASH = "Class ref in pre-verified class resolved to unexpected implementation";
    
    GlobalCrashHandler() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    
    /**
     * 初始化动作
     *
     * 加载当前fast crash的数量<br>
     * 发起reset的延迟任务<br>
     * 判断是否需要进入修复模式
     */
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex)) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }
    
    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     * 
     * @param ex
     *            throwable
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        String content = getStackTraceInfo(ex);
        File file = Environment.getExternalStorageDirectory();
        File eFile = new File(file.getPath(), "qingqing.log");
        if (eFile.exists()) {
            eFile.delete();
        }
        try {
            eFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(eFile);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BaseData.isForeground = false;
        return false;
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
    
    /**
     * Such as Xposed, if it try to load some class before we load from patch
     * files. With dalvik, it will crash with
     * "Class ref in pre-verified class resolved to unexpected implementation".
     * With art, it may crash at some times. But we can't know the actual crash
     * type. If it use Xposed, we can just clean patch or mention user to
     * uninstall it.
     */
//    private void tinkerPreVerifiedCrashHandler(Throwable ex) {
//        Throwable throwable = ex;
//        boolean isXposed = false;
//        while (throwable != null) {
//            if (!isXposed) {
//                isXposed = Utils.isXposedExists(throwable);
//            }
//            if (isXposed) {
//                // method 1
//                ApplicationLike applicationLike = TinkerManager
//                        .getTinkerApplicationLike();
//                if (applicationLike == null || applicationLike.getApplication() == null) {
//                    return;
//                }
//
//                if (!TinkerApplicationHelper.isTinkerLoadSuccess(applicationLike)) {
//                    return;
//                }
//                boolean isCausedByXposed = false;
//                // for art, we can't know the actually crash type
//                // just ignore art
//                if (throwable instanceof IllegalAccessError
//                        && throwable.getMessage().contains(DALVIK_XPOSED_CRASH)) {
//                    // for dalvik, we know the actual crash type
//                    isCausedByXposed = true;
//                }
//
//                if (isCausedByXposed) {
//                    SampleTinkerReport.onXposedCrash();
//                    TinkerLog.e(TAG, "have xposed: just clean tinker");
//                    // kill all other process to ensure that all process's code
//                    // is the same.
//                    ShareTinkerInternals
//                            .killAllOtherProcess(applicationLike.getApplication());
//
//                    TinkerApplicationHelper.cleanPatch(applicationLike);
//                    ShareTinkerInternals.setTinkerDisableWithSharedPreferences(
//                            applicationLike.getApplication());
//                    return;
//                }
//            }
//            throwable = throwable.getCause();
//        }
//    }
    
    /**
     * if tinker is load, and it crash more than MAX_CRASH_COUNT, then we just
     * clean patch.
     */
//    private boolean tinkerFastCrashProtect() {
//        ApplicationLike applicationLike = TinkerManager.getTinkerApplicationLike();
//
//        if (applicationLike == null || applicationLike.getApplication() == null) {
//            return false;
//        }
//        if (!TinkerApplicationHelper.isTinkerLoadSuccess(applicationLike)) {
//            return false;
//        }
//
//        final long elapsedTime = SystemClock.elapsedRealtime()
//                - applicationLike.getApplicationStartElapsedTime();
//        // this process may not install tinker, so we use
//        // TinkerApplicationHelper api
//        if (elapsedTime < QUICK_CRASH_ELAPSE) {
//            String currentVersion = TinkerApplicationHelper
//                    .getCurrentVersion(applicationLike);
//            if (ShareTinkerInternals.isNullOrNil(currentVersion)) {
//                return false;
//            }
//
//            SharedPreferences sp = applicationLike.getApplication().getSharedPreferences(
//                    ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG,
//                    Context.MODE_MULTI_PROCESS);
//            int fastCrashCount = sp.getInt(currentVersion, 0) + 1;
//            if (fastCrashCount >= MAX_CRASH_COUNT) {
//                SampleTinkerReport.onFastCrashProtect();
//                TinkerApplicationHelper.cleanPatch(applicationLike);
//                TinkerLog.e(TAG,
//                        "tinker has fast crash more than %d, we just clean patch!",
//                        fastCrashCount);
//                return true;
//            }
//            else {
//                sp.edit().putInt(currentVersion, fastCrashCount).apply();
//                TinkerLog.e(TAG, "tinker has fast crash %d times", fastCrashCount);
//            }
//        }
//        return false;
//    }
}
