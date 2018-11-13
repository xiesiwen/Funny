package com.qingqing.project.offline.groupchat;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.ChatSettingsManager;
import com.qingqing.base.dialog.CompDefaultDialogBuilder;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.setting.SettingToggleValueItem;
import com.qingqing.base.view.setting.SimpleSettingItem;
import com.qingqing.project.offline.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dubo on 15/12/23.
 */
@Deprecated
public abstract class GroupChatSettingFragment extends BaseFragment {

    private ExpandGridView mMemberGirdView;
    private MemberAdapter memberAdapter;
    private List<UserProto.ChatUserInfo> mMemList;
    private SimpleSettingItem mGroupId;
    private SimpleSettingItem mGroupNum;
    private SimpleSettingItem mGroupNick;
    // 消息免打扰
    private SettingToggleValueItem mIgnoreMsgItem;
    private TextView mSubmit;
    private int mAppType;
    private String GroupId;
    private String exitStr;

    public interface SetingListener extends FragListener {
        void toEditNameFragment(String content);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            GroupId = bundle.getString("groupId");
        } else {
            ToastWrapper.show("组ID不正确");
            return;
        }

        mAppType = BaseData.getClientType();
        mGroupId = (SimpleSettingItem) view.findViewById(R.id.group_id);
        mGroupNum = (SimpleSettingItem) view.findViewById(R.id.group_num);
        mSubmit = (TextView) view.findViewById(R.id.disband_group_submit);
        mGroupNick = (SimpleSettingItem) view.findViewById(R.id.group_name);
        mIgnoreMsgItem = (SettingToggleValueItem) view.findViewById(R.id.item_ignore_msg);
        mIgnoreMsgItem.setVisibility(mAppType == AppCommon.AppType.qingqing_teacher ? View.VISIBLE : View.GONE);
        mIgnoreMsgItem.setChecked(ChatSettingsManager.getInstance().isMsgNotificationIgnored(GroupId));

        mIgnoreMsgItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ChatSettingsManager.getInstance().ignoreMsgNotification(GroupId);
                } else {
                    ChatSettingsManager.getInstance().unignoreMsgNotification(GroupId);
                }
            }
        });

        mMemList = new ArrayList<>();
        mMemberGirdView = (ExpandGridView) view.findViewById(R.id.gv_member_list);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int itemWidth = (width - 5 * (5 - 1)) / 5;// 格子宽度
        mMemberGirdView.setColumnWidth(itemWidth); // 设置列表项宽
        mMemberGirdView.setHorizontalSpacing(5); // 设置列表项水平间距
        mMemberGirdView.setStretchMode(GridView.NO_STRETCH);
        mMemberGirdView.setNumColumns(5); // 设置列数量=列表集合数

        memberAdapter = new MemberAdapter(getActivity(), R.layout.views_chat_circle_head,
                mMemList);
        mMemberGirdView.setAdapter(memberAdapter);

        initData();

        exitStr = getString(R.string.confirm_exit_group);
        switch (mAppType) {
            case AppCommon.AppType.qingqing_student:
                mGroupNick.setHasActionV2(false);
                mGroupId.setHasActionV2(false);
                mGroupNum.setHasActionV2(false);
                mSubmit.setText(R.string.im_logout_group);
                break;
            case AppCommon.AppType.qingqing_teacher:
                mSubmit.setText(R.string.im_logout_group);
                mGroupNick.setHasActionV2(false);
                mGroupId.setHasActionV2(false);
                mGroupNum.setHasActionV2(false);
                break;
            case AppCommon.AppType.qingqing_ta:
                mSubmit.setText(R.string.im_disband_group);
                mGroupNick.setHasAction(true);
                mGroupId.setHasAction(false);
                mGroupNum.setHasAction(false);
                exitStr = getString(R.string.confirm_disband_group);
                mGroupNick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mFragListener != null) {
                            ((SetingListener) mFragListener)
                                    .toEditNameFragment(mGroupNick.getValue().toString());
                        }
                    }
                });

                mMemberGirdView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (memberAdapter.isInDelMode) {
                                    memberAdapter.isInDelMode = false;
                                    memberAdapter.notifyDataSetChanged();
                                    return true;
                                }
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                break;

        }

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new CompDefaultDialogBuilder(getActivity())
                        .setTitle(R.string.ind_dialog_title)
                        .setContent(exitStr)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,
                                                        int i) {
                                        dialogInterface.dismiss();
                                        exitGroup(mAppType);
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,
                                                        int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).setCancelable(false).show();

            }
        });

    }

    protected void onEnterToUserDetail(UserProto.ChatUserInfo chatUserInfo) {

    }

    public List<UserProto.ChatUserInfo> getMemList() {
        return mMemList;
    }

    private void initData() {
        ImProto.SimpleChatGroupIdRequest simpleChatGroupIdRequest = new ImProto.SimpleChatGroupIdRequest();
        simpleChatGroupIdRequest.chatGroupId = GroupId;
        newProtoReq(CommonUrl.CHAT_GROUP_INFO_URL.url()).setSendMsg(simpleChatGroupIdRequest)
                .setRspListener(new ProtoListener(ImProto.ChatGroupDetailResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        ImProto.ChatGroupDetailResponse chatGroupDetailResponse = (ImProto.ChatGroupDetailResponse) result;
                        mGroupNum.setValue(String.valueOf(chatGroupDetailResponse
                                .members.length));
                        mGroupNick.setValue(chatGroupDetailResponse.chatGroupName);
                        mGroupId.setValue(chatGroupDetailResponse.chatGroupId);
                        for (UserProto.ChatUserInfo chatUserInfo : chatGroupDetailResponse.members) {
                            mMemList.add(chatUserInfo);
                        }
                        updateUserInfo();
                        memberAdapter.notifyDataSetChanged();
                        Logger.d("groupid==" + chatGroupDetailResponse.chatGroupId);
                    }
                }).req();
    }

    class MemberAdapter extends ArrayAdapter<UserProto.ChatUserInfo> {
        private int res;
        private boolean isInDelMode;
        private List<UserProto.ChatUserInfo> objects;

        public MemberAdapter(Context context, int resource,
                             List<UserProto.ChatUserInfo> objects) {
            super(context, resource, objects);
            this.objects = objects;
            res = resource;
            isInDelMode = false;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(res, null);
                holder.headImageView = (AsyncImageViewV2) convertView
                        .findViewById(R.id.chat_head);
                holder.textView = (TextView) convertView.findViewById(R.id.chat_nick);
                holder.badgeDeleteView = (ImageView) convertView
                        .findViewById(R.id.badge_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position == getCount() - 1) {
                if (BaseData.getClientType() != AppCommon.AppType.qingqing_ta) {
                    // 如果不是创建者或者没有相应权限，不提供加减人按钮,不提供加人按钮
                    convertView.setVisibility(View.INVISIBLE);
                } else {
                    if (isInDelMode) {
                        convertView.setVisibility(View.INVISIBLE);
                    } else {
                        convertView.setVisibility(View.VISIBLE);
                        // holder.headImageView.setImageUrl("",
                        // R.drawable.icon_minususer);
                        holder.headImageView.setImageResource(R.drawable.icon_minususer);
                        convertView.findViewById(R.id.badge_delete).setVisibility(
                                View.INVISIBLE);
                        holder.textView.setVisibility(View.GONE);
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                isInDelMode = true;
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            } else if (position == getCount() - 2) {
                // 如果不是创建者或者没有相应权限
                if (BaseData.getClientType() != AppCommon.AppType.qingqing_ta) {
                    convertView.setVisibility(View.INVISIBLE);
                } else {
                    if (isInDelMode) {
                        convertView.setVisibility(View.INVISIBLE);
                    } else {
                        convertView.setVisibility(View.VISIBLE);
                        convertView.findViewById(R.id.badge_delete).setVisibility(
                                View.INVISIBLE);
                        holder.headImageView.setImageResource(R.drawable.icon_adduser);
                        holder.textView.setVisibility(View.GONE);
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AddUser();

                            }
                        });
                    }
                }
            } else {
                if (isInDelMode) {
                    // 如果是删除模式下，显示减人图标
                    convertView.findViewById(R.id.badge_delete).setVisibility(
                            View.VISIBLE);
                } else {
                    convertView.findViewById(R.id.badge_delete).setVisibility(
                            View.INVISIBLE);
                }
                convertView.setVisibility(View.VISIBLE);
                String headImgUrl = ImageUrlUtil.getHeadImg(objects.get(position)
                        .newHeadImage);
                holder.textView.setVisibility(View.VISIBLE);
                holder.headImageView.setImageUrl(headImgUrl,
                        LogicConfig.getDefaultHeadIcon(objects.get(position).sex));
                holder.textView.setText(objects.get(position).nick);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isInDelMode) {
                            if (objects.get(position).qingqingUserId
                                    .equals(BaseData.qingqingUserId())) {
                                ToastWrapper.show("不能删除自己");
                            } else {
                                DelUser(objects.get(position), position);
                            }

                        } else {
                            if(BaseData.getClientType() == AppCommon.AppType.qingqing_teacher && objects.get(position).userType == UserProto.UserType.teacher) {
                                onAvatarClick(objects.get(position).qingqingUserId);
                            } else if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                                onAvatarClick(objects.get(position).qingqingUserId);
                            }
                        }
                    }
                });
            }
            return convertView;
        }

        @Override
        public int getCount() {
            Logger.w("getCount" + super.getCount() + 2);
            return super.getCount() + 2;

        }

    }

    private static class ViewHolder {
        AsyncImageViewV2 headImageView;
        TextView textView;
        ImageView badgeDeleteView;
    }

    protected abstract void AddUser();

    protected abstract void updateUserInfo();

    /**
     * 跳转详情 老师、助教
     *
     * @param data 手机号 轻轻userid
     */
    protected abstract void onAvatarClick(String data);

    private void exitGroup(int type) {

        switch (type) {
            case AppCommon.AppType.qingqing_student:
            case AppCommon.AppType.qingqing_teacher:
                final ImProto.SimpleChatGroupIdRequest simpleRequest = new ImProto.SimpleChatGroupIdRequest();
                simpleRequest.chatGroupId = GroupId;
                newProtoReq(CommonUrl.CHAT_EXIT_GROUP.url())
                        .setSendMsg(simpleRequest)
                        .setRspListener(
                                new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                                    @Override
                                    public void onDealResult(Object result) {
                                        ToastWrapper.show("退出群组成功");
                                        onExitGroup(GroupId);
                                        if (couldOperateUI()) {
                                            getActivity().finish();
                                        }
                                    }

                                    @Override
                                    public boolean onDealError(int errorCode,
                                                               Object result) {
                                        ToastWrapper.show(getErrorHintMessage("退出群组失败"));
                                        return true;
                                    }
                                }).req();
                break;
            case AppCommon.AppType.qingqing_ta:
                ImProto.SimpleChatGroupIdRequest simpleChatGroupIdRequest = new ImProto.SimpleChatGroupIdRequest();
                simpleChatGroupIdRequest.chatGroupId = GroupId;
                newProtoReq(CommonUrl.CHAT_GROUP_DISMISS.url())
                        .setSendMsg(simpleChatGroupIdRequest)
                        .setRspListener(
                                new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                                    @Override
                                    public void onDealResult(Object result) {
                                        onDisbandGroup(GroupId);
                                        if (couldOperateUI()) {
                                            getActivity().finish();
                                        }
                                    }

                                    @Override
                                    public boolean onDealError(int errorCode,
                                                               Object result) {
                                        ToastWrapper.show(getErrorHintMessage("解散群组失败"));
                                        return true;
                                    }
                                }).req();
                break;
        }

    }

    protected void onExitGroup(String groupId) {

    }

    protected void onDisbandGroup(String groupId) {

    }

    private void DelUser(UserProto.ChatUserInfo userInfo, final int pos) {
        ImProto.KickChatGroupMemberRequest kickChatGroupMemberRequest = new ImProto.KickChatGroupMemberRequest();
        kickChatGroupMemberRequest.chatGroupId = GroupId;
        kickChatGroupMemberRequest.qingqingUserIds = new String[]{userInfo.qingqingUserId};
        newProtoReq(CommonUrl.CHAT_KICK_USER.url()).setSendMsg(kickChatGroupMemberRequest)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {

                    @Override
                    public void onDealResult(Object result) {
                        if (pos < mMemList.size()) {
                            mMemList.remove(pos);
                        }
                        memberAdapter.isInDelMode = false;
                        memberAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public boolean onDealError(int errorCode, Object result) {

                        boolean ret = true;
                        switch (errorCode) {
                            case 1010:
                                ToastWrapper.show(getErrorHintMessage("群组不存在"));
                                break;
                            default:
                                ret = false;
                                break;
                        }

                        return ret;
                    }

                }).req();
    }

}
