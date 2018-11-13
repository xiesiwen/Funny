package com.qingqing.base.nim.view;

import android.content.Context;

import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/18.
 */
public interface ChatRowProvider {
    
    ChatRowView getChatRowView(Context context, Message message);
    
    int getChatRowTypeCount();
    
    /**
     * 获取chatrow type，必须大于0, 从1开始有序排列
     */
    int getChatRowViewType(Message message);
}
