package com.qingqing.base.news;

import android.text.TextUtils;

import com.qingqing.base.log.Logger;
import com.qingqing.base.mqtt.IMqttMsgHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by huangming on 2016/12/23.
 */

public class SystemNewsHolder {

    private static final String TAG = SystemNewsHolder.class.getSimpleName();

    private static Map<String, SystemNewsBody> sNewsBodies = new HashMap<>();


    public static SystemNewsBody getNewsBody(News news) {
        SystemNewsBody body = sNewsBodies.get(news.getId());
        if (body == null) {
            body = parse(news);
            sNewsBodies.put(news.getId(), body);
        }
        return body;
    }

    private static SystemNewsBody parse(News news) {
        SystemNewsBody body = new SystemNewsBody();
        try {
            JSONObject json = new JSONObject(news.getBody());

            Iterator<String> iterator = json.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (IMqttMsgHandler.KEY_TITLE.equals(key)) {
                    body.setTitle(json.getString(key));
                } else if (IMqttMsgHandler.KEY_CONTENT.equals(key)) {
                    body.setContent(json.getString(key));
                } else {
                    body.setAttribute(key, json.get(key));
                }
            }
            //轻轻活动消息特殊处理
            NewsConversationType type = NewsConversationType.mapStringToValue(news.getConversationType());
            if (type == NewsConversationType.ACTIVITY || type == NewsConversationType.COMPANY_NEWS || type == NewsConversationType.INFORMATION) {
                body.setContent(body.getTitle());
                body.setTitle("轻轻小贴士");
            } else if (type == NewsConversationType.QQ_COLLEGE) {
                if (TextUtils.isEmpty(body.getContent())) {
                    body.setContent(body.getTitle());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, "parse " + news.toString(), e);
        }
        return body;
    }

}
