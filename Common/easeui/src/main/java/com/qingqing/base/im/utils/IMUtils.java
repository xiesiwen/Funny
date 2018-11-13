package com.qingqing.base.im.utils;

import android.text.TextUtils;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.model.EaseBigImage;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.push.proto.v1.PushMsg;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.ExtFieldParser;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.domain.ExtField;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.ExecUtil;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2015/12/27.
 */
public class IMUtils {
    
    public static String getName(ContactInfo info) {
        return getName(info, "");
        
    }
    
    public static String getName(ContactInfo info, String defaultName) {
        if (info != null) {
            if (!TextUtils.isEmpty(info.getAlias())) {
                return info.getAlias();
            }
            if (!TextUtils.isEmpty(info.getNick())) {
                return info.getNick();
            }
            if (TextUtils.isEmpty(defaultName)) {
                return info.getUsername();
            }
        }
        return defaultName;
    }
    
    /**
     * @param prioritizedName,
     *            优先显示
     */
    public static String getName(String prioritizedName, ContactInfo info) {
        if (!TextUtils.isEmpty(prioritizedName)) {
            return prioritizedName;
        }
        return getName(info);
    }
    
    public static String getName(String userName) {
        if (TextUtils.isEmpty(userName)) {
            return "";
        }
        ContactInfo info = ChatManager.getInstance().getContactModel()
                .getContactInfo(userName);
        return getName(info, userName);
    }
    
    public static String getName(String userName, String defaultName) {
        if (TextUtils.isEmpty(userName)) {
            return "";
        }
        ContactInfo info = ChatManager.getInstance().getContactModel()
                .getContactInfo(userName);
        return getName(info, defaultName);
    }
    
    public static String getAvatar(String userName) {
        return getAvatar(
                ChatManager.getInstance().getContactModel().getContactInfo(userName));
    }
    
    public static String getAvatar(ContactInfo info) {
        return info != null ? info.getAvatar() : "";
    }
    
    public static void setContactAvatar(AsyncImageViewV2 headImg, ContactInfo info) {
        setContactAvatar(headImg, info, getDefaultHeadIcon(info));
    }
    
    public static void setContactAvatar(AsyncImageViewV2 headImg, ContactInfo info,
            int defaultImage) {
        setImg(headImg, ImageUrlUtil.getHeadImg(info != null ? info.getAvatar() : ""), 0,
                0, defaultImage);
    }
    
    public static void setAvatar(AsyncImageViewV2 headImg, String headImgUrl) {
        setImg(headImg, ImageUrlUtil.getHeadImg(headImgUrl), 0, 0,
                R.drawable.user_pic_boy);
    }
    
    public static void setAvatar(AsyncImageViewV2 headImg, String headImgUrl,
            int defaultIcon) {
        setImg(headImg, ImageUrlUtil.getHeadImg(headImgUrl), 0, 0, defaultIcon);
    }
    
    public void setChatImg(AsyncImageViewV2 headImg, String url) {
        setImg(headImg, url, 0, R.drawable.icon_chat04, R.drawable.icon_chat04);
    }
    
    static void setImg(AsyncImageViewV2 headImg, String url, int loadingImage,
            int failedImage, int defaultImage) {
        headImg.setImageUrl(url, loadingImage, failedImage, defaultImage);
    }
    
    public static int getSexType(String userName) {
        ContactInfo info = ChatManager.getInstance().getContactModel()
                .getContactInfo(userName);
        return info != null && info.getSex() == 0 ? UserProto.SexType.female
                : UserProto.SexType.male;
    }
    
    public static int getDefaultHeadIcon(ContactInfo info) {
        return info != null && info.getSex() == 0 ? R.drawable.user_pic_girl
                : R.drawable.user_pic_boy;
    }
    
    public static int getDefaultHeadIcon(String userName) {
        return getDefaultHeadIcon(
                ChatManager.getInstance().getContactModel().getContactInfo(userName));
    }
    
    /**
     * 检查是否需要上报，需要则上传im消息接收
     *
     * @param msg
     *            EMMessage
     */
    public static void uploadIMMsgReceived(final EMMessage msg) {
        ExecUtil.execute(new Runnable() {
            @Override
            public void run() {
                ExtField extField = ExtFieldParser.getExt(msg);
                if (extField.needReport && !TextUtils.isEmpty(extField.qingqingMsgId)) {
                    uploadIMMsgAction(
                            PushMsg.HuanxinMsgReportRequest.HuanxinMsgReportStatus.readed_huanxin_msg_report_status,
                            extField.qingqingMsgId);
                }
            }
        });
    }
    
