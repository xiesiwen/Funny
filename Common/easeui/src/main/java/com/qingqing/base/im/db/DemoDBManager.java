package com.qingqing.base.im.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.DemoApplication;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.domain.GroupRole;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DemoDBManager {
    private volatile static DemoDBManager dbMgr;
    private DbOpenHelper dbHelper;

    private DemoDBManager() {
        dbHelper = DbOpenHelper.getInstance(DemoApplication.getCtx().getApplicationContext());
    }

    public static synchronized DemoDBManager getInstance() {
        if (dbMgr == null) {
            synchronized (DemoDBManager.class) {
                if (dbMgr == null) {
                    dbMgr = new DemoDBManager();
                }
            }
        }
        return dbMgr;
    }

    /**
     * 保存好友list
     */
    synchronized public void saveContactList(List<ContactInfo> contactList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            for (ContactInfo user : contactList) {
                saveContact(user);
            }
        }
    }

    /**
     * 获取好友list
     */
    synchronized public Map<String, ContactInfo> getContactList() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Map<String, ContactInfo> users = new HashMap<String, ContactInfo>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + ContactDao.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                String username = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_USERNAME));
                String nick = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_NICK));
                String avatar = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_AVATAR));
                String type = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_TYPE));
                String alias = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_ALIAS));
                String extra = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_EXTRA));
                int friends = cursor.getInt(cursor.getColumnIndex(ContactDao.COLUMN_NAME_FRIENDS));
                int sex = cursor.getInt(cursor.getColumnIndex(ContactDao.COLUMN_NAME_SEX));
                long userId = cursor.getLong(cursor.getColumnIndex(ContactDao.COLUMN_NAME_USERID));
                String phone = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_PHONE));
                String lever = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_LEVER));
                String rate = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_PROFILE_RATE));
                String isNew = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_IS_NEW));
                String isHasNew = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_IS_HAS_NEW));
                // 5.6.5版本
                String taUserId = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_TA_USER_ID));
                String taNick = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_TA_NICK));
                String groupRoleString = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_GROUP_ROLE));
                String teacherRoleString = cursor.getString(cursor.getColumnIndex(ContactDao.COLUMN_NAME_TEACHER_ROLE));
                ContactInfo user = new ContactInfo(username, friends, sex, type);
                user.setNick(nick);
                user.setAlias(alias);
                user.setAvatar(avatar);
                user.setExtra(extra);
                user.setPhone(phone);
                user.setUserId(userId);
                user.setLever(lever);
                user.setHasNew(Boolean.parseBoolean(isHasNew));
                user.setTaNick(taNick);
                user.setTaUserID(taUserId);
                if (!TextUtils.isEmpty(groupRoleString)) {
                    try {
                        JSONArray roleArray = new JSONArray(groupRoleString);
                        
                        for (int i = 0; i < roleArray.length(); i++) {
                            JSONObject object = (JSONObject) roleArray.get(i);
                            String groupId = object
                                    .optString(ContactDao.EXTRA_NAME_GROUP_ID);
                            String groupNick = object
                                    .optString(ContactDao.EXTRA_NAME_GROUP_NICK);
                            String groupRole = object
                                    .optString(ContactDao.EXTRA_NAME_GROUP_ROLE);
                            String[] rolesString = groupRole.split(",");
                            ArrayList<Integer> roles = new ArrayList<>();
                            for (int j = 0; j < rolesString.length; j++) {
                                roles.add(Integer.valueOf(rolesString[j]));
                            }
                            user.setGroupRole(groupId, groupNick, roles);
                            
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!TextUtils.isEmpty(teacherRoleString)) {
                    String[] rolesString = teacherRoleString.split(",");
                    ArrayList<Integer> roles = new ArrayList<>();
                    for (int j = 0; j < rolesString.length; j++) {
                        roles.add(Integer.valueOf(rolesString[j]));
                    }
                    user.setTeacherRole(roles);
                }
                
                if (!TextUtils.isEmpty(rate)) {
                    if (rate.endsWith(".0")) {
                        String r = rate.substring(0, rate.length() - 2);
                        user.setProfileComplete(Long.parseLong(r));
                    }
                }
                else {
                    user.setProfileComplete(0L);
                }
                
                user.setNew(Boolean.parseBoolean(isNew));
                users.put(username, user);
            }
            cursor.close();
        }
        return users;
    }

    /**
     * 删除一个联系人
     */
    synchronized public void deleteContact(String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(ContactDao.TABLE_NAME, ContactDao.COLUMN_NAME_USERNAME + " = ?", new String[]{username});
        }
    }

    /**
     * 保存一个联系人
     */
    synchronized public void saveContact(ContactInfo user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactDao.COLUMN_NAME_USERNAME, user.getUsername());
        values.put(ContactDao.COLUMN_NAME_NICK, user.getNick());
        values.put(ContactDao.COLUMN_NAME_AVATAR, user.getAvatar());
        values.put(ContactDao.COLUMN_NAME_ALIAS, user.getAlias());
        values.put(ContactDao.COLUMN_NAME_TYPE, user.getType().name());
        values.put(ContactDao.COLUMN_NAME_FRIENDS, user.getFriends());
        values.put(ContactDao.COLUMN_NAME_SEX, user.getSex());
        values.put(ContactDao.COLUMN_NAME_EXTRA, user.getExtra());
        values.put(ContactDao.COLUMN_NAME_LEVER,user.getLever());
        values.put(ContactDao.COLUMN_NAME_PROFILE_RATE,user.getProfileComplete());
        values.put(ContactDao.COLUMN_NAME_IS_NEW,user.isNew());
        values.put(ContactDao.COLUMN_NAME_IS_HAS_NEW,user.isHasNew());
        // 4.9.5版本不保存电话到数据库
        // TA需要存放在数据库
        if(BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
            values.put(ContactDao.COLUMN_NAME_PHONE, user.getPhone());
        }
        values.put(ContactDao.COLUMN_NAME_USERID, user.getUserId());

        // 5.6.5版本
        values.put(ContactDao.COLUMN_NAME_TA_NICK,user.getTaNick());
        values.put(ContactDao.COLUMN_NAME_TA_USER_ID,user.getTaUserID());

        // 5.8.0版本增加群组角色
        JSONArray groupRoleArray = new JSONArray();
        Iterator iterator = user.getGroupRole().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            JSONObject object = new JSONObject();
            try {
                object.put(ContactDao.EXTRA_NAME_GROUP_ID, entry.getKey());
                GroupRole groupRole = (GroupRole) entry.getValue();
                object.put(ContactDao.EXTRA_NAME_GROUP_NICK, groupRole.getNick());
                String roleValue = "";
                for (Integer role : groupRole.getRole()) {
                    roleValue += role + ",";
                }
                if (roleValue.endsWith(",")) {
                    roleValue = roleValue.substring(0, roleValue.length() - 1);
                }
                object.put(ContactDao.EXTRA_NAME_GROUP_ROLE, roleValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            groupRoleArray.put(object);
        }
        values.put(ContactDao.COLUMN_NAME_GROUP_ROLE, groupRoleArray.toString());

        // 5.9.7版本增加trm身份
        ArrayList role = user.getTeacherRole();
        String roleValue = "";
        if (role != null) {
            for (int i = 0; i < role.size(); i++) {
                roleValue += role.get(i) + ",";
            }
            if (roleValue.endsWith(",")) {
                roleValue = roleValue.substring(0, roleValue.length() - 1);
            }
        }
        values.put(ContactDao.COLUMN_NAME_TEACHER_ROLE, roleValue);

        if (db.isOpen()) {
            db.replace(ContactDao.TABLE_NAME, null, values);
        }
    }

    synchronized public void closeDB() {
        if (dbHelper != null) {
            dbHelper.closeDB();
        }
        dbMgr = null;
    }


}