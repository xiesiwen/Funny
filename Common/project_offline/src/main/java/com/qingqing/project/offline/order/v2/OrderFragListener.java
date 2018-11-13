package com.qingqing.project.offline.order.v2;

import com.qingqing.qingqingbase.ui.BaseFragment;

/**
 * 下单流程fragment回调
 *
 * Created by tanwei on 2016/9/18.
 */
public interface OrderFragListener extends BaseFragment.FragListener{
    void done(OrderParams params);
    void back();
}
