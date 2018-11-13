package com.qingqing.project.offline.view.city;

import com.qingqing.base.bean.City;
import com.qingqing.base.utils.HanziToPinyin;

import java.util.Comparator;

/**
 * Created by xiejingwen on 2017/5/27.
 */

public class NameComparatorUtils {

    public static boolean isNumber(char character) {
        return character >= '0' && character <= '9';
    }

    public static boolean isLetter(char character) {
        return (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z');
    }
    public static boolean isLetter(String string){
        for (int i =0;i<string.length();i++){
            char c = string.charAt(i);
            if (!isLetter(c)){
                return false;
            }
        }
        return true;
    }

    public static class CityComparator implements Comparator<City> {

        @Override
        public int compare(City info1, City info2) {
            String lhs = info1.name;
            String rhs = info2.name;
            if (lhs == null) {
                return -1;
            }
            if (rhs == null) {
                return 1;
            }
            if (lhs == "") {
                return -1;
            }
            if (rhs == "") {
                return 1;
            }
            String lhsSortKey = HanziToPinyin.getSortKey(lhs).toLowerCase();
            String rhsSortKey = HanziToPinyin.getSortKey(rhs).toLowerCase();
            int lhsLength = lhsSortKey.length();
            int rhsLength = rhsSortKey.length();
            for (int i = 0; i < lhsLength && i < rhsLength; i++) {
                char lhsChar = lhsSortKey.charAt(i);
                char rhsChar = rhsSortKey.charAt(i);
                if (lhsChar == rhsChar) {
                } else if (!isNumber(lhsChar) && !isLetter(lhsChar)) {
                    if (isLetter(rhsChar)) {
                        return -1;
                    } else if (isNumber(rhsChar)) {
                        return -1;
                    } else {
                        return lhsChar > rhsChar ? 1 : -1;
                    }
                } else if (isNumber(lhsChar)) {
                    if (isLetter(rhsChar)) {
                        return -1;
                    } else if (isNumber(rhsChar)) {
                        return lhsChar > rhsChar ? 1 : -1;
                    } else {
                        return 1;
                    }
                } else {
                    if (isLetter(rhsChar)) {
                        return lhsChar > rhsChar ? 1 : -1;
                    } else if (isNumber(rhsChar)) {
                        return 1;
                    } else {
                        return 1;
                    }
                }
            }
            if (lhsLength > rhsLength) {
                return 1;
            } else if (lhsLength == rhsLength) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
