package com.qingqing.base.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.easeui.R;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.bean.MessageInfo;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.badge.StrokeBadgeView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 消息列表的适配器
 *
 * <p>
 * 5.0 修改by tanwei
 * </p>
 *
 * Created by chenwei_sh on 2015/12/18.
 */
public class MessageAdapter extends BaseAdapter<MessageInfo> {

    public MessageAdapter(Context context, List<MessageInfo> list) {
        super(context, list);
    }
    
    @Override
    public View createView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_message_history,
                parent, false);
    }
    
    @Override
    public ViewHolder<MessageInfo> createViewHolder() {
        
        return new MessageHolder();
    }

    private class MessageHolder extends BaseAdapter.ViewHolder<MessageInfo> {
        private TextView mNick; // 老师、家长、助教的昵称
        private TextView mTime; // 普通消息时间
        private StrokeBadgeView mUnreadMessageCount; // 会话未读消息数
        private StrokeBadgeView mUnreadSysMessageCount; // 系统消息未读消息数
        private TextView mLastMsg; // 最后一条消息
        private AsyncImageViewV2 mIcon; // 头像
        private LinearLayout llNormalNews;// 普通消息
        private TextView tvSysMsg; // 系统消息
        private ImageView ivStatus; // 发送状态
        
        @Override
        public void init(Context context, View convertView) {
            Logger.i("init_AD");
            if (context != null && convertView != null) {
                mNick = (TextView) convertView.findViewById(R.id.tv_nick);
                mUnreadMessageCount = (StrokeBadgeView) convertView
                        .findViewById(R.id.tv_unread_numbers);
                mUnreadSysMessageCount = (StrokeBadgeView) convertView
                        .findViewById(R.id.tv_unread_sys_msg_numbers);
                mLastMsg = (TextView) convertView.findViewById(R.id.tv_last_message);
                mTime = (TextView) convertView.findViewById(R.id.tv_time);
                mIcon = (AsyncImageViewV2) convertView.findViewById(R.id.aiv_icon);
                llNormalNews = (LinearLayout) convertView
                        .findViewById(R.id.ll_normal_msg);
                tvSysMsg = (TextView) convertView.findViewById(R.id.tv_sys_msg);
                ivStatus = (ImageView) convertView.findViewById(R.id.iv_status);
                
                if (mUnreadMessageCount != null) {
                    mUnreadMessageCount.setStrokeColor(
                            BaseApplication.getCtx().getResources().getColor(
                                    com.qingqing.qingqingbase.R.color.badge_red));
                    mUnreadMessageCount.setFillColor(
                            BaseApplication.getCtx().getResources().getColor(
                                    com.qingqing.qingqingbase.R.color.badge_red));
                }
            }
        }
        
        @Override
        public void update(Context context, MessageInfo messageInfo) {
            mUnreadMessageCount.setVisibility(View.INVISIBLE);
            mUnreadSysMessageCount.setVisibility(View.INVISIBLE);
            if (messageInfo != null) {
                IMUtils.setAvatar(mIcon, messageInfo.getUserIconUrl(),
                        messageInfo.getDefaultIcon());
                if (messageInfo.getEMMsgType() == null) {
                    
                    switch (messageInfo.getCustomType()) {
                        // 大事记
                        case MessageInfo.CUSTOM_MSG_MEMORABILIA:
                        // 小贴士
                        case MessageInfo.CUSTOM_MSG_TIPS:

                            showSystemMsg(false);
                            mNick.setText(messageInfo.getUserName());
                            ivStatus.setVisibility(View.GONE);
                            mTime.setText(getLastMsgTime(messageInfo.getLastMsgTime()));
                            mLastMsg.setText(messageInfo.getLastMsgContent());
                            mUnreadSysMessageCount
                                    .setVisibility(messageInfo.getUnreadCount() > 0 ? View.VISIBLE
                                            : View.GONE);
                            break;
                        
                        // 生源宝助手
                        case MessageInfo.CUSTOM_MSG_STUDENT_RESOURSE_HELPER:

                            showSystemMsg(false);
                            mNick.setText(messageInfo.getUserName());
                            ivStatus.setVisibility(View.GONE);
                            mTime.setText(getLastMsgTime(messageInfo.getLastMsgTime()));
                            mLastMsg.setText(messageInfo.getLastMsgContent());
                            mUnreadSysMessageCount
                                    .setVisibility(messageInfo.getUnreadCount() > 0 ? View.VISIBLE
                                            : View.GONE);
                            break;
                        case MessageInfo.CUSTOM_MSG_SYSTEM:
                            // 系统消息
                            showSystemMsg(true);
                            mUnreadSysMessageCount
                                    .setVisibility(messageInfo.getUnreadCount() > 0 ? View.VISIBLE
                                            : View.GONE);
                            break;

                    }
                }
                else {
                    // 普通的会话消息
                    showSystemMsg(false);
                    mNick.setText(messageInfo.getUserName());
                    
                    EMMessage lastMessage = messageInfo.getLastMessage();
                    String lastStrMsg = "";
                    ivStatus.setVisibility(View.GONE);
                    if (lastMessage != null) {
                        if (lastMessage.status == EMMessage.Status.FAIL
                                && context != null) {
                            ivStatus.setBackgroundResource(R.drawable.icon_chat02);
                            ivStatus.setVisibility(View.VISIBLE);
                        }
                        else if (lastMessage.status == EMMessage.Status.INPROGRESS
                                && context != null) {
                            ivStatus.setBackgroundResource(R.drawable.icon_sending);
                            ivStatus.setVisibility(View.VISIBLE);
                        }
                        lastStrMsg = getMessageDigest(lastMessage, context);
                    }
                    mTime.setText(getLastMsgTime(messageInfo.getLastMsgTime()));
                    mLastMsg.setText(lastStrMsg);
                    // 未读消息的数目
                    if (messageInfo.getUnreadCount() > 0) {
                        if (messageInfo.getUnreadCount() <= 99) {
                            mUnreadMessageCount
                                    .setBadgeCount(messageInfo.getUnreadCount());
                        }
                        else {
                            mUnreadMessageCount.setBadgeText(BaseApplication.getCtx()
                                    .getString(R.string.three_point));
                        }
                        mUnreadMessageCount.setVisibility(View.VISIBLE);
                    }
                    else {
                        mUnreadMessageCount.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
        
        /**
         * 根据消息内容和消息类型获取消息内容提示
         */
        private String getMessageDigest(EMMessage message, Context context) {
            String digest = "";
            switch (message.getType()) {
                case LOCATION:
                    // 位置消息
                    digest = getStrng(context, R.string.location_info);
                    break;
                case IMAGE:
                    // 图片消息
                    digest = getStrng(context, R.string.picture_info);
                    break;
                case VOICE:
                    // 语音消息
                    digest = getStrng(context, R.string.voice_info);
                    break;
                case VIDEO:
                    // 视频消息
                    digest = getStrng(context, R.string.video_info);
                    break;
                case TXT:
                    // 文本消息
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    if (txtBody != null) {
                        digest = txtBody.getMessage() == null ? "" : txtBody.getMessage();
                    }
                    break;
                case FILE:
                    // 普通文件消息
                    digest = getStrng(context, R.string.file_info);
                    break;
                case CMD:
                    // cmd 消息
                    CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
                    if (cmdMsg != null) {
                        if (cmdMsg.msgType == CmdMsg.CMD_TYPE_TEXT) {
                            // cmd 的文本消息， 需要解析出文本的内容
                            String txt = CmdMsgParser.parseCmdMsgBody(cmdMsg).getString(
                                    CmdMsg.Text.TEXT);
                            digest = txt == null ? "" : txt;
                        }
                        else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_CONSULT_ST
                                || cmdMsg.msgType == CmdMsg.CMD_TYPE_CONSULT_TA) {
                            // 咨询
                            digest = getStrng(context, R.string.cmd_consult);
                        }
                        else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_REC_TEACHER) {
                            // 推荐
                            digest = getStrng(context, R.string.cmd_recommend);
                        }
                        else if (cmdMsg.msgType == CmdMsg.CMD_TYPE_ST_BIND_TA) {
                            // 新学生绑定助教
                            digest = getStrng(context, R.string.student_info);
                        }
                        else if (cmdMsg.isGroupCmdMsg()) {
                            // 群组cmd消息
                            digest = CmdMsgParser.getGroupMsgText(context, cmdMsg);
                        }
                        else if (cmdMsg.isLectureCmdMsg()) {
                            // Lecture cmd消息
                            digest = CmdMsgParser.getLectureText(context, cmdMsg, ChatManager.getInstance().getMockUserName(message));
                        }
                    }
                    break;
            }
            
            return digest;
        }
        
        /**
         * 获取最后一条消息的时间
         * <p/>
         * 当天的消息显示详细的时间（小时：分钟） ；
         * <p/>
         * 昨天及以前的消息显示详细的日期即可（年/月/日）
         *
         */
        private String getLastMsgTime(long lastMsgTime) {
            if (DateUtils.yearandmonthanddayFormat.format(lastMsgTime).equals(
                    DateUtils.yearandmonthanddayFormat.format(NetworkTime.currentTimeMillis()))) {
                return DateUtils.hmSdf.format(lastMsgTime);
            }
            else {
                return DateUtils.yearandmonthanddayFormat.format(lastMsgTime);
            }
        }
        
        private String getStrng(Context context, int resId) {
            return context.getResources().getString(resId);
        }
        
        private void showSystemMsg(boolean isSysMsg) {
            if (isSysMsg) {
                tvSysMsg.setVisibility(View.VISIBLE);
                llNormalNews.setVisibility(View.GONE);
            }
            else {
                tvSysMsg.setVisibility(View.GONE);
                llNormalNews.setVisibility(View.VISIBLE);
            }
        }
    }
    
}
