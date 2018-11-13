package com.qingqing.base.nim.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.easeui.widget.EaseVoiceRecorderView;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.SelectPictureManager;
import com.qingqing.base.activity.ImageShowActivity;
import com.qingqing.base.bean.ImageGroup;
import com.qingqing.base.bean.MultiMediaFactory;
import com.qingqing.base.bean.MultiMediaItem;
import com.qingqing.base.bean.UserBehaviorLogExtraData;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.im.ui.MapActivity;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.AudioConsecutivePlayer;
import com.qingqing.base.nim.ChatCoordinationListener;
import com.qingqing.base.nim.ChatCoordinator;
import com.qingqing.base.nim.MessageOperator;
import com.qingqing.base.nim.domain.AudioMessageBody;
import com.qingqing.base.nim.domain.ChatType;
import com.qingqing.base.nim.domain.ImageMessageBody;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.domain.MessageFactory;
import com.qingqing.base.nim.domain.TextMessageBody;
import com.qingqing.base.nim.domain.services.MessageService;
import com.qingqing.base.nim.utils.Constants;
import com.qingqing.base.nim.utils.MessageUtils;
import com.qingqing.base.nim.view.ChatExtendMenu;
import com.qingqing.base.nim.view.ChatExtendMenuItem;
import com.qingqing.base.nim.view.ChatInputMenu;
import com.qingqing.base.nim.view.ChatMenuContainer;
import com.qingqing.base.nim.view.ChatMenuListener;
import com.qingqing.base.nim.view.ChatRowClickListener;
import com.qingqing.base.nim.view.ChatRowDefaultProvider;
import com.qingqing.base.nim.view.ChatRowProvider;
import com.qingqing.base.nim.view.ExtendedChatRowProvider;
import com.qingqing.base.nim.view.MessageListView;
import com.qingqing.base.utils.ClipboardUtil;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.qingqingbase.ui.BaseFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/8/17.
 *
 * Initialize steps：
 *
 * 1：初始化参数onInitializeArguments
 *
 * 2：初始化View
 *
 * 3：初始化ConversationCoordinator并调用join方法
 *
 * 4：初始化MessageOperator
 *
 * 退出页面时：调用ConversationCoordinator的leave方法
 *
 */
