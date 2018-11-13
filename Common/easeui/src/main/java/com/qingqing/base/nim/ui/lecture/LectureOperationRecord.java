package com.qingqing.base.nim.ui.lecture;

import com.qingqing.base.data.BaseData;

/**
 * 学堂操作记录，缓存用户的操作
 *
 * Created by tanwei on 2016/5/30.
 */
public class LectureOperationRecord {
    
    public static final String USER_UNKNOWN = "unknown";// 标记未登录用户的id
    
    public String userId;// 用户id
    
    public String lectureId;// 学堂id
    
    public long eventId;// 日历提醒事件id
    
    public boolean followed;// 是否设置提醒（服务器）
    
    public int pageIndex;// 聊天历史最后浏览的下标页(所有)
    
    public int positionInPage;// 聊天历史最后浏览的页中第一条可见消息的position(所有)
    
    public int filterPageIndex;// 聊天历史最后浏览的下标页（筛选）
    
    public int positionInFilterPage;// 聊天历史最后浏览的页中第一条可见消息的position(筛选)
    
    public boolean isFilterExpert;// 是否只看专家
    
    public LectureOperationRecord() {}
    
    public LectureOperationRecord(String lectureId) {
        if (BaseData.isUserIDValid()) {
            userId = BaseData.qingqingUserId();
        }
        else {
            userId = USER_UNKNOWN;
        }
        this.lectureId = lectureId;
    }
}
