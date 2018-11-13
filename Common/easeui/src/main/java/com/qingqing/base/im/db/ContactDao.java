package com.qingqing.base.im.db;

import android.content.Context;

import com.qingqing.base.im.domain.ContactInfo;

import java.util.List;
import java.util.Map;

public class ContactDao {
    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COLUMN_NAME_NICK = "nick";
    public static final String COLUMN_NAME_PHONE = "phone";
    public static final String COLUMN_NAME_ALIAS = "alias";
    public static final String COLUMN_NAME_AVATAR = "avatar";
    public static final String COLUMN_NAME_SEX = "sex";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_FRIENDS = "friends";
    public static final String COLUMN_NAME_EXTRA = "extra";
    public static final String COLUMN_NAME_USERID = "userid";
    public static final String COLUMN_NAME_IS_NEW = "isnew";
    public static final String COLUMN_NAME_IS_HAS_NEW = "ishasnew";
    public static final String COLUMN_NAME_LEVER = "lever";
    public static final String COLUMN_NAME_PROFILE_RATE = "profilerate";
    public static final String COLUMN_NAME_TA_USER_ID = "ta_user_id";
    public static final String COLUMN_NAME_TA_NICK = "ta_nick";
    public static final String COLUMN_NAME_GROUP_ROLE = "group_role";
    public static final String COLUMN_NAME_TEACHER_ROLE = "teacher_role";
    
    public static final String EXTRA_NAME_GROUP_ID = "group_id";
    public static final String EXTRA_NAME_GROUP_NICK = "nick";
    public static final String EXTRA_NAME_GROUP_ROLE = "role";
    
    public ContactDao(Context context) {}
    
    /**
     * 保存好友list
     */
    public void saveContactList(List<ContactInfo> contactList) {
        DemoDBManager.getInstance().saveContactList(contactList);
    }
    
    /**
     * 获取好友list
     */
    public Map<String, ContactInfo> getContactList() {
        
        return DemoDBManager.getInstance().getContactList();
    }
    
    /**
     * 删除一个联系人
     */
    public void deleteContact(String username) {
        DemoDBManager.getInstance().deleteContact(username);
    }
    
    /**
     * 保存一个联系人
     */
    public void saveContact(ContactInfo user) {
        DemoDBManager.getInstance().saveContact(user);
    }
    
}
