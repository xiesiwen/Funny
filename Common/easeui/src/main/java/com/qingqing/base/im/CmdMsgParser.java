package com.qingqing.base.im;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.model.GroupRankInfo;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.domain.GroupRole;
import com.qingqing.base.im.utils.IMUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by huangming on 2015/12/25.
 */
public class CmdMsgParser extends MsgParser {
    public static final String REVOKE_MSG_USER_ID = "revoke_msg_user_id";
    
    public static boolean isReceiveCmdMsg(EMMessage message) {
        return isReceiveMsg(message);
    }
    
    public static CmdMsg getCmdMsg(EMMessage message) {
        String curUser = ChatManager.getInstance().getCurrentUserName();
        
        if (message == null || TextUtils.isEmpty(curUser)) {
            return null;
        }
        if (!(message.getBody() instanceof CmdMessageBody)) {
            return null;
        }
        String action = ((CmdMessageBody) message.getBody()).action;
        if (TextUtils.isEmpty(action)) {
            return null;
        }
        CmdMsg cmdMsg = new CmdMsg();
        cmdMsg.isReceiveMsg = isReceiveCmdMsg(message);
        cmdMsg.from = message.getFrom();
        cmdMsg.to = message.getTo();
        try {
            cmdMsg.revokeMsgUserId = message.getStringAttribute(REVOKE_MSG_USER_ID);
        } catch (Exception ignored) {
            
        }
        try {
            cmdMsg.body = message.getStringAttribute("action", "");
            JSONObject json = new JSONObject(cmdMsg.body);
            cmdMsg.msgType = json.optInt(CmdMsg.TYPE);
            if ((cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_REVOKE_MESSAGE
                    || cmdMsg.msgType == CmdMsg.CMD_TYPE_SINGLE_REVOKE_MESSAGE)
                    && TextUtils.isEmpty(cmdMsg.revokeMsgUserId)) {
                cmdMsg.revokeMsgUserId = json.optString(CmdMsg.Group.QQ_USER_ID);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cmdMsg;
    }
    
    public static Bundle parseCmdMsgBody(EMMessage message) {
        return parseCmdMsgBody(getCmdMsg(message));
    }
    
    public static Bundle parseCmdMsgBody(CmdMsg cmdMsg) {
        return parseCmdMsgBody(cmdMsg != null ? cmdMsg.body : "",
                cmdMsg != null ? cmdMsg.msgType : -1);
        
    }
    
    public static Bundle parseCmdMsgBody(String body, int msgType) {
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(body)) {
            try {
                JSONObject json = new JSONObject(body);
                bundle.putInt(CmdMsg.TYPE, msgType);
                switch (msgType) {
                    case CmdMsg.CMD_TYPE_CONSULT_TA:
                    case CmdMsg.CMD_TYPE_CONSULT_ST:
                        bundle.putString(CmdMsg.Consult.QQ_TEACHER_ID,
                                json.optString(CmdMsg.Consult.QQ_TEACHER_ID));
                        bundle.putString(CmdMsg.Consult.FORMAT_COURSE_GRADE,
                                json.optString(CmdMsg.Consult.FORMAT_COURSE_GRADE));
                        bundle.putString(CmdMsg.Consult.TEACHER_REAL_NAME,
                                json.optString(CmdMsg.Consult.TEACHER_REAL_NAME));
                        bundle.putString(CmdMsg.Consult.TA_REAL_NAME,
                                json.optString(CmdMsg.Consult.TA_REAL_NAME));
                        bundle.putString(CmdMsg.Consult.ADDRESS,
                                json.optString(CmdMsg.Consult.ADDRESS));
                        bundle.putString(CmdMsg.Consult.NICK,
                                json.optString(CmdMsg.Consult.NICK));
                        bundle.putString(CmdMsg.Consult.TEACHER_SECOND_ID,
                                json.optString(CmdMsg.Consult.TEACHER_SECOND_ID));
                        bundle.putString(CmdMsg.Consult.SECOND_ID,
                                json.optString(CmdMsg.Consult.SECOND_ID));
                        bundle.putString(CmdMsg.Consult.HEAD_IMG,
                                json.optString(CmdMsg.Consult.HEAD_IMG));
                        break;
                    case CmdMsg.CMD_TYPE_TEXT:
                    case CmdMsg.CMD_TYPE_REMIND_ST:
                    case CmdMsg.CMD_TYPE_OVERDUE_APPLY_CANCEL_COURSE:
                        bundle.putString(CmdMsg.Text.TEXT,
                                json.optString(CmdMsg.Text.TEXT));
                        break;
                    case CmdMsg.CMD_TYPE_ST_BIND_TA:
                        bundle.putString(CmdMsg.BindTA.QQ_STUDENT_ID,
                                json.optString(CmdMsg.BindTA.QQ_STUDENT_ID));
                        bundle.putString(CmdMsg.BindTA.PHONE_NUMBER,
                                json.optString(CmdMsg.BindTA.PHONE_NUMBER));
                        bundle.putString(CmdMsg.BindTA.GRADE_NAME,
                                json.optString(CmdMsg.BindTA.GRADE_NAME));
                        bundle.putString(CmdMsg.BindTA.HEAD_IMG,
                                json.optString(CmdMsg.BindTA.HEAD_IMG));
                        bundle.putString(CmdMsg.BindTA.FLOW_TYPE,
                                json.optString(CmdMsg.BindTA.FLOW_TYPE));
                        break;
                    case CmdMsg.CMD_TYPE_REC_TEACHER:
                        bundle.putString(CmdMsg.RecTeacher.QQ_TEACHER_ID,
                                json.optString(CmdMsg.RecTeacher.QQ_TEACHER_ID));
                        bundle.putString(CmdMsg.RecTeacher.NICK,
                                json.optString(CmdMsg.RecTeacher.NICK));
                        bundle.putString(CmdMsg.RecTeacher.TEACHER_SECOND_ID,
                                json.optString(CmdMsg.RecTeacher.TEACHER_SECOND_ID));
                        bundle.putString(CmdMsg.RecTeacher.SECOND_ID,
                                json.optString(CmdMsg.RecTeacher.SECOND_ID));
                        bundle.putString(CmdMsg.RecTeacher.HEAD_IMG,
                                json.optString(CmdMsg.RecTeacher.HEAD_IMG));
                        bundle.putInt(CmdMsg.RecTeacher.GOOD_APPRAISE_COUNT,
                                json.optInt(CmdMsg.RecTeacher.GOOD_APPRAISE_COUNT));
                        bundle.putString(CmdMsg.RecTeacher.DESCRPTION,
                                json.optString(CmdMsg.RecTeacher.DESCRPTION));
                        bundle.putString(CmdMsg.RecTeacher.COURSE_NAME,
                                json.optString(CmdMsg.RecTeacher.COURSE_NAME));
                        bundle.putDouble(CmdMsg.RecTeacher.MIN_PRICE,
                                json.optDouble(CmdMsg.RecTeacher.MIN_PRICE));
                        bundle.putDouble(CmdMsg.RecTeacher.MAX_PRICE,
                                json.optDouble(CmdMsg.RecTeacher.MAX_PRICE));
                        bundle.putString(CmdMsg.RecTeacher.GRADE_COURSE,
                                json.optString(CmdMsg.RecTeacher.GRADE_COURSE));
                        break;
                    case CmdMsg.CMD_TYPE_GROUP_UPDATE:
                    case CmdMsg.CMD_TYPE_GROUP_EXIT:
                    case CmdMsg.CMD_TYPE_GROUP_REMOVED:
                    case CmdMsg.CMD_TYPE_GROUP_INVITED:
                    case CmdMsg.CMD_TYPE_GROUP_CREATED:
                    case CmdMsg.CMD_TYPE_GROUP_ANNOUNCE:
                    case CmdMsg.CMD_TYPE_GROUP_REVOKE_MESSAGE:
                    case CmdMsg.CMD_TYPE_SINGLE_REVOKE_MESSAGE:
                        bundle.putString(CmdMsg.Group.QQ_USER_ID,
                                json.optString(CmdMsg.Group.QQ_USER_ID));
                        if (json.has(CmdMsg.Group.GROUP_NAME)) {
                            bundle.putString(CmdMsg.Group.GROUP_NAME,
                                    json.optString(CmdMsg.Group.GROUP_NAME));
                        }
                        if (json.has(CmdMsg.Group.GROUP_MEMBERS)) {
                            String members = json.optString(CmdMsg.Group.GROUP_MEMBERS);
                            if (!TextUtils.isEmpty(members)) {
                                try {
                                    JSONArray membersJsonArray = new JSONArray(members);
                                    ArrayList<String> userNames = new ArrayList<>();
                                    int length = membersJsonArray.length();
                                    for (int i = 0; i < length; i++) {
                                        userNames.add(membersJsonArray.getString(i));
                                    }
                                    bundle.putStringArrayList(CmdMsg.Group.GROUP_MEMBERS,
                                            userNames);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (json.has(CmdMsg.Group.GROUP_ANNOUNCE)) {
                            bundle.putString(CmdMsg.Group.GROUP_ANNOUNCE,
                                    json.optString(CmdMsg.Group.GROUP_ANNOUNCE));
                        }
                        if (json.has(CmdMsg.Group.MSG_ID)) {
                            bundle.putString(CmdMsg.Group.MSG_ID,
                                    json.optString(CmdMsg.Group.MSG_ID));
                        }
                        break;
                    case CmdMsg.CMD_TYPE_GROUP_COURSE_REPORT:
                        bundle.putString(CmdMsg.Group.QQ_USER_ID,
                                json.optString(CmdMsg.Group.QQ_USER_ID));
                        bundle.putString(CmdMsg.Group.SHARE_CODE,
                                json.optString(CmdMsg.Group.SHARE_CODE));
                        bundle.putString(CmdMsg.Group.HEAD_IMAGE,
                                json.optString(CmdMsg.Group.HEAD_IMAGE));
                        bundle.putString(CmdMsg.Group.NICK,
                                json.optString(CmdMsg.Group.NICK));
                        bundle.putString(CmdMsg.Group.REPORT_TITLE,
                                json.optString(CmdMsg.Group.REPORT_TITLE));
                        bundle.putInt(CmdMsg.Group.TOTAL_WORDS_COUNT,
                                json.optInt(CmdMsg.Group.TOTAL_WORDS_COUNT));
                        bundle.putInt(CmdMsg.Group.TOTAL_AUDIO_TIME_LENGTH,
                                json.optInt(CmdMsg.Group.TOTAL_AUDIO_TIME_LENGTH));
                        bundle.putInt(CmdMsg.Group.TOTAL_PICTURE_COUNT,
                                json.optInt(CmdMsg.Group.TOTAL_PICTURE_COUNT));
                        break;
                    case CmdMsg.CMD_TYPE_SHARE_PLAN_SUMMARIZE:
                        bundle.putString(CmdMsg.Group.QQ_USER_ID,
                                json.optString(CmdMsg.Group.QQ_USER_ID));
                        bundle.putString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_TYPE,
                                json.optString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_TYPE));
                        bundle.putString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_TITLE,
                                json.optString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_TITLE));
                        bundle.putString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_CONTENT, json
                                .optString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_CONTENT));
                        bundle.putString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_REF_ID,
                                json.optString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_REF_ID));
                        break;
                    case CmdMsg.CMD_TYPE_GROUP_RANK:
                        bundle.putString(CmdMsg.Group.RANK_TITLE,
                                json.optString(CmdMsg.Group.RANK_TITLE));
                        JSONArray infoArray = json
                                .optJSONArray(CmdMsg.Group.RANK_INFO_LIST);
                        ArrayList<GroupRankInfo> rankInfoList = new ArrayList<>();
                        if (infoArray != null) {
                            for (int i = 0; i < infoArray.length(); i++) {
                                JSONObject object = (JSONObject) infoArray.get(i);
                                
                                String userType = object
                                        .optString(CmdMsg.Group.RANK_USER_TYPE);
                                String indexValue = object
                                        .optString(CmdMsg.Group.RANK_INDEX_VALUE);
                                int indexRank = object
                                        .optInt(CmdMsg.Group.RANK_INDEX_RANK);
                                int indexTrend = object
                                        .optInt(CmdMsg.Group.RANK_INDEX_TREND);
                                String headImage = object
                                        .optString(CmdMsg.Group.RANK_HEAD_IMAGE);
                                String nick = object.optString(CmdMsg.Group.RANK_NICK);
                                String qqUserId = object
                                        .optString(CmdMsg.Group.RANK_QQ_USER_ID);
                                
                                rankInfoList.add(
                                        new GroupRankInfo(userType, indexValue, indexRank,
                                                indexTrend, headImage, nick, qqUserId));
                            }
                        }
                        bundle.putParcelableArrayList(CmdMsg.Group.RANK_INFO_LIST,
                                rankInfoList);
                        bundle.putString(CmdMsg.Group.SHARE_CODE,
                                json.optString(CmdMsg.Group.SHARE_CODE));
                        break;
                    case CmdMsg.CMD_TYPE_LECTURE_STOP_TALK:
                        bundle.putString(CmdMsg.Lecture.TO_STOP_USER_ID,
                                json.optString(CmdMsg.Lecture.TO_STOP_USER_ID));
                        bundle.putString(CmdMsg.Lecture.TO_STOP_USER_NICK,
                                json.optString(CmdMsg.Lecture.TO_STOP_USER_NICK));
                        bundle.putInt(CmdMsg.Lecture.TO_STOP_USER_TYPE,
                                json.optInt(CmdMsg.Lecture.TO_STOP_USER_TYPE));
                        bundle.putBoolean(CmdMsg.Lecture.IS_ALLOW,
                                json.optBoolean(CmdMsg.Lecture.IS_ALLOW));
                        break;
                    case CmdMsg.CMD_TYPE_LECTURE_PLAY_PPT:
                        bundle.putString(CmdMsg.Lecture.PPT_IMG_URL,
                                json.optString(CmdMsg.Lecture.PPT_IMG_URL));
                        bundle.putInt(CmdMsg.Lecture.PPT_IMG_INDEX,
                                json.optInt(CmdMsg.Lecture.PPT_IMG_INDEX));
                        bundle.putLong(CmdMsg.Lecture.SEND_TIME,
                                json.optLong(CmdMsg.Lecture.SEND_TIME));
                        break;
                    case CmdMsg.CMD_TYPE_LECTURE_CHANGE_ROOM:
                        bundle.putString(CmdMsg.Lecture.CHAT_ROOM_ID,
                                json.optString(CmdMsg.Lecture.CHAT_ROOM_ID));
                        break;
                    case CmdMsg.CMD_TYPE_LECTURE_CHANGE_PPT:
                        JSONArray jsonArray = json
                                .optJSONArray(CmdMsg.Lecture.PPT_IMG_URLS);
                        
