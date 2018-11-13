package com.qingqing.project.offline.groupchat;

import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 群聊天设置群名称页面
 */

public class BaseGroupChatEditNameActivity extends BaseActionBarActivity {
    protected String mGroupChatId;
    @NonNull
    protected String mGroupChatName;

    protected LimitEditText mEtEditInfo;
    protected ImageView mInputClear;
    protected TextView mInputTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.layout_my_profile_edit_info);
        initView();
        addListeners();
        updateUI();
    }

    @CallSuper
    protected void updateUI() {
        mEtEditInfo.setText(mGroupChatName);
        mEtEditInfo.setSelection(mGroupChatName.length());
        mInputTips.setText(R.string.text_edit_group_chat_name_input);
    }

    @CallSuper
    protected void addListeners() {
        mInputClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEtEditInfo.setText("");
            }
        });
        LimitedTextWatcher limitedTextWatcher = new LimitedTextWatcher(14, LimitedTextWatcher.FilterMode.NO_EMOJI){
            @Override
            public void afterTextChecked(Editable s) {
                mGroupChatName = mEtEditInfo.getText().toString();
            }
        };
        mEtEditInfo.addTextChangedListener(limitedTextWatcher);
    }

    @CallSuper
    protected void initView() {
        mEtEditInfo = (LimitEditText) findViewById(R.id.et_edit_info);
        mInputClear = (ImageView) findViewById(R.id.input_clear);
        mInputTips = (TextView) findViewById(R.id.input_tips);
    }

    @CallSuper
    protected void initData() {
        Intent intent = getIntent();
        mGroupChatId = intent.getStringExtra(BaseParamKeys.PARAM_STRING_CHAT_GROUP_ID);
        mGroupChatName = intent.getStringExtra(BaseParamKeys.PARAM_STRING_GROUP_CHAT_NAME);
    }

    /** 更改群名称
     * @param groupId
     * @param content
     */
    protected void saveGroupInfo(String groupId, String content) {
        ImProto.ChatGroupUpdateRequest chatGroupUpdateRequest = new ImProto.ChatGroupUpdateRequest();
        chatGroupUpdateRequest.chatGroupId = groupId;
        chatGroupUpdateRequest.chatGroupName = TextUtils.isEmpty(content) ? "" : content;
        newProtoReq(CommonUrl.CHAT_GROUP_UPDATE_INFO.url()).setSendMsg(chatGroupUpdateRequest)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        ToastWrapper.show("保存成功");
                        Intent intent = new Intent();
                        intent.putExtra(BaseParamKeys.PARAM_STRING_GROUP_CHAT_NAME, mEtEditInfo.getText().toString());
                        setResult(RESULT_OK,intent);
                        UIUtil.hideSoftInput(BaseGroupChatEditNameActivity.this);
                        finish();
                    }
                }).req();
    }
}
