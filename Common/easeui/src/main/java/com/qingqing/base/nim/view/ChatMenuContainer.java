package com.qingqing.base.nim.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.easemob.easeui.R;

/**
 * Created by huangming on 2016/8/23.
 */
public class ChatMenuContainer extends LinearLayout {
    
    private ChatExtendMenu mExtendMenu;
    private ChatInputMenu mInputMenu;
    
    private ChatMenuListener mMenuListener;
    
    public ChatMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInputMenu = (ChatInputMenu) findViewById(R.id.chat_input_menu);
        mExtendMenu = (ChatExtendMenu) findViewById(R.id.chat_extend_menu);
        
        mInputMenu.setExtendMenu(mExtendMenu);
    }
    
    public void setMenuListener(ChatMenuListener listener) {
        this.mMenuListener = listener;
        mExtendMenu.setMenuListener(listener);
        mInputMenu.setMenuListener(listener);
    }
    
    public void setExtendMenuItems(int[] titleResIds, int[] iconResIds, int[] itemIds) {
        mExtendMenu.setMenuItems(titleResIds, iconResIds, itemIds);
    }

    public void hideExtendMenu() {
        mExtendMenu.setVisibility(GONE);
    }
}
