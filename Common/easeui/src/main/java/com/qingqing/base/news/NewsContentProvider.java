package com.qingqing.base.news;

import android.content.Context;

/**
 * Created by huangming on 2016/12/28.
 */

public interface NewsContentProvider {

    String getConversationIconUrl(Context context, NewsConversation conversation);

    int getConversationDefaultIcon(Context context, NewsConversation conversation);

    String getConversationTitle(Context context, NewsConversation conversation);

    String getConversationContent(Context context, NewsConversation conversation);

    String getNewsIconUrl(Context context, News news);

    int getNewsDefaultIcon(Context context, News news);

    String getNewsTitle(Context context, News news);

    String getNewsContent(Context context, News news);

    boolean isUnreadDigitMode(String type);

    int getMessageFreeIcon();
}
