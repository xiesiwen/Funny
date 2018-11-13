package com.qingqing.base.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BaseAdapter;
import java.util.List;

/**
 * Created by 主讲人适配器 on 2016/5/28.
 */

public class KeyNotSpeakerAdapter extends BaseAdapter<UserProto.SimpleUserInfoV2> {

    public KeyNotSpeakerAdapter(Context context, List<UserProto.SimpleUserInfoV2> list) {
        super(context, list);
    }

    @Override
    public View createView(Context context, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_keynote_speaker, parent, false);
    }
    @Override
    public ViewHolder<UserProto.SimpleUserInfoV2> createViewHolder() {
        return new KeyNoteSpeakerViewHolder();
    }
    class KeyNoteSpeakerViewHolder extends
            BaseAdapter.ViewHolder<UserProto.SimpleUserInfoV2> implements View.OnClickListener {
        AsyncImageViewV2 mHeadImg;
        TextView mNickName;

        @Override
        public void init(Context context, View convertView) {
//            convertView.setOnClickListener(this);
            mHeadImg = (AsyncImageViewV2) convertView.findViewById(R.id.user_avator);
            mNickName = (TextView) convertView.findViewById(R.id.user_nick);
        }

        @Override
        public void update(Context context, UserProto.SimpleUserInfoV2 data) {
            mHeadImg.setImageUrl(ImageUrlUtil.getHeadImg(data), LogicConfig
                    .getDefaultHeadIcon(data));
            if(!TextUtils.isEmpty(data.nick)){
                mNickName.setText(data.nick);
            }
        }

        @Override
        public void onClick(View v) {
            if (position >= 0 && position < list.size()) {
//                Intent intent = new Intent(ChatSettingsActivity.this, ImageShowActivity.class);
//                startActivity(intent);
            }
        }
    }
    
}
