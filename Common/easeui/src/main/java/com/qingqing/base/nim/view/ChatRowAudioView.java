package com.qingqing.base.nim.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.qingqing.base.im.AudioBatchProcesser;
import com.qingqing.base.nim.PlayStatusObservable;
import com.qingqing.base.nim.PlayStatusObserver;
import com.qingqing.base.nim.domain.AudioMessageBody;
import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/18.
 */
public class ChatRowAudioView extends ChatRowDefaultView implements PlayStatusObserver {
    
    private ImageView audioPlayView;
    private View notListenedView;
    private TextView lengthTextView;
    
    private int bubbleMinWidth;
    private int bubbleMaxWidth;
    private int bubbleIncreaseWidth;
    
    private PlayStatusObservable playObservable;
    
    public ChatRowAudioView(Context context, Message message) {
        super(context, message);
        
        bubbleMinWidth = context.getResources()
                .getDimensionPixelSize(R.dimen.row_voice_bubble_min_width);
        bubbleMaxWidth = context.getResources()
                .getDimensionPixelSize(R.dimen.row_voice_bubble_max_width);
        bubbleIncreaseWidth = context.getResources()
                .getDimensionPixelOffset(R.dimen.row_voice_increase_width);
    }
    
    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        audioPlayView = (ImageView) findViewById(R.id.img_audio);
        notListenedView = findViewById(R.id.img_not_listened);
        lengthTextView = (TextView) findViewById(R.id.tv_length);
    }
    
    @Override
    protected int onInflateReceivedLayout() {
        return R.layout.chat_row_new_received_audio;
    }
    
    @Override
    protected int onInflateSentLayout() {
        return R.layout.chat_row_new_sent_audio;
    }
    
    public TextView getLengthTextView() {
        return lengthTextView;
    }
    
    public View getNotListenedView() {
        return notListenedView;
    }
    
    private ImageView getAudioPlayView() {
        return audioPlayView;
    }
    
    private boolean shouldCalculateBubbleWidth() {
        return bubbleMinWidth > 0 && bubbleMaxWidth >= bubbleMinWidth
                && bubbleIncreaseWidth >= 0;
    }
    
    // 参考微信规则：时间<=10秒每增加1秒增加同等长度
    // 时间>10秒，没增加10秒增加同等长度
    private int calculateBubbleWidthBy(int voiceLength) {
        int result;
        if (voiceLength < 0) {
            result = bubbleMinWidth;
        }
        else if ((voiceLength / 10) == 0) {
            result = bubbleMinWidth + (voiceLength % 10) * bubbleIncreaseWidth;
        }
        else {
            result = bubbleMinWidth + (9 + voiceLength / 10) * bubbleIncreaseWidth;
        }
        return Math.min(result, bubbleMaxWidth);
    }
    
    @Override
    protected void onSetupViewBy(Message message) {
        AudioMessageBody body = (AudioMessageBody) message.getBody();
        
        final int length = body.getLength();
        
        if (getBubbleLayout() != null && getBubbleLayout().getLayoutParams() != null
                && shouldCalculateBubbleWidth()) {
            getBubbleLayout().getLayoutParams().width = calculateBubbleWidthBy(length);
            getBubbleLayout().requestLayout();
        }
        if (getLengthTextView() != null) {
            if (length > 0) {
                getLengthTextView().setText(length + "\"");
                getLengthTextView().setVisibility(View.VISIBLE);
            }
            else {
                getLengthTextView().setVisibility(View.GONE);
            }
        }
        
        if (getProgressBar() != null) {
            getProgressBar().setVisibility(
                    body.getStatus() == Message.Status.IN_PROGRESS ? VISIBLE : GONE);
        }
        
        playStatusChanged();
        
    }
    
    @Override
    protected void updatePlayObservable(PlayStatusObservable playStatusObservable) {
        this.playObservable = playStatusObservable;
        if (getPlayObservable() != null) {
            getPlayObservable().registerObserver(this);
        }
    }
    
    private PlayStatusObservable getPlayObservable() {
        return playObservable;
    }
    
    private boolean isAudioPlaying() {
        return getPlayObservable() != null && getPlayObservable().isPlaying(getMessage());
    }
    
    @Override
    public void onPlayStatusChanged() {
        playStatusChanged();
    }
    
    private void playStatusChanged() {
        if (isAudioPlaying()) {
            startAudio();
        }
        else {
            stopAudio();
        }
        
        if (getNotListenedView() != null && getMessage() != null) {
            getNotListenedView()
                    .setVisibility(getMessage().isListened() ? GONE : VISIBLE);
        }
    }
    
    public void startAudio() {
        if (getAudioPlayView() == null) {
            return;
        }
        if (isSendDirect()) {
            getAudioPlayView().setImageResource(R.drawable.voice_to_icon);
        }
        else {
            getAudioPlayView().setImageResource(R.drawable.voice_from_icon);
        }
        
        if (getAudioPlayView().getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) getAudioPlayView().getDrawable()).start();
        }
    }
    
    public void stopAudio() {
        
        if (getAudioPlayView() == null) {
            return;
        }
        if (getAudioPlayView().getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) getAudioPlayView().getDrawable()).stop();
        }
        if (isSendDirect()) {
            getAudioPlayView().setImageResource(R.drawable.icon_volume06);
        }
        else {
            getAudioPlayView().setImageResource(R.drawable.icon_volume03);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (getPlayObservable() != null) {
            getPlayObservable().unregisterObserver(this);
        }
    }
    
}
