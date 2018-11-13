package com.qingqing.base.nim.domain;

import com.qingqing.api.push.proto.v1.ChatRoom;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.nim.exception.NetworkError;
import com.qingqing.base.nim.utils.MessageUtils;

import java.util.List;

/**
 * Created by huangming on 2016/8/22.
 */
class MessageByIndexLoader extends MessageServerLoader {
    
    private String from;
    private int chatType;
    private int[] indexArray;
    
    MessageByIndexLoader(Message message, int[] indexArray) {
        this.from = MessageUtils.getConversationId(message);
        this.chatType = chatTypeToInteger(message.getChatType());
        this.indexArray = indexArray;
    }
    
    MessageByIndexLoader(String from, ChatType chatType, int[] indexArray) {
        this.from = from;
        this.chatType = chatTypeToInteger(chatType);
        this.indexArray = indexArray;
    }
    
    private int getChatType() {
        return chatType;
    }
    
    private String getFrom() {
        return from;
    }
    
    private int[] getIndexArray() {
        return indexArray;
    }
    
    @Override
    void loadMessages(final Observer<List<Message>> callback) {
        ChatRoom.PullChatRoomMsgByIndexIdsRequest request = new ChatRoom.PullChatRoomMsgByIndexIdsRequest();
        request.from = getFrom();
        request.chatType = getChatType();
        request.hasChatType = true;
        request.messageIndexs = getIndexArray();
        new ProtoReq(CommonUrl.IM_PULL_MSG_BY_INDEX_URL.url()).setSendMsg(request)
                .setRspListener(
                        new ProtoListener(ChatRoom.ChatRoomHistoryMsgListResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                ChatRoom.ChatRoomHistoryMsgListResponse response = (ChatRoom.ChatRoomHistoryMsgListResponse) result;
                                if (callback != null) {
                                    callback.onNext(processResponse(response));
                                    callback.onCompleted();
                                }
                            }
                            
                            @Override
                            public void onDealError(HttpError error, boolean isParseOK,
                                                    int errorCode, Object result) {
                                if (callback != null) {
                                    callback.onError(new NetworkError(errorCode,
                                            "loadMessagesByIndex error"));
                                }
                            }
                        })
                .req();
    }
}
