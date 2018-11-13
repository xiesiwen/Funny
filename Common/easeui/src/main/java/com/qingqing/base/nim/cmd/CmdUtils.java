package com.qingqing.base.nim.cmd;

import android.content.Context;
import android.text.TextUtils;

import com.easemob.easeui.R;
import com.qingqing.base.nim.domain.ChatManager;
import com.qingqing.base.nim.domain.CmdMessageBody;

/**
 * Created by huangming on 2016/8/27.
 */
public class CmdUtils {
    
    private CmdUtils() {}
    
    public static String getText(Context context, CmdMessageBody body) {
        int cmdType = body.getType();
        if (cmdType == CmdType.CMD_TYPE_TEXT) {
            return TextCmd.getText(body);
        }
        else if (isLectureCmd(cmdType)) {
            return getLectureText(context, body);
        }
        else {
            return "";
        }
    }
    
    public static String getLectureText(Context context, CmdMessageBody body) {
        int cmdType = body.getType();
        switch (cmdType) {
            case CmdType.CMD_TYPE_LECTURE_DESTROYED:
                return context.getString(R.string.tips_lecture_room_end);
            case CmdType.CMD_TYPE_LECTURE_STOP_TALK:
                String userId = LectureCmd.getToStopUserId(body);
                String userNick = LectureCmd.getToStopUserNick(body);
                boolean allowTalk = LectureCmd.isAllow(body);
                if (TextUtils.isEmpty(userId)) {
                    return context
                            .getString(!allowTalk ? R.string.tips_manager_forbid_all_speak
                                    : R.string.tips_manager_allow_all_speak);
                }
                if (userId.equals(ChatManager.getInstance().getCurrentUserId())) {
                    return context.getString(
                            !allowTalk ? R.string.tips_you_speak_forbidden_by_manager
                                    : R.string.tips_you_speak_allow_by_manager);
                }
                else {
                    return context
                            .getString(
                                    !allowTalk ? R.string.tips_speak_forbidden_by_manager
                                            : R.string.tips_speak_allow_by_manager,
                                    userNick);
                }
            case CmdType.CMD_TYPE_LECTURE_PLAY_PPT:
                int pptIndex = LectureCmd.getPptIndex(body);
                return context.getString(R.string.tips_expert_play_ppt_index,
                        String.valueOf(pptIndex + 1));
            default:
                return "";
        }
    }
    
    public static boolean isLectureCmd(int cmdType) {
        return cmdType == CmdType.CMD_TYPE_LECTURE_STOP_TALK
                || cmdType == CmdType.CMD_TYPE_LECTURE_DESTROYED
                || cmdType == CmdType.CMD_TYPE_LECTURE_PLAY_PPT
                || cmdType == CmdType.CMD_TYPE_LECTURE_CHANGE_ROOM
                || cmdType == CmdType.CMD_TYPE_LECTURE_CHANGE_PPT;
    }
    
    public static boolean isLectureNeedShowCmd(int cmdType) {
        return cmdType == CmdType.CMD_TYPE_LECTURE_STOP_TALK
                || cmdType == CmdType.CMD_TYPE_LECTURE_DESTROYED
                || cmdType == CmdType.CMD_TYPE_LECTURE_PLAY_PPT;
    }
    
    public static boolean isCmdShowAsText(int cmdType) {
        return cmdType == CmdType.CMD_TYPE_TEXT
                || cmdType == CmdType.CMD_TYPE_LECTURE_STOP_TALK
                || cmdType == CmdType.CMD_TYPE_LECTURE_DESTROYED
                || cmdType == CmdType.CMD_TYPE_LECTURE_PLAY_PPT;
    }
    
}
