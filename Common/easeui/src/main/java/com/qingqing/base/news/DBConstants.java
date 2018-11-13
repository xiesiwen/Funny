package com.qingqing.base.news;

/**
 * Created by huangming on 2016/11/29.
 */

public class DBConstants {

    private DBConstants() {

    }

    static final String TABLE_CONVERSATION = "t_conversation";

    static final String TABLE_NEWS = "t_news";

    static class ConversationColumns {
        static final String KEY_ID = "_id";

        static final String KEY_NAME = "name";

        static final String KEY_USER_ID = "user_id";

        static final String KEY_UNREAD_COUNT = "unread_count";

        static final String KEY_TYPE = "type";

        static final String KEY_FIRST = "first";

        static final String KEY_STICK_TOP = "stick_top";
        
        static final String[] COLUMNS = new String[] { KEY_ID, KEY_USER_ID, KEY_NAME,
                KEY_UNREAD_COUNT, KEY_TYPE, KEY_FIRST, KEY_STICK_TOP };
        
    }

    static class NewsColumns {

        static final String KEY_ID = "_id";

        static final String KEY_USER_ID = "user_id";

        static final String KEY_BODY = "body";

        static final String KEY_TYPE = "type";

        static final String KEY_CONVERSATION_TYPE = "c_type";

        static final String KEY_BID = "bid";

        static final String KEY_FROM = "_from";

        static final String KEY_CONVERSATION_ID = "conversation_id";

        static final String KEY_UNREAD = "unread";

        static final String KEY_CREATED_TIME = "created_time";

        static final String KEY_TO = "msg_to";

        static final String[] COLUMNS = new String[]{KEY_ID, KEY_USER_ID, KEY_FROM, KEY_CONVERSATION_ID, KEY_BODY, KEY_CONVERSATION_TYPE,
                KEY_TYPE, KEY_BID, KEY_UNREAD, KEY_CREATED_TIME, KEY_TO};
    }

}
