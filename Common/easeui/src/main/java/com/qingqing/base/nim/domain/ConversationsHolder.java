package com.qingqing.base.nim.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangming on 2016/8/19.
 *
 * Information Holder 会话持有者
 *
 * 操作数据库(待加入)
 */
final class ConversationsHolder {
    
    private Map<String, Conversation> conversations = Collections
            .synchronizedMap(new HashMap<String, Conversation>());
    
    ConversationsHolder() {}
    
    Map<String, Conversation> getConversations() {
        return conversations;
    }
    
    boolean containsConversation(Conversation conversation) {
        return getConversations().containsKey(conversation.getId());
    }
    
    boolean containsConversation(String conversationId) {
        return getConversations().containsKey(conversationId);
    }
    
    Conversation getConversation(String conversationId) {
        return getConversations().get(conversationId);
    }
    
    void addConversation(Conversation conversation) {
        getConversations().put(conversation.getId(), conversation);
    }
    
    void removeConversation(Conversation conversation) {
        if (conversation != null) {
            conversation.removeAllMessages();
        }
        getConversations().remove(conversation.getId());
    }
    
    void removeConversation(String conversationId) {
        Conversation conversation = getConversation(conversationId);
        if (conversation != null) {
            conversation.removeAllMessages();
        }
        getConversations().remove(conversation.getId());
    }
    
}
