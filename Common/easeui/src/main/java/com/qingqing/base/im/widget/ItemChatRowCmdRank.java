package com.qingqing.base.im.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.view.AsyncImageViewV2;

/**
 * Created by lihui on 2017/7/25.
 */

public class ItemChatRowCmdRank extends RelativeLayout {
    private ImageView mIvRank;
    private AsyncImageViewV2 mAivHeadImage;
    private TextView mTvNick;
    private ImageView mIvTrend;
    private TextView mTvValue;
    
    public ItemChatRowCmdRank(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_row_cmd_rank,
                this);
        initView(view);
    }
    
    private void initView(View view) {
        mIvRank = (ImageView) view.findViewById(R.id.iv_rank);
        mAivHeadImage = (AsyncImageViewV2) view.findViewById(R.id.aiv_head_image);
        mTvNick = (TextView) view.findViewById(R.id.tv_nick);
        mIvTrend = (ImageView) view.findViewById(R.id.iv_trend);
        mTvValue = (TextView) view.findViewById(R.id.tv_value);
    }
    
    public ItemChatRowCmdRank setRank(int rank) {
        int resId = 0;
        switch (rank) {
            case 1:
                resId = R.drawable.icon_medal1;
                break;
            case 2:
                resId = R.drawable.icon_medal2;
                break;
            case 3:
                resId = R.drawable.icon_medal3;
                break;
            default:
                break;
        }
        mIvRank.setImageResource(resId);
        return this;
    }
    
    public ItemChatRowCmdRank setHeadImage(String headImage) {
        mAivHeadImage.setImageUrl(headImage,
                LogicConfig.getDefaultHeadIcon(UserProto.SexType.male));
        return this;
    }
    
    public ItemChatRowCmdRank setNick(String nick) {
        mTvNick.setText(nick);
        return this;
    }
    
    public ItemChatRowCmdRank setTrend(int trend) {
        int resId;
        switch (trend) {
            case AppCommon.TendencyType.balance_tendency_type:
                resId = R.drawable.icon_keep;
                break;
            case AppCommon.TendencyType.ascend_tendency_type:
                resId = R.drawable.icon_up;
                break;
            case AppCommon.TendencyType.descend_tendency_type:
                resId = R.drawable.icon_down;
                break;
            default:
                resId = R.drawable.icon_keep;
                break;
        }
        mIvTrend.setImageResource(resId);
        return this;
    }
    
    public ItemChatRowCmdRank setIndexValue(String indexValue) {
        mTvValue.setText(indexValue);
        return this;
    }
}
