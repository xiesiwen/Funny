package com.qingqing.base.interfaces;

import java.util.List;

/**
 * Created by huangming on 2016/9/9.
 */
public interface Delivery<T> {

    void post(T t);

    void postBatch(List<T> tList);

}
