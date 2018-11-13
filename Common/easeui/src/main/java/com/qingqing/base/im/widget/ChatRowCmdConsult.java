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
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.view.AsyncImageViewV2;

/**
 * Created by huangming on 2015/12/26.
 */
public class ChatRowCmdConsult extends EaseChatRow {
    
    private AsyncImageViewV2 mHeadImg;
    private TextView mNickTv;
    private TextView mTaRealNameTv;
    private TextView mTeacherRealNameTv;
    private TextView mGradeSubjectTv;
    private TextView mAddressTv;
    
    public ChatRowCmdConsult(Context context, EMMessage message, int position,
            BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        inflater.inflate(
                message.direct == EMMessage.Direct.RECEIVE && !extField.isSelfMock
                        ? R.layout.chat_row_received_cmd_consult
                        : R.layout.chat_row_received_cmd_consult,
                this);
    }
    
    @Override
    protected void onFindViewById() {
        mHeadImg = (AsyncImageViewV2) findViewById(R.id.img_teacher_head);
        mNickTv = (TextView) findViewById(R.id.tv_teacher_nick);
        mTaRealNameTv = (TextView) findViewById(R.id.tv_ta_real_name);
        mTeacherRealNameTv = (TextView) findViewById(R.id.tv_teacher_real_name);
        mGradeSubjectTv = (TextView) findViewById(R.id.tv_teacher_grade_subject);
        mAddressTv = (TextView) findViewById(R.id.tv_teacher_address);
        
    }
    
    @Override
    protected void onUpdateView() {
        
    }
    
    @Override
    protected void onSetUpView() {
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        if (cmdMsg != null) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            String address = bundle.getString(CmdMsg.Consult.ADDRESS);
            String realName = bundle.getString(CmdMsg.Consult.TEACHER_REAL_NAME);
            String taRealName = bundle.getString(CmdMsg.Consult.TA_REAL_NAME);
            String courseGrade = bundle.getString(CmdMsg.Consult.FORMAT_COURSE_GRADE);
            String nick = bundle.getString(CmdMsg.Consult.NICK);
            String headImg = bundle.getString(CmdMsg.Consult.HEAD_IMG);
            
            mAddressTv.setText(!TextUtils.isEmpty(address) ? address : "");
            mGradeSubjectTv.setText(!TextUtils.isEmpty(courseGrade) ? courseGrade : "");
            mNickTv.setText(!TextUtils.isEmpty(nick) ? nick : "");
            if (!TextUtils.isEmpty(taRealName)) {
                mTaRealNameTv.setVisibility(VISIBLE);
                mTaRealNameTv.setText(taRealName);
            }
            else {
                mTaRealNameTv.setText("");
                mTaRealNameTv.setVisibility(GONE);
            }
            
            if (!TextUtils.isEmpty(realName)) {
                mTeacherRealNameTv.setVisibility(VISIBLE);
                mTeacherRealNameTv.setText(realName);
            }
            else {
                mTeacherRealNameTv.setText("");
                mTeacherRealNameTv.setVisibility(GONE);
            }
            
            IMUtils.setAvatar(mHeadImg, headImg);
            
            if (userAvatarView != null) {
                userAvatarView
                        .setVisibility(extField.needShowFrom ? View.VISIBLE : View.GONE);
            }
            
        }
    }
    
    @Override
    protected void onBubbleClick() {
        
    }
}
