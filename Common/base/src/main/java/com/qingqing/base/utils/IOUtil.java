package com.qingqing.base.utils;

import com.qingqing.base.log.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by wangxiaxin on 2016/12/12.
 */

public final class IOUtil {

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            Logger.w("Closing", e);
        }
    }

    public static void flush(OutputStream stream) {
        if(stream != null) {
            try {
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
