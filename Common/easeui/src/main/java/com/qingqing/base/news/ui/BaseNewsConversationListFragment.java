package com.qingqing.base.news.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.easeui.R;
import com.easemob.easeui.model.EaseAtMessageHelper;
import com.easemob.easeui.utils.EaseSmileUtils;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.ChatSettingsManager;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.log.Logger;
import com.qingqing.base.news.NewsContentProvider;
import com.qingqing.base.news.NewsConversation;
import com.qingqing.base.news.NewsConversationComparator;
import com.qingqing.base.news.NewsConversationType;
import com.qingqing.base.news.NewsDateUtils;
import com.qingqing.base.news.NewsEvent;
import com.qingqing.base.news.NewsEventAction;
import com.qingqing.base.news.NewsEventListener;
import com.qingqing.base.news.NewsManager;
import com.qingqing.base.news.NewsUtils;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.badge.StrokeBadgeView;
import com.qingqing.qingqingbase.ui.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天列表界面
 *
 * Created by huangming on 2016/12/2.
 */

public class BaseNewsConversationListFragment extends BaseFragment {
    
    private ConversationAdapter mAdapter;
    protected List<NewsConversation> mConversationList = new ArrayList<>();
    protected ListView mConversationListView;
    private static final String TAG = BaseNewsConversationListFragment.class
            .getSimpleName();
    
    private final static int REQ_CODE_CHAT_NEWS = UIConstants.REQ_CODE_CHAT_NEWS;
    private final static int REQ_CODE_SYSTEM_NEWS = UIConstants.REQ_CODE_SYSTEM_NEWS;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_news_conversation_list, container,
                false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        final ListView conversationListView = (ListView) view
                .findViewById(R.id.list_news_conversation);
        
        mConversationListView = conversationListView;
        
        conversationListView.setOnItemClickListener(mItemClickListener);
        conversationListView.setOnItemLongClickListener(mItemLongClickListener);
        
        fillConversationList();
        
