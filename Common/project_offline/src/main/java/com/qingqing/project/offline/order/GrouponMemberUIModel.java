package com.qingqing.project.offline.order;

import android.content.Context;
import android.databinding.Bindable;

import com.qingqing.api.proto.v1.order.Order;
import com.qingqing.qingqingbase.ui.BaseUIModel;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.project.offline.BR;
import com.qingqing.project.offline.R;

import java.util.Date;

/**
 * Created by wangxiaxin on 2016/8/29.
 *
 * 群组成员 ui model
 */
public class GrouponMemberUIModel extends BaseUIModel {
    
    private Order.GroupUserOrderInfo userInfo;
    
    public GrouponMemberUIModel(Context ctx) {
        super(ctx);
    }
    
    public void setMemberInfo(Order.GroupUserOrderInfo detail) {
        this.userInfo = detail;
        notifyPropertyChanged(BR.chiefMember);
        notifyPropertyChanged(BR.memberTitle);
        notifyPropertyChanged(BR.memberJoinTime);
    }
    
    @Bindable
    public boolean isChiefMember() {
        return userInfo != null && userInfo.isLeader;
    }
    
    @Bindable
    public String getMemberTitle() {
        if (isChiefMember()) {
            return mCtx.getString(R.string.groupon_detail_chief_member_title,
                    userInfo.userInfo.nick);
        }
        else {
            return userInfo != null ? userInfo.userInfo.nick : "";
        }
    }
    
    @Bindable
    public String getMemberHeadUrl() {
        return userInfo != null ? ImageUrlUtil.getHeadImg(userInfo.userInfo) : "";
    }
    
    @Bindable
    public String getMemberJoinTime() {
        if (isChiefMember()) {
            return mCtx.getString(R.string.groupon_detail_chief_join_time,
                    DateUtils.mdsdf.format(new Date(userInfo.createTime)));
        }
        else if (userInfo != null) {
            return mCtx.getString(R.string.groupon_detail_join_time,
                    DateUtils.mdsdf.format(new Date(userInfo.createTime)));
        }
        else {
            return "";
        }
    }
}
