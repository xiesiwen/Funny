package com.qingqing.base.nim.domain;

import com.qingqing.base.nim.exception.ChatRoomJoinError;
import com.qingqing.base.nim.exception.ChatRoomLeaveError;
import com.qingqing.base.nim.domain.services.ChatRoomService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/8/17.
 *
 * 聊天室的加入和离开
 *
 * 聊天室的成员信息
 *
 */
class ChatRoomManager implements ChatRoomService {
    
    private ChatRoomsHolder holder = new ChatRoomsHolder();
    
    ChatRoomManager() {}
    
    private ChatRoomsHolder getHolder() {
        return holder;
    }
    
    @Override
    public void joinChatRoom(String chatRoomId, Callback callback) {
        callback.onError(new ChatRoomJoinError(chatRoomId + " chat room join error"));
    }
    
    @Override
    public void leaveChatRoom(String chatRoomId, final Callback callback) {
        callback.onError(new ChatRoomLeaveError(chatRoomId + " chat room leave Error"));
    }
    
    @Override
    public List<ChatRoom> getAllChatRooms() {
        return new ArrayList<>(getHolder().getChatRooms().values());
    }
    
    @Override
    public ChatRoom getChatRoom(String chatRoomId) {
        return getHolder().getChatRoom(chatRoomId);
    }
    
    @Override
    public void deleteChatRoom(String chatRoomId, Callback callback) {}
    
    ChatRoom getAndAddChatRoom(String chatRoomId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        if (chatRoom == null) {
            chatRoom = new ChatRoom(chatRoomId);
            getHolder().addChatRoom(chatRoom);
        }
        return chatRoom;
    }
    
    void removeChatRoom(ChatRoom chatRoom) {
        getHolder().removeChatRoom(chatRoom);
    }
    
    void removeChatRoom(String chatRoomId) {
        getHolder().removeChatRoom(chatRoomId);
    }
}
