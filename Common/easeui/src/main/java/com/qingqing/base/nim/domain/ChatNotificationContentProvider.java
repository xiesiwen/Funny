package com.qingqing.base.nim.domain;

import android.content.Context;
import android.content.Intent;

/**
 * Created by huangming on 2016/8/31.
 */
public interface ChatNotificationContentProvider {
    
    /**
     * 修改标题,这里使用默认
     */
    String getTitle(Context context, Message message);
    
    /**
     * 设置小图标，这里为默认
     */
    int getSmallIcon(Context context, Message message);
    
    /**
     * 设置状态栏的消息提示，可以根据message的类型做相应提示
     * 设置发送notification时状态栏提示新消息的内容(比如Xxx发来了一条图片消息)
     */
    String getTickerText(Context context, Message message);
    
    /**
     * 设置notification持续显示的新消息提示(比如2个联系人发来了5条消息)
     */
    String getContentText(Context context, Message message, int userCount, int messageCount);
    
    /**
     * 设置点击通知栏跳转事件
     */
    Intent getContentIntent(Context context, Message message);


    boolean isNotifyAllowed(Context context, Message message);

}
