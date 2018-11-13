package com.qingqing.base.news;

import com.qingqing.base.utils.ExecUtil;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by huangming on 2016/12/6.
 */

public class NewsEventNotifier {

    private Hashtable<NewsEventAction, Set<NewsEventListener>> filteredEventListeners = new Hashtable<>();

    NewsEventNotifier() {
        for (NewsEventAction eventAction : NewsEventAction.values()) {
            filteredEventListeners.put(eventAction, new HashSet<NewsEventListener>());
        }
    }

    public void registerEventListener(NewsEventListener eventListener) {
        registerEventListener(eventListener, NewsEventAction.values());
    }

    public void registerEventListener(NewsEventListener eventListener, NewsEventAction... eventActions) {
        if (eventActions != null) {
            for (NewsEventAction eventAction : eventActions) {
                Set<NewsEventListener> eventListeners = filteredEventListeners.get(eventAction);
                if (eventListeners != null) {
                    eventListeners.add(eventListener);
                }
            }
        }
    }

    public void unregisterEventListener(NewsEventListener eventListener) {
        for (NewsEventAction eventAction : filteredEventListeners.keySet()) {
            Set<NewsEventListener> eventListeners = filteredEventListeners.get(eventAction);
            if (eventListeners != null) {
                eventListeners.remove(eventListener);
            }
        }
    }

    public void notifyUnreadCountChanged(final NewsConversation conversation) {
        notifyEvent(NewsEventAction.UnreadCountChanged, conversation);
    }

    public void notifyNewNews(News news) {
        notifyEvent(NewsEventAction.NewNews, news);
    }

    void notifyLastNewsChanged(NewsConversation conversation) {
        notifyEvent(NewsEventAction.LastNewsChanged, conversation);
    }

    void notifyConversationListChanged() {
        notifyEvent(NewsEventAction.ConversationListChanged, null);
    }

    private void notifyEvent(final NewsEventAction eventAction, final Object eventData) {
        ExecUtil.executeUI(new Runnable() {
            @Override
            public void run() {
                Set<NewsEventListener> eventListeners = filteredEventListeners.get(eventAction);
                if (eventListeners != null) {
                    for (NewsEventListener eventListener : eventListeners) {
                        eventListener.onEvent(new NewsEvent(eventAction, eventData));
                    }
                }
            }
        });
    }

//    void reset() {
//        for (NewsEventAction eventAction : NewsEventAction.values()) {
//            Set<NewsEventListener> eventListeners = filteredEventListeners.get(eventAction);
//            if (eventListeners != null) {
//                eventListeners.clear();
//            }
//        }
//    }

}
