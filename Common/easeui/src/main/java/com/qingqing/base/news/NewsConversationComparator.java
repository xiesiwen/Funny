package com.qingqing.base.news;

import java.util.Comparator;

/**
 * Created by huangming on 2016/12/5.
 */

public class NewsConversationComparator implements Comparator<NewsConversation> {

    @Override
    public int compare(NewsConversation lhs, NewsConversation rhs) {
        if (lhs.getPriority() > rhs.getPriority()) {
            return -1;
        } else if (lhs.getPriority() < rhs.getPriority()) {
            return 1;
        } else {
            if (lhs.getLastNewsTime() > rhs.getLastNewsTime()) {
                return -1;
            } else if (lhs.getLastNewsTime() < rhs.getLastNewsTime()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
