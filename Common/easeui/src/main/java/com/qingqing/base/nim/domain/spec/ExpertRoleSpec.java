package com.qingqing.base.nim.domain.spec;

import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.spec.Spec;

/**
 * Created by huangming on 2016/8/29.
 */
public class ExpertRoleSpec implements Spec<Message> {
    
    @Override
    public boolean isSatisfiedBy(Message product) {
        boolean isExpert = false;
        for (int i = 0; i < product.getRole().getRoleType().size(); i++) {
            if (product.hasPlayedRole() && product.getRole().getRoleType()
                    .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type) {
                isExpert = true;
            }
        }
        
        return isExpert;
    }
}
