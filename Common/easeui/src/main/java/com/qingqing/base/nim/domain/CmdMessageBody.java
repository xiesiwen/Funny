package com.qingqing.base.nim.domain;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by huangming on 2016/8/18.
 */
public class CmdMessageBody extends MessageBody {
    
    private final int type;
    
    private HashMap<String, Object> params = new HashMap<>(2);
    
    CmdMessageBody(int type) {
        this.type = type;
    }
    
    public int getType() {
        return type;
    }
    
    void setParams(HashMap<String, Object> params) {
        this.params = params;
    }
    
    private HashMap<String, Object> getParams() {
        return params;
    }
    
    public int getInt(String key) {
        try {
            return (int) getParams().get(key);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public String getString(String key) {
        try {
            String value = (String) getParams().get(key);
            return value != null ? value : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    public boolean getBoolean(String key) {
        try {
            return (boolean) getParams().get(key);
        } catch (Exception e) {
            return false;
        }
    }
    
    public long getLong(String key) {
        try {
            return (long) getParams().get(key);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public double getDouble(String key) {
        try {
            return (double) getParams().get(key);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public JSONArray getJSONArray(String key) {
        try {
            return (JSONArray) getParams().get(key);
        } catch (Exception e) {
            return new JSONArray();
        }
    }
    
    public JSONObject getJSONObject(String key) {
        try {
            return (JSONObject) getParams().get(key);
        } catch (Exception e) {
            return new JSONObject();
        }
    }
    
}
