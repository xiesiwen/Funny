package com.qingqing.base.time;

import java.util.TimeZone;

import com.qingqing.base.data.SPManager;
import com.qingqing.base.utils.PackageUtil;

import android.os.SystemClock;

/**
 * 网络时间 管理类
 *
 * @see NetworkTime#begin(NetworkTimeFetcher)
 * @see NetworkTime#end()
 *
 * @see NetworkTime#currentTimeMillis()
 *
 */
public class NetworkTime {
    public static final String TAG = "NetworkTime";
    private static volatile NetworkTime sInstance;
    
    public interface NetworkTimeFetcher {
        void onFetchNetworkTime(final NetworkTimeDisposer holder);
    }
    
    public interface NetworkTimeDisposer {
        void onDisposerNetworkTime(final long time);
    }

    private static final String KEY_NETWORK_LAST_REQ_TIME = "network_last_req_time";
    private static final int CHECK_INTERVAL = 60 * 30;
    private boolean isFetching;
    
    private long mNetworkTime;
    private long mElapsedRealTime;
    
    private NetworkTimeFetcher networkTimeFetcher;
    private NetworkTimeDisposer holder = new NetworkTimeDisposer() {
        @Override
        public void onDisposerNetworkTime(long time) {
            if(time > 0){
                mNetworkTime = time;
                mElapsedRealTime = SystemClock.elapsedRealtime();
                SPManager.put(KEY_NETWORK_LAST_REQ_TIME,mElapsedRealTime);
            }
            isFetching = false;
        }
    };
    

    private NetworkTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+08:00"));
    }
    
    /**
     * 初始化方法
     */
    public static void begin(NetworkTimeFetcher fetcher) {
    }
    

    private boolean isLocalTimeEnable() {
        return "true".equals(PackageUtil.getMetaData("qing.local.test"))
                && SPManager.getBoolean("local_time_enable", false);
    }
    

    /**
     * 获取网络时间，使用前许调用 init方法
     */
    public static long currentTimeMillis() {
        if (sInstance != null && SPManager.getLong(KEY_NETWORK_LAST_REQ_TIME,0) > 0) {
            return sInstance.mNetworkTime + SystemClock.elapsedRealtime()
                    - sInstance.mElapsedRealTime;
        }
        return System.currentTimeMillis();
    }
    
    public void destroy() {
        CountDownCenter.INSTANCE().cancelTask(TAG);
        networkTimeFetcher = null;
        sInstance = null;
    }
    
    /**
     * 停止使用，释放一些内存
     * 
     * 如果要再使用，必须先调用 {@link #begin(NetworkTimeFetcher)},否则不会同步网络时间
     */
    public static void end() {
        if (sInstance != null) {
            sInstance.destroy();
        }
    }
}
