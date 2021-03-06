package com.easemob.easeui.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.easemob.util.DensityUtil;

/**
 * 按+按钮出来的扩展按钮
 *
 */
public class EaseChatExtendMenu extends GridView{

    protected Context context;
    private List<ChatMenuItemModel> itemModels = new ArrayList<ChatMenuItemModel>();

    public EaseChatExtendMenu(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseChatExtendMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EaseChatExtendMenu(Context context) {
        super(context);
        init(context, null);
    }
    
    private void init(Context context, AttributeSet attrs){
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseChatExtendMenu);
        int numColumns = ta.getInt(R.styleable.EaseChatExtendMenu_numColumns, 4);
        ta.recycle();
        
        setNumColumns(numColumns);
        setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        setGravity(Gravity.CENTER_VERTICAL);
        setVerticalSpacing(DensityUtil.dip2px(context, 8));
    }
    
    /**
     * 初始化
     */
    public void init(){
        setAdapter(new ItemAdapter(context, itemModels));
    }
    
    /**
     * 注册menu item
     * 
     * @param name
     *            item名字
     * @param drawableRes
     *            item背景
     * @param itemId
     *             id
     * @param listener
     *            item点击事件
     */
    public void registerMenuItem(String name, int drawableRes, int itemId, EaseChatExtendMenuItemClickListener listener) {
        registerMenuItem(name, drawableRes, itemId, false, listener);
    }

    public void registerMenuItem(String name, int drawableRes, int itemId, boolean remain, EaseChatExtendMenuItemClickListener listener) {
        ChatMenuItemModel item = new ChatMenuItemModel();
        item.name = name;
        item.image = drawableRes;
        item.id = itemId;
        item.remain = remain;
        item.clickListener = listener;
        itemModels.add(item);
    }
    
    /**
     * 注册menu item
     * 
     * @param nameRes
     *            item名字的resource id
     * @param drawableRes
     *            item背景
     * @param itemId
     *             id
     * @param listener
     *            item点击事件
     */
    public void registerMenuItem(int nameRes, int drawableRes, int itemId, EaseChatExtendMenuItemClickListener listener) {
        registerMenuItem(context.getString(nameRes), drawableRes, itemId, listener);
    }

    public void registerMenuItem(int nameRes, int drawableRes, int itemId, boolean remain, EaseChatExtendMenuItemClickListener listener) {
        registerMenuItem(context.getString(nameRes), drawableRes, itemId, remain, listener);
    }

    public void hideMenuItemRemain(int itemId) {
        for(ChatMenuItemModel model : itemModels) {
            if(model.id == itemId) {
                model.remain = false;
                notifyDataChanged();
                return;
            }
        }
    }

    private void notifyDataChanged() {
        if(getAdapter() instanceof BaseAdapter) {
            ((BaseAdapter)getAdapter()).notifyDataSetChanged();
        }
    }
    
    
    private class ItemAdapter extends ArrayAdapter<ChatMenuItemModel>{

        private Context context;

        public ItemAdapter(Context context, List<ChatMenuItemModel> objects) {
            super(context, 1, objects);
            this.context = context;
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ChatMenuItem menuItem = null;
            if(convertView == null){
                convertView = new ChatMenuItem(context);
            }
            menuItem = (ChatMenuItem) convertView;
            menuItem.setImage(getItem(position).image);
            menuItem.setText(getItem(position).name);
            menuItem.showRemain(getItem(position).remain);
            menuItem.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if(getItem(position).clickListener != null){
                        getItem(position).clickListener.onClick(getItem(position).id, v);
                    }
                }
            });
            return convertView;
        }
        
        
    }
    
    
    public interface EaseChatExtendMenuItemClickListener{
        void onClick(int itemId, View view);
    }
    
    
    class ChatMenuItemModel{
        String name;
        int image;
        int id;
        boolean remain;
        EaseChatExtendMenuItemClickListener clickListener;
    }
    
    class ChatMenuItem extends LinearLayout {
        private ImageView imageView;
        private TextView textView;
        private ImageView remainImg;

        public ChatMenuItem(Context context, AttributeSet attrs, int defStyle) {
            this(context, attrs);
        }

        public ChatMenuItem(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
        }

        public ChatMenuItem(Context context) {
            super(context);
            init(context, null);
        }

        private void init(Context context, AttributeSet attrs) {
            LayoutInflater.from(context).inflate(R.layout.ease_chat_menu_item, this);
            imageView = (ImageView) findViewById(R.id.image);
            textView = (TextView) findViewById(R.id.text);
            remainImg = (ImageView) findViewById(R.id.img_remain);
        }

        public void showRemain(boolean remain) {
            remainImg.setVisibility(remain ? VISIBLE : GONE);
        }

        public void setImage(int resid) {
            imageView.setImageResource(resid);
        }

        public void setText(int resid) {
            textView.setText(resid);
        }

        public void setText(String text) {
            textView.setText(text);
        }
    }
}
