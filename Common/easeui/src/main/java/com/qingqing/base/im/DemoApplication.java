package com.qingqing.base.im;

import android.content.Context;
import android.content.Intent;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.model.EaseNotifier;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.PackageUtil;

public class DemoApplication extends BaseApplication {
    
    // login user name
    public final String PREF_USERNAME = "username";
    /**
     * 当前用户nickname,为了苹果推送不是userid而是昵称
     */
    public static String currentUserNick = "";
    
    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        initEMChatOption();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // init demo helper
        if (AppUtil.isMainProcess()) {
            ChatManager.getInstance().init(sCtx);
        }
    }
    
    private void initEMChatOption() {
        EaseNotifier.EaseNotificationInfoProvider notifyProvider = new EaseNotifier.EaseNotificationInfoProvider() {
            
            @Override
            public String getTitle(EMMessage message) {
                // 修改标题,这里使用默认
                return null;
            }
            
            @Override
            public int getSmallIcon(EMMessage message) {
                // 设置小图标，这里为默认
                return 0;
            }
            
            @Override
            public String getDisplayedText(EMMessage message) {
                // 设置状态栏的消息提示，可以根据message的类型做相应提示
                return EaseNotifier.getNotificationTitle(message);
            }
            
            @Override
            public String getLatestText(EMMessage message, int fromUsersNum,
                    int messageNum) {
                String currentMsg = getDisplayedText(message);
                return currentMsg + "  (还有" + fromUsersNum + "个联系人的" + messageNum
                        + "条未读消息)";
            }
            
            @Override
            public Intent getLaunchIntent(EMMessage message) {
                // 设置点击通知栏跳转事件
                Intent intent = new Intent(
                        Constant.ACTION_IM_MESSAGE + "_" + PackageUtil.getPackageName());
                intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return intent;
            }
        };
        
        ChatManager.getInstance().setCustomerNotifyProvider(notifyProvider);
    }
    
}
