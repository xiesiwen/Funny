package com.qingqing.base.nim.cmd;

import com.qingqing.base.nim.domain.CmdMessageBody;
import com.qingqing.base.utils.ImageUrlUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/8/27.
 */
public class LectureCmd {
    
    public static final String TO_STOP_USER_ID = "to_stop_qingqing_user_id";
    public static final String TO_STOP_USER_NICK = "to_stop_user_nick";
    public static final String TO_STOP_USER_TYPE = "to_stop_user_type";
    public static final String IS_ALLOW = "is_allow";
    public static final String PPT_IMG_URL = "image_url";
    public static final String PPT_IMG_INDEX = "image_index";
    public static final String SEND_TIME = "send_time";
    public static final String CHAT_ROOM_ID = "chatroom_id";
    public static final String PPT_IMG_URLS = "img_urls";
    
    public static String getToStopUserId(CmdMessageBody body) {
        return body.getString(TO_STOP_USER_ID);
    }
    
    public static String getToStopUserNick(CmdMessageBody body) {
        return body.getString(TO_STOP_USER_NICK);
    }
    
    public static int getToStopUserType(CmdMessageBody body) {
        return body.getInt(TO_STOP_USER_TYPE);
    }
    
    public static boolean isAllow(CmdMessageBody body) {
        return body.getBoolean(IS_ALLOW);
    }
    
    public static String getPptImageUrl(CmdMessageBody body) {
        return body.getString(PPT_IMG_URL);
    }
    
    public static int getPptIndex(CmdMessageBody body) {
        return body.getInt(PPT_IMG_INDEX);
    }
    
    public static long getSendTime(CmdMessageBody body) {
        return body.getLong(SEND_TIME);
    }
    
    public static String getChatRoomId(CmdMessageBody body) {
        return body.getString(CHAT_ROOM_ID);
    }
    
    public static List<String> getImgUrls(CmdMessageBody body) {
        List<String> imgUrls = new ArrayList<>();
        JSONArray jsonArray = body.getJSONArray(PPT_IMG_URLS);
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                imgUrls.add(ImageUrlUtil.getOriginImg(jsonArray.optString(i)));
            }
        }
        return imgUrls;
    }
    
}
