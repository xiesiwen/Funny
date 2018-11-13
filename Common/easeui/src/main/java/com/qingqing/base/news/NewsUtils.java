package com.qingqing.base.news;

import android.content.Context;
import android.text.TextUtils;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.easeui.R;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/11/29.
 */

public class NewsUtils {
    
    private NewsUtils() {
        
    }
    
    static List<NewsConversation> getAllChatConversations() {
        List<NewsConversation> conversations = new ArrayList<>();
        try {
            // 获取所有会话，包括陌生人
            List<EMConversation> conversationList = new ArrayList<>(
                    EMChatManager.getInstance().getAllConversations().values());
            
            // 过滤掉messages size为0的conversation
            for (int i = 0; i < conversationList.size(); i++) {
                EMConversation conversation = conversationList.get(i);
                if (conversation.getAllMessages().size() == 0) {
                    conversationList.remove(i);
                    i--;
                }
            }
            
            for (EMConversation emConversation : conversationList) {
                if (emConversation.getType() == EMConversation.EMConversationType.Chat
                        && !emConversation.isGroup()) {
                    if (TextUtils.isEmpty(emConversation.getUserName())) {
                        continue;
                    }
                    NewsConversation conversation = new NewsConversation(
                            emConversation.getUserName(),
                            NewsConversationType.SINGLE_CHAT.getValue());
                    EMMessage message = emConversation.getLastMessage();
                    if (message != null) {
                        News news = new News(message.getMsgId(), 0,
                                getNewsContent(message, BaseApplication.getCtx()), null,
                                NewsConversationType.SINGLE_CHAT.getValue());
                        news.setCreatedTime(message.getMsgTime());
                        news.setFrom(message.getFrom());
                        news.setTo(message.getTo());
                        news.setConversationId(
                                message.direct == EMMessage.Direct.SEND ? message.getTo()
                                        : message.getFrom());
                        conversation.setLastNews(news);
                    }
                    conversation.setUnreadNewsCount(emConversation.getUnreadMsgCount());
                    conversations.add(conversation);
                }
                else if (emConversation
                        .getType() == EMConversation.EMConversationType.GroupChat
                        || emConversation.isGroup()) {
                    EMGroup group = EMGroupManager.getInstance()
                            .getGroup(emConversation.getUserName());
                    EMMessage message = emConversation.getLastMessage();
                    String groupId;
                    String groupName;
                    if (group != null) {
                        groupId = group.getGroupId();
                        groupName = group.getGroupName();
                    }
                    else {
                        groupId = ChatManager.getInstance()
                                .getGroupIdFromConversation(emConversation);
                        groupName = ChatManager.getInstance()
                                .getGroupNameFromConversation(emConversation);
                    }
                    if (TextUtils.isEmpty(groupId)) {
                        continue;
                    }
                    
                    NewsConversation conversation = new NewsConversation(groupId,
                            NewsConversationType.GROUP_CHAT.getValue(), groupName);
                    
                    if (message != null) {
                        News news = new News(message.getMsgId(), 0,
                                getNewsContent(message, BaseApplication.getCtx()), null,
                                NewsConversationType.GROUP_CHAT.getValue());
                        news.setCreatedTime(message.getMsgTime());
                        news.setFrom(message.getFrom());
                        news.setTo(message.getTo());
                        news.setConversationId(message.getTo());
                        conversation.setLastNews(news);
                    }
                    conversation.setUnreadNewsCount(emConversation.getUnreadMsgCount());
                    conversations.add(conversation);
                    
                }
            }
        } catch (Exception e) {
            Logger.e("NewsUtils", "getAllChatConversations", e);
        }
        return conversations;
    }
    
    public static boolean isChatType(String type) {
        return TextUtils.equals(type, NewsConversationType.GROUP_CHAT.getValue())
                || TextUtils.equals(type, NewsConversationType.SINGLE_CHAT.getValue());
    }
    
