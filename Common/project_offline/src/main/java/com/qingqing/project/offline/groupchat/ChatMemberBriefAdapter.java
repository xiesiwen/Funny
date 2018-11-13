package com.qingqing.project.offline.groupchat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.OnItemClickListener;
import com.qingqing.project.offline.R;

import java.util.List;

import static com.qingqing.project.offline.groupchat.BaseGroupMemberListActivity.isAdmin;

/**
 * 群聊设置头部成员Adapter
 */

public class ChatMemberBriefAdapter extends RecyclerView.Adapter<ChatMemberBriefAdapter.ChatMemberBriefHolder> {

    List<ChatBean> mList;

    public static final int TYPE_MEMBER = 0;

    public static final int TYPE_ADD = 1;

    public static final int TYPE_DELETE = 2;

    public static final int MAX_NUM = 10; //adapter中最多有10个

    OnItemClickListener listener;

    boolean isAdmin;

    /**
     * @param list
     * @param itemClickListener
     * @param admin             如果不是Admin的话，进设置页面，是没有加号和减号的
     */
    public ChatMemberBriefAdapter(List<ChatBean> list, @NonNull OnItemClickListener itemClickListener, boolean admin) {
        this.listener = itemClickListener;
        this.mList = list;
        this.isAdmin = admin;
    }

    @Override
    public ChatMemberBriefHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat_setting_member_brief, parent, false);
        return new ChatMemberBriefHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(ChatMemberBriefHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_MEMBER:
                ChatBean bean = mList.get(position);
                if (bean != null) {
                    UserProto.ChatUserInfo info = null;
                    if (bean.userInfoAdmin != null) {
                        info = bean.userInfoAdmin.chatUserInfo;
                    } else if (bean.userInfo != null) {
                        info = bean.userInfo;
                    }
                    if (info != null) {
                        holder.bindData(info);
                    }
                }
                break;
            case TYPE_ADD:
                holder.showAddIcon();
                break;
            case TYPE_DELETE:
                holder.showDeleteIcon();
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!isAdmin) { //不是管理员就不显示加号和减号
            return TYPE_MEMBER;
        }
        int count = getItemCount();
        if (count == 1 || mList == null) {
            return TYPE_ADD;
        } else if (position < count - 2) {
            return TYPE_MEMBER;
        } else if (position == count - 2) {
            return TYPE_ADD;
        } else {
            return TYPE_DELETE;
        }
    }

    @Override
    public int getItemCount() { //既然是一个群，那么至少得有两个人
        int result = 0;
        if (!isAdmin) { //不是管理员，不显示加号和减号
            result = mList == null ? 0 : mList.size();
            return result > MAX_NUM ? MAX_NUM : result;
        }
        if (mList == null || mList.size() == 0) {
            return 1;
        } else {
            result = mList.size() + 2;
            return result > 10 ? MAX_NUM : result;
        }
    }

    static class ChatMemberBriefHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        AsyncImageViewV2 mIvAvatar;
        TextView mTvNick;
         ImageView mIvAdminTag;


        OnItemClickListener mListener;

        public ChatMemberBriefHolder(View itemView, @NonNull OnItemClickListener itemClickListener) {
            super(itemView);
            mIvAvatar = (AsyncImageViewV2) itemView.findViewById(R.id.iv_avatar);
            mIvAdminTag = (ImageView) itemView.findViewById(R.id.iv_admin_tag);
            mTvNick = (TextView) itemView.findViewById(R.id.tv_nick);

            itemView.setOnClickListener(this);
            mListener = itemClickListener;
        }

        void bindData(UserProto.ChatUserInfo data) {
            AppUtil.setVisibilityVisbleOrGoneForViewsArray(true,  mTvNick);
            mIvAvatar.setImageUrl(ImageUrlUtil.getHeadImg(data.newHeadImage), LogicConfig.getDefaultHeadIcon(data.sex));
            if (isAdmin(data)) {
                mIvAdminTag.setVisibility(View.VISIBLE);
            } else {
                mIvAdminTag.setVisibility(View.GONE);
            }
            String nick = TextUtils.isEmpty(data.nick) ? "" : data.nick;
            if (nick.length() > 3) {
                nick = nick.substring(0, 3)+"...";//595要求最多显示三个字，超过三个字点点点
            }
            mTvNick.setText(nick);
        }

        void showAddIcon() {
            mIvAvatar.setImageResource(R.drawable.chat_member_add);
            AppUtil.setVisibilityVisbleOrGoneForViewsArray(false, mIvAdminTag, mTvNick);
        }

        void showDeleteIcon() {
            mIvAvatar.setImageResource(R.drawable.chat_member_jian);
            AppUtil.setVisibilityVisbleOrGoneForViewsArray(false, mIvAdminTag, mTvNick);
        }



        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }
    }
}

