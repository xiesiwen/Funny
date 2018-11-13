package com.qingqing.base.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * View 的一些操作方法
 *
 * Created by lihui on 2018/1/9.
 */

public class ViewUtil {
    /**
     * 设置 margin 单位 px 必须为正数
     */
    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view != null
                && view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view
                    .getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
}
