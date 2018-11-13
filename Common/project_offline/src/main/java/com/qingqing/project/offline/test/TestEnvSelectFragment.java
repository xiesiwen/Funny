package com.qingqing.project.offline.test;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gallery.ui.widget.MarginDecoration;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.SPManager;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.recycler.RecyclerAdapter;
import com.qingqing.base.view.recycler.RecyclerView;
import com.qingqing.base.view.text.TextView;
import com.qingqing.project.offline.R;
import com.qingqing.qingqingbase.ui.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wangxiaxin on 2017/10/31.
 *
 * 测试环境中 用于选择 测试域名的界面
 */
public class TestEnvSelectFragment extends BaseFragment {
    
    private RecyclerView envView;
    private ArrayList<String> envList = new ArrayList<>();
    private RecyclerAdapter envAdapter;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Collections.addAll(envList, "tst", "tst1", "tst2", "tst3", "tst4", "tst5", "tst6",
                "dev", "dev1","dev2");
        envView.setLayoutManager(new LinearLayoutManager(getContext()));
        envView.addItemDecoration(new MarginDecoration(getContext()));
        envAdapter = new EnvAdapter(getContext(), envList);
        envView.setAdapter(envAdapter);
        envAdapter.setItemClickListener(
                new RecyclerAdapter.RecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(RecyclerAdapter.RecyclerViewHolder vh,
                            int position) {
                        String data = envList.get(position);
                        SPManager.put(DefaultDataCache.SP_KEY_TEST_ENV_NAME, data);
                        envAdapter.notifyDataSetChanged();
                        ToastWrapper.show("请重启应用以生效");
                    }
                });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        envView = new RecyclerView(getContext());
        envView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        return envView;
    }
    
    private class EnvAdapter extends RecyclerAdapter<String> {
        
        public EnvAdapter(Context context, List<String> list) {
            super(context, list);
        }
        
        @Override
        protected int getItemLayoutId(int viewType) {
            return R.layout.item_test_env_select;
        }
        
        @Override
        public RecyclerViewHolder<String> createViewHolder(View itemView, int viewType) {
            return new EnvViewHolder(itemView);
        }
    }
    
    private class EnvViewHolder extends RecyclerAdapter.RecyclerViewHolder<String> {
        
        TextView tvTitle;
        ImageView ivSelect;
        
        EnvViewHolder(View itemView) {
            super(itemView);
            
            tvTitle = (TextView) itemView.findViewById(R.id.tv_env_title);
            ivSelect = (ImageView) itemView.findViewById(R.id.iv_env_sel);
        }
        
        @Override
        public void init(Context context) {}
        
        @Override
        public void update(Context context, String data) {
            tvTitle.setText(data);
            ivSelect.setVisibility(data.equals(
                    SPManager.getString(DefaultDataCache.SP_KEY_TEST_ENV_NAME, LogicConfig.TEST_DEFAULT_ENV))
                            ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
