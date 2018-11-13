package com.qingqing.base.nim.view;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by huangming on 2016/8/23.
 */
public interface ChatInputMenuListener {

    void onSendTextMessage(String content);

    boolean onPressToSpeakBtnTouch(View v, MotionEvent event);

}
