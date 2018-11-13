package com.qingqing.base.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipUtil {
    
    public static boolean zip(String sourcePath, String zipFilePath) {
        File sourceFile = new File(sourcePath);
        File targetFile = new File(zipFilePath);
        
        FileOutputStream fos;
        boolean ret = false;
        
        try {
            fos = new FileOutputStream(targetFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            if (!zipFileInternal(new File[] { sourceFile }, zos)) {
                targetFile.delete();
            }else{
                ret = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public static boolean zip(File[] files, File zipFile) {
        
        boolean ret = false;
        
        if(files == null || files.length == 0)
            return ret;
        
        if (zipFile.exists())
            zipFile.delete();
        
        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            if (!zipFileInternal(files, zos)) {
                zipFile.delete();
            }else{
                ret = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public static boolean zip(ArrayList<File> files, File zipFile) {
        
        boolean ret = false;
        
        if(files == null || files.size() == 0)
            return ret;
        
        if (zipFile.exists())
            zipFile.delete();
        
        File[] filesArray = new File[files.size()];
        files.toArray(filesArray);
        return zip(filesArray,zipFile);
    }
    
    public static boolean unzip(File dstzipFile) {
        final int BUFFER = 2048;
        try {
            ZipFile zipFile = new ZipFile(dstzipFile.getAbsolutePath());
            Enumeration<? extends ZipEntry> emu = zipFile.entries();
            while (emu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) emu.nextElement();
                // 会把目录作为一个file读出一次，所以只建立目录就可以，之下的文件还会被迭代到。
                if (entry.isDirectory()) {
                    new File(dstzipFile.getPath() + entry.getName()).mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(
                        zipFile.getInputStream(entry));
                File file = new File(dstzipFile.getParent() + File.separator
                        + entry.getName());
                // 加入这个的原因是zipfile读取文件是随机读取的，这就造成可能先读取一个文件
                // 而这个文件所在的目录还没有出现过，所以要建出目录来。
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
                int count;
                byte data[] = new byte[BUFFER];
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    bos.write(data, 0, count);
                }
                bos.flush();
                bos.close();
                bis.close();
            }
            zipFile.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
private static boolean zipFileInternal(File[] sourceFiles, ZipOutputStream zos) {
        
        boolean ret = false;
        FileInputStream fis = null;
        try {
            for (File file : sourceFiles) {
                
                if (!file.exists() || !file.canRead())
                    continue;
                
                fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(file.getName()));
                
                int b = 0;
                byte[] buffer = new byte[1024];
                while ((b = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, b);
                }
                zos.closeEntry();
                IOUtil.closeQuietly(fis);
            }
            ret = true;
        } catch (Exception e) {} finally {
            IOUtil.closeQuietly(fis);
            IOUtil.closeQuietly(zos);
        }
        
        return ret;
    }
}
