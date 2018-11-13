package com.qingqing.base.nim;

import android.os.Handler;

import com.qingqing.base.interfaces.AbstractCoordinator;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.domain.ChatManager;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.domain.services.MessageService;
import com.qingqing.base.nim.utils.MessageUtils;
import com.qingqing.base.task.LimitingNumberTask;
import com.qingqing.base.view.ToastWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.easemob.easeui.R;

/**
 * Created by huangming on 2016/8/18.
 */
public class MessageOperator extends AbstractCoordinator
        implements MessageService.Listener {
    
    private static final String TAG = "whuthmMessageOperator";
    
    private static final long REFRESH_DELAY_MILLIS = 300;
    
    private static final int MSG_DELAY_REFRESH = 7002;
    private final ChatCoordinator conversationCoordinator;
    
    private MessageInterceptor interceptor;
    
    private LimitingNumberTask limitingNumberTask;
    
    private MessageService.Listener listener;
    
    private Handler handler;
    
    private Set<Message> pendingToAddCache = new HashSet<>();
    
    public MessageOperator(ChatCoordinator conversationCoordinator) {
        this.conversationCoordinator = conversationCoordinator;
        this.handler = new CacheHandler();
    }
    
    public void setInterceptor(MessageInterceptor interceptor) {
        this.interceptor = interceptor;
    }
    
    public MessageInterceptor getInterceptor() {
        return interceptor;
    }
    
    private ChatCoordinator getConversationCoordinator() {
        return conversationCoordinator;
    }
    
    private boolean isCoordinatorInitialized() {
        return getConversationCoordinator().isInitialized();
    }
    
    private String getConversationId() {
        return getConversationCoordinator().getConversationId();
    }
    
    private Handler getHandler() {
        return handler;
    }
    
    private Set<Message> getPendingToAddCache() {
        return pendingToAddCache;
    }
    
    public void setMessageServiceListener(MessageService.Listener listener) {
        this.listener = listener;
    }
    
    private MessageService.Listener getMessageServiceListener() {
        return listener;
    }
    
    public void send(Message message) {
        if (isNumberOfSpeechLimited()) {
            ToastWrapper.show(R.string.tips_normal_user_speak_limit);
            Logger.e(TAG, "isNumberOfSpeechLimited");
            return;
        }
        if (!isCoordinatorInitialized()) {
            Logger.e(TAG, "isCoordinatorInitialized false");
            return;
        }
        if(getLimitingNumberTask() != null) {
            getLimitingNumberTask().increase();
        }
        ChatManager.getInstance().getMessageService().sendMessage(message, null);
    }
    
    public void resend(Message message) {
        ChatManager.getInstance().getMessageService().resendMessage(message, null);
    }
    
    public void delete(Message message) {
        ChatManager.getInstance().getMessageService().deleteMessage(message, null);
    }
    
    public void loadMessageBody(Message message) {
        ChatManager.getInstance().getMessageService().loadMessageBody(message);
    }
    
    private LimitingNumberTask getLimitingNumberTask() {
        return limitingNumberTask;
    }
    
    public void setLimitingNumberTask(LimitingNumberTask limitingNumberTask) {
        if (this.limitingNumberTask != null) {
            this.limitingNumberTask.cancel();
        }
        this.limitingNumberTask = limitingNumberTask;
        this.limitingNumberTask.execute();
    }
    
    /**
     * 发言次数是否被限制
     */
    private boolean isNumberOfSpeechLimited() {
        return getLimitingNumberTask() != null && !getLimitingNumberTask().isAllowed();
    }
    
    @Override
    public void onMessageAdded(Message message) {
        ChatManager.getInstance().getNotifier().onNewMessage(message);
        if (MessageUtils.getConversationId(message).equals(getConversationId())) {
            
            if (getInterceptor() != null
                    && getInterceptor().onInterceptMessage(message)) {
                return;
            }
            
            Logger.i(TAG, "onMessageAdded  : " + message.getId());
            if (getMessageServiceListener() != null) {
                getMessageServiceListener().onMessageAdded(message);
            }
            // getPendingToAddCache().add(message);
            // if (MessageUtils.isSendDirect(message)) {
            // removeDelayRefreshMessage();
            // consumeCachedMessages();
            // }
            // else {
            // refreshDelayed();
            // }
        }
    }
    
    private void consumeCachedMessages() {
        if (getPendingToAddCache().size() > 0) {
            Logger.i(TAG, "consumeCachedMessages" + " " + Thread.currentThread() + "  "
                    + getPendingToAddCache().size());
            if (getMessageServiceListener() != null) {
                getMessageServiceListener()
                        .onMessagesAdded(new ArrayList<>(getPendingToAddCache()));
            }
            getPendingToAddCache().clear();
        }
    }
    
    @Override
    public void onMessagesAdded(List<Message> messages) {
        ChatManager.getInstance().getNotifier().onNewMessages(messages);
        if (messages != null && getMessageServiceListener() != null) {
            int index = 0;
            int size = messages.size();
            while (index < size) {
                Message message = messages.get(index);
                if (MessageUtils.getConversationId(message).equals(getConversationId())) {
                    index++;
                    continue;
                }
                else {
                    messages.remove(index);
                    size--;
                }
            }
            if (size > 0) {
                Logger.i(TAG, "onMessageListAdded  : " + messages.size());
                
                if (getInterceptor() != null
                        && getInterceptor().onInterceptMessages(messages)) {
                    return;
                }
                
                if (getMessageServiceListener() != null) {
                    getMessageServiceListener().onMessagesAdded(messages);
                }
                
                // getPendingToAddCache().addAll(messages);
                // refreshDelayed();
            }
        }
    }
    
    @Override
    public void onMessageRemoved(Message message) {
        if (MessageUtils.getConversationId(message).equals(getConversationId())) {
            if (getMessageServiceListener() != null) {
                getMessageServiceListener().onMessageRemoved(message);
            }
        }
    }
    
    @Override
    public void onMessageStatusChanged(Message message) {
        if (MessageUtils.getConversationId(message).equals(getConversationId())) {
            if (getMessageServiceListener() != null) {
                getMessageServiceListener().onMessageStatusChanged(message);
            }
        }
    }
    
    public void markMsgAsListened(final Message message) {
        if (!message.isListened()) {
            ChatManager.getInstance().getMessageService().markMsgAsListened(message);
        }
    }
    
    @Override
    public void initialize() {
        setInitialized(true);
        ChatManager.getInstance().getMessageService().addMessageListener(this);
    }
    
    @Override
    public void destroy() {
        setDestroyed(true);
        if (getLimitingNumberTask() != null) {
            getLimitingNumberTask().cancel();
        }
        setMessageServiceListener(null);
        ChatManager.getInstance().getMessageService().removeMessageListener(this);
        removeDelayRefreshMessage();
    }
    
    private void removeDelayRefreshMessage() {
        getHandler().removeMessages(MSG_DELAY_REFRESH);
    }
    
    private void refreshDelayed() {
        if (!getHandler().hasMessages(MSG_DELAY_REFRESH)) {
            getHandler().sendEmptyMessageDelayed(MSG_DELAY_REFRESH, REFRESH_DELAY_MILLIS);
        }
    }
    
    private class CacheHandler extends Handler {
        
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case MSG_DELAY_REFRESH:
                    consumeCachedMessages();
                    break;
                default:
                    break;
            }
        }
    }
    
}
