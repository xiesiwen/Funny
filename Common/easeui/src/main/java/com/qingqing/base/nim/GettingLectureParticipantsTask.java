package com.qingqing.base.nim;

import com.qingqing.api.commentsvc.proto.v1.CommentCountingProto;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.interfaces.OnNext;
import com.qingqing.base.task.AbstractCancelableTask;
import com.qingqing.base.task.GettingUserViewsTask;
import com.qingqing.base.utils.ExecUtil;

/**
 * Created by huangming on 2016/8/18.
 *
 * 循环获取讲堂参与人数
 */
public abstract class GettingLectureParticipantsTask extends AbstractCancelableTask
        implements OnNext<Integer> , Runnable {
    
    private final String lectureId;
    private final GettingUserViewsTask gettingUserViewsTask;
    
    public GettingLectureParticipantsTask(String lectureId) {
        this.lectureId = lectureId;
        gettingUserViewsTask = new GettingUserViewsTask(getLectureId(),
                CommentCountingProto.CountRefType.lecture_count_type, true) {
            
            @Override
            public void onNext(Integer integer) {
                if(isCanceled()) {
                    return;
                }
                GettingLectureParticipantsTask.this.onNext(integer);
            }
            
            @Override
            public void onCompleted() {
                if(isCanceled()) {
                    return;
                }
                processDelayed();
            }
        };
    }
    
    public String getLectureId() {
        return lectureId;
    }
    
    private GettingUserViewsTask getGettingUserViewsTask() {
        return gettingUserViewsTask;
    }
    
    @Override
    protected void onExecute() {
        processDelayed();
    }
    
    @Override
    public void run() {
        if(isCanceled()) {
            return;
        }
        getGettingUserViewsTask().execute();
    }
    
    private void processDelayed() {
        ExecUtil.executeUIDelayed(this,
                DefaultDataCache.INSTANCE().getLectureAttendanceSyncInterval() * 1000);
    }
    
}
