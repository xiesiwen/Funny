package com.qingqing.base.nim.domain.services;

import com.qingqing.base.nim.domain.Callback;
import com.qingqing.base.nim.domain.ChatRoom;

import java.util.List;

/**
 * Created by huangming on 2016/8/22.
 */
public interface ChatRoomService {
    
    void joinChatRoom(String chatRoomId, Callback callback);

    void leaveChatRoom(String chatRoomId, Callback callback);
    
    List<ChatRoom> getAllChatRooms();
    
    ChatRoom getChatRoom(String chatRoomId);
    
    void deleteChatRoom(String chatRoomId, Callback callback);
    
}
