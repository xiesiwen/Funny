package com.qingqing.base.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 数组与阵列之间的转换方法
 *
 * Created by lihui on 2017/9/25.
 */

public class ListUtil {
    
    public static List<Integer> arrayToList(int[] array) {
        if (array == null) {
            return null;
        }
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }
    
    public static int[] listToArray(List<Integer> list) {
        if (list == null) {
            return null;
        }
        
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
