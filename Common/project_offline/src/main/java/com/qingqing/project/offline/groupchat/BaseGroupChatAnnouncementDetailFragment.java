package com.qingqing.project.offline.groupchat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.utils.ClipboardUtil;
import com.qingqing.base.utils.StringUtil;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;
import com.qingqing.qingqingbase.ui.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 群公告详情
 */

public class BaseGroupChatAnnouncementDetailFragment extends BaseFragment implements View.OnLongClickListener {

    ScrollView mScrollView;
    TextView mTvContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chat_announcement_detail, container, false);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
        mTvContent = (TextView) view.findViewById(R.id.tv_content);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String content = getArguments().getString(BaseParamKeys.PARAM_STRING_GROUP_CHAT_ANNOUNCEMENT_CONTENT);
        setText(content);
        mScrollView.setOnLongClickListener(this);
        mTvContent.setOnLongClickListener(this);
    }

    public void setText(String content) {
        if (mTvContent != null) {
            Pattern p = Pattern
                    .compile(StringUtil.PATTERN_URL_WITH_OR_WITHOUT_HTTP);
            Matcher m = p.matcher(content);
            List<Pair<Integer, String>> list = new ArrayList<>();
            int lastIndex = 0;
            while (m.find()) {
                String url = m.group();
                url = url.replaceAll("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]", "");
                int index = content.indexOf(url, lastIndex) ;
                if (index <0) {
                    int tempIndex = url.indexOf("http");
                    if (tempIndex < 0) {
                        tempIndex = url.indexOf("www");
                    }
                    if (tempIndex > 0) {
                        url = url.substring(tempIndex);
                    }
                    index = content.indexOf(url);
                }
                if (index > -1) {
                    Pair<Integer, String> pair = new Pair<>(content.indexOf(url, lastIndex), url);
                    lastIndex = pair.first + pair.second.length();
                    list.add(pair);
                }
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(content);
            for (Pair<Integer, String> pair : list) {
                String url = pair.second;
                int index = pair.first;
                CustomUrlSpan customUrlSpan = new CustomUrlSpan(getActivity(), url);
                spannableStringBuilder.setSpan(customUrlSpan, index,
                        index + url.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            mTvContent.setText(spannableStringBuilder);
            mTvContent.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.scrollview || v.getId() == R.id.tv_content) {
            String content = mTvContent.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                ClipboardUtil.copyToClipBoard(content);
                ToastWrapper.show(R.string.text_copied_to_clipboard);
                return true;
            }
        }
        return false;
    }

    @Nullable
    public TextView getTextView() {
        return mTvContent;
    }
}
