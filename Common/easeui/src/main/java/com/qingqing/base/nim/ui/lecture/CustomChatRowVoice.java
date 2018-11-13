package com.qingqing.base.nim.ui.lecture;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.easeui.widget.EaseChatMessageList;
import com.easemob.easeui.widget.chatrow.EaseChatRowFile;
import com.easemob.util.EMLog;

/**
 * 修改语音消息item，切换播放控制源
 *
 * Created by tanwei on 2016/6/7.
 */
public class CustomChatRowVoice extends EaseChatRowFile {

    private ImageView voiceImageView;
    private TextView voiceLengthView;
    private ImageView readStutausView;

    private int bubbleMinWidth;
    private int bubbleMaxWidth;
    private int bubbleIncreaseWidth;


    public CustomChatRowVoice(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);

        bubbleMinWidth = context.getResources().getDimensionPixelSize(com.easemob.easeui.R.dimen.row_voice_bubble_min_width);
        bubbleMaxWidth = context.getResources().getDimensionPixelSize(com.easemob.easeui.R.dimen.row_voice_bubble_max_width);
        bubbleIncreaseWidth = context.getResources().getDimensionPixelOffset(com.easemob.easeui.R.dimen.row_voice_increase_width);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
                com.easemob.easeui.R.layout.ease_row_received_voice : com.easemob.easeui.R.layout.ease_row_sent_voice, this);
    }

    @Override
    protected void onFindViewById() {
        voiceImageView = ((ImageView) findViewById(com.easemob.easeui.R.id.iv_voice));
        voiceLengthView = (TextView) findViewById(com.easemob.easeui.R.id.tv_length);
        readStutausView = (ImageView) findViewById(com.easemob.easeui.R.id.iv_unread_voice);
    }

    @Override
    public void setUpView(EMMessage message, int position, EaseChatMessageList.MessageListItemClickListener itemClickListener) {
        boolean isPlaying = ((LectureHistoryAdapter)adapter).getCurPlayPosition() == position;
        if (isPlaying) {
            startVoice();
        } else {
            stopVoice();
        }
        super.setUpView(message, position, itemClickListener);
    }

    private boolean shouldCalculateBubbleWidth() {
        return bubbleMinWidth > 0 && bubbleMaxWidth >= bubbleMinWidth && bubbleIncreaseWidth >= 0;
    }

    //参考微信规则：时间<=10秒每增加1秒增加同等长度
    //时间>10秒，没增加10秒增加同等长度
    private int calculateBubbleWidthBy(int voiceLength) {
        int result = 0;
        if(voiceLength < 0) {
            result = bubbleMinWidth;
        } else if((voiceLength / 10) == 0) {
            result = bubbleMinWidth + (voiceLength % 10) * bubbleIncreaseWidth;
        } else {
            result = bubbleMinWidth + (9 + voiceLength / 10) * bubbleIncreaseWidth;
        }
        return Math.min(result, bubbleMaxWidth);
    }

    public int getVoiceLength(){
        int length = 0;
        if(message != null && message.getBody() != null){
            VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
            length = voiceBody.getLength();
        }
        return length;
    }
    @Override
    protected void onSetUpView() {
        VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
        int len = voiceBody.getLength();
        if (bubbleLayout != null && bubbleLayout.getLayoutParams() != null && shouldCalculateBubbleWidth()) {
            bubbleLayout.getLayoutParams().width = calculateBubbleWidthBy(len);
            bubbleLayout.requestLayout();
        }
        if (len > 0) {
            voiceLengthView.setText(voiceBody.getLength() + "\"");
            voiceLengthView.setVisibility(View.VISIBLE);
        } else {
            voiceLengthView.setVisibility(View.INVISIBLE);
        }
        boolean isPlaying = ((LectureHistoryAdapter)adapter).getCurPlayPosition() == position;
        if (isPlaying) {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                voiceImageView.setImageResource(com.easemob.easeui.R.drawable.voice_from_icon);
            } else {
                voiceImageView.setImageResource(com.easemob.easeui.R.drawable.voice_to_icon);
            }
        } else {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                voiceImageView.setImageResource(com.easemob.easeui.R.drawable.icon_volume03);
            } else {
                voiceImageView.setImageResource(com.easemob.easeui.R.drawable.icon_volume06);
            }
        }

        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.isListened()) {
                // 隐藏语音未听标志
                readStutausView.setVisibility(View.INVISIBLE);
            } else {
                readStutausView.setVisibility(View.VISIBLE);
            }
            EMLog.d(TAG, "it is receive msg");
            if (message.status == EMMessage.Status.INPROGRESS) {
                progressBar.setVisibility(View.VISIBLE);
                setMessageReceiveCallback();
            } else {
                progressBar.setVisibility(View.INVISIBLE);

            }
            return;
        }

        // until here, deal with send voice msg
        handleSendMessage();
    }

    // show the voice playing animation
    public void startVoice() {
        // play voice, and start animation
        if (message.direct == EMMessage.Direct.RECEIVE) {
            voiceImageView.setImageResource(com.easemob.easeui.R.drawable.voice_from_icon);
        } else {
            voiceImageView.setImageResource(com.easemob.easeui.R.drawable.voice_to_icon);
        }
        AnimationDrawable voiceAnimation = (AnimationDrawable) voiceImageView.getDrawable();
        if (voiceAnimation != null) {
            voiceAnimation.start();
        }
    }

    public void stopVoice() {
        if (voiceImageView.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) voiceImageView.getDrawable()).stop();
        }
        if (message.direct == EMMessage.Direct.RECEIVE) {
            voiceImageView.setImageResource(com.easemob.easeui.R.drawable.icon_volume03);
        } else {
            voiceImageView.setImageResource(com.easemob.easeui.R.drawable.icon_volume06);
        }
    }

    @Override
    protected void onUpdateView() {
        super.onUpdateView();
    }
}