package com.easemob.easeui.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.TextView;

import com.easemob.EMChatRoomChangeListener;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.controller.EaseUI;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.model.EaseAtMessageHelper;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.easeui.utils.EaseImageUtils;
import com.easemob.easeui.utils.EaseUserUtils;
import com.easemob.easeui.widget.EaseAlertDialog;
import com.easemob.easeui.widget.EaseAlertDialog.AlertDialogUser;
import com.easemob.easeui.widget.EaseChatExtendMenu;
import com.easemob.easeui.widget.EaseChatInputMenu;
import com.easemob.easeui.widget.EaseChatInputMenu.ChatInputMenuListener;
import com.easemob.easeui.widget.EaseChatMessageList;
import com.easemob.easeui.widget.EaseVoiceRecorderView;
import com.easemob.easeui.widget.EaseVoiceRecorderView.EaseVoiceRecorderCallback;
import com.easemob.easeui.widget.chatrow.EaseCustomChatRowProvider;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.SelectPictureManager;
import com.qingqing.base.bean.UserBehaviorLogExtraData;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.constant.ThemeConstant;
import com.qingqing.base.core.CountDownCenter;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.dialog.CompDefaultDialog;
import com.qingqing.base.dialog.CompDefaultDialogBuilder;
import com.qingqing.base.dialog.component.CompDialogBackgroundColorConsole;
import com.qingqing.base.dialog.component.CompDialogDefaultFooter;
import com.qingqing.base.dialog.component.CompDialogScrollTextViewContent;
import com.qingqing.base.dialog.component.CompDialogUIComponent;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.im.AudioBatchProcesser;
import com.qingqing.base.im.ChatConversationInitializer;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.Constant;
import com.qingqing.base.im.domain.ChatMessage;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.domain.GroupRole;
import com.qingqing.base.im.ui.MapActivity;
import com.qingqing.base.log.Logger;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.teacherindex.TeacherIndexManager;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.NoAnimPtrListView;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.editor.LimitEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import static com.easemob.EMNotifierEvent.Event.EventNewMessage;

/**
 * 可以直接new出来使用的聊天对话页面fragment，
 * 使用时需调用setArguments方法传入chatType(会话类型)和userId(用户或群id) app也可继承此fragment续写 <br/>
 * <br/>
 * 参数传入示例可查看demo里的ChatActivity
 */
