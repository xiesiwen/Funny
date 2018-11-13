package com.qingqing.base.interfaces;

/**
 * Created by huangming on 2016/8/18.
 */
public interface Cancelable {

    boolean isCanceled();

    void cancel();
}
