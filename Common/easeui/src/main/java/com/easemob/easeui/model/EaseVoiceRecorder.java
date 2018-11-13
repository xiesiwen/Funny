package com.easemob.easeui.model;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.easeui.EaseConstant;
import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;
import com.qingqing.base.log.Logger;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public abstract class EaseVoiceRecorder {

    private Context context;

    public static final int MSG_AMPLITUDE = 0;
    public static final int MSG_COUNT_DOWN = 1;

    private boolean isRecording = false;
    private long startTime;
    private String voiceFilePath = null;
    private String voiceFileName = null;
    private File file;
    private Handler handler;
    private int maxLength;

    private Timer mTimer;

    protected EaseVoiceRecorder(Context context, Handler handler, int maxLength) {
        this.context = context;
        this.handler = handler;
        this.maxLength = maxLength;
    }

    public static EaseVoiceRecorder getVoiceRecorder(Context context, Handler handler, int maxLength, int chatType) {
        if(chatType == EaseConstant.CHATTYPE_CHATROOM) {
            return new MP3VoiceRecorder(context, handler, maxLength);
        } else {
            return new AacVoiceRecorder(context, handler, maxLength);
        }
    }

    public Context getContext() {
        return context;
    }

    protected abstract boolean onStartRecord();

    protected abstract int getAmplitude();

    /**
     * start recording to the file
     */
    public void startRecording() {
        voiceFileName = getVoiceFileName(EMChatManager.getInstance().getCurrentUser());
        voiceFilePath = PathUtil.getInstance().getVoicePath() + "/" + getVoiceFileName();
//        voiceFilePath = Environment.getExternalStorageDirectory().getPath() + "/" + getVoiceFileName();
        file = new File(voiceFilePath);
        if (onStartRecord()) {
            startTime = new Date().getTime();
            isRecording = true;
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                int count = 0;

                @Override
                public void run() {
                    android.os.Message msg = new android.os.Message();
                    msg.what = MSG_COUNT_DOWN;
                    msg.arg1 = count;
                    count++;
                    handler.sendMessage(msg);
                }
            }, 0, 1000);

            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                    try {
                        while (isRecording) {
                            android.os.Message msg = new android.os.Message();
                            msg.what = MSG_AMPLITUDE;
                            msg.arg1 = getAmplitude();
                            handler.sendMessage(msg);
                            SystemClock.sleep(100);
                        }
                    } catch (Exception e1) {
                        // from the crash report website, found one NPE crash from
                        // one android 4.0.4 htc phone
                        // maybe handler is null for some reason
                        EMLog.e("voice", e1.toString());
                    }
                    e.onComplete();
                }
            }).subscribeOn(Schedulers.computation()).subscribe();
        }
    }

    protected abstract void onDiscardRecord();

    public void discardRecording() {
        isRecording = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        onDiscardRecord();
    }

    protected abstract void onStopRecord();

    public int stopRecoding() {
        isRecording = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        onStopRecord();
        if (getVoiceFile() == null || !getVoiceFile().exists()
                || !getVoiceFile().isFile()) {
            return EMError.INVALID_FILE;
        }
        if (getVoiceFile().length() == 0) {
            Logger.w("sendVoiceMessage: stopRecoding Fail: fileName:"
                    + getVoiceFile().getName() + ", length:" + getVoiceFile().length());
            getVoiceFile().delete();
            return EMError.INVALID_FILE;
        }
        int seconds = (int) (new Date().getTime() - getStartTime()) / 1000;
        Logger.o("sendVoiceMessage: stopRecoding: fileName:" + getVoiceFile().getName()
                + ", length:" + getVoiceFile().length());
        EMLog.d("voice", "voice recording finished. seconds:" + seconds + " file length:"
                + getVoiceFile().length());
        return seconds;
    }

    protected abstract String getVoiceFileName(String uid);


    public boolean isRecording() {
        return isRecording;
    }


    public String getVoiceFilePath() {
        return voiceFilePath;
    }

    public String getVoiceFileName() {
        return voiceFileName;
    }

    protected long getStartTime() {
        return startTime;
    }

    protected int getMaxLength() {
        return maxLength;
    }

    public File getVoiceFile() {
        return file;
    }
}
