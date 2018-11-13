package com.qingqing.project.offline.view.city;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qingqing.base.bean.City;
import com.qingqing.base.view.PartitionAdapter;
import com.qingqing.project.offline.R;

import java.util.List;

/**
 * Created by xiejingwen on 2017/5/27.
 * 选择城市的Adapter
 */


public class SearchCityPartitionAdapter extends PartitionAdapter<City> {

    private boolean showHead = true;

    SearchCityPartitionAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_partition_search_city, parent, false);
    }

    @Override
    protected void bindView(View v, int partition, List<City> infos, Object headInfo, int offset, boolean isHead) {
        final City city = !isHead && infos != null
                && offset >= 0 && offset < infos.size() ? infos
                .get(offset) : null;
        final int position = getPosByPartitionAndOffset(partition, offset);
        updateView(v, city, headInfo, position, isHead);
    }

    protected void updateView(View v, City city, Object headInfo, int position, boolean isHead) {
        TextView head = (TextView) v.findViewById(R.id.tv_head);
        TextView content = (TextView) v.findViewById(R.id.tv_item_partition);
        if (isHead) {
            if(showHead){
                head.setText((String) headInfo);
                if (head.getVisibility() != View.VISIBLE) {
                    head.setVisibility(View.VISIBLE);
                }
                if (content.getVisibility() != View.GONE) {
                    content.setVisibility(View.GONE);
                }
                v.setTag(PinnedListScrollHelper.TAG_HEAD);
            }
        } else {
            content.setText(city.name);
            if (head.getVisibility() != View.GONE) {
                head.setVisibility(View.GONE);
            }
            if (content.getVisibility() != View.VISIBLE) {
                content.setVisibility(View.VISIBLE);
            }
            v.setTag(PinnedListScrollHelper.TAG_CONTENT);
        }
    }

    public void setShowHead(boolean showHead) {
        this.showHead = showHead;
    }
}
