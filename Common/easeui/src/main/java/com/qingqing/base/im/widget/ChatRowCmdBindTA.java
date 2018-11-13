package com.qingqing.base.im.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.utils.TextViewUtil;

/**
 * Created by huangming on 2015/12/26. 只有助教端
 */
public class ChatRowCmdBindTA extends EaseChatRow {
    
    protected TextView contentView;
    
    public ChatRowCmdBindTA(Context context, EMMessage message, int position,
            BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        inflater.inflate(R.layout.chat_row_received_cmd_bind_ta, this);
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
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        if (cmdMsg != null) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            String gradeName = bundle.getString(CmdMsg.BindTA.GRADE_NAME);
            String headImg = bundle.getString(CmdMsg.BindTA.HEAD_IMG);
            String phone = bundle.getString(CmdMsg.BindTA.PHONE_NUMBER);
            String flowType = bundle.getString(CmdMsg.BindTA.FLOW_TYPE);
            
            // 添加电话识别
            String content = "家长信息：" + gradeName + ", " + phone;
            if (!TextUtils.isEmpty(flowType)) {
                content += (", " + flowType);
            }
            contentView.setText(content);
            TextViewUtil.detectPhoneNumber(contentView);
            if (userAvatarView != null) {
                userAvatarView
                        .setVisibility(extField.needShowFrom ? View.VISIBLE : View.GONE);
            }
            
        }
        
    }
    
    @Override
    protected void onBubbleClick() {}
}
