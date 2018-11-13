package com.qingqing.base.im.widget;

import android.content.Context;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;

/**
 * 只有老师端
 */
public class ChatRowCmdOverdueApplyCancelCourse extends EaseChatRow {
    
    protected TextView contentView;
    
    public ChatRowCmdOverdueApplyCancelCourse(Context context, EMMessage message,
            int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        inflater.inflate(
                message.direct == EMMessage.Direct.RECEIVE && !extField.isSelfMock
                        ? R.layout.chat_row_received_cmd_overdue_apply_cancel_course
                        : R.layout.chat_row_sent_cmd_overdue_apply_cancel_course,
                this);
    }
    
    @Override
    protected void onFindViewById() {
        contentView = (TextView) findViewById(R.id.tv_chatcontent);
    }
    
    @Override
    protected void onUpdateView() {
        
    }
    
    @Override
    protected void onSetUpView() {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(message);
        
        if (bundle != null) {
            String text = bundle.getString(CmdMsg.Text.TEXT);
            contentView.setText(text);
        }
        
    }
    
    @Override
    protected void onBubbleClick() {}
}
