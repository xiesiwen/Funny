package com.qingqing.base.dns;

import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.SPManager;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.PackageUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wangxiaxin on 2016/11/14.
 * <p>
 * 域名管理类，主要用于请求超时 的 域名切换
 */

public final class HostManager {
    
    private static final String TAG = "HostManager";
    
    private static HostManager sInstance = null;
    private HashMap<String, HostList> hostListMap;
    private boolean isIntranet;
    private static final String SP_KEY_HOST_CONTENT = "host_content";
    private boolean enable = true;// 是否启用
                                  // 备用域名逻辑
    
    private static final String DEFAULT_CONTENT = "{\n"
            + "\"passport.changingedu.com\": [\"passport-stby.changingedu.com\"],\n"
            + "\"api.changingedu.com\": [\"api-stby.changingedu.com\"],\n"
            + "\"cdn-api.changingedu.com\": [\"api-stby.changingedu.com\"],\n"
            + "\"time.changingedu.com\": [\"time-stby.changingedu.com\"],\n"
            + "\"pushapi.changingedu.com\": [\"pushapi-stby.changingedu.com\"],\n"
            + "\"cdn-activity.changingedu.com\": [\"activity-stby.changingedu.com\"],\n"
            + "\"taapi.changingedu.com\": [\"taapi-stby.changingedu.com\"],\n"
            + "\"activity.changingedu.com\": [\"activity-stby.changingedu.com\"],\n"
            + "\"front.changingedu.com\": [\"front-stby.changingedu.com\"],\n"
            + "\"m.changingedu.com\": [\"m-stby.changingedu.com\"]\n" + "\n" + "}";
    
    private static final String DEFAULT_TEST_CONTENT = "{\n"
            + "\"passport-idc.changingedu.com\": [\"passport-stby-idc.changingedu.com\"],\n"
            + "\"api-idc.changingedu.com\": [\"api-stby-idc.changingedu.com\"],\n"
            + "\"cdn-api-idc.changingedu.com\": [\"api-stby-idc.changingedu.com\"],\n"
            + "\"time-idc.changingedu.com\": [\"time-stby-idc.changingedu.com\"],\n"
            + "\"pushapi-idc.changingedu.com\": [\"pushapi-stby-idc.changingedu.com\"],\n"
            + "\"cdn-activity-idc.changingedu.com\": [\"activity-stby-idc.changingedu.com\"],\n"
            + "\"taapi-idc.changingedu.com\": [\"taapi-stby-idc.changingedu.com\"],\n"
            + "\"activity-idc.changingedu.com\": [\"activity-stby-idc.changingedu.com\"],\n"
            + "\"front-idc.changingedu.com\": [\"front-stby-idc.changingedu.com\"],\n"
            + "\"m-idc.changingedu.com\": [\"m-stby-idc.changingedu.com\"]\n" + "\n"
            + "}";
    
    public static HostManager INSTANCE() {
        if (sInstance == null) {
            synchronized (HostManager.class) {
                if (sInstance == null) {
                    sInstance = new HostManager();
                }
            }
        }
        return sInstance;
    }
    
    private HostManager() {
        // 构造
        hostListMap = new HashMap<>();
        loadContent();
    }
    
    public boolean isEnable() {
        return enable;
    }
    
