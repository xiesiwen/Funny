package com.qingqing.base.news.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.base.news.NewsContentProvider;
import com.qingqing.base.news.NewsConversationType;
import com.qingqing.base.news.NewsDateUtils;
import com.qingqing.base.news.NewsManager;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.news.News;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huangming on 2016/12/5.
 */

class SystemNewsAdapter extends BaseAdapter<News> {

    protected Set<String> clickedNewses = new HashSet<>();

    protected String conversationType;

    SystemNewsAdapter(Context context, String conversationType, List<News> list) {
        super(context, list);
        this.conversationType = conversationType;
    }

    @Override
    public View createView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                NewsConversationType.QQ_COLLEGE.getValue().equals(conversationType) ? R.layout.list_item_qq_college_news : R.layout.list_item_system_news,
                parent,
                false);
    }

    @Override
    public ViewHolder<News> createViewHolder() {
        return new NewsHolder();
    }

    protected void newsClicked(News news) {
        if (!clickedNewses.contains(news.getId())) {
            clickedNewses.add(news.getId());
            notifyDataSetChanged();
        }
    }


    class NewsHolder extends ViewHolder<News> {

        AsyncImageViewV2 icon;

        TextView title;
        TextView time;
        TextView content;
        View remain;

        @Override
        public void init(Context context, View convertView) {

            icon = (AsyncImageViewV2) convertView.findViewById(R.id.icon);

            title = (TextView) convertView.findViewById(R.id.title);
            time = (TextView) convertView.findViewById(R.id.tv_time);
            content = (TextView) convertView.findViewById(R.id.tv_system_message_content);
            remain = convertView.findViewById(R.id.img_unread_remain);
        }

        @Override
        public void update(final Context context, final News data) {
            NewsContentProvider provider = NewsManager.getInstance().getContentProvider();
            icon.setImageUrl(provider.getNewsIconUrl(context, data), provider.getNewsDefaultIcon(context, data));

            time.setText(NewsDateUtils.getDateTextNew(data.getCreatedTime()));
            title.setText(provider.getNewsTitle(context, data));
            content.setText(provider.getNewsContent(context, data));

            remain.setVisibility(!clickedNewses.contains(data.getId()) && data.isUnread() ? View.VISIBLE : View.GONE);
        }

    }

}
