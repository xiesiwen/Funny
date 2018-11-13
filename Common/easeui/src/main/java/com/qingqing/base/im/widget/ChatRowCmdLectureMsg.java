package com.qingqing.base.im.widget;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;

/**
 * Created by huangming on 2016/5/30.
 */
public class ChatRowCmdLectureMsg extends EaseChatRow {

    protected TextView contentView;

    public ChatRowCmdLectureMsg(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(R.layout.chat_row_received_cmd_group_msg, this);
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
            contentView.setText(CmdMsgParser.getLectureText(getContext(), cmdMsg, ChatManager.getInstance().getMockUserName(message)));
        }
    }

    @Override
    protected void onBubbleClick() {
    }
}