public abstract class BaseChatFragment extends BaseFragment
        implements ChatRowClickListener , ChatCoordinationListener , ChatMenuListener ,
        MessageService.Listener {
    
    private static final String TAG = "NewBaseChatFragment";
    
    protected static final int REQUEST_CODE_MAP = 1;
    private static final int KEY_USE_CAMERA = 3345;
    private static final int KEY_USE_GALLERY = 3346;
    
    // -------Views------
    protected MessageListView mListView;
    private MessageAdapter mAdapter;
    
    private ChatExtendMenu mExtendMenu;
    private ChatInputMenu mInputMenu;
    private ChatMenuContainer mMenuContainer;
    
    private TextView mSpeakForbiddenTv;
    
    protected EaseVoiceRecorderView mAudioRecorderView;
    
    private AudioConsecutivePlayer mConsecutivePlayer;
    
    // -------参数------
    private String mConversationId;
    private ChatType mChatType;
    
    protected String mPageId;
    
    // -------ChatCoordinator------
    private ChatCoordinator mCoordinator;
    
    // -------MessageOperator------
    private MessageOperator mMessageOperator;
    
    // -------图片选择------
    private SelectPictureManager mSelPicManager;
    private static boolean sCouldDealPic = false;
    private int currentStartIndex = -1;
    
    protected String getConversationId() {
        return mConversationId;
    }
    
    protected void setConversationId(String conversationId) {
        this.mConversationId = conversationId;
    }
    
    protected ChatType getChatType() {
        return mChatType;
    }
    
    private void setChatType(ChatType mChatType) {
        this.mChatType = mChatType;
    }
    
    protected abstract ChatCoordinator createCoordinator();
    
    protected abstract ChatCoordinationListener createChatCoordinationListener();
    
    private void setCoordinator(ChatCoordinator coordinator) {
        this.mCoordinator = coordinator;
    }
    
    protected ChatCoordinator getCoordinator() {
        return mCoordinator;
    }
    
    private void setMessageOperator(MessageOperator messageOperator) {
        this.mMessageOperator = messageOperator;
    }
    
    protected MessageOperator getMessageOperator() {
        return mMessageOperator;
    }
    
    protected MessageAdapter getAdapter() {
        return mAdapter;
    }
    
    protected MessageListView getListView() {
        return mListView;
    }
    
    protected EaseVoiceRecorderView getAudioRecorderView() {
        return mAudioRecorderView;
    }
    
    private SelectPictureManager getSelPicManager() {
        return mSelPicManager;
    }
    
    protected AudioConsecutivePlayer getConsecutivePlayer() {
        return mConsecutivePlayer;
    }
    
    protected ChatExtendMenu getExtendMenu() {
        return mExtendMenu;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 显示Menu
        setHasOptionsMenu(true);
        setTitle(getTitle());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_chat, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mSelPicManager = new SelectPictureManager(this);
        mSelPicManager.setSelectPicListener(mSelPicListener);
        getActivity().registerReceiver(mHomeKeyEventReceiver,
                new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        
        onInitializeArguments(getArguments());
        
        onInitializeViews(view);
        
        onInitializeCoordinator();
        
        onInitializeMessageOperator();
    }
    
    @CallSuper
    protected void onInitializeArguments(Bundle bundle) {
        setConversationId(bundle.getString(Constants.EXTRA_CONVERSATION_ID));
        setChatType(
                ChatType.mapStringToValue(bundle.getString(Constants.EXTRA_CHAT_TYPE)));
    }
    
    @CallSuper
    protected void onInitializeViews(View view) {
        mListView = (MessageListView) view.findViewById(R.id.list_chat);
        mAdapter = new MessageAdapter(getActivity(), getChatRowProvider(),
                getExtendedChatRowProvider(), this);
        mListView.setAdapter(mAdapter);
        
        mConsecutivePlayer = new AudioConsecutivePlayer(getActivity());
        mConsecutivePlayer.setRepository(mAdapter);
        
        mAdapter.setPlayStatusObservable(mConsecutivePlayer);
        
        mMenuContainer = (ChatMenuContainer) view.findViewById(R.id.container_chat_menu);
        mInputMenu = (ChatInputMenu) view.findViewById(R.id.chat_input_menu);
        mExtendMenu = (ChatExtendMenu) view.findViewById(R.id.chat_extend_menu);
        
        mMenuContainer.setMenuListener(this);
        mMenuContainer.setExtendMenuItems(getExtendMenuTitleResIds(),
                getExtendMenuIconResIds(), getExtendMenuItemIds());
        
        mAudioRecorderView = (EaseVoiceRecorderView) view
                .findViewById(R.id.audio_recorder);
        mAudioRecorderView.setChatType(EaseConstant.CHATTYPE_CHATROOM);
        
        mSpeakForbiddenTv = (TextView) view.findViewById(R.id.tv_speak_forbidden);
        
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                UIUtil.hideSoftInput(getActivity());
                mMenuContainer.hideExtendMenu();
                return false;
            }
        });
        
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }
    
    protected void clearOutInputText() {
        if (mInputMenu != null) {
            mInputMenu.clearOutInputText();
        }
    }
    
    private TextView getSpeakForbiddenTv() {
        return mSpeakForbiddenTv;
    }
    
    protected void setSpeakForbidden(boolean speakForbidden) {
        getSpeakForbiddenTv().setVisibility(speakForbidden ? View.VISIBLE : View.GONE);
        getSpeakForbiddenTv().setText(getSpeakForbiddenText());
        if (speakForbidden) {
            clearOutInputText();
            UIUtil.hideSoftInput(getContext());
        }
    }
    
    protected CharSequence getSpeakForbiddenText() {
        return getResources().getText(R.string.text_speak_forbidden_default);
    }
    
    protected CharSequence getSpeakForbiddenTips() {
        return getResources().getText(R.string.tips_speak_forbidden_default);
    }
    
    @CallSuper
    protected void onInitializeCoordinator() {
        setCoordinator(createCoordinator());
        getCoordinator().setCoordinationListener(createChatCoordinationListener());
        getCoordinator().initialize();
    }
    
    @CallSuper
    protected void onInitializeMessageOperator() {
        setMessageOperator(new MessageOperator(getCoordinator()));
        getMessageOperator().setMessageServiceListener(this);
        getMessageOperator().initialize();
    }
    
    @Override
    public void onInitialized() {
        getAdapter().setMessages(getCoordinator().getConversationMessages());
        setTitle(getTitle());
        forceSelectListLast();
    }
    
    @Override
    public void onInitializeFailed() {}
    
    protected boolean isInitialized() {
        return getCoordinator() != null && getCoordinator().isInitialized();
    }
    
    protected String getTitle() {
        String title;
        if (getChatType() == ChatType.Chat) {
            title = getString(R.string.chat_title);
        }
        else {
            title = getString(R.string.title_chat_room_default);
        }
        return title;
    }
    
    protected int[] getExtendMenuItemIds() {
        return new int[] { Constants.EXTEND_ITEM_CAMERA, Constants.EXTEND_ITEM_PICTURE };
    }
    
    protected int[] getExtendMenuTitleResIds() {
        return new int[] { R.string.attach_picture, R.string.attach_take_pic };
    }
    
    protected int[] getExtendMenuIconResIds() {
        return new int[] { R.drawable.chat_photo, R.drawable.chat_camera };
    }
    
    @NonNull
    protected ChatRowProvider getChatRowProvider() {
        return new ChatRowDefaultProvider();
    }
    
    protected ExtendedChatRowProvider getExtendedChatRowProvider() {
        return null;
    }
    
    @Override
    public void onBubbleClick(Message message) {
        switch (message.getMsgType()) {
            case IMAGE:
                clickImageBubble(message);
                break;
            case AUDIO:
                clickAudioBubble(message);
                break;
            default:
                break;
        }
    }
    
    private void clickImageBubble(Message message) {
        ImageMessageBody body = (ImageMessageBody) message.getBody();
        String localUrl = body.getLocalUrl();
        String remoteUrl = body.getRemoteUrl();
        String url = !TextUtils.isEmpty(localUrl) ? localUrl
                : (!TextUtils.isEmpty(remoteUrl) ? ImageUrlUtil.getOriginImg(remoteUrl)
                        : null);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        
        ArrayList<MultiMediaItem> list = new ArrayList<>();
        list.add(MultiMediaFactory.createImage(url));
        Intent intent = new Intent(getActivity(), ImageShowActivity.class);
        intent.putExtra(ImageShowActivity.KEY_IMG_GROUP, new ImageGroup(list));
        intent.putExtra(ImageShowActivity.KEY_IDX_IN_GROUP, 0);
        intent.putExtra(ImageShowActivity.PARAM_BOOLEAN_SUPPORT_SHOW_PAGE_INDEX, false);
        startActivity(intent);
    }
    
    private void clickAudioBubble(Message message) {
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta
                && !TextUtils.isEmpty(mPageId)) {
            try {
                AudioMessageBody body = (AudioMessageBody) message.getBody();
                // 点击播放音频埋点
                UserBehaviorLogManager.INSTANCE().saveClickLog(mPageId,
                        StatisticalDataConstants.CLICK_LECTURE_PLAY_AUDIO,
                        new UserBehaviorLogExtraData.Builder()
                                .addExtraData(
                                        StatisticalDataConstants.LOG_EXTRA_AUDIO_LENGTH,
                                        body.getLength())
                                .addExtraData(StatisticalDataConstants.LOG_EXTRA_AUDIO_ID,
                                        message.getId())
                                .build());
            } catch (Exception e) {
                Logger.i("audio buried point Exception");
            }
        }
        getMessageOperator().markMsgAsListened(message);
        if (MessageUtils.isBodyLoading(message)) {
            return;
        }
        else if (MessageUtils.isBodyNeededToLoad(message)) {
            getMessageOperator().loadMessageBody(message);
        }
        else if (MessageUtils.isBodyLoaded(message)) {
            getConsecutivePlayer().toggleBy(message);
        }
    }
    
    @Override
    public void onBubbleLongClick(final Message message) {
        // 消息框长按
        final Activity activity = getActivity();
        CharSequence[] items;
        final boolean isChatRoom = message.getChatType() == ChatType.ChatRoom;
        final boolean isTextMessage = message.getMsgType() == Message.Type.TEXT;
        if (isChatRoom && isTextMessage) {
            items = new String[] { activity.getString(R.string.text_dlg_list_item_copy) };
        }
        else if (!isChatRoom) {
            if (!isTextMessage) {
                items = new String[] {
                        activity.getString(R.string.text_dlg_list_item_delete) };
            }
            else {
                items = new String[] {
                        activity.getString(R.string.text_dlg_list_item_copy),
                        activity.getString(R.string.text_dlg_list_item_delete) };
            }
        }
        else {
            return;
        }
        new CompatDialog.Builder(activity, R.style.Theme_Dialog_Compat_Only_List)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.i(TAG, "onMessageBubbleLongClick : type = "
                                + message.getMsgType().name() + ", which = " + which);
                        if (isChatRoom) {
                            ClipboardUtil.copyToClipBoard(
                                    ((TextMessageBody) message.getBody()).getText());
                            ToastWrapper.show(R.string.tips_message_text_copy);
                        }
                        else {
                            if (isTextMessage) {
                                if (which == 1) {
                                    deleteMessage(message);
                                }
                                else {
                                    ClipboardUtil.copyToClipBoard(
                                            ((TextMessageBody) message.getBody())
                                                    .getText());
                                    ToastWrapper.show(R.string.tips_message_text_copy);
                                }
                            }
                            else {
                                deleteMessage(message);
                            }
                        }
                        dialog.dismiss();
                    }
                }).show();
    }
    
    @Override
    public void onAvatarClick(Message message) {}
    
    @Override
    public void onResendClick(Message message) {
        getMessageOperator().resend(message);
    }
    
    @Override
    public void onExtendMenuItemClick(ChatExtendMenuItem item, int itemId, int position) {
        if (!isInitialized()) {
            Logger.e(TAG, "isInitialized failed , can not click MenuItem to send");
            return;
        }
        switch (itemId) {
            case Constants.EXTEND_ITEM_CAMERA: // 拍照
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
            case Constants.EXTEND_ITEM_PICTURE:
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
            case Constants.EXTEND_ITEM_LOCATION: // 位置
                try {
                    startActivityForResult(new Intent(getActivity(), MapActivity.class),
                            REQUEST_CODE_MAP);
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
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
    
    @Override
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
        return getAudioRecorderView().onPressToSpeakBtnTouch(v, event,
                new EaseVoiceRecorderView.EaseVoiceRecorderCallback() {
                    @Override
                    public void onVoiceRecordComplete(String voiceFilePath,
                            int voiceTimeLength) {
                        // 发送语音消息
                        sendAudioMessage(voiceFilePath, voiceTimeLength);
                        try {
                            File file = new File(voiceFilePath);
                            Logger.o("sendVoiceMessage: send: fileName:" + file.getName()
                                    + ", length:" + file.length());
                        } catch (Exception ignored) {}
                    }
                });
    }
    
    @Override
    public void onSendTextMessage(String content) {
        sendTextMessage(content);
    }
    
    @Override
    public void onMessageAdded(Message message) {
        boolean isLoadingHistory = isLoadingHistoryMessage(message);
        getAdapter().addMessage(message);
        if (isLoadingHistory) {
            selectHistoryLast();
        }
        else {
            selectListLast();
        }
        getConsecutivePlayer().playIfNeeded();
    }
    
    @Override
    public void onMessagesAdded(List<Message> messages) {
        boolean isLoadingHistory = false;
        if (messages != null && messages.size() > 0) {
            isLoadingHistory = isLoadingHistoryMessage(messages.get(0));
        }
        getAdapter().addMessages(messages);
        if (isLoadingHistory) {
            selectHistoryLast();
        }
        else {
            selectListLast();
        }
        getConsecutivePlayer().playIfNeeded();
    }
    
    private boolean isLoadingHistoryMessage(Message message) {
        if (message == null) {
            return false;
        }
        
        if (getAdapter().getCount() > 0 && getAdapter().getItem(0) instanceof Message) {
            currentStartIndex = ((Message) getAdapter().getItem(0)).getIndex();
        }
        
        return currentStartIndex >= message.getIndex();
    }
    
    @Override
    public void onMessageRemoved(Message message) {
        getAdapter().removeMessage(message);
    }
    
    @Override
    public void onMessageStatusChanged(Message message) {
        getAdapter().notifyDataSetChanged();
    }
    
    protected void sendTextMessage(String content) {
        sendMessage(MessageFactory.createTextMessage(getConversationId(), getChatType(),
                content));
    }
    
    protected void deleteMessage(Message message) {
        getMessageOperator().delete(message);
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
        Message message = MessageFactory.createImageMessage(getConversationId(),
                getChatType(), imagePath);
        sendMessage(message);
    }
    
    protected void sendAudioMessage(String audioPath, int length) {
        Message message = MessageFactory.createAudioMessage(getConversationId(),
                getChatType(), audioPath, length);
        sendMessage(message);
    }
    
    private void sendMessage(Message message) {
        if (!onInterceptSendMessage(message)) {
            onSendMessage(message);
            getMessageOperator().send(message);
            forceSelectListLast();
        }
    }
    
    protected void onSendMessage(Message message) {}
    
    protected boolean onInterceptSendMessage(Message message) {
        return false;
    }
    
    private void selectHistoryLast() {
        if (getListView() != null) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                if (mAdapter.getItem(i) instanceof Message) {
                    if (((Message) mAdapter.getItem(i)).getIndex() == currentStartIndex
                            && i > 0) {
                        if (couldOperateUI() && getListView() != null) {
                            getListView().setSelection(i - 1);
                        }
                        return;
                    }
                }
            }
            if (couldOperateUI() && getListView() != null) {
                getListView().setSelection(0);
            }
        }
    }
    
    private void selectListLast() {
        if (getListView() != null) {
            getListView().selectListLast();
        }
    }
    
    private void forceSelectListLast() {
        if (getListView() != null) {
            getListView().forceSelectListLast();
        }
    }
    
    /**
     * 照相获取图片
     */
    protected void selectPicFromCamera() {
        if (!EaseCommonUtils.isExitsSdcard()) {
            ToastWrapper.show(R.string.sd_card_does_not_exist);
            return;
        }
        sCouldDealPic = true;
        mSelPicManager.key(KEY_USE_CAMERA).openCamera();
    }
    
    /**
     * 从图库获取图片
     */
    protected void selectPicFromLocal() {
        if (!EaseCommonUtils.isExitsSdcard()) {
            ToastWrapper.show(R.string.sd_card_does_not_exist);
            return;
        }
        sCouldDealPic = true;
        mSelPicManager.key(KEY_USE_GALLERY).openGallery();
    }
    
    private SelectPictureManager.SelectPicListener mSelPicListener = new SelectPictureManager.SelectPicListener() {
        @Override
        public void onPicSelected(int key, File outputFile) {
            Logger.o(TAG, "picture size is " + outputFile.length() + "----"
                    + outputFile.getAbsolutePath());
            if (outputFile.exists())
                sendImageMessage(outputFile.getAbsolutePath());
        }
    };
    
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
                    getConsecutivePlayer().stopPlay();
                }
            }
        }
    };
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSelPicManager != null) {
            mSelPicManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    @Override
    public boolean onBackPressed() {
        if (getExtendMenu().onBackPressed()) {
            return true;
        }
        if (getActivity() != null) {
            getActivity().finish();
        }
        return super.onBackPressed();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getConsecutivePlayer().destroy();
        getCoordinator().destroy();
        getMessageOperator().destroy();
        getAdapter().destroy();
        getActivity().unregisterReceiver(mHomeKeyEventReceiver);
    }
}
