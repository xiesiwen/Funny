package com.qingqing.base.nim.domain;

import com.qingqing.base.nim.utils.BitmapUtils;
import com.qingqing.base.time.NetworkTime;

import java.io.File;
import java.util.UUID;

/**
 * Created by huangming on 2016/8/17.
 */
public class MessageFactory {
    
    private MessageFactory() {}
    
    private static Message createSendMessage(String conversationId, ChatType chatType,
            Message.Type msgType) {
        Message message = new Message(UUID.randomUUID().toString(), chatType, msgType);
        ChatContact from = new ChatContact(ChatManager.getInstance().getCurrentUserId());
        message.setFromContact(from);
        ChatContact to = new ChatContact(conversationId);
        message.setToContact(to);
        message.setDirect(Message.Direct.SEND);
        message.setListened(true);
        message.setStatus(Message.Status.CREATE);
        message.setUnread(false);
        message.setMsgTime(NetworkTime.currentTimeMillis());
        return message;
    }
    
    public static Message createTextMessage(String conversationId, ChatType chatType,
            String msgText) {
        Message message = createSendMessage(conversationId, chatType, Message.Type.TEXT);
        TextMessageBody body = new TextMessageBody(msgText);
        message.setBody(body);
        return message;
    }
    
    public static Message createImageMessage(String conversationId, ChatType chatType,
            String imageFilePath) {
        Message message = createSendMessage(conversationId, chatType, Message.Type.IMAGE);
        ImageMessageBody body = new ImageMessageBody(new File(imageFilePath));
        body.setStatus(Message.Status.CREATE);
        int[] imgSize = BitmapUtils.getBitmapSize(imageFilePath);
        body.setWidth(imgSize[0]);
        body.setHeight(imgSize[1]);
        message.setBody(body);
        return message;
    }
    
    public static Message createAudioMessage(String conversationId, ChatType chatType,
            String audioFilePath, int length) {
        Message message = createSendMessage(conversationId, chatType, Message.Type.AUDIO);
        AudioMessageBody body = new AudioMessageBody(new File(audioFilePath), length);
        body.setStatus(Message.Status.CREATE);
        message.setBody(body);
        return message;
    }
    
}
