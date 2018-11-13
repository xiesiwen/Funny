package com.qingqing.base.news;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Created by huangming on 2016/12/23.
 */

public interface NewsActivitySkipper {

    void gotoActivity(Activity from, News news);

    void gotoActivity(Fragment from, NewsConversation conversation);

}
