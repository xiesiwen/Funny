package com.qingqing.base.im;

import com.easemob.chat.EMMessage;

import java.util.Comparator;

/**
 * Created by huangming on 2016/5/30.
 */
public class MessageComparator implements Comparator<EMMessage> {

    @Override
    public int compare(EMMessage lhs, EMMessage rhs) {
        if (lhs.getMsgTime() > rhs.getMsgTime()) {
            return 1;
        } else if (lhs.getMsgTime() == rhs.getMsgTime()) {
            return 0;
        } else {
            return -1;
        }
    }
}