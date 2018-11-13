package com.qingqing.base.news;

import android.content.Context;
import android.text.TextUtils;

import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.utils.ImageUrlUtil;

/**
 * Created by huangming on 2016/12/28.
 */

public abstract class AbsNewsContentProvider implements NewsContentProvider {

    @Override
    public String getConversationIconUrl(Context context, NewsConversation conversation) {
        NewsConversationType type = NewsConversationType.mapStringToValue(conversation.getType());
        switch (type) {
            case SINGLE_CHAT:
                return ImageUrlUtil.getHeadImg(IMUtils.getAvatar(conversation.getId()));
            default:
                return "";
        }
    }

    @Override
    public String getConversationTitle(Context context, NewsConversation conversation) {
        NewsConversationType type = NewsConversationType.mapStringToValue(conversation.getType());
        switch (type) {
            case SINGLE_CHAT:
                return IMUtils.getName(conversation.getLastNews().getConversationId());
            case GROUP_CHAT:
                return conversation.getName();
            default:
                return conversation.getType();
        }
    }

    @Override
    public String getConversationContent(Context context, NewsConversation conversation) {
        News lastNews = conversation.getLastNews();
        String lastNewsText = "";
        if (lastNews != null) {
            if (NewsUtils.isChatType(conversation.getType())) {
                lastNewsText = lastNews.getBody();
            } else {
                lastNewsText = SystemNewsHolder.getNewsBody(lastNews).getContent();
            }
        }
        if (lastNewsText == null) {
            lastNewsText = "";
        }
        return lastNewsText;
    }

    @Override
    public String getNewsIconUrl(Context context, News news) {
        NewsConversationType type = NewsConversationType.mapStringToValue(news.getConversationType());
        switch (type) {
            case SINGLE_CHAT:
                return ImageUrlUtil.getHeadImg(IMUtils.getAvatar(news.getFrom()));
            case GROUP_CHAT:
                return "";
            default:
                SystemNewsBody body = SystemNewsHolder.getNewsBody(news);
                return ImageUrlUtil.getHeadImg(body.getStringAttribute("icon"));
        }
    }

    @Override
    public String getNewsTitle(Context context, News news) {
        SystemNewsBody body = SystemNewsHolder.getNewsBody(news);
        return TextUtils.isEmpty(body.getTitle()) ? "轻轻家教" : body.getTitle();
    }

    @Override
    public String getNewsContent(Context context, News news) {
        SystemNewsBody body = SystemNewsHolder.getNewsBody(news);
        return body.getContent();
    }

    @Override
    public int getMessageFreeIcon() {
        return 0;
    }
}
