package com.qingqing.base.im;

/**
 * Created by huangming on 2015/12/23.
 */
public interface DataRequestListener {
    public void onRequest();

    public void onRequest(Object... parameters);
}
