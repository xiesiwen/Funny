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
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.view.AsyncImageViewV2;

/**
 * Created by huangming on 2015/12/26.
 */
public class ChatRowCmdRecTeacher extends EaseChatRow {
    
    private AsyncImageViewV2 mHeadImg;
    private TextView mNickTv;
    private TextView mDescriptionTv;
    private TextView mGoodAppraiseTv;
    private TextView mCourseTv;
    private TextView mPriceTv;
    
    public ChatRowCmdRecTeacher(Context context, EMMessage message, int position,
            BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        boolean send = cmdMsg != null && extField.isSelfMock;
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE && !send
                ? R.layout.chat_row_received_cmd_rec_teacher
                : R.layout.chat_row_sent_cmd_rec_teacher, this);
    }
    
    @Override
    protected void onFindViewById() {
        
        mHeadImg = (AsyncImageViewV2) findViewById(R.id.img_teacher_head);
        mNickTv = (TextView) findViewById(R.id.tv_teacher_nick);
        mDescriptionTv = (TextView) findViewById(R.id.tv_teacher_description);
        mGoodAppraiseTv = (TextView) findViewById(R.id.tv_teacher_good_appraise);
        mCourseTv = (TextView) findViewById(R.id.tv_teacher_course);
        mPriceTv = (TextView) findViewById(R.id.tv_price);
        
    }
    
    @Override
    protected void onUpdateView() {}
    
    @Override
    protected void onSetUpView() {
        
        if (message.direct == EMMessage.Direct.SEND && bubbleLayout != null) {
            bubbleLayout.setBackgroundDrawable(
                    getResources().getDrawable(R.drawable.bg_mechat_white));
        }
        
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        if (cmdMsg != null) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            String nick = bundle.getString(CmdMsg.RecTeacher.NICK);
            String headImg = bundle.getString(CmdMsg.RecTeacher.HEAD_IMG);
            int goodAppraiseCount = bundle.getInt(CmdMsg.RecTeacher.GOOD_APPRAISE_COUNT);
            String description = bundle.getString(CmdMsg.RecTeacher.DESCRPTION);
            String courseName = bundle.getString(CmdMsg.RecTeacher.COURSE_NAME);
            String gradeCourse = bundle.getString(CmdMsg.RecTeacher.GRADE_COURSE);
            double minPrice = bundle.getDouble(CmdMsg.RecTeacher.MIN_PRICE);
            double maxPrice = bundle.getDouble(CmdMsg.RecTeacher.MAX_PRICE);
            if (mPriceTv != null) {
                mPriceTv.setText(
                        getResources().getString(R.string.chat_row_teacher_price_text,
                                LogicConfig.getFormatDotString(minPrice),
                                LogicConfig.getFormatDotString(maxPrice)));
            }
            if (!TextUtils.isEmpty(gradeCourse)) {
                mCourseTv.setVisibility(VISIBLE);
                mCourseTv.setText(gradeCourse);
            }
            else {
                mCourseTv.setVisibility(GONE);
                mCourseTv.setText("");
            }
            if (goodAppraiseCount < 10) {
                mGoodAppraiseTv.setText(getResources().getString(
                        R.string.chat_row_teacher_no_good_appraise_text, courseName));
            }
            else {
                mGoodAppraiseTv.setText(getResources().getString(
                        R.string.chat_row_teacher_good_appraise_text, courseName,
                        String.valueOf(goodAppraiseCount)));
            }
            mDescriptionTv.setText(description == null ? "" : description);
            mDescriptionTv.setVisibility(VISIBLE);
            mNickTv.setText(TextUtils.isEmpty(nick) ? "" : nick);
            IMUtils.setAvatar(mHeadImg, headImg);
            String curUserName = extField.isSelfMock ? message.getTo()
                    : message.getFrom();
            String showName = IMUtils.getName(curUserName);
            String avatar = IMUtils.getAvatar(curUserName);
            int defaultHeadIcon = IMUtils.getDefaultHeadIcon(curUserName);
            if (userAvatarView != null) {
                userAvatarView
                        .setVisibility(extField.needShowFrom ? View.VISIBLE : View.GONE);
                IMUtils.setAvatar(userAvatarView, avatar, defaultHeadIcon);
            }
            if (extField.isSelfMock) {
                if (usernickView != null) {
                    usernickView.setText(IMUtils.getName(showName));
                    usernickView.setVisibility(GONE);
                }
                if (userAvatarView != null) {
                    userAvatarView.setOnClickListener(null);
                }
            }
            
        }
    }
    
    @Override
    protected void setClickListener() {
        super.setClickListener();
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        if (cmdMsg != null && extField.isSelfMock) {
            if (userAvatarView != null) {
                userAvatarView.setOnClickListener(null);
            }
        }
    }
    
    @Override
    protected void onBubbleClick() {
        
    }
}
