package com.qingqing.base.nim.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangming on 2016/8/22.
 */
public class ChatRoomsHolder {
    
    private Map<String, ChatRoom> chatRooms = Collections
            .synchronizedMap(new HashMap<String, ChatRoom>());
    
    ChatRoomsHolder() {}
    
    Map<String, ChatRoom> getChatRooms() {
        return chatRooms;
    }
    
    boolean containsChatRoom(ChatRoom chatRoom) {
        return getChatRooms().containsKey(chatRoom.getChatRoomId());
    }
    
    ChatRoom getChatRoom(String chatRoomId) {
        return getChatRooms().get(chatRoomId);
    }
    
    void addChatRoom(ChatRoom chatRoom) {
        getChatRooms().put(chatRoom.getChatRoomId(), chatRoom);
    }
    
    void removeChatRoom(ChatRoom chatRoom) {
        removeChatRoom(chatRoom.getChatRoomId());
    }
    
    void removeChatRoom(String chatRoomId) {
        getChatRooms().remove(chatRoomId);
    }
    
}
