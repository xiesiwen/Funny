package com.qingqing.project.offline.order.v3;

import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.coursepackage.CoursePackageProto;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.constant.CourseConstant;
import com.qingqing.project.offline.order.v2.OrderFragListener;
import com.qingqing.project.offline.order.v2.OrderParams;
import com.qingqing.project.offline.order.v2.OrderSetCountFragment;
import com.qingqing.project.offline.order.v2.OrderTeacherInfoHolder;
import com.qingqing.qingqingbase.ui.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 统一内容包下单界面（第一步的界面）
 *
 * Created by tanwei on 2017/8/21.
 */

public class BaseFavourablePackOrderFragment extends BaseFragment {
    
    protected static final String TAG = "baseFavourableOrder";
    
    protected OrderParams mParams;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mParams = getArguments()
                .getParcelable(BaseParamKeys.PARAM_PARCELABLE_ORDER_CONFIRM);
        Logger.v(TAG, "order param: " + mParams);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_favourable_pack_order, container,
                false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setTitle(R.string.title_favourable_pack_order);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.title_favourable_pack_order);


        // 计算总课次
        final CoursePackageProto.CoursePackageUnits units = mParams.getPacketById();
        int count = 0;
        boolean online = mParams.getSiteType() == OrderCommonEnum.OrderSiteType.live_ost;
        if (units != null && units.packageUnits.length > 0) {

            // 忽略混合的优惠包，取第一个
            CoursePackageProto.CoursePackageUnit packetUnit = units.packageUnits[0];

            switch (packetUnit.chargeContactType) {
                case OrderCommonEnum.OrderSiteTypeContactType.online_ostct:
                    if (online) {
                        count += packetUnit.chargeCourseCount;
                    }
                    break;

                case OrderCommonEnum.OrderSiteTypeContactType.offline_ostct:
                    if (!online) {
                        count += packetUnit.chargeCourseCount;
                    }
                    break;
            }

            switch (packetUnit.freeContactType) {
                case OrderCommonEnum.OrderSiteTypeContactType.online_ostct:
                    if (online) {
                        count += packetUnit.freeCountCount;
                    }
                    break;

                case OrderCommonEnum.OrderSiteTypeContactType.offline_ostct:
                    if (!online) {
                        count += packetUnit.freeCountCount;
                    }
                    break;
            }
        }
        else {
            Logger.v(TAG, "favourable pack not exists " + mParams.getPacketId());
            ToastWrapper.show(R.string.text_favourable_pack_not_exists);
            getActivity().finish();
            return;
        }

        // 保存参数
        mParams.setCourseCount(count);
        // 6.2在线的优惠包改为2小时
        mParams.setCourseLength(OrderSetCountFragment.COURSE_LENGTH_DEFAULT);

        OrderTeacherInfoHolder holder = new OrderTeacherInfoHolder();
        holder.initView(view.findViewById(R.id.layout_teacher_info));
        holder.updateTeacherInfo(getActivity(), mParams);

        TextView tvSiteTypeInfo = (TextView) view
                .findViewById(R.id.fragment_base_fp_order_tv_site_type);
        StringBuilder siteTypeInfo = new StringBuilder(
                CourseConstant.getSiteType(mParams.getSiteType()));
        final String info = getString(online ? R.string.text_course_packet_rule_online
                : R.string.text_course_packet_rule_offline, count);
        siteTypeInfo.append(info);
        tvSiteTypeInfo.setText(siteTypeInfo);

        view.findViewById(R.id.fragment_base_fp_order_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 选时间
                        if (mFragListener instanceof OrderFragListener) {
                            ((OrderFragListener) mFragListener).done(mParams);
                        }

                    }
                });
    }

    @Override
    public boolean onBackPressed() {
        if (mParams.getPacketId() > 0) {
            mParams.setPacketId(0);
        }
        return super.onBackPressed();
    }
}
