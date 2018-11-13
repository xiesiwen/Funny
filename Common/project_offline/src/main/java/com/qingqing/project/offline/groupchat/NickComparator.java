package com.qingqing.project.offline.groupchat;

import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.base.utils.HanziToPinyin;

import java.util.Comparator;

import static com.qingqing.project.offline.groupchat.BaseGroupMemberListActivity.isAdmin;

/**
 * 管理员排在前面，按照A-Z排序
 */

public class NickComparator implements Comparator<ChatBean>{

    @Override
    public int compare(ChatBean bean1, ChatBean bean2) {
        UserProto.ChatUserInfo info1, info2;
        if (bean1.userInfoAdmin != null && bean2.userInfoAdmin != null) {
            info1 = bean1.userInfoAdmin.chatUserInfo;
            info2 = bean2.userInfoAdmin.chatUserInfo;
        } else {
            info1 = bean1.userInfo;
            info2 = bean2.userInfo;
        }
        if (info1 == null) {
            return -1;
        }
        if (info2 == null) {
            return 1;
        }
        boolean isAdmin1 = isAdmin(info1);
        boolean isAdmin2 = isAdmin(info2);
        if (isAdmin1 && !isAdmin2) { // 管理员排在前面
            return -1;
        }
        if (!isAdmin1 && isAdmin2) {//  管理员排在前面
            return 1;
        }
        String lhs = info1.nick;
        String rhs = info2.nick;
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
            if (lhsChar == rhsChar) { //这里不能返回0，应该接着遍历char数组
            } else if (!isNumber(lhsChar) && !isLetter(lhsChar)) { //左边是汉字
                if (isLetter(rhsChar)) { //右边是字母
                    return -1;
                } else if (isNumber(rhsChar)) { //右边是数字
                    return -1;
                } else {
                    return lhsChar > rhsChar ? 1 : -1;
                }
            } else if (isNumber(lhsChar)) { //左边是数字
                if (isLetter(rhsChar)) { //右边是字母
                    return 1;
                } else if (isNumber(rhsChar)) {
                    return lhsChar > rhsChar ? 1 : -1;
                } else {
                    return 1;
                }
            } else { //左边是字母
                if (isLetter(rhsChar)) {
                    return lhsChar > rhsChar ? 1 : -1;
                } else if (isNumber(rhsChar)) { //右边是数字
                    return -1;
                } else { // 右边是汉字
                    return -1;
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

    protected static boolean isNumber(char character) {
        return character >= '0' && character <= '9';
    }

    protected static boolean isLetter(char character) {
        return (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z');
    }
}
