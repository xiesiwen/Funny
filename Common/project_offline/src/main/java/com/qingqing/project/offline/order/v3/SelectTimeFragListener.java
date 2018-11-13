package com.qingqing.project.offline.order.v3;

import com.qingqing.base.ui.AbstractFragment;

/**
 * 选时间的流程监听
 *
 * Created by tanwei on 2017/8/8.
 */

public interface SelectTimeFragListener extends AbstractFragment.FragListener {
    void done(SelectTimeParamsV3 params);
}
