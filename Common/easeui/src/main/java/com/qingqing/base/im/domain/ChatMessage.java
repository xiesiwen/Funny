package com.qingqing.base.im.domain;

import android.text.TextUtils;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.Constant;
import com.qingqing.base.im.ExtFieldParser;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.utils.ListUtil;

import java.util.ArrayList;

/**
 * Created by huangming on 2016/5/31.
 */
public class ChatMessage {
    
    private EMMessage emMessage;
    private ExtField extField;
    
    private int chatScene;
    
    private boolean hasExtUser;
    
    public ChatMessage(EMMessage emMessage) {
        update(emMessage);
    }
    
    public void update(EMMessage message) {
        
        this.emMessage = message;
        
        if (emMessage.getChatType() == EMMessage.ChatType.GroupChat) {
            chatScene = Constant.CHATTYPE_GROUP;
        }
        else if (emMessage.getChatType() == EMMessage.ChatType.ChatRoom) {
            chatScene = Constant.CHATTYPE_CHATROOM;
        }
        else {
            chatScene = Constant.CHATTYPE_SINGLE;
        }
        
        extField = ExtFieldParser.getExt(emMessage);
        if (extField.fromUserInfo != null
                && !TextUtils.isEmpty(extField.fromUserInfo.qingqingUserId)) {
            hasExtUser = true;
            
            if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                saveGroupContactInfo(message.getTo());
            }
        }
        else {
            hasExtUser = false;
            
            extField.fromUserInfo = new ExtField.FromUserInfo();
            extField.fromUserInfo.qingqingUserId = emMessage.direct == EMMessage.Direct.SEND
                    ? ChatManager.getInstance().getCurrentUserName()
                    : emMessage.getFrom();
            extField.fromUserInfo.nick = "";
            extField.fromUserInfo.headImg = "";
            extField.fromUserInfo.sexType = UserProto.SexType.unknown;
            extField.fromUserInfo.userType = UserProto.UserType.unknown_user_type;
            extField.fromUserInfo.roleType = new ArrayList<>();
            extField.fromUserInfo.roleType
                    .add(ImProto.ChatRoomRoleType.general_chat_room_role_type);
        }
    }
    
    private void saveGroupContactInfo(String groupId) {
        UserProto.ChatUserInfo chatUserInfo = new UserProto.ChatUserInfo();
        chatUserInfo.qingqingUserId = extField.fromUserInfo.qingqingUserId;
        chatUserInfo.sex = extField.fromUserInfo.sexType;
        chatUserInfo.userType = extField.fromUserInfo.userType;
        chatUserInfo.newHeadImage = extField.fromUserInfo.headImg;
        chatUserInfo.nick = extField.fromUserInfo.nick;
        Integer[] tmpRoles = extField.fromUserInfo.roleType
                .toArray(new Integer[extField.fromUserInfo.roleType.size()]);
        int[] roles = new int[tmpRoles.length];
        for (int i = 0; i < tmpRoles.length; i++) {
            roles[i] = tmpRoles[i];
        }
        chatUserInfo.userRole = roles;
        if (extField.fromUserInfo.teacherExtend != null) {
            chatUserInfo.teacherExtend = new UserProto.ChatUserInfo.TeacherExtend();
            chatUserInfo.teacherExtend.teacherRole = ListUtil
                    .listToArray(extField.fromUserInfo.teacherExtend.teacherRole);
        }
        if (ChatManager.getInstance().getContactService() != null) {
            ChatManager.getInstance().getContactService()
                    .saveGroupContactInfo(chatUserInfo, groupId);
        }
    }
    
    public String getCurUserName() {
        return extField.fromUserInfo.qingqingUserId;
    }
    
    public ArrayList<Integer> getChatRoomType() {
        return new ArrayList<>(extField.fromUserInfo.roleType);
    }
    
    public int getSexType() {
        return hasExtUser ? extField.fromUserInfo.sexType
                : IMUtils.getSexType(extField.fromUserInfo.qingqingUserId);
    }
    
    public int getUserType() {
        return hasExtUser ? extField.fromUserInfo.userType
                : UserProto.UserType.unknown_user_type;
    }
    
    public String getHeadImage() {
        return hasExtUser ? extField.fromUserInfo.headImg
                : IMUtils.getAvatar(extField.fromUserInfo.qingqingUserId);
    }
    
    public String getNick() {
        return hasExtUser ? extField.fromUserInfo.nick
                : IMUtils.getName(extField.fromUserInfo.qingqingUserId);
    }
    
    public int getDefaultHeadIcon() {
        return hasExtUser
                ? (extField.fromUserInfo.sexType == UserProto.SexType.female
                        ? R.drawable.user_pic_girl
                        : R.drawable.user_pic_boy)
                : IMUtils.getDefaultHeadIcon(extField.fromUserInfo.qingqingUserId);
    }
    
    public boolean hasExtUser() {
        return hasExtUser;
    }
    
    public int getChatRoomScene() {
        return chatScene;
    }
}
