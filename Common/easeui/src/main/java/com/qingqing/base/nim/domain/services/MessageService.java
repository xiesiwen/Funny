package com.qingqing.base.nim.domain.services;

import com.qingqing.base.nim.domain.Callback;
import com.qingqing.base.nim.domain.Message;

import java.util.List;

/**
 * Created by huangming on 2016/8/17.
 */
public interface MessageService {
    
    void deleteMessage(Message message, Callback callback);
    
    void sendMessage(Message message, Callback callback);
    
    void resendMessage(Message message, Callback callback);
    
    void markMsgAsListened(Message message);
    
    void loadHistoryMessages(String conversationId, int msgCount, Callback callback);
    
    void removeMessageListener(Listener listener);
    
    void addMessageListener(Listener listener);

    void loadMessageBody(Message message);
    
    public interface Listener {
        
        void onMessageAdded(Message message);
        
        void onMessagesAdded(List<Message> messages);
        
        void onMessageRemoved(Message message);
        
        void onMessageStatusChanged(Message message);
        
    }
    
}
