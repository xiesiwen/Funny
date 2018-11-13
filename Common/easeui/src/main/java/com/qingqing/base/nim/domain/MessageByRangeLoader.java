package com.qingqing.base.nim.domain;

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
public class MessageByRangeLoader extends MessageServerLoader {
    
    private String from;
    private int chatType;
    private int count;
    private int startIndex;

    MessageByRangeLoader(Message message, int count) {
        this.from = MessageUtils.getConversationId(message);
        this.chatType = chatTypeToInteger(message.getChatType());
        this.startIndex = message.getIndex();
        this.count = count;
    }
    
    MessageByRangeLoader(String from, ChatType chatType, int startIndex, int count) {
        this.from = from;
        this.chatType = chatTypeToInteger(chatType);
        this.startIndex = startIndex;
        this.count = count;
    }
    
    private String getFrom() {
        return from;
    }
    
    private int getChatType() {
        return chatType;
    }
    
    private int getCount() {
        return count;
    }
    
    private int getStartIndex() {
        return startIndex;
    }
    
    @Override
    void loadMessages(final Observer<List<Message>> callback) {
        com.qingqing.api.push.proto.v1.ChatRoom.PullChatRoomMsgByRangeRequest request = new com.qingqing.api.push.proto.v1.ChatRoom.PullChatRoomMsgByRangeRequest();
        request.from = getFrom();
        request.chatType = getChatType();
        request.hasChatType = true;
        request.count = getCount();
        request.startIndex = getStartIndex();
        new ProtoReq(CommonUrl.IM_PULL_MSG_BY_RANGE_URL.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(
                        com.qingqing.api.push.proto.v1.ChatRoom.ChatRoomHistoryMsgListResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        com.qingqing.api.push.proto.v1.ChatRoom.ChatRoomHistoryMsgListResponse response = (com.qingqing.api.push.proto.v1.ChatRoom.ChatRoomHistoryMsgListResponse) result;
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
                }).req();
    }
}
