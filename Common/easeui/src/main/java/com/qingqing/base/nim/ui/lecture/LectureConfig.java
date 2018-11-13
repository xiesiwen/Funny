package com.qingqing.base.nim.ui.lecture;

import java.util.ArrayList;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.LectureProto;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.view.DialogLectureUserDetail;

import android.content.Context;
import android.text.TextUtils;

/**
 * 学堂状态等 Created by lihui on 2016/5/27.
 */
public class LectureConfig {

    /**
     * STATUS 状态值对应了统计中的值，修改时需注意对应
     * </p>
     *
     * @see <a href="http://wiki.changingedu.com/pages/viewpage.action?pageId=2634855">5.0.5student端页面级日志</a>
     */
    public static final int LECTURE_STATUS_PREPARE = 1;
    public static final int LECTURE_STATUS_LIVE = 2;
    public static final int LECTURE_STATUS_END = 3;
    // 讲堂提前开始时间(ms)
    public static final int DIFF_TIMEMILLIS_AHEAD_OF_LECTURE = 10 * 60 * 1000;

    private static final String TAG = "lecture";
    private static final int CHAT_ROOM_PRIVILEGE_HIGH = 99;
    private static final int CHAT_ROOM_PRIVILEGE_LOW = 95;


    /**
     * 获取聊天室用户信息
     *
     * @param qingqingUserId
     */
    public static void reqLectureUserInfo(String qingqingUserId,
                                          final LectureUserInfoCallBack callBack) {
        if (TextUtils.isEmpty(qingqingUserId)) {
            return;
        }
        if (callBack != null) {
            UserProto.SimpleQingQingUserIdRequest request = new UserProto.SimpleQingQingUserIdRequest();
            request.qingqingUserId = qingqingUserId;
            new ProtoReq(CommonUrl.LECTURE_USER_INFO_URL.url())
                    .setSendMsg(request)
                    .setRspListener(
                            new ProtoListener(LectureProto.LecturerResponse.class) {
                                @Override
                                public void onDealResult(Object result) {
                                    LectureProto.LecturerResponse response = (LectureProto.LecturerResponse) result;
                                    LectureProto.Lecturer lecturer = response
                                            .lecturer;
                                    if (lecturer != null) {
                                        callBack.onSuccess(lecturer);
                                    } else {
                                        callBack.onFail();
                                    }
                                }

                                @Override
                                public boolean onDealError(int errorCode, Object result) {
                                    callBack.onFail();
                                    return true;
                                }
                            }).req();
        }
    }

    /**
     * 点击头像后是否弹窗
     *
     * @param userRoleType   自己的讲堂角色类型
     * @param targetRoleType 点击用户的讲堂角色类型
     * @return 是否弹窗
     */
    public static boolean isShowAvatarDialog(int userRoleType,
                                             int targetRoleType) {
        /**
         * 权限表
         *
         * userRole|
         *
         * | admin | mc | general | guest | unknown | targetRole
         *
         * --------------------------------------------------
         *
         * admin | X | 3 | 1 | 1 | 1 |
         *
         * --------------------------------------------------
         *
         * mc | X | 3 | 1 | 1 | 1 |
         *
         * --------------------------------------------------
         *
         * general | X | 3 | X | X | X |
         *
         * --------------------------------------------------
         *
         * guest | X | 3 | X | X | X |
         *
         * --------------------------------------------------
         *
         * unknown | X | 3 | X | X | X |
         *
         *
         * 注： 1.unknown 为将来新增的未知类型 2."3"代表根据讲师的 userType 来判断弹框 3."1"代表弹出禁言框
         */

        if (targetRoleType == ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
            return false;
        }

        if (targetRoleType == ImProto.ChatRoomRoleType.mc_chat_room_role_type) {
            return true;
        }

        int privilege = getUserDetailDialogPrivilege(userRoleType);

        return privilege == CHAT_ROOM_PRIVILEGE_HIGH;

    }

    /**
     * 点击头像弹窗时是否需要请求数据
     *
     * @param targetRoleType 点击用户的讲堂角色类型
     * @return 是否需要请求
     */
    public static boolean isNeedReqUserInfoToShowDialog(
            int targetRoleType) {
        return targetRoleType == ImProto.ChatRoomRoleType.mc_chat_room_role_type;
    }

