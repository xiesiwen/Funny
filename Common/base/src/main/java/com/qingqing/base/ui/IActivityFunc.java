package com.qingqing.base.ui;

import android.os.Message;

/**
 * Created by richie.wang on 2015/12/6.
 *
 * activity的通用接口类
 */
public interface IActivityFunc {
    /**
     * 处理完成Message，需要返回true
     * */
    boolean onHandlerUIMsg(Message msg);
}
