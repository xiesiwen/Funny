package com.qingqing.base.news.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.bean.UserBehaviorLogExtraData;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.fragment.PtrListFragment;
import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.log.Logger;
import com.qingqing.base.news.NewsComparator;
import com.qingqing.base.news.News;
import com.qingqing.base.news.NewsConversation;
import com.qingqing.base.news.NewsConversationType;
import com.qingqing.base.news.NewsManager;

import java.util.Collections;
import java.util.List;

/**
 * 系统消息界面
 *
 * add by tanwei @6.3
 */
public class SystemNewsListFragment extends PtrListFragment {

    private static final String TAG = "NewsListFragment";

    private String mConversationId;
    private String mConversationType;
    private NewsConversation mConversation;

    private List<News> mNewsList;
    private SystemNewsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_system_news_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConversationId = getArguments().getString(BaseParamKeys.PARAM_STRING_NEWS_CONVERSATION_ID);
        mConversationType = getArguments().getString(BaseParamKeys.PARAM_STRING_NEWS_CONVERSATION_TYPE);
        NewsConversation conversation = NewsManager.getInstance().getConversationManager().getConversation(mConversationId);

        setTitle(mConversationType);

        mNewsList = conversation.getAllNews();
        mConversation = conversation;

        Collections.sort(mNewsList, new NewsComparator());

        if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
            mAdapter = new SystemNewsAdapterStu(getActivity(), mConversationType,
                    mNewsList);
        }
        else {
            mAdapter = new SystemNewsAdapter(getActivity(), mConversationType, mNewsList);
        }

        mPtrListView.setAdapter(mAdapter);

        mPtrListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News news = mNewsList.get(position);
                mAdapter.newsClicked(news);
                if(NewsManager.getInstance().getContentProvider().isUnreadDigitMode(mConversationType)) {
                    mConversation.markNewsAsRead(news);
                }

                NewsManager.getInstance().getActivitySkipper().gotoActivity(getActivity(), news);

                if (!TextUtils.isEmpty(getPageId())) {
                    UserBehaviorLogManager.INSTANCE().saveClickLog(getPageId(),
                            StatisticalDataConstants.CLICK_ME_MESSAGE_MESSAGE,new UserBehaviorLogExtraData.Builder()
                                    .addExtraData(StatisticalDataConstants.LOG_EXTRA_E_OBJECT_ID,news.getId()).build());
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(getPageId())) {
            UserBehaviorLogManager.INSTANCE().savePageLog(getPageId());
        }
    }
    
    private String getPageId() {
        String pageId = "";
        switch (NewsConversationType.mapStringToValue(mConversationType)) {
            case TODO:
                pageId = StatisticalDataConstants.LOG_PAGE_BACKLOG_MESSAGE;
                break;
            case TEACHING_TASK:
                pageId = StatisticalDataConstants.LOG_PAGE_TEACHING_TASK_MESSAGE;
                break;
            case NOTIFICATION:
                pageId = StatisticalDataConstants.LOG_PAGE_INFORM_MESSAGE;
                break;
            case REFUND:
                pageId = StatisticalDataConstants.LOG_PAGE_REFUND_MESSAGE;
                break;
            case ACTIVITY:
                pageId = StatisticalDataConstants.LOG_PAGE_ACTIVITY_MESSAGE;
                break;
        }

        return pageId;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mConversation != null) {
            mConversation.markAllNewsAsRead();
        }
    }

    @Override
    public void onRefreshFromEnd(String tag) {
        Logger.e(TAG, "onRefreshFromEnd");
        loadMoreNews();
    }

    private void loadMoreNews() {
        mConversation.loadMoreNews(new Observer<List<News>>() {
            @Override
            public void onCompleted() {
                Logger.i(TAG, "onCompleted");
                if(!couldOperateUI()) {
                    return;
                }
                finishRefresh(true);
            }

            @Override
            public void onError(Throwable e) {
                Logger.e(TAG, "onError", e);
                if(!couldOperateUI()) {
                    return;
                }
                finishRefresh(false);
            }

            @Override
            public void onNext(List<News> newses) {
                Logger.i(TAG, "onNext");
                if(!couldOperateUI()) {
                    return;
                }
                mNewsList.addAll(newses);
                Collections.sort(mNewsList, new NewsComparator());
                mAdapter.notifyDataSetChanged();
            }
        });
    }

}
