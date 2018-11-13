package com.qingqing.base.im.utils;

import android.text.TextUtils;

import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.utils.HanziToPinyin;

import java.util.Comparator;

/**
 * 联系人列表相关方法
 * 
 * Created by lihui on 2018/1/5.
 */

public class ContactUtils {
    public static boolean isNumber(char character) {
        return character >= '0' && character <= '9';
    }
    
    public static boolean isLetter(char character) {
        return (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z');
    }
    
    public static String getContactName(ContactInfo contactInfo) {
        String contactName = TextUtils.isEmpty(contactInfo.getAlias())
                ? contactInfo.getNick()
                : contactInfo.getAlias();
        return TextUtils.isEmpty(contactName) ? contactInfo.getUsername() : contactName;
    }
    
    public static class ContactComparator implements Comparator<ContactInfo> {
        
        @Override
        public int compare(ContactInfo info1, ContactInfo info2) {
            String lhs = getContactName(info1);
            String rhs = getContactName(info2);
            if (lhs == null) {
                return -1;
            }
            if (rhs == null) {
                return 1;
            }
            if ("".equals(lhs)) {
                return -1;
            }
            if ("".equals(rhs)) {
                return 1;
            }
            String lhsSortKey = HanziToPinyin.getSortKey(lhs).toLowerCase();
            String rhsSortKey = HanziToPinyin.getSortKey(rhs).toLowerCase();
            int lhsLength = lhsSortKey.length();
            int rhsLength = rhsSortKey.length();
            for (int i = 0; i < lhsLength && i < rhsLength; i++) {
                char lhsChar = lhsSortKey.charAt(i);
                char rhsChar = rhsSortKey.charAt(i);
                if (lhsChar == rhsChar) {}
                else if (!isNumber(lhsChar) && !isLetter(lhsChar)) {
                    if (isLetter(rhsChar)) {
                        return -1;
                    }
                    else if (isNumber(rhsChar)) {
                        return -1;
                    }
                    else {
                        return lhsChar > rhsChar ? 1 : -1;
                    }
                }
                else if (isNumber(lhsChar)) {
                    if (isLetter(rhsChar)) {
                        return -1;
                    }
                    else if (isNumber(rhsChar)) {
                        return lhsChar > rhsChar ? 1 : -1;
                    }
                    else {
                        return 1;
                    }
                }
                else {
                    if (isLetter(rhsChar)) {
                        return lhsChar > rhsChar ? 1 : -1;
                    }
                    else if (isNumber(rhsChar)) {
                        return 1;
                    }
                    else {
                        return 1;
                    }
                }
            }
            if (lhsLength > rhsLength) {
                return 1;
            }
            else if (lhsLength == rhsLength) {
                return 0;
            }
            else {
                return -1;
            }
        }
    }
}
