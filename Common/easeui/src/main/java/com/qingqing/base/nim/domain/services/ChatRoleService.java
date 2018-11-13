package com.qingqing.base.nim.domain.services;

import com.qingqing.base.nim.domain.ChatRole;

/**
 * Created by huangming on 2016/8/22.
 */
public interface ChatRoleService {

    ChatRole getCurrentRoleBy(String conversationId) ;

}
