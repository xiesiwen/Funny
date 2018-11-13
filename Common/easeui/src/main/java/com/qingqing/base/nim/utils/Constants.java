package com.qingqing.base.nim.utils;

import com.qingqing.base.im.Constant;

/**
 * Created by huangming on 2016/8/18.
 */
public class Constants {
    
    private Constants() {}
    
    public static final int REQUEST_CODE_SETTINGS = 10000;
    public static final int RESULT_CODE_CHANGE_ROLE = 20001;
    public static final int RESULT_CODE_FINISH_LECTURE = 20002;
    
    public static final int EXTEND_ITEM_CAMERA = 1;
    public static final int EXTEND_ITEM_PICTURE = 2;
    public static final int EXTEND_ITEM_SETTINGS = 3;
    public static final int EXTEND_ITEM_LOCATION = 4;
    
    public static final String EXTRA_CHAT_TYPE = Constant.EXTRA_CHAT_TYPE;
    public static final String EXTRA_CONVERSATION_ID = Constant.EXTRA_USER_ID;
    public static final String EXTRA_LECTURE_ID = "lecture_id";
    public static final String EXTRA_LECTURE_INFO = "lecture_info";


    public static final String EXTRA_CHANGE_ROLE_TYPE = "change_role_type";
    public static final String EXTRA_CHANGE_ROLE_USER_INFO = "change_role_user_info";
    
}
