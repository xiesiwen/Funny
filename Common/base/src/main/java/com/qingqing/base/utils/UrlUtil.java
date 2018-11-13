package com.qingqing.base.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.qingqing.base.log.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author richie.wang
 * @createTime 2015年7月7日
 * @summary
 */
public final class UrlUtil {
    /**
     * 传入参数不需 encode ， 否则会二次 encode 引发识别错误
     * 
     * @param url
     * @param key
     * @param value
     * @return
     */
    public static String addParamToUrl(String url, String key, String value) {
        
        HashMap<String, String> params = new HashMap<>();
        params.put(key, value);
        return addParamToUrl(url, params);
    }
    
    /**
     * 传入参数不需 encode ， 否则会二次 encode 引发识别错误
     * 
     * @param url
     * @param paramMap
     * @return
     */
    public static String addParamToUrl(String url, Map<String, String> paramMap) {
        
        if (TextUtils.isEmpty(url) || paramMap == null || paramMap.isEmpty()) {
            return url;
        }
        
        // 获取参数部分
        URL formattedUrl;
        Map<String, String> queryPairs = new LinkedHashMap<>();
        try {
            formattedUrl = new URL(url);
        } catch (MalformedURLException e) {
            return url;
        }
        String query = formattedUrl.getQuery();
        if (!TextUtils.isEmpty(query)) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx <= 0) {
                    continue;
                }
                queryPairs.put(URLDecoder.decode(pair.substring(0, idx)),
                        URLDecoder.decode(pair.substring(idx + 1)));
            }
        }
        
        // 过滤重复的 key
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (queryPairs.containsKey(entry.getKey())) {
                continue;
            }
            queryPairs.put(entry.getKey(), entry.getValue());
        }
        
        // 获取新的 query
        String queryString = "";
        for (Map.Entry<String, String> entry : queryPairs.entrySet()) {
            
            String keyString = entry.getKey();
            if (TextUtils.isEmpty(keyString))
                continue;
            
            keyString = URLEncoder.encode(keyString);
            String valueString = entry.getValue();
            if (!TextUtils.isEmpty(valueString)) {
                valueString = URLEncoder.encode(valueString);
            }
            queryString += (keyString + "=" + valueString + "&");
        }

        if (queryString.endsWith("&")) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        
        Uri.Builder builder = Uri.parse(url).buildUpon();
        return URLDecoder.decode(builder.query(queryString).build().toString());
        
        // // 如果包含#，需要先拿掉#部分
        // String hashContent = null;
        // boolean hasHashFlag = url.contains("#");
        // if (hasHashFlag) {
        // int hashIdx = url.indexOf('#');
        // hashContent = url.substring(hashIdx);
        // url = url.substring(0, hashIdx);
        // }
        //
        // // 这里要检验，如果url中已经有了参数，则不添加
        // boolean hasQuestionMark = url.contains("?");
        // Iterator<Map.Entry<String, String>> it =
        // paramMap.entrySet().iterator();
        // StringBuilder sb = new StringBuilder(url);
        // while (it.hasNext()) {
        //
        // Map.Entry<String, String> entry = it.next();
        // String key = entry.getKey();
        // String value = entry.getValue();
        // if (queryPairs.containsKey(key)) {
        // continue;
        // }
        //
        // if (!hasQuestionMark) {
        // sb.append("?");
        // }
        //
        // if (hasQuestionMark) {
        // sb.append("&");
        // }
        // sb.append(String.format("%s=%s", key, value));
        // hasQuestionMark = true;
        // }
        //
        // if (!TextUtils.isEmpty(hashContent)) {
        // sb.append(hashContent);
        // }
        // return sb.toString();
    }
    
    /**
     * 根据url 获取 协议串
     */
    public static String getProtocol(String url) {
        String subString = url;
        try {
            subString = url.substring(0, url.indexOf("://"));
        } catch (Exception e) {
            Logger.w(e);
        }
        return subString;
    }
    
    private static int[] getHostPosition(String url) {
        if (TextUtils.isEmpty(url))
            return null;
        
        int start = url.indexOf("://");
        if (start < 0)
            start = 0;
        int end = url.indexOf("?");
        if (end < 0) {
            end = url.length();
        }
        int end2 = url.indexOf("/", start + 3);
        if (end2 < 0) {
            end2 = url.length();
        }
        
        return (new int[] { start + 3, Math.min(end, end2) });
    }
    
    /**
     * 根据url 获取 主机
     */
    public static String getHost(String url) {
        
        int[] hostPos = getHostPosition(url);
        if (hostPos == null)
            return "";
        
        return url.substring(hostPos[0], hostPos[1]);
    }
    
    /** 替换 url中的host为 指定的host */
    public static String setHost(String url, String host) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(host))
            return url;
        
        int[] hostPos = getHostPosition(url);
        if (hostPos == null)
            return url;
        
        StringBuilder sb = new StringBuilder(url);
        sb.replace(hostPos[0], hostPos[1], host);
        return sb.toString();
    }
    
    /**
     * 根据url 获取 参数
     */
    public static String getUrlParam(String url, String key) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        
        int start = url.indexOf("?");
        if (start < 0)
            return "";
        
        int end = url.indexOf("#");
        if (end < 0) {
            end = url.length();
        }
        
        String paramString = url.substring(start + 1, end);
        String[] params = paramString.split("&");
        for (String param : params) {
            if (param.startsWith(key)) {
                return param.substring(param.indexOf("=") + 1, param.length());
            }
        }
        
        return "";
    }
}
