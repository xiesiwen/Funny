package com.easemob.easeui.model;

import android.text.TextUtils;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.utils.EaseUserUtils;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.im.domain.ContactInfo;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EaseAtMessageHelper {
    private final List<String> toAtUserList = new ArrayList<>();
    private Set<String> atMeGroupList = null;
    private static EaseAtMessageHelper instance = null;
    
    public synchronized static EaseAtMessageHelper get() {
        if (instance == null) {
            instance = new EaseAtMessageHelper();
        }
        return instance;
    }
    
    private EaseAtMessageHelper() {
        atMeGroupList = EasePreferenceManager.getInstance().getAtMeGroups();
        if (atMeGroupList == null)
            atMeGroupList = new HashSet<>();
        
    }
    
    /**
     * add user you want to @
     * 
     * @param username
     */
    public void addAtUser(String username) {
        synchronized (toAtUserList) {
            if (!toAtUserList.contains(username)) {
                toAtUserList.add(username);
            }
        }
    }
    
    public void removeAtUser(String username) {
        synchronized (toAtUserList) {
            if (toAtUserList.contains(username)) {
                toAtUserList.remove(username);
            }
        }
    }
    
    /**
     * check if be mentioned(@) in the content
     * 
     * @param content
     * @param groupId
     * @return
     */
    public boolean containsAtUsername(String content, String groupId) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(groupId)) {
            return false;
        }
        synchronized (toAtUserList) {
            Iterator iterator = toAtUserList.iterator();
            while (iterator.hasNext()) {
                String username = (String) iterator.next();
                String nick = null;
                if (EaseUserUtils.getUserInfo(username) != null) {
                    ContactInfo user = EaseUserUtils.getUserInfo(username);
                    if (user != null && user.getGroupRole(groupId) != null) {
                        nick = user.getGroupRole(groupId).getNick();
                    }
                }
                if (nick == null || !content.contains(nick)) {
                    iterator.remove();
                }
            }
        }
        return toAtUserList.size() > 0;
    }
    
    public boolean containsAtAll(String content) {
        String atAll = "@" + BaseApplication.getCtx().getString(R.string.all_members);
        if (content.contains(atAll)) {
            return true;
        }
        return false;
    }
    
    /**
     * get the users be mentioned(@)
     * 
     * @param content
     * @param groupId
     * @return
     */
    public List<String> getAtMessageUsernames(String content, String groupId) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(groupId)) {
            return null;
        }
        synchronized (toAtUserList) {
            List<String> list = null;
            for (String username : toAtUserList) {
                String nick = null;
                if (EaseUserUtils.getUserInfo(username) != null) {
                    ContactInfo user = EaseUserUtils.getUserInfo(username);
                    if (user != null && user.getGroupRole(groupId) != null) {
                        nick = user.getGroupRole(groupId).getNick();
                    }
                }
                if (nick != null && content.contains(nick)) {
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(username);
                }
            }
            return list;
        }
    }
    
    /**
     * parse the message, get and save group id if I was mentioned(@)
     * 
     * @param message
     */
    public void parseMessages(EMMessage message) {
        int size = atMeGroupList.size();
        if (message.getChatType() == EMMessage.ChatType.GroupChat) {
            String groupId = message.getTo();
            try {
                JSONArray jsonArray = message
                        .getJSONArrayAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String username = jsonArray.getString(i);
                    if (EMChatManager.getInstance().getCurrentUser().equals(username)) {
                        if (!atMeGroupList.contains(groupId)) {
                            atMeGroupList.add(groupId);
                            break;
                        }
                    }
                }
            } catch (Exception e1) {
                // Determine whether is @ all message
                String usernameStr = message
                        .getStringAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, null);
                if (usernameStr != null) {
                    if (usernameStr.toUpperCase()
                            .equals(EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL)) {
                        if (!atMeGroupList.contains(groupId)) {
                            atMeGroupList.add(groupId);
                        }
                    }
                }
            }
            
            if (atMeGroupList.size() != size) {
                EasePreferenceManager.getInstance().setAtMeGroups(atMeGroupList);
            }
        }
    }
    
    /**
     * get groups which I was mentioned
     * 
     * @return
     */
    public Set<String> getAtMeGroups() {
        return atMeGroupList;
    }
    
    /**
     * remove group from the list
     * 
     * @param groupId
     */
    public void removeAtMeGroup(String groupId) {
        if (atMeGroupList.contains(groupId)) {
            atMeGroupList.remove(groupId);
            EasePreferenceManager.getInstance().setAtMeGroups(atMeGroupList);
        }
    }
    
    /**
     * check if the input groupId in atMeGroupList
     * 
     * @param groupId
     * @return
     */
    public boolean hasAtMeMsg(String groupId) {
        return atMeGroupList.contains(groupId);
    }
    
    public boolean isAtMeMsg(EMMessage message) {
        ContactInfo user = EaseUserUtils.getUserInfo(message.getFrom());
        if (user != null) {
            try {
                JSONArray jsonArray = message
                        .getJSONArrayAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG);
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    String username = jsonArray.getString(i);
                    if (username.equals(EMChatManager.getInstance().getCurrentUser())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // perhaps is a @ all message
                String atUsername = message
                        .getStringAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, null);
                if (atUsername != null) {
                    if (atUsername.toUpperCase()
                            .equals(EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL)) {
                        return true;
                    }
                }
                return false;
            }
            
        }
        return false;
    }
    
    public ArrayList<String> getMsgAtUserList(EMMessage message) {
        ArrayList<String> atUserList = new ArrayList<>();
        try {
            JSONArray jsonArray = message
                    .getJSONArrayAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                String username = jsonArray.getString(i);
                atUserList.add(username);
            }
        } catch (Exception e) {
            // TODO: ALL 时需要拉取群组列表？
            // perhaps is a @ all message
            // String atUsername = message
            // .getStringAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, null);
            // if (atUsername != null) {
            // if (atUsername.toUpperCase()
            // .equals(EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL)) {
            //
            // }
            // }
        }
        
        return atUserList;
    }
    
    public JSONArray atListToJsonArray(List<String> atList) {
        JSONArray jArray = new JSONArray();
        if (atList != null) {
            int size = atList.size();
            for (int i = 0; i < size; i++) {
                String username = atList.get(i);
                jArray.put(username);
            }
        }
        return jArray;
    }
    
    public void cleanToAtUserList() {
        synchronized (toAtUserList) {
            toAtUserList.clear();
        }
    }

    public List<String> getToAtUserList(){
        return toAtUserList;
    }
}
