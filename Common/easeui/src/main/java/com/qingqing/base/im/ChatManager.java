package com.qingqing.base.im;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMGroupChangeListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.BuildConfig;
import com.easemob.easeui.R;
import com.easemob.easeui.controller.EaseUI;
import com.easemob.easeui.model.EaseAtMessageHelper;
import com.easemob.easeui.model.EaseNotifier;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.google.protobuf.nano.MessageNano;
import com.qingqing.api.proto.v1.AssistantProto;
import com.qingqing.api.proto.v1.StudentProto;
import com.qingqing.api.proto.v1.TeacherProto;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.consult.Consult;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.core.CountDownCenter;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.ChatSettingsManager;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.HttpReq;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.im.domain.ChatMessage;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.domain.ExtField;
import com.qingqing.base.im.receiver.CallReceiver;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.im.utils.PreferenceManager;
import com.qingqing.base.log.Logger;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.DeviceUtil;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.view.ToastWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

import static com.qingqing.base.im.CmdMsgParser.getCmdMsg;
import static com.qingqing.base.im.CmdMsgParser.parseCmdMsgBody;

/**
 * Created by huangming on 2015/12/21.
 */
public class ChatManager {
    
    protected static final String TAG = "ChatManager";
    private static final String KEY_STICK_TOP = "stick_top";
    private static final String KEY_GROUP_TYPE = "group_type";
    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_GROUP_NAME = "group_name";
    private static final String KEY_GROUP_ANNOUNCE = "group_announce";
    private static final String KEY_GROUP_HAS_NEW_ANNOUNCE = "group_has_new_announce";
    private static final String KEY_GROUP_HAS_NEW_RANK = "group_has_new_rank";
    private static final String REQ_GROUP_CONTACT_TAG = "ReqGroupContact";
    private static final int CHECK_INTERVAL = 60;
    
    private static volatile ChatManager sInstance;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> toReqUserBuffer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> backupReqUserBuffer = new ConcurrentHashMap<>();
    /**
     * EMEventListener
     */
    protected EMEventListener eventListener = null;
    HashMap<String, GroupAnnouncementUpdateListener> mGroupAnnouncementUpdateListener = new HashMap<>();
    private EaseUI easeUI;
    private ChatModel chatModel = null;
    /**
     * HuanXin sync groups status listener
     */
    private List<DataSyncListener> syncGroupsListeners;
    /**
     * HuanXin sync blacklist status listener
     */
    private List<DataSyncListener> syncBlackListListeners;
    private boolean isSyncingGroupsWithServer = false;
    private boolean isSyncingGroupsListWithApi = false;
    private boolean isGroupsSyncedWithServer = false;
    private boolean alreadyNotified = false;
    private Context appContext;
    private CallReceiver callReceiver;
    private EMConnectionListener connectionListener;
    private LocalBroadcastManager broadcastManager;
    private boolean isGroupAndContactListenerRegistered;
    private ContactService contactService;
    private Handler handler;
    private EaseNotifier.EaseNotificationInfoProvider mCustomerNotifyProvider;
    private EaseNotifier.EaseNotificationInfoProvider mDefaultNotifyProvider = new EaseNotifier.EaseNotificationInfoProvider() {
        
        @Override
        public String getTitle(EMMessage message) {
            // 修改标题,这里使用默认
            return null;
        }
        
        @Override
        public int getSmallIcon(EMMessage message) {
            // 设置小图标，这里为默认
            return 0;
        }
        
        @Override
        public String getDisplayedText(EMMessage message) {
            // 设置状态栏的消息提示，可以根据message的类型做相应提示
            return appContext.getString(R.string.chat_notify_text);
        }
        
        @Override
        public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
            return null;
            // return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
        }
        
