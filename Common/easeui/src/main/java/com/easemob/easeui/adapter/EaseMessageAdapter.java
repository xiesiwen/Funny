/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.easeui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.model.EaseBigImage;
import com.easemob.easeui.widget.EaseChatMessageList.MessageListItemClickListener;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.easemob.easeui.widget.chatrow.EaseChatRowBigExpression;
import com.easemob.easeui.widget.chatrow.EaseChatRowFile;
import com.easemob.easeui.widget.chatrow.EaseChatRowImage;
import com.easemob.easeui.widget.chatrow.EaseChatRowLocation;
import com.easemob.easeui.widget.chatrow.EaseChatRowText;
import com.easemob.easeui.widget.chatrow.EaseChatRowVideo;
import com.easemob.easeui.widget.chatrow.EaseChatRowVoice;
import com.easemob.easeui.widget.chatrow.EaseCustomChatRowProvider;
import com.qingqing.base.activity.ImageShowActivity;
import com.qingqing.base.bean.ImageGroup;
import com.qingqing.base.bean.MultiMediaFactory;
import com.qingqing.base.bean.MultiMediaItem;
import com.qingqing.base.im.AudioBatchProcesser;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.MessageComparator;
import com.qingqing.base.im.domain.ChatMessage;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.im.widget.ChatRowCmdBindTA;
import com.qingqing.base.im.widget.ChatRowCmdConsult;
import com.qingqing.base.im.widget.ChatRowCmdCourseReport;
import com.qingqing.base.im.widget.ChatRowCmdGroupMsg;
import com.qingqing.base.im.widget.ChatRowCmdLectureMsg;
import com.qingqing.base.im.widget.ChatRowCmdNone;
import com.qingqing.base.im.widget.ChatRowCmdOverdueApplyCancelCourse;
import com.qingqing.base.im.widget.ChatRowCmdRank;
import com.qingqing.base.im.widget.ChatRowCmdRecTeacher;
import com.qingqing.base.im.widget.ChatRowCmdRemindST;
import com.qingqing.base.im.widget.ChatRowCmdSharePlanSummarize;
import com.qingqing.base.im.widget.ChatRowCmdText;
import com.qingqing.base.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.easemob.chat.EMMessage.Type.IMAGE;

public class EaseMessageAdapter extends BaseAdapter {
    
    private final static String TAG = "EaseMessageAdapter";
    
    private Context context;
    
    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
    private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
    private static final int HANDLER_MESSAGE_SEEK_TO = 2;
    private static final int HANDLER_MESSAGE_FORCE_SELECT_LAST = 3;
    private static final int HANDLER_MESSAGE_SEEK_HISTORY = 4;
    
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
    
    // reference to conversation object in chatsdk
    private EMConversation conversation;
    List<EMMessage> messages = new ArrayList<EMMessage>();
    
    private int[] filterRoleTypes;
    
    private String toChatUsername;
    
    private MessageListItemClickListener itemClickListener;
    private EaseCustomChatRowProvider customRowProvider;
    
    private boolean showUserNick;
    private boolean showAvatar;
    private Drawable myBubbleBg;
    private Drawable otherBuddleBg;
    
    private ListView listView;
    
    public EaseMessageAdapter(Context context, String username, int chatType,
            ListView listView) {
        this.context = context;
        this.listView = listView;
        toChatUsername = username;
        this.conversation = EMChatManager.getInstance().getConversation(username);
    }
    
    public List<EMMessage> getMessages() {
        return messages;
    }
    
