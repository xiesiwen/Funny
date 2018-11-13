package com.qingqing.base.nim.comparator;

import com.qingqing.base.nim.domain.Message;

import java.util.Comparator;

/**
 * Created by huangming on 2016/8/18.
 *
 * 根据编号排序
 */
public class MessageIndexComparator implements Comparator<Message> {
    
    @Override
    public int compare(Message lhs, Message rhs) {
        if (lhs.getIndex() == Message.INVALID_INDEX) {
            return 0;
        }
        if (rhs.getIndex() == Message.INVALID_INDEX) {
            return 0;
        }
        if (lhs.getIndex() > rhs.getIndex()) {
            return 1;
        }
        else if (lhs.getIndex() == rhs.getIndex()) {
            return 0;
        }
        else {
            return -1;
        }
    }
}
