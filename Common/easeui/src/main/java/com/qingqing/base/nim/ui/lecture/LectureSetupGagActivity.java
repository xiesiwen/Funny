package com.qingqing.base.nim.ui.lecture;

import java.util.ArrayList;
import java.util.List;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.LectureProto;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.im.ChatRoomModel;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.AtMostListView;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.setting.SettingToggleValueItem;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Created by guohuifeng on 2016/5/25.
 * 禁言页面
 */
public class LectureSetupGagActivity extends BaseActionBarActivity{
    public final String TAG = "SetGagActivity";
    private SettingToggleValueItem mSetAllUserGag;
    private GagUserAdapter mAdapter = null;
    private AtMostListView mListView;
    private String mQingQingLectureId;
    private ArrayList<ImProto.StopTalkItem> mGagUserList = new ArrayList<>();
    private boolean mIsAllUserStopTalk;
    private View mGagTip;
    private String mChatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_setup_gag);
        initView();
        reqGagUserList();
    }

    private void initView() {
        mQingQingLectureId = getIntent().getStringExtra(BaseParamKeys.PARAM_STRING_LECTURE_ID);
        mChatRoomId = getIntent().getStringExtra(BaseParamKeys.PARAM_STRING_CHAT_ROOM_ID);
        mSetAllUserGag = (SettingToggleValueItem) findViewById(R.id.set_all_user_gag);
        mGagTip = findViewById(R.id.gag_tip);
        mAdapter = new GagUserAdapter(LectureSetupGagActivity.this,
                mGagUserList);
        mListView = (AtMostListView) findViewById(R.id.lv_gag_user);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (position >= 0 && position < mGagUserList.size()) {
                    allowUserTalk(mGagUserList.get(position));
                }
            }
        });
        mListView.setAdapter(mAdapter);
        mSetAllUserGag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (mIsAllUserStopTalk != arg1) {
                    stopAllUserTalk(arg1);
                }
            }
        });
    }

    /**
     * 获取禁言列表
     */
    public void reqGagUserList() {
        if (TextUtils.isEmpty(mQingQingLectureId)) {
            return;
        }
        mGagUserList.clear();
        LectureProto.SimpleQingQingLectureIdRequest request = new LectureProto.SimpleQingQingLectureIdRequest();
        request.qingqingLectureId = mQingQingLectureId;
        newProtoReq(CommonUrl.LECTURE_CHAT_ROOM_DETAIL.url()).setSendMsg(request).setRspListener(
                new ProtoListener(LectureProto.LectureChatRoomDetailResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        LectureProto.LectureChatRoomDetailResponse response = (LectureProto.LectureChatRoomDetailResponse) result;
                        parseRsp(response);
                    }

                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                                            int errorCode, Object result) {
                        Logger.w(TAG, "get gag user list error", error);
                    }
                }).req();
    }

    private void parseRsp(LectureProto.LectureChatRoomDetailResponse response){
        if(response == null){
            return;
        }
        mGagUserList.clear();
        if (couldOperateUI()) {
            mGagTip.setVisibility(response.stopTalkItems.length>0?View.VISIBLE:View.GONE);
            mIsAllUserStopTalk = response.isAllStopTalk;
            mSetAllUserGag.setChecked(mIsAllUserStopTalk);
            if (response.stopTalkItems.length > 0) {
                for (int i = 0; i < response.stopTalkItems.length; i++) {
                    ImProto.StopTalkItem stopTalkItem = response.stopTalkItems[i];
                    mGagUserList.add(stopTalkItem);
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }
    class GagUserAdapter extends BaseAdapter<ImProto.StopTalkItem> {

        public GagUserAdapter(Context context, List<ImProto.StopTalkItem> list) {
            super(context, list);
        }

        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_keynote_speaker, parent, false);
        }

        @Override
        public ViewHolder<ImProto.StopTalkItem> createViewHolder() {
            return new GagUserViewHolder();
        }

    }

    class GagUserViewHolder extends
            BaseAdapter.ViewHolder<ImProto.StopTalkItem>{

        AsyncImageViewV2 mHeadImg;
        TextView mNickName;
        TextView mRelieveGag;

        @Override
        public void init(Context context, View convertView) {
            mHeadImg = (AsyncImageViewV2) convertView.findViewById(R.id.user_avator);
            mNickName = (TextView) convertView.findViewById(R.id.user_nick);
            mRelieveGag = (TextView) convertView.findViewById(R.id.relieve_gag);
            mRelieveGag.setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.icon).setVisibility(View.GONE);
        }

        @Override
        public void update(Context context, ImProto.StopTalkItem data) {
            mHeadImg.setImageUrl(ImageUrlUtil.getHeadImg(data.stopTalkUserInfo), LogicConfig
                    .getDefaultHeadIcon(data.stopTalkUserInfo));
            String nick = data.stopTalkUserInfo.nick;
            if (!TextUtils.isEmpty(nick)) {
                mNickName.setText(nick);
            }
        }
    }

    /**
     * 取消禁言
     *
     */
    public void allowUserTalk(final ImProto.StopTalkItem stopTalkItem) {
        if(stopTalkItem == null || TextUtils.isEmpty(stopTalkItem.stopTalkUserInfo.qingqingUserId)){
            return;
        }
        final String qqUserId = stopTalkItem.stopTalkUserInfo.qingqingUserId;
        UserProto.QingQingUser user = new UserProto.QingQingUser();
        user.qingqingUserId = qqUserId;
        LectureProto.LectureTalkOperationRequest request = new LectureProto.LectureTalkOperationRequest();
        request.qingqingLectureId = mQingQingLectureId;
        request.userToStop = user;
        newProtoReq(CommonUrl.LECTURE_ALLOW_TALK_URL.url())
                .setSendMsg(request)
                .setRspListener(
                        new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                ChatRoomModel model = ChatRoomModel.getModel(mChatRoomId);
                                if(model != null) {
                                    model.removeSpeakForbiddenUser(qqUserId);
                                }
                                if(couldOperateUI()){
                                    if(mGagUserList.contains(stopTalkItem)){
                                        mGagUserList.remove(stopTalkItem);
                                        mGagTip.setVisibility(mGagUserList.size()>0?View.VISIBLE:View.GONE);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    ToastWrapper.show(getString(R.string.release_stop_talk_success));
                                }
                            }
                        }).req();
    }

    /**
     * 全员禁言
     */
    public void stopAllUserTalk(final boolean flag) {
        if (TextUtils.isEmpty(mQingQingLectureId)) {
            return;
        }
        HttpUrl urlConfig = null;
        if(flag){
            //全员禁言
            urlConfig = CommonUrl.LECTURE_STOP_TALK_URL.url();
        }else{
            urlConfig = CommonUrl.LECTURE_ALLOW_TALK_URL.url();
            //全员不禁言
        }
        if(urlConfig == null){
            return;
        }
        LectureProto.LectureTalkOperationRequest request = new LectureProto.LectureTalkOperationRequest();
        request.qingqingLectureId = mQingQingLectureId;
        newProtoReq(urlConfig).setSendMsg(request).setRspListener(
                new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        if (couldOperateUI()) {
                            if(flag){
                                ToastWrapper.show(getString(R.string.stop_all_talk_success));
                            }else{
                                ToastWrapper.show(getString(R.string.allow_all_talk_success));
                            }
                            mIsAllUserStopTalk = flag;
                            ChatRoomModel model = ChatRoomModel.getModel(mChatRoomId);
                            if(model != null) {
                                model.setSpeakAllForbidden(mIsAllUserStopTalk);
                            }
                            mSetAllUserGag.setChecked(mIsAllUserStopTalk);
                            reqGagUserList();
                        }
                    }

                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                                            int errorCode, Object result) {
                        Logger.w(TAG, "stop all user talk error", error);
                        mIsAllUserStopTalk = !flag;
                        if (couldOperateUI()) {
                            mSetAllUserGag.setChecked(mIsAllUserStopTalk);
                        }
                        super.onDealError(error, isParseOK, errorCode, result);
                    }
                }).req();
    }

}
