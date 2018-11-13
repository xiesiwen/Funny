package com.qingqing.base.news;

import com.qingqing.base.news.News;

import java.util.Comparator;

/**
 * Created by huangming on 2016/12/5.
 */

public class NewsComparator implements Comparator<News> {

    @Override
    public int compare(News lhs, News rhs) {
        if (lhs.getCreatedTime() > rhs.getCreatedTime()) {
            return -1;
        }
        else if (lhs.getCreatedTime() < rhs.getCreatedTime()) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
