package com.qingqing.base.nim.view;

import android.content.Context;

import com.qingqing.base.nim.cmd.CmdUtils;
import com.qingqing.base.nim.domain.CmdMessageBody;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.utils.MessageUtils;

import org.jivesoftware.smack.packet.Packet;

/**
 * Created by huangming on 2016/8/18.
 *
 * 默认ChatRow
 */
public class ChatRowDefaultProvider implements ChatRowProvider {
    private static final int MSG_TYPE_UNKNOWN = 0;
    private static final int MSG_TYPE_RECEIVED_TEXT = 1;
    private static final int MSG_TYPE_SENT_TEXT = 2;
    private static final int MSG_TYPE_RECEIVED_IMAGE = 3;
    private static final int MSG_TYPE_SENT_IMAGE = 4;
    private static final int MSG_TYPE_RECEIVED_AUDIO = 5;
    private static final int MSG_TYPE_SENT_AUDIO = 6;
    
    private static final int MSG_TYPE_CMD_SHOW_AS_TEXT = 7;
    
    @Override
    public ChatRowView getChatRowView(Context context, Message message) {
        switch (message.getMsgType()) {
            case IMAGE:
                return new ChatRowImageView(context, message);
            case AUDIO:
                return new ChatRowAudioView(context, message);
            case TEXT:
                return new ChatRowTextView(context, message);
            case CMD:
                return getChatRowCmdView(context, message);
            default:
                return new ChatRowUnKnownView(context, message);
            
        }
    }
    
    @Override
    public int getChatRowTypeCount() {
        return 8;
    }
    
    @Override
    public int getChatRowViewType(Message message) {
        if (message == null || message.getChatType() == null) {
            return MSG_TYPE_UNKNOWN;
        }
        boolean isSendDirect = MessageUtils.isSendDirect(message);
        switch (message.getMsgType()) {
            case IMAGE:
                return isSendDirect ? MSG_TYPE_SENT_IMAGE : MSG_TYPE_RECEIVED_IMAGE;
            case AUDIO:
                return isSendDirect ? MSG_TYPE_SENT_AUDIO : MSG_TYPE_RECEIVED_AUDIO;
            case TEXT:
                return isSendDirect ? MSG_TYPE_SENT_TEXT : MSG_TYPE_RECEIVED_TEXT;
            case CMD:
                return getChatRowCmdViewType(message);
            default:
                return MSG_TYPE_UNKNOWN;
            
        }
    }
    
    private ChatRowView getChatRowCmdView(Context context, Message message) {
        CmdMessageBody body = (CmdMessageBody) message.getBody();
        int cmdType = body.getType();
        if (CmdUtils.isCmdShowAsText(cmdType)) {
            return new ChatRowCmdTextView(context, message);
        }
        return new ChatRowUnKnownView(context, message);
    }
    
    private int getChatRowCmdViewType(Message message) {
        CmdMessageBody body = (CmdMessageBody) message.getBody();
        int cmdType = body.getType();
        if (CmdUtils.isCmdShowAsText(cmdType)) {
            return MSG_TYPE_CMD_SHOW_AS_TEXT;
        }
        return MSG_TYPE_UNKNOWN;
    }
}