                        int jsonArrayLength = jsonArray != null ? jsonArray.length() : 0;
                        String[] imgUrls = new String[jsonArrayLength];
                        if (jsonArrayLength > 0) {
                            for (int i = 0; i < jsonArrayLength; i++) {
                                imgUrls[i] = jsonArray.optString(i);
                            }
                        }
                        bundle.putStringArray(CmdMsg.Lecture.PPT_IMG_URLS, imgUrls);
                        bundle.putLong(CmdMsg.Lecture.SEND_TIME,
                                json.optLong(CmdMsg.Lecture.SEND_TIME));
                        break;
                    default:
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return bundle;
    }
    
    public static String getLectureText(Context context, CmdMsg cmdMsg,
            String curUserName) {
        if (context == null || cmdMsg == null) {
            return "";
        }
        if (cmdMsg.msgType == CmdMsg.CMD_TYPE_LECTURE_DESTROYED) {
            return context.getString(R.string.tips_lecture_room_end);
        }
        else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_LECTURE_STOP_TALK) {
            Bundle bundle = parseCmdMsgBody(cmdMsg);
            String userName = bundle.getString(CmdMsg.Lecture.TO_STOP_USER_ID);
            String userNick = bundle.getString(CmdMsg.Lecture.TO_STOP_USER_NICK);
            // int userType = bundle.getInt(CmdMsg.Lecture.TO_STOP_USER_TYPE);
            boolean allowTalk = bundle.getBoolean(CmdMsg.Lecture.IS_ALLOW);
            if (TextUtils.isEmpty(userName)) {
                return context
                        .getString(!allowTalk ? R.string.tips_manager_forbid_all_speak
                                : R.string.tips_manager_allow_all_speak);
            }
            if (userName.equals(curUserName)) {
                return context.getString(
                        !allowTalk ? R.string.tips_you_speak_forbidden_by_manager
                                : R.string.tips_you_speak_allow_by_manager);
            }
            else {
                return context
                        .getString(!allowTalk ? R.string.tips_speak_forbidden_by_manager
                                : R.string.tips_speak_allow_by_manager, userNick);
            }
        }
        else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_LECTURE_PLAY_PPT) {
            Bundle bundle = parseCmdMsgBody(cmdMsg);
            int pptIndex = bundle.getInt(CmdMsg.Lecture.PPT_IMG_INDEX);
            return context.getString(R.string.tips_expert_play_ppt_index,
                    String.valueOf(pptIndex + 1));
        }
        return "";
    }
    
    public static boolean isLectureSpeakAllForbiddenMsg(Bundle bundle) {
        String userName = bundle.getString(CmdMsg.Lecture.TO_STOP_USER_ID);
        return TextUtils.isEmpty(userName);
    }
    
    public static String getLectureSpeakForbiddenUserName(Bundle bundle) {
        return bundle.getString(CmdMsg.Lecture.TO_STOP_USER_ID);
    }
    
    public static boolean isLectureSpeakForbidden(Bundle bundle) {
        return !bundle.getBoolean(CmdMsg.Lecture.IS_ALLOW);
    }
    
    public static String getGroupMsgText(Context context, CmdMsg cmdMsg) {
        if (context == null || cmdMsg == null) {
            return "";
        }
        Bundle bundle = parseCmdMsgBody(cmdMsg);
        String userName = bundle.getString(CmdMsg.Group.QQ_USER_ID);
        String groupName = bundle.getString(CmdMsg.Group.GROUP_NAME);
        if (TextUtils.isEmpty(userName)) {
            return "";
        }
        ArrayList<String> members = bundle.getStringArrayList(CmdMsg.Group.GROUP_MEMBERS);
        switch (cmdMsg.msgType) {
            case CmdMsg.CMD_TYPE_GROUP_UPDATE:
                if (userName.equals(BaseData.getSafeUserId())) {
                    if (!TextUtils.isEmpty(groupName)) {
                        return context.getString(
                                R.string.chat_cmd_msg_group_update_me_text, groupName);
                    }
                    else {
                        return context.getString(
                                R.string.chat_cmd_msg_group_update_me_empty_text);
                    }
                }
                else {
                    if (!TextUtils.isEmpty(groupName)) {
                        return context.getString(
                                R.string.chat_cmd_msg_group_update_others_text,
                                getGroupName(userName, cmdMsg.to), groupName);
                    }
                    else {
                        return context.getString(
                                R.string.chat_cmd_msg_group_update_others_empty_text,
                                getGroupName(userName, cmdMsg.to));
                    }
                }
            case CmdMsg.CMD_TYPE_GROUP_EXIT:
                return context.getString(R.string.chat_cmd_msg_group_user_exit_text,
                        getGroupName(userName, cmdMsg.to));
            case CmdMsg.CMD_TYPE_GROUP_REMOVED:
                if (userName.equals(BaseData.getSafeUserId())) {
                    return context.getString(
                            R.string.chat_cmd_msg_group_user_removed_member_text,
                            getGroupNames(members, cmdMsg.to));
                }
                else if (members != null && members.contains(BaseData.getSafeUserId())) {
                    return context
                            .getString(R.string.chat_cmd_msg_group_user_removed_me_text);
                }
                else {
                    return context.getString(
                            R.string.chat_cmd_msg_group_user_removed_others_text,
                            getGroupName(userName, cmdMsg.to),
                            getGroupNames(members, cmdMsg.to));
                }
            case CmdMsg.CMD_TYPE_GROUP_INVITED:
                if (userName.equals(BaseData.getSafeUserId())) {
                    return context.getString(
                            R.string.chat_cmd_msg_group_user_invited_member_text,
                            getGroupNames(members, cmdMsg.to));
                }
                else if (members != null && members.contains(BaseData.getSafeUserId())) {
                    return context.getString(
                            R.string.chat_cmd_msg_group_user_invited_me_text,
                            getGroupName(userName, cmdMsg.to));
                }
                else {
                    return context.getString(
                            R.string.chat_cmd_msg_group_user_invited_others_text,
                            getGroupName(userName, cmdMsg.to),
                            getGroupNames(members, cmdMsg.to));
                }
            case CmdMsg.CMD_TYPE_GROUP_CREATED:
                if (userName.equals(BaseData.getSafeUserId())) {
                    return context.getString(R.string.chat_cmd_msg_group_created_me_text);
                }
                else {
                    return context.getString(
                            R.string.chat_cmd_msg_group_created_others_text,
                            getGroupName(userName, cmdMsg.to));
                }
            case CmdMsg.CMD_TYPE_GROUP_REVOKE_MESSAGE:
                if (userName.equals(BaseData.getSafeUserId())) {
                    EMConversation conversation = EMChatManager.getInstance()
                            .getConversation(cmdMsg.to);
                    if (conversation != null && cmdMsg.revokeMsgUserId != null) {
                        ContactInfo revokeMsgUser = ChatManager.getInstance()
                                .getContactService()
                                .getContactInfo(cmdMsg.revokeMsgUserId);
                        if (revokeMsgUser != null) {
                            if (revokeMsgUser.getUsername()
                                    .equals(BaseData.getSafeUserId())) {
                                return context.getString(
                                        R.string.chat_cmd_msg_group_revoke_me_message_text);
                            }
                            else {
                                return context.getString(
                                        R.string.chat_cmd_msg_group_revoke_others_message_text,
                                        getGroupName(revokeMsgUser.getUsername(),
                                                cmdMsg.to));
                            }
                            
                        }
                    }
                }
                break;
            case CmdMsg.CMD_TYPE_SINGLE_REVOKE_MESSAGE:
                if (userName.equals(BaseData.getSafeUserId())) {
                    EMConversation conversation = EMChatManager.getInstance()
                            .getConversation(cmdMsg.to);
                    if (conversation != null && cmdMsg.revokeMsgUserId != null) {
                        ContactInfo revokeMsgUser = ChatManager.getInstance()
                                .getContactService()
                                .getContactInfo(cmdMsg.revokeMsgUserId);
                        if (revokeMsgUser != null) {
                            if (revokeMsgUser.getUsername()
                                    .equals(BaseData.getSafeUserId())) {
                                return context.getString(
                                        R.string.chat_cmd_msg_group_revoke_me_message_text);
                            }
                            else {
                                return context.getString(
                                        R.string.chat_cmd_msg_single_revoke_others_message_text,
                                        IMUtils.getName(revokeMsgUser));
                            }
                        }
                    }
                }
                else {
                    // 对方撤回
                    EMConversation conversation = EMChatManager.getInstance()
                            .getConversation(cmdMsg.from);
                    if (conversation != null && cmdMsg.revokeMsgUserId != null) {
                        ContactInfo revokeMsgUser = ChatManager.getInstance()
                                .getContactService()
                                .getContactInfo(cmdMsg.revokeMsgUserId);
                        if (revokeMsgUser != null) {
                            return context.getString(
                                    R.string.chat_cmd_msg_single_revoke_others_message_text,
                                    IMUtils.getName(revokeMsgUser));
                        }
                    }
                }
                break;
            default:
                return "";
        }
        return "";
    }
    
    public static String getGroupName(String userId, final String groupId) {
        ContactInfo contactInfo = ChatManager.getInstance().getContactService()
                .getContactInfo(userId);
        if (contactInfo == null) {
            ChatManager.getInstance().getGroupContactInfo(groupId, userId);
            return "";
        }
        
        GroupRole groupRole = contactInfo.getGroupRole(groupId);
        if (groupRole != null) {
            return groupRole.getNick();
        }
        else {
            ChatManager.getInstance().getGroupContactInfo(groupId, userId);
            return contactInfo.getNick();
        }
    }
    
    private static String getGroupNames(ArrayList<String> members, String groupId) {
        StringBuilder sb = new StringBuilder();
        if (members != null && members.size() >= 1) {
            int length = members.size();
            for (int i = 0; i < length; i++) {
                sb.append(getGroupName(members.get(i), groupId));
                if (i < length - 1) {
                    sb.append("、");
                }
            }
        }
        return sb.toString();
    }
    
}
