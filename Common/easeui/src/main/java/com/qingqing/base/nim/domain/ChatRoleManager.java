package com.qingqing.base.nim.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangming on 2016/8/22.
 */
class ChatRoleManager {
    
    private Map<String, ChatRole> chatRoles = Collections
            .synchronizedMap(new HashMap<String, ChatRole>());
    
    ChatRoleManager() {
        
    }

}
