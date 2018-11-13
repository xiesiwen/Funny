package com.qingqing.base.utils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.qingqing.base.log.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by wangxiaxin on 2017/6/5.
 *
 * 联系人的相关工具类
 */

public class ContactUtil {
    
    private static final String TAG = "ContactUtil";
    
    /**
     * Use a simple string represents the long.
     */
    private static final String COLUMN_CONTACT_ID = ContactsContract.Data.CONTACT_ID;
    private static final String COLUMN_RAW_CONTACT_ID = ContactsContract.Data.RAW_CONTACT_ID;
    private static final String COLUMN_MIMETYPE = ContactsContract.Data.MIMETYPE;
    private static final String COLUMN_NAME = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
    private static final String COLUMN_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String COLUMN_NUMBER_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
    private static final String COLUMN_EMAIL = ContactsContract.CommonDataKinds.Email.DATA;
    private static final String COLUMN_EMAIL_TYPE = ContactsContract.CommonDataKinds.Email.TYPE;
    private static final String MIMETYPE_STRING_NAME = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    private static final String MIMETYPE_STRING_PHONE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    private static final String MIMETYPE_STRING_EMAIL = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
    
    /**
     * 根据name获取联系人ID
     */
    public static String[] getContactIDByName(String name) {
        Cursor cursor = UtilsMgr.getCtx().getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[] { ContactsContract.Contacts._ID },
                ContactsContract.Contacts.DISPLAY_NAME + "='" + name + "'", null, null);
        if (cursor != null) {
            ArrayList<String> contactIDList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String id = cursor
                        .getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                contactIDList.add(id);
            }
            cursor.close();
            return contactIDList.toArray(new String[contactIDList.size()]);
        }
        
        return null;
    }
    
    /**
     * 名称为name的联系人是否存在
     */
    public static boolean isContactExist(String name) {
        String[] contactIDs = getContactIDByName(name);
        return contactIDs != null && contactIDs.length > 0;
    }
    
    /**
     * 联系人是否存在
     */
    public static boolean isContactExist(String name, String phone) {
        String[] contactIDs = getContactIDByName(name);
        if (contactIDs == null || contactIDs.length <= 0)
            return false;
        
        for (String contactID : contactIDs) {
            String rawContactId = getRawContactID(contactID);
            if (!TextUtils.isEmpty(rawContactId)) {
                // 对比此联系人详情的电话号码中，是否包含phone
                PhoneInfo[] phoneInfos = getPhone(rawContactId);
                if (phoneInfos == null || phoneInfos.length <= 0)
                    return false;
                for (PhoneInfo phoneInfo : phoneInfos) {
                    if (phoneInfo.number.replace(" ", "").equals(phone))
                        return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 根据 rawContactID 获取 电话号码
     */
    public static PhoneInfo[] getPhone(String rawContactID) {
        ContentResolver contentResolver = UtilsMgr.getCtx().getContentResolver();
        Cursor phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " = ?",
                new String[] { rawContactID }, null);
        if (phoneCursor != null) {
            
            ArrayList<PhoneInfo> resultList = new ArrayList<>();
            
            while (phoneCursor.moveToNext()) {
                // 获取号码
                String number = phoneCursor.getString(phoneCursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                // 获取号码类型
                int numberType = phoneCursor.getInt(phoneCursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                PhoneInfo info = new PhoneInfo(number, numberType);
                resultList.add(info);
            }
            
            phoneCursor.close();
            return resultList.toArray(new PhoneInfo[resultList.size()]);
        }
        
        return null;
    }
    
    /**
     * 根据contact id 获取 raw contact id
     */
    public static String getRawContactID(String contactID) {
        if (TextUtils.isEmpty(contactID))
            return "";
        ContentResolver contentResolver = UtilsMgr.getCtx().getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[] { ContactsContract.RawContacts._ID },
                ContactsContract.RawContacts.CONTACT_ID + " = ?",
                new String[] { contactID }, null);
        // 该查询结果一般只返回一条记录，所以我们直接让游标指向第一条记录
        if (cursor != null && cursor.moveToFirst()) {
            // 读取第一条记录的RawContacts._ID列的值
            String rawContactId = cursor
                    .getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
            cursor.close();
            return rawContactId;
        }
        return "";
    }
    
    private static boolean addContact(String name, PhoneInfo phoneInfo) {
        return addContact(name, new PhoneInfo[] { phoneInfo });
    }
    
    /**
     * You must specify the contact's ID.
     *
     * @param name
     *            名称
     * @param phone
     *            电话
     */
    private static boolean addContact(String name, String phone) {
        return addContact(name,
                new PhoneInfo(phone, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE));
    }
    
    private static int findIdlePhoneType(HashSet<Integer> existTypes) {
        if (existTypes == null || existTypes.isEmpty()) {
            return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
        }
        for (int type = 1; type < 20; type++) {
            if (!existTypes.contains(type))
                return type;
        }
        
        return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
    }
    
    /**
     * 找出existTypes中存在，但是updateTypes中没有的type， -1表示未找到
     */
    private static int findUpdatePhoneType(HashSet<Integer> existTypes,
            HashSet<Integer> updateTypes) {
        
        if (existTypes == null || existTypes.isEmpty()) {
            return -1;
        }
        
        if (updateTypes == null || updateTypes.isEmpty()) {
            return existTypes.iterator().next();
        }
        
        Iterator<Integer> existIt = existTypes.iterator();
        while (existIt.hasNext()) {
            int type = existIt.next();
            if (!updateTypes.contains(type)) {
                return type;
            }
        }
        
        return -1;
    }
    
    private static boolean addContact(String name, PhoneInfo[] phoneInfos) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        
        if (TextUtils.isEmpty(name) || phoneInfos == null || phoneInfos.length <= 0)
            return false;
        
        if (isContactExist(name)) {
            return false;
        }
        
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_NAME)
                .withValue(COLUMN_NAME, name).build());
        
        HashSet<Integer> currentTypeSet = new HashSet<>();
        for (PhoneInfo phoneInfo : phoneInfos) {
            if (!TextUtils.isEmpty(phoneInfo.number)) {
                if (phoneInfo.type <= 0) {
                    phoneInfo.type = findIdlePhoneType(currentTypeSet);
                }
                currentTypeSet.add(phoneInfo.type);
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                        .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_PHONE)
                        .withValue(COLUMN_NUMBER, phoneInfo.number)
                        .withValue(COLUMN_NUMBER_TYPE, phoneInfo.type).build());
            }
        }
        
        // if (!contact.getEmail().trim().equals("")) {
        // ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
        // .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
        // .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_EMAIL)
        // .withValue(COLUMN_EMAIL, contact.getEmail())
        // .withValue(COLUMN_EMAIL_TYPE, contact.getEmailType()).build());
        // Log.d(TAG, "add email: " + contact.getEmail());
        // }
        try {
            UtilsMgr.getCtx().getContentResolver()
                    .applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        } catch (Exception e) {
            Logger.w(e);
            return false;
        }
    }
    
    /**
     * Delete contacts who's name equals contact.getName();
     *
     * @param name
     */
    public static void deleteContactByName(String name) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String[] contactIDs = getContactIDByName(name);
        if (contactIDs != null && contactIDs.length > 0) {
            for (String contactID : contactIDs) {
                deleteContact(contactID);
            }
        }
    }
    
    public static boolean deleteContact(String contactId) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        if (!TextUtils.isEmpty(contactId)) {
            // delete contact
            ops.add(ContentProviderOperation
                    .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(
                            ContactsContract.RawContacts.CONTACT_ID + "=" + contactId,
                            null)
                    .build());
            // delete contact information such as phone number,email
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(COLUMN_CONTACT_ID + "=" + contactId, null).build());
            try {
                UtilsMgr.getCtx().getContentResolver()
                        .applyBatch(ContactsContract.AUTHORITY, ops);
                return true;
            } catch (Exception e) {
                Logger.w(e);
            }
        }
        return false;
    }
    
    public static void updateContact(String name, @NonNull PhoneInfo newPhone) {
        updateContact(name, null, new PhoneInfo[] { newPhone });
    }
    
    public static void updateContact(String name, @Nullable String newName,
            @NonNull PhoneInfo newPhone) {
        updateContact(name, newName, new PhoneInfo[] { newPhone });
    }
    
    public static void updateContact(String name, @NonNull PhoneInfo[] newPhones) {
        updateContact(name, null, newPhones);
    }
    
    /**
     * @param name
     *            The contact wants to be updated. The name should exists.
     * @param newName
     *            新的名称 新的联系人信息
     */
    public static void updateContact(String name, @Nullable String newName,
            @NonNull PhoneInfo[] newPhones) {
        
        if (TextUtils.isEmpty(name))
            return;
        
        String[] contactIDs = getContactIDByName(name);
        if (contactIDs == null || contactIDs.length <= 0) {
            addContact(!TextUtils.isEmpty(newName) ? newName : name, newPhones);
            return;
        }
        
        for (String contactID : contactIDs) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            
            // update name
            if (!TextUtils.isEmpty(newName)) {
                ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                                COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                                new String[] { contactID, MIMETYPE_STRING_NAME })
                        .withValue(COLUMN_NAME, newName).build());
            }
            
            String rawContactID = getRawContactID(contactID);
            PhoneInfo[] currentInfos = getPhone(rawContactID);
            HashSet<Integer> currentTypeSet = new HashSet<>();
            if (currentInfos != null && currentInfos.length > 0) {
                for (PhoneInfo info : currentInfos) {
                    currentTypeSet.add(info.type);
                }
            }
            
            HashSet<Integer> updateTypeSet = new HashSet<>();
            
            // update number
            for (PhoneInfo phoneInfo : newPhones) {
                if (!TextUtils.isEmpty(phoneInfo.number)) {
                    if (phoneInfo.type <= 0) {
                        // 优先更新现有的号码，多出来的部分，再新加
                        final int type = findUpdatePhoneType(currentTypeSet,
                                updateTypeSet);
                        if (type > 0) {
                            phoneInfo.type = type;
                            updateTypeSet.add(phoneInfo.type);
                            ops.add(ContentProviderOperation
                                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                                    .withSelection(
                                            COLUMN_RAW_CONTACT_ID + "=? AND "
                                                    + COLUMN_MIMETYPE + "=? AND "
                                                    + COLUMN_NUMBER_TYPE + " =?",
                                            new String[] { rawContactID,
                                                    MIMETYPE_STRING_PHONE,
                                                    String.valueOf(type) })
                                    .withValue(COLUMN_NUMBER, phoneInfo.number)
                                    .withValue(COLUMN_NUMBER_TYPE, phoneInfo.type)
                                    .build());
                        }
                        else {
                            phoneInfo.type = findIdlePhoneType(currentTypeSet);
                            currentTypeSet.add(phoneInfo.type);
                            updateTypeSet.add(phoneInfo.type);
                            // add 一个号码类型
                            ops.add(ContentProviderOperation
                                    .newInsert(ContactsContract.Data.CONTENT_URI)
                                    .withValue(COLUMN_RAW_CONTACT_ID, rawContactID)
                                    .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_PHONE)
                                    .withValue(COLUMN_NUMBER, phoneInfo.number)
                                    .withValue(COLUMN_NUMBER_TYPE, phoneInfo.type)
                                    .build());
                        }
                    }
                }
            }
            // update email if mail
            // if (false) {
            // ops.add(ContentProviderOperation
            // .newUpdate(ContactsContract.Data.CONTENT_URI)
            // .withSelection(
            // COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
            // new String[]{contactID, MIMETYPE_STRING_EMAIL})
            // .withValue(COLUMN_EMAIL, contactNew.getEmail())
            // .withValue(COLUMN_EMAIL_TYPE,
            // contactNew.getEmailType()).build());
            // }
            try {
                ContentResolver contentResolver = UtilsMgr.getCtx()
                        .getContentResolver();
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                Logger.w(TAG, e);
            }
        }
    }
    
    public static final class PhoneInfo {
        public String number;
        public int type;
        
        public PhoneInfo(String number) {
            this.number = number;
        }
        
        /**
         * @param type
         *            {@link ContactsContract.CommonDataKinds.Phone#TYPE_HOME}
         */
        public PhoneInfo(String number, int type) {
            this.number = number;
            this.type = type;
        }
    }
}