    Handler handler = new Handler() {
        private void refreshList() {
            messages.clear();
            // UI线程不能直接使用conversation.getAllMessages()
            // 否则在UI刷新过程中，如果收到新的消息，会导致并发问题
            List<EMMessage> messageList = conversation.getAllMessages();
            List<String> msgIds = new ArrayList<String>();
            if (messageList != null) {
                for (int i = 0; i < messageList.size(); i++) {
                    EMMessage message = messageList.get(i);
                    if (isFilter(message) && !msgIds.contains(message.getMsgId())) {
                        msgIds.add(message.getMsgId());
                        messages.add(message);
                        // 当当前用户等于getFrom和RECEIVE时，设置direct为SEND
                        if (message.getFrom()
                                .equals(ChatManager.getInstance().getCurrentUserName())
                                && message.direct == EMMessage.Direct.RECEIVE) {
                            message.direct = EMMessage.Direct.SEND;
                        }
                        if (conversation
                                .getType() == EMConversation.EMConversationType.ChatRoom) {
                            if (AudioBatchProcesser.getInstance(context)
                                    .isAudioMessageListened(message)) {
                                EMChatManager.getInstance().setMessageListened(message);
                            }
                        }
                        
                        // 回报已读状态
                        if (message.isUnread()) {
                            IMUtils.uploadIMMsgRead(message);
                        }
                    }
                    else {
                        Logger.w("EaseMessageAdapter", "refreshList Duplicate or filter");
                    }
                    
                    // 设置为unread
                    conversation.getMessage(i);
                }
            }
            Collections.sort(messages, new MessageComparator());
            notifyDataSetChanged();
        }
        
        @Override
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case HANDLER_MESSAGE_REFRESH_LIST:
                    refreshList();
                    break;
                case HANDLER_MESSAGE_SELECT_LAST:
                    if (messages.size() > 0
                            && listView.getLastVisiblePosition() >= messages.size() - 1) {
                        listView.setSelection(messages.size() - 1);
                    }
                    break;
                case HANDLER_MESSAGE_FORCE_SELECT_LAST:
                    if (messages.size() > 0) {
                        listView.setSelection(messages.size() - 1);
                    }
                    break;
                case HANDLER_MESSAGE_SEEK_TO:
                    int position = message.arg1;
                    listView.setSelection(position);
                    break;
                case HANDLER_MESSAGE_SEEK_HISTORY:
                    int historyPosition = messages.size() - message.arg1;
                    listView.smoothScrollToPosition(historyPosition);
                    
                    if (historyPosition < 0) {
                        int lackNumber = Math.abs(historyPosition);
                        // TODO: 一次加载上限？ 性能问题？
                        if (!conversation.isGroup()) {
                            conversation.loadMoreMsgFromDB(messages.get(0).getMsgId(),
                                    lackNumber);
                        }
                        else {
                            conversation.loadMoreGroupMsgFromDB(
                                    messages.get(0).getMsgId(), lackNumber);
                        }
                        seekHistoryByUnreadNumber(message.arg1);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    
    public void filterRoles(int... roleTypes) {
        this.filterRoleTypes = roleTypes;
        refresh();
    }
    
    boolean isFilter(EMMessage message) {
        if (filterRoleTypes == null || filterRoleTypes.length <= 0) {
            return true;
        }
        ChatMessage chatMessage = new ChatMessage(message);
        for (int roleType : filterRoleTypes) {
            for (int i = 0; i < chatMessage.getChatRoomType().size(); i++) {
                if (roleType == chatMessage.getChatRoomType().get(i)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 刷新页面
     */
    public void refresh() {
        if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
            return;
        }
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(msg);
    }
    
    /**
     * 刷新页面, 选择最后一个
     */
    public void refreshSelectLast() {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_SELECT_LAST));
    }
    
    /**
     * 刷新页面, 选择Position
     */
    public void refreshSeekTo(int position) {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
        msg.arg1 = position;
        handler.sendMessage(msg);
    }
    
    /**
     * 返回到指定数量的未读消息(选择 last - unreadNumber)
     */
    public void seekHistoryByUnreadNumber(int unreadNumber) {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_HISTORY);
        msg.arg1 = unreadNumber;
        handler.sendMessage(msg);
    }
    
    public void refreshForceSelectLast() {
        handler.removeMessages(HANDLER_MESSAGE_FORCE_SELECT_LAST);
        handler.removeMessages(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_FORCE_SELECT_LAST));
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
        if (customRowProvider != null
                && customRowProvider.getCustomChatRowTypeCount() > 0) {
            return customRowProvider.getCustomChatRowTypeCount() + 14;
        }
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
        
        if (customRowProvider != null
                && customRowProvider.getCustomChatRowType(message) > 0) {
            return customRowProvider.getCustomChatRowType(message) + 13;
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
        if (message.getType() == IMAGE) {
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
        if (customRowProvider != null
                && customRowProvider.getCustomChatRow(message, position, this) != null) {
            return customRowProvider.getCustomChatRow(message, position, this);
        }
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
                chatRow = new EaseChatRowVoice(context, message, position, this);
                break;
            case VIDEO:
                chatRow = new EaseChatRowVideo(context, message, position, this);
                break;
            case CMD:
                CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
                if (cmdMsg != null) {
                    // 特殊 ui 展示，需要单独处理
                    if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_COURSE_REPORT) {
                        chatRow = new ChatRowCmdCourseReport(context, message, position,
                                this);
                        break;
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_SHARE_PLAN_SUMMARIZE) {
                        chatRow = new ChatRowCmdSharePlanSummarize(context, message,
                                position, this);
                        break;
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_RANK) {
                        chatRow = new ChatRowCmdRank(context, message, position, this);
                        break;
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_SINGLE_REVOKE_MESSAGE) {
                        chatRow = new ChatRowCmdGroupMsg(context, message, position,
                                this);
                        break;
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_OVERDUE_APPLY_CANCEL_COURSE) {
                        chatRow = new ChatRowCmdOverdueApplyCancelCourse(context, message,
                                position, this);
                        break;
                    }
                    
                    // 群聊，讲堂通用 ui 处理
                    if (cmdMsg.isGroupCmdMsg()) {
                        chatRow = new ChatRowCmdGroupMsg(context, message, position,
                                this);
                        break;
                    }
                    if (cmdMsg.isLectureNeedShowCmdMsg()) {
                        chatRow = new ChatRowCmdLectureMsg(context, message, position,
                                this);
                        break;
                    }
                    
                    // 通用消息处理
                    switch (cmdMsg.msgType) {
                        case CmdMsg.CMD_TYPE_TEXT:
                            chatRow = new ChatRowCmdText(context, message, position,
                                    this);
                            break;
                        case CmdMsg.CMD_TYPE_REMIND_ST:
                            chatRow = new ChatRowCmdRemindST(context, message, position,
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
    
    public String getToChatUsername() {
        return toChatUsername;
    }
    
    public void setShowUserNick(boolean showUserNick) {
        this.showUserNick = showUserNick;
    }
    
    public void setShowAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
    }
    
    public void setMyBubbleBg(Drawable myBubbleBg) {
        this.myBubbleBg = myBubbleBg;
    }
    
    public void setOtherBuddleBg(Drawable otherBuddleBg) {
        this.otherBuddleBg = otherBuddleBg;
    }
    
    public void setItemClickListener(MessageListItemClickListener listener) {
        itemClickListener = listener;
    }
    
    public void setCustomChatRowProvider(EaseCustomChatRowProvider rowProvider) {
        customRowProvider = rowProvider;
    }
    
    public boolean isShowUserNick() {
        return showUserNick;
    }
    
    public boolean isShowAvatar() {
        return showAvatar;
    }
    
    public Drawable getMyBubbleBg() {
        return myBubbleBg;
    }
    
    public Drawable getOtherBuddleBg() {
        return otherBuddleBg;
    }
    
}
