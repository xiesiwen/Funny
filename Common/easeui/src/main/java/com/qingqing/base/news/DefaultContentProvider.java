package com.qingqing.base.news;

import android.content.Context;

import com.easemob.easeui.R;

/**
 * Created by huangming on 2016/12/28.
 */

class DefaultContentProvider extends AbsNewsContentProvider {

    @Override
    public int getConversationDefaultIcon(Context context, NewsConversation conversation) {
        return 0;
    }

    @Override
    public int getNewsDefaultIcon(Context context, News news) {
        NewsConversationType type = NewsConversationType.mapStringToValue(news.getConversationType());
        switch (type) {
            case SINGLE_CHAT:
                return R.drawable.user_pic_boy;
            case UNKNOWN:
            default:
                return 0;
        }
    }

    @Override
    public boolean isUnreadDigitMode(String type) {
        return false;
    }
}
