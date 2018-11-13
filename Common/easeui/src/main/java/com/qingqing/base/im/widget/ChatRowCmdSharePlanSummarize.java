package com.qingqing.base.im.widget;

import android.content.Context;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;

/**
 * 分享教学计划/阶段总结的 cmd 样式
 *
 * Created by lihui on 2017/11/1.
 */
public class ChatRowCmdSharePlanSummarize extends EaseChatRow {
    
    private TextView mTvShareTitle;
    private TextView mTvShareContent;
    
    public ChatRowCmdSharePlanSummarize(Context context, EMMessage message, int position,
            BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(message);
        String fromUserId = bundle.getString(CmdMsg.Group.QQ_USER_ID);
        inflater.inflate(!BaseData.getSafeUserId().equals(fromUserId)
                ? R.layout.chat_row_received_cmd_share_plan_summarize
                : R.layout.chat_row_sent_cmd_share_plan_summarize, this);
    }
    
    @Override
    protected void onFindViewById() {
        mTvShareTitle = (TextView) findViewById(R.id.tv_share_title);
        mTvShareContent = (TextView) findViewById(R.id.tv_share_content);
    }
    
    @Override
    protected void onUpdateView() {}
    
    @Override
    protected void onSetUpView() {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(message);
        
        if (message.direct == EMMessage.Direct.SEND && bubbleLayout != null) {
            bubbleLayout.setBackgroundDrawable(
                    getResources().getDrawable(R.drawable.bg_mechat_white));
        }
        
        mTvShareTitle.setText(bundle.getString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_TITLE));
        mTvShareContent
                .setText(bundle.getString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_CONTENT));
    }
    
    @Override
    protected void onBubbleClick() {}
}
