package com.qingqing.base.news;

import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.log.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangming on 2016/11/29.
 * <p>
 * 消息会话
 * <p>
 * 1.管理当前消息会话中所有消息
 * <p>
 * 2.存储消息会话中消息
 * <p>
 * 3.分页加载数据库中消息
 */

public class NewsConversation {
    
    private static final String TAG = NewsConversation.class.getSimpleName();
    
    /**
     * The maximum priority
     */
    public static final int MAX_PRIORITY = 10;
    
    /**
     * The minimum priority
     */
    public static final int MIN_PRIORITY = 1;
    
    /**
     * The normal (default) priority value
     */
    public static final int NORM_PRIORITY = 1;

    /**
     * 群聊置顶优先级
     */
    public static final int TOP_NEWS_PRIORITY = 5;
    
    private final String id;
    private final String name;
    private final String type;
    private final Map<String, News> allNews;
    private final Map<String, News> unreadNewses;
    
    private News lastNews;
    
    private int unreadNewsCount;
    
    private int priority = NORM_PRIORITY;
    
    private boolean displayContent = true;
    
    private String status;
    
    private boolean first = true;
    
    private String subName;
    
    private boolean stickTop = false;

    public NewsConversation(String id, String type) {
        this(id, type, null);
    }
    
    public NewsConversation(String id, String type, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.allNews = Collections.synchronizedMap(new HashMap<String, News>());
        this.unreadNewses = Collections.synchronizedMap(new HashMap<String, News>());
    }
    
    public boolean isStickTop() {
        return stickTop;
    }
    
    public NewsConversation setStickTop(boolean stickTop) {
        this.stickTop = stickTop;
        return this;
    }
    
    public void setSubName(String subName) {
        this.subName = subName;
    }
    
    public String getSubName() {
        return subName;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public int getUnreadNewsCount() {
        return unreadNewsCount;
    }
    
    public void setUnreadNewsCount(int unreadNewsCount) {
        this.unreadNewsCount = unreadNewsCount;
    }
    
    public void setPriority(int priority) {
        if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
            throw new IllegalArgumentException("Priority out of range: " + priority);
        }
        this.priority = priority;
    }
    
    public void setDisplayContent(boolean displayContent) {
        this.displayContent = displayContent;
    }
    
    public boolean isDisplayContent() {
        return displayContent;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return status;
    }
    
    public int getPriority() {
        return priority;
    }

    private void changeUnreadNewsCount(int unreadNewsCount) {
        if (unreadNewsCount < 0) {
            unreadNewsCount = 0;
            Logger.e(TAG,
                    "changeUnreadNewsCount unreadNewsCount < 0 : " + unreadNewsCount);
        }
        int preUnreadCount = getUnreadNewsCount();
        setUnreadNewsCount(unreadNewsCount);
        if (preUnreadCount != getUnreadNewsCount()) {
            // notify--------
            NewsManager.getInstance().getEventNotifier().notifyUnreadCountChanged(this);
        }
    }
    
