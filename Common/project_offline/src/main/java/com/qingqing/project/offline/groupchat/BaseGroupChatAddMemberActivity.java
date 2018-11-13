package com.qingqing.project.offline.groupchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.qingqingbase.ui.BaseActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

/**
 * 群聊天加人页面，从群聊天设置中点击加号跳转过来
 */

public class BaseGroupChatAddMemberActivity extends BaseActivity implements View.OnClickListener {

    protected String mChatGroupId;

    //View的成员变量开始
    protected LinearLayout mLlRoot;
    protected LimitEditText mEtSearch;
    protected ImageView mIvClear;
    protected TextView mTvCancel;
    protected RelativeLayout mRlSearchTeacherResult;
    protected AsyncImageViewV2 mIvAvatar;
    protected TextView mTvNickAndTeachAge;
    protected TextView mTvNickAndPhoneNum;
    protected TextView mTvAdd;
    protected TextView mTvUserNotExits;
    protected RelativeLayout mRlSearchHint;
    protected ImageView mIvAddMemberSearch;
    protected TextView mTvSearchPhoneNum;
    //View的成员变量结束

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.activity_add_group_chat_member);
        initView();
        addListener();
    }

    /**
     * 先于setContentView调用，用于从Intent里面拿东西
     */
    @CallSuper
    protected void initData() {
        Intent intent = getIntent();
        mChatGroupId = intent.getStringExtra(BaseParamKeys.PARAM_STRING_CHAT_GROUP_ID);
    }

    @CallSuper
    protected void initView() {
        mLlRoot = (LinearLayout) findViewById(R.id.ll_root);
        mEtSearch = (LimitEditText) findViewById(R.id.et_search);
        mIvClear = (ImageView) findViewById(R.id.iv_clear);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mRlSearchTeacherResult = (RelativeLayout) findViewById(R.id.rl_search_teacher_result);
        mIvAvatar = (AsyncImageViewV2) findViewById(R.id.iv_avatar);
        mTvNickAndTeachAge = (TextView) findViewById(R.id.tv_nick_and_teach_age);
        mTvNickAndPhoneNum = (TextView) findViewById(R.id.tv_nick_and_phone_num);
        mTvAdd = (TextView) findViewById(R.id.tv_add);
        mTvUserNotExits = (TextView) findViewById(R.id.tv_user_not_exits);
        mRlSearchHint = (RelativeLayout) findViewById(R.id.rl_search_hint);
        mIvAddMemberSearch = (ImageView) findViewById(R.id.iv_add_member_search);
        mTvSearchPhoneNum = (TextView) findViewById(R.id.tv_search_phone_num);

    }

    @CallSuper
    protected void addListener() {
        mTvCancel.setOnClickListener(this);
        mRlSearchHint.setOnClickListener(this);
        mTvAdd.setOnClickListener(this);
        mIvClear.setOnClickListener(this);
    }

    @CallSuper
    protected void addGroupMember(@NonNull String qingqingUserId) {
        if (TextUtils.isEmpty(qingqingUserId)||TextUtils.isEmpty(mChatGroupId)) {
            return;
        }
        ImProto.AddChatGroupMemberRequest addChatGroupMemberRequest = new ImProto.AddChatGroupMemberRequest();
        addChatGroupMemberRequest.chatGroupId = mChatGroupId;
        addChatGroupMemberRequest.qingqingUserIds = new String[]{qingqingUserId};
        addChatGroupMemberRequest.userRole = UserProto.ChatGroupUserRole.normal_chat_group_user_role;
        newProtoReq(CommonUrl.CHAT_GROUP_ADD_USER.url())
                .setSendMsg(addChatGroupMemberRequest)
                .setRspListener(
                        new ProtoListener(ImProto.AddChatGroupMemberResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                if (couldOperateUI()) {
                                    setResult(RESULT_OK);
                                    ToastWrapper.showWithIcon(R.string.text_add_success,R.drawable.icon_toast_yes);
                                    UIUtil.hideSoftInput(BaseGroupChatAddMemberActivity.this);
                                    finish();
                                }
                            }
                        }).req();
    }

    @Override
    public void onClick(View v) {
        if (!couldOperateUI()) {
            return;
        }
    }

    @CallSuper
    protected void showNoResult() {
        AppUtil.setVisibilityVisbleOrGoneForViewsArray(false, mRlSearchHint, mRlSearchTeacherResult);
        if (mTvUserNotExits.getVisibility() != View.VISIBLE) {
            mTvUserNotExits.setVisibility(View.VISIBLE);
        }
    }

    @CallSuper
    protected void showSearchResult() {
        AppUtil.setVisibilityVisbleOrGoneForViewsArray(false, mRlSearchHint, mTvUserNotExits);
        if (mRlSearchTeacherResult.getVisibility() != View.VISIBLE) {
            mRlSearchTeacherResult.setVisibility(View.VISIBLE);
        }
    }

    @CallSuper
    protected void showSearchHint(String text) {
        AppUtil.setVisibilityVisbleOrGoneForViewsArray(false, mTvUserNotExits, mRlSearchTeacherResult);
        AppUtil.setVisibilityVisbleOrGoneForViewsArray(true, mRlSearchHint);
    }

    @CallSuper
    protected void hideResults() {
        AppUtil.setVisibilityVisbleOrGoneForViewsArray(false, mTvUserNotExits, mRlSearchHint, mRlSearchTeacherResult);
    }



    @Override
    public void onSetStatusBarMode() {
        setStatusBarColor(R.color.white, true);
    }
}
