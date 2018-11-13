package com.qingqing.base.nim.ui.lecture;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.LectureProto;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.bean.UserBehaviorLogExtraData;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.SPWrapper;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.dialog.CompDefaultDialogBuilder;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.im.Constant;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.LectureCoordinationListener;
import com.qingqing.base.nim.LectureCoordinator;
import com.qingqing.base.nim.domain.ChatManager;
import com.qingqing.base.nim.domain.ChatRole;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.domain.spec.ExpertRoleSpec;
import com.qingqing.base.nim.ui.BaseChatRoomFragment;
import com.qingqing.base.nim.utils.ChatContactUtils;
import com.qingqing.base.nim.utils.Constants;
import com.qingqing.base.nim.utils.MessageUtils;
import com.qingqing.base.nim.view.ChatExtendMenuItem;
import com.qingqing.base.share.ShareShow;
import com.qingqing.base.spec.Spec;
import com.qingqing.base.task.LimitingNumberTask;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.utils.UrlUtil;
import com.qingqing.base.view.PPTView;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.helpcover.HelpCoverShow;
import com.qingqing.base.view.ptr.OnRefreshListener;

import java.util.HashMap;

/**
 * Created by huangming on 2016/8/17.
 */
public class BaseLectureFragment extends BaseChatRoomFragment
        implements LectureCoordinationListener , PPTView.PPTViewListener {
    
    protected static final int MENU_ITEM_SHARE = 11;
    private static final Spec<Message> SPEC_EXPERT_ROLE = new ExpertRoleSpec();
    private static final String TAG = "BaseLectureFragment";
    protected PPTView mPptView;
    protected View mAllSpeakForbiddenView;
    private String mLectureId;
    private LectureProto.LectureInfo mLectureInfo;
    private View mJustLookAtExpertView;
    private TextView mJustLookAtExpertTextView;
    private SPWrapper mSpWrapper = new SPWrapper("im_just_look_at_expert");
    private HelpCoverShow mHelpPopupShow;
    private View mHelpCoverShowItem;
    
    private CompatDialog mUserDetailDialog;
    private String mClickedQingQingUserId;
    private double mShareFeedbackAmount;
    private double mShareFeedbackAmountOfLecture;
    private MenuItem mShareMenu;
    private OnShareFeedbackListener mOnShareFeedbackListener;
    private OnPageRedirectListener mOnPageRedirectListener;
    OnUserDetailDialogClickListener mOnUserDetailDialogClickListener = new OnUserDetailDialogClickListener() {
        @Override
        public void onExpertCancel() {
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                    UserBehaviorLogManager.INSTANCE().saveClickLog(
                            StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                            StatisticalDataConstants.CLICK_LECTURE_ROOM_EXPERT_AVATAR,
                            new UserBehaviorLogExtraData.Builder().addExtraData(
                                    StatisticalDataConstants.LOG_EXTRA_E_CLICK_TYPE, 2)
                                    .build());
                }
            }
        }
        
        @Override
        public void onStudentMute(final boolean isMute) {
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                final String qingqingUserId = mClickedQingQingUserId;
                LectureConfig.allowOrStopTalkUser(isMute, qingqingUserId, getLectureId(),
                        new LectureConfig.LectureAllowOrStopUserTalkCallBack() {
                            
                            @Override
                            public void onSuccess() {
                                if (couldOperateUI() && getCoordinator() != null) {
                                    if (!isMute) {
                                        getCoordinator().getLectureRoom()
                                                .addForbiddenUser(qingqingUserId);
                                        ToastWrapper.show(
                                                getString(R.string.stop_talk_success));
                                    }
                                    else {
                                        getCoordinator().getLectureRoom()
                                                .removeForbiddenUser(qingqingUserId);
                                        ToastWrapper.show(getString(
                                                R.string.release_stop_talk_success));
                                    }
                                }
                                
                            }
                            
                            @Override
                            public void onFail() {
                                if (couldOperateUI()) {
                                    if (!isMute) {
                                        ToastWrapper
                                                .show(getString(R.string.stop_talk_fail));
                                    }
                                    else {
                                        ToastWrapper.show(getString(
                                                R.string.release_stop_talk_fail));
                                    }
                                    
                                }
                            }
                        });
            }
        }
        
        @Override
        public void onTeacherAttention() {
            // 收藏老师
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (mOnPageRedirectListener != null) {
                    mOnPageRedirectListener.onTeacherAttention(mClickedQingQingUserId);
                }
            }
        }
        
        @Override
        public void onTeacherEnterHomePage() {
            // 老师主页
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (mOnPageRedirectListener != null) {
                    mOnPageRedirectListener.onEnterTeacherHome(mClickedQingQingUserId);
                }
                
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                    UserBehaviorLogManager.INSTANCE().saveClickLog(
                            StatisticalDataConstants.LOG_PAGE_LECTURE_ROOM,
                            StatisticalDataConstants.CLICK_LECTURE_ROOM_EXPERT_AVATAR,
                            new UserBehaviorLogExtraData.Builder().addExtraData(
                                    StatisticalDataConstants.LOG_EXTRA_E_ENTER_HOME, 1)
                                    .build());
                }
                else if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                    UserBehaviorLogManager.INSTANCE().saveClickLog(
                            StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                            StatisticalDataConstants.CLICK_LECTURE_ROOM_EXPERT_AVATAR,
                            new UserBehaviorLogExtraData.Builder().addExtraData(
                                    StatisticalDataConstants.LOG_EXTRA_E_CLICK_TYPE, 1)
                                    .build());
                }
            }
        }
        
        @Override
        public void onAssistEnterHomePage() {
            // 助教详情
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (mOnPageRedirectListener != null) {
                    mOnPageRedirectListener.onEnterAssistantHome(mClickedQingQingUserId);
                }
            }
        }
        
        @Override
        public void onExit() {
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                    UserBehaviorLogManager.INSTANCE().saveClickLog(
                            StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                            StatisticalDataConstants.CLICK_LECTURE_ROOM_EXPERT_AVATAR,
                            new UserBehaviorLogExtraData.Builder().addExtraData(
                                    StatisticalDataConstants.LOG_EXTRA_E_CLICK_TYPE, 2)
                                    .build());
                }
            }
        }
        
        @Override
        public void onExitButton() {
            if (couldOperateUI()) {
                mUserDetailDialog.dismiss();
                
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                    UserBehaviorLogManager.INSTANCE().saveClickLog(
                            StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM,
                            StatisticalDataConstants.CLICK_LECTURE_ROOM_EXPERT_AVATAR,
                            new UserBehaviorLogExtraData.Builder().addExtraData(
                                    StatisticalDataConstants.LOG_EXTRA_E_CLICK_TYPE, 3)
                                    .build());
                }
            }
        }
        
    };
    
    public double getShareFeedbackAmount() {
        return mShareFeedbackAmount;
    }
    
    public void setShareFeedbackAmount(double shareFeedbackAmount) {
        this.mShareFeedbackAmount = shareFeedbackAmount;
    }
    
    public String getLectureId() {
        return mLectureId;
    }
    
    private void setLectureId(String lectureId) {
        this.mLectureId = lectureId;
    }
    
    protected LectureProto.LectureInfo getLectureInfo() {
        return mLectureInfo;
    }
    
    private void setLectureInfo(LectureProto.LectureInfo lectureInfo) {
        this.mLectureInfo = lectureInfo;
    }
    
    public TextView getJustLookAtExpertTextView() {
        return mJustLookAtExpertTextView;
    }
    
    public View getJustLookAtExpertView() {
        return mJustLookAtExpertView;
    }
    
    @Override
    protected LectureCoordinationListener createChatCoordinationListener() {
        return this;
    }
    
    @Override
    protected LectureCoordinator createCoordinator() {
        return new LectureCoordinator(getLectureId());
    }
    
    @Override
    protected LectureCoordinator getCoordinator() {
        return (LectureCoordinator) super.getCoordinator();
    }
    
    public PPTView getPptView() {
        return mPptView;
    }
    
    public View getAllSpeakForbiddenView() {
        return mAllSpeakForbiddenView;
    }
    
    @Override
    protected String getTitle() {
        if (getCoordinator() != null) {
            String title = getCoordinator().getLectureName();
            if (!TextUtils.isEmpty(title)) {
                return title;
            }
        }
        return super.getTitle();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_lecture, container, false);
    }
    
    @Override
    protected void onInitializeViews(View view) {
        super.onInitializeViews(view);
        getConsecutivePlayer().setContinuable(true);
        
        setTitleGravity(Gravity.LEFT);
        mJustLookAtExpertView = view.findViewById(R.id.btn_expert_and_all_mode_switch);
        mJustLookAtExpertView.setVisibility(View.VISIBLE);
        mJustLookAtExpertTextView = (TextView) mJustLookAtExpertView;
        mJustLookAtExpertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInitialized()) {
                    setJustLookAtExpertMode(!isJustLookAtExpertMode());
                }
                else {
                    Logger.e(TAG, "JustLookAtExpert isInitialized = " + false);
                }
            }
        });
        setExtendView(mJustLookAtExpertView);
        
        mPptView = (PPTView) view.findViewById(R.id.view_ppt);
        mPptView.setPPTViewListener(this);
        mAllSpeakForbiddenView = view.findViewById(R.id.tv_all_speak_forbidden);
        
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
            mListView.setOnLoadListener(new OnRefreshListener() {
                @Override
                public void onRefreshFromStart(String tag) {
                    getCoordinator().notifyRefreshFromStart();
                }
                
                @Override
                public void onRefreshFromEnd(String tag) {
                
                }
            });
        }
        
        mHelpCoverShowItem = view.findViewById(R.id.iv_sync_page);
    }
    
    @Override
    protected void onInitializeArguments(Bundle bundle) {
        super.onInitializeArguments(bundle);
        setLectureId(bundle.getString(Constants.EXTRA_LECTURE_ID));
        setLectureInfo((LectureProto.LectureInfo) bundle
                .getParcelable(Constants.EXTRA_LECTURE_INFO));
    }
    
    @Override
    protected void onInitializeMessageOperator() {
        super.onInitializeMessageOperator();
        getMessageOperator().setInterceptor(getCoordinator());
    }
    
    @Override
    public void onInitialized() {
        super.onInitialized();
        setConversationId(getCoordinator().getConversationId());
        setCanSendAudio(getCoordinator().canSendAudio());
        setSpeakForbidden(getCoordinator().isCurrentUserSpeakForbidden());
        getMessageOperator().setLimitingNumberTask(new LimitingNumberTask(
                getLimitedMaxSpeechNumber(), getLimitedTimeInterval()));
        setJustLookAtExpertMode(isJustLookAtExpertMode());
        
        setAllSpeakForbiddenViewVisibility();
        boolean hasPpt = getCoordinator().getLectureRoom().hasPpt();
        getPptView().setVisibility(
                getCoordinator().getLectureRoom().hasPpt() ? View.VISIBLE : View.GONE);
        boolean isExpertRole = getCoordinator().isExpertRole();
        if (hasPpt) {
            int pptSize = getCoordinator().getLectureRoom().getPptSize();
            int pptIndex = getCoordinator().getLectureRoom().getPptIndex();
            getPptView().setData(getCoordinator().getLectureRoom().getPpt()
                    .toArray(new String[pptSize]));
            getPptView().setAdmin(isExpertRole);
            getPptView().setMaxPage(isExpertRole ? pptSize - 1 : pptIndex);
            getPptView().setCurrentPage(pptIndex);
            
            prepareHelpShow();
        }
    }
    
    @Override
    public void onInitializeFailed() {
        super.onInitializeFailed();
        getActivity().finish();
    }
    
    private void prepareHelpShow() {
        if (getCoordinator() != null && getCoordinator().isExpertRole()
                && getCoordinator().getLectureRoom().hasPpt()
                && !HelpCoverShow.isHelpCoverDone(TAG)) {
            mHelpCoverShowItem.post(new Runnable() {
                @Override
                public void run() {
                    mHelpPopupShow = new HelpCoverShow(getContext(), TAG);
                    mHelpPopupShow.addStep(new HelpCoverShow.DrawStepBuilder(getContext())
                            .addClipRectRegion(mHelpCoverShowItem)
                            .setTipLayoutParams(HelpCoverShow.SHOW_PARAM_DRAWABLE_BELOW_VIEW
                                    | HelpCoverShow.SHOW_PARAM_DRAWABLE_ALIGN_LEFT)
                            .setTipResId(R.drawable.lecture_ppt_lead_1).apply());
                    mHelpPopupShow.startStep();
                }
            });
        }
    }
    
    boolean isJustLookAtExpertMode() {
        return mSpWrapper.getBoolean(getJustLookAtExpertKey());
    }
    
    private void setJustLookAtExpertMode(boolean justLookAtExpertMode) {
        mSpWrapper.put(getJustLookAtExpertKey(), justLookAtExpertMode);
        
        if (getAdapter() != null) {
            getAdapter().filterSpec(justLookAtExpertMode ? SPEC_EXPERT_ROLE : null);
        }
        
        getConsecutivePlayer().stopPlay();
        UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                StatisticalDataConstants.CLICK_LECTURE_ROOM_ONLY_EXPERT);
        if (isJustLookAtExpertMode()) {
            // messageList.getListView().setSelection(0);
        }
        
        getJustLookAtExpertTextView().setText(justLookAtExpertMode
                ? R.string.text_look_at_all : R.string.text_just_look_at_expert);
        getJustLookAtExpertView().setSelected(justLookAtExpertMode);
        // if (messageList != null && messageList.getMessageAdapter() != null) {
        // messageList.getMessageAdapter().filterRoles(justLookAtExpert ?
        // new int[]{ImProto.ChatRoomRoleType.mc_chat_room_role_type} :
        // new int[0]);
        // }
    }
    
    public String getJustLookAtExpertKey() {
        return ChatManager.getInstance().getCurrentUserId() + "/" + getLectureId();
    }
    
    @Override
    protected CharSequence getSpeakForbiddenText() {
        if (getCoordinator().isCurrentUserSpeakForbidden()) {
            if (getCoordinator().isFinished()) {
                return getResources().getText(R.string.text_lecture_room_end);
            }
            else if (getCoordinator().isSpeakAllForbidden()) {
                return getResources().getText(R.string.text_manager_forbid_all_speak);
            }
            else {
                return getResources().getText(R.string.text_speak_forbidden_by_manager);
            }
        }
        return super.getSpeakForbiddenText();
    }
    
    @Override
    protected CharSequence getSpeakForbiddenTips() {
        if (getCoordinator().isCurrentUserSpeakForbidden()) {
            if (getCoordinator().isFinished()) {
                return getResources().getText(R.string.tips_lecture_room_end);
            }
            else if (getCoordinator().isSpeakAllForbidden()) {
                return getResources().getText(R.string.tips_manager_forbid_all_speak);
            }
            else {
                return getResources()
                        .getText(R.string.tips_you_speak_forbidden_by_manager);
            }
        }
        return super.getSpeakForbiddenTips();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta
                || menu.findItem(MENU_ITEM_SHARE) != null) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }
        String menuText = couldOperateUI()
                ? getResources().getString(R.string.text_menu_share) : "";
        if (!TextUtils.isEmpty(menuText)) {
            mShareMenu = menu.add(Menu.NONE, MENU_ITEM_SHARE, Menu.NONE, menuText);
            mShareMenu.setIcon(R.drawable.chat_top_share);
            MenuItemCompat.setShowAsAction(mShareMenu,
                    MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        }
        super.onCreateOptionsMenu(menu, inflater);
        
        refreshShareStatus();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ITEM_SHARE) {
            share();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    protected int[] getExtendMenuItemIds() {
        return new int[] { Constants.EXTEND_ITEM_PICTURE, Constants.EXTEND_ITEM_CAMERA,
                Constants.EXTEND_ITEM_SETTINGS };
    }
    
    protected int[] getExtendMenuTitleResIds() {
        return new int[] { R.string.attach_picture, R.string.attach_take_pic,
                R.string.chat_menu_item_settings_text };
    }
    
    protected int[] getExtendMenuIconResIds() {
        return new int[] { R.drawable.chat_photo, R.drawable.chat_camera,
                R.drawable.chat_set };
    }
    
    @Override
    public void onExtendMenuItemClick(ChatExtendMenuItem item, int itemId, int position) {
        if (!isInitialized()) {
            Logger.e(TAG, "isInitialized failed , can not click MenuItem to send");
            return;
        }
        if (itemId == Constants.EXTEND_ITEM_SETTINGS) {
            // click设置
            if (getCoordinator() != null && getCoordinator().getChatRole() != null) {
                if (mOnPageRedirectListener != null) {
                    mOnPageRedirectListener.onEnterSetting();
                }
            }
            else {
                Logger.e(TAG, "ChatRole is null");
            }
        }
        else {
            if (getCoordinator().isCurrentUserSpeakForbidden()) {
                ToastWrapper.show(getSpeakForbiddenTips());
            }
            else {
                super.onExtendMenuItemClick(item, itemId, position);
            }
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_SETTINGS
                && resultCode == Constants.RESULT_CODE_CHANGE_ROLE) {
            if (resultCode == Constants.RESULT_CODE_CHANGE_ROLE) {
                if (data != null) {
                    UserProto.SimpleUserInfoV2 infoV2 = data
                            .getParcelableExtra(Constants.EXTRA_CHANGE_ROLE_USER_INFO);
                    int roleType = data.getIntExtra(Constants.EXTRA_CHANGE_ROLE_TYPE, 0);
                    ChatRole chatRole;
                    if (infoV2 != null) {
                        chatRole = ChatContactUtils.getChatRoleBy(infoV2, roleType);
                    }
                    else {
                        chatRole = ChatContactUtils.getSelfChatRole(roleType);
                    }
                    if (chatRole != null) {
                        getCoordinator().changeRole(chatRole);
                    }
                }
            }
            else if (resultCode == Constants.RESULT_CODE_FINISH_LECTURE) {
                getCoordinator().finishLecture(true);
            }
        }
    }
    
    @Override
    protected void onSendMessage(Message message) {
        super.onSendMessage(message);
        message.setRole(getCoordinator().getChatRole());
        // 配合ios用于屏蔽ains
        message.setAttribute("em_ignore_notification", true);
    }
    
    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mShareFeedbackAmount = args.getDouble(Constant.EXTRA_SHARE_FEEDBACK_AMOUNT, 0);
        mShareFeedbackAmountOfLecture = args
                .getDouble(Constant.EXTRA_SHARE_FEEDBACK_AMOUNT_OF_LECTURE, 0);
    }
    
    /**
     * 是否显示分享得2元
     */
    private void refreshShareStatus() {
        if (mOnShareFeedbackListener == null || mShareFeedbackAmountOfLecture < 0.01) {
            if (mShareMenu != null) {
                mShareMenu.setIcon(R.drawable.chat_top_share);
                mShareMenu.setTitle("");
            }
        }
        else {
            if (mShareMenu != null) {
                mShareMenu.setIcon(0);
                mShareMenu.setTitle(getResources().getString(
                        R.string.lecture_detail_share_title_feedback,
                        LogicConfig.getFormatDotString(mShareFeedbackAmountOfLecture
                                + DefaultDataCache.INSTANCE().getLiveRegisterBonus())));
            }
        }
    }
    
    @Override
    protected boolean onInterceptSendMessage(Message message) {
        if (getCoordinator() != null) {
            if (getCoordinator().isGuestRole()) {
                Logger.e(TAG, "游客不能发送消息");
                return true;
            }
            if (!getCoordinator().isExpertRole()
                    && message.getMsgType() == Message.Type.AUDIO) {
                Logger.e(TAG, "非主讲不支持语音");
                return true;
            }
        }
        return super.onInterceptSendMessage(message);
    }
    
    private int getLimitedMaxSpeechNumber() {
        if (getCoordinator() != null && !getCoordinator().isAdminOrExpertRole()) {
            return DefaultDataCache.INSTANCE().getLectureNormalUserSendMsgCountPreMin();
        }
        return -1;
    }
    
    private long getLimitedTimeInterval() {
        return 60 * 1000;
    }
    
    @Override
    public void onNumberOfParticipantsChanged() {
        setExtendTitle("(" + getCoordinator().getNumberOfParticipants() + ")");
    }
    
    @Override
    public void onFinished(boolean needForceFinish) {
        if (needForceFinish) {
            getActivity().setResult(Constants.RESULT_CODE_FINISH_LECTURE);
        }
        setSpeakForbidden(getCoordinator().isCurrentUserSpeakForbidden());
    }
    
    @Override
    public void onSpeakForbiddenChanged() {
        setAllSpeakForbiddenViewVisibility();
        setSpeakForbidden(getCoordinator().isCurrentUserSpeakForbidden());
    }
    
    @Override
    public void onRoleChanged() {
        setCanSendAudio(getCoordinator().canSendAudio());
        setAllSpeakForbiddenViewVisibility();
        setSpeakForbidden(getCoordinator().isCurrentUserSpeakForbidden());
        getMessageOperator().setLimitingNumberTask(new LimitingNumberTask(
                getLimitedMaxSpeechNumber(), getLimitedTimeInterval()));
        
        boolean isExpertRole = getCoordinator().isExpertRole();
        setAllSpeakForbiddenViewVisibility();
        getPptView().setMaxPage(
                isExpertRole ? getCoordinator().getLectureRoom().getPptSize() - 1
                        : getCoordinator().getLectureRoom().getPptIndex());
        getPptView().setAdmin(isExpertRole);
        prepareHelpShow();
    }
    
    @Override
    public void onPptPlayed() {
        boolean isExpertRole = getCoordinator().isExpertRole();
        int pptIndex = getCoordinator().getLectureRoom().getPptIndex();
        int pptSize = getCoordinator().getLectureRoom().getPptSize();
        if (isExpertRole) {
            getPptView().setMaxPage(pptSize - 1);
        }
        else if (getPptView().getMaxPage() < pptIndex) {
            getPptView().setMaxPage(pptIndex);
        }
        int pptDisplayedSize = getPptView().getPPTDisplayedSize();
        int pptCurrentPage = getPptView().getCurrentPage();
        if (!isExpertRole && pptCurrentPage != pptIndex && pptIndex >= 0
                && pptIndex < pptDisplayedSize) {
            getPptView().setCurrentPage(pptIndex);
        }
    }
    
    @Override
    public void onPptChanged() {
        setAllSpeakForbiddenViewVisibility();
        if (getCoordinator() != null && getCoordinator().getLectureRoom() != null) {
            boolean hasPpt = getCoordinator().getLectureRoom().hasPpt();
            int pptSize = getCoordinator().getLectureRoom().getPptSize();
            int pptIndex = getCoordinator().getLectureRoom().getPptIndex();
            getPptView().setVisibility(getCoordinator().getLectureRoom().hasPpt()
                    ? View.VISIBLE : View.GONE);
            getPptView().setData(getCoordinator().getLectureRoom().getPpt()
                    .toArray(new String[pptSize]));
            boolean isExpertRole = getCoordinator().isExpertRole();
            if (hasPpt) {
                getPptView().setAdmin(isExpertRole);
                getPptView().setMaxPage(isExpertRole ? pptSize - 1 : pptIndex);
                getPptView().setCurrentPage(pptIndex);
            }
        }
    }
    
    @Override
    public void onLoadingHistoryMessage(boolean hasHistoryMessage) {
        if (!hasHistoryMessage) {
            ToastWrapper.show(R.string.no_more_messages);
        }
    }
    
    void setCanSendAudio(boolean canSendAudio) {
        getView().findViewById(R.id.container_audio)
                .setVisibility(canSendAudio ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public void onBubbleClick(Message message) {
        switch (message.getMsgType()) {
            case AUDIO:
                UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                        StatisticalDataConstants.CLICK_LECTURE_ROOM_VOICE);
                break;
            case IMAGE:
                UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                        StatisticalDataConstants.CLICK_LECTURE_ROOM_PICTURE);
                break;
            default:
                break;
        }
        super.onBubbleClick(message);
    }
    
    @Override
    public void onBubbleLongClick(Message message) {
        if (message.getMsgType() == Message.Type.TEXT) {
            UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                    StatisticalDataConstants.CLICK_LECTURE_ROOM_WORD);
        }
        super.onBubbleLongClick(message);
    }
    
    @Override
    public void onAvatarClick(final Message message) {
        super.onAvatarClick(message);
        if (MessageUtils.isSendDirect(message)) {
            return;
        }
        if (getCoordinator() == null || getCoordinator().getChatRole() == null) {
            Logger.e(TAG, "current user role is null");
            return;
        }
        if (!message.hasPlayedRole()) {
            return;
        }
        LectureCoordinator coordinator = getCoordinator();
        final ChatRole chatRole = message.getRole();
        mClickedQingQingUserId = chatRole.getRoleId();
        final int targetRoleType = LectureConfig
                .getHighestRoleType(chatRole.getRoleType());
        final int userRoleType = LectureConfig
                .getHighestRoleType(coordinator.getChatRole().getRoleType());
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta
                || LectureConfig.isShowAvatarDialog(userRoleType, targetRoleType)) {
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta
                    || LectureConfig.isNeedReqUserInfoToShowDialog(targetRoleType)) {
                LectureConfig.reqLectureUserInfo(mClickedQingQingUserId,
                        new LectureConfig.LectureUserInfoCallBack() {
                            @Override
                            public void onSuccess(LectureProto.Lecturer lecturer) {
                                if (couldOperateUI()) {
                                    updateUserDetailDialog(targetRoleType, lecturer,
                                            false);
                                }
                            }
                            
                            @Override
                            public void onFail() {
                        
                        }
                        });
            }
            else {
                UserProto.SimpleUserInfoV2 userInfoV2 = new UserProto.SimpleUserInfoV2();
                userInfoV2.qingqingUserId = message.getFrom();
                userInfoV2.newHeadImage = ChatContactUtils.getHeadImage(message);
                userInfoV2.nick = ChatContactUtils.getNick(message);
                userInfoV2.sex = ChatContactUtils.getSexType(message);
                userInfoV2.userType = ChatContactUtils.getUserType(message);
                LectureProto.Lecturer lecturer = new LectureProto.Lecturer();
                lecturer.userInfo = userInfoV2;
                // isMute 根据环信给状态，当前已被禁言的用户为 true
                boolean isMute = getCoordinator().getLectureRoom()
                        .isUserInForbiddenList(mClickedQingQingUserId);
                updateUserDetailDialog(targetRoleType, lecturer, isMute);
            }
            
        }
    }
    
    private void updateUserDetailDialog(int targetRoleType,
            LectureProto.Lecturer lecturer, boolean isMute) {
        mUserDetailDialog = LectureConfig.updateUserDetailDialog(getActivity(),
                mUserDetailDialog, mOnUserDetailDialogClickListener, targetRoleType,
                isMute, lecturer);
        mUserDetailDialog.show();
    }
    
    private String getLogPageId() {
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
            return StatisticalDataConstants.LOG_PAGE_LECTURE_ROOM;
        }
        else if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
            return StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM;
        }
        else {
            // TA 暂时没有
            return StatisticalDataConstants.LOG_PAGE_LECTURE_ROOM;
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        UserBehaviorLogManager.INSTANCE().savePageLog(getLogPageId(),
                new UserBehaviorLogExtraData.Builder()
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_LECTURE_ID,
                                getLectureId())
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_ACTIVE_STATE, 2)
                        .build());
    }
    
    @Override
    public void onPause() {
        super.onPause();
        UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                StatisticalDataConstants.CLICK_LECTURE_EXIT_ROOM);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        getPptView().showController();
    }
    
    private void setAllSpeakForbiddenViewVisibility() {
        if (getCoordinator() != null && getAllSpeakForbiddenView() != null) {
            getAllSpeakForbiddenView().setVisibility(
                    getPptView().isPortrait() && getCoordinator().isSpeakAllForbidden()
                            && getCoordinator().isAdminOrExpertRole() ? View.VISIBLE
                                    : View.GONE);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPptView().setPPTViewListener(null);
    }
    
    @Override
    public void onExpandChanged(boolean expanded) {
        setAllSpeakForbiddenViewVisibility();
    }
    
    @Override
    public void onSyncPage(int pageIndex) {
        getCoordinator().playPpt(pageIndex);
    }
    
    @Override
    public void onSyncPageModeChanged(boolean mode) {
        if (mode) {
            getCoordinator().playPpt(getPptView().getCurrentPage());
        }
    }
    
    private void share() {
        if (getLectureInfo() != null) {
            if (mOnShareFeedbackListener == null || mShareFeedbackAmountOfLecture < 0.01
                    || (BaseData.isUserIDValid() && mShareFeedbackAmount < 0.01)) {
                String pageId = StatisticalDataConstants.LOG_PAGE_LECTURE_ROOM;
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                    pageId = StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM;
                }
                ShareShow share = new ShareShow(getActivity(), pageId);
                
                String title = getString(R.string.lecture_detail_base_share_title,
                        getLectureInfo().title);
                String content = getLectureInfo().description;
                String shareUrl = CommonUrl.H5_SHARE_LECTURE.url()
                        + getLectureInfo().qingqingLectureId;
                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("city_id", String.valueOf(getLectureInfo().cityId));
                switch (DefaultDataCache.INSTANCE().getAppType()) {
                    case AppCommon.AppType.qingqing_student:
                        paramMap.put("actid", "20161001LIVE");
                        break;
                    case AppCommon.AppType.qingqing_ta:
                        paramMap.put("actid", "20161TASLIVE");
                        break;
                }
                
                shareUrl = UrlUtil.addParamToUrl(shareUrl, paramMap);
                String shareIcon = ImageUrlUtil
                        .getDefaultCropImg(getLectureInfo().coverImage);
                String chid = "";
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {}
                else if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                    chid = getString(R.string.chid_ta_lecture);
                }
                
                share.setShareLink(shareUrl, getString(R.string.chnid_lecture), chid)
                        .setShareTitle(title).setShareContent(content)
                        .setShareIcon(shareIcon).setDefaultShareIcon(R.drawable.share)
                        .show();
                Logger.v("share Url: " + shareUrl);
            }
            else {
                mOnShareFeedbackListener.onShareFeedback();
            }
        }
        else {
            mOnShareFeedbackListener.onShareFeedback();
        }
    }
    
    @Override
    public boolean onBackPressed() {
        if (!getPptView().isPortrait()) {
            getPptView().toggleScreenOrientation();
            return true;
        }
        if (getExtendMenu().onBackPressed()) {
            return true;
        }
        new CompDefaultDialogBuilder(getActivity())
                .setTitle(R.string.text_dlg_exit_lecture_title)
                .setContent(R.string.text_dlg_exit_lecture_message)
                .setPositiveButton(R.string.text_dlg_exit_lecture_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.text_dlg_exit_lecture_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        })
                .show();
        return false;
    }
    
    public void setOnPageRedirectListener(OnPageRedirectListener onPageRedirectListener) {
        this.mOnPageRedirectListener = onPageRedirectListener;
    }
    
    public void setOnShareFeedbackListener(OnShareFeedbackListener listener) {
        this.mOnShareFeedbackListener = listener;
    }
    
    public interface OnShareFeedbackListener {
        void onShareFeedback();
    }
    
    public interface OnPageRedirectListener {
        void onEnterTeacherHome(String qqUserId);
        
        void onEnterAssistantHome(String qqUserId);
        
        void onTeacherAttention(String qqUserId);
        
        void onEnterSetting();
    }
}
