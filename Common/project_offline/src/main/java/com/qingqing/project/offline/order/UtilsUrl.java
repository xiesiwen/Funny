package com.qingqing.project.offline.order;

import java.util.HashMap;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.UrlUtil;

import android.text.TextUtils;

/**
 * 获取 url 的方法 Created by lihui on 2016/4/20.
 */
public class UtilsUrl {
    /**
     * 老师主页的获取方法
     */
    public static class TeacherHomeUrlBuilder {
        /**
         * 新增聊天入口
         **/
        public static final String APP_ENTER_ASSISTANT_IM = "assistant_im";
        /**
         * 新增搜索入口
         **/
        public static final String APP_ENTER_ASSISTANT_SEARCH = "assistant_search";
        /**
         * 社交网络推荐
         **/
        public static final String APP_ENTER_ASSISTANT_RECOMMEND_TO_SHARE = "assistant_recommandToShare";
        /**
         * 给家长推荐老师
         **/
        public static final String APP_ENTER_ASSISTANT_RECOMMEND_FOR_STUDENT = "assistant_recommandForStudent";
        /**
         * 绑定家长生成订单
         **/
        public static final String APP_ENTER_ASSISTANT_ORDER = "assistant_order";
        /**
         * 我的老师入口
         **/
        public static final String APP_ENTER_ASSISTANT_TEACHER_LIST = "assistant_teacherlist";
        /**
         * 家长推荐老师列表入口
         **/
        public static final String APP_ENTER_ASSISTANT_ALREADY_RECOMMEND_FOR_STUDENT = "assistant_alreadyRecommandForStudent";
        
        // 老师的 qqUserId
        String qqUserId = "";
        // 邀请的学生 id
        String studentIdForActivity = "";
        // 下单的学生 id
        String studentIdForOrder = "";
        // 助教分享的助教 id
        String assistantId = "";
        // app 进入 h5 的入口
        String appEnter = "";
        // 有奖活动 id
        String actId = "";
        // 教研管理群聊的群组 id
        String chatGroupId = "";
        // 是否为创建寒假包的入口
        private boolean isWinterPackage = false;
        
        public TeacherHomeUrlBuilder setStudentIdForActivity(String studentId) {
            this.studentIdForActivity = studentId;
            return this;
        }
        
        public TeacherHomeUrlBuilder setStudentIdForOrder(String studentIdForOrder) {
            this.studentIdForOrder = studentIdForOrder;
            return this;
        }
        
        public TeacherHomeUrlBuilder setAssistantId(String assistantId) {
            this.assistantId = assistantId;
            return this;
        }
        
        public TeacherHomeUrlBuilder setQqUserId(String qqUserId) {
            this.qqUserId = qqUserId;
            return this;
        }
        
        public TeacherHomeUrlBuilder setAppEnter(String appEnter) {
            this.appEnter = appEnter;
            return this;
        }
        
        public TeacherHomeUrlBuilder setActivityId(String actId) {
            this.actId = actId;
            return this;
        }
        
        public TeacherHomeUrlBuilder setChatGroupId(String chatGroupId) {
            this.chatGroupId = chatGroupId;
            return this;
        }
        
        public TeacherHomeUrlBuilder setIsWinterPackage(boolean isWinterPackage) {
            this.isWinterPackage = isWinterPackage;
            return this;
        }
        
        public String build() {
            HashMap<String, String> param = new HashMap<>();
            if (!TextUtils.isEmpty(studentIdForActivity)) {
                param.put("sid", studentIdForActivity);
            }
            if (!TextUtils.isEmpty(studentIdForOrder)) {
                param.put("qingqing_student_id", studentIdForOrder);
            }
            param.put("assid", assistantId);
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                param.put("isTeacher", "1");
            }
            if (!TextUtils.isEmpty(appEnter)) {
                param.put("app_enter", appEnter);
            }
            if (!TextUtils.isEmpty(actId)) {
                param.put("actid", actId);
            }
            if (!TextUtils.isEmpty(chatGroupId)) {
                param.put("chat_group_id", chatGroupId);
            }
            if (isWinterPackage) {
                param.put("is_winter_package", "1");
            }
            
            param.put("hardware", "1");
            String url = CommonUrl.MAIN_H5_URL.url() + qqUserId + ".html";
            url = UrlUtil.addParamToUrl(url, param);
            Logger.i("teacherUrl = " + url);
            return url;
        }
    }
}
