package com.qingqing.base.im.ui;

import java.util.ArrayList;
import java.util.List;

import com.easemob.easeui.R;
import com.easemob.easeui.model.EaseAtMessageHelper;
import com.easemob.easeui.utils.EaseSmileUtils;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.constant.ThemeConstant;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.ChatSettingsManager;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.news.NewsContentProvider;
import com.qingqing.base.news.NewsConversation;
import com.qingqing.base.news.NewsDateUtils;
import com.qingqing.base.news.NewsManager;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.base.view.badge.StrokeBadgeView;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.qingqingbase.ui.BaseFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by xiejingwen on 2017/8/7.
 */

public class SearchFragment extends BaseFragment {
    private int mThemeColor;
    private ListView mSearchListView;
    private List<NewsConversation> mTotalConversationList;
    private List<NewsConversation> mConversationList;
    private BaseAdapter<NewsConversation> mAdapter;
    private String mSearchText;
    private NewsContentProvider mNewsContentProvider;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mThemeColor = ThemeConstant.getThemeColor(getContext());
        mTotalConversationList = NewsManager.getInstance().getConversationManager()
                .getAllConversations();
        mConversationList = new ArrayList<>();
        LimitEditText editTextSearch = (LimitEditText) view.findViewById(R.id.text_search);
        TextView textViewCancel = (TextView) view.findViewById(R.id.text_cancel);
        textViewCancel.setTextColor(mThemeColor);
        mSearchListView = (ListView) view.findViewById(R.id.list_search);
        textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        editTextSearch.addTextChangedListener(new LimitedTextWatcher() {
            @Override
            public void afterTextChecked(Editable s) {
                if (s != null) {
                    mSearchText = s.toString();
                    search();
                }
            }
        });
        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsConversation newsConversation = (NewsConversation) parent.getAdapter().getItem(position);
                NewsManager.getInstance().getActivitySkipper().gotoActivity(SearchFragment.this, newsConversation);
                getActivity().finish();
            }
        });
            
    }
    
    private void search() {
        if (mAdapter == null) {
            mAdapter = new SearchAdapter(getContext(), mConversationList);
            mSearchListView.setAdapter(mAdapter);
        }
        if(mNewsContentProvider == null){
            mNewsContentProvider = NewsManager.getInstance().getContentProvider();
        }
        mConversationList.clear();
        if (!TextUtils.isEmpty(mSearchText)) {
            for (NewsConversation newsConversation : mTotalConversationList) {
                String title = mNewsContentProvider.getConversationTitle(getContext(),
                        newsConversation);
                if (title.contains(mSearchText)) {
                    mConversationList.add(newsConversation);
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }
    
    private class SearchAdapter extends BaseAdapter<NewsConversation> {
        
        public SearchAdapter(Context context, List list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_search, parent,
                    false);
        }
        
        @Override
        public ViewHolder createViewHolder() {
            return new SearchHolder();
        }
        
        private class SearchHolder extends ViewHolder<NewsConversation> {
            private AsyncImageViewV2 iconView;
            private TextView titleTextView;
            private TextView subtitleTextView;
            private TextView contentTextView;
            private TextView dateTextView;
            private TextView statusTextView;
            
            private StrokeBadgeView mDigitRemainView;
            private View mRemainView;
            
            private View mContentContainer;
            
            @Override
            public void init(Context context, View convertView) {
                iconView = (AsyncImageViewV2) convertView
                        .findViewById(R.id.img_conversation_icon);
                titleTextView = (TextView) convertView
                        .findViewById(R.id.tv_conversation_title);
                subtitleTextView = (TextView) convertView
                        .findViewById(R.id.tv_conversation_subtitle);
                contentTextView = (TextView) convertView
                        .findViewById(R.id.tv_conversation_content);
                dateTextView = (TextView) convertView
                        .findViewById(R.id.tv_conversation_date);
                
                mDigitRemainView = (StrokeBadgeView) convertView
                        .findViewById(R.id.view_unread_digit_remain);
                mRemainView = convertView.findViewById(R.id.img_unread_remain);
                
                mContentContainer = convertView
                        .findViewById(R.id.container_conversation_content);
                
                statusTextView = (TextView) convertView
                        .findViewById(R.id.tv_conversation_status);
            }
            
            @Override
            public void update(Context context, NewsConversation data) {
                NewsContentProvider provider = NewsManager.getInstance()
                        .getContentProvider();
                iconView.setImageUrl(provider.getConversationIconUrl(context, data),
                        provider.getConversationDefaultIcon(context, data));
                String title = provider.getConversationTitle(context, data);
                if (title.contains(mSearchText)) {
                    int index = title.indexOf(mSearchText);
                    SpannableString spannableString = new SpannableString(title);
                    spannableString.setSpan(new ForegroundColorSpan(mThemeColor), index,
                            index + mSearchText.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    titleTextView.setText(spannableString);
                }
                else {
                    titleTextView.setText(title);
                }
                dateTextView.setText(NewsDateUtils.getDateTextNew(data.getLastNewsTime()));
                
                boolean displayContent = data.isDisplayContent();
                
                mContentContainer
                        .setVisibility(displayContent ? View.VISIBLE : View.GONE);
                dateTextView.setVisibility(displayContent ? View.VISIBLE : View.GONE);
                
                boolean hasStatus = !TextUtils.isEmpty(data.getStatus());
                statusTextView.setVisibility(hasStatus ? View.VISIBLE : View.GONE);
                statusTextView.setText(hasStatus ? data.getStatus() : "");
                
                if (!TextUtils.isEmpty(data.getSubName())) {
                    subtitleTextView.setText("(" + data.getSubName() + ")");
                }
                else {
                    subtitleTextView.setText("");
                }
                
                String conversationContent = provider.getConversationContent(context,
                        data);
                int unreadNewsCount = data.getUnreadNewsCount();
                boolean hasUnread = unreadNewsCount > 0;
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                    
                    contentTextView.setText(
                            EaseSmileUtils.getSmiledText(context, conversationContent),
                            TextView.BufferType.SPANNABLE);
                    
                    mDigitRemainView.setBadgeCount(unreadNewsCount);
                    mDigitRemainView.setVisibility(
                            provider.isUnreadDigitMode(data.getType()) && hasUnread
                                    ? View.VISIBLE : View.GONE);
                    
                    mRemainView.setVisibility(
                            !provider.isUnreadDigitMode(data.getType()) && hasUnread
                                    ? View.VISIBLE : View.GONE);
                }
                else {
                    // 5.8.0新增群置顶、免打扰、@功能
                    
                    String groupId = data.getId();
                    boolean notNotify = ChatSettingsManager.getInstance()
                            .isMsgNotificationIgnored(groupId);
                    boolean showUnreadDigit = provider.isUnreadDigitMode(data.getType());
                    boolean hasAt = EaseAtMessageHelper.get().hasAtMeMsg(groupId);
                    boolean hasAnnounce = ChatManager.getInstance()
                            .hasNewGroupAnnounce(groupId);
                    boolean hasGroupRank = ChatManager.getInstance()
                            .hasNewGroupRank(groupId);
                    
                    if (hasAt) {
                        String at = context.getString(R.string.chat_at_by_sb);
                        String text = at + conversationContent;
                        Spannable spannable = EaseSmileUtils.getSmiledText(context, text);
                        spannable.setSpan(
                                new ForegroundColorSpan(
                                        context.getResources().getColor(R.color.tip_red)),
                                0, at.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        contentTextView.setText(spannable, TextView.BufferType.SPANNABLE);
                    }
                    else if (hasAnnounce) {
                        // 群公告 @5.8.5
                        String announce = context
                                .getString(R.string.chat_new_group_announce);
                        String text = announce + conversationContent;
                        Spannable spannable = EaseSmileUtils.getSmiledText(context, text);
                        spannable.setSpan(
                                new ForegroundColorSpan(
                                        context.getResources().getColor(R.color.tip_red)),
                                0, announce.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        contentTextView.setText(spannable, TextView.BufferType.SPANNABLE);
                    }
                    else if (hasGroupRank) {
                        String text;
                        // 排行榜
                        String rankString = context.getString(R.string.trm_group_rank);
                        if (rankString.equals(conversationContent)) {
                            // 最近一条消息为排行榜消息则只显示排行榜
                            text = rankString;
                        }
                        else {
                            text = rankString + conversationContent;
                        }
                        Spannable spannable = EaseSmileUtils.getSmiledText(context, text);
                        spannable.setSpan(
                                new ForegroundColorSpan(
                                        context.getResources().getColor(R.color.tip_red)),
                                0, rankString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        contentTextView.setText(spannable, TextView.BufferType.SPANNABLE);
                    }
                    else if (notNotify && hasUnread) {
                        String unreadCountStr = context
                                .getString(R.string.chat_unread_count, unreadNewsCount);
                        contentTextView.setText(
                                EaseSmileUtils.getSmiledText(context,
                                        unreadCountStr + conversationContent),
                                TextView.BufferType.SPANNABLE);
                    }
                    else {
                        contentTextView.setText(
                                EaseSmileUtils.getSmiledText(context,
                                        conversationContent),
                                TextView.BufferType.SPANNABLE);
                    }
                    
                    if (notNotify) {
                        Drawable drawable = context.getResources()
                                .getDrawable(provider.getMessageFreeIcon());
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight());
                        titleTextView.setCompoundDrawables(null, null, drawable, null);
                        mRemainView.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                        mDigitRemainView.setVisibility(View.GONE);
                    }
                    else {
                        if (hasUnread) {
                            if (showUnreadDigit) {
                                mRemainView.setVisibility(View.GONE);
                                mDigitRemainView.setVisibility(View.VISIBLE);
                                mDigitRemainView.setBadgeCount(unreadNewsCount);
                            }
                            else {
                                mRemainView.setVisibility(View.VISIBLE);
                                mDigitRemainView.setVisibility(View.GONE);
                            }
                        }
                        else {
                            mRemainView.setVisibility(View.GONE);
                            mDigitRemainView.setVisibility(View.GONE);
                        }
                        titleTextView.setCompoundDrawables(null, null, null, null);
                    }
                }
            }
        }
    }
}
