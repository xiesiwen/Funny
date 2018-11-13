package com.easemob.easeui.model;

import com.easemob.easeui.R;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.utils.EaseSmileUtils;

/**
 * Created by tangyutian on 2017/7/5.
 * 聊天emoji数据
 */

public class EaseDefaultEmojiDatas {

    private static int[] emojis = new int[]{
            0x1F60a,
            0x1F603,
            0x1F609,
            0x1F62e,
            0x1F60b,
            0x1F60e,
            0x1F621,
            0x1F616,
            0x1F633,
            0x1F61e,
            0x1F62d,
            0x1F610,
            0x1F607,
            0x1F62c,
            0x1F606,
            0x1F631,
            0x1F385,
            0x1F634,
            0x1F615,
            0x1F637,
            0x1F62f,
            0x1F60f,
            0x1F611,
            0x1F496,
            0x1F494,
            0x1F319,
            0x1f31f,
            0x1f31e,
            0x1F308,
            0x1F60d,
            0x1F61a,
            0x1F48b,
            0x1F339,
            0x1F342,
            0x1F44d
};

    private static final EaseEmojicon[] DATA = createData();

    private static EaseEmojicon[] createData(){
        EaseEmojicon[] datas = new EaseEmojicon[emojis.length];
        for(int i = 0; i < emojis.length; i++){
            datas[i] = new EaseEmojicon(0, new String(Character.toChars(emojis[i])), EaseEmojicon.Type.EMOJI);
        }
        return datas;
    }

    public static EaseEmojicon[] getData(){
        return DATA;
    }
}
