package com.qingqing.base.nim.domain;

import com.qingqing.api.push.proto.v1.ChatRoom;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.exception.SendError;

import org.json.JSONException;

/**
 * Created by huangming on 2016/8/17.
 */
class ChatRoomMessageSender extends AbstractMessageSender {
    
    private static final String TAG = "WhuthmChatRoomMessageSender";
    
    ChatRoomMessageSender(MessageManager messageManager) {
        super(messageManager);
    }
    
    @Override
    protected void onSend(final Message message, final Callback callback) {
        getMessageManager().setMsgStatus(message, Message.Status.IN_PROGRESS);
        ChatRoom.PushChatRoomMsgRequest request = new ChatRoom.PushChatRoomMsgRequest();
        request.from = message.getFrom();
        request.to = message.getTo();
        request.uuid = message.getId();
        try {
            request.messageInfo = MessageParser.getSendBodyBy(message);
            request.messageExt = MessageParser.getSendExtBy(message);
        } catch (JSONException e) {
            e.printStackTrace();
            getMessageManager().setMsgStatus(message, Message.Status.FAIL);
            return;
        }
        new ProtoReq(CommonUrl.IM_CHAT_ROOM_MSG_SEND_URL.url()).setSendMsg(request)
                .setRspListener(
                        new ProtoListener(ChatRoom.PushChatRoomMsgResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                ChatRoom.PushChatRoomMsgResponse response = (ChatRoom.PushChatRoomMsgResponse) result;
                                // TODO 这里的类型强转是否安全
                                message.setIndex((int) response.messageIndex);
                                Logger.i(TAG, "Send success : " + message.getId()
                                        + ", index" + message.getIndex());
                                getMessageManager().setMsgStatus(message,
                                        Message.Status.SUCCESS);
                                if (callback != null) {
                                    callback.onCompleted();
                                }
                            }
                            
                            @Override
                            public void onDealError(HttpError error, boolean isParseOK,
                                                    int errorCode, Object result) {
                                super.onDealError(error, isParseOK, errorCode, result);
                                Logger.e(TAG, "send message error : " + errorCode + ", "
                                        + message.getId());
                                getMessageManager().setMsgStatus(message,
                                        Message.Status.FAIL);
                                if (callback != null) {
                                    callback.onError(new SendError(
                                            "ChatRoomMessageSender failed"));
                                }
                            }
                        })
                .req();
    }
}
