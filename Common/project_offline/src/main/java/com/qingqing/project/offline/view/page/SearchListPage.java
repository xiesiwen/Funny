package com.qingqing.project.offline.view.page;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qingqing.base.view.pager.Page;
import com.qingqing.project.offline.R;

/**
 * Created by tangyutian on 2016/8/4.
 * 搜索列表……好像没用了
 */

public class SearchListPage implements Page {
    private BaseAdapter mAdapter;
    private ListView mListview;
    private View mNothingView;
    private View mLoadingView;
    private TextView mTvNotFind;
    private AdapterView.OnItemClickListener mListener;
    private Context mContext;
    private ImageView mIvNoResult;

    public SearchListPage(BaseAdapter adapter){
        mAdapter = adapter;
    }

    @Override
    public void initPage(final Context context, View page) {
        mListview =  (ListView)page.findViewById(R.id.list);
        mNothingView = page.findViewById(R.id.no_result);
        mLoadingView = page.findViewById(R.id.text);
        mTvNotFind = (TextView)page.findViewById(R.id.not_find);
        mContext = context;
        if (mListener!=null){
            mListview.setOnItemClickListener(mListener);
        }
        mIvNoResult = (ImageView)page.findViewById(R.id.iv_no_result);
        update(null);
    }

    private void update(@Nullable String text){
        if (mAdapter!=null) {
            (mListview).setAdapter(mAdapter);
            mLoadingView.setVisibility(View.GONE);
            if (mAdapter.getCount() == 0) {
                mListview.setVisibility(View.GONE);
                if (text!=null){
                    mTvNotFind.setText(mContext.getString(R.string.text_not_find_result,text));
                }
                mNothingView.setVisibility(View.VISIBLE);
            } else {
                mListview.setVisibility(View.VISIBLE);
                mNothingView.setVisibility(View.GONE);
            }
        }else {
            mLoadingView.setVisibility(View.VISIBLE);
            mListview.setVisibility(View.GONE);
            mNothingView.setVisibility(View.GONE);
        }
    }
    public void setAdapter(BaseAdapter adapter,String text){
        mAdapter = adapter;
        update(text);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        mListener = listener;
        if (mListview !=null) {
            mListview.setOnItemClickListener(listener);
        }
    }

    @Override
    public View createPage(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.search_question_page, parent, false);
    }

    public void setNoResultImage(int resourceId){
        if (mIvNoResult !=null){
            mIvNoResult.setImageResource(resourceId);
        }
    }
}
