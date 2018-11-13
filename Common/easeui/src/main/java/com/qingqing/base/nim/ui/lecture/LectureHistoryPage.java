package com.qingqing.base.nim.ui.lecture;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.domain.ExtField;
import com.qingqing.base.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * 学堂历史单页数据结构
 *
 * Created by tanwei on 2016/5/31.
 */
public class LectureHistoryPage {
    
    public static final int HISTORY_COUNT_PER_PAGE = 20;
    
    private static Constructor<ImageMessageBody> imageBodyConstructor;
    private static Constructor<VoiceMessageBody> voiceBodyConstructor;
    
    private int pageIndex;
    private int mVoiceCount;
    
    private ArrayList<EMMessage> mHistoryList;
    
    // 5.2新增试听，试听不受限于单页数量限制
    private boolean isPageCountLimit;
    
    public LectureHistoryPage(int index, ImProto.ChatRoomMsgHistoryItem[] list) {
        
        mHistoryList = new ArrayList<>();
        pageIndex = index;
        isPageCountLimit = true;
        if (list != null) {
            convertData(list);
        }
        else {
            Logger.w("history page empty");
        }
    }
    
    /** 5.2新增试听，试听不受限于单页数量限制 */
    public LectureHistoryPage(ImProto.ChatRoomMsgHistoryItem[] list) {
        
        mHistoryList = new ArrayList<>();
        isPageCountLimit = false;
        if (list != null) {
            convertData(list);
        }
        else {
            Logger.w("history page empty");
        }
    }
    
    private void createImageBodyConstructor() {
        try {
            imageBodyConstructor = ImageMessageBody.class
                    .getDeclaredConstructor(String.class, String.class, String.class);
            imageBodyConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    private void createVoiceBodyConstructor() {
        try {
            voiceBodyConstructor = VoiceMessageBody.class
                    .getDeclaredConstructor(String.class, String.class, int.class);
            voiceBodyConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    /** 获取当前页下标 */
    public int getPageIndex() {
        return pageIndex;
    }
    
    /** 获取当前页转换完成的消息结构数据 */
    public ArrayList<EMMessage> getHistoryList() {
        return mHistoryList;
    }
    
    public boolean hasVoice() {
        return mVoiceCount > 0;
    }
    
    // ChatRoomMsgHistoryItem转换成EMMessage
    private void convertData(ImProto.ChatRoomMsgHistoryItem[] list) {
        int size = list.length;
        
        if (size > 0) {
            
            if (!isPageCountLimit && size > HISTORY_COUNT_PER_PAGE) {
                Logger.w("history item in a page : " + size);
            }
            
            for (int i = 0; i < size; i++) {
                ImProto.ChatRoomMsgHistoryItem item = list[i];
                EMMessage msg = convertItemToMEssage(item);
                if (msg != null) {
                    mHistoryList.add(msg);
                }
            }
            
        }
        else {
            Logger.w("history item 0");
        }
    }
    
    private EMMessage convertItemToMEssage(ImProto.ChatRoomMsgHistoryItem item) {
        EMMessage msg = null;
        String userId = item.fromUser != null ? item.fromUser.qingqingUserId : null;// 环信测试发的消息fromUser和extData为空
        boolean send = BaseData.qingqingUserId().equals(userId);
        switch (item.msgType) {
            case ImProto.ChatMsgHistoryType.audio_chat_msg_history_type:
                ++mVoiceCount;
                if (send) {
                    msg = EMMessage.createSendMessage(EMMessage.Type.VOICE);
                }
                else {
                    msg = EMMessage.createReceiveMessage(EMMessage.Type.VOICE);
                    msg.setFrom(userId);
                }
                if (voiceBodyConstructor == null) {
                    createVoiceBodyConstructor();
                }
                try {
                    VoiceMessageBody messageBody = voiceBodyConstructor.newInstance(null,
                            item.encodedMediaId, item.mediaTimeLength);
                    msg.addBody(messageBody);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                break;
            
            case ImProto.ChatMsgHistoryType.txt_chat_msg_history_type:
                if (send) {
                    msg = EMMessage.createTxtSendMessage(item.content, userId);
                }
                else {
                    msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                    msg.setFrom(userId);
                    msg.addBody(new TextMessageBody(item.content));
                }
                break;
            
            case ImProto.ChatMsgHistoryType.img_chat_msg_history_type:
                if (send) {
                    msg = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
                }
                else {
                    msg = EMMessage.createReceiveMessage(EMMessage.Type.IMAGE);
                }
                
                msg.setFrom(userId);
                if (imageBodyConstructor == null) {
                    createImageBodyConstructor();
                }
                try {
                    ImageMessageBody messageBody = imageBodyConstructor.newInstance(null,
                            item.imageUrl, null);
                    msg.addBody(messageBody);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                break;
            
            case ImProto.ChatMsgHistoryType.cmd_chat_msg_history_type:
                
                if (send) {
                    msg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                    msg.setFrom(userId);
                }
                else {
                    msg = EMMessage.createReceiveMessage(EMMessage.Type.CMD);
                    msg.setFrom(userId);
                }
                
                break;
            
            case ImProto.ChatMsgHistoryType.video_chat_msg_history_type:
                break;
            
            case ImProto.ChatMsgHistoryType.loc_chat_msg_history_type:
                break;
            
            default:
                Logger.v("unknown type");
                return null;
        }
        
        if (msg != null) {
            msg.setAcked(true);
            msg.setDelivered(true);
            msg.setMsgId(item.huanxinMsgId);
            msg.setChatType(EMMessage.ChatType.ChatRoom);
            msg.setMsgTime(item.sendTime);
            msg.setUnread(false);// 设置为已读
            msg.status = EMMessage.Status.SUCCESS;// 设置状态为成功（发送或者接收）
            msg.setListened(true);// 设置语音消息为已听
            // Logger.v("time：" + DateUtil.ymdhmsSdf.format(new
            // Date(item.getSendTime())));
            
            JSONObject json = null;
            
            try {
                json = new JSONObject(item.extData);
            } catch (JSONException e) {
                // e.printStackTrace();
            }
            
            if (json != null) {
                
                if (msg.getType() == EMMessage.Type.CMD) {
                    String action = json.optString("action");
                    msg.setAttribute("action", action);
                    CmdMessageBody body = new CmdMessageBody(action);
                    msg.addBody(body);
                    
                    JSONObject cmdJson = null;
                    try {
                        cmdJson = new JSONObject(action);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // 过滤不需要显示的cmd消息
                    if (cmdJson != null && !CmdMsg
                            .isLectureNeedShowCmdMsg(cmdJson.optInt(CmdMsg.TYPE))) {
                        return null;
                    }
                }
                msg.setAttribute(ExtField.Attr.FROM_USER_INFO,
                        json.optString(ExtField.Attr.FROM_USER_INFO));
            }
        }
        
        return msg;
    }
}