        @Override
        public Intent getLaunchIntent(EMMessage message) {
            // 设置点击通知栏跳转事件
            Intent intent = new Intent(
                    Constant.ACTION_IM_MESSAGE + "_" + PackageUtil.getPackageName());
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return intent;
        }
    };
    private List<ChatCallback> mChatCallbackList = new ArrayList<>();
    
    private ChatManager() {}
    
    public static ChatManager getInstance() {
        if (sInstance == null) {
            synchronized (ChatManager.class) {
                if (sInstance == null) {
                    sInstance = new ChatManager();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 根据用户 Id
     * 在数据库中查询用户类型，调取接口获取用户的电话号码，并拨打电话，尽量使用{@link #callContact(String, ContactInfo.Type)}替代
     * <p/>
     * 注:需保证用户在数据库中存在，当前未做默认类型处理，当数据库不存在时记录日志上传服务器
     *
     * @param qingQingUserId
     *            用户 Id
     */
    public static void callContact(String qingQingUserId) {
        ContactInfo contactInfo = ChatManager.getInstance().getContactModel()
                .getContactInfo(qingQingUserId);
        if (contactInfo != null) {
            callContact(qingQingUserId, contactInfo.getType());
        }
        else {
            Logger.o("call contact without user info in database.");
        }
    }
    
    /**
     * 根据 用户 Id 和 用户类型，调取接口获取用户的电话号码，并拨打电话
     *
     * @param qingQingUserId
     *            用户 Id
     * @param type
     *            用户类型
     */
    public static void callContact(String qingQingUserId, ContactInfo.Type type) {
        getUserPhoneNumber(qingQingUserId, type, new GetPhoneNumberCallback() {
            @Override
            public void onSuccess(String phoneNumber) {
                DeviceUtil.makeCall(phoneNumber);
            }
        });
    }
    
    /**
     * 根据 用户 Id 和 用户类型，调取接口获取用户的电话号码
     *
     * @param qingQingUserId
     *            用户 Id
     * @param type
     *            用户类型
     */
    public static void getPhoneNum(String qingQingUserId, ContactInfo.Type type,
            GetPhoneNumberCallback getPhoneNumberCallback) {
        getUserPhoneNumber(qingQingUserId, type, getPhoneNumberCallback);
    }
    
    /**
     * 根据 用户
     * Id，调取接口获取用户的电话号码，成功时回调，尽量使用{@link #getUserPhoneNumber(String, ContactInfo.Type, GetPhoneNumberCallback)}
     * 替代
     * <p/>
     * 注意:需保证用户在数据库中存在，当前未做默认类型，当数据库不存在时记录日志上传服务器
     *
     * @param qingQingUserId
     *            用户 Id
     * @param callback
     *            获取电话成功时的回调
     */
    public static void getUserPhoneNumber(String qingQingUserId,
            GetPhoneNumberCallback callback) {
        ContactInfo contactInfo = ChatManager.getInstance().getContactModel()
                .getContactInfo(qingQingUserId);
        if (contactInfo != null) {
            getUserPhoneNumber(qingQingUserId, contactInfo.getType(), callback);
        }
        else {
            Logger.o("get user phone number without user info in database.");
        }
    }
    
    /**
     * 根据 用户 Id 和 用户类型，调取接口获取用户的电话号码，成功时回调
     *
     * @param qingQingUserId
     *            用户 Id
     * @param type
     *            用户类型
     * @param callback
     *            获取电话成功时的回调
     */
    public static void getUserPhoneNumber(String qingQingUserId, ContactInfo.Type type,
            final GetPhoneNumberCallback callback) {
        if (type == null) {
            return;
        }
        
        HttpUrl urlConfig = null;
        MessageNano message = null;
        
        switch (type) {
            case Teacher:
                if (DefaultDataCache.INSTANCE()
                        .getAppType() == AppCommon.AppType.qingqing_ta) {
                    urlConfig = CommonUrl.TA_TEACHER_PHONE_URL.url();
                }
                else {
                    urlConfig = CommonUrl.GET_TEACHER_PHONE_NUMBER.url();
                }
                message = new TeacherProto.SimpleQingQingTeacherIdRequest();
                ((TeacherProto.SimpleQingQingTeacherIdRequest) message).qingqingTeacherId = qingQingUserId;
                break;
            case Student:
                if (DefaultDataCache.INSTANCE()
                        .getAppType() == AppCommon.AppType.qingqing_ta) {
                    urlConfig = CommonUrl.TA_CUSTOMER_PHONE_URL.url();
                    message = new UserProto.SimpleQingQingUserIdRequest();
                    ((UserProto.SimpleQingQingUserIdRequest) message).qingqingUserId = qingQingUserId;
                }
                else {
                    urlConfig = CommonUrl.GET_STUDENT_PHONE_NUMBER.url();
                    message = new StudentProto.SimpleQingQingStudentIdRequest();
                    ((StudentProto.SimpleQingQingStudentIdRequest) message).qingqingStudentId = qingQingUserId;
                }
                break;
            case Assistant:
                urlConfig = CommonUrl.GET_ASSISTANT_PHONE_NUMBER.url();
                message = new AssistantProto.SimpleQingQingAssistantIdRequest();
                ((AssistantProto.SimpleQingQingAssistantIdRequest) message).qingqingAssistantId = qingQingUserId;
                break;
            default:
                return;
        }
        
        final HttpUrl requestType = urlConfig;
        new ProtoReq(urlConfig).setSendMsg(message)
                .setRspListener(new ProtoListener(Consult.GetPhoneNumberResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        Consult.GetPhoneNumberResponse response = (Consult.GetPhoneNumberResponse) result;
                        if (response.phoneNumber.length != 0
                                && !TextUtils.isEmpty(response.phoneNumber[0])) {
                            callback.onSuccess(response.phoneNumber[0]);
                        }
                        else {
                            // 4.9.5 老师端试听课联系家长，提示文案需单独处理
                            if (requestType == CommonUrl.GET_STUDENT_PHONE_NUMBER.url()
                                    && BaseData
                                            .getClientType() == AppCommon.AppType.qingqing_teacher) {
                                ToastWrapper.show(
                                        R.string.base_empty_call_number_trial_class);
                            }
                            else {
                                ToastWrapper
                                        .show(R.string.base_empty_call_number_default);
                            }
                        }
                    }
                    
                    @Override
                    public boolean onDealError(int errorCode, Object result) {
                        ToastWrapper.show(
                                getErrorHintMessage(R.string.base_call_unknown_error));
                        return true;
                    }
                }).req();
    }
    
    public void init(Context context) {
        handler = new Handler(context.getApplicationContext().getMainLooper());
        appContext = context;
        easeUI = EaseUI.getInstance();
        easeUI.init(context);
        // 设为调试模式，打成正式包时，最好设为false，以免消耗额外的资源
        if (BuildConfig.DEBUG)
            EMChat.getInstance().setDebugMode(true);
        // get easeui instance
        // 调用easeui的api设置providers
        setEaseUIProviders();
        chatModel = new ChatModel(context);
        if (isQQLoggedIn()) {
            chatModel.init();
        }
        // 设置chat options
        setChatOptions();
        // 初始化PreferenceManager
        PreferenceManager.init(context);
        
        // 设置全局监听
        setGlobalListeners();
        broadcastManager = LocalBroadcastManager.getInstance(appContext);
        
        AudioPlayerController
                .setSpeakerOpened(easeUI.getSettingsProvider().isSpeakerOpened());
        
        // 定时请求待查的群组身份信息（1分钟1次）
        CountDownCenter.INSTANCE().addRepeatTask(REQ_GROUP_CONTACT_TAG, CHECK_INTERVAL,
                new CountDownCenter.CountDownListener() {
                    @Override
                    public void onCountDown(String tag, int leftCount) {
                        if (leftCount == 0) {
                            checkAndReqContact();
                        }
                    }
                });
    }
    
    private void setChatOptions() {
        // easeui库默认设置了一些options，可以覆盖
        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        options.allowChatroomOwnerLeave(true);
    }
    
    public void setCustomerNotifyProvider(
            EaseNotifier.EaseNotificationInfoProvider provider) {
        mCustomerNotifyProvider = provider;
    }
    
    protected void setEaseUIProviders() {
        // 需要easeui库显示用户头像和昵称设置此provider
        easeUI.setUserProfileProvider(new EaseUI.EaseUserProfileProvider() {
            
            @Override
            public ContactInfo getUser(String username) {
                return getContactModel().getContactInfo(username);
            }
        });
        
        // 不设置，则使用easeui默认的
        easeUI.setSettingsProvider(new EaseUI.EaseSettingsProvider() {
            
            @Override
            public boolean isSpeakerOpened() {
                return chatModel.getSettingMsgSpeaker();
            }
            
            @Override
            public boolean isMsgVibrateAllowed(EMMessage message) {
                if (isMsgNotificationIgnored(message)) {
                    return false;
                }
                return chatModel.getSettingMsgVibrate();
            }
            
            @Override
            public boolean isMsgSoundAllowed(EMMessage message) {
                if (isMsgNotificationIgnored(message)) {
                    return false;
                }
                return chatModel.getSettingMsgSound();
            }
            
            @Override
            public boolean isMsgNotifyAllowed(EMMessage message) {
                if (isMsgNotificationIgnored(message)) {
                    return false;
                }
                return chatModel.getSettingMsgNotification();
            }
        });
        // 不设置，则使用easeui默认的
        easeUI.getNotifier().setNotificationInfoProvider(
                mCustomerNotifyProvider != null ? mCustomerNotifyProvider
                        : mDefaultNotifyProvider);
    }
    
    private boolean isMsgNotificationIgnored(EMMessage message) {
        return ChatSettingsManager.getInstance()
                .isMsgNotificationIgnored(getConversationId(message));
    }
    
    /**
     * 设置全局事件监听
     */
    protected void setGlobalListeners() {
        syncGroupsListeners = new ArrayList<DataSyncListener>();
        syncBlackListListeners = new ArrayList<DataSyncListener>();
        
        isGroupsSyncedWithServer = chatModel.isGroupsSynced();
        
        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                Logger.o("onDisconnected : " + error);
                if (error == EMError.USER_REMOVED) {
                    onCurrentAccountRemoved();
                }
                else if (error == EMError.CONNECTION_CONFLICT) {
                    onConnectionConflict();
                }
                else {
                    onCurrentAccountDisconnected();
                }
            }
            
            @Override
            public void onConnected() {
                Logger.o("onConnected");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Object> e)
                            throws Exception {
                        notifyForRecevingEvents();
                        e.onComplete();
                    }
                }).subscribeOn(Schedulers.computation()).subscribe();
                if (!isGroupsSyncedWithServer) {
                    asyncFetchGroupsFromServer(null);
                }
                chatModel.init();
                onCurrentAccountConnected();
            }
        };
        
        IntentFilter callFilter = new IntentFilter(
                EMChatManager.getInstance().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }
        
        // 注册通话广播接收者
        appContext.registerReceiver(callReceiver, callFilter);
        // 注册连接监听
        EMChatManager.getInstance().addConnectionListener(connectionListener);
        // 注册群组和联系人监听
        registerGroupAndContactListener();
        // 注册消息事件监听
        registerEventListener();
        
    }
    
    /**
     * 注册群组和联系人监听，由于logout的时候会被sdk清除掉，再次登录的时候需要再注册一下
     */
    public void registerGroupAndContactListener() {
        if (!isGroupAndContactListenerRegistered) {
            // 注册群组变动监听
            EMGroupManager.getInstance()
                    .addGroupChangeListener(new MyGroupChangeListener());
            // 注册联系人变动监听
            EMContactManager.getInstance().setContactListener(new MyContactListener());
            isGroupAndContactListenerRegistered = true;
        }
        
    }
    
    /**
     * 账号在别的设备登录
     */
    protected void onConnectionConflict() {
        Logger.o(TAG, "onConnectionConflict");
        login(true, null);
    }
    
    /**
     * 账号被移除
     */
    protected void onCurrentAccountRemoved() {
        Logger.o(TAG, "onCurrentAccountRemoved");
        login(true, null);
    }
    
    protected void onCurrentAccountDisconnected() {
        Logger.o(TAG, "onCurrentAccountDisconnected");
    }
    
    protected void onCurrentAccountConnected() {
        Logger.o(TAG, "onCurrentAccountConnected");
    }
    
    /**
     * 全局事件监听 因为可能会有UI页面先处理到这个消息，所以一般如果UI页面已经处理，这里就不需要再次处理 activityList.size() <= 0
     * 意味着所有页面都已经在后台运行，或者已经离开Activity Stack
     */
    protected void registerEventListener() {
        eventListener = new EMEventListener() {
            @Override
            public void onEvent(EMNotifierEvent event) {
                EMMessage message = null;
                if (event.getData() instanceof EMMessage) {
                    message = (EMMessage) event.getData();
                    EMLog.d(TAG, "receive the event : " + event.getEvent() + ",id : "
                            + message.getMsgId());
                }
                
                if (message != null) {
                    Logger.i(TAG,
                            "onEvent : msg is id " + message.getMsgId() + "  form "
                                    + message.getFrom() + ", body : "
                                    + message.getBody().toString());
                }
                else {
                    Log.e(TAG, "onEvent : msg is null");
                    return;
                }
                Logger.o(TAG, "Message arrived : " + event.getEvent());
                
                // 必须要加上此句话，否则连续发送两条消息，只显示一条unread
                ChatConversationInitializer.getInstance().initChatConversationBy(message);
                EMConversation conversation = EMChatManager.getInstance()
                        .getConversation(getConversationId(message));
                ChatRoomModel chatRoomModel = null;
                if (message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    if (conversation != null) {
                        // 设置为read
                        conversation.getMessage(message.getMsgId());
                        
                        // 已经设置禁言时非管理员用户不能接受到消息
                        chatRoomModel = ChatRoomModel
                                .getModel(conversation.getUserName());
                        if (chatRoomModel != null
                                && chatRoomModel.isCurrentUserSpeakForbidden()) {
                            ChatMessage chatMessage = new ChatMessage(message);
                            for (int i = 0; i < chatMessage.getChatRoomType()
                                    .size(); i++) {
                                int role = chatMessage.getChatRoomType().get(i);
                                boolean canSpeak = false;
                                if (role == ImProto.ChatRoomRoleType.mc_chat_room_role_type
                                        || role == ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                                    canSpeak = true;
                                }
                                
                                if (!canSpeak) {
                                    conversation.removeMessage(message.getMsgId());
                                }
                            }
                        }
                    }
                }
                else if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                    if (conversation != null) {
                        
                        // 从本地或网络获取群组信息
                        EMGroup group = EMGroupManager.getInstance()
                                .getGroup(message.getTo());
                        if (group == null) {
                            try {
                                group = EMGroupManager.getInstance()
                                        .getGroupFromServer(message.getTo());
                            } catch (EaseMobException e) {
                            
                            }
                        }
                        
                        if (group != null) {
                            setConversationExtra(conversation, KEY_GROUP_ID,
                                    group.getGroupId());
                            setConversationExtra(conversation, KEY_GROUP_NAME,
                                    group.getGroupName());
                        }
                    }
                }
                
                ExtField extField = checkExtField(conversation, message);
                EaseAtMessageHelper.get().parseMessages(message);
                
                switch (event.getEvent()) {
                    case EventNewMessage:
                        AudioBatchProcesser.getInstance(appContext)
                                .asyncFetchMessageIfNeeded(message);
                        // 应用在后台，不需要刷新UI,通知栏提示新消息
                        if (!AppUtil.isAppForeground()
                                && message.getChatType() != EMMessage.ChatType.ChatRoom) {
                            getNotifier().onNewMsg(message);
                        }
                        if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                            asyncFetchGroupFromServer(message.getTo(), null);
                            checkGroupContact(message);
                        }
                        // 回报接收状态
                        IMUtils.uploadIMMsgReceived(message);
                        break;
                    case EventOfflineMessage:
                        if (!AppUtil.isAppForeground()
                                && message.getChatType() != EMMessage.ChatType.ChatRoom) {
                            EMLog.d(TAG, "received offline messages");
                            List<EMMessage> messages = (List<EMMessage>) event.getData();
                            getNotifier().onNewMesg(messages);
                            // 回报接收状态
                            IMUtils.uploadIMMsgReceived(messages);
                        }
                        if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                            asyncFetchGroupFromServer(message.getTo(), null);
                            checkGroupContact(message);
                        }
                        break;
                    // below is just giving a example to show a cmd toast, the
                    // app should not follow this
                    // so be careful of this
                    case EventNewCMDMessage:
                        CmdMsg cmdMsg = getCmdMsg(message);
                        if (cmdMsg.isReceiveMsg) {
                            Logger.i(TAG, "EventNewCMDMessage " + cmdMsg.msgType + "  "
                                    + extField.isSelfMock + "   " + message.isUnread());
                            
                            boolean notify = !cmdMsg.isLectureCmdMsg();
                            message.setAttribute("em_ignore_notification", !notify);
                            
                            EMChatManager.getInstance().importMessage(message, true);
                            
                            if (chatRoomModel != null) {
                                chatRoomModel.handleLectureCmdMsg(cmdMsg);
                            }
                            
                            if (cmdMsg.msgType == CmdMsg.CMD_TYPE_ST_BIND_TA) {
                                getContactModel().asyncFetchData(message.getFrom(),
                                        ContactInfo.Type.Student);
                            }
                            else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_CONSULT_ST) {
                                // 当咨询时需要判断是否是绑定
                                getContactModel().asyncFetchData(message.getFrom(),
                                        ContactInfo.Type.Assistant, false/** 非必需拉取 */
                                );
                            }
                            else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_CONSULT_TA) {
                                getContactModel().asyncFetchData(message.getFrom(),
                                        ContactInfo.Type.Student, false/** 非必需拉取 */
                                );
                            }
                            else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_SINGLE_REVOKE_MESSAGE) {
                                // 单聊撤回，双方均要看见cmd消息
                                Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
                                String userName = bundle
                                        .getString(CmdMsg.Group.QQ_USER_ID);
                                String revokeMsgId = bundle
                                        .getString(CmdMsg.Group.MSG_ID);
                                
                                if (conversation != null) {
                                    if (BaseData.getSafeUserId().equals(userName)) {
                                        EMMessage revokeMsg = conversation
                                                .getMessage(revokeMsgId);
                                        if (revokeMsg != null) {
                                            message.setAttribute(
                                                    CmdMsgParser.REVOKE_MSG_USER_ID,
                                                    userName);
                                        }
                                    }
                                    conversation.removeMessage(revokeMsgId);
                                }
                            }
                            else if (cmdMsg.isGroupCmdMsg()) {
                                boolean needShowUnread = false;
                                // 保存群公告
                                if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_ANNOUNCE) {
                                    if (conversation != null) {
                                        Bundle bundle = CmdMsgParser
                                                .parseCmdMsgBody(cmdMsg);
                                        String announce = bundle
                                                .getString(CmdMsg.Group.GROUP_ANNOUNCE);
                                        if (announce != null) {
                                            setGroupAnnounce(getConversationId(message),
                                                    announce);
                                        }
                                        GroupAnnouncementUpdateListener listener = mGroupAnnouncementUpdateListener
                                                .get(message.getTo());
                                        if (listener != null) {
                                            listener.onGroupAnnouncementUpdate();
                                        }
                                        conversation.removeMessage(message.getMsgId());
                                    }
                                }
                                // 只显示自己发出的撤回消息
                                else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_REVOKE_MESSAGE) {
                                    Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
                                    String userName = bundle
                                            .getString(CmdMsg.Group.QQ_USER_ID);
                                    String revokeMsgId = bundle
                                            .getString(CmdMsg.Group.MSG_ID);
                                    
                                    if (conversation != null) {
                                        if (BaseData.getSafeUserId().equals(userName)) {
                                            EMMessage revokeMsg = conversation
                                                    .getMessage(revokeMsgId);
                                            if (revokeMsg != null) {
                                                message.setAttribute(
                                                        CmdMsgParser.REVOKE_MSG_USER_ID,
                                                        userName);
                                            }
                                        }
                                        conversation.removeMessage(revokeMsgId);
                                    }
                                    
                                    if (!BaseData.getSafeUserId().equals(userName)) {
                                        if (conversation != null) {
                                            conversation
                                                    .removeMessage(message.getMsgId());
                                        }
                                    }
                                }
                                else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_RANK) {
                                    if (conversation != null) {
                                        setHasGroupNewRank(getConversationId(message),
                                                true);
                                    }
                                    needShowUnread = true;
                                }
                                else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_COURSE_REPORT) {
                                    needShowUnread = true;
                                }
                                else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_SHARE_PLAN_SUMMARIZE) {
                                    needShowUnread = true;
                                }
                                else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_OVERDUE_APPLY_CANCEL_COURSE) {
                                    needShowUnread = true;
                                }
                                
                                // 按需拉取群列表信息
                                if (needRefreshGroupList(cmdMsg)) {
                                    getGroupListType();
                                }
                                // 不需提醒的消息
                                if (!needIgnoreMessage(cmdMsg)) {
                                    handleGroupCmdMsg(message, cmdMsg);
                                }
                                checkGroupContact(message);
                                if (conversation != null && !needShowUnread) {
                                    conversation.markMessageAsRead(message.getMsgId());
                                }
                            }
                            // EMChatManager.getInstance().saveMessage(message,
                            // true);
                            
                            // 回报接收状态
                            IMUtils.uploadIMMsgReceived(message);
                        }
                        else {
                            Log.e(TAG, "Can't receive cmd msg!");
                        }
                        
                        // 不需提醒的消息
                        if (conversation != null && needIgnoreMessage(cmdMsg)) {
                            conversation.removeMessage(message.getMsgId());
                        }
                        break;
                    case EventDeliveryAck:
                        message.setDelivered(true);
                        break;
                    case EventReadAck:
                        message.setAcked(true);
                        break;
                    // add other events in case you are interested in
                    default:
                        break;
                }
                
                // 新消息通知
                appContext.sendBroadcast(new Intent(Constant.ACTION_NEW_MESSAGE));
                
            }
        };
                            
        EMChatManager.getInstance().registerEventListener(eventListener);
    }
    
    private ExtField checkExtField(EMConversation conversation, EMMessage message) {
        ExtField ext = ExtFieldParser.getExt(message);
        if (ext.noShow) {
            getNotifier().onNewMsg(message);
            
            if (conversation != null) {
                conversation.removeMessage(message.getMsgId());
            }
        }
        
        return ext;
    }
    
    /**
     * 登录时检查/本地消息中的 extField
     */
    private void checkExtField(Iterator<EMMessage> messageIterator) {
        if (messageIterator == null) {
            return;
        }
        EMMessage message = messageIterator.next();
        
        if (message != null) {
            ExtField ext = ExtFieldParser.getExt(message);
            if (ext.noShow) {
                getNotifier().onNewMsg(message);
                
                messageIterator.remove();
            }
        }
    }
    
    private void checkOfflineMessages() {
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance()
                .getAllConversations();
        // fix fabric assistant #530
        if (conversations.size() > 0) {
            Iterator<EMConversation> conversationIterator = conversations.values()
                    .iterator();
            while (conversationIterator.hasNext()) {
                EMConversation conversation = conversationIterator.next();
                if (conversation != null && conversation.getAllMessages() != null) {
                    Iterator<EMMessage> messageIterator = conversation.getAllMessages()
                            .iterator();
                    while (messageIterator.hasNext()) {
                        checkExtField(messageIterator);
                    }
                }
            }
        }
    }
    
    private boolean needRefreshGroupList(CmdMsg cmdMsg) {
        if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_UPDATE
                || cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_INVITED
                || cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_CREATED) {
            return true;
        }
        return false;
    }
    
    public void checkGroupContact(EMMessage message) {
        if (getContactService() == null) {
            return;
        }
        
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        
        CopyOnWriteArrayList<String> reqUserIds = new CopyOnWriteArrayList<>();
        if (cmdMsg != null) {
            if (!needIgnoreMessage(cmdMsg)) {
                Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
                String userName = bundle.getString(CmdMsg.Group.QQ_USER_ID);
                ArrayList<String> members = bundle
                        .getStringArrayList(CmdMsg.Group.GROUP_MEMBERS);
                if (!TextUtils.isEmpty(userName)
                        && getContactService().getContactInfo(userName) == null) {
                    reqUserIds.add(userName);
                }
                if (members != null) {
                    for (String member : members) {
                        if (getContactService().getContactInfo(member) == null) {
                            reqUserIds.add(member);
                        }
                    }
                }
            }
        }
        else {
            if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                if (getContactService().getContactInfo(message.getFrom()) == null) {
                    reqUserIds.add(message.getFrom());
                }
                
                ArrayList<String> atUserList = EaseAtMessageHelper.get()
                        .getMsgAtUserList(message);
                for (String atUser : atUserList) {
                    if (getContactService().getContactInfo(atUser) == null) {
                        reqUserIds.add(atUser);
                    }
                }
            }
        }
        
        if (reqUserIds.size() > 0) {
            addToReqUserList(message.getTo(), reqUserIds);
        }
    }
    
    /**
     * 加入到待查群组联系人信息中
     *
     * @param groupId
     * @param userIds
     */
    private void addToReqUserList(String groupId, CopyOnWriteArrayList<String> userIds) {
        if (toReqUserBuffer.get(groupId) != null) {
            CopyOnWriteArrayList<String> arrayList = toReqUserBuffer.get(groupId);
            arrayList.addAll(userIds);
        }
        else {
            toReqUserBuffer.put(groupId, userIds);
        }
        // TODO: 如果buffer内大于一定数量的话，立刻请求？
    }
    
    /**
     * 定期调接口查询群组联系人信息
     */
    private void checkAndReqContact() {
        backupReqUserBuffer.clear();
        synchronized (toReqUserBuffer) {
            Iterator<Map.Entry<String, CopyOnWriteArrayList<String>>> iterator = toReqUserBuffer
                    .entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CopyOnWriteArrayList<String>> entry = iterator.next();
                String key = entry.getKey();
                CopyOnWriteArrayList<String> value = entry.getValue();
                backupReqUserBuffer.put(key, value);
            }
            toReqUserBuffer.clear();
        }
        
        reqContact();
    }
    
    private void reqContact() {
        Iterator<Map.Entry<String, CopyOnWriteArrayList<String>>> iterator = backupReqUserBuffer
                .entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CopyOnWriteArrayList<String>> entry = iterator.next();
            String key = entry.getKey();
            CopyOnWriteArrayList<String> value = entry.getValue();
            // 去重
            CopyOnWriteArrayList<String> userIds = new CopyOnWriteArrayList<>(
                    new HashSet<>(value));
            
            Iterator iterator1 = userIds.iterator();
            while (iterator1.hasNext()) {
                String userId = (String) iterator1.next();
                
                // 已有联系人群组信息的话，不查询
                ContactInfo contactInfo = getContactService().getContactInfo(userId);
                if (contactInfo != null && contactInfo.getGroupRole(key) != null) {
                    // CopyOnWriteArrayList 的 Iterator 不支持（remove、set 和
                    // add），需使用本身的修改操作
                    userIds.remove(userId);
                    // iterator1.remove();
                }
            }
            if (userIds.size() > 0) {
                getGroupContactInfo(key, userIds.toArray(new String[] {}));
            }
            iterator.remove();
        }
    }
    
    private boolean needIgnoreMessage(CmdMsg cmdMsg) {
        // 5.8.5 TA 端屏蔽“您已邀请某某某加入群聊”和“您已将某某某移出群聊”两种消息类型。
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta
                && (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_REMOVED
                        || cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_INVITED)) {
            Bundle bundle = parseCmdMsgBody(cmdMsg);
            String userName = bundle.getString(CmdMsg.Group.QQ_USER_ID);
            if (BaseData.getSafeUserId().equals(userName)) {
                return true;
            }
        }
        // 不显示与自己不相关的退群和加群消息
        if (cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_EXIT
                || cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_REMOVED
                || cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_INVITED) {
            Bundle bundle = parseCmdMsgBody(cmdMsg);
            String userName = bundle.getString(CmdMsg.Group.QQ_USER_ID);
            ArrayList<String> members = bundle
                    .getStringArrayList(CmdMsg.Group.GROUP_MEMBERS);
            if (!BaseData.getSafeUserId().equals(userName)
                    && (members == null || !members.contains(BaseData.getSafeUserId()))) {
                return true;
            }
        }
        return false;
    }
    
    public String getConversationId(EMMessage message) {
        String conversationId;
        if (message.getChatType() != EMMessage.ChatType.Chat) {
            conversationId = message.getTo();
        }
        else {
            conversationId = message.direct == EMMessage.Direct.SEND ? message.getTo()
                    : message.getFrom();
        }
        return conversationId;
    }
    
    public String getMockUserName(EMMessage message) {
        return getMockUserName(getConversationId(message));
    }
    
    public String getMockUserName(String conversationId) {
        String userName;
        ChatRoomModel chatRoomModel = ChatRoomModel.getModel(conversationId);
        if (chatRoomModel != null) {
            userName = chatRoomModel.getCurUserName();
        }
        else {
            userName = BaseData.qingqingUserId();
        }
        return userName;
        
    }
    
    public int getUnreadMsgsCount() {
        Collection<EMConversation> conversationCollection = EMChatManager.getInstance()
                .getAllConversations().values();
        int msgCount = 0;
        for (EMConversation conversation : conversationCollection) {
            if (conversation.getType() != EMConversation.EMConversationType.ChatRoom) {
                msgCount += conversation.getUnreadMsgCount();
            }
            else {
                conversation.markAllMessagesAsRead();
            }
        }
        return msgCount;
    }
    
    void handleGroupCmdMsg(EMMessage message, CmdMsg cmdMsg) {
        String groupId = message.getTo();
        Logger.i(TAG,
                "handleGroupCmdMsg type = " + cmdMsg.msgType + " groupId = " + groupId);
        if (!TextUtils.isEmpty(groupId)) {
            asyncFetchGroupFromServer(groupId, null);
            appContext.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }
    }
    
    /**
     * 是否登录成功过
     *
     * @return
     */
    public boolean isLoggedIn() {
        String qingqingUserId = BaseData.qingqingUserId();
        return EMChat.getInstance().isLoggedIn() && BaseData.isUserIDValid()
                && qingqingUserId != null
                && qingqingUserId.equals(EMChatManager.getInstance().getCurrentUser());
    }
    
    /**
     *
     */
    public boolean isQQLoggedIn() {
        return BaseData.isUserIDValid();
    }
    
    /**
     * 获取消息通知类
     *
     * @return
     */
    public EaseNotifier getNotifier() {
        return easeUI.getNotifier();
    }
    
    /**
     * 获取好友list
     */
    Map<String, ContactInfo> getContactList() {
        return chatModel.getContactModel().getContactList();
    }
    
    public String getCurrentUserName() {
        if (BaseData.isUserIDValid()) {
            return BaseData.qingqingUserId();
        }
        else {
            return "unknown";
        }
    }
    
    public ChatModel getChatModel() {
        return chatModel;
    }
    
    public ContactModel getContactModel() {
        return chatModel.getContactModel();
    }
    
    public void addSyncGroupListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (!syncGroupsListeners.contains(listener)) {
            syncGroupsListeners.add(listener);
        }
    }
    
    public void removeSyncGroupListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (syncGroupsListeners.contains(listener)) {
            syncGroupsListeners.remove(listener);
        }
    }
    
    /**
     * 增加群组免打扰
     *
     * @param groupId
     *            支持多个
     */
    public void addReceiveNotNotifyGroup(String... groupId) {
        List<String> noNotifyGroup = EMChatManager.getInstance().getChatOptions()
                .getReceiveNoNotifyGroup();
        if (noNotifyGroup == null) {
            noNotifyGroup = new ArrayList<>();
        }
        noNotifyGroup.addAll(Arrays.asList(groupId));
        
        EMChatManager.getInstance().getChatOptions()
                .setReceiveNotNoifyGroup(noNotifyGroup);
    }
    
    /**
     * 取消群组免打扰
     *
     * @param groupId
     *            支持多个
     */
    public void removeReceiveNotNotifyGroup(String... groupId) {
        List<String> noNotifyGroup = EMChatManager.getInstance().getChatOptions()
                .getReceiveNoNotifyGroup();
        if (noNotifyGroup == null) {
            noNotifyGroup = new ArrayList<>();
        }
        
        noNotifyGroup.removeAll(Arrays.asList(groupId));
        
        EMChatManager.getInstance().getChatOptions()
                .setReceiveNotNoifyGroup(noNotifyGroup);
    }
    
    /**
     * 指定群组是否为免打扰
     *
     * @param groupId
     *            群组 id
     */
    public boolean isGroupReceiveNotNotify(String groupId) {
        List<String> noNotifyGroup = EMChatManager.getInstance().getChatOptions()
                .getReceiveNoNotifyGroup();
        
        return noNotifyGroup != null && noNotifyGroup.contains(groupId);
    }
    
    /**
     * 获取指定群中指定人员的联系人信息
     *
     * @param groupId
     *            群组 id
     * @param userIds
     *            需要查询的 qqUserId
     */
    public void getGroupContactInfo(final String groupId, String... userIds) {
        // 当本地没有群组时，不再请求
        if (EMGroupManager.getInstance().getGroup(groupId) == null) {
            return;
        }
        
        ImProto.ChatGroupMemberInfoRequest builder = new ImProto.ChatGroupMemberInfoRequest();
        builder.chatGroupId = groupId;
        builder.qingqingUserIds = userIds;
        new ProtoReq(CommonUrl.CHAT_GROUP_MEMBER_URL.url()).setSendMsg(builder)
                .setRspListener(
                        new ProtoListener(ImProto.ChatGroupMemberInfoResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                ImProto.ChatGroupMemberInfoResponse response = (ImProto.ChatGroupMemberInfoResponse) result;
                                for (int i = 0; i < response.members.length; i++) {
                                    ChatManager.getInstance().getContactService()
                                            .saveGroupContactInfo(response.members[i],
                                                    groupId);
                                }
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
    
    public void getGroupListType() {
        getGroupListType(null);
    }
    
    /**
     * 拉取群组列表，并设置群组类型
     */
    public void getGroupListType(final GroupListTypeUpdateListener listener) {
        if (isSyncingGroupsListWithApi) {
            return;
        }
        
        isSyncingGroupsListWithApi = true;
        new ProtoReq(CommonUrl.CHAT_GROUP_LIST_URL.url()).setRspListener(
                new ProtoListener(ImProto.ChatGroupInfoForListResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        super.onDealResult(result);
                        ImProto.ChatGroupInfoForListResponse response = (ImProto.ChatGroupInfoForListResponse) result;
                        
                        for (int i = 0; i < response.groupInfos.length; i++) {
                            ImProto.ChatGroupInfoForList groupInfo = response.groupInfos[i];
                            setGroupType(groupInfo.chatGroupId,
                                    groupInfo.chatGroupTypeEnum);
                        }
                        if (listener != null) {
                            listener.onGroupListTypeUpdate();
                        }
                        isSyncingGroupsListWithApi = false;
                    }
                    
                    @Override
                    public boolean onDealError(int errorCode, Object result) {
                        isSyncingGroupsListWithApi = false;
                        return super.onDealError(errorCode, result);
                    }
                    
                    @Override
                    public void onError(HttpReq<byte[]> request, HttpError error) {
                        super.onError(request, error);
                        isSyncingGroupsListWithApi = false;
                    }
                }).reqSilent();
    }
    
    /**
     * 设置群组的类型，参见{@link com.qingqing.api.proto.v1.im.ImProto.ChatGroupType}
     *
     * @param groupId
     * @param type
     */
    public void setGroupType(String groupId, int type) {
        if (!isLoggedIn()) {
            return;
        }
        
        setConversationExtra(EMChatManager.getInstance().getConversation(groupId),
                KEY_GROUP_TYPE, String.valueOf(type));
    }
    
    /**
     * 获取群组的类型，参见{@link com.qingqing.api.proto.v1.im.ImProto.ChatGroupType}
     *
     * @param groupId
     */
    public int getGroupType(String groupId) {
        if (!isLoggedIn()) {
            return ImProto.ChatGroupType.unknown_chat_group_type;
        }
        
        int type = ImProto.ChatGroupType.unknown_chat_group_type;
        
        String groupType = getConversationExtra(
                EMChatManager.getInstance().getConversation(groupId), KEY_GROUP_TYPE);
        
        if (!TextUtils.isEmpty(groupType)) {
            try {
                type = Integer.parseInt(groupType);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        
        return type;
    }
    
    /**
     * 获取是否设置过groupChatType
     *
     * @param groupId
     *            groupId
     * @return 未设置返回false
     */
    public boolean hasSetGroupType(String groupId) {
        if (!isLoggedIn()) {
            return false;
        }
        
        String groupType = getConversationExtra(
                EMChatManager.getInstance().getConversation(groupId), KEY_GROUP_TYPE);
        return !TextUtils.isEmpty(groupType);
    }
    
    /**
     * 设置群组是否置顶
     *
     * @param groupId
     * @param stickTop
     */
    public void setGroupStickTop(String groupId, boolean stickTop) {
        if (!isLoggedIn()) {
            return;
        }
        
        setConversationExtra(EMChatManager.getInstance().getConversation(groupId),
                KEY_STICK_TOP, String.valueOf(stickTop ? 1 : 0));
    }
    
    /**
     * 获取群组是否置顶
     *
     * @param groupId
     */
    public boolean getGroupStickTop(String groupId) {
        if (!isLoggedIn()) {
            return false;
        }
        boolean isStickTop = false;
        String value = getConversationExtra(
                EMChatManager.getInstance().getConversation(groupId), KEY_STICK_TOP);
        
        if (!TextUtils.isEmpty(value)) {
            try {
                isStickTop = Integer.parseInt(value) == 1;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        
        return isStickTop;
    }
    
    /**
     * 获取群组是否有未读的群公告
     *
     * @param groupId
     */
    public boolean hasNewGroupAnnounce(String groupId) {
        if (!isLoggedIn()) {
            return false;
        }
        boolean hasGroupAnnounce = false;
        String value = getConversationExtra(
                EMChatManager.getInstance().getConversation(groupId),
                KEY_GROUP_HAS_NEW_ANNOUNCE);
        
        if (!TextUtils.isEmpty(value)) {
            try {
                hasGroupAnnounce = Integer.parseInt(value) == 1;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return hasGroupAnnounce;
    }
    
    /**
     * 设置群组公告是否已读
     *
     * @param groupId
     * @param hasRead
     */
    public void setGroupAnnounceRead(String groupId, boolean hasRead) {
        if (!isLoggedIn()) {
            return;
        }
        
        setConversationExtra(EMChatManager.getInstance().getConversation(groupId),
                KEY_GROUP_HAS_NEW_ANNOUNCE, hasRead ? "0" : "1");
    }
    
    /**
     * 获取群组的群公告
     *
     * @param groupId
     */
    public String getGroupAnnounce(String groupId) {
        if (!isLoggedIn()) {
            return null;
        }
        
        return getConversationExtra(EMChatManager.getInstance().getConversation(groupId),
                KEY_GROUP_ANNOUNCE);
    }
    
    /**
     * 设置群组的群公告，并设为未读（若群公告为空，则设为已读）
     *
     * @param groupId
     * @param groupAnnounce
     */
    public void setGroupAnnounce(String groupId, String groupAnnounce) {
        if (!isLoggedIn()) {
            return;
        }
        
        setConversationExtra(EMChatManager.getInstance().getConversation(groupId),
                KEY_GROUP_ANNOUNCE, groupAnnounce);
        setConversationExtra(EMChatManager.getInstance().getConversation(groupId),
                KEY_GROUP_HAS_NEW_ANNOUNCE, TextUtils.isEmpty(groupAnnounce) ? "0" : "1");
    }
    
    /**
     * 获取群组是否有新的指标排名
     *
     * @param groupId
     */
    public boolean hasNewGroupRank(String groupId) {
        if (!isLoggedIn()) {
            return false;
        }
        boolean hasGroupAnnounce = false;
        String value = getConversationExtra(
                EMChatManager.getInstance().getConversation(groupId),
                KEY_GROUP_HAS_NEW_RANK);
        
        if (!TextUtils.isEmpty(value)) {
            try {
                hasGroupAnnounce = Integer.parseInt(value) == 1;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return hasGroupAnnounce;
    }
    
    /**
     * 设置群组是否有新的指标排名
     *
     * @param groupId
     * @param hasGroupNewRank
     */
    public void setHasGroupNewRank(String groupId, boolean hasGroupNewRank) {
        if (!isLoggedIn()) {
            return;
        }
        
        setConversationExtra(EMChatManager.getInstance().getConversation(groupId),
                KEY_GROUP_HAS_NEW_RANK, hasGroupNewRank ? "1" : "0");
    }
    
    public String getGroupIdFromConversation(EMConversation conversation) {
        if (conversation != null) {
            return getConversationExtra(conversation, KEY_GROUP_ID);
        }
        return null;
    }
    
    public String getGroupNameFromConversation(EMConversation conversation) {
        if (conversation != null) {
            return getConversationExtra(conversation, KEY_GROUP_NAME);
        }
        return null;
    }
    
    private void setConversationExtra(EMConversation conversation, String key,
            String value) {
        if (conversation == null) {
            return;
        }
        JSONObject object = null;
        String extra = conversation.getExtField();
        try {
            if (!TextUtils.isEmpty(extra)) {
                object = new JSONObject(extra);
            }
            else {
                object = new JSONObject();
            }
        } catch (JSONException e) {
            object = new JSONObject();
        }
        
        try {
            object.put(key, value);
        } catch (Exception e) {
            Logger.w(TAG, "setConversationExtra: extra: " + extra, e);
        }
        
        // FIXME: 环信会话的 extField 有 bug ， 当会话内没有消息时，设置 extField 无效
        conversation.setExtField(object.toString());
    }
    
    @Nullable
    private String getConversationExtra(EMConversation conversation, String key) {
        if (conversation == null) {
            return null;
        }
        
        String value = null;
        String extra = conversation.getExtField();
        if (!TextUtils.isEmpty(extra)) {
            try {
                JSONObject object = new JSONObject(extra);
                Object valueObject = object.opt(key);
                value = String.valueOf(valueObject);
                value = object.optString(key, null);
            } catch (Exception e) {
                Logger.w(TAG, "getConversationExtra: extra: " + extra, e);
            }
        }
        
        return value;
    }
    
    /**
     * 同步操作，从服务器获取群组列表
     * 
     * 该方法会记录更新状态，可以通过isSyncingGroupsFromServer获取是否正在更新
     * 和isGroupsSyncedWithServer获取是否更新已经完成
     *
     * @throws EaseMobException
     */
    public synchronized void asyncFetchGroupsFromServer(final EMCallBack callback) {
        if (isSyncingGroupsWithServer) {
            return;
        }
        
        isSyncingGroupsWithServer = true;
        
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                try {
                    EMGroupManager.getInstance().getGroupsFromServer();
                    
                    // in case that logout already before server returns, we
                    // should return immediately
                    if (!isLoggedIn()) {
                        return;
                    }
                    
                    chatModel.setGroupsSynced(true);
                    
                    isGroupsSyncedWithServer = true;
                    isSyncingGroupsWithServer = false;
                    
                    // 通知listener同步群组完毕
                    notifyGroupSyncListeners(true);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (EaseMobException ex) {
                    chatModel.setGroupsSynced(false);
                    isGroupsSyncedWithServer = false;
                    isSyncingGroupsWithServer = false;
                    notifyGroupSyncListeners(false);
                    if (callback != null) {
                        callback.onError(ex.getErrorCode(), ex.toString());
                    }
                }
                e.onComplete();
                
            }
        }).subscribeOn(Schedulers.computation()).subscribe();
    }
    
    public synchronized void asyncFetchGroupFromServer(final String groupId,
            final EMCallBack callback) {
        if (isSyncingGroupsWithServer) {
            return;
        }
        
        isSyncingGroupsWithServer = true;
        
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                try {
                    EMGroup group = EMGroupManager.getInstance()
                            .getGroupFromServer(groupId);
                    if (group != null) {
                        EMGroupManager.getInstance().createOrUpdateLocalGroup(group);
                        if (!hasSetGroupType(groupId)) {
                            getGroupListType();
                        }
                    }
                    
                    // in case that logout already before server returns, we
                    // should return immediately
                    if (!isLoggedIn()) {
                        return;
                    }
                    
                    chatModel.setGroupsSynced(true);
                    
                    isGroupsSyncedWithServer = true;
                    isSyncingGroupsWithServer = false;
                    
                    // 通知listener同步群组完毕
                    notifyGroupSyncListeners(true);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (EaseMobException ex) {
                    chatModel.setGroupsSynced(false);
                    isGroupsSyncedWithServer = false;
                    isSyncingGroupsWithServer = false;
                    notifyGroupSyncListeners(false);
                    if (callback != null) {
                        callback.onError(ex.getErrorCode(), ex.toString());
                    }
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.computation()).subscribe();
    }
    
    public void notifyGroupSyncListeners(final boolean success) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DataSyncListener listener : syncGroupsListeners) {
                        listener.onSyncComplete(success);
                    }
                }
            });
        }
    }
    
    public void notifyBlackListSyncListener(final boolean success) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DataSyncListener listener : syncBlackListListeners) {
                        listener.onSyncComplete(success);
                    }
                }
            });
        }
    }
    
    public boolean isSyncingGroupsWithServer() {
        return isSyncingGroupsWithServer;
    }
    
    public boolean isGroupsSyncedWithServer() {
        return isGroupsSyncedWithServer;
    }
    
    public synchronized void notifyForRecevingEvents() {
        if (alreadyNotified) {
            return;
        }
        
        // 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
        EMChat.getInstance().setAppInited();
        alreadyNotified = true;
    }
    
    synchronized void reset() {
        isSyncingGroupsWithServer = false;
        
        chatModel.setGroupsSynced(false);
        
        isGroupsSyncedWithServer = false;
        
        alreadyNotified = false;
        isGroupAndContactListenerRegistered = false;
        
        chatModel.reset();
    }
    
    public void login(boolean forced, final ChatCallback callback) {
        Logger.o(TAG, "login forced = " + forced);
        addLoginChatCallback(callback);
        ChatSettingsManager.init(getCurrentUserName());
        if (!isQQLoggedIn()) {
            Logger.o(TAG, "必须先登陆轻轻账号，才能登陆聊天账号");
            notifyLoginChatCallbackError(0, "必须先登陆轻轻账号，才能登陆聊天账号");
            deleteLoginChatCallback(callback);
            return;
        }
        else {
            if (chatModel != null && !chatModel.getContactModel().isSynced()) {
                chatModel.getContactModel().asyncFetchData();
            }
        }
        if (isLoggedIn() && !forced) {
            // wait等待自动登陆,当disconnect（其他设备登陆或者被移除）时，再强制登陆
            Logger.o(TAG, "等待自动登陆");
            notifyLoginChatCallbackSuccess();
            deleteLoginChatCallback(callback);
        }
        else {
            new ProtoReq(CommonUrl.CHAT_LOGIN_URL.url()).setRspListener(
                    new ProtoListener(ImProto.ChatLoginInfoResponse.class) {
                        @Override
                        public void onDealResult(Object result) {
                            ImProto.ChatLoginInfoResponse response = (ImProto.ChatLoginInfoResponse) result;
                            
                            Logger.o(TAG, "onResponse 返回密码");
                            String curUserName = getCurrentUserName();
                            String password = response.loginToken;
                            if (!TextUtils.isEmpty(curUserName)
                                    && !TextUtils.isEmpty(curUserName)) {
                                login(curUserName, password, callback);
                            }
                            else {
                                Logger.o(TAG, "onResponse 返回密码结果错误");
                                notifyLoginChatCallbackError(0, "返回密码结果错误");
                                deleteLoginChatCallback(callback);
                            }
                        }
                        
                        @Override
                        public void onDealError(HttpError error, boolean isParseOK,
                                int errorCode, Object result) {
                            Logger.e(TAG, "返回密码结果错误");
                            notifyLoginChatCallbackError(0, "返回密码结果错误");
                            deleteLoginChatCallback(callback);
                        }
                    }).req();
        }
    }
    
    public void login(final String userName, final String password,
            final ChatCallback callback) {
        endCall();
        EMChatManager.getInstance().login(userName, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                // 注册群组和联系人监听
                registerGroupAndContactListener();
                // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                // ** manually load all local groups and
                EMGroupManager.getInstance().loadAllGroups();
                EMChatManager.getInstance().loadAllConversations();
                checkOfflineMessages();
                Logger.o(TAG, "Login Success");
                
                notifyLoginChatCallbackSuccess();
                deleteLoginChatCallback(callback);
            }
            
            @Override
            public void onError(int i, String s) {
                Logger.e(TAG, "Login Error : code = " + i + "  error = " + s);
                // 当用户名和密码错误是，重新强制请求登陆
                if (i == -1005) {
                    // login(true, null);
                }
                ToastWrapper.show(R.string.chat_login_failed_text);
                notifyLoginChatCallbackError(i, s);
                deleteLoginChatCallback(callback);
            }
            
            @Override
            public void onProgress(int i, String s) {
                notifyLoginChatCallbackProgress(i, s);
                deleteLoginChatCallback(callback);
            }
        });
    }
    
    public void logout(final ChatCallback callback) {
        endCall();
        ChatSettingsManager.init(getCurrentUserName());
        EMChatManager.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                reset();
                Logger.o(TAG, "Login Error");
                if (callback != null) {
                    callback.onSuccess();
                }
            }
            
            @Override
            public void onError(int i, String s) {
                Logger.e(TAG, "Logout Error : code = " + i + "  error = " + s);
                if (callback != null) {
                    callback.onError(i, s);
                }
            }
            
            @Override
            public void onProgress(int i, String s) {
                if (callback != null) {
                    callback.onProgress(i, s);
                }
            }
        });
        
    }
    
    public void endCall() {
        try {
            EMChatManager.getInstance().endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 如果外部，手动调用了 该方法，则在响应完成后， 需要手动调用
     * {@link ChatManager#deleteLoginChatCallback(ChatCallback)}
     */
    public void addLoginChatCallback(ChatCallback callback) {
        synchronized (this) {
            if (callback != null) {
                mChatCallbackList.add(callback);
            }
        }
    }
    
    public void deleteLoginChatCallback(ChatCallback callback) {
        synchronized (this) {
            if (callback != null) {
                int idx = mChatCallbackList.indexOf(callback);
                if (idx >= 0) {
                    mChatCallbackList.remove(idx);// TODO:for 循环remove item 错误
                }
            }
        }
    }
    
    private void notifyLoginChatCallbackSuccess() {
        for (ChatCallback cb : mChatCallbackList) {
            cb.onSuccess();
        }
    }
    
    private void notifyLoginChatCallbackProgress(int progress, String status) {
        for (ChatCallback cb : mChatCallbackList) {
            cb.onProgress(progress, status);
        }
    }
    
    private void notifyLoginChatCallbackError(int code, String error) {
        for (ChatCallback cb : mChatCallbackList) {
            cb.onError(code, error);
        }
    }
    
    public ContactService getContactService() {
        return contactService;
    }
    
    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }
    
    /**
     * 增加收到群公告的监听
     *
     * @param groupId
     * @param listener
     */
    public void addGroupAnnouncementUpdateListener(String groupId,
            GroupAnnouncementUpdateListener listener) {
        mGroupAnnouncementUpdateListener.put(groupId, listener);
    }
    
    /**
     * 移除收到群公告的监听
     *
     * @param groupId
     */
    public void removeGroupAnnouncementUpdateListener(String groupId) {
        mGroupAnnouncementUpdateListener.remove(groupId);
    }
    
    public interface GetPhoneNumberCallback {
        public void onSuccess(String phoneNumber);
    }
    
    public interface ChatCallback {
        public void onSuccess();
        
        public void onProgress(int progress, String status);
        
        public void onError(int code, String error);
    }
    
    public interface GroupAnnouncementUpdateListener {
        void onGroupAnnouncementUpdate();
    }
    
    public interface GroupListTypeUpdateListener {
        void onGroupListTypeUpdate();
    }
    
    /**
     * 群组变动监听
     */
    class MyGroupChangeListener implements EMGroupChangeListener {
        
        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter,
                String reason) {
            
            Logger.i(TAG, "onInvitationReceived groupId + " + groupId + "  groupName = "
                    + groupName);
            asyncFetchGroupFromServer(groupId, null);
            boolean hasGroup = false;
            for (EMGroup group : EMGroupManager.getInstance().getAllGroups()) {
                if (group.getGroupId().equals(groupId)) {
                    hasGroup = true;
                    break;
                }
            }
            if (!hasGroup)
                return;
            Logger.i(TAG, "add group : groupId " + groupId);
        }
        
        @Override
        public void onInvitationAccpted(String groupId, String inviter, String reason) {
            Logger.i(TAG, "onInvitationAccpted : groupId " + groupId);
        }
        
        @Override
        public void onInvitationDeclined(String groupId, String invitee, String reason) {
            Logger.i(TAG, "onInvitationDeclined : groupId " + groupId);
        }
        
        @Override
        public void onUserRemoved(String groupId, String groupName) {
            // TODO 提示用户被T了，demo省略此步骤
            Logger.i(TAG,
                    "onUserRemoved groupId = " + groupId + "  groupName = " + groupName);
        }
        
        @Override
        public void onGroupDestroy(String groupId, String groupName) {
            // 群被解散
            // TODO 提示用户群被解散,demo省略
            Logger.i(TAG,
                    "onGroupDestroy groupId = " + groupId + "  groupName = " + groupName);
        }
        
        @Override
        public void onApplicationReceived(String groupId, String groupName,
                String applyer, String reason) {
            // 用户申请加入群聊
            Logger.i(TAG, "onApplicationReceived : groupId " + groupId);
        }
        
        @Override
        public void onApplicationAccept(String groupId, String groupName,
                String accepter) {
            Logger.i(TAG, "onApplicationAccept : groupId " + groupId);
        }
        
        @Override
        public void onApplicationDeclined(String groupId, String groupName,
                String decliner, String reason) {
            // 加群申请被拒绝，demo未实现
            Logger.i(TAG, "onApplicationDeclined : groupId " + groupId);
        }
    }
    
    /***
     * 好友变化listener
     */
    public class MyContactListener implements EMContactListener {
        
        @Override
        public void onContactAdded(List<String> usernameList) {
            // 保存增加的联系人
            Map<String, ContactInfo> localUsers = getContactList();
            Map<String, ContactInfo> toAddUsers = new HashMap<String, ContactInfo>();
            for (String username : usernameList) {
                ContactInfo user = new ContactInfo(username, 1, 2,
                        ContactInfo.Type.Unknown);
                // 添加好友时可能会回调added方法两次
                if (!localUsers.containsKey(username)) {
                    chatModel.getContactModel().saveContact(user);
                }
                toAddUsers.put(username, user);
            }
            localUsers.putAll(toAddUsers);
            // 发送好友变动广播
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
        }
        
        @Override
        public void onContactDeleted(final List<String> usernameList) {
            // 被删除
            Map<String, ContactInfo> localUsers = getContactList();
            for (String username : usernameList) {
                localUsers.remove(username);
                chatModel.getContactModel().deleteContact(username);
            }
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
        }
        
        @Override
        public void onContactInvited(String username, String reason) {
            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
        }
        
        @Override
        public void onContactAgreed(String username) {
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
        }
        
        @Override
        public void onContactRefused(String username) {
            // 参考同意，被邀请实现此功能,demo未实现
            Log.d(username, username + "拒绝了你的好友请求");
        }
        
    }
}
