package com.qingqing.base.nim.ui.lecture;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.model.EaseBigImage;
import com.easemob.easeui.widget.EaseChatMessageList;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.easemob.easeui.widget.chatrow.EaseChatRowBigExpression;
import com.easemob.easeui.widget.chatrow.EaseChatRowFile;
import com.easemob.easeui.widget.chatrow.EaseChatRowImage;
import com.easemob.easeui.widget.chatrow.EaseChatRowLocation;
import com.easemob.easeui.widget.chatrow.EaseChatRowText;
import com.easemob.easeui.widget.chatrow.EaseChatRowVideo;
import com.qingqing.base.activity.ImageShowActivity;
import com.qingqing.base.bean.ImageGroup;
import com.qingqing.base.bean.MultiMediaFactory;
import com.qingqing.base.bean.MultiMediaItem;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.im.widget.ChatRowCmdBindTA;
import com.qingqing.base.im.widget.ChatRowCmdConsult;
import com.qingqing.base.im.widget.ChatRowCmdGroupMsg;
import com.qingqing.base.im.widget.ChatRowCmdLectureMsg;
import com.qingqing.base.im.widget.ChatRowCmdNone;
import com.qingqing.base.im.widget.ChatRowCmdRecTeacher;
import com.qingqing.base.im.widget.ChatRowCmdText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 适配聊天历史的adaper，根据EaseMessageAdapter改造而来，主要是改动了数据来源
 *
 * Created by tanwei on 2016/6/1.
 */
public class LectureHistoryAdapter extends BaseAdapter {
    
    private Context context;
    
    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_EXPRESSION = 12;
    private static final int MESSAGE_TYPE_RECV_EXPRESSION = 13;
    
    private List<EMMessage> messages;
    
    private EaseChatMessageList.MessageListItemClickListener itemClickListener;
    
    private int mCurrentPlayPosition = -1;// 当前播放的消息position
    
    public LectureHistoryAdapter(Context context) {
        this.context = context;
        messages = new ArrayList<>();
    }
    
    /** 获取当前正在播放的语音消息位置,未播放返回-1 */
    public int getCurPlayPosition() {
        return mCurrentPlayPosition;
    }
    
    /** 设置当前正在播放的语音消息位置 */
    public void setCurPlayPosition(int position) {
        mCurrentPlayPosition = position;
    }
    
    public void resetData(List<EMMessage> list) {
        if (messages.size() > 0) {
            messages.clear();
        }
        
        messages.addAll(list);
        notifyDataSetChanged();
    }
    
    public void clearData() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    public EMMessage getItem(int position) {
        if (messages != null && position < messages.size()) {
            return messages.get(position);
        }
        return null;
    }
    
    public long getItemId(int position) {
        return position;
    }
    
    /**
     * 获取item数
     */
    public int getCount() {
        return messages.size();
    }
    
    /**
     * 获取item类型数
     */
    public int getViewTypeCount() {
        return 14;
    }
    
    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        EMMessage message = getItem(position);
        if (message == null) {
            return -1;
        }
        
