package com.qingqing.base.utils;

import android.os.Environment;

import com.qingqing.base.log.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.qingqing.base.utils.DeviceUtil.isSDCardExists;

/**
 * Created by wangxiaxin on 2016/12/26.
 *
 * 文件相关的工具类
 */

public final class FileUtil {
    
    /**
     * 获取 外部存储中的 cache根目录
     * 
     * 如果发生异常，则返回内部存储的cache根目录
     */
    public static File getExternalRootCacheFile() {
        if (isSDCardExists()) {
            File dir = UtilsMgr.getCtx().getExternalCacheDir();
            if (dir == null) {
                dir = new File(Environment.getExternalStorageDirectory(),
                        "Android/data/" + PackageUtil.getPackageName() + "/cache");
            }
            if (checkDir(dir)) {
                return dir;
            }
        }
        return getInternalRootCacheFile();
    }
    
    /**
     * 获取 内部存储中的 cache根目录
     */
    public static File getInternalRootCacheFile() {
        return UtilsMgr.getCtx().getCacheDir();
    }
    
    /**
     * 获取 外部存储中的 根目录
     */
    public static File getExternalRootFile() {
        if (isSDCardExists())
            return Environment.getExternalStorageDirectory();
        return null;
    }
    
    /**
     * 清空一个指定的目录以及里面所有包含的文件，支持嵌套
     */
    public static boolean clearDir(File dir) {
        
        if (dir == null || !dir.exists() || dir.isFile())
            return false;
        
        boolean ret = true;
        if (dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            if (fileList != null && fileList.length > 0) {
                for (File f : fileList) {
                    if (f.isFile()) {
                        if (!f.delete())
                            ret = false;
                    }
                    else if (f.isDirectory()) {
                        String[] subFiles = f.list();
                        if (subFiles != null && subFiles.length > 0) {
                            if (!clearDir(f))
                                ret = false;
                            if (!f.delete())
                                ret = false;
                        }
                        else {
                            if (!f.delete())
                                ret = false;
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * 删除一个文件或者目录
     */
    public static boolean delete(File fileOrDir) {
        if (fileOrDir == null || !fileOrDir.exists())
            return false;
        
        if (fileOrDir.isFile()) {
            return fileOrDir.delete();
        }
        else if (fileOrDir.isDirectory()) {
            return clearDir(fileOrDir) && fileOrDir.delete();
        }
        
        return false;
    }
    
    /**
     * 计算指定的文件或者目录的大小 in byte
     *
     * @param fileOrDir
     *            文件或者目录
     */
    public static long size(File fileOrDir) {
        if (fileOrDir == null || !fileOrDir.exists())
            return 0L;
        
        if (fileOrDir.isFile()) {
            return fileOrDir.length();
        }
        else if (fileOrDir.isDirectory()) {
            File[] fileList = fileOrDir.listFiles();
            if (fileList == null || fileList.length <= 0) {
                return 0L;
            }
            else {
                long size = 0;
                for (File f : fileList) {
                    size += size(f);
                }
                return size;
            }
        }
        else {
            return 0L;
        }
    }
    
    /**
     * 创建一个文件，如果已存在，返回false
     */
    public static boolean newFile(File file) {
        // 确保目录存在
        if (file == null)
            return false;
        if (file.exists() && file.isFile())
            return false;
        File dir = file.getParentFile();
        
        boolean ret = false;
        try {
            if (!dir.exists() || !dir.isDirectory()) {
                if (dir.mkdirs())
                    ret = file.createNewFile();
            }
            else {
                ret = file.createNewFile();
            }
            return ret;
        } catch (IOException e) {
            Logger.w(e);
            return false;
        }
    }
    
    /** 列出一个文件下的所有文件，包括嵌套的 */
    public static List<File> allFiles(File dir) {
        
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return null;
        
        ArrayList<File> fileArrayList = new ArrayList<>();
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                fileArrayList.add(f);
            }
            else if (f.isDirectory()) {
                List<File> subFiles = allFiles(f);
                if (subFiles != null) {
                    fileArrayList.addAll(subFiles);
                }
            }
        }
        
        return fileArrayList;
    }
    
    /**
     * 检测指定文件夹 是否存在，不存在则创建
     * 
     * @return check 操作之后，该文件夹 是否存在
     */
    public static boolean checkDir(File dir) {
        if (dir != null) {
            // 这里在返回之前确保目录存在
            if (dir.exists() && dir.isDirectory())
                return true;
            else
                return dir.mkdirs();
        }
        return false;
    }
}
