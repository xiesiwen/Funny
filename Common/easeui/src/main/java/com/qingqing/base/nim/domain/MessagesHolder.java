package com.qingqing.base.nim.domain;

import com.qingqing.base.nim.utils.MessageUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangming on 2016/8/19.
 * 
 * Information Holder
 * 
 * 操作数据库(待加入)
 */
class MessagesHolder {
    
    private Map<String, Map<String, Message>> messageMap = Collections
            .synchronizedMap(new HashMap<String, Map<String, Message>>());
    
    MessagesHolder() {}
    
    boolean containsMessage(Message message) {
        String conversationId = MessageUtils.getConversationId(message);
        Map<String, Message> messages = messageMap.get(conversationId);
        return messages != null && messages.containsKey(message.getId());
    }
    
    void addMessage(Message message) {
        String conversationId = MessageUtils.getConversationId(message);
        Map<String, Message> messages = messageMap.get(conversationId);
        if (messages == null) {
            messages = Collections.synchronizedMap(new HashMap<String, Message>());
            messageMap.put(conversationId, messages);
        }
        messages.put(message.getId(), message);
    }
    
    void addMessages(List<Message> messages) {
        for (Message message : messages) {
            addMessage(message);
        }
    }
    
    void removeMessage(Message message) {
        String conversationId = MessageUtils.getConversationId(message);
        Map<String, Message> messages = messageMap.get(conversationId);
        if (messages != null) {
            messages.remove(message.getId());
        }
    }
    
    void removeMessagesBy(String conversationId) {
        Map<String, Message> messages = messageMap.get(conversationId);
        if (messages != null) {
            messageMap.remove(conversationId);
            messages.clear();
        }
        
    }
    
}