        mAdapter = new ConversationAdapter(getActivity(), mConversationList);
        conversationListView.setAdapter(mAdapter);
        NewsManager.getInstance().getEventNotifier().registerEventListener(
                mNewsEventListener, NewsEventAction.ConversationListChanged,
                NewsEventAction.LastNewsChanged, NewsEventAction.UnreadCountChanged);
    }

    protected void fillConversationList() {
        mConversationList.clear();
        onFillConversationList(mConversationList);
        mConversationList.addAll(
                NewsManager.getInstance().getConversationManager().getAllConversations());
        onConversationListFilled(mConversationList);
        Collections.sort(mConversationList, new NewsConversationComparator());
    }
    
    protected void onFillConversationList(List<NewsConversation> conversationList) {
        
    }
    
    protected void onConversationListFilled(List<NewsConversation> conversationList) {
        // 针对置顶的会话设置高优先级
        if (BaseData.getClientType() != AppCommon.AppType.qingqing_student) {
            for (NewsConversation conversation : conversationList) {
                if (ChatManager.getInstance().getGroupStickTop(conversation.getId())) {
                    conversation.setPriority(NewsConversation.TOP_NEWS_PRIORITY);
                }
            }
        }
    }
    
    protected List<NewsConversation> getConversationListFrom(
            List<NewsConversation> conversationList, String conversationType) {
        List<NewsConversation> conversations = new ArrayList<>();
        for (NewsConversation conversation : conversationList) {
            if (conversation != null && conversation.getType().equals(conversationType)) {
                conversations.add(conversation);
            }
        }
        return conversations;
    }
    
    protected NewsConversation getConversationFrom(
            List<NewsConversation> conversationList, String conversationId) {
        for (NewsConversation conversation : conversationList) {
            if (conversation != null && conversation.getId().equals(conversationId)) {
                return conversation;
            }
        }
        return null;
    }
    
    protected NewsConversation getConversationFrom(String conversationId) {
        return getConversationFrom(mConversationList, conversationId);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        NewsManager.getInstance().getConversationManager().loadAllChatConversations();
        ChatManager.getInstance().asyncFetchGroupsFromServer(null);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NewsManager.getInstance().getEventNotifier()
                .unregisterEventListener(mNewsEventListener);
    }
    
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NewsConversation conversation = mConversationList.get(position);
            NewsManager.getInstance().getActivitySkipper()
                    .gotoActivity(BaseNewsConversationListFragment.this, conversation);
        }
    };
    
    private AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                long id) {
            final NewsConversation conversation = mConversationList.get(position);
            return onConversationLongClick(conversation);
        }
    };
    
    protected boolean onConversationLongClick(NewsConversation conversation) {
        return false;
    }
    
    protected void deleteConversation(NewsConversation conversation) {
        final String conversationId = conversation.getId();
        final String conversationType = conversation.getType();
        if (NewsUtils.isChatType(conversationType)) {
            if (NewsConversationType.GROUP_CHAT.getValue()
                    .equals(conversation.getType())) {
                EaseAtMessageHelper.get().removeAtMeGroup(conversation.getId());
            }
            EMChatManager.getInstance().deleteConversation(conversationId);
        }
        NewsManager.getInstance().getConversationManager()
                .deleteConversation(conversationId);
    }
    
    protected NewsEventListener mNewsEventListener = new NewsEventListener() {
        @Override
        public void onEvent(NewsEvent event) {
            Logger.i(TAG, "onEvent : " + event);
            fillConversationList();
            notifyDataChanged();
        }
    };
    
    protected void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.i(TAG, "onActivityResult : " + requestCode);
        switch (requestCode) {
            case REQ_CODE_CHAT_NEWS:
                NewsManager.getInstance().getConversationManager()
                        .loadAllChatConversations();
                break;
            case REQ_CODE_SYSTEM_NEWS:
                break;
            default:
                break;
        }
    }
    
    static class ConversationAdapter extends BaseAdapter<NewsConversation> {
        
        public ConversationAdapter(Context context, List<NewsConversation> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_news_conversation,
                    parent, false);
        }
        
        @Override
        public ViewHolder<NewsConversation> createViewHolder() {
            return new ConversationHolder();
        }
    }
    
    static class ConversationHolder extends BaseAdapter.ViewHolder<NewsConversation> {
        
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
            dateTextView = (TextView) convertView.findViewById(R.id.tv_conversation_date);
            
            mDigitRemainView = (StrokeBadgeView) convertView
                    .findViewById(R.id.view_unread_digit_remain);
            // mDigitRemainView.setMaxPlusCount(9);
            mRemainView = convertView.findViewById(R.id.img_unread_remain);
            
            mContentContainer = convertView
                    .findViewById(R.id.container_conversation_content);
            
            statusTextView = (TextView) convertView
                    .findViewById(R.id.tv_conversation_status);
        }
        
        @Override
        public void update(Context context, NewsConversation data) {
            NewsContentProvider provider = NewsManager.getInstance().getContentProvider();
            iconView.setImageUrl(provider.getConversationIconUrl(context, data),
                    provider.getConversationDefaultIcon(context, data));
            titleTextView.setText(provider.getConversationTitle(context, data));
            dateTextView.setText(NewsDateUtils.getDateTextNew(data.getLastNewsTime()));
            
            boolean displayContent = data.isDisplayContent();
            
            mContentContainer.setVisibility(displayContent ? View.VISIBLE : View.GONE);
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
            
            String conversationContent = provider.getConversationContent(context, data);
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
                boolean hasGroupRank = ChatManager.getInstance().hasNewGroupRank(groupId);
                
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
                    String announce = context.getString(R.string.chat_new_group_announce);
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
                    String unreadCountStr = context.getString(R.string.chat_unread_count,
                            unreadNewsCount);
                    contentTextView.setText(
                            EaseSmileUtils.getSmiledText(context,
                                    unreadCountStr + conversationContent),
                            TextView.BufferType.SPANNABLE);
                }
                else {
                    contentTextView.setText(
                            EaseSmileUtils.getSmiledText(context, conversationContent),
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
