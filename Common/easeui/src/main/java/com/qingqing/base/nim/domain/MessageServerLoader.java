package com.qingqing.base.nim.domain;

import com.qingqing.api.push.proto.v1.ChatRoom;
import com.qingqing.base.interfaces.Observer;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/8/22.
 */
abstract class MessageServerLoader {
    
    abstract void loadMessages(Observer<List<Message>> callback);
    
    protected int chatTypeToInteger(ChatType chatType) {
        switch (chatType) {
            case Chat:
                return com.qingqing.api.push.proto.v1.ChatRoom.ChatType.chat;
            case ChatRoom:
                return com.qingqing.api.push.proto.v1.ChatRoom.ChatType.chatroom;
            case GroupChat:
                return com.qingqing.api.push.proto.v1.ChatRoom.ChatType.groupchat;
            default:
                return ChatRoom.ChatType.chat;
        }
    }
    
    protected List<Message> processResponse(
            com.qingqing.api.push.proto.v1.ChatRoom.ChatRoomHistoryMsgListResponse response) {
        com.qingqing.api.push.proto.v1.ChatRoom.ChatRoomMessageInfo[] chatRoomMessageInfoList = response.msgInfos;
        List<Message> messageList = new ArrayList<>(
                chatRoomMessageInfoList != null ? chatRoomMessageInfoList.length : 0);
        for (com.qingqing.api.push.proto.v1.ChatRoom.ChatRoomMessageInfo info : chatRoomMessageInfoList) {
            try {
                Message message = MessageParser.getMessageBy(info.messageInfo, info.uuid);
                if (message != null) {
                    message.setIndex((int) info.messageIndex);
                    messageList.add(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return messageList;
    }
    
}
