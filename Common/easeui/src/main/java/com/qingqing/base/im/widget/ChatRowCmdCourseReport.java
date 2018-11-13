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
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.view.AsyncImageViewV2;

/**
 * 课程报告的 cmd 样式
 *
 * Created by lihui on 2017/7/18.
 */
public class ChatRowCmdCourseReport extends EaseChatRow {
    
    private TextView mTvCourseReportTitle;
    private AsyncImageViewV2 mIvCourseReporterHeadImage;
    private TextView mTvCourseReporterNick;
    private TextView mTvCourseReportBrief;
    private String mShareCode;
    
    public ChatRowCmdCourseReport(Context context, EMMessage message, int position,
            BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(message);
        String fromUserId = bundle.getString(CmdMsg.Group.QQ_USER_ID);
        inflater.inflate(!BaseData.getSafeUserId().equals(fromUserId)
                ? R.layout.chat_row_received_cmd_course_report
                : R.layout.chat_row_sent_cmd_course_report, this);
    }
    
    @Override
    protected void onFindViewById() {
        mTvCourseReportTitle = (TextView) findViewById(R.id.tv_course_report_title);
        mIvCourseReporterHeadImage = (AsyncImageViewV2) findViewById(
                R.id.iv_course_reporter_head_image);
        mTvCourseReporterNick = (TextView) findViewById(R.id.tv_course_reporter_nick);
        mTvCourseReportBrief = (TextView) findViewById(R.id.tv_course_report_brief);
    }
    
    @Override
    protected void onUpdateView() {}
    
    @Override
    protected void onSetUpView() {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(message);
        
        mShareCode = bundle.getString(CmdMsg.Group.SHARE_CODE);
        
        if (message.direct == EMMessage.Direct.SEND && bubbleLayout != null) {
            bubbleLayout.setBackgroundDrawable(
                    getResources().getDrawable(R.drawable.bg_mechat_white));
        }
        if (usernickView != null) {
            usernickView.setText(bundle.getString(CmdMsg.Group.NICK));
            usernickView.setVisibility(VISIBLE);
        }
        
        mTvCourseReportTitle.setText(bundle.getString(CmdMsg.Group.REPORT_TITLE));
        IMUtils.setAvatar(mIvCourseReporterHeadImage,
                bundle.getString(CmdMsg.Group.HEAD_IMAGE),
                chatMessage.getDefaultHeadIcon());
        mTvCourseReporterNick.setText(bundle.getString(CmdMsg.Group.NICK));
        
        int wordCount = bundle.getInt(CmdMsg.Group.TOTAL_WORDS_COUNT);
        int audioLength = bundle.getInt(CmdMsg.Group.TOTAL_AUDIO_TIME_LENGTH);
        int pictureCount = bundle.getInt(CmdMsg.Group.TOTAL_PICTURE_COUNT);
        String brief = "";
        if (wordCount > 0) {
            brief += wordCount + "字" + " ";
        }
        if (audioLength > 0) {
            brief += audioLength + "秒语音" + " ";
        }
        if (pictureCount > 0) {
            brief += pictureCount + "张图片" + " ";
        }
        if (brief.endsWith(" ")) {
            brief = brief.substring(0, brief.length() - 1);
        }
        mTvCourseReportBrief.setText(brief);
        
    }
    
    @Override
    protected void onBubbleClick() {}
}
