package com.qingqing.base.nim.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.easemob.util.DateUtils;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.constant.ThemeConstant;
import com.qingqing.base.nim.domain.ChatType;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.ui.lecture.LectureConfig;
import com.qingqing.base.nim.utils.ChatContactUtils;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;

import java.util.Date;

/**
 * Created by huangming on 2016/8/18.
 */
public abstract class ChatRowDefaultView extends ChatRowView {
    
    private AsyncImageViewV2 avatarView;
    private TextView nickView;
    private TextView msgTimeView;
    private TextView roleView;
    private View bubbleLayout;
    private ImageView resendView;
    private ProgressBar progressBar;
    
    public ChatRowDefaultView(Context context, Message message) {
        super(context, message);
    }
    
    @CallSuper
    @Override
    protected void onFindViewById() {
        msgTimeView = (TextView) findViewById(R.id.tv_msg_time);
        avatarView = (AsyncImageViewV2) findViewById(R.id.img_avatar);
        nickView = (TextView) findViewById(R.id.tv_nick);
        bubbleLayout = findViewById(R.id.layout_bubble);
        roleView = (TextView) findViewById(R.id.tv_user_role);
        
        resendView = (ImageView) findViewById(R.id.img_resend);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }
    
    public AsyncImageViewV2 getAvatarView() {
        return avatarView;
    }
    
    public ProgressBar getProgressBar() {
        return progressBar;
    }
    
    public TextView getNickView() {
        return nickView;
    }
    
    public ImageView getResendView() {
        return resendView;
    }
    
    public TextView getMsgTimeView() {
        return msgTimeView;
    }
    
    public TextView getRoleView() {
        return roleView;
    }
    
    public View getBubbleLayout() {
        return bubbleLayout;
    }
    
    protected final void onSetupViewBy(final Message preMessage, final Message message) {
        
        final boolean isSendDirect = isSendDirect();
        
        final boolean isSingleChat = message.getChatType() == ChatType.Chat;
        
        if (getMsgTimeView() != null) {
            if (preMessage == null) {
                getMsgTimeView().setText(
                        DateUtils.getTimestampString(new Date(message.getMsgTime())));
                getMsgTimeView().setVisibility(View.VISIBLE);
            }
            else {
                // 两条消息时间离得如果稍长，显示时间
                if (DateUtils.isCloseEnough(message.getMsgTime(),
                        preMessage.getMsgTime())) {
                    getMsgTimeView().setVisibility(View.GONE);
                }
                else {
                    getMsgTimeView().setText(
                            DateUtils.getTimestampString(new Date(message.getMsgTime())));
                    getMsgTimeView().setVisibility(View.VISIBLE);
                }
            }
        }
        
        if (getAvatarView() != null) {
            getAvatarView().setImageUrl(
                    ImageUrlUtil.getHeadImg(ChatContactUtils.getHeadImage(message)),
                    ChatContactUtils.getHeadDefaultIcon(message));
            getAvatarView().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getChatRowClickListener() != null) {
                        getChatRowClickListener().onAvatarClick(getMessage());
                    }
                }
            });
        }
        
        if (getNickView() != null) {
            getNickView().setVisibility(isSingleChat ? GONE : VISIBLE);
            getNickView().setText(ChatContactUtils.getNick(message));
        }
        
        if (getRoleView() != null) {
            if (message.hasPlayedRole()) {
                getRoleView().setVisibility(VISIBLE);
            }
            else {
                getRoleView().setVisibility(GONE);
            }
            
            boolean hasPlayedRole = message.hasPlayedRole();
            int roleType = hasPlayedRole
                    ? LectureConfig.getHighestRoleType(message.getRole().getRoleType())
                    : ImProto.ChatRoomRoleType.unknown_chat_room_role_type;
            int userType = hasPlayedRole ? message.getRole().getUserType()
                    : UserProto.UserType.unknown_user_type;
            String roleText = getRoleText(roleType, userType);
            getRoleView().setVisibility(
                    !isSingleChat && hasPlayedRole && !TextUtils.isEmpty(roleText)
                            ? VISIBLE : GONE);
            getRoleView().setText(roleText);
            getRoleView().setBackgroundDrawable(getRoleBackground(roleType));
        }
        
        if (getResendView() != null) {
            getResendView().setVisibility(
                    isSendDirect && message.getStatus() == Message.Status.FAIL ? VISIBLE
                            : GONE);
            getResendView().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getChatRowClickListener() != null) {
                        getChatRowClickListener().onResendClick(message);
                    }
                }
            });
        }
        
        if (getBubbleLayout() != null) {
            if (message.getDirect() == Message.Direct.SEND) {
                int resId = ThemeConstant.getThemeImChatMyBubbleIcon(getContext());
                if (resId == 0) {
                    getBubbleLayout().setBackgroundResource(R.drawable.bg_mechat_white);
                }
                else {
                    getBubbleLayout().setBackgroundResource(resId);
                }
            }
            getBubbleLayout().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getChatRowClickListener() != null) {
                        getChatRowClickListener().onBubbleClick(message);
                    }
                }
            });
            
            getBubbleLayout().setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getChatRowClickListener() != null) {
                        getChatRowClickListener().onBubbleLongClick(message);
                    }
                    return true;
                }
            });
        }
        
        if (getProgressBar() != null) {
            boolean inProgress = message.getStatus() == Message.Status.IN_PROGRESS;
            getProgressBar().setVisibility(inProgress ? VISIBLE : GONE);
        }
        
        onSetupViewBy(message);
    }
    
    protected void onSetupViewBy(Message message) {
        
    }
    
    String getRoleText(Integer roomRoleType, Integer userType) {
        if (roomRoleType != null) {
            switch (roomRoleType) {
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
    
    Drawable getRoleBackground(Integer roomRoleType) {
        if (roomRoleType != null) {
            switch (roomRoleType) {
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
}
