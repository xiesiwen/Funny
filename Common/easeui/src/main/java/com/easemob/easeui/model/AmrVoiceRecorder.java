package com.easemob.easeui.model;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.format.Time;

import com.easemob.util.EMLog;

import java.io.IOException;

/**
 * Created by huangming on 2016/6/21.
 */
public class AmrVoiceRecorder extends EaseVoiceRecorder {

    MediaRecorder recorder;

    protected int audioOutputFormat = MediaRecorder.OutputFormat.AMR_NB;
    protected int audioEncoder = MediaRecorder.AudioEncoder.AMR_NB;

    AmrVoiceRecorder(Context context, Handler handler, int maxLength) {
        super(context, handler, maxLength);
    }

    @Override
    protected boolean onStartRecord() {
        try {
            // need to create recorder every time, otherwise, will got exception
            // from setOutputFile when try to reuse
            if (recorder != null) {
                recorder.release();
                recorder = null;
            }
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(audioOutputFormat);
            recorder.setAudioEncoder(audioEncoder);
            recorder.setAudioChannels(1); // MONO
            recorder.setAudioSamplingRate(8000); // 8000Hz
            recorder.setAudioEncodingBitRate(12800); // seems if change this to
            recorder.setOutputFile(getVoiceFilePath());
            recorder.prepare();
            recorder.start();
            return true;
        } catch (IOException e) {
            EMLog.e("voice", "prepare() failed");
            return false;
        }
    }

    @Override
    protected int getAmplitude() {
        return recorder != null ? recorder.getMaxAmplitude() * getMaxLength() / 0x7fff : 0;
    }

    @Override
    protected void onDiscardRecord() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                if (getVoiceFile() != null && getVoiceFile().exists() && !getVoiceFile().isDirectory()) {
                    getVoiceFile().delete();
                }
            } catch (IllegalStateException e) {
            } catch (RuntimeException e) {
            }
        }
    }

    @Override
    protected void onStopRecord() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (recorder != null) {
            recorder.release();
        }
    }

    @Override
    protected String getVoiceFileName(String uid) {
        Time now = new Time();
        now.setToNow();
        return uid + now.toString().substring(0, 15) + ".amr";
    }

}