    /**
     * 根据消息内容和消息类型获取消息内容提示
     */
    private static String getNewsContent(EMMessage message, Context context) {
        String digest = "未知消息";
        switch (message.getType()) {
            case LOCATION:
                // 位置消息
                digest = getString(context, com.easemob.easeui.R.string.location_info);
                break;
            case IMAGE:
                // 图片消息
                digest = getString(context, com.easemob.easeui.R.string.picture_info);
                break;
            case VOICE:
                // 语音消息
                digest = getString(context, com.easemob.easeui.R.string.voice_info);
                break;
            case VIDEO:
                // 视频消息
                digest = getString(context, com.easemob.easeui.R.string.video_info);
                break;
            case TXT:
                // 文本消息
                TextMessageBody txtBody = (TextMessageBody) message.getBody();
                if (txtBody != null && !TextUtils.isEmpty(txtBody.getMessage())) {
                    digest = txtBody.getMessage();
                }
                break;
            case FILE:
                // 普通文件消息
                digest = getString(context, com.easemob.easeui.R.string.file_info);
                break;
            case CMD:
                // cmd 消息
                CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
                if (cmdMsg != null) {
                    if (cmdMsg.msgType == CmdMsg.CMD_TYPE_TEXT) {
                        // cmd 的文本消息， 需要解析出文本的内容
                        String txt = CmdMsgParser.parseCmdMsgBody(cmdMsg)
                                .getString(CmdMsg.Text.TEXT);
                        digest = txt == null ? "" : txt;
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_CONSULT_ST
                            || cmdMsg.msgType == CmdMsg.CMD_TYPE_CONSULT_TA) {
                        // 咨询
                        digest = getString(context,
                                com.easemob.easeui.R.string.cmd_consult);
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_REC_TEACHER) {
                        // 推荐
                        digest = getString(context,
                                com.easemob.easeui.R.string.cmd_recommend);
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_ST_BIND_TA) {
                        // 新学生绑定助教
                        digest = getString(context,
                                com.easemob.easeui.R.string.student_info);
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_RANK) {
                        digest = getString(context,
                                com.easemob.easeui.R.string.trm_group_rank);
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_COURSE_REPORT) {
                        String txt = CmdMsgParser.parseCmdMsgBody(cmdMsg)
                                .getString(CmdMsg.Group.REPORT_TITLE);
                        digest = txt == null ? "" : txt;
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_SHARE_PLAN_SUMMARIZE) {
                        String txt = CmdMsgParser.parseCmdMsgBody(cmdMsg)
                                .getString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_TITLE);
                        digest = txt == null ? "" : txt;
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_OVERDUE_APPLY_CANCEL_COURSE) {
                        String txt = CmdMsgParser.parseCmdMsgBody(cmdMsg)
                                .getString(CmdMsg.Text.TEXT);
                        digest = txt == null ? "" : txt;
                    }
                    else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_SINGLE_REVOKE_MESSAGE) {
                        String txt = null;
                        String userId = CmdMsgParser.parseCmdMsgBody(cmdMsg)
                                .getString(CmdMsg.Group.QQ_USER_ID);
                        if (BaseData.getSafeUserId().equals(userId)) {
                            txt = getString(context,
                                    R.string.chat_cmd_msg_group_revoke_me_message_text);
                        }
                        else {
                            ContactInfo contactInfo = ChatManager.getInstance()
                                    .getContactService().getContactInfo(userId);
                            if (contactInfo != null) {
                                txt = context.getString(
                                        R.string.chat_cmd_msg_single_revoke_others_message_text,
                                        IMUtils.getName(contactInfo));
                            }
                        }
                        digest = txt == null ? "" : txt;
                    }
                    else if (cmdMsg.isGroupCmdMsg()) {
                        // 群组cmd消息
                        digest = CmdMsgParser.getGroupMsgText(context, cmdMsg);
                    }
                    else if (cmdMsg.isLectureCmdMsg()) {
                        // Lecture cmd消息
                        digest = CmdMsgParser.getLectureText(context, cmdMsg,
                                ChatManager.getInstance().getMockUserName(message));
                    }
                }
                break;
            default:
                break;
        }
        
        return digest;
    }
    
    private static String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }
    
    public static boolean isUnreadDigitMode(String type) {
        return isChatType(type)
                || TextUtils.equals(type, NewsConversationType.TEACHING_TASK.getValue())
                || TextUtils.equals(type, NewsConversationType.TODO.getValue());
    }
    
}
