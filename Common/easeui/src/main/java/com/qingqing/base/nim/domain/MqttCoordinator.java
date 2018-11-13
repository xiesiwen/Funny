package com.qingqing.base.nim.domain;

import android.text.TextUtils;

import com.qingqing.base.log.Logger;
import com.qingqing.base.mqtt.BaseMsgReceiver;
import com.qingqing.base.mqtt.IMqttMsgHandler;
import com.qingqing.base.mqtt.MQTTMessage;
import com.qingqing.base.mqtt.MqttManager;
import com.qingqing.base.utils.ExecUtil;

import org.json.JSONException;

/**
 * Created by huangming on 2016/8/23.
 */
class MqttCoordinator extends AbstractMessageDispatcher implements MessageMonitor {
    
    private static final String TAG = "whuthmMqttCoordinator";
    
    private BaseMsgReceiver mqttMsgReceiver;
    
    MqttCoordinator() {
        setMqttMsgReceiver(new MqttMsgReceiver());
    }
    
    @Override
    public void registerMonitor() {
        Logger.i(TAG, "registerMonitor : " + getMqttMsgReceiver());
        MqttManager.getInstance().setSingleMsgReceiver(getMqttMsgType(),
                getMqttMsgReceiver());
    }
    
    @Override
    public void unregisterMonitor() {
        Logger.i(TAG, "unregisterMonitor");
        MqttManager.getInstance().removeSingleMsgReceiver(getMqttMsgType(),
                getMqttMsgReceiver());
    }
    
    private BaseMsgReceiver getMqttMsgReceiver() {
        return mqttMsgReceiver;
    }
    
    private void setMqttMsgReceiver(BaseMsgReceiver mqttMsgReceiver) {
        this.mqttMsgReceiver = mqttMsgReceiver;
    }
    
    private int getMqttMsgType() {
        return 405;
    }
    
    private class MqttMsgReceiver extends BaseMsgReceiver {
        
        @Override
        public void onMsgReceive(MQTTMessage msg, int from) {
            parseMessageBy(msg.getString(IMqttMsgHandler.KEY_IM_HUANXIN_MSG),
                    msg.getInt(IMqttMsgHandler.KEY_IM_INT_MSG_INDEX),
                    msg.getString(IMqttMsgHandler.KEY_IM_MSG_UUID));
        }
    }
    
    private void parseMessageBy(final String msgContent, final int msgIndex,
            final String UUID) {
        ExecUtil.execute(ExecUtil.MODE_COMPUTE,new Runnable() {
            @Override
            public void run() {
                
                if (TextUtils.isEmpty(UUID)) {
                    Logger.e(TAG, "UUID is null, disposed");
                    return;
                }
                Message message = null;
                try {
                    message = MessageParser.getMessageBy(msgContent, UUID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (message != null) {
                    message.setIndex(msgIndex);
                    Logger.i(TAG, "parseMessageBy :  " + message.getId() + " ,  index"
                            + message.getIndex());
                    dispatchMessage(message);
                }
            }
        });
    }
    
}
