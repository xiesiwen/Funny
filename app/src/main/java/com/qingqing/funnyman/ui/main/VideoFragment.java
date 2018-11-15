package com.qingqing.funnyman.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.qingqing.funnyman.R;
import com.qingqing.qingqingbase.ui.BaseFragment;

/**
 * Created by xiejingwen on 2018/11/14.
 */

public class VideoFragment extends BaseFragment{
    private TabHost tabHost;
    private ViewPager viewPager;
    private RecyclerView mFocusView;
    private RecyclerView mTotalView;
    private TopicsView mTopicsView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_video, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabHost = view.findViewById(R.id.tabHost);
        viewPager = view.findViewById(R.id.viewPager);
        initViewPager();
    }

    private void initViewPager() {
        mFocusView = new RecyclerView(getContext());
        mFocusView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mTotalView = new RecyclerView(getContext());

    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoHolder>{

        public VideoAdapter(){

        }

        @Override
        public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(VideoHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

    }

    private class VideoHolder extends RecyclerView.ViewHolder{

        public VideoHolder(View itemView) {
            super(itemView);
        }
    }

    private class VideoPageAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return false;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof View) {
                container.removeView((View) object);
            }
        }
    }

}