public class EaseChatFragment extends EaseBaseFragment
        implements EMEventListener , TeacherIndexManager.TeacherIndexUpdateListener {
    protected static final String TAG = "EaseChatFragment";
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    protected static final int REQUEST_CODE_AT_LIST = 4;
    private static final String SEND_TO_BE_SENT_MESSAGE_TAG = "SendToBeSentMessage";
    
    protected boolean chatInit = false;
    protected boolean allowTalk = false;
    protected boolean forbidAction = false;
    protected int groupType;
    
    /**
     * 传入fragment的参数
     */
    protected Bundle fragmentArgs;
    protected int chatType;
    protected String toChatUsername;
    protected EaseChatMessageList messageList;
    protected EaseChatInputMenu inputMenu;
    
    protected EMConversation conversation;
    
    protected InputMethodManager inputManager;
    protected ClipboardManager clipboard;
    
    protected Handler handler = new Handler();
    // protected File cameraFile;
    protected EaseVoiceRecorderView voiceRecorderView;
    protected TextView unreadTipsView;
    protected NoAnimPtrListView listView;
    protected LimitEditText editText;
    protected TextView forbidSpeakTv;
    
    protected boolean isloading;
    protected boolean haveMoreData = true;
    protected int pagesize = 20;
    protected GroupListener groupListener;
    protected EMMessage contextMenuMessage;
    private int unreadNumber;
    @Nullable
    protected UserProto.ChatUserInfo selfInfo;
    
    protected static final int ITEM_TAKE_PICTURE = 1;
    protected static final int ITEM_PICTURE = 2;
    protected static final int ITEM_LOCATION = 3;
    protected static final int ITEM_VIDEO = 11;
    protected static final int ITEM_FILE = 12;
    protected static final int ITEM_VOICE_CALL = 13;
    protected static final int ITEM_VIDEO_CALL = 14;
    
    protected int[] itemStrings = { R.string.attach_picture, R.string.attach_take_pic,
            R.string.attach_video, R.string.attach_location };
    protected int[] itemdrawables = { R.drawable.icon_chat_pic,
            R.drawable.icon_chat_camera, R.drawable.icon_chat_video,
            R.drawable.icon_chat_location };
    protected int[] itemIds = { ITEM_PICTURE, ITEM_TAKE_PICTURE, ITEM_VIDEO,
            ITEM_LOCATION };
    protected boolean isMessageListInited;
    protected MyItemClickListener extendMenuItemClickListener;
    
    private SelectPictureManager mSelPicMgr;
    private static final int KEY_USE_CAMERA = 3345;
    private static final int KEY_OPEN_GALLERY = 3346;
    
    private static boolean couldDealPic = false;
    
    ValueAnimator mUnreadTipsGoneAnimator;
    int mUnreadTipsWidth;
    boolean mIsUnreadTipsAnimationRunning = false;
    /**
     * 群聊时，创建群组时才置为Constant.CHATSCNE_NEW_CREATE(群组)
     * 单聊时，从联系人进入是需要设置Constant.CHATSCNE_NEW_CREATE
     */
    protected int chatScene;
    
    /**
     * 退课时需要的订单 id
     */
    private String qqOrderCourseId;
    /**
     * 进入时自动发送的消息
     */
    private ArrayList<String> toBeSentMessage;
    /**
     * 只有从会话列表进入聊天时才需要立刻请求前置接口,其他地方进入在发送前请求前置接口
     */
    protected boolean initImmediate;
    
    protected EMChatRoomChangeListener chatRoomChangeListener;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ease_fragment_chat, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 聊天中选择图片最大可压缩到1M （2^20 = 1048576 B）
        mSelPicMgr = new SelectPictureManager(this).setMaxFileSize(1 << 20);
        mSelPicMgr.setSelectPicListenerV2(mSelectPicListener);
        getActivity().registerReceiver(mHomeKeyEventReceiver,
                new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        TeacherIndexManager.getInstance().addListener(this);
    }
    
    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(mHomeKeyEventReceiver);
        TeacherIndexManager.getInstance().removeListener(this);
        if (mUnreadTipsGoneAnimator != null) {
            mUnreadTipsGoneAnimator.end();
        }
        super.onDestroyView();
    }
    
    @Override
    public void onFetchDone() {
        messageList.refresh();
    }
    
    private SelectPictureManager.SelectPicListenerV2 mSelectPicListener = new SelectPictureManager.SelectPicListenerV2() {
        @Override
        public void onPicSelected(int key, List<File> outputFiles) {
            if ((key == KEY_USE_CAMERA || key == KEY_OPEN_GALLERY) && couldDealPic) {
                couldDealPic = false;
                if (outputFiles != null) {
                    sendMultipleImage(outputFiles);
                }
            }
        }
    };
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        
        fragmentArgs = getArguments();
        // 判断单聊还是群聊
        chatType = fragmentArgs.getInt(EaseConstant.EXTRA_CHAT_TYPE,
                EaseConstant.CHATTYPE_SINGLE);
        // 会话人或群组id
        toChatUsername = fragmentArgs.getString(EaseConstant.EXTRA_USER_ID);
        if (toChatUsername == null) {
            toChatUsername = "";
        }
        chatScene = fragmentArgs.getInt(Constant.EXTRA_CHAT_SCENE, 0);
        
        initImmediate = fragmentArgs.getBoolean(Constant.EXTRA_CHAT_INIT_IMM, false);
        qqOrderCourseId = fragmentArgs
                .getString(BaseParamKeys.PARAM_STRING_ORDER_COURSE_ID, "");
        toBeSentMessage = fragmentArgs.getStringArrayList(Constant.EXTRA_MSG_TO_BE_SENT);
        
        super.onActivityCreated(savedInstanceState);
        
        Logger.i(TAG, "onActivityCreated initImmediate = " + initImmediate
                + ", chatScene = " + chatScene);
        // 群聊时需要区分教研管理群，因此强制拉取 init 接口
        // 5.9.7 需要获取老师的 trm 身份，因此强制拉取
        if (chatType == Constant.CHATTYPE_GROUP || initImmediate
                || (chatType == Constant.CHATTYPE_SINGLE && BaseData
                        .getClientType() == AppCommon.AppType.qingqing_teacher)) {
            reqChatInit(null);
        }
        if (chatType == Constant.CHATTYPE_GROUP) {
            ChatManager.getInstance().addGroupAnnouncementUpdateListener(toChatUsername,
                    new ChatManager.GroupAnnouncementUpdateListener() {
                        @Override
                        public void onGroupAnnouncementUpdate() {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    if (couldOperateUI()) {
                                        popGroupAnnounceDialog();
                                    }
                                }
                            });
                            
                        }
                    });
        }
        
        if (allowTalk && chatInit) {
            sendToBeSentMessage();
        }
    }
    
    private void sendToBeSentMessage() {
        if (toBeSentMessage == null || toBeSentMessage.size() <= 0) {
            return;
        }
        
        // 定时发送待发消息，目前默认间隔1秒
        CountDownCenter.INSTANCE().addRepeatTask(SEND_TO_BE_SENT_MESSAGE_TAG, 1,
                new CountDownCenter.CountDownListener() {
                    @Override
                    public void onCountDown(String tag, int leftCount) {
                        if (leftCount == 0) {
                            if (toBeSentMessage.size() <= 0) {
                                CountDownCenter.INSTANCE()
                                        .cancelTask(SEND_TO_BE_SENT_MESSAGE_TAG);
                            }
                            
                            sendTextMessage(toBeSentMessage.get(0));
                            toBeSentMessage.remove(0);
                        }
                    }
                });
    }
    
    private void showAnimationAndGoneUnreadTips() {
        if (mIsUnreadTipsAnimationRunning) {
            return;
        }
        
        if (mUnreadTipsGoneAnimator == null) {
            mUnreadTipsGoneAnimator = ValueAnimator.ofFloat(0, 1f);
            mUnreadTipsGoneAnimator.setDuration(500);
            mUnreadTipsGoneAnimator
                    .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float progress = (float) animation.getAnimatedValue();
                            unreadTipsView.setTranslationX(mUnreadTipsWidth * progress);
                            unreadTipsView.setAlpha(1 - progress);
                        }
                    });
            mUnreadTipsGoneAnimator.addListener(new Animator.AnimatorListener() {
                
                @Override
                public void onAnimationStart(Animator animation) {
                    mUnreadTipsWidth = unreadTipsView.getWidth();
                }
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    unreadTipsView.setVisibility(View.GONE);
                    mIsUnreadTipsAnimationRunning = false;
                }
                
                @Override
                public void onAnimationCancel(Animator animation) {
                    unreadTipsView.setVisibility(View.GONE);
                    mIsUnreadTipsAnimationRunning = false;
                }
                
                @Override
                public void onAnimationRepeat(Animator animation) {
                
                }
            });
        }
        mUnreadTipsGoneAnimator.start();
        mIsUnreadTipsAnimationRunning = true;
    }
    
    /**
     * init view
     */
    protected void initView() {
        // 按住说话录音控件
        voiceRecorderView = (EaseVoiceRecorderView) getView()
                .findViewById(R.id.voice_recorder);
        voiceRecorderView.setChatType(chatType);
        // 消息列表layout
        messageList = (EaseChatMessageList) getView().findViewById(R.id.message_list);
        if (chatType != EaseConstant.CHATTYPE_SINGLE)
            messageList.setShowUserNick(true);
        listView = messageList.getListView();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                // 当显示到第一条未读时，不显示未读提示
                if (firstVisibleItem <= listView.getCount() - unreadNumber) {
                    if (couldOperateUI() && unreadTipsView != null) {
                        showAnimationAndGoneUnreadTips();
                    }
                }
            }
        });
            
        // 未读提示
        unreadTipsView = (TextView) getView().findViewById(R.id.tv_unread_tips);
        if (unreadTipsView != null) {
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                unreadTipsView
                        .setTextColor(getResources().getColor(R.color.primary_blue));
                unreadTipsView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.icon_imchat_history, 0, 0, 0);
            }
            else if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                unreadTipsView
                        .setTextColor(getResources().getColor(R.color.primary_orange));
                unreadTipsView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.icon_remind, 0, 0, 0);
            }
            unreadTipsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageList.seekHistoryByUnreadNumber(unreadNumber);
                    showAnimationAndGoneUnreadTips();
                }
            });
        }
        
        extendMenuItemClickListener = new MyItemClickListener();
        inputMenu = (EaseChatInputMenu) getView().findViewById(R.id.input_menu);
        registerExtendMenuItem();
        // init input menu
        inputMenu.init(null);
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            inputMenu.setGroupId(toChatUsername);
        }
        editText = (LimitEditText) getView().findViewById(R.id.et_sendmessage);
        inputMenu.setChatInputMenuListener(new ChatInputMenuListener() {
            
            @Override
            public void onSendMessage(String content) {
                // 发送文本消息
                sendTextMessage(content);
            }
            
            @Override
            public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
                return voiceRecorderView.onPressToSpeakBtnTouch(v, event,
                        new EaseVoiceRecorderCallback() {
                            
                            @Override
                            public void onVoiceRecordComplete(String voiceFilePath,
                                    int voiceTimeLength) {
                                // 发送语音消息
                                sendVoiceMessage(voiceFilePath, voiceTimeLength);
                            }
                        });
            }
            
            @Override
            public void onAtFunctionInput() {
                onAtFunction();
            }
            
            @Override
            public void onBigExpressionClicked(EaseEmojicon emojicon) {
                // 发送大表情(动态表情)
                sendBigExpressionMessage(emojicon.getName(), emojicon.getIdentityCode());
            }
        });
        
        inputManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        clipboard = (ClipboardManager) getActivity()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        forbidSpeakTv = (TextView) getView().findViewById(R.id.tv_forbid_speak);
        setSpeakForbidden(false);
    }
    
    /**
     * 输入 @ 时的处理
     */
    protected void onAtFunction() {
        if (chatType == Constant.CHATTYPE_GROUP) {
            if (chatFragmentListener != null) {
                chatFragmentListener.onEnterGroupChatMemberListActivity(toChatUsername,
                        REQUEST_CODE_AT_LIST);
            }
        }
    }
    
    protected void setSpeakForbidden(boolean speakForbidden) {
        forbidSpeakTv.setVisibility(speakForbidden ? View.VISIBLE : View.GONE);
        forbidSpeakTv.setText(getForbidSpeakText());
        
    }
    
    protected CharSequence getForbidSpeakText() {
        return getResources().getText(R.string.text_speak_forbidden_default);
    }
    
    protected CharSequence getForbidSpeakTips() {
        return getResources().getText(R.string.tips_speak_forbidden_default);
    }
    
    /**
     * 设置属性，监听等
     */
    protected void setUpView() {
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            // 监听当前会话的群聊解散被T事件
            groupListener = new GroupListener();
            EMGroupManager.getInstance().addGroupChangeListener(groupListener);
        }
        if (chatType != EaseConstant.CHATTYPE_CHATROOM) {
            onConversationInit();
            onMessageListInit();
        }
        
        setRefreshLayoutListener();
        
        // show forward message if the message is not null
        String forward_msg_id = getArguments().getString("forward_msg_id");
        if (forward_msg_id != null) {
            // 发送要转发的消息
            forwardMessage(forward_msg_id);
        }
    }
    
    protected void onChatRoomInit() {
        
    }
    
    /**
     * -1表示没有限制
     */
    protected int getSpeakCountLimit() {
        return -1;
    }
    
    /**
     * 注册底部菜单扩展栏item; 覆盖此方法时如果不覆盖已有item，item的id需大于3
     */
    protected void registerExtendMenuItem() {
        for (int i = 0; i < itemStrings.length; i++) {
            inputMenu.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i],
                    extendMenuItemClickListener);
        }
    }
    
    protected void onConversationInit() {
        // 获取当前conversation对象
        conversation = EMChatManager.getInstance().getConversation(toChatUsername);
        // 获取未读数
        unreadNumber = conversation.getUnreadMsgCount();
        
        if (unreadTipsView != null) {
            unreadTipsView.setText(
                    getResources().getString(R.string.chat_unread_tips, unreadNumber));
            unreadTipsView.setVisibility(unreadNumber > 0 ? View.VISIBLE : View.GONE);
        }
        // 把此会话的未读数置为0
        conversation.markAllMessagesAsRead();
        // 排行榜未读标记清除
        ChatManager.getInstance().setHasGroupNewRank(toChatUsername, false);
        // 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
        // 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
        final List<EMMessage> msgs = conversation.getAllMessages();
        int msgCount = msgs != null ? msgs.size() : 0;
        if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
            String msgId = null;
            if (msgs != null && msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            if (chatType == EaseConstant.CHATTYPE_SINGLE) {
                conversation.loadMoreMsgFromDB(msgId, pagesize - msgCount);
            }
            else {
                conversation.loadMoreGroupMsgFromDB(msgId, pagesize - msgCount);
            }
        }
        
    }
    
    protected void onMessageListInit() {
        messageList.init(toChatUsername, chatType,
                chatFragmentListener != null
                        ? chatFragmentListener.onSetCustomChatRowProvider()
                        : null);
        // 设置list item里的控件的点击事件
        setListItemClickListener();
        
        messageList.getListView().setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                inputMenu.hideExtendMenuContainer();
                return false;
            }
        });
        
        isMessageListInited = true;
    }
    
    protected void setListItemClickListener() {
        messageList.setItemClickListener(
                new EaseChatMessageList.MessageListItemClickListener() {
                    
                    @Override
                    public void onUserAvatarClick(ChatMessage message) {
                        if (chatFragmentListener != null
                                && chatFragmentListener.onAvatarClick(message)) {
                            return;
                        }
                        if (chatFragmentListener != null) {
                            chatFragmentListener.onAvatarClick(message.getCurUserName());
                        }
                    }
                    
                    @Override
                    public void onUserAvatarLongClick(ChatMessage message) {
                        if (chatFragmentListener != null
                                && chatFragmentListener.onAvatarLongClick(message)) {
                            return;
                        }
                        if (chatFragmentListener != null) {
                            chatFragmentListener
                                    .onAvatarLongClick(message.getCurUserName());
                        }
                    }
                    
                    @Override
                    public void onResendClick(final EMMessage message) {
                        resendMessage(message);
                    }
                    
                    @Override
                    public void onBubbleLongClick(EMMessage message) {
                        contextMenuMessage = message;
                        if (chatFragmentListener != null) {
                            chatFragmentListener.onMessageBubbleLongClick(message);
                        }
                    }
                    
                    @Override
                    public boolean onBubbleClick(EMMessage message) {
                        if (chatFragmentListener != null) {
                            return chatFragmentListener.onMessageBubbleClick(message);
                        }
                        return false;
                    }
                });
    }
    
    protected void setRefreshLayoutListener() {
        listView.setOnLoadListener(new com.qingqing.base.view.ptr.OnRefreshListener() {
            
            @Override
            public void onRefreshFromStart(String tag) {
                loadMoreMessage();
            }
            
            @Override
            public void onRefreshFromEnd(String tag) {
            
            }
        });
    }
    
    private void loadMoreMessage() {
        if (listView.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
            List<EMMessage> messages;
            try {
                if (chatType == EaseConstant.CHATTYPE_SINGLE) {
                    messages = conversation.loadMoreMsgFromDB(
                            messageList.getItem(0).getMsgId(), pagesize);
                }
                else {
                    messages = conversation.loadMoreGroupMsgFromDB(
                            messageList.getItem(0).getMsgId(), pagesize);
                }
            } catch (Exception e1) {
                return;
            }
            if (messages.size() > 0) {
                messageList.refreshSeekTo(messages.size() - 1);
                if (messages.size() != pagesize) {
                    haveMoreData = false;
                }
            }
            else {
                haveMoreData = false;
            }
            
            isloading = false;
            
        }
        else {
            ToastWrapper.show(R.string.no_more_messages);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (mSelPicMgr != null) {
                mSelPicMgr.onActivityResult(requestCode, resultCode, data);
            }
            if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
                // if (cameraFile != null && cameraFile.exists())
                // sendImageMessage(cameraFile.getAbsolutePath());
            }
            else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            }
            else if (requestCode == REQUEST_CODE_MAP) { // 地图
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("address");
                if (locationAddress != null && !locationAddress.equals("")) {
                    sendLocationMessage(latitude, longitude, locationAddress);
                }
                else {
                    ToastWrapper.show(R.string.unable_to_get_loaction);
                }
                
            }
            else if (requestCode == REQUEST_CODE_AT_LIST && data != null) {
                UserProto.ChatUserInfo info = data.getParcelableExtra(
                        BaseParamKeys.PARAM_PARACLE_GROUP_CHAT_MEMBERS);
                inputAtUsername(info.qingqingUserId, false);
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(toChatUsername)) {
            AudioBatchProcesser.getInstance(getActivity())
                    .joinConversation(toChatUsername);
        }
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            EaseAtMessageHelper.get().removeAtMeGroup(toChatUsername);
        }
        if (isMessageListInited)
            messageList.refresh();
        EaseUI.getInstance().pushActivity(getActivity());
        // register the event listener when enter the foreground
        EMChatManager.getInstance().registerEventListener(this,
                new EMNotifierEvent.Event[] { EventNewMessage,
                        EMNotifierEvent.Event.EventOfflineMessage,
                        EMNotifierEvent.Event.EventDeliveryAck,
                        EMNotifierEvent.Event.EventReadAck });
    }
    
    @Override
    public void onStop() {
        super.onStop();
        // unregister this event listener when this activity enters the
        // background
        AudioBatchProcesser.getInstance(getActivity()).leaveConversation();
        EMChatManager.getInstance().unregisterEventListener(this);
        
        // 把此activity 从foreground activity 列表里移除
        EaseUI.getInstance().popActivity(getActivity());
    }
    
    /**
     * 监听是否点击了home键将客户端推到后台
     */
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    // 表示按了home键,程序到了后台
                    // ToastWrapper.show("home");
                    AudioPlayerController.stopCurrentPlayer();
                }
            }
        }
    };
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (!TextUtils.isEmpty(toChatUsername)) {
            AudioBatchProcesser.getInstance(getActivity())
                    .getAudioBatchController(toChatUsername).stop();
        }
        mLimitSpeakTask.cancel();
        handler.removeCallbacks(mLimitSpeakTask);
        if (groupListener != null) {
            EMGroupManager.getInstance().removeGroupChangeListener(groupListener);
        }
        AudioPlayerController.stopCurrentPlayer();
        if (chatType == Constant.CHATTYPE_GROUP) {
            ChatManager.getInstance()
                    .removeGroupAnnouncementUpdateListener(toChatUsername);
        }
        TeacherIndexManager.getInstance().clear();
    }
    
    /**
     * 事件监听,registerEventListener后的回调事件
     * <p/>
     * see {@link EMNotifierEvent}
     */
    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage:
                // 获取到message
                EMMessage message = (EMMessage) event.getData();
                
                String username = null;
                // 群组消息
                if (message.getChatType() == ChatType.GroupChat
                        || message.getChatType() == ChatType.ChatRoom) {
                    username = message.getTo();
                }
                else {
                    // 单聊消息
                    username = message.getFrom();
                }
                
                // 如果是当前会话的消息，刷新聊天页面
                if (username.equals(toChatUsername)) {
                    onNewMessage(message);
                    messageList.refreshSelectLast();
                }
                else {
                    // 如果消息不是和当前聊天ID的消息
                    EaseUI.getInstance().getNotifier().onNewMsg(message);
                }
                
                break;
            case EventDeliveryAck:
            case EventReadAck:
                // 获取到message
                messageList.refresh();
                break;
            case EventOfflineMessage:
                // a list of offline messages
                // List<EMMessage> offlineMessages = (List<EMMessage>)
                // event.data;
                messageList.refresh();
                break;
            default:
                break;
        }
        
    }
    
    protected void onNewMessage(EMMessage message) {}
    
    @Override
    public boolean onBackPressed() {
        UIUtil.closeInputManager(this);
        if (inputMenu == null || inputMenu.onBackPressed()) {
            getActivity().finish();
        }
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            EaseAtMessageHelper.get().removeAtMeGroup(toChatUsername);
            EaseAtMessageHelper.get().cleanToAtUserList();
        }
        return false;
    }
    
    protected void showChatroomToast(final String toastContent) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                ToastWrapper.show(toastContent);
            }
        });
    }
    
    /**
     * 扩展菜单栏item点击事件
     */
    class MyItemClickListener
            implements EaseChatExtendMenu.EaseChatExtendMenuItemClickListener {
        
        @Override
        public void onClick(int itemId, View view) {
            if (chatFragmentListener != null) {
                if (chatFragmentListener.onExtendMenuItemClick(itemId, view)) {
                    return;
                }
            }
            switch (itemId) {
                case ITEM_TAKE_PICTURE: // 拍照
                    selectPicFromCamera();
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                        UserBehaviorLogManager.INSTANCE().saveClickLog(
                                StatisticalDataConstants.LOG_PAGE_TEACHER_IM_DETAIL,
                                StatisticalDataConstants.CLICK_CHAT_EXPAND,
                                new UserBehaviorLogExtraData.Builder().addExtraData(
                                        StatisticalDataConstants.LOG_EXTRA_E_CHAT_EXPAND_TYPE,
                                        2).build());
                    }
                    break;
                case ITEM_PICTURE:
                    selectPicFromLocal(); // 图库选择图片
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                        UserBehaviorLogManager.INSTANCE().saveClickLog(
                                StatisticalDataConstants.LOG_PAGE_TEACHER_IM_DETAIL,
                                StatisticalDataConstants.CLICK_CHAT_EXPAND,
                                new UserBehaviorLogExtraData.Builder().addExtraData(
                                        StatisticalDataConstants.LOG_EXTRA_E_CHAT_EXPAND_TYPE,
                                        1).build());
                    }
                    break;
                case ITEM_LOCATION: // 位置
                    try {
                        startActivityForResult(
                                new Intent(getActivity(), MapActivity.class),
                                REQUEST_CODE_MAP);
                        if (BaseData
                                .getClientType() == AppCommon.AppType.qingqing_teacher) {
                            UserBehaviorLogManager.INSTANCE().saveClickLog(
                                    StatisticalDataConstants.LOG_PAGE_TEACHER_IM_DETAIL,
                                    StatisticalDataConstants.CLICK_CHAT_EXPAND,
                                    new UserBehaviorLogExtraData.Builder().addExtraData(
                                            StatisticalDataConstants.LOG_EXTRA_E_CHAT_EXPAND_TYPE,
                                            4).build());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                
                default:
                    break;
            }
        }
        
    }
    
    protected LimitSpeakTask mLimitSpeakTask = new LimitSpeakTask(getSpeakCountLimit());
    
    protected class LimitSpeakTask implements Runnable {
        
        long timeInterval;
        
        int limitCount;
        
        int limitTimes;
        
        boolean canceled;
        
        public LimitSpeakTask(int limitCount) {
            this(60 * 1000, limitCount);
        }
        
        public LimitSpeakTask(int timeInterval, int limitCount) {
            this.timeInterval = timeInterval;
            this.limitCount = limitCount;
        }
        
        public void setLimit(int limitCount) {
            this.limitCount = limitCount;
        }
        
        public void start() {
            if (canceled || limitCount < 0) {
                return;
            }
            handler.postDelayed(this, timeInterval);
        }
        
        public void cancel() {
            canceled = true;
        }
        
        @Override
        public void run() {
            if (canceled || limitCount < 0) {
                return;
            }
            limitTimes = 0;
            handler.postDelayed(this, timeInterval);
        }
        
        public void increase() {
            limitTimes++;
        }
        
        public boolean canSpeak() {
            return limitCount < 0 || limitTimes < limitCount;
        }
    }
    
    // 发送消息方法
    // ==========================================================================
    protected void sendTextMessage(String content) {
        if (EaseAtMessageHelper.get().containsAtUsername(content, toChatUsername)) {
            sendAtMessage(content);
        }
        else {
            EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
            sendMessage(message);
        }
    }
    
    /**
     * send @ message, only support group chat message
     *
     * @param content
     */
    @SuppressWarnings("ConstantConditions")
    private void sendAtMessage(String content) {
        if (chatType != EaseConstant.CHATTYPE_GROUP) {
            EMLog.e(TAG, "only support group chat message");
            return;
        }
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        EMGroup group = EMGroupManager.getInstance().getGroup(toChatUsername);
        if (group != null
                && EMChatManager.getInstance().getCurrentUser().equals(group.getOwner())
                && EaseAtMessageHelper.get().containsAtAll(content)) {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG,
                    EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL);
        }
        else {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG,
                    EaseAtMessageHelper.get().atListToJsonArray(EaseAtMessageHelper.get()
                            .getAtMessageUsernames(content, toChatUsername)));
        }
        sendMessage(message);
        
    }
    
    /**
     * input @
     *
     * @param username
     */
    protected void inputAtUsername(String username, boolean autoAddAtSymbol) {
        if (EMChatManager.getInstance().getCurrentUser().equals(username)
                || chatType != EaseConstant.CHATTYPE_GROUP) {
            return;
        }
        EaseAtMessageHelper.get().addAtUser(username);
        ContactInfo user = EaseUserUtils.getUserInfo(username);
        if (user != null) {
            username = user.getNick();
            if (chatType == EaseConstant.CHATTYPE_GROUP) {
                GroupRole groupRole = user.getGroupRole(toChatUsername);
                if (groupRole != null) {
                    username = groupRole.getNick();
                }
            }
        }
        if (autoAddAtSymbol)
            inputMenu.insertText("@" + username + " ");
        else
            inputMenu.insertText(username + " ");
    }
    
    /**
     * input @
     *
     * @param username
     */
    protected void inputAtUsername(String username) {
        inputAtUsername(username, true);
    }
    
    protected void sendBigExpressionMessage(String name, String identityCode) {
        EMMessage message = EaseCommonUtils.createExpressionMessage(toChatUsername, name,
                identityCode);
        sendMessage(message);
    }
    
    protected void sendVoiceMessage(String filePath, int length) {
        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length,
                toChatUsername);
        sendMessage(message);
    }
    
    private void sendMultipleImage(final List<File> outputFiles) {
        if (outputFiles == null || outputFiles.size() <= 0) {
            return;
        }
        
        if (outputFiles.get(0).exists()) {
            sendImageMessage(outputFiles.get(0).getAbsolutePath());
        }
        
        if (outputFiles.size() > 1) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendMultipleImage(outputFiles.subList(1, outputFiles.size()));
                }
            }, 100);
        }
    }
    
    protected void sendImageMessage(String imagePath) {
        File file = new File(imagePath);
        try {
            String imagePathLowerCase = imagePath.toLowerCase();
            if (!(imagePathLowerCase.endsWith(".jpg")
                    || imagePathLowerCase.endsWith(".png")
                    || imagePathLowerCase.endsWith(".jpeg"))) {
                ToastWrapper.show(R.string.chat_image_format_limit_text);
                return;
            }
            else if (!file.exists()) {
                ToastWrapper.show(R.string.chat_image_not_exists_text);
                return;
            }
            else if (file.length() > 10 * 1024 * 1024) {
                ToastWrapper.show(R.string.chat_image_limit_text);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // 发送原图，需注意原始图片大小，压缩后再发送
        EMMessage message = EMMessage.createImageSendMessage(imagePath, true,
                toChatUsername);
        sendMessage(message);
    }
    
    protected void sendLocationMessage(double latitude, double longitude,
            String locationAddress) {
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude,
                locationAddress, toChatUsername);
        sendMessage(message);
    }
    
    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
        EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath,
                videoLength, toChatUsername);
        sendMessage(message);
    }
    
    protected void sendFileMessage(String filePath) {
        EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
        sendMessage(message);
    }
    
    protected void sendMessage(EMMessage message) {
        if (chatFragmentListener != null) {
            // 设置扩展属性
            chatFragmentListener.onSetMessageAttributes(message);
        }
        // 如果是群聊，设置chattype,默认是单聊
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            message.setChatType(ChatType.GroupChat);
        }
        else if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            message.setChatType(ChatType.ChatRoom);
        }
        sendMessageImpl(message);
    }
    
    public void resendMessage(EMMessage message) {
        message.status = EMMessage.Status.CREATE;
        sendMessageImpl(message);
    }
    
    protected void sendMessageImpl(EMMessage message) {
        if (!isMessageListInited) {
            return;
        }
        if (!allowTalk) {
            if (!chatInit) {
                Logger.i("Chat init Start");
                reqChatInit(message);
                onChatRoomInit();
            }
            else {
                if (forbidAction) {
                    ToastWrapper.showWithIcon(R.string.chat_init_failed_not_in_group,
                            R.drawable.icon_task_warning);
                }
                else {
                    talkUnAllowed(null);
                }
            }
            return;
        }
        if (!mLimitSpeakTask.canSpeak()) {
            ToastWrapper.show(R.string.tips_normal_user_speak_limit);
            return;
        }
        if (message != null) {
            // 发送消息
            
            onSendMessage(message);
            mLimitSpeakTask.increase();
            EMChatManager.getInstance().sendMessage(message, null);
            EaseAtMessageHelper.get().cleanToAtUserList();
            // 刷新ui
            if (!conversation.getAllMessages().contains(message)) {
                conversation.addMessage(message);
            }
            messageList.refreshForceSelectLast();
        }
    }
    
    protected void onSendMessage(EMMessage message) {
        
    }
    
    protected void talkUnAllowed(String hintMsg) {
        
        String toastString = hintMsg;
        if (chatType == Constant.CHATTYPE_SINGLE) {
            ContactInfo contactInfo = ChatManager.getInstance().getContactModel()
                    .getContactInfo(toChatUsername);
            if (contactInfo != null
                    && contactInfo.getType() == ContactInfo.Type.Assistant) {
                if (TextUtils.isEmpty(toastString)) {
                    toastString = getString(R.string.chat_unbind_tips_text);
                }
            }
            else {
                if (TextUtils.isEmpty(toastString)) {
                    toastString = getString(R.string.chat_not_allow_talk_text);
                }
            }
        }
        else {
            if (TextUtils.isEmpty(toastString)) {
                toastString = getString(R.string.chat_not_allow_talk_text);
            }
        }
        ToastWrapper.show(toastString);
    }
    
    // ===================================================================================
    
    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    protected void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor != null) {
            String picturePath = null;
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
            }
            
            if (picturePath == null || picturePath.equals("null")) {
                ToastWrapper.show(R.string.cant_find_pictures);
                return;
            }
            sendImageMessage(picturePath);
        }
        else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                ToastWrapper.show(R.string.cant_find_pictures);
                return;
                
            }
            sendImageMessage(file.getAbsolutePath());
        }
        
    }
    
    /**
     * 根据uri发送文件
     *
     * @param uri
     */
    protected void sendFileByUri(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            
            try {
                cursor = getActivity().getContentResolver().query(uri, filePathColumn,
                        null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (cursor != null) {
                cursor.close();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            ToastWrapper.show(R.string.File_does_not_exist);
            return;
        }
        // 大于10M不让发送
        if (file.length() > 10 * 1024 * 1024) {
            ToastWrapper.show(R.string.The_file_is_not_greater_than_10_m);
            return;
        }
        sendFileMessage(filePath);
    }
    
    /**
     * 照相获取图片
     */
    protected void selectPicFromCamera() {
        if (!EaseCommonUtils.isExitsSdcard()) {
            ToastWrapper.show(R.string.sd_card_does_not_exist);
            return;
        }
        
        couldDealPic = true;
        mSelPicMgr.key(KEY_USE_CAMERA).openCamera();
        // cameraFile = new File(PathUtil.getInstance().imagePath,
        // EMChatManager
        // .getInstance().getCurrentUser() + System.currentTimeMillis() +
        // ".jpg");
        // cameraFile.getParentFile().mkdirs();
        // startActivityForResult(new
        // Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
        // MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
        // REQUEST_CODE_CAMERA);
    }
    
    /**
     * 从图库获取图片
     */
    protected void selectPicFromLocal() {
        // Intent intent;
        // if (Build.VERSION.SDK_INT < 19) {
        // intent = new Intent(Intent.ACTION_GET_CONTENT);
        // intent.setType("image/*");
        //
        // }
        // else {
        // intent = new Intent(Intent.ACTION_PICK,
        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // }
        // startActivityForResult(intent, REQUEST_CODE_LOCAL);
        if (!EaseCommonUtils.isExitsSdcard()) {
            ToastWrapper.show(R.string.sd_card_does_not_exist);
            return;
        }
        
        couldDealPic = true;
        mSelPicMgr.maxSize(9).key(KEY_OPEN_GALLERY).openGallery();
    }
    
    /**
     * 点击清空聊天记录
     */
    protected void emptyHistory() {
        String msg = getResources().getString(R.string.Whether_to_empty_all_chats);
        new EaseAlertDialog(getActivity(), null, msg, null, new AlertDialogUser() {
            
            @Override
            public void onResult(boolean confirmed, Bundle bundle) {
                if (confirmed) {
                    // 清空会话
                    EMChatManager.getInstance().clearConversation(toChatUsername);
                    messageList.refresh();
                }
            }
        }, true).show();
    }
    
    /**
     * 点击进入群组详情
     */
    protected void toGroupDetails() {
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            EMGroup group = EMGroupManager.getInstance().getGroup(toChatUsername);
            if (group == null) {
                ToastWrapper.show(R.string.gorup_not_found);
                return;
            }
            if (chatFragmentListener != null) {
                chatFragmentListener.onEnterToChatDetails();
            }
        }
    }
    
    public void onEnterToChatDetails() {
        
    }
    
    /**
     * 隐藏软键盘
     */
    protected void hideKeyboard() {
        if (getActivity().getWindow()
                .getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(
                        getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    /**
     * 转发消息
     *
     * @param forward_msg_id
     */
    protected void forwardMessage(String forward_msg_id) {
        final EMMessage forward_msg = EMChatManager.getInstance()
                .getMessage(forward_msg_id);
        EMMessage.Type type = forward_msg.getType();
        switch (type) {
            case TXT:
                if (forward_msg.getBooleanAttribute(
                        EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                    sendBigExpressionMessage(
                            ((TextMessageBody) forward_msg.getBody()).getMessage(),
                            forward_msg.getStringAttribute(
                                    EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, null));
                }
                else {
                    // 获取消息内容，发送消息
                    String content = ((TextMessageBody) forward_msg.getBody())
                            .getMessage();
                    sendTextMessage(content);
                }
                break;
            case IMAGE:
                // 发送图片
                String filePath = ((ImageMessageBody) forward_msg.getBody())
                        .getLocalUrl();
                if (filePath != null) {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        // 不存在大图发送缩略图
                        filePath = EaseImageUtils.getThumbnailImagePath(filePath);
                    }
                    sendImageMessage(filePath);
                }
                break;
            default:
                break;
        }
        
        if (forward_msg.getChatType() == EMMessage.ChatType.ChatRoom) {
            EMChatManager.getInstance().leaveChatRoom(forward_msg.getTo());
        }
    }
    
    /**
     * 监测群组解散或者被T事件
     */
    class GroupListener extends EaseGroupRemoveListener {
        
        @Override
        public void onUserRemoved(final String groupId, String groupName) {
            getActivity().runOnUiThread(new Runnable() {
                
                public void run() {
                    if (toChatUsername.equals(groupId)) {
                        if (couldOperateUI()) {
                            getActivity().finish();
                        }
                    }
                }
            });
        }
        
        @Override
        public void onGroupDestroy(final String groupId, String groupName) {
            // 群组解散正好在此页面，提示群组被解散，并finish此页面
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (toChatUsername.equals(groupId)) {
                        if (couldOperateUI()) {
                            getActivity().finish();
                        }
                    }
                }
            });
        }
        
    }
    
    protected EaseChatFragmentListener chatFragmentListener;
    
    public void setChatFragmentListener(EaseChatFragmentListener chatFragmentListener) {
        this.chatFragmentListener = chatFragmentListener;
    }
    
    public interface EaseChatFragmentListener {
        /**
         * 设置消息扩展属性
         */
        void onSetMessageAttributes(EMMessage message);
        
        /**
         * 进入会话详情
         */
        void onEnterToChatDetails();
        
        /**
         * 用户头像点击事件
         *
         * @param username
         */
        void onAvatarClick(String username);
        
        /**
         * 用户头像长按事件
         *
         * @param username
         */
        void onAvatarLongClick(String username);
        
        /**
         * 用户头像点击事件
         */
        boolean onAvatarClick(ChatMessage message);
        
        /**
         * 用户头像长按事件
         */
        boolean onAvatarLongClick(ChatMessage message);
        
        /**
         * 消息气泡框点击事件
         */
        boolean onMessageBubbleClick(EMMessage message);
        
        /**
         * 消息气泡框长按事件
         */
        void onMessageBubbleLongClick(EMMessage message);
        
        /**
         * 扩展输入栏item点击事件,如果要覆盖EaseChatFragment已有的点击事件，return true
         *
         * @param view
         * @param itemId
         * @return
         */
        boolean onExtendMenuItemClick(int itemId, View view);
        
        /**
         * 设置自定义chatrow提供者
         *
         * @return
         */
        EaseCustomChatRowProvider onSetCustomChatRowProvider();
        
        /**
         * 输入@后拉起群成员列表
         * 
         * @param groupChatId
         */
        void onEnterGroupChatMemberListActivity(@NonNull String groupChatId,
                int requestCode);
        
    }
    
    /**
     * 单聊的场景需要确认 群聊时，只有群组创建时才需要setScene
     */
    protected void reqChatInit(final EMMessage message) {
        if (chatType == Constant.CHATTYPE_SINGLE) {
            ImProto.ChatInitRequest builder = new ImProto.ChatInitRequest();
            builder.qingqingUserId = toChatUsername;
            if (chatScene == Constant.CHATSCNE_NEW_CREATE) {
                builder.scene = ImProto.ChatInitScene.new_create_chat_init_scene;
                builder.hasScene = true;
            }
            if (!TextUtils.isEmpty(qqOrderCourseId)) {
                builder.scene = ImProto.ChatInitScene.cancel_course_chat_init_scene;
                builder.hasScene = true;
                ImProto.ChatInitRequest.CancelCourseSceneExtend extend = new ImProto.ChatInitRequest.CancelCourseSceneExtend();
                extend.qingqingOrderCourseId = qqOrderCourseId;
                extend.hasQingqingOrderCourseId = true;
                builder.setCancelCourse(extend);
            }
            
            newProtoReq(CommonUrl.CHAT_INIT_URL.url()).setSendMsg(builder)
                    .setRspListener(new ProtoListener(ImProto.ChatInitResponse.class) {
                        @Override
                        public void onDealResult(Object result) {
                            ImProto.ChatInitResponse response = (ImProto.ChatInitResponse) result;
                            UserProto.ChatUserInfo chatUserInfo = response.member;
                            updateMember(chatUserInfo);
                            ChatConversationInitializer.getInstance()
                                    .notifyChatConversationInitialized(toChatUsername);
                            Logger.i(TAG, "Chat Init ok ");
                            chatInit = true;
                            allowTalk = true;
                            if (couldOperateUI()) {
                                // 4.8
                                // 增加延迟处理，否则fragemnt还未加载进activity
                                postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (couldOperateUI()) {
                                            sendMessageImpl(message);
                                            sendToBeSentMessage();
                                            if (messageList != null) {
                                                messageList.refresh();
                                            }
                                        }
                                    }
                                }, 500);
                            }
                        }
                        
                        @Override
                        public boolean onDealError(int errorCode, Object result) {
                            ImProto.ChatInitResponse response = (ImProto.ChatInitResponse) result;
                            switch (errorCode) {
                                case 1011:
                                    UserProto.ChatUserInfo chatUserInfo = response.member;
                                    updateMember(chatUserInfo);
                                    Logger.e(TAG, "Chat Init :" + errorCode);
                                    chatInit = true;
                                    allowTalk = false;
                                    talkUnAllowed(getErrorHintMessage(null));
                                    break;
                                default:
                                    Logger.e(TAG, "Chat Init response error");
                                    ToastWrapper.show(getErrorHintMessage(
                                            R.string.chat_init_failed_text));
                                    initFailed(message);
                                    break;
                            }
                            return true;
                        }
                        
                        @Override
                        public void onDealError(HttpError error, boolean isParseOK,
                                int errorCode, Object result) {
                            super.onDealError(error, isParseOK, errorCode, result);
                            if (!isParseOK)
                                initFailed(message);
                        }
                    }).req();
            
        }
        else if (chatType == Constant.CHATTYPE_GROUP) {
            ImProto.ChatGroupInitRequest builder = new ImProto.ChatGroupInitRequest();
            builder.chatGroupId = toChatUsername;
            if (chatScene == Constant.CHATSCNE_NEW_CREATE) {
                builder.scene = ImProto.ChatGroupInitScene.new_create_chat_group_init_scene;
                builder.hasScene = true;
            }
            
            newProtoReq(CommonUrl.CHAT_GROUP_INIT_URL.url()).setSendMsg(builder)
                    .setRspListener(
                            new ProtoListener(ImProto.ChatGroupInitResponse.class) {
                                @Override
                                public void onDealResult(Object result) {
                                    reqChatGroupMemebersIfNeeded();
                                    Logger.i(TAG, "Chat Group Init ok ");
                                    ImProto.ChatGroupInitResponse response = (ImProto.ChatGroupInitResponse) result;
                                    selfInfo = response.userInfo;
                                    chatInit = true;
                                    allowTalk = true;
                                    groupType = response.chatGroupType;
                                    ChatManager.getInstance().setGroupType(toChatUsername,
                                            groupType);
                                    if (ChatManager.getInstance()
                                            .getContactService() != null) {
                                        ChatManager.getInstance().getContactService()
                                                .saveGroupContactInfo(selfInfo,
                                                        toChatUsername);
                                    }
                                    loadUserIndex();
                                    if (couldOperateUI()) {
                                        sendMessageImpl(message);
                                        if (messageList != null) {
                                            messageList.refresh();
                                        }
                                    }
                                    
                                    if (response.hasGroupAnnounce
                                            && !response.groupAnnounce.equals(ChatManager
                                                    .getInstance()
                                                    .getGroupAnnounce(toChatUsername))) {
                                        ChatManager.getInstance().setGroupAnnounce(
                                                toChatUsername, response.groupAnnounce);
                                    }
                                    if (ChatManager.getInstance()
                                            .hasNewGroupAnnounce(toChatUsername)) {
                                        popGroupAnnounceDialog();
                                    }
                                    ChatManager.getInstance().asyncFetchGroupFromServer(
                                            toChatUsername, null);
                                    
                                    if (couldOperateUI()) {
                                        // 4.8
                                        // 增加延迟处理，否则fragemnt还未加载进activity
                                        postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (couldOperateUI()) {
                                                    sendToBeSentMessage();
                                                    if (messageList != null) {
                                                        messageList.refresh();
                                                    }
                                                }
                                            }
                                        }, 500);
                                    }
                                }
                                
                                @Override
                                public boolean onDealError(int errorCode, Object result) {
                                    
                                    switch (errorCode) {
                                        case 1011:
                                            reqChatGroupMemebersIfNeeded();
                                            Logger.e(TAG,
                                                    "Chat Group Init :" + errorCode);
                                            chatInit = true;
                                            allowTalk = false;
                                            talkUnAllowed(getErrorHintMessage(null));
                                            break;
                                        case 1014:
                                            reqChatGroupMemebersIfNeeded();
                                            Logger.e(TAG,
                                                    "Chat Group Init :" + errorCode);
                                            chatInit = true;
                                            allowTalk = false;
                                            forbidAction = true;
                                            ToastWrapper.showWithIcon(
                                                    R.string.chat_init_failed_not_in_group,
                                                    R.drawable.icon_task_warning);
                                            break;
                                        default:
                                            Logger.e(TAG,
                                                    "Chat Group Init response error");
                                            ToastWrapper.show(getErrorHintMessage(
                                                    R.string.chat_init_failed_text));
                                            initFailed(message);
                                            break;
                                    }
                                    return true;
                                }
                                
                                @Override
                                public void onDealError(HttpError error,
                                        boolean isParseOK, int errorCode, Object result) {
                                    super.onDealError(error, isParseOK, errorCode,
                                            result);
                                    if (!isParseOK)
                                        initFailed(message);
                                }
                                
                            })
                    .req();
        }
    }
    
    private void loadUserIndex() {
        if (groupType == ImProto.ChatGroupType.teaching_research_chat_group_type) {
            ArrayList<String> userIds = new ArrayList<>();
            for (EMMessage each : conversation.getAllMessages()) {
                CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(each);
                if (cmdMsg == null) {
                    userIds.add(each.getFrom());
                }
                else if (cmdMsg.isGroupCmdMsg()) {
                    Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
                    String userId = bundle.getString(CmdMsg.Group.QQ_USER_ID);
                    if (!TextUtils.isEmpty(userId)) {
                        userIds.add(userId);
                    }
                }
            }
            TeacherIndexManager.getInstance()
                    .loadGroupData(userIds.toArray(new String[] {}));
        }
    }
    
    protected void popGroupAnnounceDialog() {
        ChatManager.getInstance().setGroupAnnounceRead(toChatUsername, true);
        
        String groupAnnounce = ChatManager.getInstance().getGroupAnnounce(toChatUsername);
        if (TextUtils.isEmpty(groupAnnounce)) {
            return;
        }
        final CompDefaultDialog groupAnnounceDialog = new CompDefaultDialogBuilder(
                getActivity()).setCloseDrawable(R.drawable.icon_notice_close)
                        .setTitle(R.string.chat_group_announce_title)
                        .setCloseDrawableGravity(Gravity.RIGHT)
                        .setCloseDrawablePadding(
                                getResources().getDimension(R.dimen.dimen_6))
                        .setContent(groupAnnounce).setComponentHeader(null)
                        .setComponentContent(
                                new CompDialogScrollTextViewContent(getActivity()))
                        .setTheme(R.style.CompDialogTheme_Notice)
                        .setComponentConsole(
                                new CompDialogBackgroundColorConsole(getActivity()))
                        .setComponentFooter(new CompDialogDefaultFooter(getActivity()))
                        .setPositiveButton(R.string.chat_group_announce_check_content,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.dismiss();
                                        onEnterGroupAnnounce();
                                    }
                                })
                        .setCancelable(false).build();
        groupAnnounceDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                adjustDialogUI(dialog);
            }
        });
        groupAnnounceDialog.show();
    }
    
     void adjustDialogUI(DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        // adjust title
        int padding = getResources().getDimensionPixelSize(R.dimen.dimen_20);
        CompDialogUIComponent title = ((CompDefaultDialog) dialog)
                .getUIComponent(CompDialogUIComponent.Type.TITLE);
        TextView titleView = (TextView) title.getContentView();
        int titleBackground = ThemeConstant.getThemeImChatNoticeTitleIcon(getContext());
        titleView.setPadding(padding, padding, padding, padding);
        if (titleBackground != 0) {
            titleView.setBackgroundResource(titleBackground);
        }
        titleView.setTextColor(Color.WHITE);
        
        // adjust content
        // 1.群公告最多显示8行，大于8行时...
        // 2. 群公告只有一行时居中，多于一行时左对齐
        int contentMaxLines = 8;
        CompDialogUIComponent content = ((CompDefaultDialog) dialog)
                .getUIComponent(CompDialogUIComponent.Type.CONTENT);
        final TextView textView = (TextView) content.getContentView();
        float lineSpacing = getResources().getDimension(R.dimen.dimen_5);
        int lineCount = textView.getLineCount();
        textView.setPadding(padding, padding, padding,
                getResources().getDimensionPixelSize(R.dimen.dimen_12));
        textView.setLineSpacing(lineSpacing, 1);
        if (getActivity() == null) {
            textView.setTextColor(Color.BLACK);
        }
        else {
            // Fabric #2352 Fragment a{1c403922} not attached to Activity
            textView.setTextColor(getResources().getColor(R.color.black));
        }
        textView.setMovementMethod(new ScrollingMovementMethod());
        if (lineCount > 1) {
            textView.setGravity(Gravity.LEFT);
        }
        else {
            textView.setGravity(Gravity.CENTER);
        }
        
        // adjust buttons
        // 群公告少于8行时，不显示查看全部按钮
        CompDialogUIComponent console = ((CompDefaultDialog) dialog)
                .getUIComponent(CompDialogUIComponent.Type.CONSOLE);
        if (lineCount > contentMaxLines) {
            View view = console
                    .findViewById(com.qingqing.qingqingbase.R.id.compat_dlg_positive_btn);
            if (view != null) {
                int buttonBackground = ThemeConstant
                        .getThemeImChatNoticeButtonIcon(getContext());
                if (buttonBackground != 0) {
                    view.setBackgroundResource(buttonBackground);
                }
            }
            console.setVisibility(View.VISIBLE);
        }
        else {
            console.setVisibility(View.GONE);
        }
        
        textView.setMaxLines(contentMaxLines);
        textView.setEllipsize(TextUtils.TruncateAt.END);
    }
    
    protected void onEnterGroupAnnounce() {
        
    }
    
    protected void reqChatGroupMemebersIfNeeded() {
        Logger.i(TAG, "reqChatGroupMemebersIfNeeded" + chatType);
        if (chatType == Constant.CHATTYPE_GROUP) {
            EMGroup group = EMGroupManager.getInstance().getGroup(toChatUsername);
            if (group != null) {
                List<String> memberUserNames = group.getMembers();
                if (memberUserNames != null && memberUserNames.size() > 0) {
                    reqChatGroupMemebers(getUsersNotInLocal(group));
                }
                else {
                    Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(
                                @io.reactivex.annotations.NonNull ObservableEmitter<Object> e)
                                throws Exception {
                            try {
                                EMGroup group = EMGroupManager.getInstance()
                                        .getGroupFromServer(toChatUsername);
                                reqChatGroupMemebers(getUsersNotInLocal(group));
                            } catch (EaseMobException e1) {
                                e1.printStackTrace();
                            }
                            e.onComplete();
                        }
                    }).subscribeOn(Schedulers.computation()).subscribe();
                }
            }
        }
    }
    
    private List<String> getUsersNotInLocal(EMGroup group) {
        List<String> groupMembers = group != null ? group.getMembers() : null;
        List<String> usersNotInLocal = new ArrayList<>();
        if (groupMembers != null && groupMembers.size() > 0) {
            String[] copyOf = groupMembers.toArray(new String[] {}); // # Fabric 526
                                                                     // ConcurrentModificationException
            for (int i = 0, size = copyOf.length; i < size; i++) {
                String userName = copyOf[i];
                ContactInfo contactInfo = ChatManager.getInstance().getContactModel()
                        .getContactInfo(userName);
                if (contactInfo == null
                        || contactInfo.getGroupRole(toChatUsername) == null) {
                    usersNotInLocal.add(userName);
                    Logger.i(TAG, "getUsersNotInLocal userName = " + "  " + userName);
                }
            }
        }
        return usersNotInLocal;
    }
    
    protected void reqChatGroupMemebers(List<String> userNames) {
        if (userNames == null || userNames.size() <= 0) {
            return;
        }
        ImProto.ChatGroupMemberInfoRequest builder = new ImProto.ChatGroupMemberInfoRequest();
        builder.chatGroupId = toChatUsername;
        ArrayList<String> strings = new ArrayList<String>();
        for (String userName : userNames) {
            strings.add(userName);
        }
        builder.qingqingUserIds = strings.toArray(new String[] {});
        newProtoReq(CommonUrl.CHAT_GROUP_MEMBER_URL.url()).setSendMsg(builder)
                .setRspListener(
                        new ProtoListener(ImProto.ChatGroupMemberInfoResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                ImProto.ChatGroupMemberInfoResponse response = (ImProto.ChatGroupMemberInfoResponse) result;
                                Logger.i(TAG, "reqChatGroupMemebers onResponse");
                                updateMemberInfos(response.members, toChatUsername);
                            }
                            
                            @Override
                            public boolean onDealError(int errorCode, Object result) {
                                if (errorCode == 1014) {
                                    return true;
                                }
                                return false;
                            }
                        })
                .req();
    }
    
    protected void updateMemberInfos(UserProto.ChatUserInfo[] chatUserInfos,
            String groupId) {
        if (chatUserInfos != null && chatUserInfos.length > 0) {
            for (UserProto.ChatUserInfo chatUserInfo : chatUserInfos) {
                updateGroupMember(chatUserInfo, groupId);
            }
            if (couldOperateUI() && messageList != null) {
                messageList.refresh();
            }
        }
        else {
            Logger.e(TAG, "updateMemberInfos 0");
        }
    }
    
    protected void updateMember(UserProto.ChatUserInfo chatUserInfo) {
        
    }
    
    protected void updateGroupMember(UserProto.ChatUserInfo chatUserInfo,
            String groupId) {
        
    }
    
    void initFailed(EMMessage message) {
        if (message != null && (message.getBody() instanceof TextMessageBody)
                && couldOperateUI() && editText != null) {
            editText.setText(((TextMessageBody) message.getBody()).getMessage());
        }
    }
    
    /**
     * init接口返回用户信息，用于判断是否是管理员
     * 
     * @return
     */
    @Nullable
    protected int[] getCurrentUserRole() {
        if (selfInfo == null) {
            return null;
        }
        return selfInfo.userRole;
    }
}
