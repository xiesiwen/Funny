package com.qingqing.base.im;

import android.content.Context;

import com.qingqing.base.im.db.ContactDao;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangming on 2015/12/23.
 */
public class ContactModel extends DataModel {

    private ConcurrentHashMap<String, ContactInfo> contactList = new ConcurrentHashMap<String, ContactInfo>();

    private ContactDao userDao;

    ContactModel(Context context) {
        super(context);

        userDao = new ContactDao(context.getApplicationContext());
    }

    public ContactInfo getContactInfo(String userName) {
        if (userName == null) {
            return null;
        }
        return contactList.get(userName);
    }
    
    public List<ContactInfo> getFriendsByType(ContactInfo.Type type) {
        List<ContactInfo> contacts = new ArrayList<>();
        if (contactList.size() > 0) {
            Iterator iterator = contactList.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                ContactInfo contactInfo = (ContactInfo) entry.getValue();
                if (contactInfo != null && contactInfo.getType() == type
                        && contactInfo.isFriends()) {
                    contacts.add(contactInfo);
                }
            }
        }
        return contacts;
    }

    void init() {
        super.init();
        runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                Map<String, ContactInfo> contacts = userDao.getContactList();
                if (contacts != null) {
                    contactList.clear();
                    contactList.putAll(contacts);
                }
            }
        });
    }

    public void saveContact(final ContactInfo contactInfo) {
        contactList.put(contactInfo.getUsername(), contactInfo);
        runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                userDao.saveContact(contactInfo);
            }
        });
    }

    public void saveContactList(final List<ContactInfo> contacts) {
        if (contacts != null && contacts.size() > 0) {
            for (ContactInfo contactInfo : contacts) {
                contactList.put(contactInfo.getUsername(), contactInfo);
            }
            runOnWorkerThread(new Runnable() {
                @Override
                public void run() {
                    userDao.saveContactList(contacts);
                }
            });
        }
    }

    public void deleteContact(final String userName) {
        contactList.remove(userName);
        runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                userDao.deleteContact(userName);
            }
        });
    }

    public void deleteContact(final ContactInfo contactInfo) {
        deleteContact(contactInfo.getUsername());
    }

    public void deleteContactList(List<ContactInfo> contacts) {
        if (contacts != null && contacts.size() > 0) {
            final ArrayList<String> userNames = new ArrayList<String>();
            for (ContactInfo contactInfo : contacts) {
                String userName = contactInfo.getUsername();
                userNames.add(userName);
                contactList.remove(userName);
            }
            runOnWorkerThread(new Runnable() {
                @Override
                public void run() {
                    for (String userName : userNames) {
                        userDao.deleteContact(userName);
                    }
                }
            });
        }
    }

    Map<String, ContactInfo> getContactList() {
        return contactList;
    }

    @Override
    public boolean isSynced() {
        return PreferenceManager.getInstance().isContactSynced();
    }

    @Override
    public void setSynced(boolean isSynced) {
        PreferenceManager.getInstance().setContactSynced(isSynced);
    }

    void reset() {
        super.reset();
        contactList.clear();
    }
}
