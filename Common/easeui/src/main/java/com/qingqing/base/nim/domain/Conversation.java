package com.qingqing.base.nim.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by huangming on 2016/8/17.
 */
public class Conversation {
    
    private final String id;
    private final ChatType chatType;

    private int startIndex;
    
    private List<Message> messages = Collections
            .synchronizedList(new ArrayList<Message>());
    
    Conversation(String id, ChatType chatType) {
        this.id = id;
        this.chatType = chatType;
    }
    
    public String getId() {
        return id;
    }
    
    public ChatType getChatType() {
        return chatType;
    }
    
    List<Message> getMessages() {
        return messages;
    }
    
    void addMessage(Message message) {
        getMessages().add(message);
    }
    
    void addMessages(List<Message> messages) {
        getMessages().addAll(messages);
    }
    
    void removeMessage(Message message) {
        getMessages().remove(message);
    }

    void removeAllMessages() {
        getMessages().clear();
    }

    public List<Message> getAllMessages() {
        return messages;
    }

    void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }
}
