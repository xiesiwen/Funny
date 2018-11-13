package com.qingqing.base.nim.domain;

import android.text.TextUtils;

import com.qingqing.base.im.domain.ExtField;
import com.qingqing.base.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by huangming on 2016/8/19.
 */
public class MessageParser {
    
    public static final String TAG = "MessageParser";
    
    public static final String ATTR_CHAT_TYPE = "chat_type";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_MSG_ID = "msg_id";
    public static final String ATTR_FROM = "from";
    public static final String ATTR_TIMESTAMP = "timestamp";
    public static final String ATTR_PAYLOAD = "payload";
    public static final String ATTR_BODIES = "bodies";
    public static final String ATTR_TO = "to";
    public static final String ATTR_MSG = "msg";
    public static final String ATTR_URL = "url";
    public static final String ATTR_MEDIA_ID = "media_id";
    public static final String ATTR_LOCALURL = "localurl";
    public static final String ATTR_THUMB_LOCALURL = "thumblocalurl";
    public static final String ATTR_FILENAME = "filename";
    public static final String ATTR_THUMBNAIL = "thumb";
    public static final String ATTR_SECRET = "secret";
    public static final String ATTR_SIZE = "size";
    public static final String ATTR_IMG_WIDTH = "width";
    public static final String ATTR_IMG_HEIGHT = "height";
    public static final String ATTR_THUMBNAIL_SECRET = "thumb_secret";
    public static final String ATTR_LENGTH = "length";
    public static final String ATTR_ADDRESS = "addr";
    public static final String ATTR_LATITUDE = "lat";
    public static final String ATTR_LONGITUDE = "lng";
    public static final String ATTR_ACTION = "action";
    public static final String ATTR_PARAM = "param";
    public static final String ATTR_FILE_LENGTH = "file_length";
    public static final String ATTR_EXT = "ext";
    
    public static final String ATTR_CMD_TYPE = "t";
    
    public static String getSendBodyBy(Message message) throws JSONException {
        JSONObject bodyJson = new JSONObject();
        Message.Type msgType = message.getMsgType();
        bodyJson.put(ATTR_TYPE, msgType.getValue());
        switch (message.getMsgType()) {
            case TEXT:
                TextMessageBody txtBody = (TextMessageBody) message.getBody();
                bodyJson.put(ATTR_MSG, txtBody.getText());
                break;
            case IMAGE:
                ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
                bodyJson.put(ATTR_URL, imageBody.getRemoteUrl());
                bodyJson.put(ATTR_THUMBNAIL, imageBody.getThumbnailUrl());
                bodyJson.put(ATTR_FILENAME, imageBody.getFileName());
                JSONObject sizeJson = new JSONObject();
                sizeJson.put(ATTR_IMG_WIDTH, imageBody.getWidth());
                sizeJson.put(ATTR_IMG_HEIGHT, imageBody.getHeight());
                bodyJson.put(ATTR_SIZE, sizeJson);
                break;
            case AUDIO:
                AudioMessageBody audioBody = (AudioMessageBody) message.getBody();
                bodyJson.put(ATTR_URL, audioBody.getRemoteUrl());
                bodyJson.put(ATTR_LENGTH, audioBody.getLength());
                bodyJson.put(ATTR_FILENAME, audioBody.getFileName());
                bodyJson.put(ATTR_MEDIA_ID, audioBody.getMediaId());
                break;
            case UNKNOWN:
            default:
                break;
            
        }
        return bodyJson.toString();
    }
    
