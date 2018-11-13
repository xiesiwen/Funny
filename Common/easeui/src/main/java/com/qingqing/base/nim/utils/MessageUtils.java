package com.qingqing.base.nim.utils;

import android.text.TextUtils;

import com.qingqing.base.nim.domain.AudioMessageBody;
import com.qingqing.base.nim.domain.ChatType;
import com.qingqing.base.nim.domain.Message;

import org.jivesoftware.smack.packet.Packet;

/**
 * Created by huangming on 2016/8/19.
 */
public class MessageUtils {
    
    private MessageUtils() {
        
    }
    
    public static String getConversationId(Message message) {
        if (message.getChatType() != ChatType.Chat) {
            return message.getTo();
        }
        else {
            return isSendDirect(message) ? message.getTo() : message.getFrom();
        }
    }
    
    public static boolean isSendDirect(Message message) {
        return message.getDirect() != Message.Direct.RECEIVE;
    }
    
    public static boolean isBodyLoaded(Message message) {
        if (message.getBody() instanceof AudioMessageBody) {
            AudioMessageBody body = (AudioMessageBody) message.getBody();
            if (isSendDirect(message) && !TextUtils.isEmpty(body.getLocalUrl())) {
                return true;
            }
            return body.getStatus() == Message.Status.SUCCESS;
        }
        return true;
    }
    
    public static boolean isBodyLoading(Message message) {
        if (message.getBody() instanceof AudioMessageBody) {
            AudioMessageBody body = (AudioMessageBody) message.getBody();
            if (isSendDirect(message) && !TextUtils.isEmpty(body.getLocalUrl())) {
                return false;
            }
            return body.getStatus() == Message.Status.IN_PROGRESS;
        }
        return false;
    }
    
    public static boolean isBodyLoadFailed(Message message) {
        if (message.getBody() instanceof AudioMessageBody) {
            AudioMessageBody body = (AudioMessageBody) message.getBody();
            if (isSendDirect(message) && !TextUtils.isEmpty(body.getLocalUrl())) {
                return false;
            }
            return body.getStatus() == Message.Status.FAIL;
        }
        return false;
    }
    
    public static boolean isBodyNeededToLoad(Message message) {
        if (message.getBody() instanceof AudioMessageBody) {
            AudioMessageBody body = (AudioMessageBody) message.getBody();
            if (isSendDirect(message) && !TextUtils.isEmpty(body.getLocalUrl())) {
                return false;
            }
            return body.getStatus() != Message.Status.SUCCESS
                    && body.getStatus() != Message.Status.IN_PROGRESS;
        }
        return false;
    }
    
    public static boolean isAudioMessage(Message message) {
        return message.getMsgType() == Message.Type.AUDIO;
    }
    
}
