package com.qingqing.base.nim.domain.services;

import com.qingqing.base.nim.domain.Callback;
import com.qingqing.base.nim.domain.Conversation;
import com.qingqing.base.nim.domain.Message;

import java.util.List;

/**
 * Created by huangming on 2016/8/17.
 */
public interface ConversationService {
    
    void deleteConversation(String conversationId, Callback callback);
    
    Conversation getConversation(String conversationId);
    
    List<Conversation> getAllConversations();

    List<Message> getConversationMessages(String conversationId);
    
}
