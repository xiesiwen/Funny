package com.easemob.easeui.widget.chatrow;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.TextMessageBody;
import com.easemob.easeui.R;
import com.easemob.easeui.utils.EaseSmileUtils;
import com.easemob.exceptions.EaseMobException;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.ChatRoomModel;
import com.qingqing.base.utils.TextViewUtil;

public class EaseChatRowText extends EaseChatRow {

    protected TextView contentView;

    public EaseChatRowText(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(
                message.direct == EMMessage.Direct.RECEIVE && !extField.isSelfMock
                        ? R.layout.ease_row_received_message
                        : R.layout.ease_row_sent_message,
                this);
    }
    
    @Override
    protected void onFindViewById() {
        contentView = (TextView) findViewById(R.id.tv_chatcontent);
    }

    @Override
    public void onSetUpView() {
        //家长每条消息最多100字，其它身份每条消息最多500字。家长发的文本超过20行强制截断防止被金箍棒。
        if (message.direct == EMMessage.Direct.SEND && message.getChatType() == ChatType.ChatRoom) {
            ChatRoomModel chatRoomModel = ChatRoomModel.getModel(ChatManager.getInstance().getConversationId(message));
            
            boolean isStudent = false;
            if (chatRoomModel != null) {
                for (int i = 0; i < chatRoomModel.getChatRoomType().size(); i++) {
                    if (chatRoomModel.getChatRoomType()
                            .get(i) != ImProto.ChatRoomRoleType.mc_chat_room_role_type
                            && chatRoomModel.getChatRoomType().get(
                                    i) != ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                        isStudent = true;
                    }
                }
            }
            
            if (isStudent) {
                contentView.setMaxLines(20);
                contentView.setFilters(
                        new InputFilter[] { new InputFilter.LengthFilter(100) });
            }
            else {
                contentView.setMaxLines(Integer.MAX_VALUE);
                contentView.setFilters(
                        new InputFilter[] { new InputFilter.LengthFilter(500) });
            }
        }
        
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
        Spannable span = EaseSmileUtils.getSmiledText(context, txtBody.getMessage());
        contentView.setText(span, BufferType.SPANNABLE);
        contentView.setMovementMethod(LinkMovementMethod.getInstance());
        
        int clickColorRes = 0;
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher
                && message.direct == EMMessage.Direct.SEND) {
            clickColorRes = R.color.teacher_click_highlight;
        } else if (message.direct == EMMessage.Direct.RECEIVE) {
            clickColorRes = R.color.teacher_click_highlight;//因为去掉了收到消息布局文件里面的autoLink，需要手动设置url文字的颜色
        }
        com.qingqing.qingqingbase.utils.TextViewUtil.detectUrl(contentView,
                clickColorRes);
        TextViewUtil.detectPhoneNumber(contentView, clickColorRes);
        handleTextMessage();
    }
    
    protected void handleTextMessage() {
        if (message.direct == EMMessage.Direct.SEND) {
            setMessageSendCallback();
            switch (message.status) {
                case CREATE:
                    progressBar.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.GONE);
                    // 发送消息
//                sendMsgInBackground(message);
                    break;
                case SUCCESS: // 发送成功
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.GONE);
                    break;
                case FAIL: // 发送失败
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS: // 发送中
                    progressBar.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        } else {
            if (!message.isAcked() && message.getChatType() == ChatType.Chat) {
                try {
                    EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                    message.isAcked = true;
                } catch (EaseMobException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void setClickListener() {
        super.setClickListener();

        //处理BubbleLayout和TextView长按事件冲突的问题
        if (contentView != null) {
            contentView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleLongClick(message);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
        // TODO Auto-generated method stub

    }


}
