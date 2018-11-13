package com.qingqing.project.offline.view.city;

import android.view.View;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qingqing.base.bean.City;
import com.qingqing.base.utils.HanziToPinyin;
import com.qingqing.project.offline.view.city.SearchCityPartitionAdapter;

/**
 * Created by xiejingwen on 2017/6/12.
 * 用于城市选择滑动显示浮动字母Title的效果
 */

public class PinnedListScrollHelper implements AbsListView.OnScrollListener {
    public static final String TAG_HEAD = "tag_head";
    public static final String TAG_CONTENT = "tag_content";
    private TextView mPinnedTextView;

    public PinnedListScrollHelper(TextView textView) {
        mPinnedTextView = textView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view == null || view.getAdapter() == null) {
            return;
        }
        int headCount = ((ListView) view).getHeaderViewsCount();
        if (firstVisibleItem < headCount) {
            if (mPinnedTextView.getVisibility() != View.INVISIBLE) {
                mPinnedTextView.setVisibility(View.INVISIBLE);
            }
            return;
        }
        int pinnedHeight = mPinnedTextView.getHeight();
        ListAdapter adapter = getWrappedAdapter(view);
        if (adapter instanceof SearchCityPartitionAdapter) {
            View child = getFirstHeadPinnedView(view);
            Object item = adapter.getItem(firstVisibleItem - ((ListView) view).getHeaderViewsCount());
            if (mPinnedTextView.getVisibility() != View.VISIBLE) {
                mPinnedTextView.setVisibility(View.VISIBLE);
            }
            if (child != null) {
                int top = child.getTop();
                if (top > 0 && top < pinnedHeight) {
                    if (top < pinnedHeight) {
                        mPinnedTextView.setTranslationY(Math.max(top, 0) - pinnedHeight);
                    }
                } else {
                    if (mPinnedTextView.getTranslationY() != 0) {
                        mPinnedTextView.setTranslationY(0);
                    }
                }
            } else {
                if (mPinnedTextView.getTranslationY() != 0) {
                    mPinnedTextView.setTranslationY(0);
                }
            }
            setPinnedText(item);
        }
    }

    private View getFirstHeadPinnedView(AbsListView listView) {
        int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = listView.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag.equals(TAG_HEAD)) {
                return child;
            }
        }
        return null;
    }

    private ListAdapter getWrappedAdapter(AbsListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        return adapter;
    }

    private void setPinnedText(Object item){
        if(item == null){
            return;
        }
        if (item instanceof City) {
            String tip = HanziToPinyin.getSortKey(((City) item).name).substring(0, 1).toUpperCase();
            mPinnedTextView.setText(tip);
        }
        if (item instanceof String) {
            mPinnedTextView.setText((String) item);
        }
    }
}
