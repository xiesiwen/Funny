package com.qingqing.base.news;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Created by huangming on 2016/12/23.
 */

class DefaultActivitySkipper implements NewsActivitySkipper {

    @Override
    public void gotoActivity(Activity from, News news) {
        //nothing
    }

    @Override
    public void gotoActivity(Fragment from, NewsConversation conversation) {
        //nothing
    }
}
