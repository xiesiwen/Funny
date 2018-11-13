package com.qingqing.base.nim.view;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.text.InputFilter;
import android.view.View;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.nim.domain.ChatType;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.domain.TextMessageBody;
import com.qingqing.base.utils.TextViewUtil;

/**
 * Created by huangming on 2016/8/18.
 */
public class ChatRowTextView extends ChatRowDefaultView {
    
    private TextView msgTextView;
    
    public ChatRowTextView(Context context, Message message) {
        super(context, message);
    }
    
    @Override
    protected int onInflateReceivedLayout() {
        return R.layout.chat_row_new_received_text;
    }
    
    @Override
    protected int onInflateSentLayout() {
        return R.layout.chat_row_new_sent_text;
    }
    
    @CallSuper
    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        msgTextView = (TextView) findViewById(R.id.tv_msg_text);
    }
    
    @Override
    protected void onSetupViewBy(Message message) {
        super.onSetupViewBy(message);
        
        TextMessageBody textMessageBody = (TextMessageBody) message.getBody();
        msgTextView.setText(textMessageBody.getText());
        
        // 家长每条消息最多100字，其它身份每条消息最多500字。家长发的文本超过20行强制截断防止被金箍棒。
        if (isSendDirect() && message.getChatType() == ChatType.ChatRoom) {
            boolean isStudent = false;
            
            if (message.hasPlayedRole()) {
                for (int i = 0; i < message.getRole().getRoleType().size(); i++) {
                    if (message.getRole().getRoleType()
                            .get(i) != ImProto.ChatRoomRoleType.mc_chat_room_role_type
                            && message.getRole().getRoleType().get(
                                    i) != ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                        isStudent = true;
                    }
                }
            }
            if (isStudent) {
                msgTextView.setMaxLines(20);
                msgTextView.setFilters(
                        new InputFilter[] { new InputFilter.LengthFilter(100) });
            }
            else {
                msgTextView.setMaxLines(Integer.MAX_VALUE);
                msgTextView.setFilters(
                        new InputFilter[] { new InputFilter.LengthFilter(500) });
            }
        }
        msgTextView.setText(textMessageBody.getText());
        
        int clickColorRes = 0;
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher
                && isSendDirect()) {
            clickColorRes = R.color.teacher_click_highlight;
        }
        TextViewUtil.detectPhoneNumber(msgTextView, clickColorRes);
        com.qingqing.qingqingbase.utils.TextViewUtil.detectUrl(msgTextView,
                clickColorRes);
        
        msgTextView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (getChatRowClickListener() != null) {
                    getChatRowClickListener().onBubbleLongClick(getMessage());
                }
                return true;
            }
        });
    }
}
