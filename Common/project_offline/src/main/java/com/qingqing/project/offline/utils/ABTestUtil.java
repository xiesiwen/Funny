package com.qingqing.project.offline.utils;

import com.adhoc.adhocsdk.AdhocTracker;

/**
 * Created by haoxinxin on 2017/8/23. a/b test 的一些方法
 */

public class ABTestUtil {

    public static boolean isUsingTeacherNativeFragmentV2() {
        return AdhocTracker.getFlag("isUseV2TeacherHomePage", false);
    }
}