    /**
     * 所有的域名 统统使用备用域名
     */
    public void allUseBackupHost() {
        if (!hostListMap.isEmpty()) {
            Iterator<Map.Entry<String, HostList>> it = hostListMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, HostList> entry = it.next();
                HostList hostList = entry.getValue();
                if (hostList != null) {
                    hostList.useNextHost();
                }
            }
        }
    }
    
    /**
     * 切换到下一个对应的域名
     */
    public void useNextHost(String mainHost) {
        HostList hostList = hostListMap.get(mainHost);
        if (hostList != null) {
            hostList.useNextHost();
        }
    }
    
    /** 当前的域名 使用的是否是最后一个域名 */
    public boolean isUsingLastHost(String mainHost) {
        HostList hostList = hostListMap.get(mainHost);
        return hostList != null && hostList.isLast();
    }
    
    /**
     * 获取当前主host 对应的 host
     */
    public String currentHost(String mainHost) {
        HostList hostList = hostListMap.get(mainHost);
        if (hostList != null) {
            return hostList.getHost();
        }
        else {
            return mainHost;
        }
    }
    
    /**
     * 解析，并保存，供外部调用
     */
    public void parseAndSave(String content) {
        if (parseContent(content)) {
            saveContent(content);
        }
    }
    
    /**
     * 解析内容,并保存到结构中
     */
    private boolean parseContent(String content) {
        
        if (!enable)
            return false;

        if (isIntranet() && HttpUrl.supportHostSwitch) {
            content = content.replace("-idc",
                    "-" + SPManager.getString(DefaultDataCache.SP_KEY_TEST_ENV_NAME,
                            LogicConfig.TEST_DEFAULT_ENV));
        }
        
        try {
            JSONObject obj = new JSONObject(content);
            Iterator<String> iterator = obj.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = obj.optString(key);
                JSONArray array = new JSONArray(value);
                
                HostList hostList = hostListMap.get(key);
                if (hostList == null) {
                    hostList = new HostList(key);
                    hostListMap.put(key, hostList);
                }
                hostList.updateList(array);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    private void saveContent(String content) {
        if (!enable)
            return;
        SPManager.put(SP_KEY_HOST_CONTENT, content);
    }
    
    /**
     * 加载配置信息
     */
    private void loadContent() {
        
        if (!enable)
            return;
        
        isIntranet = "true".equals(PackageUtil.getMetaData("qingqing.net"));
        String content = SPManager.getString(SP_KEY_HOST_CONTENT,
                isIntranet ? DEFAULT_TEST_CONTENT : DEFAULT_CONTENT);
        parseContent(content);
    }

    public boolean isIntranet(){
        return isIntranet;
    }
    
    public boolean hasHostList(String mainHost) {
        HostList hostList = hostListMap.get(mainHost);
        return hostList != null && hostList.hasList();
    }

    /**
     * 判断是否是主域名
     * */
    public boolean isMainHost(String hostString){
        return hasHostList(hostString);
    }

    /**
     * 根据给定的域名，找出对应的主域名，如果未找到，则返回原来的域名
     * */
    public String findMainHost(String hostString){
        boolean isMainHost = isMainHost(hostString);
        if(isMainHost){
            return hostString;
        }else{
            for (Map.Entry entry : hostListMap.entrySet()) {
                HostList hostList = (HostList) entry.getValue();
                if (hostList != null) {
                    if(hostList.index(hostString) >=0){
                        return hostList.defaultHost();
                    }
                }
            }
        }

        return hostString;
    }
    
    public HashMap<String, HostList> getHostListMap() {
        return hostListMap;
    }
    
    /**
     * 所有域名中是否有已开启的备用域名
     */
    public boolean hasBackupDomainEnabled() {
        for (Map.Entry entry : hostListMap.entrySet()) {
            HostList hostList = (HostList) entry.getValue();
            if (hostList != null && hostList.selIdx > -1) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 指定域名是否已开启备用域名
     */
    public boolean hasBackupDomainEnabled(String mainHost) {
        HostList hostList = hostListMap.get(mainHost);
        
        if (hostList != null && hostList.domain.equals(mainHost)
                && hostList.selIdx > -1) {
            return true;
        }
        
        return false;
    }
    
    public class HostList {
        
        public int selIdx;
        public String domain;
        public ArrayList<String> hostList;
        
        HostList(String defaultDomain) {
            hostList = new ArrayList<>();
            domain = defaultDomain;
            selIdx = -1;
        }
        
        boolean hasList() {
            return hostList != null && !hostList.isEmpty();
        }
        
        public int index(String ip) {
            return hostList.indexOf(ip);
        }
        
        // 使用下一个host
        void useNextHost() {
            
            Logger.o(TAG, "begin use next host , domain=" + domain);
            
            if (hostList.isEmpty())
                return;
            
            if (selIdx < 0) {
                selIdx = 0;
            }
            else if (selIdx < (hostList.size() - 1)) {
                ++selIdx;
            }
            
            Logger.o(TAG, "use next host , domain= " + domain + " , idx=" + selIdx);
        }
        
        public boolean isLast() {
            return selIdx == (hostList.size() - 1);
        }
        
        public boolean isDefault() {
            return selIdx == -1;
        }
        
        public String defaultHost() {
            return domain;
        }
        
        public String getHost() {
            if (selIdx < 0) {
                return domain;
            }
            else {
                int idx = Math.min(selIdx, hostList.size() - 1);
                return hostList.get(idx);
            }
        }
        
        public void clear() {
            hostList.clear();
            selIdx = -1;
        }
        
        void updateList(JSONArray array) {
            
            if (array == null)
                return;
            
            int count = array.length();
            clear();
            if (count > 0) {
                // 第一版 我们只取一个备用域名
                count = 1;
                for (int j = 0; j < count; j++) {
                    try {
                        hostList.add(array.getString(j));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                selIdx = -1;
            }
        }
        
    }
    
}
