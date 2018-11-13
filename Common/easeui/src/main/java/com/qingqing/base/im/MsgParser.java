package com.qingqing.base.im;

import android.text.TextUtils;

import com.easemob.chat.EMMessage;
import com.qingqing.base.im.domain.ExtField;

/**
 * Created by huangming on 2016/1/4.
 */
public class MsgParser {
    
    public static boolean isReceiveMsg(EMMessage message) {
        String curUser = ChatManager.getInstance().getCurrentUserName();
        if (TextUtils.isEmpty(curUser)) {
            return false;
        }
        if (message != null) {
            ExtField extField = ExtFieldParser.getExt(message);
            
            boolean targetNull = extField.targetUsers == null
                    || extField.targetUsers.size() <= 0;
            boolean filterNull = extField.filterUsers == null
                    || extField.filterUsers.size() <= 0;
            
            if (!targetNull) {
                return extField.targetUsers.contains(curUser);
            }
            else if (!filterNull) {
                return !extField.filterUsers.contains(curUser);
            }
            return true;
        }
        return false;
    }
}