    void loadUnreadCount() {
        NewsManager.getInstance().getNewsCountThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    changeUnreadNewsCount(NewsManager.getInstance().getDBDao()
                            .getUnreadNewsCountOfConversation(getId()));
                } catch (Exception e) {
                    Logger.e(TAG, "loadUnreadCount : " + toString(), e);
                }
            }
        });
    }
    
    public void markNewsAsRead(News news) {
        final String newsId = news.getId();
        if (news.isUnread()) {
            news.setUnread(false);
            unreadNewses.remove(newsId);
            changeUnreadNewsCount(getUnreadNewsCount() - 1);
            NewsManager.getInstance().getThreadPool().submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        NewsManager.getInstance().getDBDao().saveUnreadNewsAsRead(newsId);
                    } catch (Exception e) {
                        Logger.e(TAG, "markAllNewsAsRead", e);
                    }
                }
            });
        }
    }
    
    public void markAllNewsAsRead() {
        Set<String> ids = unreadNewses.keySet();
        for (String newsId : ids) {
            News news = unreadNewses.get(newsId);
            news.setUnread(false);
        }
        unreadNewses.clear();
        changeUnreadNewsCount(0);
        NewsManager.getInstance().getThreadPool().submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    NewsManager.getInstance().getDBDao()
                            .saveUnreadNewsOfConversationAsRead(getId());
                } catch (Exception e) {
                    Logger.e(TAG, "markAllNewsAsRead", e);
                }
            }
        });
    }
    
    public void markAsNotFirst() {
        setFirst(false);
        NewsManager.getInstance().getThreadPool().submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    NewsManager.getInstance().getDBDao()
                            .saveConversationAsNotFirst(getId());
                } catch (Exception e) {
                    Logger.e(TAG, "markAllNewsAsRead", e);
                }
            }
        });
    }
    
    void addNewNews(final News news) {
        if (news == null) {
            return;
        }
        if (!allNews.containsKey(news.getId())) {
            allNews.put(news.getId(), news);
            changeLastNewsIfNeeded(news, true);
            
            // notify----------
            NewsManager.getInstance().getEventNotifier().notifyNewNews(news);
            
            if (news.isUnread()) {
                unreadNewses.put(news.getId(), news);
                changeUnreadNewsCount(getUnreadNewsCount() + 1);
            }
            NewsManager.getInstance().getThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        NewsManager.getInstance().getDBDao().saveNews(news);
                    } catch (Exception e) {
                        Logger.e(TAG, "addNews", e);
                    }
                }
            });
        }
    }
    
    void deleteNewsFromMemoryCache(News news) {
        Logger.e(TAG, "deleteNewsFromMemoryCache");
        allNews.remove(news.getId());
        unreadNewses.remove(news.getId());
        NewsManager.getInstance().getEventNotifier().notifyNewNews(news);
        if (news.isUnread()) {
            news.setUnread(false);
            changeUnreadNewsCount(getUnreadNewsCount() - 1);
        }
    }
    
    void deleteAllNewses() {
        int unreadCount = getUnreadNewsCount();
        allNews.clear();
        unreadNewses.clear();
        NewsManager.getInstance().getThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    NewsManager.getInstance().getDBDao()
                            .deleteAllNewsesOfConversation(getId());
                } catch (Exception e) {
                    Logger.e(TAG, "deleteAllNewses : " + toString(), e);
                }
            }
        });
        if (unreadCount > 0) {
            NewsManager.getInstance().getEventNotifier().notifyUnreadCountChanged(this);
        }
    }
    
    public void setLastNews(News lastNews) {
        this.lastNews = lastNews;
    }
    
    private void changeLastNewsIfNeeded(News news, boolean notify) {
        if (lastNews == null || lastNews.getCreatedTime() < news.getCreatedTime()) {
            setLastNews(news);
            if (notify) {
                // notify----------
                NewsManager.getInstance().getEventNotifier().notifyLastNewsChanged(this);
            }
        }
    }
    
    public void loadMoreNews(final Observer<List<News>> callback) {
        Logger.i(TAG, "loadMoreNews");
        NewsManager.getInstance().getThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    List<News> newses = NewsManager.getInstance().getDBDao()
                            .getNewses(getType(), allNews.size(), 10);
                    for (News news : newses) {
                        allNews.put(news.getId(), news);
                        changeLastNewsIfNeeded(news, true);
                        if (news.isUnread()) {
                            unreadNewses.put(news.getId(), news);
                        }
                    }
                    
                    if (callback != null) {
                        callback.onNext(newses);
                        callback.onCompleted();
                    }
                } catch (Exception e) {
                    Logger.e(TAG, "loadMoreNews", e);
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
                
            }
        });
    }
    
    void loadMoreNews() {
        loadMoreNews(null);
    }
    
    public List<News> getAllNews() {
        List<News> allNews = new ArrayList<>(this.allNews.size());
        Set<String> keys = this.allNews.keySet();
        for (String key : keys) {
            allNews.add(this.allNews.get(key));
        }
        return allNews;
    }
    
    public News getLastNews() {
        return lastNews;
    }
    
    public long getLastNewsTime() {
        return getLastNews() != null ? getLastNews().getCreatedTime() : 0;
    }
    
    News getUnreadNews(String newsId) {
        return unreadNewses.get(newsId);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("News(");
        sb.append("id=").append(id).append("\n");
        sb.append("type=").append(type).append("\n");
        sb.append("unreadNewsCount=").append(unreadNewsCount).append("\n");
        sb.append("lastNews=").append(lastNews).append("\n");
        sb.append(")");
        return sb.toString();
    }
    
}
