package com.qingqing.project.offline.groupchat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

/**
 * 群公告编辑
 */

public class BaseGroupChatAnnouncementEditFragment extends BaseFragment {

    public static final int MAX_TEXT_NUM = 2000;

    private LimitEditText mEtContent;
    private TextView mTvTextCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_chat_announcement_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mEtContent = (LimitEditText) view.findViewById(R.id.et_content);
        mTvTextCount = (TextView) view.findViewById(R.id.tv_text_count);
        String content = getArguments().getString(BaseParamKeys.PARAM_STRING_GROUP_CHAT_ANNOUNCEMENT_CONTENT);
        mEtContent.setText(content == null ? "" : content);
        showWordsCount();
        addListener();
        forceShowKeyBoard(mEtContent);
    }


    private void forceShowKeyBoard(LimitEditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(editText)) {
            return;
        }
        imm.toggleSoftInput(0,InputMethodManager.SHOW_FORCED);
    }
    int currentWordsCount;

    private void addListener() {
        mEtContent.addTextChangedListener(new LimitedTextWatcher(MAX_TEXT_NUM, LimitedTextWatcher.FilterMode.NONE) {
            @Override
            public void afterTextChecked(Editable s) {
                showWordsCount();
                if (currentWordsCount == MAX_TEXT_NUM) {
                    ToastWrapper.showWithIcon(R.string.text_counts_exceeding_limits, R.drawable.icon_task_warning);
                }else{
                    String inputText = mEtContent.getText().toString();
                    if (mFragListener != null && mFragListener instanceof onTextChangeFragmentListener) {
                        ((onTextChangeFragmentListener) mFragListener).onTextChanged(inputText);
                    }
                }

            }
        });
    }

    public static class onTextChangeFragmentListener implements FragListener {
        @Override
        public void onStart() {
        }

        @Override
        public void onStop() {
        }

        public void onTextChanged(String text) {

        }

    }


    private void showWordsCount() {
        String inputText = mEtContent.getText().toString();
        currentWordsCount = inputText.length();
        String inputLength = String.valueOf(currentWordsCount);
        SpannableString spannableString = new SpannableString(inputLength + " | " + MAX_TEXT_NUM);
        if (currentWordsCount == MAX_TEXT_NUM) {
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, inputLength.length(), spannableString.length());
        } else {
            spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, inputLength.length(), spannableString.length());
        }
        mTvTextCount.setText(spannableString);
    }

    @Nullable
    public LimitEditText getEtContent() {
        return mEtContent;
    }
}
