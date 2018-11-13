package com.qingqing.base.im.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.qingqing.api.commentsvc.proto.v1.CommentCountingProto;
import com.qingqing.api.proto.v1.LectureProto;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.SPWrapper;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.im.AudioBatchProcesser;
import com.qingqing.base.im.ChatRoomModel;
import com.qingqing.base.im.Constant;
import com.qingqing.base.log.Logger;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.view.ToastWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/5/28.
 */
public abstract class BaseLectureChatRoomFragment extends BaseChatRoomFragment
        implements ChatRoomModel.Listener {
    
    protected String lectureId;
    
    protected ChatRoomModel chatRoomModel;
    
    protected static final int ITEM_SETTINGS = 21;
    
    protected static final int MENU_ITEM_SHARE = 11;
    
    protected int[] itemStrings = { R.string.attach_picture, R.string.attach_take_pic,
            R.string.chat_menu_item_settings_text };
    protected int[] itemdrawables = { R.drawable.chat_photo, R.drawable.chat_camera,
            R.drawable.chat_set };
    protected int[] itemIds = { ITEM_PICTURE, ITEM_TAKE_PICTURE, ITEM_SETTINGS };
    
    protected View mJustLookAtExpertView;
    protected TextView mJustLookAtExpertTextView;
    protected LectureProto.LectureInfo lectureInfo;
    
    private SPWrapper mSpWrapper = new SPWrapper("im_just_look_at_expert");
    
    private List<EMMessage> mCachedMessages;
    
    protected static final int RETRY_MAX_COUNT = 30;
    
    private int mRetryCount;
    
    private boolean mRoomChanged;
    protected MenuItem mShareMenu;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        lectureId = getArguments().getString(Constant.EXTRA_LECTURE_ID);
        lectureInfo = (LectureProto.LectureInfo) getArguments()
                .getParcelable(Constant.EXTRA_LECTURE_INFO);
        if (lectureId == null) {
            lectureId = "";
        }
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    protected void initView() {
        super.initView();
        mJustLookAtExpertView = inputMenu
                .findViewById(R.id.btn_expert_and_all_mode_switch);
        mJustLookAtExpertView.setVisibility(View.VISIBLE);
        mJustLookAtExpertTextView = (TextView) mJustLookAtExpertView;
        mJustLookAtExpertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMessageListInited) {
                    AudioPlayerController.stopCurrentPlayer();
                    setJustLookAtExpert(!mJustLookAtExpertView.isSelected());
                    
                    UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                            StatisticalDataConstants.CLICK_LECTURE_ROOM_ONLY_EXPERT);
                    if (mJustLookAtExpertView.isSelected()) {
                        messageList.getListView().setSelection(0);
                    }
                }
                else {
                    Logger.e(TAG, "JustLookAtExpert isMessageListInited = " + false);
                }
            }
        });
        
    }
    
    private String getLogPageId() {
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
            return StatisticalDataConstants.LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM;
        }
        else {
            return StatisticalDataConstants.LOG_PAGE_LECTURE_ROOM;
        }
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitleGravity(Gravity.LEFT);
    }
    
    void setJustLookAtExpert(boolean justLookAtExpert) {
        mSpWrapper.put(getJustLookAtExpertKey(), justLookAtExpert);
        if (couldOperateUI()) {
            mJustLookAtExpertTextView.setText(justLookAtExpert ? R.string.text_all_mode
                    : R.string.text_expert_mode);
            mJustLookAtExpertView.setSelected(justLookAtExpert);
            if (messageList != null && messageList.getMessageAdapter() != null) {
                messageList.getMessageAdapter()
                        .filterRoles(justLookAtExpert
                                ? new int[] {
                                        ImProto.ChatRoomRoleType.mc_chat_room_role_type }
                                : new int[0]);
            }
            
        }
    }
    
    boolean isJustLookAtExpert() {
        return mSpWrapper.getBoolean(getJustLookAtExpertKey());
    }
    
    public String getJustLookAtExpertKey() {
        return chatRoomModel != null ? chatRoomModel.getCurUserName() + "/" + lectureId
                : "unknown/" + lectureId;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFetchAttendanceTask.cancel();
        handler.removeCallbacks(mRetryInitChatRoomTask);
        if (chatRoomModel != null) {
            chatRoomModel.removeListener(this);
            chatRoomModel = null;
        }
    }
    
    @Override
    protected void onNewMessage(EMMessage message) {
        super.onNewMessage(message);
        // 配合ios用于屏蔽ains
        message.setAttribute("em_ignore_notification", true);
    }
    
    @Override
    public boolean onMessageBubbleClick(EMMessage message) {
        if (message != null) {
            EMMessage.Type type = message.getType();
            if (type != null) {
                switch (message.getType()) {
                    case VOICE:
                        UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                                StatisticalDataConstants.CLICK_LECTURE_ROOM_VOICE);
                        break;
                    case IMAGE:
                        UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                                StatisticalDataConstants.CLICK_LECTURE_ROOM_PICTURE);
                        break;
                }
            }
        }
        return super.onMessageBubbleClick(message);
    }
    
    @Override
    public void onMessageBubbleLongClick(final EMMessage message) {
        if (message != null) {
            EMMessage.Type type = message.getType();
            if (type != null) {
                switch (message.getType()) {
                    case TXT:
                        UserBehaviorLogManager.INSTANCE().saveClickLog(getLogPageId(),
                                StatisticalDataConstants.CLICK_LECTURE_ROOM_WORD);
                        break;
                }
            }
        }
        super.onMessageBubbleLongClick(message);
    }
    
    @Override
    protected void onChatRoomInit() {
        LectureProto.SimpleQingQingLectureIdRequest request = new LectureProto.SimpleQingQingLectureIdRequest();
        request.qingqingLectureId = lectureId;
        new ProtoReq(CommonUrl.LECTURE_CHAT_ROOM_JOIN_V3.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(
                        LectureProto.LectureChatRoomJoinResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        reqUserBrowsingCount();
                        mRetryCount = 0;
                        LectureProto.LectureChatRoomJoinResponse response = (LectureProto.LectureChatRoomJoinResponse) result;
                        
                        Logger.i(TAG, "Lecture Chat Room Init ok ");
                        toChatUsername = response.chatRoomId;
                        chatInit = true;
                        allowTalk = true;
                        chatRoomModel = new ChatRoomModel(response);
                        ChatRoomModel.putModel(toChatUsername, chatRoomModel);
                        if (couldOperateUI()) {


                            getView().findViewById(R.id.content_voice)
                                    .setVisibility(!isMcRole()
                                                    ? View.GONE : View.VISIBLE);
                            AudioBatchProcesser.getInstance(getActivity())
                                    .joinConversation(toChatUsername);
                            mLimitSpeakTask.setLimit(getSpeakCountLimit());
                            mLimitSpeakTask.start();
                            chatRoomModel.addListener(BaseLectureChatRoomFragment.this);
                            setTitle();
                            setSpeakForbidden(isCurrentUserSpeakForbidden());
                        }
                        initChatRoomInternal();
                    }
                    
                    @Override
                    public boolean onDealError(int errorCode, Object result) {
                        switch (errorCode) {
                            case 1203:
                                if (couldOperateUI()) {
                                    if (!mRoomChanged) {
                                        ToastWrapper.show(getErrorHintMessage(
                                                R.string.tips_join_lecture_failed));
                                        finishActivity();
                                    }
                                    else if (mRetryCount >= RETRY_MAX_COUNT) {
                                        ToastWrapper
                                                .show(R.string.tips_join_lecture_failed);
                                        finishActivity();
                                    }
                                    else {
                                        mRetryCount++;
                                        Logger.e(TAG,
                                                "delay retry init chatRoom : mRetryCount = "
                                                        + mRetryCount);
                                        delayRetryInitChatRoom();
                                    }
                                }
                                return true;
                            case 1202:
                                if (couldOperateUI()) {
                                    ToastWrapper.show(getErrorHintMessage(
                                            R.string.tips_lecture_filled));
                                    finishActivity();
                                }
                                return true;
                            default:
                                if (couldOperateUI()) {
                                    ToastWrapper.show(R.string.tips_join_lecture_failed);
                                    finishActivity();
                                }
                                return true;
                        }
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                            int errorCode, Object result) {
                        super.onDealError(error, isParseOK, errorCode, result);
                        Logger.e(TAG, "Chat Room Init response error" + errorCode);
                    }
                }).req();
    }
    
    protected void finishActivity() {
        if (couldOperateUI()) {
            getActivity().finish();
        }
    }
    
    private void delayRetryInitChatRoom() {
        handler.postDelayed(mRetryInitChatRoomTask, 3000);
    }
    
    private Runnable mRetryInitChatRoomTask = new Runnable() {
        @Override
        public void run() {
            onChatRoomInit();
        }
    };
    
    @Override
    protected void onChatRoomDestroyed() {
        super.onChatRoomDestroyed();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (chatRoomModel != null) {
                    chatRoomModel.finishChatRoom();
                }
            }
        });
    }
    
    @Override
    public void onFinished() {
        if (couldOperateUI()) {
            getActivity().setResult(Activity.RESULT_OK);
            setSpeakForbidden(isCurrentUserSpeakForbidden());
        }
    }
    
    @Override
    public void onSpeakForbiddenChanged() {
        if (couldOperateUI()) {
            setSpeakForbidden(isCurrentUserSpeakForbidden());
        }
    }
    
    @Override
    public void onRoleChanged() {
        if (couldOperateUI()) {
            getView().findViewById(R.id.content_voice)
                    .setVisibility(isMcRole()
                                    ? View.GONE : View.VISIBLE);
            mLimitSpeakTask.setLimit(getSpeakCountLimit());
            mLimitSpeakTask.start();
            setSpeakForbidden(isCurrentUserSpeakForbidden());
            messageList.refreshSelectLast();
        }
    }
    
    @Override
    public void onRoomChanged() {
        mRoomChanged = true;
        if (couldOperateUI() && messageList != null
                && messageList.getMessageAdapter() != null) {
            String preRoomId = toChatUsername;
            String curRoomId = chatRoomModel.getRoomId();
            List<EMMessage> messages = messageList.getMessageAdapter().getMessages();
            List<EMMessage> messagesCopy = new ArrayList<>(
                    messages != null ? messages.size() : 0);
            EMChatManager.getInstance().deleteConversation(preRoomId);
            EMChatManager.getInstance().leaveChatRoom(preRoomId);
            for (EMMessage message : messages) {
                message.setTo(curRoomId);
                messagesCopy.add(message);
            }
            
            AudioPlayerController.stopCurrentPlayer();
            
            mCachedMessages = messagesCopy;
            onChatRoomInit();
        }
    }
    
    protected void initChatRoomInternal() {
        super.onChatRoomInit();
    }
    
    @Override
    protected void onConversationInit() {
        super.onConversationInit();
        if (mCachedMessages != null) {
            for (EMMessage message : mCachedMessages) {
                conversation.addMessage(message);
            }
        }
    }
    
    protected boolean isCurrentUserSpeakForbidden() {
        return chatRoomModel != null && chatRoomModel.isCurrentUserSpeakForbidden();
    }
    
    private boolean isGuestRole() {
        boolean isGuest = false;
        if (chatRoomModel != null) {
            for (int i = 0; i < chatRoomModel.getChatRoomType().size(); i++) {
                if (chatRoomModel.getChatRoomType()
                        .get(i) == ImProto.ChatRoomRoleType.guest_chat_room_role_type) {
                    isGuest = true;
                }
            }
        }
        return isGuest;
    }
    
    private boolean isMcRole() {
        boolean isExpert = false;
        if(chatRoomModel!=null){
            for (int i = 0; i < chatRoomModel.getChatRoomType().size(); i++) {
                if (chatRoomModel.getChatRoomType()
                        .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type) {
                    isExpert = true;
                }
            }
        }
        return isExpert;
    }
    
    protected boolean isAdminOrMcRole() {
        boolean isAdminOrMc = false;
        
        if (chatRoomModel != null) {
            for (int i = 0; i < chatRoomModel.getChatRoomType().size(); i++) {
                if (chatRoomModel.getChatRoomType()
                        .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type
                        || chatRoomModel.getChatRoomType().get(
                                i) == ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    isAdminOrMc = true;
                }
            }
        }
        
        return isAdminOrMc;
    }
    
    protected boolean isAllSpeakForbidden() {
        return chatRoomModel != null && chatRoomModel.isSpeakAllForbidden();
    }
    
    @Override
    protected void onMessageListInit() {
        super.onMessageListInit();
        setJustLookAtExpert(isJustLookAtExpert());
    }
    
    @Override
    protected int getSpeakCountLimit() {
        if (chatRoomModel != null) {
            if (!isAdminOrMcRole()) {
                return DefaultDataCache.INSTANCE()
                        .getLectureNormalUserSendMsgCountPreMin();
            }
        }
        return super.getSpeakCountLimit();
    }
    
    @Override
    protected CharSequence getForbidSpeakText() {
        if (isCurrentUserSpeakForbidden()) {
            if (chatRoomModel.isFinished()) {
                return getResources().getText(R.string.text_lecture_room_end);
            }
            else if (chatRoomModel.isSpeakAllForbidden()) {
                return getResources().getText(R.string.text_manager_forbid_all_speak);
            }
            else {
                return getResources().getText(R.string.text_speak_forbidden_by_manager);
            }
        }
        return super.getForbidSpeakText();
    }
    
    @Override
    protected CharSequence getForbidSpeakTips() {
        if (isCurrentUserSpeakForbidden()) {
            if (chatRoomModel.isFinished()) {
                return getResources().getText(R.string.tips_lecture_room_end);
            }
            else if (chatRoomModel.isSpeakAllForbidden()) {
                return getResources().getText(R.string.tips_manager_forbid_all_speak);
            }
            else {
                return getResources()
                        .getText(R.string.tips_you_speak_forbidden_by_manager);
            }
        }
        return super.getForbidSpeakTips();
    }
    
    @Override
    protected void onSendMessage(EMMessage message) {
        super.onSendMessage(message);
        if (chatRoomModel != null) {
            chatRoomModel.generateMsgExtFromUser(message);
        }
    }
    
    @Override
    protected String getTitle() {
        String title = chatRoomModel != null ? chatRoomModel.getName() : "";
        if (TextUtils.isEmpty(title)) {
            title = getResources().getString(R.string.title_chat_room_default);
        }
        return title;
    }
    
    @Override
    protected void registerExtendMenuItem() {
        for (int i = 0; i < itemStrings.length; i++) {
            inputMenu.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i],
                    extendMenuItemClickListener);
        }
    }
    
    @Override
    protected void sendVoiceMessage(String filePath, int length) {
        if (chatRoomModel != null && !isMcRole()) {
            // 待确认是否Toast以及管理员是否可以发送语音
            Logger.e(TAG, "非主讲不支持语音");
        }
        else {
            super.sendVoiceMessage(filePath, length);
        }
    }
    
    @Override
    protected void sendMessageImpl(EMMessage message) {
        if (chatRoomModel != null && isGuestRole()) {
            // 待确认游客提示
            Logger.e(TAG, "游客不能发送消息");
        }
        else {
            if (mJustLookAtExpertView.isSelected()) {
                ToastWrapper.show(R.string.tips_just_look_at_expert);
            }
            super.sendMessageImpl(message);
        }
    }
    
    @Override
    public boolean onExtendMenuItemClick(int itemId, View view) {
        if (itemId == ITEM_SETTINGS) {
            // 点击设置按钮
            return true;
        }
        if (isCurrentUserSpeakForbidden()) {
            ToastWrapper.show(getForbidSpeakTips());
            return true;
        }
        return super.onExtendMenuItemClick(itemId, view);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        String menuText = couldOperateUI()
                ? getResources().getString(R.string.text_menu_share) : "";
        if (!TextUtils.isEmpty(menuText)) {
            mShareMenu = menu.add(Menu.NONE, MENU_ITEM_SHARE, Menu.NONE, menuText);
            mShareMenu.setIcon(R.drawable.chat_top_share);
            MenuItemCompat.setShowAsAction(mShareMenu,
                    MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    protected String getMenuText() {
        return super.getMenuText();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ITEM_SHARE) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void reqUserBrowsingCount() {
        CommentCountingProto.QingqingCountingRequest request = new CommentCountingProto.QingqingCountingRequest();
        request.countRefType = CommentCountingProto.CountRefType.lecture_count_type;
        request.hasCountRefType = true;
        request.qingqingRefId = lectureId;
        request.isQingqingEncode = true;
        new ProtoReq(CommonUrl.COUNTING_VIEW_URL.url()).setSendMsg(request)
                .setRspListener(
                        new ProtoListener(ProtoBufResponse.SimpleIntDataResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                ProtoBufResponse.SimpleIntDataResponse response = (ProtoBufResponse.SimpleIntDataResponse) result;
                                if (couldOperateUI()) {
                                    setExtendTitle("(" + response.data + ")");
                                }
                                startFetchAttendance();
                            }
                            
                            @Override
                            public void onDealError(HttpError error, boolean isParseOK,
                                    int errorCode, Object result) {
                                startFetchAttendance();
                            }
                        })
                .req();
    }
    
    private void startFetchAttendance() {
        if (couldOperateUI()) {
            mFetchAttendanceTask.start();
        }
    }
    
    private FetchAttendanceTask mFetchAttendanceTask = new FetchAttendanceTask();
    
    private class FetchAttendanceTask implements Runnable {
        
        boolean canceled = false;
        
        public void cancel() {
            canceled = true;
        }
        
        public void start() {
            canceled = false;
            handler.post(this);
        }
        
        @Override
        public void run() {
            if (canceled) {
                return;
            }
            CommentCountingProto.QingqingCountingRequest request = new CommentCountingProto.QingqingCountingRequest();
            request.countRefType = CommentCountingProto.CountRefType.lecture_count_type;
            request.qingqingRefId = lectureId;
            request.isQingqingEncode = true;
            new ProtoReq(CommonUrl.COUNTING_VIEW_COUNT_URL.url()).setSendMsg(request)
                    .setRspListener(new ProtoListener(
                            ProtoBufResponse.SimpleIntDataResponse.class) {
                        @Override
                        public void onDealResult(Object result) {
                            if (canceled) {
                                return;
                            }
                            // 处理title
                            ProtoBufResponse.SimpleIntDataResponse response = (ProtoBufResponse.SimpleIntDataResponse) result;
                            Logger.i(TAG, "FetchAttendanceTask " + response.data);
                            int memberNumber = response.data;
                            if (couldOperateUI()) {
                                setExtendTitle("(" + memberNumber + ")");
                                // setSubtitle("(" + memberNumber + ")");
                                // //测试Subtitle
                            }
                            processTask();
                        }
                        
                        @Override
                        public void onDealError(HttpError error, boolean isParseOK,
                                int errorCode, Object result) {
                            if (canceled) {
                                return;
                            }
                            processTask();
                        }
                    }).req();
        }
        
        private void processTask() {
            handler.postDelayed(this,
                    DefaultDataCache.INSTANCE().getLectureAttendanceSyncInterval()
                            * 1000);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        LectureProto.SimpleQingQingLectureIdRequest request = new LectureProto.SimpleQingQingLectureIdRequest();
        request.qingqingLectureId = lectureId;
        
        new ProtoReq(CommonUrl.LECTURE_CHAT_ROOM_LEAVE.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        Logger.i(TAG, "Lecture Chat Room Leave ok ");
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                            int errorCode, Object result) {
                        super.onDealError(error, isParseOK, errorCode, result);
                        Logger.e(TAG, "Chat Room Init Leave error" + errorCode);
                    }
                }).req();
        
        handler.removeCallbacks(mFetchAttendanceTask);
    }
}
