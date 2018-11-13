package com.easemob.easeui.model;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.format.Time;

import com.qingqing.base.media.audiorecorder.AudioRecordRequestPermissionDialog;
import com.sinaapp.bashell.VoAACEncoder;

import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by huangming on 2016/6/21.
 */
public class AacVoiceRecorder extends EaseVoiceRecorder {

    private static final double MAX_REPORTABLE_AMP = 32767d;
    private static final double MAX_REPORTABLE_DB = 90.3087d;

    AudioRecord audioRecord;

    private boolean isStart;

    private final static int ONE_KB = 1024;
    private final int simpleRate = 16000;

    private int amplitude;

    private VoAACEncoder voAACEncoder;

    protected AacVoiceRecorder(Context context, Handler handler, int maxLength) {
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
                        int bufferSizeInBytes = getBufferSizeInBytes(simpleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                        byte[] tempBuffer = new byte[bufferSizeInBytes];
                        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, simpleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
                        audioRecord.startRecording();
                        voAACEncoder = new VoAACEncoder();
                        voAACEncoder.Init(simpleRate, simpleRate * 16, (short) 1, (short) 1);
                        fos = new FileOutputStream(getVoiceFilePath());
                        while (isStart) {
                            int bufferRead = audioRecord.read(tempBuffer, 0, bufferSizeInBytes);
                            byte[] copyBytes = copyBytes(tempBuffer, bufferRead);
                            byte[] ret = voAACEncoder.Enc(copyBytes);
                            if (bufferRead > 0) {

                                short maxAmplitude = 0;
                                for (int i = 0; i < bufferRead / 2 && i < bufferSizeInBytes / 2; i++) {
                                    if (Math.abs(tempBuffer[i]) > maxAmplitude) {
                                        maxAmplitude = (short) Math.abs(getShort(tempBuffer[i * 2], tempBuffer[i * 2 + 1]));
                                    }
                                }
                                setAmplitude((int) Math.rint((double) maxAmplitude * getMaxLength() / 0x7fff));
                                fos.write(ret);
                            } else {
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
                }
            }).subscribeOn(Schedulers.computation()).subscribe();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int getBufferSizeInBytes(int sampleRateInHz, int channelConfig, int audioFormat) {
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        return (minBufferSize / ONE_KB * ONE_KB) + (minBufferSize % ONE_KB == 0 ? 0 : ONE_KB);
    }

    private byte[] copyBytes(byte[] src, int count) {
        return copyBytes(src, 0, count);
    }

    private byte[] copyBytes(byte[] src, int offset, int count) {
        if(count < 0) {
            return new byte[0];
        }
        int length = src.length;
        if(offset == 0 && offset + count == length) {
            return src;
        }
        byte[] dst = new byte[count];
        if(offset < 0) {
            return dst;
        }
        for(int i = 0; i < count && i < length - offset; i++) {
            dst[i] = src[offset + i];
        }
        return dst;
    }

    private short getShort(byte leftByte, byte rightByte) {
        return (short) ((((short) rightByte) << 8) + leftByte);
    }

    private void stopAudioRecord() {
        try {
            if (voAACEncoder != null) {
                voAACEncoder.Uninit();
                voAACEncoder = null;
            }

            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected int getAmplitude() {
        return amplitude;
    }


    void setAmplitude(int amplitude) {
        this.amplitude = Math.min(getMaxLength(), Math.max(amplitude, 0));
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
        return uid + now.toString().substring(0, 15) + ".aac";
    }
}
