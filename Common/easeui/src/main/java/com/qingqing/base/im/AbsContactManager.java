package com.qingqing.base.im;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.qingqing.api.proto.ta.v1.StudentForTA;
import com.qingqing.api.proto.ta.v1.TeacherForTA;
import com.qingqing.api.proto.v1.StudentProto;
import com.qingqing.api.proto.v1.TeacherProto;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.ListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huangming on 2015/12/27.
 */
public abstract class AbsContactManager implements DataRequestListener, ContactService {

    private static final String TAG = "AbsContactManager";

    protected ContactModel model;

    private List<DataRefreshListener> refreshListeners = new ArrayList<DataRefreshListener>();

    private Handler mainHandler;

    protected AbsContactManager() {
        model = ChatManager.getInstance().getContactModel();

        mainHandler = new Handler(DemoApplication.getCtx().getMainLooper());
    }

    public boolean isSynced() {
        return model.isSynced();
    }

    public void asyncFetchData() {
        // 保存本次更新联系人时间戳
        BaseData.setLastUpdateContactsListTime(NetworkTime.currentTimeMillis());
    }

    public ContactInfo getContactInfo(String userName) {
        return model.getContactInfo(userName);
    }

    public List<ContactInfo> getFriendsList() {
        List<ContactInfo> contactInfoList = new ArrayList<ContactInfo>();

        Map<String, ContactInfo> contactList = model.getContactList();
        for (String userName : contactList.keySet()) {
            ContactInfo contactInfo = contactList.get(userName);
            if (contactInfo != null && contactInfo.isFriends()) {
                contactInfoList.add(contactInfo);
            }
        }
        return contactInfoList;
    }

    public void saveContactInfo(ContactInfo contactInfo) {
        model.saveContact(contactInfo);
        notifyRefreshListener();
    }

    public void saveSelf(UserProto.LimitUserInfoV2 info) {
        saveContactInfo(info, ContactInfo.Type.Self, ContactInfo.RELATION_SELF);
    }

    public void saveFriend(UserProto.LimitUserInfoV2 info, ContactInfo.Type type) {
        saveContactInfo(info, type, ContactInfo.RELATION_FRIENDS);
    }

