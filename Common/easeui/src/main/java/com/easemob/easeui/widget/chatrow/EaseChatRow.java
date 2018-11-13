package com.easemob.easeui.widget.chatrow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseMessageAdapter;
import com.easemob.easeui.widget.EaseChatMessageList;
import com.easemob.easeui.widget.EaseChatMessageList.MessageListItemClickListener;
import com.easemob.util.DateUtils;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.ThemeConstant;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.Constant;
import com.qingqing.base.im.ExtFieldParser;
import com.qingqing.base.im.domain.ChatMessage;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.domain.ExtField;
import com.qingqing.base.im.domain.GroupRole;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.teacherindex.TeacherIndex;
import com.qingqing.base.teacherindex.TeacherIndexManager;
import com.qingqing.base.utils.TeachingRoleUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.ToastWrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EaseChatRow extends LinearLayout {
    protected static final String TAG = EaseChatRow.class.getSimpleName();
    
    protected LayoutInflater inflater;
    protected Context context;
    protected BaseAdapter adapter;
    protected EMMessage message;
    protected ExtField extField;
    protected ChatMessage chatMessage;
    protected int position;
    
    protected TextView timeStampView;
    protected AsyncImageViewV2 userAvatarView;
    protected View bubbleLayout;
    protected TextView usernickView;
    
    protected TextView percentageView;
    protected ProgressBar progressBar;
    protected ImageView statusView;
    protected Activity activity;
    
    protected TextView ackedView;
    protected TextView deliveredView;
    protected TextView userRoleView;
    protected ImageView teachingRoleView;
    protected ImageView groupRoleView;
    protected TextView userIndexView;
    
    protected EMCallBack messageSendCallback;
    protected EMCallBack messageReceiveCallback;
    
    protected MessageListItemClickListener itemClickListener;
    
    public EaseChatRow(Context context, EMMessage message, int position,
            BaseAdapter adapter) {
        super(context);
        this.context = context;
        this.activity = (Activity) context;
        this.message = message;
        this.extField = ExtFieldParser.getExt(message);
        this.position = position;
        this.adapter = adapter;
        inflater = LayoutInflater.from(context);
        
        chatMessage = new ChatMessage(message);
        
        initView();
    }
    
    private void initView() {
        onInflatView();
        timeStampView = (TextView) findViewById(R.id.timestamp);
        userAvatarView = (AsyncImageViewV2) findViewById(R.id.iv_userhead);
        bubbleLayout = findViewById(R.id.bubble);
        usernickView = (TextView) findViewById(R.id.tv_userid);
        
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        statusView = (ImageView) findViewById(R.id.msg_status);
        ackedView = (TextView) findViewById(R.id.tv_ack);
        deliveredView = (TextView) findViewById(R.id.tv_delivered);
        
        userRoleView = (TextView) findViewById(R.id.tv_user_role);
        teachingRoleView = (ImageView) findViewById(R.id.iv_teaching_role);
        groupRoleView = (ImageView) findViewById(R.id.iv_group_role);
        userIndexView = (TextView) findViewById(R.id.tv_user_index);
        
        onFindViewById();
    }
    
    /**
     * 根据当前message和position设置控件属性等
     *
     * @param message
     * @param position
     */
    public void setUpView(EMMessage message, int position,
            EaseChatMessageList.MessageListItemClickListener itemClickListener) {
        this.message = message;
        this.extField = ExtFieldParser.getExt(message);
        this.position = position;
        this.itemClickListener = itemClickListener;
        chatMessage = new ChatMessage(message);
        
        setUpBaseView();
        onSetUpView();
        setClickListener();
    }
    
    private void setUpBaseView() {
        // 设置用户昵称头像，bubble背景等
        TextView timestamp = (TextView) findViewById(R.id.timestamp);
        if (timestamp != null) {
            if (position == 0) {
                timestamp.setText(
                        DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            }
            else {
                // 两条消息时间离得如果稍长，显示时间
                EMMessage prevMessage = (EMMessage) adapter.getItem(position - 1);
                if (prevMessage != null && DateUtils.isCloseEnough(message.getMsgTime(),
                        prevMessage.getMsgTime())) {
                    timestamp.setVisibility(View.GONE);
                }
                else {
                    timestamp.setText(
                            DateUtils.getTimestampString(new Date(message.getMsgTime())));
                    timestamp.setVisibility(View.VISIBLE);
                }
            }
        }
        // 设置头像和nick
        String showName = null;
        if (chatMessage.getChatRoomScene() == Constant.CHATTYPE_GROUP) {
            ContactInfo contactInfo = ChatManager.getInstance().getContactService()
                    .getContactInfo(chatMessage.getCurUserName());
            
            if (contactInfo != null) {
                GroupRole groupRole = contactInfo.getGroupRole(message.getTo());
                if (groupRole != null) {
                    showName = groupRole.getNick();
                }
            }
        }
        if (showName == null) {
            showName = chatMessage.getNick();
        }
        
        String avatar = chatMessage.getHeadImage();
        int defaultHeadIcon = chatMessage.getDefaultHeadIcon();
        
        if (extField.isSelfMock) {
            showName = IMUtils.getName(message.getTo());
            avatar = IMUtils.getAvatar(message.getTo());
            defaultHeadIcon = IMUtils.getDefaultHeadIcon(message.getTo());
        }
        
        if (usernickView != null) {
            usernickView.setText(showName);
            if (message.getChatType() != EMMessage.ChatType.ChatRoom) {
                usernickView.setVisibility(GONE);
            }
        }
        if (userAvatarView != null) {
            IMUtils.setAvatar(userAvatarView, avatar, defaultHeadIcon);
        }
        
        if (deliveredView != null) {
            if (message.isDelivered) {
                deliveredView.setVisibility(View.VISIBLE);
            }
            else {
                deliveredView.setVisibility(View.INVISIBLE);
            }
        }
        
        if (ackedView != null) {
            if (message.isAcked) {
                if (deliveredView != null) {
                    deliveredView.setVisibility(View.INVISIBLE);
                }
                ackedView.setVisibility(
                        BaseData.getClientType() == AppCommon.AppType.qingqing_ta
                                ? View.VISIBLE
                                : View.GONE);
            }
            else {
                ackedView.setVisibility(View.GONE);
            }
        }
        
        if (userRoleView != null) {
            String roleText = getRoleText(chatMessage.getChatRoomType(),
                    chatMessage.getUserType());
            userRoleView.setVisibility(
                    chatMessage.getChatRoomScene() == Constant.CHATTYPE_CHATROOM
                            && !chatMessage.hasExtUser() && !TextUtils.isEmpty(roleText)
                                    ? VISIBLE
                                    : GONE);
            userRoleView.setText(roleText);
            userRoleView.setBackgroundDrawable(
                    getRoleBackground(chatMessage.getChatRoomType()));
        }
        
        if (teachingRoleView != null) {
            if (chatMessage.getChatRoomScene() == Constant.CHATTYPE_GROUP
                    && message.getChatType() == EMMessage.ChatType.GroupChat
                    && message.direct == Direct.RECEIVE && !extField.isSelfMock
                    && ChatManager.getInstance()
                            .getGroupType(ChatManager.getInstance().getConversationId(
                                    message)) == ImProto.ChatGroupType.normal_chat_group_type) {
                ContactInfo contactInfo = com.qingqing.base.im.ChatManager.getInstance()
                        .getContactModel().getContactInfo(chatMessage.getCurUserName());
                int resId = 0;
                if (contactInfo != null) {
                    resId = TeachingRoleUtil.getTeachingRoleImageResTeacherList(
                            contactInfo.getHighestTeacherRole());
                }
                
                if (resId == 0) {
                    teachingRoleView.setVisibility(GONE);
                }
                else {
                    teachingRoleView.setVisibility(VISIBLE);
                    teachingRoleView.setImageResource(resId);
                }
            }
            else {
                teachingRoleView.setVisibility(GONE);
            }
        }
        
        if (groupRoleView != null) {
            ContactInfo contactInfo = com.qingqing.base.im.ChatManager.getInstance()
                    .getContactModel().getContactInfo(chatMessage.getCurUserName());
            if (contactInfo != null) {
                groupRoleView.setImageResource(R.drawable.icon_chat_gly);
                groupRoleView.setVisibility(
                        chatMessage.getChatRoomScene() == Constant.CHATTYPE_GROUP
                                && message.getChatType() == EMMessage.ChatType.GroupChat
                                && isGroupAdmin(contactInfo.getGroupRole()) ? VISIBLE
                                        : GONE);
            }
        }
        
        if (userIndexView != null) {
            userIndexView.setVisibility(GONE);
            
            if (message.getChatType() == EMMessage.ChatType.GroupChat
                    && message.direct == Direct.RECEIVE && !extField.isSelfMock
                    && ChatManager.getInstance()
                            .getGroupType(ChatManager.getInstance().getConversationId(
                                    message)) == ImProto.ChatGroupType.teaching_research_chat_group_type) {
                ContactInfo contactInfo = com.qingqing.base.im.ChatManager.getInstance()
                        .getContactModel().getContactInfo(chatMessage.getCurUserName());
                if (contactInfo != null) {
                    if (getHighestGroupRoleType(contactInfo
                            .getGroupRole()) == ImProto.ChatGroupType.normal_chat_group_type) {
                        TeacherIndex teacherIndex = TeacherIndexManager.getInstance()
                                .getTeacherIndex(chatMessage.getCurUserName());
                        if (teacherIndex != null) {
                            userIndexView.setVisibility(VISIBLE);
                            userIndexView.setText(getUserIndexString(teacherIndex));
                        }
                    }
                }
                else {
                    ChatManager.getInstance().checkGroupContact(message);
                }
            }
        }
        
        if (adapter instanceof EaseMessageAdapter) {
            if (userAvatarView != null) {
                if (((EaseMessageAdapter) adapter).isShowAvatar())
                    userAvatarView.setVisibility(View.VISIBLE);
                else
                    userAvatarView.setVisibility(View.GONE);
                if (usernickView != null) {
                    if (((EaseMessageAdapter) adapter).isShowUserNick()
                            && !isGroupMeMsg())
                        usernickView.setVisibility(View.VISIBLE);
                    else
                        usernickView.setVisibility(View.GONE);
                }
            }
            if (bubbleLayout != null) {
                if (message.direct == Direct.SEND || extField.isSelfMock) {
                    if (bubbleLayout.getBackground() != null) {
                        // 在 Adapter 中设置 .9 图片时，因计算效率会出现
                        bubbleLayout.setBackground(getDefaultMyBubbleBg());
                    }
                }
                else if (message.direct == Direct.RECEIVE && !extField.isSelfMock) {
                    if (((EaseMessageAdapter) adapter).getOtherBuddleBg() != null) {
                        bubbleLayout.setBackground(
                                ((EaseMessageAdapter) adapter).getOtherBuddleBg());
                    }
                }
                
                onBubbleLayoutBg();
            }
        }
        else {
            // 处理本地历史记录的ui显示
            // 如果出现其他情况（非聊天和讲座历史），这里需要细化区分
            if (userAvatarView != null) {
                userAvatarView.setVisibility(View.VISIBLE);
            }
            if (usernickView != null) {
                if (isGroupMeMsg()) {
                    usernickView.setVisibility(GONE);
                }
                else {
                    usernickView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    
    private Drawable getDefaultMyBubbleBg() {
        int resId = ThemeConstant.getThemeImChatMyBubbleIcon(getContext());
        if (resId == 0) {
            return getResources().getDrawable(R.drawable.bg_mechat_white);
        }
        else {
            return getResources().getDrawable(resId);
        }
    }
    
    private int getHighestGroupRoleType(ConcurrentHashMap<String, GroupRole> groupRole) {
        int groupRoleType = ImProto.ChatGroupType.normal_chat_group_type;
        if (groupRole != null) {
            Iterator iterator = groupRole.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String groupId = (String) entry.getKey();
                if (groupId.equals(message.getTo())) {
                    GroupRole role = (GroupRole) entry.getValue();
                    for (int i = 0; i < role.getRole().size(); i++) {
                        if (role.getRole().get(
                                i) == UserProto.ChatGroupUserRole.manager_chat_group_user_role) {
                            groupRoleType = UserProto.ChatGroupUserRole.manager_chat_group_user_role;
                        }
                        else if (role.getRole().get(
                                i) == UserProto.ChatGroupUserRole.owner_chat_group_user_role) {
                            groupRoleType = UserProto.ChatGroupUserRole.owner_chat_group_user_role;
                        }
                    }
                }
            }
        }
        
        return groupRoleType;
    }
    
    private String getUserIndexString(TeacherIndex teacherIndex) {
        return (teacherIndex.hasSchoolAge ? teacherIndex.schoolAge : "--") + "年" + " | "
                + (teacherIndex.hasAvgCourseTime
                        ? LogicConfig.getFormatDotString(teacherIndex.avgCourseTime)
                        : "--")
                + "课时" + " | " + "留存 "
                + (teacherIndex.hasRetentionRate
                        ? (LogicConfig.getFormatDotString(teacherIndex.retentionRate)
                                + "%")
                        : "--");
    }
    
    private boolean isGroupMeMsg() {
        return message.getChatType() == EMMessage.ChatType.GroupChat
                && BaseData.getSafeUserId().equals(chatMessage.getCurUserName());
    }
    
    protected void onBubbleLayoutBg() {
        if (message.direct == Direct.RECEIVE && !extField.isSelfMock
                && message.getChatType() == EMMessage.ChatType.ChatRoom) {
            boolean isExpert = false;
            
            if (chatMessage != null) {
                for (int i = 0; i < chatMessage.getChatRoomType().size(); i++) {
                    isExpert = chatMessage.getChatRoomType()
                            .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type;
                }
            }
            if (isExpert) {
                bubbleLayout.setBackgroundResource(R.drawable.bg_trchat);
            }
            else {
                bubbleLayout.setBackgroundResource(R.drawable.bg_youchat);
            }
        }
    }
    
    private boolean isGroupAdmin(ConcurrentHashMap<String, GroupRole> roomRoleType) {
        if (roomRoleType != null) {
            Iterator iterator = roomRoleType.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String groupId = (String) entry.getKey();
                if (groupId.equals(message.getTo())) {
                    GroupRole groupRole = (GroupRole) entry.getValue();
                    for (int i = 0; i < groupRole.getRole().size(); i++) {
                        if (groupRole.getRole().get(
                                i) == UserProto.ChatGroupUserRole.manager_chat_group_user_role) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    String getRoleText(ArrayList<Integer> roomRoleType, Integer userType) {
        if (roomRoleType != null) {
            int roleType = ImProto.ChatRoomRoleType.general_chat_room_role_type;
            for (int i = 0; i < roomRoleType.size(); i++) {
                if (roomRoleType
                        .get(i) == ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    roleType = ImProto.ChatRoomRoleType.admin_chat_room_role_type;
                }
                else if (roomRoleType
                        .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type
                        && roleType != ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    roleType = ImProto.ChatRoomRoleType.mc_chat_room_role_type;
                }
            }
            
            switch (roleType) {
                case ImProto.ChatRoomRoleType.admin_chat_room_role_type:
                    return getResources().getString(R.string.text_role_admin);
                case ImProto.ChatRoomRoleType.mc_chat_room_role_type:
                    if (userType == UserProto.UserType.ta) {
                        return getResources().getString(R.string.text_role_expert_and_ta);
                    }
                    else {
                        return getResources().getString(R.string.text_role_expert);
                    }
                default:
                    break;
            }
        }
        return "";
    }
    
    Drawable getRoleBackground(ArrayList<Integer> roomRoleType) {
        if (roomRoleType != null) {
            int roleType = ImProto.ChatRoomRoleType.general_chat_room_role_type;
            for (int i = 0; i < roomRoleType.size(); i++) {
                if (roomRoleType
                        .get(i) == ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    roleType = ImProto.ChatRoomRoleType.admin_chat_room_role_type;
                }
                else if (roomRoleType
                        .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type
                        && roleType != ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    roleType = ImProto.ChatRoomRoleType.mc_chat_room_role_type;
                }
            }
            
            switch (roleType) {
                case ImProto.ChatRoomRoleType.admin_chat_room_role_type:
                    return getResources()
                            .getDrawable(R.drawable.shape_corner_rect_orange_light_solid);
                case ImProto.ChatRoomRoleType.mc_chat_room_role_type:
                    return getResources()
                            .getDrawable(R.drawable.shape_corner_rect_blue_solid);
                default:
                    break;
            }
        }
        return new ColorDrawable(Color.TRANSPARENT);
    }
    
    /**
     * 设置消息发送callback
     */
    protected void setMessageSendCallback() {
        if (messageSendCallback == null) {
            messageSendCallback = new EMCallBack() {
                
                @Override
                public void onSuccess() {
                    updateView();
                }
                
                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (percentageView != null)
                                percentageView.setText(progress + "%");
                            
                        }
                    });
                }
                
                @Override
                public void onError(int code, String error) {
                    updateView();
                }
            };
        }
        message.setMessageStatusCallback(messageSendCallback);
    }
    
    /**
     * 设置消息接收callback
     */
    protected void setMessageReceiveCallback() {
        if (messageReceiveCallback == null) {
            messageReceiveCallback = new EMCallBack() {
                
                @Override
                public void onSuccess() {
                    updateView();
                }
                
                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (percentageView != null) {
                                percentageView.setText(progress + "%");
                            }
                        }
                    });
                }
                
                @Override
                public void onError(int code, String error) {
                    updateView();
                }
            };
        }
        message.setMessageStatusCallback(messageReceiveCallback);
    }
    
    protected void setClickListener() {
        if (bubbleLayout != null) {
            bubbleLayout.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        if (!itemClickListener.onBubbleClick(message)) {
                            // 如果listener返回false不处理这个事件，执行lib默认的处理
                            onBubbleClick();
                        }
                    }
                }
            });
            
            bubbleLayout.setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleLongClick(message);
                    }
                    return true;
                }
            });
        }
        
        if (statusView != null) {
            statusView.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onResendClick(message);
                    }
                }
            });
        }
        
        if (userAvatarView != null) {
            userAvatarView.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onUserAvatarClick(chatMessage);
                    }
                }
            });
            userAvatarView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onUserAvatarLongClick(chatMessage);
                    }
                    return true;
                }
            });
        }
    }
    
    protected void updateView() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (message.status == EMMessage.Status.FAIL) {
                    
                    if (message.getError() == EMError.MESSAGE_SEND_INVALID_CONTENT) {
                        ToastWrapper
                                .show(activity.getString(R.string.send_fail) + activity
                                        .getString(R.string.error_send_invalid_content));
                    }
                    else if (message
                            .getError() == EMError.MESSAGE_SEND_NOT_IN_THE_GROUP) {
                        ToastWrapper
                                .show(activity.getString(R.string.send_fail) + activity
                                        .getString(R.string.error_send_not_in_the_group));
                    }
                    else {
                        ToastWrapper.show(activity.getString(R.string.send_fail)
                                + activity.getString(R.string.connect_failuer_toast));
                    }
                }
                
                onUpdateView();
            }
        });
        
    }
    
    /**
     * 填充layout
     */
    protected abstract void onInflatView();
    
    /**
     * 查找chatrow里的控件
     */
    protected abstract void onFindViewById();
    
    /**
     * 消息状态改变，刷新listview
     */
    protected abstract void onUpdateView();
    
    /**
     * 设置更新控件属性
     */
    protected abstract void onSetUpView();
    
    /**
     * 聊天气泡被点击事件
     */
    protected abstract void onBubbleClick();
    
}