    /**
     * 根据自己的 roleType, 获取点击头像的优先级等级
     *
     * @param userRoleType 自己的 roleType
     * @return 决定点击头像弹框的优先级
     */
    public static int getUserDetailDialogPrivilege(int userRoleType) {
        if (userRoleType == ImProto.ChatRoomRoleType.admin_chat_room_role_type
                || userRoleType == ImProto.ChatRoomRoleType.mc_chat_room_role_type) {
            return CHAT_ROOM_PRIVILEGE_HIGH;
        } else {
            return CHAT_ROOM_PRIVILEGE_LOW;
        }
    }

    /**
     * 更新用户详情的弹窗
     *
     * @param context          Activity
     * @param userDetailDialog 弹窗对象
     * @param listener         弹窗的点击监听
     * @param targetRoleType   目标用户的 roleType
     * @param isMute
     * @param lecturer         目标用户的 Lecturer 信息  @return 更新后的弹窗对象
     */
    public static CompatDialog updateUserDetailDialog(Context context,
                                                      CompatDialog userDetailDialog,
                                                      OnUserDetailDialogClickListener listener,
                                                      int targetRoleType,
                                                      boolean isMute,
                                                      LectureProto.Lecturer lecturer) {
        if (userDetailDialog == null) {
            DialogLectureUserDetail dialog = new DialogLectureUserDetail(context);
            dialog.setLecturer(lecturer, targetRoleType, isMute, listener);

            userDetailDialog = new CompatDialog.Builder(context, R.style.Theme_Dialog_Compat_ArcImage).setCustomView(dialog).create();
        } else {
            DialogLectureUserDetail dialog = (DialogLectureUserDetail) userDetailDialog.getCustomView();
            dialog.setLecturer(lecturer, targetRoleType, isMute, listener);
        }

        return userDetailDialog;
    }

    /**
     * 设置单个用户禁言或解除禁言
     *
     * @param qingQingUserId
     */
    public static void allowOrStopTalkUser(boolean isMute, String qingQingUserId, String lectureId,
                                           final LectureAllowOrStopUserTalkCallBack callBack) {
        if (TextUtils.isEmpty(qingQingUserId)) {
            return;
        }
        UserProto.QingQingUser user = new UserProto.QingQingUser();
        user.qingqingUserId = qingQingUserId;
        LectureProto.LectureTalkOperationRequest request = new LectureProto.LectureTalkOperationRequest();
        request.qingqingLectureId = lectureId;
        request.userToStop = user;
        new ProtoReq(isMute ? CommonUrl.LECTURE_ALLOW_TALK_URL.url() : CommonUrl.LECTURE_STOP_TALK_URL.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        if (callBack != null) {
                            callBack.onSuccess();
                        }
                    }

                    @Override
                    public boolean onDealError(int errorCode, Object result) {
                        if (callBack != null) {
                            callBack.onFail();
                        }
                        return true;
                    }
                }).req();
    }
    
    /**
     * 获取最高权限的角色类型
     */
    public static int getHighestRoleType(ArrayList<Integer> roleTypeList) {
        int roleType = ImProto.ChatRoomRoleType.guest_chat_room_role_type;
        if (roleTypeList != null) {
            for (int i = 0; i < roleTypeList.size(); i++) {
                if (roleTypeList
                        .get(i) == ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    roleType = ImProto.ChatRoomRoleType.admin_chat_room_role_type;
                }
                else if (roleTypeList
                        .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type
                        && roleType != ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    roleType = ImProto.ChatRoomRoleType.mc_chat_room_role_type;
                }
                else if (roleTypeList
                        .get(i) == ImProto.ChatRoomRoleType.general_chat_room_role_type
                        && (roleType != ImProto.ChatRoomRoleType.admin_chat_room_role_type
                                && roleType != ImProto.ChatRoomRoleType.mc_chat_room_role_type)) {
                    roleType = ImProto.ChatRoomRoleType.general_chat_room_role_type;
                }
                else if (roleTypeList
                        .get(i) == ImProto.ChatRoomRoleType.unknown_chat_room_role_type) {
                    roleType = ImProto.ChatRoomRoleType.unknown_chat_room_role_type;
                }
                
            }
        }
        
        return roleType;
    }
    
    public interface LectureUserInfoCallBack {
        void onSuccess(LectureProto.Lecturer lecturer);

        void onFail();
    }

    public interface LectureAllowOrStopUserTalkCallBack {
        void onSuccess();

        void onFail();
    }
}