    public void saveContactInfo(UserProto.LimitUserInfoV2 info, ContactInfo.Type type, int friends) {
        if (info != null && info.userInfo!=null) {
            UserProto.SimpleUserInfoV2 userInfo = info.userInfo;
            String userName = userInfo.qingqingUserId;
            String nick = userInfo.nick;
            String avatar = userInfo.newHeadImage;
            int sex;
            if (!userInfo.hasSex){
                sex = UserProto.SexType.unknown;
            }else {
                sex = userInfo.sex;
            }
            if (TextUtils.isEmpty(userName)) {
                return;
            }
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo == null) {
                contactInfo = new ContactInfo(userName, friends, sex, type);
            }
            contactInfo.setSex(sex);
            contactInfo.setFriends(friends);
            contactInfo.setNick(nick);
            contactInfo.setAvatar(avatar);
            saveContactInfo(contactInfo);
        }
    }

    public void saveContactInfo(UserProto.SimpleUserInfoV2 info, ContactInfo.Type type,
            int friends) {
        if (info != null) {
            String userName = info.qingqingUserId;
            String nick = info.nick;
            String avatar = info.newHeadImage;
            int sex;
            if (!info.hasSex) {
                sex = UserProto.SexType.unknown;
            }
            else {
                sex = info.sex;
            }
            if (TextUtils.isEmpty(userName)) {
                return;
            }
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo == null) {
                contactInfo = new ContactInfo(userName, friends, sex, type);
            }
            contactInfo.setSex(sex);
            contactInfo.setFriends(friends);
            contactInfo.setNick(nick);
            contactInfo.setAvatar(avatar);
            saveContactInfo(contactInfo);
        }
    }

    public void saveContactInfo(UserProto.LimitUserInfo info, ContactInfo.Type type) {
        if (info != null && info.userInfo!=null) {
            UserProto.SimpleUserInfo userInfo = info.userInfo;
            String userName = userInfo.qingqingUserId;
            String nick = userInfo.nick;
            String avatar = userInfo.newHeadImage;
            int sex = userInfo.sex;
            if (TextUtils.isEmpty(userName)) {
                return;
            }
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo == null) {
                contactInfo = new ContactInfo(userName, ContactInfo.RELATION_STRANGER, sex, type);
            }
            contactInfo.setSex(sex);
            contactInfo.setNick(nick);
            contactInfo.setAvatar(avatar);
            saveContactInfo(contactInfo);
        }
    }

    public void saveContactInfo(String userName, String nick, String headImg, int sex, String phone, ContactInfo.Type type, int friends) {
        if (userName != null) {
            if (TextUtils.isEmpty(userName)) {
                return;
            }
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo == null) {
                contactInfo = new ContactInfo(userName, friends, sex, type);
            }
            contactInfo.setSex(sex);
            contactInfo.setFriends(friends);
            contactInfo.setNick(nick);
            contactInfo.setAvatar(headImg);
            contactInfo.setPhone(phone);
            saveContactInfo(contactInfo);
        }
    }

    public void saveContactInfo(String userName, String nick, ContactInfo.Type type) {
        if (userName != null) {
            if (TextUtils.isEmpty(userName)) {
                return;
            }
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo == null) {
                contactInfo = new ContactInfo(userName, 0, UserProto.SexType.male, type);
            }
            contactInfo.setNick(nick);
            saveContactInfo(contactInfo);
        }
    }

    public void saveContactRemark(String userName, String remark) {
        if (userName != null) {
            if (TextUtils.isEmpty(userName)) {
                return;
            }
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo != null) {
                contactInfo.setAlias(remark);
                saveContactInfo(contactInfo);
            }

        }
    }

    public void saveContactInfo(UserProto.ChatUserInfo userInfo) {
        if (userInfo != null) {
            String userName = userInfo.qingqingUserId;
            ContactInfo contactInfo = getContactInfo(userName);
            int sex = userInfo.sex;
            int userType = userInfo.userType;
            ContactInfo.Type type = getContactType(userType);
            if (TextUtils.isEmpty(userName)) {
                return;
            }
            if (contactInfo == null) {
                contactInfo = new ContactInfo(userName, ContactInfo.RELATION_STRANGER, sex, type);
            }
            contactInfo.setSex(sex);
            contactInfo.setAvatar(userInfo.newHeadImage);
            contactInfo.setNick(userInfo.nick);
            contactInfo.setTaNick(
                    userInfo.assistantInfo != null ? userInfo.assistantInfo.nick : null);
            contactInfo.setTaUserID(userInfo.assistantInfo != null
                    ? userInfo.assistantInfo.qingqingUserId
                    : null);
            if (userInfo.teacherExtend != null
                    && userInfo.teacherExtend.teacherRole != null) {
                contactInfo.setTeacherRole(
                        ListUtil.arrayToList(userInfo.teacherExtend.teacherRole));
            }
            else {
                contactInfo.setTeacherRole(null);
            }
            saveContactInfo(contactInfo);
        }
    }
    
    public void saveGroupContactInfo(UserProto.ChatUserInfo userInfo, String groupId) {
        if (userInfo != null) {
            String userName = userInfo.qingqingUserId;
            ContactInfo contactInfo = getContactInfo(userName);
            int sex = userInfo.sex;
            int userType = userInfo.userType;
            ContactInfo.Type type = getContactType(userType);
            if (TextUtils.isEmpty(userName)) {
                return;
            }
            if (contactInfo == null) {
                contactInfo = new ContactInfo(userName, ContactInfo.RELATION_STRANGER,
                        sex, type);
            }
            contactInfo.setSex(sex);
            contactInfo.setAvatar(userInfo.newHeadImage);
            contactInfo.setTaNick(
                    userInfo.assistantInfo != null ? userInfo.assistantInfo.nick : null);
            contactInfo.setTaUserID(
                    userInfo.assistantInfo != null ? userInfo.assistantInfo.qingqingUserId
                            : null);
            if (userInfo.teacherExtend != null
                    && userInfo.teacherExtend.teacherRole != null) {
                contactInfo.setTeacherRole(
                        ListUtil.arrayToList(userInfo.teacherExtend.teacherRole));
            }
            else {
                contactInfo.setTeacherRole(null);
            }
            ArrayList<Integer> groupRole = new ArrayList<>();
            for (int i = 0; i < userInfo.userRole.length; i++) {
                groupRole.add(userInfo.userRole[i]);
            }
            contactInfo.setGroupRole(groupId,userInfo.nick,groupRole);
            saveContactInfo(contactInfo);
        }
    }
    
    public static ContactInfo.Type getContactType(Integer userType) {
        if (userType != null) {
            switch (userType) {
                case UserProto.UserType.student:
                    return ContactInfo.Type.Student;
                case UserProto.UserType.teacher:
                    return ContactInfo.Type.Teacher;
                case UserProto.UserType.ta:
                    return ContactInfo.Type.Assistant;
                default:
                    break;
            }
        }
        return ContactInfo.Type.Unknown;
    }

    @Override
    public void onRequest() {
        asyncFetchData();
    }

    @Override
    public void onRequest(Object... parameters) {
        if (parameters != null && parameters.length >= 2) {
            try {
                String userName = (String) parameters[0];
                ContactInfo.Type type = (ContactInfo.Type) parameters[1];

                boolean needed = true;
                if (parameters.length >= 3) {
                    needed = (Boolean) parameters[2];
                }

                Log.i(TAG, "onRequest : userName = " + userName + "  type = " + type);
                if (!TextUtils.isEmpty(userName) && type != null) {
                    switch (type) {
                        case Student:
                            asyncFetchBindStudentFromServer(userName, needed);
                            break;
                        case Teacher:
                            asyncFetchBindTeacherFromServer(userName, needed);
                            break;
                        case Assistant:
                            asyncFetchBindTAFromServer(userName, needed);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void asyncFetchBindTAFromServer(String userName, boolean needed) {
        if (!needed) {
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo == null || !contactInfo.isFriends()) {
                asyncFetchBindTAFromServer(userName);
            } else {
                Logger.i(TAG, "TA exists");
            }
        } else {
            asyncFetchBindTAFromServer(userName);
        }
    }

    protected void asyncFetchBindTAFromServer(final String userName) {

    }

    protected void asyncFetchBindStudentFromServer(String userName, boolean needed) {
        if (!needed) {
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo == null || !contactInfo.isFriends()) {
                asyncFetchBindStudentFromServer(userName);
            } else {
                Logger.i(TAG, "TA exists");
            }
        } else {
            asyncFetchBindStudentFromServer(userName);
        }
    }

    protected void asyncFetchBindStudentFromServer(final String userName) {
        if (TextUtils.isEmpty(userName)) {
            Logger.w("msg", "131 null student id");
            return;
        }
        StudentProto.SimpleQingQingStudentIdRequest request = new StudentProto.SimpleQingQingStudentIdRequest();
        request.qingqingStudentId =userName;
        new ProtoReq(CommonUrl.TA_STUDENT_DETAIL.url())
                .setSendMsg(request)
                .setRspListener(
                        new ProtoListener(
                                StudentForTA.TABindingStudentDetailResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                
                                Logger.i(TAG, "asyncFetchBindStudentFromServer response");
                                
                                StudentForTA.TABindingStudentDetailResponse response = (StudentForTA.TABindingStudentDetailResponse) result;
                                
                                saveContactInfo(response.studentDetail
                                        .studentLimitInfo, ContactInfo.Type.Student,
                                        ContactInfo.RELATION_FRIENDS);
                                notifyRefreshListener();
                            }
                            
                            @Override
                            public void onDealError(HttpError error, boolean isParseOK,
                                                    int errorCode, Object result) {
                                super.onDealError(error,isParseOK,errorCode,result);
                                Logger.e(TAG,
                                        "asyncFetchBindStudentFromServer response error");
                            }
                        }).req();
    }

    protected void asyncFetchBindTeacherFromServer(String userName, boolean needed) {
        if (!needed) {
            ContactInfo contactInfo = getContactInfo(userName);
            if (contactInfo == null || !contactInfo.isFriends()) {
                asyncFetchBindTeacherFromServer(userName);
            } else {
                Logger.i(TAG, "Teacher exists");
            }
        } else {
            asyncFetchBindTeacherFromServer(userName);
        }
    }

    protected void asyncFetchBindTeacherFromServer(final String userName) {
        if (TextUtils.isEmpty(userName)) {
            Logger.w("msg", "131 null teacher id");
            return;
        }
        final TeacherProto.SimpleQingQingTeacherIdRequest request = new TeacherProto.SimpleQingQingTeacherIdRequest();
        request.qingqingTeacherId = userName;

        new ProtoReq(CommonUrl.TA_TEACHER_DETAIL.url())
                .setSendMsg(request)
                .setRspListener(
                        new ProtoListener(
                                TeacherForTA.TABindingTeacherDetailResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                TeacherForTA.TABindingTeacherDetailResponse response = (TeacherForTA.TABindingTeacherDetailResponse) result;
                                
                                Logger.i(TAG, "asyncFetchBindTeacherFromServer response");
                                saveContactInfo(response.teacherDetail
                                        .teacherLimitInfo, ContactInfo.Type.Teacher,
                                        ContactInfo.RELATION_FRIENDS);
                                notifyRefreshListener();
                            }
                            
                            @Override
                            public void onDealError(HttpError error, boolean isParseOK,
                                    int errorCode, Object result) {
                                super.onDealError(error,isParseOK,errorCode,result);
                                Logger.i(TAG,
                                        "asyncFetchBindTeacherFromServer response error");
                            }
                        }).req();
    }


    public void addRefreshListener(DataRefreshListener listener) {
        if (listener == null) {
            return;
        }
        if (!refreshListeners.contains(listener)) {
            refreshListeners.add(listener);
        }
    }

    public void removeRefreshListener(DataRefreshListener listener) {
        if (listener == null) {
            return;
        }
        if (refreshListeners.contains(listener)) {
            refreshListeners.remove(listener);
        }
    }

    public void notifyRefreshListener() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DataRefreshListener listener : refreshListeners) {
                    listener.onRefresh();
                }
            }
        });
    }
}
