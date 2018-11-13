package com.qingqing.base.im;

import android.text.TextUtils;

import com.easemob.chat.EMMessage;
import com.qingqing.base.im.domain.ExtField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lihui on 2017/9/8.
 */

public class ExtFieldParser {
    public static ExtField getExt(EMMessage message) {
        ExtField ext = new ExtField();
        
        ext.noShow = Boolean
                .parseBoolean(message.getStringAttribute(ExtField.Attr.NO_SHOW, "false"));
        ext.needShowFrom = Boolean.parseBoolean(
                message.getStringAttribute(ExtField.Attr.NEED_SHOW_FROM, "false"));
        ext.isSelfMock = Boolean.parseBoolean(
                message.getStringAttribute(ExtField.Attr.IS_SELF_MOCK, "false"));
        ext.needReport = Boolean.parseBoolean(
                message.getStringAttribute(ExtField.Attr.NEED_REPORT, "false"));
        ext.needQuickResponse = Boolean.parseBoolean(
                message.getStringAttribute(ExtField.Attr.NEED_QUICK_RESPONSE, "false"));
        
        ext.qingqingMsgId = message.getStringAttribute(ExtField.Attr.QINGQING_MSG_ID, "");
        
        String targetUsers = message.getStringAttribute(ExtField.Attr.TARGET_USERS, "");
        String filterUsers = message.getStringAttribute(ExtField.Attr.FILTER_USERS, "");
        String[] targets = !TextUtils.isEmpty(targetUsers) ? targetUsers.split(",")
                : null;
        String[] filters = !TextUtils.isEmpty(filterUsers) ? filterUsers.split(",")
                : null;
        ext.targetUsers = targets != null ? Arrays.asList(targets) : null;
        ext.filterUsers = filters != null ? Arrays.asList(filters) : null;
        
        JSONObject userInfoJson = null;
        try {
            userInfoJson = message.getJSONObjectAttribute(ExtField.Attr.FROM_USER_INFO);
        } catch (Exception ignore) {}
        
        if (userInfoJson != null) {
            ext.fromUserInfo = new ExtField.FromUserInfo();
            ext.fromUserInfo.qingqingUserId = userInfoJson
                    .optString(ExtField.Attr.QINGQING_USER_ID);
            ext.fromUserInfo.nick = userInfoJson.optString(ExtField.Attr.NICK);
            ext.fromUserInfo.headImg = userInfoJson.optString(ExtField.Attr.HEAD_IMG);
            ext.fromUserInfo.sexType = userInfoJson.optInt(ExtField.Attr.SEX_TYPE);
            ext.fromUserInfo.userType = userInfoJson.optInt(ExtField.Attr.USER_TYPE);
            JSONArray roleTypeArray = userInfoJson
                    .optJSONArray(ExtField.Attr.CHAT_ROOM_AUTH_V2);
            ext.fromUserInfo.roleType = new ArrayList<>();
            if (roleTypeArray == null) {
                ext.fromUserInfo.roleType
                        .add(userInfoJson.optInt(ExtField.Attr.CHAT_ROOM_AUTH));
            }
            else {
                for (int i = 0; i < roleTypeArray.length(); i++) {
                    ext.fromUserInfo.roleType.add(roleTypeArray.optInt(i));
                }
            }
            try {
                JSONObject teacherExtendJson = userInfoJson
                        .getJSONObject(ExtField.Attr.TEACHER_EXTEND);
                if (teacherExtendJson != null) {
                    JSONArray teacherRole = teacherExtendJson
                            .optJSONArray(ExtField.Attr.TEACHER_ROLE);
                    if (teacherRole != null) {
                        ext.fromUserInfo.teacherExtend = new ExtField.FromUserInfo.TeacherExtend();
                        ext.fromUserInfo.teacherExtend.teacherRole = new ArrayList<>();
                        for (int i = 0; i < teacherRole.length(); i++) {
                            ext.fromUserInfo.teacherExtend.teacherRole
                                    .add(teacherRole.optInt(i));
                        }
                    }
                }
            } catch (JSONException ignored) {}
        }
        
        return ext;
    }
    
    public static void setExtUser(EMMessage message, String qqUserId, String nick,
            String headImage, int userType, int sex, List<Integer> userRole,
            List<Integer> teacherRole) {
        try {
            
            JSONObject object = new JSONObject();
            object.accumulate(ExtField.Attr.QINGQING_USER_ID, qqUserId);
            object.accumulate(ExtField.Attr.NICK, nick);
            object.accumulate(ExtField.Attr.HEAD_IMG, headImage);
            object.accumulate(ExtField.Attr.USER_TYPE, userType);
            object.accumulate(ExtField.Attr.SEX_TYPE, sex);
            if (userRole != null && userRole.size() > 0) {
                object.put(ExtField.Attr.CHAT_ROOM_AUTH, userRole.get(0));
                JSONArray userRoleArray = new JSONArray();
                for (int i = 0; i < userRole.size(); i++) {
                    userRoleArray.put(userRole.get(i));
                }
                object.put(ExtField.Attr.CHAT_ROOM_AUTH_V2, userRoleArray);
            }
            if (teacherRole != null && teacherRole.size() > 0) {
                JSONObject extend = new JSONObject();
                JSONArray teacherRoleArray = new JSONArray();
                for (int i = 0; i < teacherRole.size(); i++) {
                    teacherRoleArray.put(teacherRole.get(i));
                }
                extend.put(ExtField.Attr.TEACHER_ROLE, teacherRoleArray);
                object.put(ExtField.Attr.TEACHER_EXTEND, extend);
            }
            setEmMsgExtUser(message, object);
        } catch (Exception ignored) {}
    }
    
    private static void setEmMsgExtUser(EMMessage emMessage, JSONObject extObject) {
        emMessage.setAttribute(ExtField.Attr.FROM_USER_INFO, extObject);
    }
}