    /**
     * 检查是否需要上报，需要则上传im消息接收
     *
     * @param msgList
     *            EMMessage list
     */
    public static void uploadIMMsgReceived(final List<EMMessage> msgList) {
        
        ExecUtil.execute(new Runnable() {
            @Override
            public void run() {
                List<String> idList = new ArrayList<>();
                for (EMMessage msg : msgList) {
                    ExtField extField = ExtFieldParser.getExt(msg);
                    if (extField.needReport
                            && !TextUtils.isEmpty(extField.qingqingMsgId)) {
                        idList.add(extField.qingqingMsgId);
                        uploadIMMsgAction(
                                PushMsg.HuanxinMsgReportRequest.HuanxinMsgReportStatus.readed_huanxin_msg_report_status,
                                extField.qingqingMsgId);
                    }
                }
                
                if (idList.size() > 0) {
                    uploadIMMsgAction(
                            PushMsg.HuanxinMsgReportRequest.HuanxinMsgReportStatus.received_huanxin_msg_report_status,
                            idList);
                }
            }
        });
        
    }
    
    /**
     * 批量检查是否需要上报，需要则上传im消息已读
     * 
     * @param msg
     *            EMMessage
     */
    public static void uploadIMMsgRead(final EMMessage msg) {
        ExecUtil.execute(new Runnable() {
            @Override
            public void run() {
                ExtField extField = ExtFieldParser.getExt(msg);
                if (extField.needReport && !TextUtils.isEmpty(extField.qingqingMsgId)) {
                    uploadIMMsgAction(
                            PushMsg.HuanxinMsgReportRequest.HuanxinMsgReportStatus.readed_huanxin_msg_report_status,
                            extField.qingqingMsgId);
                }
            }
        });
    }
    
    /**
     * 上报环信消息状态
     *
     * @param action
     *            {@link com.qingqing.api.push.proto.v1.PushMsg.HuanxinMsgReportRequest.HuanxinMsgReportStatus
     *            HuanxinMsgReportStatus}
     * @param msgId
     *            qingqing_msg_id
     */
    public static void uploadIMMsgAction(int action, String msgId) {
        PushMsg.HuanxinMsgReportRequest request = new PushMsg.HuanxinMsgReportRequest();
        try {
            request.messageIds = new long[] { Long.parseLong(msgId) };
        } catch (NumberFormatException ignore) {}
        uploadIMMsgAction(action, request);
    }
    
    /**
     * 上报环信消息状态
     *
     * @param action
     *            {@link com.qingqing.api.push.proto.v1.PushMsg.HuanxinMsgReportRequest.HuanxinMsgReportStatus
     *            HuanxinMsgReportStatus}
     * @param idList
     *            qingqing_msg_id list
     */
    public static void uploadIMMsgAction(int action, List<String> idList) {
        PushMsg.HuanxinMsgReportRequest request = new PushMsg.HuanxinMsgReportRequest();
        request.messageIds = new long[idList.size()];
        int index = 0;
        for (String id : idList) {
            if (id != null) {
                try {
                    request.messageIds[index++] = Long.parseLong(id);
                } catch (NumberFormatException ignore) {}
            }
        }
        uploadIMMsgAction(action, request);
    }
    
    private static void uploadIMMsgAction(final int action,
            PushMsg.HuanxinMsgReportRequest request) {
        
        if (BaseData.isUserIDValid()) {
            request.qingqingUserId = BaseData.qingqingUserId();
        }
        request.actionType = action;
        request.hasActionType = true;
        
        new ProtoReq(CommonUrl.HUANXIN_MSG_COLLECT_URL.url()).setSendMsg(request)
                .setListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        Logger.v("uploadIMMsgAction suc " + action);
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                            int errorCode, Object result) {
                        Logger.v("uploadIMMsgAction error " + action);
                    }
                }.setSilentWhenError()).reqSilent();
    }
    
    public static String getImageUri(EaseBigImage easeBigImage) {
        if (easeBigImage.uri != null && new File(easeBigImage.uri.getPath()).exists()) {
            return easeBigImage.uri.toString();
        }
        else if (!TextUtils.isEmpty(easeBigImage.remotePath)) {
            return easeBigImage.remotePath;
        }
        else {
            return "";
        }
    }
}
