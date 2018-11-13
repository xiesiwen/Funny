package com.qingqing.base.im.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.domain.GroupInfo;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenwei_sh on 2015/12/23.
 */
public class BaseGroupListActivity extends BaseActionBarActivity {
    private ListView mlvGroupList;
    private ViewStub mEmptyContainer;
    
    protected GroupAdapter mGroupAdapter;
    protected int mDefaultIcon;
    protected List<GroupInfo> mGroups;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.fragment_group_list);
        mlvGroupList = (ListView) findViewById(R.id.lv_group_list);
        mEmptyContainer = (ViewStub) findViewById(R.id.vs_empty_view);
        
        mGroups = new ArrayList<>();
        createAdapter(BaseGroupListActivity.this);
        mlvGroupList.setAdapter(mGroupAdapter);
        mlvGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                onGroupItemClick(view, position);
            }
        });
        refreshGroupsData();
        ChatManager.getInstance()
                .getGroupListType(new ChatManager.GroupListTypeUpdateListener() {
                    @Override
                    public void onGroupListTypeUpdate() {
                        mGroupAdapter.notifyDataSetChanged();
                    }
                });
    }
    
    protected void refreshGroupsData() {
        
    }
    
    protected void createAdapter(Context context) {
        mGroupAdapter = new GroupAdapter(context, mGroups);
    }
    
    /** 设置empty view */
    protected void setEmptyView(int resid) {
        mEmptyContainer.setLayoutResource(resid);
        mlvGroupList.setEmptyView(mEmptyContainer);
    }
    
    /**
     * 刷新界面
     * 
     * @param list
     */
    protected void updateList(List<GroupInfo> list) {
        if (list != null) {
            this.mGroups.clear();
            this.mGroups.addAll(list);
            if (mGroupAdapter != null) {
                mGroupAdapter.notifyDataSetChanged();
            }
        }
    }
    
    protected void notifyDataSet() {
        if (mGroupAdapter != null) {
            mGroupAdapter.notifyDataSetChanged();
        }
    }
    
    protected void onGroupItemClick(View view, int position) {}
    
    /**
     * 设置item的默认图标
     *
     * @param defaultIcon
     *            defaultIcon
     */
    protected void setDefaultIcon(int defaultIcon) {
        this.mDefaultIcon = defaultIcon;
    }
    
    protected class GroupAdapter extends BaseAdapter<GroupInfo> {
        
        public GroupAdapter(Context context, List<GroupInfo> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_group, parent,
                    false);
        }
        
        @Override
        public ViewHolder<GroupInfo> createViewHolder() {
            return new GroupHolder();
        }
    }
    
    public class GroupHolder extends BaseAdapter.ViewHolder<GroupInfo> {
        protected AsyncImageViewV2 aivIcon;
        protected TextView tvName;
        
        @Override
        public void init(Context context, View convertView) {
            aivIcon = (AsyncImageViewV2) convertView.findViewById(R.id.aiv_icon);
            tvName = (TextView) convertView.findViewById(R.id.tv_name);
        }
        
        @Override
        public void update(Context context, GroupInfo groupInfo) {
            if (groupInfo != null) {
                
                aivIcon.setImageUrl("", mDefaultIcon);
                
                if (!TextUtils.isEmpty(groupInfo.getmGroupName())) {
                    tvName.setText(groupInfo.getmGroupName());
                }
                else {
                    tvName.setText("默认的群名称");// todo
                }
            }
        }
    }
}
