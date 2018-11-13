package com.qingqing.project.offline.groupchat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.qingqingbase.ui.BaseActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.ui.AbstractFragment;
import com.qingqing.base.ui.FragmentAssist;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

import java.util.Date;

/**
 * 群公告详情页面，普通成员可查看，管理员可以查看且编辑
 */

public class BaseGroupChatAnnouncementActivity extends BaseActivity implements View.OnClickListener {

    /*View的成员变量开始*/
    protected ImageView mIvBack;
    protected TextView mTvLeft;
    protected TextView mTvTitle;
    protected TextView mTvRight;
    protected RelativeLayout mRlAdminInfoContainer;
    protected FrameLayout mFlAvatarContainer;
    protected AsyncImageViewV2 mIvAvatar;
    protected ImageView mIvAdminTag;
    protected TextView mTvLastEditUserName;
    protected TextView mTvLastEditTime;
    protected FrameLayout mFlContainer;
    /*View的成员变量结束*/

    @NonNull
    protected int[] chatUserRole;
    @NonNull
    protected String mChatGroupId;

    protected boolean isInitialContentEmpty;

    protected ImProto.ChatGroupAnnounceDetailResponse mResponse;

    protected BaseGroupChatAnnouncementDetailFragment mDetailFragment;
    protected BaseGroupChatAnnouncementEditFragment mEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.activity_base_group_chat_announcement);
        if (isInitialContentEmpty) {
            //拉起键盘
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        initView();
        addListeners();
        getGroupAnnouncementDetail();
        initFragments();
    }

    @CallSuper
    protected void initFragments() {
        mFragAssist.setGroupID(R.id.fl_container).setFragOPMode(FragmentAssist.FragOPMode.MODE_SWITCH);
        mDetailFragment = new BaseGroupChatAnnouncementDetailFragment();
        mEditFragment = new BaseGroupChatAnnouncementEditFragment();
    }

    protected void getGroupAnnouncementDetail() {
        ImProto.SimpleChatGroupIdRequest request = new ImProto.SimpleChatGroupIdRequest();
        request.chatGroupId = mChatGroupId;
        newProtoReq(CommonUrl.CHAT_GROUP_ANNOUNCE_INFO.url())
                .setSendMsg(request)
                .setRspListener(new ProtoListener(ImProto.ChatGroupAnnounceDetailResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        mResponse = (ImProto.ChatGroupAnnounceDetailResponse) result;
                        if (mResponse.opsUserInfo == null) {
                            hideUserInfo(); //第一次进来的话，lastModifyUser是空的，顶部头像那些要隐藏掉
                        } else {
                            updateUI();
                        }
                        if (TextUtils.isEmpty(mResponse.groupAnnounce)) {
                            showEditPage(); //如果是空的话进编辑页面
                            mTvRight.setTextColor(ContextCompat.getColor(mTvRight.getContext(), R.color.gray_dark));
                        } else {
                            showDetailPage(); //不为空的话进详情页面
                        }
                    }
                }).req();
    }

    private void hideUserInfo() {
        mRlAdminInfoContainer.setVisibility(View.GONE);
    }

    /**
     * 更新头像什么的
     */
    @CallSuper
    protected void updateUI() {
        if (mResponse != null) {
            UserProto.ChatUserInfo info = mResponse.opsUserInfo;
            if (info != null) {
                if (mRlAdminInfoContainer.getVisibility() != View.VISIBLE) {
                    mRlAdminInfoContainer.setVisibility(View.VISIBLE);
                }
                mIvAvatar.setImageUrl(ImageUrlUtil.getHeadImg(info.newHeadImage), LogicConfig.getDefaultHeadIcon(info.sex));
                mTvLastEditUserName.setText(info.nick);
                mTvLastEditTime.setText(DateUtils.ymdhmSdf.format(new Date(mResponse.modifyTime)));
            }
        }
    }

    /**
     * 切换到编辑页面，调用前确认amIAdmin
     */
    @CallSuper
    protected void showEditPage() {
        if (mResponse != null && mEditFragment != null) {
            LimitEditText et = mEditFragment.getEtContent();
            if (et == null) {
                Bundle bundle = new Bundle();
                bundle.putString(BaseParamKeys.PARAM_STRING_GROUP_CHAT_ANNOUNCEMENT_CONTENT, mResponse.groupAnnounce);
                mEditFragment.setArguments(bundle);
            } else {
                et.setText(mResponse.groupAnnounce);
            }
            mFragAssist.switchTo(mEditFragment);
            if (et != null) {
                forceShowKeyBoard(et);
            }
            mTvLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.GONE);
            mTvRight.setVisibility(View.VISIBLE);
            mTvRight.setText(R.string.text_done);
        }
    }

    private void forceShowKeyBoard(LimitEditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(editText)) {
            return;
        }
        imm.toggleSoftInput(0,InputMethodManager.SHOW_FORCED);
    }

    /**
     * 切换到详情页面
     */
    @CallSuper
    protected void showDetailPage() {
        boolean amIAdmin = amIAdmin();
        if (mResponse != null && mDetailFragment != null) {
            TextView textView = mDetailFragment.getTextView();
            if (textView == null) {
                Bundle bundle = new Bundle();
                bundle.putString(BaseParamKeys.PARAM_STRING_GROUP_CHAT_ANNOUNCEMENT_CONTENT, mResponse.groupAnnounce);
                mDetailFragment.setArguments(bundle);
            } else {
                textView.setText(mResponse.groupAnnounce);
            }
            mFragAssist.switchTo(mDetailFragment);
            // 顶部Toolbar的一些更改
            mTvLeft.setVisibility(View.GONE);
            mIvBack.setVisibility(View.VISIBLE);
            if (amIAdmin) {
                mTvRight.setVisibility(View.VISIBLE);
                mTvRight.setText(R.string.text_edit);
                UIUtil.hideSoftInput(this);
            } else {
                mTvRight.setVisibility(View.GONE);
            }
        }
    }


    protected void handleClickAction(boolean amIadmin) {
        if (amIadmin) {
            if (mResponse != null) {
                AbstractFragment topFragment = mFragAssist.getTopFragment();
                if (mEditFragment!=null&&topFragment == mEditFragment) {
                    LimitEditText editText = mEditFragment.getEtContent();
                    if (editText != null) {
                        String content = editText.getText().toString();
                        boolean isContentEmpty = TextUtils.isEmpty(content.replaceAll(" ", ""));
                        if (mResponse.opsUserInfo == null) { //首次发布
                            if (!isContentEmpty&& content.length() <= 2000) {
                                showConfirmPublishDialog();
                            }
                            // 首次发布，但内容为空，不允许发布
                        } else if (mResponse.groupAnnounce != null && !isContentEmpty&& content.length() <= 2000) { //曾经发布过，新的内容不为空，长度小于2000
                            showConfirmPublishDialog();//右上角的完成点击，产品说这时候不去比较内容是否发生了变化
                        }
                    }
                } else if (topFragment == mDetailFragment) {
                    showEditPage();
                }
            }
        }
    }

    @CallSuper
    protected void initView() {
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mTvLeft = (TextView) findViewById(R.id.tv_left);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvRight = (TextView) findViewById(R.id.tv_right);
        mRlAdminInfoContainer = (RelativeLayout) findViewById(R.id.rl_admin_info_container);
        mFlAvatarContainer = (FrameLayout) findViewById(R.id.fl_avatar_container);
        mIvAvatar = (AsyncImageViewV2) findViewById(R.id.iv_avatar);
        mIvAdminTag = (ImageView) findViewById(R.id.iv_admin_tag);
        mTvLastEditUserName = (TextView) findViewById(R.id.tv_last_edit_user_name);
        mTvLastEditTime = (TextView) findViewById(R.id.tv_last_edit_time);
        mFlContainer = (FrameLayout) findViewById(R.id.fl_container);
    }

    @CallSuper
    protected void addListeners() {
        mIvBack.setOnClickListener(this);
        if (amIAdmin()) {
            mTvRight.setOnClickListener(this);
            mTvLeft.setOnClickListener(this);
        }
    }

    @CallSuper
    protected void initData() {
        Intent intent = getIntent();
        chatUserRole = intent.getIntArrayExtra(BaseParamKeys.PARAM_INT_CHAT_GROUP_USER_ROLE);
        mChatGroupId = intent.getStringExtra(BaseParamKeys.PARAM_STRING_CHAT_GROUP_ID);
       isInitialContentEmpty = intent.getBooleanExtra(BaseParamKeys.PARAM_BOOLEAN_IS_GROUP_CHAT_CONTENT_EMPTY, false);
    }





    @Override
    public void onClick(View v) {
    }

    protected boolean amIAdmin() {
        if (chatUserRole != null) {
            for (int i = 0; i < chatUserRole.length; i++) {
                if (chatUserRole[i] == UserProto.ChatGroupUserRole.manager_chat_group_user_role || chatUserRole[i] == UserProto.ChatGroupUserRole.owner_chat_group_user_role) {
                    return true;
                }
            }
        }
        return false;
    }

    protected CompatDialog mConfirmExitEditDialog;

    protected void showConfirmExitingEditDialog() {
        if (mConfirmExitEditDialog == null) {
            mConfirmExitEditDialog = new CompatDialog.Builder(this, R.style.Theme_Dialog_Compat_NoTitleAlert)
                    .setMessage(R.string.text_exit_current_edit)
                    .setPositiveButton(R.string.text_exit,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface,
                                                    int i) {
                                    dialogInterface.dismiss();
                                    showDetailPage();
                                }
                            })
                    .setNegativeButton(R.string.text_proceed_edit,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface,
                                                    int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setCancelable(true).create();
        }
        mConfirmExitEditDialog.show();
    }

    protected CompatDialog mConfirmPublishDialog;

    protected void showConfirmPublishDialog() {
        if (mConfirmPublishDialog == null) {
            mConfirmPublishDialog = new CompatDialog.Builder(this, R.style.Theme_Dialog_Compat_NoTitleAlert)
                    .setMessage(R.string.text_confirm_publish_announce)
                    .setPositiveButton(R.string.text_publish,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface,
                                                    int i) {
                                    dialogInterface.dismiss();
                                    sendRequest();
                                }
                            })
                    .setNegativeButton(R.string.text_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface,
                                                    int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setCancelable(true).create();
        }
        mConfirmPublishDialog.show();
    }




    @Override
    public void finish() {
        UIUtil.hideSoftInput(this);
        super.finish();
    }

    protected void sendRequest() {
        if (TextUtils.isEmpty(mChatGroupId) || mEditFragment == null) {
            return;
        }
        LimitEditText editText = mEditFragment.getEtContent();
        if (editText == null) {
            return;
        }
        String content = editText.getText().toString();
        ImProto.UpdateChatGroupAnnounceRequest request = new ImProto.UpdateChatGroupAnnounceRequest();
        request.chatGroupId = mChatGroupId;
        request.groupAnnounce = content;
        newProtoReq(CommonUrl.CHAT_GROUP_ANNOUNCE_EDIT.url())
                .setSendMsg(request)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        dismissProgressDialogDialog();
                        ToastWrapper.showWithIcon(R.string.text_successfully_published, R.drawable.icon_toast_yes);
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public boolean onDealError(int errorCode, Object result) {
                        dismissProgressDialogDialog();
                        return super.onDealError(errorCode, result);
                    }
                }).req();
        showProgressDialogDialog(true, "上传中");
    }

    @Override
    public void onBackPressed() {
        boolean amIAdmin = amIAdmin();
        if (amIAdmin) {
            AbstractFragment top = mFragAssist.getTopFragment();
            if (top == null) {
                super.onBackPressed();
            }
            if (top == mEditFragment) {
                if (mResponse != null) {
                    if (mResponse.opsUserInfo == null) {
                        super.onBackPressed();
                    } else if (mResponse.groupAnnounce != null && mEditFragment.getEtContent() != null && mResponse.groupAnnounce.equals(mEditFragment.getEtContent().getText().toString())) {
                        showDetailPage(); //返回时判断是否有内容变更，无变更直接返回
                    } else {
                        showConfirmExitingEditDialog();
                    }
                }
            } else if (top == mDetailFragment) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSetStatusBarMode() {
        setStatusBarColor(R.color.white_light, true);
    }


    public void setStatusBarColor(@ColorRes int statusBarColor, boolean moveContentViewDownwards) {

        if (!LogicConfig.isMDStatusBarStyleValid())
            return;

        // 4.4 以下不支持
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return;
        ViewGroup contentView = (ViewGroup) ((ViewGroup) findViewById(
                android.R.id.content)).getChildAt(0);
        if (moveContentViewDownwards) {
            if (contentView != null && !contentView.getFitsSystemWindows()) {
                contentView.setFitsSystemWindows(true);
            }
        }
        @ColorInt
        int colorValue = ContextCompat.getColor(this, statusBarColor);
        final boolean needDarkText = isStatusBarBgColorNeedDarkText(colorValue);
        final boolean supportSetText = autoSetStatusBarTextColor(needDarkText);
        if (needDarkText && !supportSetText) {
                colorValue = ContextCompat.getColor(this, R.color.transparent_dark);
        }
        if (contentView != null
                && contentView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 5.0以上使用系统提供的方法
                window.addFlags(
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.setStatusBarColor(colorValue);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                ViewGroup decorView = (ViewGroup) window.getDecorView();
                if (statusBarView == null) {
                    statusBarView = new View(this);
                    statusBarView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            AppUtil.getStatusBarHeight()));
                    statusBarView.setBackgroundColor(colorValue);
                    decorView.addView(statusBarView);
                }
                else {
                    statusBarView.setBackgroundColor(colorValue);
                }
            }
        }
        AppUtil.setStatusBarTextDarkOnMarshMallow(this, needDarkText);
    }

    protected View statusBarView;

    private boolean isStatusBarBgColorNeedDarkText(int bgColor) {
        int red = Color.red(bgColor), blue = Color.blue(bgColor),
                green = Color.green(bgColor);
        // 三个都大于0xcc 或者 一个为0两个大于0xcc
        int zeroCount = 0, ccCount = 0;
        if (red >= 0xcc)
            ++ccCount;
        if (blue >= 0xcc)
            ++ccCount;
        if (green >= 0xcc)
            ++ccCount;

        if (red == 0)
            ++zeroCount;
        if (blue == 0)
            ++zeroCount;
        if (green == 0)
            ++zeroCount;

        return ccCount == 3 || (ccCount == 2 && zeroCount == 1);
    }
    private boolean autoSetStatusBarTextColor(boolean needDark) {
        return setStatusBarTextColor(needDark);
    }


}