        if (message.getType() == EMMessage.Type.TXT) {
            if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION,
                    false)) {
                return message.direct == EMMessage.Direct.RECEIVE
                        ? MESSAGE_TYPE_RECV_EXPRESSION
                        : MESSAGE_TYPE_SENT_EXPRESSION;
            }
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT
                    : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getType() == EMMessage.Type.IMAGE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE
                    : MESSAGE_TYPE_SENT_IMAGE;
            
        }
        if (message.getType() == EMMessage.Type.LOCATION) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION
                    : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getType() == EMMessage.Type.VOICE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE
                    : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getType() == EMMessage.Type.VIDEO) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO
                    : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getType() == EMMessage.Type.FILE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_FILE
                    : MESSAGE_TYPE_SENT_FILE;
        }
        
        return -1;// invalid
    }
    
    protected EaseChatRow createChatRow(Context context, EMMessage message,
            int position) {
        EaseChatRow chatRow = null;
        switch (message.getType()) {
            case TXT:
                if (message.getBooleanAttribute(
                        EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                    chatRow = new EaseChatRowBigExpression(context, message, position,
                            this);
                }
                else {
                    chatRow = new EaseChatRowText(context, message, position, this);
                }
                break;
            case LOCATION:
                chatRow = new EaseChatRowLocation(context, message, position, this);
                break;
            case FILE:
                chatRow = new EaseChatRowFile(context, message, position, this);
                break;
            case IMAGE:
                chatRow = new EaseChatRowImage(context, message, position, this)
                        .setShowImageListener(
                                new EaseChatRowImage.EaseChatRowImageListener() {
                                    @Override
                                    public void showImage(EMMessage message) {
                                        showAllImage(message);
                                    }
                                });
                break;
            case VOICE:
                chatRow = new CustomChatRowVoice(context, message, position, this);
                break;
            case VIDEO:
                chatRow = new EaseChatRowVideo(context, message, position, this);
                break;
            case CMD:
                CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
                if (cmdMsg != null) {
                    if (cmdMsg.isGroupCmdMsg()) {
                        chatRow = new ChatRowCmdGroupMsg(context, message, position,
                                this);
                        break;
                    }
                    if (cmdMsg.isLectureCmdMsg()) {
                        chatRow = new ChatRowCmdLectureMsg(context, message, position,
                                this);
                        break;
                    }
                    switch (cmdMsg.msgType) {
                        case CmdMsg.CMD_TYPE_TEXT:
                            chatRow = new ChatRowCmdText(context, message, position,
                                    this);
                            break;
                        case CmdMsg.CMD_TYPE_CONSULT_ST:
                        case CmdMsg.CMD_TYPE_CONSULT_TA:
                            chatRow = new ChatRowCmdConsult(context, message, position,
                                    this);
                            break;
                        case CmdMsg.CMD_TYPE_REC_TEACHER:
                            chatRow = new ChatRowCmdRecTeacher(context, message, position,
                                    this);
                            break;
                        case CmdMsg.CMD_TYPE_ST_BIND_TA:
                            chatRow = new ChatRowCmdBindTA(context, message, position,
                                    this);
                            break;
                        // 防止错误的cmd message type
                        default:
                            chatRow = new ChatRowCmdNone(context, message, position,
                                    this);
                            break;
                    }
                }
                break;
            default:
                break;
        }
        
        return chatRow;
    }
    
    private void showAllImage(EMMessage message) {
        ArrayList<EaseBigImage> imageList = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getType() == EMMessage.Type.IMAGE) {
                EaseBigImage image;
                ImageMessageBody imgBody = (ImageMessageBody) messages.get(i).getBody();
                File file = new File(imgBody.getLocalUrl());
                if (file.exists()) {
                    Uri uri = Uri.fromFile(file);
                    image = new EaseBigImage(messages.get(i).getMsgId(), uri, null, null,
                            0);
                }
                else {
                    // The local full size pic does not exist yet.
                    // ShowBigImage needs to download it from the server
                    // first
                    image = new EaseBigImage(messages.get(i).getMsgId(), null,
                            imgBody.getSecret(), imgBody.getRemoteUrl(), 0);
                }
                
                if (messages.get(i).getMsgId().equals(message.getMsgId())) {
                    index = imageList.size();
                }
                imageList.add(image);
            }
        }
        
        ArrayList<MultiMediaItem> list = new ArrayList<>(imageList.size());
        for (EaseBigImage easeBigImage : imageList) {
            list.add(MultiMediaFactory.createImage(IMUtils.getImageUri(easeBigImage)));
        }
        Intent intent = new Intent(context, ImageShowActivity.class);
        intent.putExtra(ImageShowActivity.KEY_IMG_GROUP, new ImageGroup(list));
        intent.putExtra(ImageShowActivity.KEY_IDX_IN_GROUP, index);
        intent.putExtra(ImageShowActivity.PARAM_BOOLEAN_SUPPORT_SAVE, true);
        context.startActivity(intent);
    }
    
    public View getView(final int position, View convertView, ViewGroup parent) {
        EMMessage message = getItem(position);
        if (convertView == null) {
            convertView = createChatRow(context, message, position);
        }
        // 缓存的view的message很可能不是当前item的，传入当前message和position更新ui
        ((EaseChatRow) convertView).setUpView(message, position, itemClickListener);
        
        return convertView;
    }
    
    public void setItemClickListener(
            EaseChatMessageList.MessageListItemClickListener listener) {
        itemClickListener = listener;
    }
}
