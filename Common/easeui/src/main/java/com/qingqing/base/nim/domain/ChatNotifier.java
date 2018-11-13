package com.qingqing.base.nim.domain;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.controller.EaseUI;
import com.easemob.util.EMLog;
import com.easemob.util.EasyUtils;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.comparator.MessageIndexComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by huangming on 2016/8/31.
 */
public class ChatNotifier {
    
    private static final int CHAT_NOTIFY_ID = 1025;
    
    private static final String TAG = "ChatNotifier";
    
    private AudioManager audioManager;
    private Vibrator vibrator;
    private Ringtone ringtone;
    
    private ChatNotificationContentProvider contentProvider;
    private Context context;
    
    private int notificationCount;
    private NotificationManager notificationManager;
    private Set<String> users = new HashSet<>();
    
    private final String packageName;
    
    private long lastNotifyTime;
    
    ChatNotifier(Context context) {
        this.context = context;
        this.contentProvider = new ChatNotificationContentDefaultProvider();
        
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        this.packageName = context.getApplicationInfo().packageName;
    }
    
    private Context getContext() {
        return context;
    }
    
    private String getPackageName() {
        return packageName;
    }
    
    private Set<String> getUsers() {
        return users;
    }
    
    private Vibrator getVibrator() {
        return vibrator;
    }
    
    private AudioManager getAudioManager() {
        return audioManager;
    }
    
    private int getNotificationCount() {
        return notificationCount;
    }
    
    private void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }
    
    private void setLastNotifyTime(long lastNotifyTime) {
        this.lastNotifyTime = lastNotifyTime;
    }
    
    private long getLastNotifyTime() {
        return lastNotifyTime;
    }
    
    private void setRingtone(Ringtone ringtone) {
        this.ringtone = ringtone;
    }
    
    private Ringtone getRingtone() {
        return ringtone;
    }
    
    public ChatNotificationContentProvider getContentProvider() {
        return contentProvider;
    }
    
    private NotificationManager getNotificationManager() {
        return notificationManager;
    }
    
    private void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }
    
    public void onNewMessage(Message message) {
        handlerMessage(message);
    }
    
    public void onNewMessages(List<Message> messages) {
        handlerMessages(messages);
    }
    
    private void handlerMessage(Message message) {
        // 判断app是否在后台
        boolean isForeground = EasyUtils.isAppRunningForeground(getContext());
        if (!isForeground) {
            Logger.d(TAG, "app is running in background");
            if (getContentProvider().isNotifyAllowed(getContext(), message)) {
                List<Message> messageList = new ArrayList<>();
                messageList.add(message);
                handlerNotifiableMessages(messageList);
            }
        }
        else {
            reset();
        }
    }
    
    private void handlerMessages(List<Message> messages) {
        // 判断app是否在后台
        boolean isForeground = EasyUtils.isAppRunningForeground(getContext());
        if (!isForeground) {
            Logger.d(TAG, "app is running in background");
            List<Message> messageList = new ArrayList<>();
            for (Message message : messages) {
                if (getContentProvider().isNotifyAllowed(getContext(), message)) {
                    messageList.add(message);
                    handlerNotifiableMessages(messageList);
                }
            }
        }
        else {
            reset();
        }
    }
    
    private void handlerNotifiableMessages(List<Message> messages) {
        Collections.sort(messages, new MessageIndexComparator());
        int size = messages.size();
        Message lastMessage = messages.get(size - 1);
        for (Message message : messages) {
            getUsers().add(message.getFrom());
        }
        
        setNotificationCount(getNotificationCount() + size);
        
        try {
            final Context context = getContext();
            PackageManager packageManager = context.getPackageManager();
            String appName = (String) packageManager
                    .getApplicationLabel(context.getApplicationInfo());
            
            // notification title
            String contentTitle = appName;
            String tickerText = "";
            Intent contentIntent = context.getPackageManager()
                    .getLaunchIntentForPackage(getPackageName());
            int userSize = getUsers().size();
            String contentText = userSize + "个联系人发来" + getNotificationCount() + "条消息";
            
            int smallIcon = context.getApplicationInfo().icon;
            if (getContentProvider() != null) {
                String customTickerText = getContentProvider().getTickerText(context,
                        lastMessage);
                String customTitle = getContentProvider().getTitle(context, lastMessage);
                Intent customIntent = getContentProvider().getContentIntent(context,
                        lastMessage);
                String customContent = getContentProvider().getContentText(context,
                        lastMessage, userSize, getNotificationCount());
                int customSmallIcon = getContentProvider().getSmallIcon(context,
                        lastMessage);
                if (!TextUtils.isEmpty(customTickerText)) {
                    // 设置自定义的状态栏提示内容
                    tickerText = customTickerText;
                }
                if (!TextUtils.isEmpty(customTitle)) {
                    // 设置自定义的通知栏标题
                    contentTitle = customTitle;
                }
                if (customIntent != null) {
                    contentIntent = customIntent;
                }
                if (!TextUtils.isEmpty(customContent)) {
                    contentText = customContent;
                }
                if (customSmallIcon != 0) {
                    smallIcon = customSmallIcon;
                }
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    CHAT_NOTIFY_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // create and send notification
            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(smallIcon).setWhen(System.currentTimeMillis())
                    .setAutoCancel(true).setContentTitle(contentTitle)
                    .setTicker(tickerText).setContentText(contentText)
                    .setContentIntent(pendingIntent).build();
            
            if (getNotificationManager() == null) {
                setNotificationManager((NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE));
            }
            getNotificationManager().notify(CHAT_NOTIFY_ID, notification);
            
            vibrateAndPlayTone(lastMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 手机震动和声音提示
     */
    private void vibrateAndPlayTone(Message message) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - getLastNotifyTime() < 1000) {
            // received new messages within 2 seconds, skip play ringtone
            return;
        }
        setLastNotifyTime(currentTime);
        
        try {
            
            // 判断是否处于静音模式
            if (getAudioManager().getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                Logger.e(TAG, "in slient mode now");
                return;
            }
            long[] pattern = new long[] { 0, 180, 80, 120 };
            getVibrator().vibrate(pattern, -1);
            if (getRingtone() == null) {
                Uri notificationUri = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                setRingtone(RingtoneManager.getRingtone(getContext(), notificationUri));
                if (getRingtone() == null) {
                    Logger.e(TAG, "cant find ringtone at:" + notificationUri.getPath());
                    return;
                }
            }
            if (!getRingtone().isPlaying()) {
                String vendor = Build.MANUFACTURER;
                getRingtone().play();
                // for samsung S3, we meet a bug that the phone will
                // continue ringtone without stop
                // so add below special handler to stop it after 3s if
                // needed
                if (vendor != null && vendor.toLowerCase().contains("samsung")) {
                    Observable.timer(3,TimeUnit.SECONDS).observeOn(Schedulers.computation()).doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            try{
                                if (getRingtone().isPlaying()) {
                                    getRingtone().stop();
                                }
                            }catch (Exception exce){}
                        }
                    }).subscribe();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void reset() {
        cancelNotification();
        setNotificationCount(0);
        getUsers().clear();
    }
    
    private void cancelNotification() {
        if (getNotificationManager() != null) {
            getNotificationManager().cancel(CHAT_NOTIFY_ID);
            setNotificationManager(null);
        }
    }
    
    void destroy() {
        reset();
        context = null;
    }
    
}
