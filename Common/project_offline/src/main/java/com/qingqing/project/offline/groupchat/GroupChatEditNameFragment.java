package com.qingqing.project.offline.groupchat;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.qingqingbase.R;

/**
 * Created by dubo on 15/12/22.
 */
@Deprecated
public class GroupChatEditNameFragment extends BaseFragment {
    protected static final int MENU_ITEM_SAVE = 10;
    LimitEditText editText;
    TextView tip;
    LimitedTextWatcher limitedTextWatcher;
    ImageView clear;

    public interface EditNameFragListener extends FragListener {
        void EditName(String name);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_my_profile_edit_info, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editText = (LimitEditText) view.findViewById(R.id.et_edit_info);
        tip = (TextView) view.findViewById(R.id.input_tips);
        clear = (ImageView)view.findViewById(R.id.input_clear);


        Bundle bundle = getArguments();
        if (bundle != null) {
            String content = (String) bundle.get("content");
            editText.setText(content);
            editText.setSelection(content.length());
        }

        tip.setText("请输入14个字以内中文、英文或数字。");
        limitedTextWatcher = new LimitedTextWatcher(14, LimitedTextWatcher.FilterMode.NO_EMOJI){
            @Override
            public void afterTextChecked(Editable s) {
                if (mFragListener != null) {
                    ((EditNameFragListener) mFragListener)
                            .EditName(editText.getText().toString());
                }
            }
        };
        editText.addTextChangedListener(limitedTextWatcher);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });
    }

    public String getContent() {
        return editText.getText().toString();
    }


}
