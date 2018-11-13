package com.easemob.easeui.widget;

import java.io.File;

import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.model.EaseVoiceRecorder;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.qingqing.base.log.Logger;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.media.audiorecorder.AudioRecordRequestPermissionDialog;
import com.qingqing.base.view.ToastWrapper;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 按住说话录制控件
 */
public class EaseVoiceRecorderView extends RelativeLayout {
    protected Context context;
    protected LayoutInflater inflater;
    protected EaseVoiceRecorder voiceRecorder;
    
    protected PowerManager.WakeLock wakeLock;
    protected TextView recordingHint;
    protected ViewGroup recordingImgsContainer;
    
    protected TextView countdownTv;
    protected View recordingContainer;
    
    protected boolean overOneMin = false;
    
    protected EaseVoiceRecorderCallback callback;
    protected View touchView;
    
    protected Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 切换msg切换图片
            int what = msg.what;
            int arg1 = msg.arg1;
            switch (what) {
                case EaseVoiceRecorder.MSG_AMPLITUDE:
                    int count = recordingImgsContainer.getChildCount();
                    for (int i = 0; i < count; i++) {
                        recordingImgsContainer.getChildAt(i).setVisibility(
                                (count - i) <= arg1 ? View.VISIBLE : View.INVISIBLE);
                    }
                    break;
                case EaseVoiceRecorder.MSG_COUNT_DOWN:
                    if (arg1 >= 50 && arg1 <= 60) {
                        countdownTv.setVisibility(VISIBLE);
                        recordingContainer.setVisibility(GONE);
                        countdownTv.setText(Integer.toString(60 - arg1));
                    }
                    if (arg1 >= 60) {
                        overOneMin = true;
                        int length = stopRecoding();
                        if (touchView != null) {
                            touchView.setPressed(false);
                        }
                        if (callback != null) {
                            if (checkUploadFile()) {
                                callback.onVoiceRecordComplete(getVoiceFilePath(),
                                        length);
                            }
                            else {
                                ToastWrapper.show(R.string.chat_voice_file_check_fail);
                            }
                            
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    
    public void setChatType(int chatType) {
        voiceRecorder = EaseVoiceRecorder.getVoiceRecorder(getContext(), micImageHandler,
                recordingImgsContainer.getChildCount(), chatType);
    }
    
    public EaseVoiceRecorderView(Context context) {
        super(context);
        init(context);
    }
    
    public EaseVoiceRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public EaseVoiceRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_voice_recorder, this);
        
        recordingHint = (TextView) findViewById(R.id.recording_hint);
        recordingImgsContainer = (ViewGroup) findViewById(
                R.id.ease_recording_imgs_container);
        
        countdownTv = (TextView) findViewById(R.id.tv_record_countdown);
        recordingContainer = findViewById(R.id.container_recording);
        
        setChatType(EaseConstant.CHATTYPE_SINGLE);
        
        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
    }
    
    /**
     * 长按说话按钮touch事件
     *
     * @param v
     * @param event
     */
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event,
            final EaseVoiceRecorderCallback recorderCallback) {
        touchView = v;
        callback = recorderCallback;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                overOneMin = false;
                countdownTv.setVisibility(GONE);
                recordingContainer.setVisibility(VISIBLE);
                try {
                    AudioPlayerController.stopCurrentPlayer();
                    v.setPressed(true);
                    startRecording();
                } catch (Exception e) {
                    v.setPressed(false);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (event.getY() < 0) {
                    showReleaseToCancelHint();
                }
                else {
                    showMoveUpToCancelHint();
                }
                return true;
            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (overOneMin) {
                    return true;
                }
                if (event.getY() < 0) {
                    // discard the recorded audio.
                    discardRecording();
                }
                else {
                    // stop recording and send voice file
                    try {
                        final int length = stopRecoding();
                        if (length > 60) {
                            ToastWrapper.show(R.string.chat_voice_time_limit_text);
                        }
                        else if (length > 0) {
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (recorderCallback != null) {
                                        if (checkUploadFile()) {
                                            recorderCallback.onVoiceRecordComplete(
                                                    getVoiceFilePath(), length);
                                        }
                                        else {
                                            ToastWrapper.show(
                                                    R.string.chat_voice_file_check_fail);
                                        }
                                    }
                                }
                            }, 200);
                        }
                        else {
                            ToastWrapper.show(R.string.The_recording_time_is_too_short);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastWrapper.show(R.string.send_failure_please);
                    }
                }
                return true;
            default:
                discardRecording();
                return false;
        }
    }
    
    /**
     * 检测文件是否存在且长度大于0
     */
    private boolean checkUploadFile() {
        try {
            File file = new File(getVoiceFilePath());
            Logger.o("sendVoiceMessage: checkUploadFile: fileName:" + file.getName()
                    + ", length:" + file.length());
            if (file.exists() && file.length() > 0) {
                return true;
            }
        } catch (Exception ignored) {
            
        }
        return false;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        touchView = null;
        callback = null;
    }
    
    public interface EaseVoiceRecorderCallback {
        /**
         * 录音完毕
         *
         * @param voiceFilePath
         *            录音完毕后的文件路径
         * @param voiceTimeLength
         *            录音时长
         */
        void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength);
    }
    
    public void startRecording() {
        if (!EaseCommonUtils.isExitsSdcard()) {
            ToastWrapper.show(R.string.Send_voice_need_sdcard_support);
            return;
        }
        try {
            wakeLock.acquire();
            this.setVisibility(View.VISIBLE);
            recordingHint.setText(context.getString(R.string.move_up_to_cancel));
            recordingHint.setBackgroundColor(Color.TRANSPARENT);
            voiceRecorder.startRecording();
        } catch (Exception e) {
            new AudioRecordRequestPermissionDialog(getContext()).show();
            e.printStackTrace();
            if (wakeLock.isHeld())
                wakeLock.release();
            if (voiceRecorder != null)
                voiceRecorder.discardRecording();
            this.setVisibility(View.INVISIBLE);
            ToastWrapper.show(R.string.recoding_fail);
            return;
        }
    }
    
    public void showReleaseToCancelHint() {
        recordingHint.setText(context.getString(R.string.release_to_cancel));
    }
    
    public void showMoveUpToCancelHint() {
        recordingHint.setText(context.getString(R.string.move_up_to_cancel));
    }
    
    public void discardRecording() {
        this.setVisibility(View.INVISIBLE);
        if (wakeLock.isHeld())
            wakeLock.release();
        try {
            // 停止录音
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
            }
        } catch (Exception e) {}
    }
    
    public int stopRecoding() {
        this.setVisibility(View.INVISIBLE);
        if (wakeLock.isHeld())
            wakeLock.release();
        return voiceRecorder.stopRecoding();
    }
    
    public String getVoiceFilePath() {
        return voiceRecorder.getVoiceFilePath();
    }
    
    public String getVoiceFileName() {
        return voiceRecorder.getVoiceFileName();
    }
    
    public boolean isRecording() {
        return voiceRecorder.isRecording();
    }
    
}
