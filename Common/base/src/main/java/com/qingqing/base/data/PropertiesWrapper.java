package com.qingqing.base.data;

import com.qingqing.base.utils.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created on 2017/2/4. 用于配合判断MIUI系统的工具类
 */

public class PropertiesWrapper {
    /*
     * 作者：Mariotaku 链接：https://www.zhihu.com/question/22102139/answer/24834510
     * 来源：知乎 著作权归作者所有，转载请联系作者获得授权。
     */
    
    private final Properties properties;
    
    public PropertiesWrapper(File propFile) throws IOException {
        properties = new Properties();
        FileInputStream fis = new FileInputStream(propFile);
        properties.load(fis);
        IOUtil.closeQuietly(fis);
    }
    
    public PropertiesWrapper(String propFileFullPath) throws IOException {
        this(new File(propFileFullPath));
    }
    
    public boolean containsKey(final Object key) {
        return properties.containsKey(key);
    }
    
    public boolean containsValue(final Object value) {
        return properties.containsValue(value);
    }
    
    public Set<Map.Entry<Object, Object>> entrySet() {
        return properties.entrySet();
    }
    
    public String getProperty(final String name) {
        return properties.getProperty(name);
    }
    
    public String getProperty(final String name, final String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }
    
    public boolean isEmpty() {
        return properties.isEmpty();
    }
    
    public Enumeration<Object> keys() {
        return properties.keys();
    }
    
    public Set<Object> keySet() {
        return properties.keySet();
    }
    
    public int size() {
        return properties.size();
    }
    
    public Collection<Object> values() {
        return properties.values();
    }
}
