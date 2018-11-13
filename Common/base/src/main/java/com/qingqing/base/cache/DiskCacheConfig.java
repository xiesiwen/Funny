package com.qingqing.base.cache;

import android.util.SparseArray;

import java.io.File;

/**
 * Created by wangxiaxin on 2017/6/16.
 *
 * Disk Cache 的 builder 类，用来配置一些参数
 *
 * 如果直接使用默认配置
 */

public final class DiskCacheConfig {
    
    long reviewIntervalSeconds;// review cache的时间间隔
    SparseArray<Long> cacheSizeInfo = new SparseArray<>();
    SparseArray<DiskCacheManager.DirData> customCacheInfo = new SparseArray<>();
    File rootDir;
    
    public static DiskCacheConfig create() {
        return new DiskCacheConfig();
    }
    
    private DiskCacheConfig() {
        
    }
    
    /**
     * 设置cache 的review 时间间隔，不得小于300秒（5分钟）
     */
    public DiskCacheConfig setReviewInterval(long seconds) {
        
        if (seconds < 300) {
            seconds = 300;
        }
        reviewIntervalSeconds = seconds;
        return this;
    }
    
    /**
     * 设置具体类型的cache大小
     *
     * @param cacheType
     *            {@link DiskCacheManager#TYPE_LOCAL_PHOTO}
     */
    public DiskCacheConfig setCacheSizeInKB(int cacheType, long cacheSize) {
        this.cacheSizeInfo.put(cacheType, cacheSize * 1024);
        return this;
    }
    
    /**
     * 设置具体类型的cache大小
     *
     * @param cacheType
     *            {@link DiskCacheManager#TYPE_LOCAL_PHOTO}
     */
    public DiskCacheConfig setCacheSizeInMB(int cacheType, long cacheSize) {
        this.cacheSizeInfo.put(cacheType, cacheSize * 1024 * 1024);
        return this;
    }

    /**
     * 设置具体类型的cache大小
     *
     * @param cacheType
     *            {@link DiskCacheManager#TYPE_LOCAL_PHOTO}
     */
    public DiskCacheConfig customizeCache(int cacheType, long cacheSize,String dirName) {
//        this.customCacheInfo.put(cacheType, new DiskCacheManager.DirData());
        return this;
    }

    /**设置cache的根目录*/
    public DiskCacheConfig setCacheRootDir(File rootDir){
        this.rootDir = rootDir;
        return this;
    }
    
}
