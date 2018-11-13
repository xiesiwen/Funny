package com.qingqing.base.im;

import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.base.im.domain.ContactInfo;

/**
 * Created by huangming on 2017/3/30.
 */

public interface ContactService {
    ContactInfo getContactInfo(String userName);
    
    void saveContactInfo(UserProto.ChatUserInfo userInfo);
    
    void saveGroupContactInfo(UserProto.ChatUserInfo userInfo, String groupId);
}
