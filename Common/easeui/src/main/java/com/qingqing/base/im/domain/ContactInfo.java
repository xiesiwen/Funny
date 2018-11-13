package com.qingqing.base.im.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.qingqing.api.proto.base.TeacherCommonProto;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ContactInfo implements Parcelable {
    
    public static final int RELATION_FRIENDS = 1;
    public static final int RELATION_STRANGER = 0;
    public static final int RELATION_SELF = -1;
    
    /**
     * qingqing userid
     */
    private String username;
    private String nick;
    private String alias;
    // 0:女 1 :男, 2 unknown
    private int sex;
    private Type type;
    // > 0:好友, 0:陌生人 -1 self
    private int friends;
    private String phone;
    private long userId;
    
    private String lever;
    private long profileComplete;
    
    private boolean isNew;
    
    private boolean isHasNew;
    
    private boolean isChoose = false;
    
    private String taUserID;// 家长的助教qingqingUserId，565新增
    
    private String taNick;// 家长的助教nick,565新增
    
    /**
     * {@link com.qingqing.api.proto.base.TeacherCommonProto.TeacherTeachingResearchRoleType}
     */
    private ArrayList<Integer> teacherRole = new ArrayList<>(); // 597 新增，老师的 trm 信息
    
    ConcurrentHashMap<String, GroupRole> groupRole = new ConcurrentHashMap<>();
    
    protected JSONObject extraJson = new JSONObject();
    
    /**
     * 昵称首字母
     */
    private String initialLetter;
    /**
     * 用户头像
     */
    private String avatar;
    
    public ContactInfo(String username, int friends, int sex, String type) {
        this(username, friends, sex, Type.valueOf(type));
    }
    
    /*
     * public ContactInfo(String username, int friends, int sexType, Type type) {
     * this(username, friends, 0, type); switch (sexType) { case
     * UserProto.SexType.female: sex = 0; break; case UserProto.SexType.male: sex =
     * 1; break; case UserProto.SexType.unknown: sex = 2; break; } }
     */
    
    public ContactInfo(String username, int friends, int sex, Type type) {
        this.username = username;
        this.friends = friends;
        this.sex = sex;
        this.type = type;
    }
    
    protected ContactInfo(Parcel in) {
        username = in.readString();
        nick = in.readString();
        initialLetter = in.readString();
        avatar = in.readString();
        alias = in.readString();
        type = Type.valueOf(in.readString());
        friends = in.readInt();
        sex = in.readInt();
        String extra = in.readString();
        phone = in.readString();
        userId = in.readLong();
        lever = in.readString();
        profileComplete = in.readLong();
        taNick = in.readString();
        taUserID = in.readString();
        teacherRole = in.readArrayList(Integer.class.getClassLoader());
        try {
            extraJson = new JSONObject(extra);
        } catch (Exception e) {
            e.printStackTrace();
            extraJson = new JSONObject();
        }
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(nick);
        dest.writeString(initialLetter);
        dest.writeString(avatar);
        dest.writeString(alias);
        dest.writeString(type.name());
        dest.writeInt(friends);
        dest.writeInt(sex);
        dest.writeString(extraJson.toString());
        dest.writeString(phone);
        dest.writeLong(userId);
        dest.writeString(lever);
        dest.writeLong(profileComplete);
        dest.writeString(taNick);
        dest.writeString(taUserID);
        dest.writeList(teacherRole);
    }
    
    public static final Creator<ContactInfo> CREATOR = new Creator<ContactInfo>() {
        @Override
        public ContactInfo createFromParcel(Parcel in) {
            return new ContactInfo(in);
        }
        
        @Override
        public ContactInfo[] newArray(int size) {
            return new ContactInfo[size];
        }
    };
    
    public ConcurrentHashMap<String, GroupRole> getGroupRole() {
        return groupRole;
    }
    
    public GroupRole getGroupRole(String groupId) {
        if (groupRole != null && groupRole.get(groupId) != null) {
            return groupRole.get(groupId);
        }
        return null;
    }
    
    public ContactInfo setGroupRole(ConcurrentHashMap<String, GroupRole> groupRole) {
        this.groupRole.clear();
        this.groupRole.putAll(groupRole);
        return this;
    }
    
    public ContactInfo setGroupRole(String groupId, String nick,
            ArrayList<Integer> groupRole) {
        GroupRole role = new GroupRole();
        role.setNick(nick);
        role.setRole(groupRole);
        this.groupRole.put(groupId, role);
        return this;
    }
    
    public String getInitialLetter() {
        return initialLetter;
    }
    
    public void setInitialLetter(String initialLetter) {
        this.initialLetter = initialLetter;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public ContactInfo setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }
    
    public ContactInfo setAlias(String alias) {
        this.alias = alias;
        return this;
    }
    
    public ContactInfo setLever(String lever) {
        this.lever = lever;
        return this;
    }
    
    public boolean isNew() {
        return isNew;
    }
    
    public ContactInfo setNew(boolean isNew) {
        this.isNew = isNew;
        return this;
    }
    
    public ContactInfo setHasNew(boolean isHasNew) {
        this.isHasNew = isHasNew;
        return this;
    }
    
    public boolean isHasNew() {
        return isHasNew;
    }
    
    public ContactInfo setProfileComplete(long profileComplete) {
        this.profileComplete = profileComplete;
        return this;
    }
    
    public String getLever() {
        return lever;
    }
    
    public double getProfileComplete() {
        return profileComplete;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public Type getType() {
        return type;
    }
    
    public int getSex() {
        return sex;
    }
    
    public void setSex(int sex) {
        this.sex = sex;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public ContactInfo setNick(String nick) {
        this.nick = nick;
        return this;
    }
    
    public String getNick() {
        return this.nick == null ? this.username : this.nick;
    }
    
    public boolean isFriends() {
        return friends == RELATION_FRIENDS;
    }
    
    public int getFriends() {
        return friends;
    }
    
    public void setFriends(int friends) {
        this.friends = friends;
    }
    
    public String getExtra() {
        return extraJson.toString();
    }
    
    public ContactInfo setExtra(String extra) {
        try {
            extraJson = new JSONObject(extra);
        } catch (Exception e) {
            e.printStackTrace();
            extraJson = new JSONObject();
        }
        return this;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public ContactInfo setPhone(String phone) {
        this.phone = phone;
        return this;
    }
    
    public ContactInfo setUserId(long userId) {
        this.userId = userId;
        return this;
    }
    
    public String getTaNick() {
        return taNick;
    }
    
    public void setTaNick(String taNick) {
        this.taNick = taNick;
    }
    
    public String getTaUserID() {
        return taUserID;
    }
    
    public void setTaUserID(String taUserID) {
        this.taUserID = taUserID;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public boolean isChoose() {
        return isChoose;
    }
    
    public void setIsChoose(boolean isChoose) {
        this.isChoose = isChoose;
    }
    
    public ArrayList<Integer> getTeacherRole() {
        return teacherRole;
    }
    
    public int getHighestTeacherRole() {
        if (teacherRole == null || teacherRole.size() <= 0) {
            return TeacherCommonProto.TeacherTeachingResearchRoleType.normal_teaching_research_role_type;
        }
        
        int result = TeacherCommonProto.TeacherTeachingResearchRoleType.normal_teaching_research_role_type;
        for (int i = 0; i < teacherRole.size(); i++) {
            if (TeacherCommonProto.TeacherTeachingResearchRoleType.trm_teaching_research_role_type == teacherRole
                    .get(i)) {
                result = teacherRole.get(i);
            }
            else if (TeacherCommonProto.TeacherTeachingResearchRoleType.trmt_teaching_research_role_type == teacherRole
                    .get(i)
                    && TeacherCommonProto.TeacherTeachingResearchRoleType.trm_teaching_research_role_type != result) {
                result = teacherRole.get(i);
            }
        }
        
        return result;
    }
    
    public ContactInfo setTeacherRole(List<Integer> teacherRole) {
        if (this.teacherRole == null) {
            this.teacherRole = new ArrayList<>();
        }
        else {
            this.teacherRole.clear();
        }
        
        if (teacherRole != null) {
            this.teacherRole.addAll(teacherRole);
        }
        return this;
    }
    
    @Override
    public int hashCode() {
        return 17 * getUsername().hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ContactInfo)) {
            return false;
        }
        return getUsername().equals(((ContactInfo) o).getUsername());
    }
    
    @Override
    public String toString() {
        return nick == null ? username : nick;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public enum Type {
        Unknown, Teacher, Student, Assistant, Self
    }
    
}
