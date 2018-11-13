package com.qingqing.base.nim.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.easemob.easeui.R;

/**
 * Created by huangming on 2016/8/23.
 */
public class ChatExtendMenu extends GridView
        implements AdapterView.OnItemClickListener , IExtendMenuView {
    
    private ChatExtendMenuListener mMenuListener;
    
    private LayoutInflater mInflater;
    
    private ChatExtendMenuAdapter mAdapter;
    
    public ChatExtendMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mInflater = LayoutInflater.from(context);
    }

     void setMenuListener(ChatExtendMenuListener listener) {
        this.mMenuListener = listener;
    }

    private ChatExtendMenuListener getMenuListener() {
        return mMenuListener;
    }
    
    void setMenuItems(int[] titleResIds, int[] iconResIds, int[] itemIds) {
        mAdapter = new ChatExtendMenuAdapter(titleResIds, iconResIds, itemIds);
        setOnItemClickListener(this);
        setAdapter(mAdapter);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getMenuListener() != null) {
            getMenuListener().onExtendMenuItemClick(
                    (ChatExtendMenuItem) view, (int) mAdapter.getItemId(position),
                    position);
        }
    }
    
    @Override
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }
    
    @Override
    public void toggleVisible() {
        setVisibility(isVisible() ? GONE : VISIBLE);
    }
    
    @Override
    public void show() {
        setVisibility(VISIBLE);
    }
    
    @Override
    public void hide() {
        setVisibility(GONE);
    }
    
    class ChatExtendMenuAdapter extends BaseAdapter {
        final int[] titleResIds;
        final int[] iconResIds;
        final int[] itemIds;
        
        ChatExtendMenuAdapter(int[] titleResIds, int[] iconResIds, int[] itemIds) {
            if (titleResIds == null || iconResIds == null || itemIds == null) {
                throw new RuntimeException("Chat Extend Menu data is null!!!");
            }
            
            if (titleResIds.length != iconResIds.length
                    || iconResIds.length != itemIds.length || titleResIds.length == 0) {
                throw new RuntimeException("Chat Extend Menu data is not equals!!!");
            }
            this.titleResIds = titleResIds;
            this.iconResIds = iconResIds;
            this.itemIds = itemIds;
        }
        
        @Override
        public int getCount() {
            return itemIds.length;
        }
        
        @Override
        public Object getItem(int position) {
            return itemIds[position];
        }
        
        @Override
        public long getItemId(int position) {
            return itemIds[position];
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChatExtendMenuHolder holder;
            if (convertView == null) {
                holder = new ChatExtendMenuHolder();
                convertView = holder.item = (ChatExtendMenuItem) mInflater
                        .inflate(R.layout.chat_extend_menu_item_default, parent, false);
                convertView.setTag(holder);
            }
            else {
                holder = (ChatExtendMenuHolder) convertView.getTag();
            }
            
            holder.item.setIcon(iconResIds[position]);
            holder.item.setTitle(titleResIds[position]);
            return convertView;
        }
        
    }
    
    private static class ChatExtendMenuHolder {
        ChatExtendMenuItem item;
    }

    @Override
    public boolean onBackPressed() {
        if (getVisibility() == VISIBLE) {
            setVisibility(GONE);
            return true;
        }
        return false;
    }
}
