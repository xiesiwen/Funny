package com.qingqing.base.cache;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import com.qingqing.base.data.SPManager;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.FileUtil;
import com.qingqing.base.utils.MD5Util;

import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

/**
 * Created by wangxiaxin on 2016/12/26.
 * <p>
 * 文件全局管理类 since 5.5.0
 * <p>
 * 网络数据 {@link DiskCacheManager#TYPE_NETWORK_DATA}
 * <p>
 * 网络图片缓存 {@link DiskCacheManager#TYPE_NETWORK_PIC}
 * <p>
 * 本地照片缓存 {@link DiskCacheManager#TYPE_LOCAL_PHOTO}
 * <p>
 * 音频缓存 {@link DiskCacheManager#TYPE_LOCAL_AUDIO}
 * <p>
 * 视频缓存 {@link DiskCacheManager#TYPE_LOCAL_VIDEO}
 * <p>
 * 默认使用FIFO维护
 */

public final class DiskCacheManager {
    
    private static final String TAG = "DiskCacheManager";
    
    public static final int TYPE_NETWORK_DATA = 0x01;
    public static final int TYPE_NETWORK_PIC = 0x02;
    public static final int TYPE_LOCAL_PHOTO = 0x04;
    public static final int TYPE_LOCAL_AUDIO = 0x08;
    public static final int TYPE_LOCAL_VIDEO = 0x10;
    public static final int TYPE_DOWNLOAD = 0x20;
    public static final int TYPE_LOG = 0x40;
    
    private static final long KB_SIZE = 1024;
    private static final long MB_SIZE = 1024 * KB_SIZE;
    private static final long GB_SIZE = 1024 * MB_SIZE;
    
    private static final long DEFAULT_NETWORK_DATA_CACHE_SIZE = 100 * MB_SIZE;
    // private static final long DEFAULT_NETWORK_PIC_CACHE_SIZE = 80 * MB_SIZE;
    // private static final long DEFAULT_NETWORK_SMALL_PIC_CACHE_SIZE = 20 *
    // MB_SIZE;
    private static final long DEFAULT_LOCAL_PHOTO_CACHE_SIZE = 500 * MB_SIZE;
    private static final long DEFAULT_LOCAL_AUDIO_CACHE_SIZE = 500 * MB_SIZE;
    private static final long DEFAULT_LOCAL_VIDEO_CACHE_SIZE = 500 * MB_SIZE;
    private static final long DEFAULT_DOWNLOAD_CACHE_SIZE = 300 * MB_SIZE;
    private static final long DEFAULT_LOCAL_LOG_SIZE = 5 * MB_SIZE;
    /** 默认 24小时 自定review cache size */
    private static final long DEFAULT_REVIEW_INTERVAL = 86400_0_000;// ms
    private static final String KEY_REVIEW_CACHE = "review_cache_interval";
    
    private static DiskCacheManager sInstance;
    private boolean isReviewingCache;// 是否在review cache情况
    
    class DirData {
        File dir;
        long maxSize;
        
        DirData(File dir, long maxSize) {
            this.dir = dir;
            this.maxSize = maxSize;
        }
    }
    
    private SparseArray<DirData> dirArray = new SparseArray<>();
    private final SparseBooleanArray reviewFlagArray = new SparseBooleanArray(10);
    
    public static DiskCacheManager instance() {
        if (sInstance == null) {
            synchronized (DiskCacheManager.class) {
                if (sInstance == null) {
                    sInstance = new DiskCacheManager();
                }
            }
        }
        return sInstance;
    }
    
    private DiskCacheManager() {
        final File externalCacheRootDir = FileUtil.getExternalRootCacheFile();
        final File internalCacheRootDir = FileUtil.getInternalRootCacheFile();

        // 目前 OK HTTP 使用，且自身维护
        dirArray.put(TYPE_NETWORK_DATA,
                new DirData(new File(internalCacheRootDir, "logicCache"),
                        DEFAULT_NETWORK_DATA_CACHE_SIZE));
        // 目前 FRESCO 使用，不设置具体maxSize, 其内部维护
        dirArray.put(TYPE_NETWORK_PIC,
                new DirData(new File(externalCacheRootDir, "netPic"), 0));
        // 拍照和相册 使用
        dirArray.put(TYPE_LOCAL_PHOTO,
                new DirData(new File(externalCacheRootDir, "localPhoto"),
                        DEFAULT_LOCAL_PHOTO_CACHE_SIZE));
        dirArray.put(TYPE_LOCAL_AUDIO,
                new DirData(new File(externalCacheRootDir, "localAudio"),
                        DEFAULT_LOCAL_AUDIO_CACHE_SIZE));
        dirArray.put(TYPE_LOCAL_VIDEO,
                new DirData(new File(externalCacheRootDir, "localVideo"),
                        DEFAULT_LOCAL_VIDEO_CACHE_SIZE));
        dirArray.put(TYPE_DOWNLOAD,
                new DirData(new File(externalCacheRootDir, "downloadFile"),
                        DEFAULT_DOWNLOAD_CACHE_SIZE));
        dirArray.put(TYPE_LOG, new DirData(new File(externalCacheRootDir, "localLog"),
                DEFAULT_LOCAL_LOG_SIZE));
        
        // 删除废弃的几个文件夹和文件
        FileUtil.delete(new File(externalCacheRootDir, "cache_normal"));
        FileUtil.delete(new File(externalCacheRootDir, "cache_small"));
        FileUtil.delete(new File(externalCacheRootDir, "Log"));
        FileUtil.delete(new File(externalCacheRootDir, "temp_camera"));
        File[] currentFileList = externalCacheRootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        for (File f : currentFileList) {
            f.delete();
        }
    }
    
