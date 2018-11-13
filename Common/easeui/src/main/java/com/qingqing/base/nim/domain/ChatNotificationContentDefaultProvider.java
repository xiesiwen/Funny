package com.qingqing.base.nim.domain;

import android.content.Context;
import android.content.Intent;

import com.qingqing.base.nim.utils.ChatContactUtils;
import com.qingqing.base.nim.utils.MessageUtils;

/**
 * Created by huangming on 2016/8/31.
 */
public class ChatNotificationContentDefaultProvider
        implements ChatNotificationContentProvider {
    
    @Override
    public String getTitle(Context context, Message message) {
        return null;
    }
    
    @Override
    public int getSmallIcon(Context context, Message message) {
        return 0;
    }
    
    @Override
    public String getTickerText(Context context, Message message) {
        ChatType chatType = message.getChatType();
        String chatInGroup = (chatType == ChatType.Chat ? "" : "在群聊中");
        String nickName = ChatContactUtils.getNick(message);
        
        MessageBody body = message.getBody();
        if (body instanceof TextMessageBody) {
            return nickName + " : " + ((TextMessageBody) body).getText();
        }
        else if (body instanceof ImageMessageBody) {
            return nickName + chatInGroup + "发了一张图片";
        }
        else if (body instanceof AudioMessageBody) {
            return nickName + chatInGroup + "发了一段语音";
        }
        else {
            return "您有一条新消息";
        }
    }
    
    @Override
    public String getContentText(Context context, Message message, int userCount,
            int messageCount) {
        String currentMsg = getTickerText(context, message);
        return currentMsg + "  (还有" + userCount + "个联系人的" + messageCount + "条未读消息)";
    }
    
    @Override
    public Intent getContentIntent(Context context, Message message) {
        return null;
    }
    
    @Override
    public boolean isNotifyAllowed(Context context, Message message) {
        Message.Type msgType = message.getMsgType();
        return (!message.hasAttribute("em_ignore_notification")
                || !message.getBooleanAttribute("em_ignore_notification"))
                && msgType != Message.Type.CMD && msgType != null
                && !MessageUtils.isSendDirect(message);
    }
}
