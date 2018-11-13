package com.qingqing.base.nim.domain;

import com.qingqing.base.nim.domain.services.ConversationService;
import com.qingqing.base.nim.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/8/19.
 *
 * 核心模型：会话管理
 *
 * 职责：创建， 添加，删除会话，同时会话操作数据库,
 */
class ConversationManager implements ConversationService {
    
    private final ConversationsHolder holder = new ConversationsHolder();
    
    ConversationManager() {}
    
    private ConversationsHolder getHolder() {
        return holder;
    }
    
    private Conversation createConversationBy(Message message) {
        return createConversation(MessageUtils.getConversationId(message),
                message.getChatType());
    }
    
    private Conversation createConversation(String conversationId, ChatType chatType) {
        return new Conversation(conversationId, chatType);
    }
    
    Conversation getAndAddConversationBy(Message message) {
        String conversationId = MessageUtils.getConversationId(message);
        Conversation conversation = getConversation(conversationId);
        if (conversation == null) {
            conversation = createConversationBy(message);
            addConversation(conversation);
        }
        return conversation;
    }
    
    Conversation getConversationBy(Message message) {
        String conversationId = MessageUtils.getConversationId(message);
        return getConversation(conversationId);
    }
    
    void addMessage(Message message) {
        Conversation conversation = getAndAddConversationBy(message);
        conversation.addMessage(message);
    }
    
    void addMessages(List<Message> messages) {
        for (Message message : messages) {
            Conversation conversation = getAndAddConversationBy(message);
            conversation.addMessage(message);
        }
    }
    
    void removeMessage(Message message) {
        Conversation conversation = getConversationBy(message);
        if (conversation != null) {
            conversation.removeMessage(message);
        }
    }
    
    @Override
    public void deleteConversation(String conversationId, Callback callback) {
        
    }
    
    @Override
    public Conversation getConversation(String conversationId) {
        return getHolder().getConversation(conversationId);
    }
    
    void addConversation(Conversation conversation) {
        if (!getHolder().containsConversation(conversation)) {
            getHolder().addConversation(conversation);
        }
    }

    void removeConversation(String conversationId) {
        if(getHolder().containsConversation(conversationId)) {
            getHolder().removeConversation(conversationId);
        }
    }
    
    @Override
    public List<Conversation> getAllConversations() {
        return new ArrayList<>(getHolder().getConversations().values());
    }
    
    @Override
    public List<Message> getConversationMessages(String conversationId) {
        List<Message> messages = new ArrayList<>();
        Conversation conversation = getConversation(conversationId);
        if (conversation != null) {
            messages.addAll(conversation.getAllMessages());
        }
        return messages;
    }
    
}
