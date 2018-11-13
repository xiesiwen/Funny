package com.qingqing.base.news;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.easemob.EMConnectionListener;
import com.easemob.chat.EMChatManager;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.msg.Mqtt;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.Constant;
import com.qingqing.base.log.Logger;
import com.qingqing.base.mqtt.BaseMsgReceiver;
import com.qingqing.base.mqtt.IMqttMsgHandler;
import com.qingqing.base.mqtt.MQTTMessage;
import com.qingqing.base.mqtt.MqttManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangming on 2016/12/6.
 */

class NewsReceivingProcessor {

    private static final String TAG = NewsReceivingProcessor.class.getSimpleName();

    private final Context context;

    private BroadcastReceiver huanXinReceiver;

    private BaseMsgReceiver mqttNewsReceiver;

    private boolean huanXinConnected;

    NewsReceivingProcessor(Context context) {
        this.context = context;

        init();
    }

    private void init() {

        Logger.i(TAG, "init");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_NEW_MESSAGE);
        intentFilter.addAction(Constant.ACTION_GROUP_CHANAGED);
        intentFilter.addAction(Constant.ACTION_CONTACT_CHANAGED);
        huanXinReceiver = new HuanXinNewsReceiver();

        context.registerReceiver(huanXinReceiver, intentFilter);

        mqttNewsReceiver = new BaseMsgReceiver() {
            @Override
            public void onMsgReceive(MQTTMessage msg, int from) {

                if (msg.receiverTime <= 0) {
                    Logger.w(MqttManager.TAG,
                            "receive msg time invalid : " + msg.receiverTime);
                    msg.receiverTime = NetworkTime.currentTimeMillis();
                }

                String conversationType = msg.msgCategory;
                if (msg.msgType == Mqtt.UserBatchPushProtoMsgType.b_qingqing_activity_revert_msg_type
                        || msg.msgType == Mqtt.DevicePushProtoMsgType.d_qingqing_activity_revert_msg_type) {
                    Logger.e(TAG, "revert msg : " + msg.msgType + ", " + msg.deviceMsgBid);
                    NewsManager.getInstance().deleteNewsBy(msg.deviceMsgBid);
                    return;
                }
                //当ct，tv,tt同时存在才显示到通知消息中
                NewsConversationType newsConversationType = NewsConversationType.mapStringToValue(conversationType);
                Logger.i(TAG, "mqttNewsReceiver : msgId=" + msg.msgId + ", msgType=" + msg.msgType + ", category=" + conversationType + ", title = " + msg.title + ", content=" + msg.content);
                if (newsConversationType != NewsConversationType.UNKNOWN && !TextUtils.isEmpty(msg.title) && !TextUtils.isEmpty(msg.content)) {

                    //特殊处理：老师端的轻轻小贴士改为轻轻学院
                    if (newsConversationType == NewsConversationType.ACTIVITY) {
                        NewsConversationType type = null;
                        if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                            type = NewsConversationType.QQ_COLLEGE;
                        } else if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                            type = NewsConversationType.COMPANY_NEWS;
                        } else if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                            type = NewsConversationType.INFORMATION;
                        }
                        if (type != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(msg.msgBody);
                                jsonObject.put(IMqttMsgHandler.KEY_CATEGORY, type.getValue());
                                msg.msgBody = jsonObject.toString();
                                msg.msgCategory = type.getValue();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }

                    News news = new News(msg.msgId, msg.msgType, msg.msgBody, msg.deviceMsgBid, msg.msgCategory);
                    news.setConversationId(msg.msgCategory);
                    news.setUnread(true);
                    news.setDirect(News.Direct.RECEIVE);
                    news.setFrom(msg.msgCategory);
                    news.setCreatedTime(msg.receiverTime);
                    if (msg.receiverTime <= 0) {
                        Logger.w(MqttManager.TAG,
                                "obtain time from msg invalid : " + msg.receiverTime);
                    }
                    NewsManager.getInstance().getConversationManager().addNewNews(news);
                }
            }
        };

        MqttManager.getInstance().getMessageHandler().setCustomMsgReceiver(mqttNewsReceiver);

        connectionChanged();
        EMChatManager.getInstance().addConnectionListener(new EMConnectionListener() {
            @Override
            public void onConnected() {
                Logger.e(TAG, "huanxin em onConnected");
                connectionChanged();
            }

            @Override
            public void onDisconnected(int i) {
                Logger.e(TAG, "huanxin em onDisconnected : " + i);
                connectionChanged();
            }
        });
    }

    private void connectionChanged() {
        boolean preHuanXinConnected = isHuanXinConnected();
        boolean huanXinConnected = EMChatManager.getInstance().isConnected();
        Logger.i(TAG, "connectionChanged : preHuanXinConnected = " + preHuanXinConnected + ", huanXinConnected = " + huanXinConnected);
        if (huanXinConnected != preHuanXinConnected) {
            setHuanXinConnected(huanXinConnected);
            NewsManager.getInstance().getConversationManager().loadAllChatConversations();
        }
    }

    private boolean isHuanXinConnected() {
        return huanXinConnected;
    }

    private void setHuanXinConnected(boolean huanXinConnected) {
        this.huanXinConnected = huanXinConnected;
    }

    private class HuanXinNewsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i(TAG, "HuanXinNewsReceiver");

            NewsManager.getInstance().getThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    NewsManager.getInstance().getConversationManager().loadAllChatConversations();
                }
            });
        }
    }


}
