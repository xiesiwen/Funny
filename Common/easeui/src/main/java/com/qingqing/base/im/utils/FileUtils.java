package com.qingqing.base.im.utils;

import com.qingqing.base.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by huangming on 2016/6/6.
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    public static boolean copyFile(File srcFile, File destFile,
                                   boolean overlay) {

        // 判断源文件是否存在
        if (!srcFile.exists()) {
            Logger.w(TAG, "源文件：" + srcFile.getPath() + "不存在！");
            return false;
        } else if (!srcFile.isFile()) {
            Logger.w(TAG, "复制文件失败，源文件：" + destFile.getPath() + "不存在！");
            return false;
        }

        // 判断目标文件是否存在
        if (destFile.exists()) {
            // 如果目标文件存在并允许覆盖
            if (overlay) {
                // 删除已经存在的目标文件，无论目标文件是目录还是单个文件
                destFile.delete();
            }
        } else {
            // 如果目标文件所在目录不存在，则创建目录
            if (!destFile.getParentFile().exists()) {
                // 目标文件所在目录不存在
                if (!destFile.getParentFile().mkdirs()) {
                    // 复制文件失败：创建目标文件所在目录失败
                    return false;
                }
            }
        }

        // 复制文件
        int byteread = 0; // 读取的字节数
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];

            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
