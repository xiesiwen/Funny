package com.qingqing.base.nim.utils;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.nim.domain.ChatContact;
import com.qingqing.base.nim.domain.ChatManager;
import com.qingqing.base.nim.domain.ChatRole;
import com.qingqing.base.nim.domain.Message;

import java.util.ArrayList;

/**
 * Created by huangming on 2016/8/24.
 */
public class ChatContactUtils {
    
    private ChatContactUtils() {
        
    }
    
    public static String getNick(Message message) {
        if (message.hasPlayedRole()) {
            return message.getRole().getRoleNick();
        }
        return IMUtils.getName(message.getFrom());
    }

    public static int getSexType(Message message) {
        if (message.hasPlayedRole()) {
            return message.getRole().getRoleSexType();
        }
        return IMUtils.getSexType(message.getFrom());
    }
    
    public static String getHeadImage(Message message) {
        if (message.hasPlayedRole()) {
            return message.getRole().getHeadImage();
        }
        return IMUtils.getAvatar(message.getFrom());
    }
    
    public static int getHeadDefaultIcon(Message message) {
        if (message.hasPlayedRole()) {
            return message.getRole().getRoleSexType() == UserProto.SexType.female
                    ? R.drawable.user_pic_girl : R.drawable.user_pic_boy;
        }
        return IMUtils.getDefaultHeadIcon(message.getFrom());
    }
    
    public static int getUserType(Message message) {
        if (message.hasPlayedRole()) {
            return message.getRole().getUserType();
        }
        return UserProto.UserType.unknown_user_type;
    }
    
    public static ChatRole getChatRoleBy(UserProto.SimpleUserInfoV2 infoV2,
            int roleType) {
        if (infoV2 == null) {
            return null;
        }
        ChatContact contact = new ChatContact(infoV2.qingqingUserId);
        contact.setSexType(infoV2.sex);
        contact.setHeadImage(infoV2.newHeadImage);
        contact.setUserType(infoV2.userType);
        contact.setNick(infoV2.nick);
        ArrayList<Integer> roleTypeArray = new ArrayList<>();
        roleTypeArray.add(roleType);
        return new ChatRole(roleTypeArray, contact);
    }
    
    public static ChatRole getSelfChatRole(int roleType) {
        String userId = ChatManager.getInstance().getCurrentUserId();
        ChatContact contact = new ChatContact(userId);
        ContactInfo info = com.qingqing.base.im.ChatManager.getInstance()
                .getContactModel().getContactInfo(userId);
        if (info != null) {
            contact.setNick(info.getNick());
            contact.setSexType(IMUtils.getSexType(userId));
            contact.setHeadImage(info.getAvatar());
            contact.setUserType(getCurrentUserType());
        }
        ArrayList<Integer> roleTypeArray = new ArrayList<>();
        roleTypeArray.add(roleType);
        return new ChatRole(roleTypeArray, contact);
    }
    
    private static int getCurrentUserType() {
        int appType = DefaultDataCache.INSTANCE().getAppType();
        
        switch (appType) {
            case AppCommon.AppType.qingqing_ta:
                return UserProto.UserType.ta;
            case AppCommon.AppType.qingqing_student:
                return UserProto.UserType.student;
            case AppCommon.AppType.qingqing_teacher:
                return UserProto.UserType.teacher;
            default:
                return UserProto.UserType.unknown_user_type;
        }
    }
    
}
