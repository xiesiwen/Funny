package com.qingqing.base.nim.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/8/22.
 *
 * 聊天室中只有加入后才能聊天
 */
public class ChatRoom {
    
    private String chatRoomId;
    private List<String> members = new ArrayList<>();
    private String name;
    
    public ChatRoom(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
    
    public String getChatRoomId() {
        return chatRoomId;
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    public List<String> getMembers() {
        return members;
    }

    void join(String userId) {
        members.add(userId);
    }

    void leave(String userId) {
        members.remove(userId);
    }

    public String getName() {
        return name;
    }
}