    public static String getSendExtBy(Message message) throws JSONException {
        JSONObject extJson = new JSONObject();
        if (message.hasPlayedRole()) {
            JSONObject fromUserInfoJson = new JSONObject();
            ArrayList<Integer> rolType = message.getRole().getRoleType();
            JSONArray roleTypeArray = new JSONArray();
            for (int i = 0; i < rolType.size(); i++) {
                roleTypeArray.put(rolType.get(i));
            }
            fromUserInfoJson.put(ExtField.Attr.CHAT_ROOM_AUTH, rolType.get(0));
            fromUserInfoJson.put(ExtField.Attr.CHAT_ROOM_AUTH_V2, roleTypeArray);
            fromUserInfoJson.put(ExtField.Attr.HEAD_IMG,
                    message.getRole().getHeadImage());
            fromUserInfoJson.put(ExtField.Attr.QINGQING_USER_ID,
                    message.getRole().getRoleId());
            fromUserInfoJson.put(ExtField.Attr.USER_TYPE,
                    message.getRole().getUserType());
            fromUserInfoJson.put(ExtField.Attr.NICK, message.getRole().getRoleNick());
            fromUserInfoJson.put(ExtField.Attr.SEX_TYPE,
                    message.getRole().getRoleSexType());
            extJson.put(ExtField.Attr.FROM_USER_INFO, fromUserInfoJson);
        }
        Map<String, Object> attributes = message.getAttributes();
        if (attributes != null) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                extJson.put(entry.getKey(), entry.getValue());
            }
        }
        return extJson.toString();
    }
    
    public static Message getMessageBy(String msgString, String msgId)
            throws JSONException {
        JSONObject msgJson = new JSONObject(msgString);
        
        if (TextUtils.isEmpty(msgId)) {
            Logger.e(TAG, "msgId is null!!!");
            return null;
        }
        
        String from = msgJson.optString(ATTR_FROM);
        if (TextUtils.isEmpty(from)) {
            Logger.e(TAG, "from is null!!!");
            return null;
        }
        String to = msgJson.optString(ATTR_TO);
        if (TextUtils.isEmpty(to)) {
            Logger.e(TAG, "to is null!!!");
            return null;
        }
        
        ChatType chatType = ChatType.mapStringToValue(msgJson.optString(ATTR_CHAT_TYPE));
        if (chatType == null) {
            Logger.e(TAG, "ChatType(" + msgJson.optString(ATTR_CHAT_TYPE)
                    + " )is not supported!!!");
            return null;
        }
        JSONObject payloadJson = msgJson.getJSONObject(ATTR_PAYLOAD);
        JSONObject extJson = payloadJson.optJSONObject(ATTR_EXT);
        JSONArray bodyArray = payloadJson.getJSONArray(ATTR_BODIES);
        JSONObject bodyJson = bodyArray.length() > 0 ? bodyArray.getJSONObject(0) : null;
        if (bodyJson == null) {
            Logger.e(TAG, "wrong msg(" + msgId + ") without body");
            return null;
        }
        Message.Type msgType = Message.Type
                .mapStringToValue(bodyJson.optString(ATTR_TYPE));
        if (msgType == Message.Type.UNKNOWN) {
            Logger.w(TAG, "MessageType(" + bodyJson.optString(ATTR_TYPE)
                    + ") is not supported");
        }
        
        Message message = new Message(msgId, chatType, msgType);
        
        message.setDirect(from.equals(ChatManager.getInstance().getCurrentUserId())
                ? Message.Direct.SEND
                : Message.Direct.RECEIVE);
        message.setStatus(Message.Status.SUCCESS);
        
        MessageBody msgBody = createMessageBody(msgType, bodyJson, extJson);
        message.setBody(msgBody);
        
        message.setMsgTime(msgJson.optLong(ATTR_TIMESTAMP));
        
        ChatContact fromContact = new ChatContact(from);
        message.setFromContact(fromContact);
        
        ChatContact toContact = new ChatContact(to);
        message.setToContact(toContact);
        
        if (payloadJson.has(ATTR_EXT) && extJson.has(ExtField.Attr.FROM_USER_INFO)) {
            JSONObject fromUserInfoJson = extJson
                    .getJSONObject(ExtField.Attr.FROM_USER_INFO);
            String userId = fromUserInfoJson.optString(ExtField.Attr.QINGQING_USER_ID);
            JSONArray rolTypeArray = fromUserInfoJson
                    .optJSONArray(ExtField.Attr.CHAT_ROOM_AUTH_V2);
            ArrayList<Integer> roleType = new ArrayList<>();
            if (rolTypeArray == null) {
                roleType.add(fromUserInfoJson.optInt(ExtField.Attr.CHAT_ROOM_AUTH));
            }
            else {
                for (int i = 0; i < rolTypeArray.length(); i++) {
                    roleType.add(rolTypeArray.getInt(i));
                }
            }
            int sexType = fromUserInfoJson.optInt(ExtField.Attr.SEX_TYPE);
            String headImage = fromUserInfoJson.optString(ExtField.Attr.HEAD_IMG);
            String nick = fromUserInfoJson.optString(ExtField.Attr.NICK);
            int userType = fromUserInfoJson.optInt(ExtField.Attr.USER_TYPE);
            ChatContact roleUser = new ChatContact(userId);
            roleUser.setNick(nick);
            roleUser.setUserType(userType);
            roleUser.setHeadImage(headImage);
            roleUser.setSexType(sexType);
            ChatRole role = new ChatRole(roleType, roleUser);
            message.setRole(role);
        }
        int extLength = extJson != null ? extJson.length() : 0;
        if (extLength > 0) {
            Iterator<String> iterator = extJson.keys();
            Map<String, Object> attributes = new HashMap<>(extLength);
            while (iterator.hasNext()) {
                String key = iterator.next();
                attributes.put(key, extJson.get(key));
            }
            message.setAttributes(attributes);
        }
        
        return message;
    }
    
    private static MessageBody createMessageBody(Message.Type msgType,
            JSONObject bodyJson, JSONObject extJson) throws JSONException {
        switch (msgType) {
            case TEXT:
                return createTextMessageBody(bodyJson);
            case AUDIO:
                return createAudioMessageBody(bodyJson);
            case IMAGE:
                return createImageMessageBody(bodyJson);
            case CMD:
                return createCmdMessageBody(
                        new JSONObject(extJson.optString(ATTR_ACTION)));
            default:
                return new UnkwonMessageBody();
        }
    }
    
    private static TextMessageBody createTextMessageBody(JSONObject bodyJson)
            throws JSONException {
        return new TextMessageBody(bodyJson.optString(ATTR_MSG));
    }
    
    private static ImageMessageBody createImageMessageBody(JSONObject bodyJson)
            throws JSONException {
        JSONObject sizeJson = bodyJson.optJSONObject(ATTR_SIZE);
        return new ImageMessageBody(bodyJson.optString(ATTR_URL),
                bodyJson.optString(ATTR_THUMBNAIL),
                bodyJson.has(ATTR_SIZE) ? sizeJson.optInt(ATTR_IMG_WIDTH) : 0,
                bodyJson.has(ATTR_SIZE) ? sizeJson.optInt(ATTR_IMG_HEIGHT) : 0);
    }
    
    private static AudioMessageBody createAudioMessageBody(JSONObject bodyJson)
            throws JSONException {
        return new AudioMessageBody(bodyJson.optString(ATTR_URL),
                bodyJson.optInt(ATTR_LENGTH), bodyJson.optString(ATTR_MEDIA_ID));
    }
    
    private static CmdMessageBody createCmdMessageBody(JSONObject cmdBodyJson)
            throws JSONException {
        HashMap<String, Object> params = new HashMap<>();
        int cmdType = cmdBodyJson.getInt(ATTR_CMD_TYPE);
        Iterator<String> iterator = cmdBodyJson.keys();
        if (iterator != null) {
            while (iterator.hasNext()) {
                String key = iterator.next();
                params.put(key, cmdBodyJson.get(key));
            }
        }
        CmdMessageBody cmdBody = new CmdMessageBody(cmdType);
        cmdBody.setParams(params);
        return cmdBody;
    }
    
}