    /**
     * 返回指定类型的目录
     */
    public File dir(int type) {
        DirData dd = dirArray.get(type);
        if (dd != null) {
            if (dd.dir != null) {
                // 这里在返回之前确保目录存在
                if (dd.dir.exists() && dd.dir.isDirectory())
                    return dd.dir;
                else
                    return dd.dir.mkdirs() ? dd.dir : null;
            }
        }
        return null;
    }
    
    public File dirPhoto() {
        return dir(TYPE_LOCAL_PHOTO);
    }

    public File dirAudio() {
        return dir(TYPE_LOCAL_AUDIO);
    }
    
    public File dirNetPic() {
        return dir(TYPE_NETWORK_PIC);
    }
    
    public File dirNetData() {
        return dir(TYPE_NETWORK_DATA);
    }
    
    /**
     * 指定类型的缓存，最大size
     */
    public long maxSize(int type) {
        DirData dd = dirArray.get(type);
        if (dd != null)
            return dd.maxSize;
        else
            return 0;
    }
    
    public long size(int type) {
        DirData dd = dirArray.get(type);
        if (dd != null) {
            return FileUtil.size(dd.dir);
        }
        else {
            return 0;
        }
    }
    
    /**
     * 所有的大小
     */
    public long size() {
        long totalSize = 0;
        for (int i = 0; i < dirArray.size(); i++) {
            totalSize += size(dirArray.keyAt(i));
        }
        
        return totalSize;
    }
    
    public String sizeString() {
        long size = size();
        if (size < KB_SIZE) {
            return size + "B";
        }
        else if (size < MB_SIZE) {
            return String.format(Locale.CHINA, "%.1fKB", ((float) size / KB_SIZE) + 0.5f);
        }
        else {
            return String.format(Locale.CHINA, "%.1fMB", ((float) size / MB_SIZE) + 0.5f);
        }
    }
    
    /**
     * 清除指定类型的缓存目录
     */
    public void clear(final int type) {
    }
    
    /**
     * 清除所有本地的文件缓存
     */
    public void clear() {
        // FileUtil.clearDir(externalCacheRootDir);
        for (int i = 0; i < dirArray.size(); i++) {
            clear(dirArray.keyAt(i));
        }
    }
    
    public boolean isDuringClear() {
        return reviewFlagArray.size() > 0;
    }
    
    /**
     * 返回一个待使用的photo文件 ，不需要创建
     */
    public File emptyPhoto(String photoName) {
        return new File(dir(TYPE_LOCAL_PHOTO), photoName);
    }
    
    private File createMd5FileByUrl(int type, String url) {
        String md5 = MD5Util.encode(url);
        if (!TextUtils.isEmpty(md5)) {
            File file = new File(dir(type), md5);
            FileUtil.newFile(file);
            return file;
        }
        return null;
    }
    
    public File createAudio(String url) {
        return createMd5FileByUrl(TYPE_LOCAL_AUDIO, url);
    }
    
    public File createVideo(String url) {
        return createMd5FileByUrl(TYPE_LOCAL_VIDEO, url);
    }
    
    /**
     * 指定目录的size是否达到上限
     */
    public boolean isDirUpperLimit(int type) {
        long maxSize = maxSize(type);
        if (maxSize > 0) {
            return size(type) >= maxSize;
        }
        
        return false;
    }
    
    /**
     * 检查一个类型的cache
     * <p>
     * 异步处理
     */
    private void reviewCache(final int type) {
        
    }
    
    public void reviewCache() {
        if (isReviewingCache)
            return;
        
        Logger.d(TAG, "begin review cache");
        isReviewingCache = true;
        reviewCache(TYPE_LOCAL_AUDIO);
        reviewCache(TYPE_LOCAL_PHOTO);
        reviewCache(TYPE_LOCAL_VIDEO);
        reviewCache(TYPE_DOWNLOAD);
        isReviewingCache = false;
    }
    
    /**
     * 自动检查
     */
    public void autoReview() {
        final long currentTime = System.currentTimeMillis();
        final long lastReviewTime = SPManager.getLong(KEY_REVIEW_CACHE, 0);
        if (currentTime - lastReviewTime > DEFAULT_REVIEW_INTERVAL) {
            SPManager.put(KEY_REVIEW_CACHE, currentTime);
            reviewCache();
        }
    }
}
