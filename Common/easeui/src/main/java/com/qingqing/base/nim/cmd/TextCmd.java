package com.qingqing.base.nim.cmd;

import com.qingqing.base.nim.domain.CmdMessageBody;

/**
 * Created by huangming on 2016/8/27.
 */
public class TextCmd {
    
    public static final String TEXT = "text";
    
    public static String getText(CmdMessageBody body) {
        return body.getString(TEXT);
    }
}
