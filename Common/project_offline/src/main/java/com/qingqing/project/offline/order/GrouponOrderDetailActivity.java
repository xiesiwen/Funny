package com.qingqing.project.offline.order;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.course.OrderCourse;
import com.qingqing.api.proto.v1.order.Order;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.share.ShareShow;
import com.qingqing.base.ui.BaseUIFrame;
import com.qingqing.qingqingbase.ui.BaseUIModel;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BaseUIAdapter;
import com.qingqing.base.view.tab.Tab;
import com.qingqing.base.view.tab.TabHost;
import com.qingqing.project.offline.BR;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.databinding.ActivityGrouponOrderDetailBinding;
import com.qingqing.project.offline.databinding.ItemGrouponMemberListBinding;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;

import java.util.List;

/**
 * Created by wangxiaxin on 2016/8/29.
 *
 * 朋友团详情
 *
 * 参数：<br>
 * {@link BaseParamKeys#PARAM_STRING_GROUP_ORDER_ID}
 * {@link BaseParamKeys#PARAM_BOOLEAN_AUTO_SHOW_SHARE}
 */
public class GrouponOrderDetailActivity extends BaseActionBarActivity
        implements Tab.TabListener {
    
    protected GrouponOrderDetailUIModel mUIModel;
    private ActivityGrouponOrderDetailBinding mUIBinding;
    private CourseTimeAdapter mCourseTimeAdapter;
    private GrouponMemberAdapter mMemberAdapter;
    private TabHost mTabHost;
    private ShareShow mShareShow;
    private boolean isAutoShowShare;// 是否自动弹出分享
    
    protected String mGrouponOrderId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI(R.layout.activity_groupon_order_detail, GrouponOrderDetailUIModel.class);
        mUIBinding = (ActivityGrouponOrderDetailBinding) getDataBinding();
        mUIModel = (GrouponOrderDetailUIModel) getUIModel();
        
        Bundle b = getIntent().getExtras();
        if (b == null) {
            b = savedInstanceState;
        }
        
        if (b == null) {
            finish();
            return;
        }
        
        mGrouponOrderId = b.getString(BaseParamKeys.PARAM_STRING_GROUP_ORDER_ID);
        if (TextUtils.isEmpty(mGrouponOrderId)) {
            finish();
            return;
        }
        
        isAutoShowShare = b.getBoolean(BaseParamKeys.PARAM_BOOLEAN_AUTO_SHOW_SHARE,
                false);
        mCourseTimeAdapter = new CourseTimeAdapter(this,
                mUIModel.getCourseTimeShowList());
        mUIBinding.amlvCourseTimeList.setAdapter(mCourseTimeAdapter);
        
        mMemberAdapter = new GrouponMemberAdapter(this, mUIModel.getGrouponMemberList());
        mUIBinding.amlvGrouponMembers.setAdapter(mMemberAdapter);
        
        mUIModel.setOrderId(mGrouponOrderId).reqDetail();
    }
    
    protected String getSelfNick() {
        return null;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
            UserBehaviorLogManager.INSTANCE()
                    .savePageLog(StatisticalDataConstants.LOG_PAGE_FRIENDS_DETAIL);
        }
    }
    
    @Override
    protected boolean onPropChanged(int i) {
        
        if (i == BR.courseTimeFolded) {
            mCourseTimeAdapter.notifyDataSetChanged();
        }
        else if (i == BR.grouponMemberList) {
            mMemberAdapter.notifyDataSetChanged();
            // 组装头像列表
            fillMemberHeadList();
            prepareShareShow();
        }
        else if (i == BR.leftSeconds) {
            // 更新倒计时UI展示
            updateCountDownUI(mUIModel.getLeftSeconds());
        }
        else if (i == BR.share) {
            if (mShareShow != null)
                mShareShow.show();
        }
        else if (i == BR.showRemarkInd) {
            showRemarkIndication();
        }
        
        return super.onPropChanged(i);
    }
    
    protected void showRemarkIndication() {
        
    }
    
    private void prepareShareShow() {
        if (mShareShow == null) {
            mShareShow = new ShareShow(this);
            
            TimeSlice slice = SelectTimeUtils
                    .parseToTimeSlice(mUIModel.getCourseTimeShowList().get(0).timeParam);
            String courseTimeString = SelectTimeUtils.getContentByTimeSlice(slice);
            
            mShareShow
                    .setShareLink(String.format(
                            CommonUrl.SHARE_GROUPON_DETAIL_URL.url().url(),
                            mUIModel.getGrouponOrderId(), mUIModel.getShareUserId()))
                    .setShareTitle(getString(R.string.groupon_share_title, getSelfNick(),
                            mUIModel.getTeacherNick()))
                    .setShareContent(getString(R.string.groupon_share_content,
                            mUIModel.getOrderCourseTitle(), mUIModel.getGroupTypeString(),
                            courseTimeString))
                    .setPageId(StatisticalDataConstants.LOG_PAGE_FRIENDS_DETAIL);
        }
        
        if (isAutoShowShare) {
            isAutoShowShare = false;
            if (mUIModel.isStudentClient() && mUIModel.isGrouponGoing()
                    && mUIModel.getLeftSeconds() > 0) {
                mShareShow.show();
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
    
    private void fillMemberHeadList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        if (mTabHost == null)
            mTabHost = mUIBinding.shtlMembers.getTabHost();
        
        mTabHost.removeAllTabs();
        for (int i = 0; i < mUIModel.getTotalMemberCount(); i++) {
            View tabView = inflater.inflate(R.layout.item_groupon_member_tablist, null);
            AsyncImageViewV2 aiv = (AsyncImageViewV2) tabView
                    .findViewById(R.id.aiv_member_head);
            View viewChief = tabView.findViewById(R.id.tv_member_chief);
            if (i < mUIModel.getGrouponMemberList().size()) {
                
                Order.GroupUserOrderInfo userInfo = mUIModel.getGrouponMemberList()
                        .get(i);
                
                aiv.setImageUrl(ImageUrlUtil.getHeadImg(userInfo.userInfo),
                        LogicConfig.getDefaultHeadIcon(userInfo.userInfo));
                if (userInfo.isLeader) {
                    viewChief.setVisibility(View.VISIBLE);
                }
                else {
                    viewChief.setVisibility(View.INVISIBLE);
                }
            }
            else {
                aiv.setImageUrl("res://xxx/" + R.drawable.icon_order_headportrait);
                viewChief.setVisibility(View.INVISIBLE);
            }
            mTabHost.addTab(mTabHost.newTab().setTabListener(this).setCustomView(tabView)
                    .setTag(i), false, false);
        }
    }
    
    private void updateCountDownUI(int leftSeconds) {
        
        int leftSecond = 0;
        int leftHour = 0;
        int leftMinute = 0;
        
        if (leftSeconds > 0) {
            leftSecond = leftSeconds % 60;
            leftHour = leftSeconds / 3600;
            leftMinute = (leftSeconds - (leftHour * 3600 + leftSecond)) / 60;
        }
        
        mUIBinding.tvCountDownHour.setText(String.format("%02d", leftHour));
        mUIBinding.tvCountDownMin.setText(String.format("%02d", leftMinute));
        mUIBinding.tvCountDownSec.setText(String.format("%02d", leftSecond));
        
        if (leftSeconds == 0) {
            mUIBinding.tvCountDownHour
                    .setBackgroundResource(R.drawable.shape_corner_rect_gray_solid);
            mUIBinding.tvCountDownMin
                    .setBackgroundResource(R.drawable.shape_corner_rect_gray_solid);
            mUIBinding.tvCountDownSec
                    .setBackgroundResource(R.drawable.shape_corner_rect_gray_solid);
        }
        else {
            mUIBinding.tvCountDownHour.setBackgroundResource(
                    R.drawable.shape_corner_rect_orange_light_solid);
            mUIBinding.tvCountDownMin.setBackgroundResource(
                    R.drawable.shape_corner_rect_orange_light_solid);
            mUIBinding.tvCountDownSec.setBackgroundResource(
                    R.drawable.shape_corner_rect_orange_light_solid);
        }
    }
    
    @Override
    public void onTabSelected(Tab tab) {
        
    }
    
    @Override
    public void onTabReselected(Tab tab) {
        
    }
    
    @Override
    public void onTabUnselected(Tab tab) {
        
    }
    
    class CourseTimeAdapter
            extends BaseUIAdapter<OrderCourse.OrderCourseInfoForOrderInfoDetail> {
        
        public CourseTimeAdapter(Context context,
                List<OrderCourse.OrderCourseInfoForOrderInfoDetail> list) {
            super(context, list);
        }
        
        @Override
        public int getItemLayoutId() {
            return R.layout.item_order_course_time_list;
        }
        
        @Override
        public Class<? extends BaseUIModel> getItemUIModelClass() {
            return OrderCourseTimeUIModel.class;
        }
        
        @Override
        public ViewHolder<OrderCourse.OrderCourseInfoForOrderInfoDetail> createViewHolder() {
            return new CourseTimeViewHolder();
        }
    }
    
    class CourseTimeViewHolder extends
            BaseUIAdapter.ViewHolder<OrderCourse.OrderCourseInfoForOrderInfoDetail> {
        
        @Override
        public void init(Context context, BaseUIFrame uiFrame) {}
        
        @Override
        public void update(Context context, BaseUIFrame uiFrame,
                OrderCourse.OrderCourseInfoForOrderInfoDetail data) {
            ((OrderCourseTimeUIModel) uiFrame.getUIModel()).setCourseTime(data);
        }
    }
    
    class GrouponMemberAdapter extends BaseUIAdapter<Order.GroupUserOrderInfo> {
        
        public GrouponMemberAdapter(Context context,
                List<Order.GroupUserOrderInfo> list) {
            super(context, list);
        }
        
        @Override
        public int getItemLayoutId() {
            return R.layout.item_groupon_member_list;
        }
        
        @Override
        public Class<? extends BaseUIModel> getItemUIModelClass() {
            return GrouponMemberUIModel.class;
        }
        
        @Override
        public ViewHolder<Order.GroupUserOrderInfo> createViewHolder() {
            return new GrouponMemberViewHolder();
        }
    }
    
    class GrouponMemberViewHolder
            extends BaseUIAdapter.ViewHolder<Order.GroupUserOrderInfo> {
        
        @Override
        public void init(Context context, BaseUIFrame uiFrame) {}
        
        @Override
        public void update(Context context, BaseUIFrame uiFrame,
                Order.GroupUserOrderInfo data) {
            
            GrouponMemberUIModel uiModel = ((GrouponMemberUIModel) uiFrame.getUIModel());
            ItemGrouponMemberListBinding uiBinding = (ItemGrouponMemberListBinding) uiFrame
                    .getDataBinding();
            
            uiModel.setMemberInfo(data);
            uiBinding.aivMemberHead
                    .setDefaultImageID(LogicConfig.getDefaultHeadIcon(data.userInfo));
            if (uiModel.isChiefMember()) {
                uiFrame.getDataBinding().getRoot()
                        .setBackgroundResource(R.drawable.white_frame);
            }
            else {
                uiFrame.getDataBinding().getRoot().setBackgroundResource(R.color.white);
            }
            uiBinding.ivUnpay.setVisibility((data.hasEffectTime && !data.hasPayTime)
                    ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
