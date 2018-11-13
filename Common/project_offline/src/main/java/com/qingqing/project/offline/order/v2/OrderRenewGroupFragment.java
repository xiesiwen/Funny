package com.qingqing.project.offline.order.v2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.order.RenewOrderHelper;

/**
 * 朋友团续课成员展示界面
 *
 * Created by tanwei on 2016/9/18.
 */
public class OrderRenewGroupFragment extends BaseFragment {

    private static final String TAG = "renewGroup";

    private OrderTeacherInfoHolder teacherViewHolder;

    private LinearLayout mMembersContainer;

    private OrderParams mParams;

    private ClickListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParams = getArguments()
                .getParcelable(BaseParamKeys.PARAM_PARCELABLE_ORDER_CONFIRM);
        Logger.v(TAG, "order param: " + mParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_renew_group, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        teacherViewHolder = new OrderTeacherInfoHolder();
        teacherViewHolder.initView(view.findViewById(R.id.layout_teacher_info));

        mMembersContainer = (LinearLayout) view.findViewById(R.id.fragment_order_renew_group_layout_members);

        mListener = new ClickListener();
        view.findViewById(R.id.fragment_order_renew_group_btn).setOnClickListener(mListener);
        view.findViewById(R.id.fragment_order_renew_group_tv_tip).setOnClickListener(mListener);

        // 家长端直接传续课信息，其他端需要获取续课信息(其他端暂时没有走不到这个界面)
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
            teacherViewHolder.updateTeacherInfo(getActivity(), mParams);
            fillMembers();

//            view.findViewById(R.id.fragment_order_renew_group_divider).setVisibility(View.VISIBLE);
//            view.findViewById(R.id.fragment_order_renew_group_tv_daifu).setVisibility(View.VISIBLE);
        }
        else {
            RenewOrderHelper.getInfoForRenew((BaseActionBarActivity) getActivity(),
                    mParams.getTeacherId(), mParams.getStudentId(),
                    new RenewOrderHelper.GetRenewInfoListener() {
                        @Override
                        public void done(OrderParams params) {
                            params.setCreateType(mParams.getCreateType());
                            mParams = params;
                            if (couldOperateUI()) {
                                teacherViewHolder.updateTeacherInfo(getActivity(),
                                        mParams);
                                fillMembers();
                            }
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UserBehaviorLogManager.INSTANCE()
                .savePageLog(StatisticalDataConstants.LOG_PAGE_FRIENDS_REORDER);
    }
    
    private void fillMembers() {
        int size = mParams.getStudentInfoList().size();
        for (int i = 0; i < size; i++) {
            View item = LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_group_member_info, null);

            if (i == size - 1) {
                item.findViewById(R.id.item_group_member_divider)
                        .setVisibility(View.GONE);
            }

            AsyncImageViewV2 avatar = (AsyncImageViewV2) item
                    .findViewById(R.id.item_group_member_avatar);
            TextView nick = (TextView) item.findViewById(R.id.item_group_member_nick);
            
//            avatar.setTag(i);
//            avatar.setOnClickListener(mListener);
            
            // 设置成员信息
            UserProto.SimpleUserInfoV2 info = mParams.getStudentInfoList().get(i);
            avatar.setImageUrl(ImageUrlUtil.getHeadImg(info),
                    LogicConfig.getDefaultHeadIcon(info));
            nick.setText(info.nick);
            
            mMembersContainer.addView(item, i + 1);
        }
    }

    class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            if (id == R.id.fragment_order_renew_group_btn) {
                if(mFragListener instanceof OrderRenewGroupFragListener){
                    ((OrderRenewGroupFragListener)mFragListener).toRenewGroupOrder(mParams);
                }
                UserBehaviorLogManager.INSTANCE().saveClickLog(
                        StatisticalDataConstants.LOG_PAGE_FRIENDS_REORDER,
                        StatisticalDataConstants.CLICK_FRIENDS_REORDER_RECORDER);
            }
            else if (id == R.id.fragment_order_renew_group_tv_tip) {
                if(mFragListener instanceof OrderRenewGroupFragListener){
                    ((OrderRenewGroupFragListener)mFragListener).toNewGroupOrder(mParams);
                }
                UserBehaviorLogManager.INSTANCE().saveClickLog(
                        StatisticalDataConstants.LOG_PAGE_FRIENDS_REORDER,
                        StatisticalDataConstants.CLICK_FRIENDS_REORDER_CHANGE_MEMBER);
//            }else{
//                int position = (int) v.getTag();
//                // TODO 响应点击头像
            }
        }
    }

    public interface OrderRenewGroupFragListener extends FragListener{
        void toRenewGroupOrder(OrderParams params);
        void toNewGroupOrder(OrderParams params);
    }
}
