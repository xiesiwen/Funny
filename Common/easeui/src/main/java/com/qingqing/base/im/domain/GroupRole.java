package com.qingqing.base.im.domain;

import java.util.ArrayList;

/**
 * 用户在群组中的特殊身份，和 groupId 相关
 *
 * Created by lihui on 2017/6/8.
 */

public class GroupRole {
    /**
     * 群昵称
     */
    private String nick;
    /**
     * 群身份 {@link com.qingqing.api.proto.v1.UserProto.ChatGroupUserRole}
     */
    private ArrayList<Integer> role = new ArrayList<>();
    
    public String getNick() {
        return nick;
    }
    
    public GroupRole setNick(String nick) {
        this.nick = nick;
        return this;
    }
    
    public ArrayList<Integer> getRole() {
        return role;
    }
    
    public GroupRole setRole(ArrayList<Integer> role) {
        this.role.clear();
        this.role.addAll(role);
        return this;
    }
    
    public boolean isSame(String nick, int... role) {
        GroupRole groupRole = new GroupRole();
        groupRole.setNick(nick);
        ArrayList<Integer> roleArray = new ArrayList<>();
        for (int aRole : role) {
            roleArray.add(aRole);
        }
        groupRole.setRole(roleArray);
        
        return isSame(groupRole);
    }
    
    public boolean isSame(GroupRole groupRole) {
        if (groupRole == null) {
            return false;
        }
        
        // 昵称不同
        if (groupRole.getNick() == null && nick != null) {
            return false;
        }
        else if (!groupRole.getNick().equals(nick)) {
            return false;
        }
        
        // 角色不同
        if (groupRole.getRole().size() != role.size()) {
            return false;
        }
        for (Integer role1 : groupRole.getRole()) {
            if (!role.contains(role1)) {
                return false;
            }
        }
        
        return true;
    }
}
