package com.qingqing.base.im;

import android.content.Context;

import com.qingqing.base.im.db.DemoDBManager;
import com.qingqing.base.im.utils.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangming on 2015/12/22.
 */
public class ChatModel extends DataModel {

    protected Map<Key, Object> valueCache = new HashMap<Key, Object>();

    ContactModel contactModel;


    ChatModel(Context context) {
        super(context);
        contactModel = new ContactModel(context);
    }

    public ContactModel getContactModel() {
        return contactModel;
    }

    void init() {
        super.init();
        contactModel.init();
    }

    void reset() {
        super.reset();
        contactModel.reset();
        runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                DemoDBManager.getInstance().closeDB();
            }
        });
    }


    public void setSettingMsgNotification(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgNotification(paramBoolean);
        valueCache.put(Key.VibrateAndPlayToneOn, paramBoolean);
    }

    public boolean getSettingMsgNotification() {
        Object val = valueCache.get(Key.VibrateAndPlayToneOn);

        if (val == null) {
            val = PreferenceManager.getInstance().getSettingMsgNotification();
            valueCache.put(Key.VibrateAndPlayToneOn, val);
        }

        return (Boolean) (val != null ? val : true);
    }

    public void setSettingMsgSound(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgSound(paramBoolean);
        valueCache.put(Key.PlayToneOn, paramBoolean);
    }

    public boolean getSettingMsgSound() {
        Object val = valueCache.get(Key.PlayToneOn);

        if (val == null) {
            val = PreferenceManager.getInstance().getSettingMsgSound();
            valueCache.put(Key.PlayToneOn, val);
        }

        return (Boolean) (val != null ? val : true);
    }

    public void setSettingMsgVibrate(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgVibrate(paramBoolean);
        valueCache.put(Key.VibrateOn, paramBoolean);
    }

    public boolean getSettingMsgVibrate() {
        Object val = valueCache.get(Key.VibrateOn);

        if (val == null) {
            val = PreferenceManager.getInstance().getSettingMsgVibrate();
            valueCache.put(Key.VibrateOn, val);
        }

        return (Boolean) (val != null ? val : true);
    }

    public void setSettingMsgSpeaker(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgSpeaker(paramBoolean);
        valueCache.put(Key.SpeakerOn, paramBoolean);
    }

    public boolean getSettingMsgSpeaker() {
        Object val = valueCache.get(Key.SpeakerOn);

        if (val == null) {
            val = PreferenceManager.getInstance().getSettingMsgSpeaker();
            valueCache.put(Key.SpeakerOn, val);
        }

        return (Boolean) (val != null ? val : true);
    }


    public void setGroupsSynced(boolean synced) {
        PreferenceManager.getInstance().setGroupsSynced(synced);
    }

    public boolean isGroupsSynced() {
        return PreferenceManager.getInstance().isGroupsSynced();
    }

    enum Key {
        VibrateAndPlayToneOn,
        VibrateOn,
        PlayToneOn,
        SpeakerOn,
        DisabledGroups,
        DisabledIds
    }

}
