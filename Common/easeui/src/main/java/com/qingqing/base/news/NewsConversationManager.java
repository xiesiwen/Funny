package com.qingqing.base.news;

import android.text.TextUtils;

import com.qingqing.base.data.ChatSettingsManager;
import com.qingqing.base.log.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangming on 2016/11/29.
 * <p>
 * 管理消息会话
 * <p>
 * 1.存储消息会话
 * <p>
 * 2.加载消息会话
 * <p>
 * 3.未读消息总数
 * <p>
 * 4.添加消息到消息会话中
 */

public class NewsConversationManager {

    private static final String TAG = NewsConversationManager.class.getSimpleName();

    private Map<String, NewsConversation> allConversations;

    NewsConversationManager() {
//        allConversations = Collections
//                .synchronizedMap(new HashMap<String, NewsConversation>());
        allConversations = new ConcurrentHashMap<>();
    }

    public int getUnreadNewsCount() {
        int totalCount = 0;
        Set<String> keys = allConversations.keySet();
        for (String key : keys) {
            totalCount += allConversations.get(key).getUnreadNewsCount();
        }
        return totalCount;
    }

    public int getUnreadNewsCountOfDigitMode() {
        int totalCount = 0;
        Set<String> keys = allConversations.keySet();
        for (String key : keys) {
            NewsConversation conversation = allConversations.get(key);
            // 5.8.0增加 开启免打扰的消息不计入未读数量
            if (conversation != null
                    && NewsManager.getInstance().getContentProvider()
                            .isUnreadDigitMode(conversation.getType())
                    && !ChatSettingsManager.getInstance()
                            .isMsgNotificationIgnored(conversation.getId())) {
                totalCount += conversation.getUnreadNewsCount();
            }
        }
        return totalCount;
    }

    public boolean hasUnreadNews() {
        Set<String> keys = allConversations.keySet();
        for (String key : keys) {
            NewsConversation conversation = allConversations.get(key);
            int count = conversation != null ? conversation.getUnreadNewsCount() : 0;
            if(count > 0) {
                return true;
            }
        }
        return false;
    }

    public List<NewsConversation> getAllConversations() {
        List<NewsConversation> allConversations = new ArrayList<>(
                this.allConversations.size());
        Set<String> keys = this.allConversations.keySet();
        for (String key : keys) {
            NewsConversation conversation = this.allConversations.get(key);
            if(conversation != null) {
                allConversations.add(conversation);
            }
        }
        return allConversations;
    }

    void loadAllConversations() {
        Logger.i(TAG, "loadAllConversations");
        NewsManager.getInstance().getThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                allConversations.clear();
                try {
                    List<NewsConversation> conversations = NewsManager.getInstance().getDBDao().getAllConversations();
                    for (NewsConversation conversation : conversations) {
                        allConversations.put(conversation.getId(), conversation);
                        conversation.loadMoreNews();
                        conversation.loadUnreadCount();
                    }
                } catch (Exception e) {
                    Logger.e(TAG, "loadAllConversations", e);
                }
                //notify
                NewsManager.getInstance().getEventNotifier().notifyConversationListChanged();
            }
        });
    }

    public void loadAllChatConversations() {
        loadAllChatConversations(true);
    }

    void clearAllChatConversations() {
        Set<String> keys = new HashSet<>(allConversations.keySet());
        for (String key : keys) {
            NewsConversation conversation = allConversations.get(key);
            if(conversation != null && NewsUtils.isChatType(conversation.getType())) {
                allConversations.remove(key);
            }
        }
    }

    void loadAllChatConversations(boolean notify) {
        clearAllChatConversations();
        Logger.i(TAG, "loadAllChatConversations : " + notify);

        List<NewsConversation> conversations = NewsUtils.getAllChatConversations();

        for (NewsConversation conversation : conversations) {
            allConversations.put(conversation.getId(), conversation);
        }

        if (notify) {
            //notify
            NewsManager.getInstance().getEventNotifier().notifyConversationListChanged();
        }
    }

    public NewsConversation getConversation(String conversationId) {
        return allConversations.get(conversationId);
    }

    private boolean addConversation(NewsConversation conversation) {
        if (conversation != null && !allConversations.containsKey(conversation.getId())) {
            allConversations.put(conversation.getId(), conversation);
            return true;
        }
        return false;
    }

    public void deleteConversation(final String conversationId) {
        NewsConversation conversation = getConversation(conversationId);
        if (conversation != null) {
            allConversations.remove(conversationId);
            NewsManager.getInstance().getEventNotifier().notifyConversationListChanged();
            NewsManager.getInstance().getThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        NewsManager.getInstance().getDBDao().deleteConversation(conversationId);
                    } catch (Exception e) {
                        Logger.e(TAG, "deleteConversation : " + conversationId, e);
                    }
                }
            });

            conversation.deleteAllNewses();
        }
    }

    public void addNewNews(News news) {
        String conversationId = news.getConversationId();
        String conversationType = news.getConversationType();
        NewsConversation conversation = getConversation(conversationId);
        if(!TextUtils.isEmpty(conversationId) && !TextUtils.isEmpty(conversationType)) {
            if (conversation == null) {
                conversation = new NewsConversation(conversationId, conversationType);
                allConversations.put(conversationId, conversation);
                NewsManager.getInstance().getEventNotifier().notifyConversationListChanged();
                saveConversation(conversation);
            }
            conversation.addNewNews(news);
        }
    }

    private void saveConversation(final NewsConversation conversation) {
        NewsManager.getInstance().getThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    NewsManager.getInstance().getDBDao().saveConversations(conversation);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(TAG, "saveConversation : " + conversation.toString(), e);
                }
            }
        });
    }
    
    /**
     * 系统消息的置顶，暂不用
     */
    public void setStickTop(String conversationId, boolean stickTop) {
        try {
            NewsManager.getInstance().getDBDao().setConversationStickTop(conversationId,
                    stickTop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 系统消息的置顶，暂不用
     */
    public boolean getStickTop(String conversationId) {
        try {
            return NewsManager.getInstance().getDBDao()
                    .getConversationStickTop(conversationId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    void markNewsAsRead(String newsId) {
        Set<String> keys = new HashSet<>(allConversations.keySet());
        for (String key : keys) {
            NewsConversation conversation = allConversations.get(key);
            News news = conversation.getUnreadNews(newsId);
            if(news != null) {
                conversation.markNewsAsRead(news);
                break;
            }
        }
    }

}
