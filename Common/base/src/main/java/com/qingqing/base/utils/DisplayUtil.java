package com.qingqing.base.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public final class DisplayUtil {
    
    // 资源文件密度换算，需要外部引用时更换为 public
    private static final float DENSITY_LDPI = 0.75f;
    private static final float DENSITY_MDPI = 1f;
    private static final float DENSITY_HDPI = 1.5f;
    private static final float DENSITY_XHDPI = 2f;
    private static final float DENSITY_XXHDPI = 3f;
    private static final float DENSITY_XXXHDPI = 4f;
    
    private static DisplayMetrics sDisplayMetrics = null;
    private static float sDensity = -1.0f;
    private static float sScaleDensity;
    private static int sPixelHeight;
    private static int sPixelWidth;
    private static float sDpiHeight;
    private static float sDpiWidth;
    private static double sScreenInches;
    
    public synchronized static void init(Context con) {
        if (sDisplayMetrics == null) {
            sDisplayMetrics = new DisplayMetrics();
            
            ((WindowManager) con.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(sDisplayMetrics);
            
            sDensity = sDisplayMetrics.density;
            sScaleDensity = sDisplayMetrics.scaledDensity;
            sPixelHeight = sDisplayMetrics.heightPixels;
            sPixelWidth = sDisplayMetrics.widthPixels;
            sDpiHeight = sPixelHeight / sDensity;
            sDpiWidth = sPixelWidth / sDensity;
            
            printInfo();
        }
    }
    
    public static int getScreenHeight() {
        return sPixelHeight;
    }
    
    public static int getScreenWidth() {
        return sPixelWidth;
    }
    
    public static float getScreenDensity() {
        return sDensity;
    }
    
    public static int dp2px(float dp) {
        return (int) (dp * sDensity + 0.5f);
    }
    
    public static int px2dp(float px) {
        return (int) (px / sDensity + 0.5f);
    }
    
    public static int px2sp(float pxValue) {
        return (int) (pxValue / sScaleDensity + 0.5f);
    }
    
    public static int sp2px(float spValue) {
        return (int) (spValue * sScaleDensity + 0.5f);
    }
    
    public static int getChannelUnit() {
        return (int) (sPixelHeight * 3 / 10.0);
    }
    
    public static void printInfo() {
        Log.i("DisplayInfo", "pixel=" + sPixelWidth + "x" + sPixelHeight + "--dpi="
                + sDensity + "--dpi=" + sDpiWidth + "x" + sDpiHeight);
    }
    
    public static float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }
    
    public static float getFontHeight(Paint paint) {
        FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }
    
    public static float getFontLeading(Paint paint) {
        FontMetrics fm = paint.getFontMetrics();
        return fm.leading - fm.ascent;
    }
    
    /**
     * 获得屏幕的物理尺寸
     */
    public static double getScreenInches() {
        if (sScreenInches <= 0) {
            int width = sDisplayMetrics.widthPixels;
            int height = sDisplayMetrics.heightPixels;
            double diagonal = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
            int dens = sDisplayMetrics.densityDpi;
            sScreenInches = (diagonal / (double) dens);
        }
        return sScreenInches;
    }
    
    public static boolean isScreenPadSize() {
        return getScreenInches() > 7;
    }
    
    /**
     * 获取资源图片的宽度,不可用于纯色的背景
     *
     * @param resId
     *            资源文件 id
     * @return 宽度, 单位 pixel
     */
    public static int getIconWidth(int resId) {
        if (resId == 0) {
            return 0;
        }
        Drawable drawable = UtilsMgr.getCtx().getResources().getDrawable(resId);
        
        if (drawable != null) {
            return drawable.getIntrinsicWidth();
        }
        else {
            return 0;
        }
    }
    
    /**
     * 获取资源图片的宽度
     *
     * @param resId
     *            资源文件 id
     * @param density
     *            图片的密度倍率,例如：图片在 xhdpi 文件夹中,为 2
     * @return 宽度, 单位 pixel
     */
    public static int getIconWidth(int resId, float density) {
        BitmapFactory.Options options = getMeasureOptions(resId);
        return (int) (options.outWidth * sDensity / density);
    }
    
    /**
     * 获取资源图片的高度,不可用于纯色的背景
     *
     * @param resId
     *            资源文件 id
     * @return 高度, 单位 pixel
     */
    public static int getIconHeight(int resId) {
        Drawable drawable = UtilsMgr.getCtx().getResources().getDrawable(resId);
        
        if (drawable != null) {
            return drawable.getIntrinsicHeight();
        }
        else {
            return 0;
        }
    }
    
    /**
     * 获取资源图片的高度
     *
     * @param resId
     *            资源文件 id
     * @param density
     *            图片的密度倍率,例如：图片在 xhdpi 文件夹中,为 2
     * @return 高度, 单位 pixel
     */
    public static int getIconHeight(int resId, float density) {
        BitmapFactory.Options options = getMeasureOptions(resId);
        return (int) (options.outHeight * sDensity / density);
    }
    
    private static BitmapFactory.Options getMeasureOptions(int resId) {
        BitmapFactory.Options dimensions = new BitmapFactory.Options();
        // 生成 null 的 bitmap，仅为测量宽度，不会消耗大量内存
        dimensions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(
                UtilsMgr.getCtx().getApplicationContext().getResources(), resId,
                dimensions);
        return dimensions;
    }
}
