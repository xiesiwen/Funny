package com.qingqing.base.news.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.news.News;
import com.qingqing.base.news.NewsContentProvider;
import com.qingqing.base.news.NewsConversationType;
import com.qingqing.base.news.NewsDateUtils;
import com.qingqing.base.news.NewsManager;
import com.qingqing.base.view.AsyncImageViewV2;

import java.util.List;

/**
 * 家长端本地消息列表adapter，修改轻轻小贴士ui 5.7.0
 *
 * Created by tanwei on 2017/4/1.
 */

public class SystemNewsAdapterStu extends SystemNewsAdapter {

    SystemNewsAdapterStu(Context context, String conversationType, List<News> list) {
        super(context, conversationType, list);
    }

    @Override
    public View createView(Context context, ViewGroup parent) {

        int layoutId = R.layout.list_item_system_news;
        if (NewsConversationType.INFORMATION.getValue().equals(conversationType)) {
            layoutId = R.layout.list_item_system_news_info_of_stu;
        }
        else if (NewsConversationType.QQ_COLLEGE.getValue().equals(conversationType)) {
            layoutId = R.layout.list_item_qq_college_news;
        }

        return LayoutInflater.from(context).inflate(layoutId, parent, false);
    }

    @Override
    public ViewHolder<News> createViewHolder() {
        return new SystemNewsAdapterStu.NewsHolder();
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

            if (title != null) {
                time.setText(NewsDateUtils.getDateTextNew(data.getCreatedTime()));
                title.setText(provider.getNewsTitle(context, data));
            }
            else {
                // 家长信息咨询消息不显示标题
                time.setText(NewsDateUtils.getDateTextNew(data.getCreatedTime()));
            }
            content.setText(provider.getNewsContent(context, data));

            remain.setVisibility(!clickedNewses.contains(data.getId()) && data.isUnread() ? View.VISIBLE : View.GONE);
        }

    }
}
