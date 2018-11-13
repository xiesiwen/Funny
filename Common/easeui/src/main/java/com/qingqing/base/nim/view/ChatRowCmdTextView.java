package com.qingqing.base.nim.view;

import android.content.Context;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.base.nim.cmd.CmdUtils;
import com.qingqing.base.nim.domain.CmdMessageBody;
import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/27.
 */
public class ChatRowCmdTextView extends ChatRowDefaultView {

    private TextView cmdTextView;
    
    public ChatRowCmdTextView(Context context, Message message) {
        super(context, message);
    }
    
    @Override
    protected int onInflateReceivedLayout() {
        return R.layout.chat_row_new_cmd_text;
    }
    
    @Override
    protected int onInflateSentLayout() {
        return R.layout.chat_row_new_cmd_text;
    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        cmdTextView = (TextView) findViewById(R.id.tv_cmd_text);
    }

    private TextView getCmdTextView() {
        return cmdTextView;
    }

    @Override
    protected void onSetupViewBy(Message message) {
        super.onSetupViewBy(message);
        CmdMessageBody body = (CmdMessageBody) message.getBody();
        if(getCmdTextView() != null) {
            getCmdTextView().setText(CmdUtils.getText(getContext(), body));
        }
    }
}
