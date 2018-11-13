package com.qingqing.base.im.widget;

import android.content.Context;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.model.GroupRankInfo;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.utils.ImageUrlUtil;

import java.util.ArrayList;

/**
 * 指标排名的 cmd 样式
 *
 * Created by lihui on 2017/7/24.
 */
public class ChatRowCmdRank extends EaseChatRow {
    private TextView mTvTitle;
    private LinearLayout mLlRank;
    
    public ChatRowCmdRank(Context context, EMMessage message, int position,
            BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflatView() {
        inflater.inflate(R.layout.chat_row_cmd_rank, this);
    }
    
    @Override
    protected void onFindViewById() {
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mLlRank = (LinearLayout) findViewById(R.id.ll_rank);
    }
    
    @Override
    protected void onUpdateView() {}
    
    @Override
    protected void onSetUpView() {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(message);
        
        String title = bundle.getString(CmdMsg.Group.RANK_TITLE);
        ArrayList<GroupRankInfo> rankInfoList = bundle
                .getParcelableArrayList(CmdMsg.Group.RANK_INFO_LIST);
        
        mTvTitle.setText(title);
        
        mLlRank.removeAllViews();
        if (rankInfoList != null) {
            for (int i = 0; i < rankInfoList.size(); i++) {
                // 最多显示3个
                if (i > 2) {
                    break;
                }
                GroupRankInfo groupRankInfo = rankInfoList.get(i);
                
                ItemChatRowCmdRank item = new ItemChatRowCmdRank(context)
                        .setRank(groupRankInfo.rank)
                        .setHeadImage(ImageUrlUtil.getHeadImg(groupRankInfo.headImage))
                        .setNick(groupRankInfo.nick).setIndexValue(groupRankInfo.value)
                        .setTrend(groupRankInfo.trend);
                
                mLlRank.addView(item);
            }
        }
    }
    
    @Override
    protected void onBubbleClick() {}
}
