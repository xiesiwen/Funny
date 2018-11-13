package com.qingqing.base.im;

/**
 * Created by huangming on 2015/12/23.
 * 数据同步listener
 */
public interface DataSyncListener {
    /**
     * 同步完毕
     * @param success true：成功同步到数据，false失败
     */
    public void onSyncComplete(boolean success);

}
