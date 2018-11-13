package com.qingqing.base.nim.ui.lecture;

import java.util.ArrayList;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.LectureProto;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.adapter.KeyNotSpeakerAdapter;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.constant.ThemeConstant;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.im.ChatRoomModel;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.utils.Constants;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.AtMostListView;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.setting.SimpleSettingItem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * Created by huangming on 2016/8/27.
 */
public class BaseLectureSettingsActivity extends BaseActionBarActivity
        implements View.OnClickListener {
    public final String TAG = "ChatSettingsActivity";
    private TextView mMeIdentity;
    private SimpleSettingItem mInvitationCodeItem;
    private SimpleSettingItem mSetGagItem;
    private KeyNotSpeakerAdapter mAdapter = null;
    private AtMostListView mListView;
    private CompatDialog mDialog = null;
    private String mInvitationCode;
    private String mQingQingLectureId;
    private String mChatRoomId;
    private int mCurrentRole;
    private TextView mCloseLecture;
    private TextView mExpert;
    private CompatDialog mUserDetailDialog;
    private String mOnClickQingQingUserId;
    private ArrayList<UserProto.SimpleUserInfoV2> mKeyNoteSpeakerList = new ArrayList<>();
    // private LoginAndRegDialog mRegDialog = new LoginAndRegDialog(
    // ChatSettingsActivity.this);
    OnUserDetailDialogClickListener mOnUserDetailDialogClickListener = new OnUserDetailDialogClickListener() {
        @Override
        public void onExpertCancel() {
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
            }
        }
        
        @Override
        public void onStudentMute(boolean isMute) {
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
            }
        }
        
        @Override
        public void onTeacherAttention() {
            // 收藏老师
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (mOnPageRedirectListener != null) {
                    mOnPageRedirectListener.onTeacherAttention(mOnClickQingQingUserId);
                }
                
            }
        }
        
        @Override
        public void onTeacherEnterHomePage() {
            // 老师主页
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (mOnPageRedirectListener != null) {
                    mOnPageRedirectListener.onEnterTeacherHome(mOnClickQingQingUserId);
                }
            }
        }
        
        @Override
        public void onAssistEnterHomePage() {
            // 助教详情
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (mOnPageRedirectListener != null) {
                    mOnPageRedirectListener.onEnterAssistantHome(mOnClickQingQingUserId);
                }
            }
        }
        
        @Override
        public void onExit() {
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
            }
        }

        @Override
        public void onExitButton() {
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
            }
        }

    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_settings);
        initView();
        reqChatRoomDetail();
    }

    public void setOnPageRedirectListener(BaseLectureFragment.OnPageRedirectListener onPageRedirectListener) {
        this.mOnPageRedirectListener = onPageRedirectListener;
    }

    private BaseLectureFragment.OnPageRedirectListener mOnPageRedirectListener;
    private void initView() {
        mQingQingLectureId = getIntent()
                .getStringExtra(BaseParamKeys.PARAM_STRING_LECTURE_ID);
        mChatRoomId = getIntent().getStringExtra(BaseParamKeys.PARAM_STRING_CHAT_ROOM_ID);
        mCurrentRole = getIntent().getIntExtra(
                BaseParamKeys.PARAM_INT_CHAT_ROOM_ROLE_TYOE,
                ImProto.ChatRoomRoleType.general_chat_room_role_type);
        mMeIdentity = (TextView) findViewById(R.id.me_identity);
        mExpert = (TextView) findViewById(R.id.expert);
        
        mCloseLecture = (TextView) findViewById(R.id.end_forum);
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
            mCloseLecture.setBackground(getResources().getDrawable(
                    R.drawable.shape_corner_rect_transparent_solid_blue_stroke));
        }
        else if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
            mCloseLecture.setBackground(getResources().getDrawable(
                    R.drawable.shape_big_corner_rect_transparent_solid_orange_stroke));
        }
        mCloseLecture.setTextColor(ThemeConstant.getThemeColor(this));
        
        mInvitationCodeItem = (SimpleSettingItem) findViewById(R.id.invitation_code);
        mSetGagItem = (SimpleSettingItem) findViewById(R.id.set_gag);
        mListView = (AtMostListView) findViewById(R.id.lv_assistant_expert);
        mInvitationCodeItem.setOnClickListener(this);
        findViewById(R.id.end_forum).setOnClickListener(this);
        mSetGagItem.setOnClickListener(this);
        mAdapter = new KeyNotSpeakerAdapter(BaseLectureSettingsActivity.this,
                mKeyNoteSpeakerList);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                if (position >= 0 && position < mKeyNoteSpeakerList.size()) {
                    UserProto.SimpleUserInfoV2 userInfoV2 = mKeyNoteSpeakerList
                            .get(position);
                    mOnClickQingQingUserId = userInfoV2.qingqingUserId;
                    if (couldOperateUI()) {
                        goToRoleMainPage(userInfoV2);
                    }
                }
            }
        });
        mListView.setAdapter(mAdapter);
        mMeIdentity.setText(getRoleByType(mCurrentRole));
    }
    
    @Override
    public void onClick(View v) {
        
        if (mInvitationCodeItem == v) {
            getInvitationCode(true);
        }
        else if (v.getId() == R.id.set_gag) {
            if (!TextUtils.isEmpty(mQingQingLectureId)) {
                Intent intent = new Intent();
                intent.setClass(this, LectureSetupGagActivity.class);
                intent.putExtra(BaseParamKeys.PARAM_STRING_LECTURE_ID,
                        mQingQingLectureId);
                intent.putExtra(BaseParamKeys.PARAM_STRING_CHAT_ROOM_ID, mChatRoomId);
                startActivity(intent);
            }
        }
        else if (v.getId() == R.id.end_forum) {
            getInvitationCode(false);
        }
    }
    
    /**
     * 获取邀请码
     */
    private void getInvitationCode(final boolean isInvitationCode) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.view_invitation_code, null);
        if (isInvitationCode) {
            view.findViewById(R.id.confirm_end_forum).setVisibility(View.GONE);
            view.findViewById(R.id.invitation_code).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.confirm_end_forum).setVisibility(View.VISIBLE);
            view.findViewById(R.id.invitation_code).setVisibility(View.GONE);
        }
        mDialog = new CompatDialog.Builder(BaseLectureSettingsActivity.this,
                R.style.Theme_Dialog_Compat_Alert)
                        .setPositiveButton(getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,
                                            int i) {
                                        LimitEditText invitationCode;
                                        if (isInvitationCode) {
                                            invitationCode = (LimitEditText) mDialog
                                                    .getCustomView()
                                                    .findViewById(R.id.invitation_code);
                                            String code = invitationCode.getText()
                                                    .toString();
                                            if (TextUtils.isEmpty(code)) {
                                                ToastWrapper.show(getString(
                                                        R.string.please_input_invitation_code));
                                                return;
                                            }
                                            else {
                                                mInvitationCode = code;
                                                Logger.i("invitationcode = "
                                                        + mInvitationCode);
                                                UIUtil.closeInputManager(invitationCode);
                                                dialogInterface.dismiss();
                                                changeChatRole();
                                            }
                                        }
                                        else {
                                            dialogInterface.dismiss();
                                            closeLecture();
                                        }
                                        
                                    }
                                })
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,
                                            int i) {
                                        dialogInterface.dismiss();
                                        
                                    }
                                })
                        .setCustomView(view).create();
        mDialog.show();
    }
    
    /**
     * 获取聊天室详情
     */
    public void reqChatRoomDetail() {
        if (TextUtils.isEmpty(mQingQingLectureId)) {
            return;
        }
        mKeyNoteSpeakerList.clear();
        LectureProto.SimpleQingQingLectureIdRequest request = new LectureProto.SimpleQingQingLectureIdRequest();
        request.qingqingLectureId = mQingQingLectureId;
        newProtoReq(CommonUrl.LECTURE_CHAT_ROOM_DETAIL.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(
                        LectureProto.LectureChatRoomDetailResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        LectureProto.LectureChatRoomDetailResponse response = (LectureProto.LectureChatRoomDetailResponse) result;
                        if (couldOperateUI()) {
                            if (response.lectureMcs.length > 0) {
                                mExpert.setVisibility(View.VISIBLE);
                                for (UserProto.SimpleUserInfoV2 simpleUserInfoV2 : response.lectureMcs) {
                                    mKeyNoteSpeakerList.add(simpleUserInfoV2);
                                }
                            }
                            else {
                                mExpert.setVisibility(View.GONE);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }).req();
    }
    
    /**
     * 关闭讲堂
     */
    public void closeLecture() {
        if (TextUtils.isEmpty(mQingQingLectureId)) {
            return;
        }
        LectureProto.SimpleQingQingLectureIdRequest request = new LectureProto.SimpleQingQingLectureIdRequest();
        request.qingqingLectureId = mQingQingLectureId;
        newProtoReq(CommonUrl.LECTURE_CHAT_ROOM_DISMISS.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        Logger.w(TAG, "close lecture success");
                        ToastWrapper.show(getString(R.string.close_lecture_success));
                        setResult(Constants.RESULT_CODE_FINISH_LECTURE);
                        finish();
                        ChatRoomModel model = ChatRoomModel.getModel(mChatRoomId);
                        if (model != null) {
                            model.finishChatRoom();
                        }
                    }
                    
                    @Override
                    public boolean onDealError(int errorCode, Object result) {
                        ToastWrapper.show(getErrorHintMessage(
                                getString(R.string.close_lecture_fail)));
                        return true;
                    }
                }).req();
    }
    
    /**
     * 用户身份切换
     */
    public void changeChatRole() {
        if (TextUtils.isEmpty(mQingQingLectureId) || TextUtils.isEmpty(mInvitationCode)) {
            return;
        }
        LectureProto.UserLectureRoleChangeRequest request = new LectureProto.UserLectureRoleChangeRequest();
        request.qingqingLectureId = mQingQingLectureId;
        request.inviteCode = mInvitationCode;
        newProtoReq(CommonUrl.LECTURE_CHAT_ROOM_CHANGE_ROLE.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(
                        LectureProto.UserLectureRoleChangeResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        LectureProto.UserLectureRoleChangeResponse response = (LectureProto.UserLectureRoleChangeResponse) result;
                        if (couldOperateUI()) {
                            int type = response.currentRole;
                            String role = getRoleByType(type);
                            UserProto.SimpleUserInfoV2 infoV2 = response.changeUserInfo;
                            Intent intent = new Intent();
                            intent.putExtra(Constants.EXTRA_CHANGE_ROLE_TYPE, type);
                            intent.putExtra(Constants.EXTRA_CHANGE_ROLE_USER_INFO,
                                    infoV2);
                            setResult(Constants.RESULT_CODE_CHANGE_ROLE, intent);
                            
                            // ChatRoomModel model =
                            // ChatRoomModel.getModel(mChatRoomId);
                            // if (model != null) {
                            // model.changeRole(response.changeUserInfo != null
                            // ? response.changeUserInfo
                            // : AccountOption.INSTANCE()
                            // .getSelfSimpleUserInfo(),
                            // type);
                            // }
                            if (!TextUtils.isEmpty(role)) {
                                ToastWrapper.show(getString(R.string.become_role, role));
                                mMeIdentity.setText(role);
                            }
                        }
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                                            int errorCode, Object result) {
                        Logger.w(TAG, "char room change role error", error);
                        if (errorCode == 1201) {
                            ToastWrapper
                                    .show(getString(R.string.invitation_code_not_exist));
                        }
                        else {
                            super.onDealError(error, isParseOK, errorCode, result);
                        }
                    }
                }).req();
    }
    
    /**
     * 根据用户身份显示身份信息
     *
     * @param type
     */
    private String getRoleByType(int type) {
        String role = "";
        showCloseLecture(false);
        switch (type) {
            case ImProto.ChatRoomRoleType.admin_chat_room_role_type:
                role = getString(R.string.admin);
                showCloseLecture(true);
                mSetGagItem.setVisibility(View.VISIBLE);
                break;
            case ImProto.ChatRoomRoleType.mc_chat_room_role_type:
                role = getString(R.string.expert);
                mSetGagItem.setVisibility(View.VISIBLE);
                showCloseLecture(true);
                break;
            case ImProto.ChatRoomRoleType.general_chat_room_role_type:
                role = getString(R.string.audience);
                break;
        }
        return role;
    }
    
    /**
     * 是否展示关闭讲堂按钮
     *
     * @param isShow
     */
    private void showCloseLecture(boolean isShow) {
        if (mCloseLecture != null) {
            mCloseLecture.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * 查看用户详情弹框
     */
    private void goToRoleMainPage(UserProto.SimpleUserInfoV2 userInfoV2) {
        final int targetRoleType = ImProto.ChatRoomRoleType.mc_chat_room_role_type;
        if (LectureConfig.isShowAvatarDialog(getCurrentRoleType(), targetRoleType)) {
            if (LectureConfig.isNeedReqUserInfoToShowDialog(targetRoleType)) {
                LectureConfig.reqLectureUserInfo(userInfoV2.qingqingUserId,
                        new LectureConfig.LectureUserInfoCallBack() {
                            @Override
                            public void onSuccess(LectureProto.Lecturer lecturer) {
                                if (couldOperateUI()) {
                                    updateUserDetailDialog(targetRoleType, lecturer);
                                }
                            }
                            
                            @Override
                            public void onFail() {
                                
                            }
                        });
            }
            else {
                LectureProto.Lecturer lecturer = new LectureProto.Lecturer();
                lecturer.userInfo = userInfoV2;
                updateUserDetailDialog(targetRoleType, lecturer);
            }
            
        }
    }
    
    private void updateUserDetailDialog(int targetRoleType,
            LectureProto.Lecturer lecturer) {
        mUserDetailDialog = LectureConfig.updateUserDetailDialog(
                BaseLectureSettingsActivity.this, mUserDetailDialog,
                mOnUserDetailDialogClickListener, targetRoleType, false, lecturer);
        mUserDetailDialog.show();
    }
    
    private int getCurrentRoleType() {
        int type = ImProto.ChatRoomRoleType.general_chat_room_role_type;
        switch (mCurrentRole) {
            case ImProto.ChatRoomRoleType.admin_chat_room_role_type:
                type = ImProto.ChatRoomRoleType.admin_chat_room_role_type;
                break;
            case ImProto.ChatRoomRoleType.mc_chat_room_role_type:
                type = ImProto.ChatRoomRoleType.mc_chat_room_role_type;
                break;
        }
        return type;
    }
    
    public String getLectureId() {
        return mQingQingLectureId;
    }
}
