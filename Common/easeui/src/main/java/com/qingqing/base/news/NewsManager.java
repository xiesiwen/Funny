package com.qingqing.base.news;

import android.content.Context;

import com.qingqing.base.im.ChatManager;
import com.qingqing.base.log.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huangming on 2016/11/29.
 * <p>
 * 1.接收消息，并将消息delivery给conversation
 * <p>
 * 2.
 */

public class NewsManager {

    private static final String TAG = "NewsManager";

    private static NewsManager sInstance = new NewsManager();

    private Context context;

    private NewsConversationManager conversationManager;

    private NewsDBDao dbDao;

    private ExecutorService newsCountThreadPool;
    private ExecutorService threadPool;

    private NewsEventNotifier eventNotifier;

    private NewsReceivingProcessor receivingProcessor;

    private NewsActivitySkipper activitySkipper;

    private NewsContentProvider contentProvider = new DefaultContentProvider();

    private NewsManager() {
        this.threadPool = Executors.newCachedThreadPool();
        this.newsCountThreadPool = Executors.newSingleThreadExecutor();

        eventNotifier = new NewsEventNotifier();
        activitySkipper = new DefaultActivitySkipper();
    }

    public static NewsManager getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        this.dbDao = new NewsDBDao(context);
        this.conversationManager = new NewsConversationManager();

        this.receivingProcessor = new NewsReceivingProcessor(context);

        ChatManager.getInstance().addLoginChatCallback(new ChatManager.ChatCallback() {
            @Override
            public void onSuccess() {
                conversationManager.loadAllChatConversations();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String error) {

            }
        });

        loadAll();
    }

    public void onUserChanged() {
        Logger.i(TAG, "onUserChanged");

        reset();
        loadAll();
    }

    public void setActivitySkipper(NewsActivitySkipper activitySkipper) {
        this.activitySkipper = activitySkipper;
    }

    public NewsActivitySkipper getActivitySkipper() {
        return activitySkipper;
    }

    private void reset() {

    }

    private void loadAll() {
        conversationManager.loadAllConversations();
    }

    public NewsConversationManager getConversationManager() {
        return conversationManager;
    }

    public NewsDBDao getDBDao() {
        return dbDao;
    }

    ExecutorService getNewsCountThreadPool() {
        return newsCountThreadPool;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public NewsEventNotifier getEventNotifier() {
        return eventNotifier;
    }

    void deleteNewsBy(final String bid) {
        getThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    News news = getDBDao().deleteNewsBy(bid);
                    if(news != null) {
                        NewsConversation conversation = getConversationManager().getConversation(news.getConversationId());
                        if(conversation != null) {
                            conversation.deleteNewsFromMemoryCache(news);
                        }
                    }
                } catch (Exception e) {
                    Logger.e(TAG, "deleteNewsBy : " + bid, e);
                }
            }
        });
    }

    public void markNewsAsRead(String newsId) {
        getConversationManager().markNewsAsRead(newsId);
    }

    public void setContentProvider(NewsContentProvider contentProvider) {
        this.contentProvider = contentProvider;
    }

    public NewsContentProvider getContentProvider() {
        return contentProvider;
    }
}
