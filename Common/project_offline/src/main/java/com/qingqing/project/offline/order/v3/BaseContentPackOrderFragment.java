package com.qingqing.project.offline.order.v3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qingqing.api.proto.v1.coursecontentpackage.CourseContentPackageProto;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.constant.CourseConstant;
import com.qingqing.project.offline.order.v2.OrderContentPackInfoHolder;
import com.qingqing.project.offline.order.v2.OrderFragListener;
import com.qingqing.project.offline.order.v2.OrderParams;
import com.qingqing.qingqingbase.ui.BaseFragment;

/**
 * 统一内容包下单界面（第一步的界面）
 *
 * Created by tanwei on 2017/8/21.
 */

public class BaseContentPackOrderFragment extends BaseFragment{

    protected static final String TAG = "baseContentOrder";

    protected OrderParams mParams;

    protected OrderContentPackInfoHolder contentInfoHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParams = getArguments()
                .getParcelable(BaseParamKeys.PARAM_PARCELABLE_ORDER_CONFIRM);
        Logger.v(TAG, "order param: " + mParams);

        final CourseContentPackageProto.CourseContentPackageForOrder contentPack = mParams
                .getSelectedContentPack();
        if (contentPack != null) {
            mParams.setCourseCount(contentPack.classCount);
            mParams.setCourseLength(contentPack.classHour / 10f);
        }
        else {
            Logger.v(TAG, "content pack not exists " + mParams.getContentPackId());
            ToastWrapper.show(R.string.text_content_pack_not_exists);
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_content_pack_order, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setTitle(R.string.title_content_pack_order);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.title_content_pack_order);

        contentInfoHolder = new OrderContentPackInfoHolder();
        contentInfoHolder
                .initView(view.findViewById(R.id.layout_content_pack_info_for_order));
        contentInfoHolder.updateContentPackInfo(getActivity(), mParams);

        TextView mTvSiteType = (TextView) view
                .findViewById(R.id.fragment_base_cp_order_tv_site_type);
        mTvSiteType.setText(CourseConstant.getSiteType(mParams.getSiteType()));

        view.findViewById(R.id.fragment_base_cp_order_btn)
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
