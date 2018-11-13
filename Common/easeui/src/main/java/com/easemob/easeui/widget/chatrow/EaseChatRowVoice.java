package com.easemob.easeui.widget.chatrow;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.easeui.R;
import com.easemob.easeui.widget.EaseChatMessageList;
import com.easemob.util.EMLog;
import com.qingqing.base.im.AudioBatchProcesser;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.media.MediaControl;
import com.qingqing.base.media.MediaPlayerController;
import com.qingqing.base.view.ToastWrapper;

import java.io.File;

public class EaseChatRowVoice extends EaseChatRowFile {

    private ImageView voiceImageView;
    private TextView voiceLengthView;
    private ImageView readStutausView;

    private int bubbleMinWidth;
    private int bubbleMaxWidth;
    private int bubbleIncreaseWidth;

    private MediaPlayerController mPlayerController;

    public EaseChatRowVoice(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);

        mPlayerController = AudioBatchProcesser.getInstance(context).getAndAddMediaPlayerController(message);
        mPlayerController.addMediaControl(mMediaControl);

        bubbleMinWidth = context.getResources().getDimensionPixelSize(R.dimen.row_voice_bubble_min_width);
        bubbleMaxWidth = context.getResources().getDimensionPixelSize(R.dimen.row_voice_bubble_max_width);
        bubbleIncreaseWidth = context.getResources().getDimensionPixelOffset(R.dimen.row_voice_increase_width);
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

    private MediaControl mMediaControl = new MediaControl() {
        @Override
        public void onError(Throwable err) {

        }

        @Override
        public void onStarted() {
            startVoice();
        }

        @Override
        public void onPrepared() {
        }

        @Override
        public void onStoped() {
            stopVoice();
        }

        @Override
        public void onCompleted() {
            stopVoice();
        }
    };

    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
                R.layout.ease_row_received_voice : R.layout.ease_row_sent_voice, this);
    }

    @Override
    protected void onFindViewById() {
        voiceImageView = ((ImageView) findViewById(R.id.iv_voice));
        voiceLengthView = (TextView) findViewById(R.id.tv_length);
        readStutausView = (ImageView) findViewById(R.id.iv_unread_voice);
    }

    @Override
    public void setUpView(EMMessage message, int position, EaseChatMessageList.MessageListItemClickListener itemClickListener) {
        if (mPlayerController != null) {
            mPlayerController.removeMediaControl(mMediaControl);
        }
        mPlayerController = AudioBatchProcesser.getInstance(context).getAndAddMediaPlayerController(message);
        mPlayerController.addMediaControl(mMediaControl);
        if (mPlayerController.isPlaying()) {
            startVoice();
        } else {
            stopVoice();
        }
        super.setUpView(message, position, itemClickListener);
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
        if (mPlayerController.isPlaying()) {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                voiceImageView.setImageResource(R.drawable.voice_from_icon);
            } else {
                voiceImageView.setImageResource(R.drawable.voice_to_icon);
            }
        } else {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                voiceImageView.setImageResource(R.drawable.icon_volume03);
            } else {
                voiceImageView.setImageResource(R.drawable.icon_volume06);
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
            voiceImageView.setImageResource(R.drawable.voice_from_icon);
        } else {
            voiceImageView.setImageResource(R.drawable.voice_to_icon);
        }
        AnimationDrawable voiceAnimation = (AnimationDrawable) voiceImageView.getDrawable();
        if (voiceAnimation != null) {
            voiceAnimation.start();
        }

        // 如果是接收的消息
        if (message.direct == EMMessage.Direct.RECEIVE) {
            try {
                if (!message.isAcked) {
                    message.isAcked = true;
                    // 告知对方已读这条消息
                    if (message.getChatType() != EMMessage.ChatType.GroupChat && message.getChatType() != EMMessage.ChatType.ChatRoom)
                        EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                }
            } catch (Exception e) {
                message.isAcked = false;
            }
            if (!message.isListened() && readStutausView != null && readStutausView.getVisibility() == View.VISIBLE) {
                // 隐藏自己未播放这条语音消息的标志
                readStutausView.setVisibility(View.INVISIBLE);
                EMChatManager.getInstance().setMessageListened(message);
                AudioBatchProcesser.getInstance(getContext()).saveListenedAudioMessage(message);
            }

        }
    }

    public void stopVoice() {
        if (voiceImageView.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) voiceImageView.getDrawable()).stop();
        }
        if (message.direct == EMMessage.Direct.RECEIVE) {
            voiceImageView.setImageResource(R.drawable.icon_volume03);
        } else {
            voiceImageView.setImageResource(R.drawable.icon_volume06);
        }
    }

    @Override
    protected void onUpdateView() {
        super.onUpdateView();
    }

    @Override
    protected void onBubbleClick() {
        String st = activity.getResources().getString(R.string.Is_download_voice_click_later);
        VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
        File file = new File(voiceBody.getLocalUrl());
        if (message.direct == EMMessage.Direct.SEND) {
            if (file.exists() && file.isFile()) {
                mPlayerController.toggleMediaPlayState();
            } else {
                EMLog.e(TAG, "file not exist");
            }
        } else {
            if (message.status == EMMessage.Status.SUCCESS) {
                if (file.exists() && file.isFile()) {
                    //1添加是否是聊天室的判断？？？？？？？？？？2通过message判断会话框的ID？？？？？？？？？？
                    if (!mPlayerController.isPlaying()) {
                        AudioPlayerController.pauseCurrentPlayer();
                        AudioBatchProcesser.getInstance(getContext()).startAudioBatchController(ChatManager.getInstance().getConversationId(message), message.getMsgId());
                    } else {
                        mPlayerController.toggleMediaPlayState();
                    }
                } else {
                    AudioBatchProcesser.getInstance(getContext()).asyncFetchMessageIfNeeded(message);
                    EMLog.e(TAG, "file not exist");
                }
            } else if (message.status == EMMessage.Status.INPROGRESS) {
                ToastWrapper.show(st);
            } else if (message.status == EMMessage.Status.FAIL) {
                ToastWrapper.show(st);
                AudioBatchProcesser.getInstance(getContext()).asyncFetchMessageIfNeeded(message);
            }
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayerController.removeMediaControl(mMediaControl);
        mPlayerController = null;
    }

}
