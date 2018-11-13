package com.qingqing.base.nim.comparator;

import com.qingqing.base.nim.domain.Message;

import java.util.Comparator;

/**
 * Created by huangming on 2016/8/17.
 * 
 * 根据时间排序
 */
public class MessageTimeComparator implements Comparator<Message> {
    @Override
    public int compare(Message lhs, Message rhs) {
        if (lhs.getMsgTime() > rhs.getMsgTime()) {
            return 1;
        }
        else if (lhs.getMsgTime() == rhs.getMsgTime()) {
            return 0;
        }
        else {
            return -1;
        }
    }
}
