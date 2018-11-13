package com.qingqing.base.im.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.widget.chatrow.EaseChatRowText;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.utils.TextViewUtil;

/**
 * Created by huangming on 2016/12/2.
 */

public class ChatRowCmdRemindST extends EaseChatRowText {
    
    public ChatRowCmdRemindST(Context context, EMMessage message, int position,
            BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        inflater.inflate(R.layout.chat_row_new_cmd_remind_st, this);
    }
    
    @Override
    public void onSetUpView() {
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        if (cmdMsg != null) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            String cmdText = bundle.getString(CmdMsg.Text.TEXT);
            contentView.setText(cmdText);
            TextViewUtil.detectPhoneNumber(contentView);
            if (userAvatarView != null) {
                userAvatarView
                        .setVisibility(extField.needShowFrom ? View.VISIBLE : View.GONE);
            }
            
        }
    }
    
}
