package com.qingqing.base.utils;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.os.Build;

/**
 * Created by wangxiaxin on 2017/6/1.
 *
 * 剪切板 工具
 *
 * todo 未完，待补充
 */

public final class ClipboardUtil {
    
    /**
     * 将指定的 字串 复制到系统剪切板
     */
    public static void copyToClipBoard(String targetString) {
        if (DeviceUtil.getSDKInt() <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // 得到剪贴板管理器
            copyToClipBoardGingerBread(targetString);
        }
        else {
            copyToClipBoardHoneyComb(targetString);
        }
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private static void copyToClipBoardGingerBread(String targetString) {
        android.text.ClipboardManager cmb = (android.text.ClipboardManager) UtilsMgr
                .getCtx().getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(targetString.trim());
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void copyToClipBoardHoneyComb(String targetString) {
        android.content.ClipboardManager cmb = (android.content.ClipboardManager) UtilsMgr
                .getCtx().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData myClip;
        myClip = ClipData.newPlainText("text", targetString);
        cmb.setPrimaryClip(myClip);
    }
    
}
