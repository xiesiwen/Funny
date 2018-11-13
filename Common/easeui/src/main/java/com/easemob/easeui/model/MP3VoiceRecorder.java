package com.easemob.easeui.model;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.format.Time;

import com.qingqing.base.media.audiorecorder.AudioRecordRequestPermissionDialog;
import com.example.lamemp3.MP3Recorder;

import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * 录制mp3格式语音
 *
 * Created by huangming on 2017/3/14.
 */

public class MP3VoiceRecorder extends EaseVoiceRecorder {
    
    private static final String TAG = "MP3VoiceRecorder";
    
    private AudioRecord audioRecord;
    
    private boolean isStart;
    
    private int amplitude;
    
    private final static int ONE_KB = 1024;
    private final int simpleRate = 8000 * 2;
    
    protected MP3VoiceRecorder(Context context, Handler handler, int maxLength) {
        super(context, handler, maxLength);
    }
    
    @Override
    protected boolean onStartRecord() {
        try {
            setAmplitude(0);
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                    stopAudioRecord();
                    FileOutputStream fos = null;
                    try {
                        isStart = true;
                        int bufferSizeInBytes = getBufferSize(simpleRate,
                                AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT);
                        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                simpleRate, AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes * 2);
                        audioRecord.startRecording();
                        short[] buffer = new short[bufferSizeInBytes];
                        byte[] mp3Buffer = new byte[buffer.length * 4];
                        MP3Recorder.init(simpleRate, 1, simpleRate, 32, 7);
                        fos = new FileOutputStream(getVoiceFilePath());
                        while (isStart) {
                            int bufferRead = audioRecord.read(buffer, 0,
                                    bufferSizeInBytes);
                            int encoderResultSize = MP3Recorder.encode(buffer, buffer,
                                    bufferRead, mp3Buffer);
                            if (bufferRead > 0 && encoderResultSize > 0) {

                                short maxAmplitude = 0;
                                for (int i = 0; i < bufferRead
                                        && i < bufferSizeInBytes; i++) {
                                    maxAmplitude = (short) Math.abs(buffer[i]);
                                }
                                setAmplitude((int) Math.rint(
                                        (double) maxAmplitude * getMaxLength() / 0x7fff));
                                fos.write(mp3Buffer, 0, encoderResultSize);
                                fos.flush();
                            }
                            else {
                                setAmplitude(0);
                            }
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        new AudioRecordRequestPermissionDialog(getContext()).show();
                    } finally {
                        try {
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    isStart = false;
                    stopAudioRecord();
                    setAmplitude(0);
                    e.onComplete();
                }
            }).subscribeOn(Schedulers.computation()).subscribe();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void setAmplitude(int amplitude) {
        this.amplitude = Math.min(getMaxLength(), Math.max(amplitude, 0));
    }
    
    private int getBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig,
                audioFormat);
        return (minBufferSize / ONE_KB * ONE_KB)
                + (minBufferSize % ONE_KB == 0 ? 0 : ONE_KB);
    }
    
    private void stopAudioRecord() {
        try {
            MP3Recorder.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected int getAmplitude() {
        return amplitude;
    }
    
    @Override
    protected void onDiscardRecord() {
        isStart = false;
        setAmplitude(0);
    }
    
    @Override
    protected void onStopRecord() {
        isStart = false;
        setAmplitude(0);
    }
    
    @Override
    protected String getVoiceFileName(String uid) {
        Time now = new Time();
        now.setToNow();
        return uid + now.toString().substring(0, 15) + ".mp3";
    }
}
