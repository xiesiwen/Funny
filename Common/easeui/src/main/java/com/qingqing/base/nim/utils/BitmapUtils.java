package com.qingqing.base.nim.utils;

import android.graphics.BitmapFactory;
import android.os.Build;

/**
 * Created by huangming on 2016/8/25.
 */
public class BitmapUtils {
    
    private BitmapUtils() {}
    
    public static int[] getBitmapSize(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(filePath, options);
            
            // android 6.0中第一次decode，options的width 和Height可能为-1
            if (Build.VERSION.SDK_INT >= 23) {
                BitmapFactory.decodeFile(filePath, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        options.inJustDecodeBounds = false;
        return new int[] { options.outWidth, options.outHeight };
    }
    
}
