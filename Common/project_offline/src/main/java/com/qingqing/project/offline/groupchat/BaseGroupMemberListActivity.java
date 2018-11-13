package com.qingqing.project.offline.groupchat;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.api.proto.v1.im.TeachingResearchImProto;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.utils.DisplayUtil;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.base.view.OnItemClickListener;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.project.offline.R;
import com.qingqing.qingqingbase.ui.BaseActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 群聊天成员列表页面
 */

public class BaseGroupMemberListActivity extends BaseActivity implements OnItemClickListener, View.OnClickListener {

    /*必传*/
    protected String mGroupChatId;
    protected int[] userRole; //当前用户角色

    /*选传*/
    protected boolean isInDeleteMode; //是否是从群聊天设置点那个减号过来的
    protected boolean isForAt; // 是否是从聊天页面输入@过来的
    
    protected ArrayList<ChatBean> mList = new ArrayList<>(); // RecyclerView的数据源
    protected ArrayList<ChatBean> mListBackup = new ArrayList<>(); // 备份一份用在本地搜索时使用
    protected ArrayList<ChatBean> mDeleteList = new ArrayList<>(); // 要删除的用户的集合
    protected ChatMembersAdapter mAdapter;
    protected DeleteMemberAdapter mDeleteAdapter; //左上角那个横向的RecyclerView的Adapter

    // View的成员变量开始
    protected LinearLayout mLlRoot;
    protected RelativeLayout mRlTop;
    protected ImageView mIvBack;
    protected TextView mTvCancel;
    protected TextView mTvTitle;
    protected TextView mTvDelete;
    protected LinearLayout mLlSearchBarContainer;
    protected RecyclerView mRlDelete;
    protected ImageView mIvSearch;
    protected LimitEditText mEtSearch;
    protected ImageView mIvClearText;
    protected RecyclerView mRecyclerView;
    //View的成员变量结束

    //这俩Response只会在输入@d进入该页面的时候才不为Null，自己去服务器拉取的
    @Nullable
    protected TeachingResearchImProto.ChatGroupDetailForTeachingResearchGroupAdminResponse adminResponse; //教研管理群
    @Nullable
    protected ImProto.ChatGroupDetailResponse mResponse; //普通群

    protected int chatGroupType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.activity_group_chat_member_list);
        initView();
        addListeners();
        updateUI();
    }

    @UiThread
    @CallSuper
    protected void updateUI() {
        mAdapter = new ChatMembersAdapter(mList, this, isInDeleteMode);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new HorizontalDividerDecoration());
        if (isInDeleteMode) { //prep HorizontalRecyclerView
            if (mDeleteList == null) {
                mDeleteList = new ArrayList<>();
            }
        }
        if (!amIAdmin()) {
            mEtSearch.setHint(R.string.text_hint_search_nick);
        }

    }

    @CallSuper
    protected void addListeners() {
        mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = mEtSearch.getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        performLocalSearch(text);
                    }
                    return true;
                }
                return false;
            }
        });
        mEtSearch.addTextChangedListener(new LimitedTextWatcher() {
            @Override
            public void afterTextChecked(Editable s) {
                super.afterTextChecked(s);
                String text = mEtSearch.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    performLocalSearch(text);
                    if (mIvClearText.getVisibility() != View.VISIBLE) {
                        mIvClearText.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mIvClearText.getVisibility() != View.GONE) {
                        mIvClearText.setVisibility(View.GONE);
                    }
                    resumeFullList();
                }
            }
        });
        mIvClearText.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
        mTvDelete.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
    }

    protected void resumeFullList() {
        if (mListBackup != null) {
            if (mList == null) {
                mList = new ArrayList<>();
            } else {
                mList.clear();
            }
            mAdapter.queryText = null;
            mList.addAll(mListBackup);
            mAdapter.notifyDataSetChanged();
        }
    }

    @CallSuper
    protected void performLocalSearch(String text) {
        if (mListBackup != null) {
            int length = mListBackup.size();
            if (!TextUtils.isEmpty(text)) { //if it's an empty string , no ops
                if (mList == null) {
                    mList = new ArrayList<>();
                } else {
                    mList.clear();
                }
                List<ChatBean> tempList = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    ChatBean bean = mListBackup.get(i);
                    if (bean != null) {
                        if (bean.userInfo != null) {
                            if (bean.userInfo.nick.contains(text)) {
                                tempList.add(bean);
                            }
                        } else if (bean.userInfoAdmin != null) {
                            if (bean.userInfoAdmin.chatUserInfo.nick.contains(text)||(bean.userInfoAdmin.teacherExtend!=null&&
                                    bean.userInfoAdmin.teacherExtend.teacherRealName!=null&&
                                    bean.userInfoAdmin.teacherExtend.teacherRealName.contains(text))) {
                                tempList.add(bean);
                            }
                        }
                    }
                }
                if (tempList.size() == 0) {//
                    mList.add(new ChatBean(null, null));//没有匹配结果，显示用户不存在
                } else {
                    mList.addAll(tempList);
                }
                mAdapter.queryText = text;
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 从Intent中获取数据什么的,包括把RecyclerView用到的List也填满数据，考虑到删除模式下不显示管理员,顺便还把列表按照A-Z进行了排序
     */
    @CallSuper
    protected void initData() {
        Intent intent = getIntent();
        /*必传*/
        mGroupChatId = intent.getStringExtra(BaseParamKeys.PARAM_STRING_CHAT_GROUP_ID);//在群未生成的情况下可以为null
        userRole = intent.getIntArrayExtra(BaseParamKeys.PARAM_INT_CHAT_GROUP_USER_ROLE);

        /*选传*/
        isInDeleteMode = intent.getBooleanExtra(BaseParamKeys.PARAM_BOOLEAN_IS_DELETE_MODE, false);
        isForAt = intent.getBooleanExtra(BaseParamKeys.PARAM_BOLEAN_IS_FOR_AT, false);

        chatGroupType = intent.getIntExtra(BaseParamKeys.PARAM_INT_GROUP_TYPE, ImProto.ChatGroupType.normal_chat_group_type);
        List<ChatBean> rawList = intent.getParcelableArrayListExtra(BaseParamKeys.PARAM_PARACLE_GROUP_CHAT_MEMBERS); //群成员，已经排序完毕

        createOrClearList();

        boolean amIadmin = amIAdmin();

        if (rawList != null) {
            int length = rawList.size();
            if (amIadmin) {
                for (int i = 0; i < length; i++) {
                    ChatBean bean = rawList.get(i); //这个List包含了用户自己。
                    if (bean != null) {
                        if (isInDeleteMode) { //删除模式下，要将管理员从列表中隐藏。 删除模式下，因为能进来说明肯定是管理员，管理员被剔除掉，所以没问题
                            if (!TextUtils.isEmpty(mGroupChatId)) {//这个群已经创建
                                if (bean.userInfoAdmin != null && !isAdmin(bean.userInfoAdmin) && chatGroupType == ImProto.ChatGroupType.teaching_research_chat_group_type) {
                                    mList.add(bean);
                                } else if (bean.userInfo != null && !isAdmin(bean.userInfo) && chatGroupType == ImProto.ChatGroupType.normal_chat_group_type) {
                                    mList.add(bean);
                                }
                            }else if (bean.userInfo != null) {//这个群还未创建，这个时候mGroupId为null，未创建的时候是可以删人的，
                                // 删除自己以外的任何人，其实外面传进来的时候已经剔除了自己
                                mList.add(bean);
                            }
                        } else if (bean.userInfoAdmin != null&&chatGroupType== ImProto.ChatGroupType.teaching_research_chat_group_type) {
                            mList.add(bean);//教研管理群管理员
                        } else if (bean.userInfo != null) {
                            mList.add(bean);//普通群
                        }
                    }
                }
            } else  {
                for (int i = 0; i < length; i++) {
                    ChatBean bean = rawList.get(i);
                    if (isInDeleteMode) { //不会走到这里，因为其实删除模式只有admin才能进来
                        if (bean.userInfo!=null&&!isAdmin(bean.userInfo)) {
                            mList.add(bean);
                        }
                    } else {
                        mList.add(bean);
                    }
                }
            }
//            Collections.sort(mList, new NickComparator()); 因为群设置页面已经把群成员排序完毕了，所以群成员页面就不需要在排序了
            fillBackupList();
        } else if (isForAt) {//从群聊页面输入@过来的，需要自己拉列表,自己排序
            getGroupChatMemberList(mGroupChatId);
        }
    }

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();


    @CallSuper
    protected void getGroupChatMemberList(@NonNull String groupChatId) {
        if (!TextUtils.isEmpty(groupChatId)) {
            ImProto.SimpleChatGroupIdRequest request = new ImProto.SimpleChatGroupIdRequest();
            request.chatGroupId = groupChatId;
            if (amIAdmin()&&chatGroupType== ImProto.ChatGroupType.teaching_research_chat_group_type) {
                newProtoReq(CommonUrl.CHAT_GROUP_INFO_ADMIN.url()).setSendMsg(request)
                        .setRspListener(new ProtoListener(TeachingResearchImProto.ChatGroupDetailForTeachingResearchGroupAdminResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                if (couldOperateUI()) {
                                    adminResponse = (TeachingResearchImProto.ChatGroupDetailForTeachingResearchGroupAdminResponse) result;
                                    if (adminResponse != null&&adminResponse.members!=null) {
                                        final int length = adminResponse.members.length;
                                        mTvTitle.setText("群成员（" + length + "）");
                                        Disposable disposable = Observable.fromCallable(new Callable<List<ChatBean>>() {
                                            @Override
                                            public List<ChatBean> call() throws Exception {
                                                int length = adminResponse.members.length;
                                                List<ChatBean> tempList = new ArrayList<ChatBean>(length);
                                                String myId = BaseData.qingqingUserId();
                                                for (int i = 0; i < length; i++) {
                                                    TeachingResearchImProto.ChatUserInfoForTeachingResearchGroupAdmin admin = adminResponse.members[i];
                                                    if (admin != null) {
                                                        if (isForAt && myId != null && myId.equals(admin.chatUserInfo.qingqingUserId)) { //@的时候需要剔除掉自己
                                                            continue;
                                                        }
                                                        ChatBean bean = new ChatBean(null, admin);
                                                        tempList.add(bean);
                                                    }
                                                }
                                                Collections.sort(tempList, new NickComparator());

                                                return tempList;
                                            }
                                        }).doAfterNext(new Consumer<List<ChatBean>>() {
                                            @Override
                                            public void accept(@io.reactivex.annotations.NonNull List<ChatBean> chatBeen) throws Exception {
                                                ArrayList<UserProto.ChatUserInfo> userInfos = new ArrayList<>();
                                                for (int i = 0; i < adminResponse.members.length; i++) {
                                                    userInfos.add(
                                                            adminResponse.members[i].chatUserInfo);
                                                }
                                                saveGroupMemberContacts(userInfos.toArray(
                                                        new UserProto.ChatUserInfo[]{}));
                                            }
                                        }).subscribeOn(Schedulers.computation())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeWith(new DisposableObserver<List<ChatBean>>(){
                                                    @Override
                                                    public void onNext(List<ChatBean> chatBeen) {
                                                        createOrClearList();
                                                        mList.addAll(chatBeen);
                                                        updateUI();
                                                        fillBackupList();
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                    }
                                                });
                                        if (compositeDisposable != null) {
                                            compositeDisposable.add(disposable);
                                        }
                                    }
                                }
                            }
                        }).req();
            } else {
                newProtoReq(CommonUrl.CHAT_GROUP_INFO_URL.url()).setSendMsg(request)
                        .setRspListener(new ProtoListener(ImProto.ChatGroupDetailResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                if (couldOperateUI()) {
                                    mResponse = (ImProto.ChatGroupDetailResponse) result;
                                    if (mResponse != null && mResponse.members != null) {
                                        mTvTitle.setText("群成员（" + mResponse.members.length + "）");
                                    }
                                    if (mResponse != null && mResponse.members != null) {
                                        Disposable disposable = Observable.fromCallable(new Callable<List<ChatBean>>() {
                                            @Override
                                            public List<ChatBean> call() throws Exception {
                                                int length = mResponse.members.length;
                                                List<ChatBean> tempList = new ArrayList<ChatBean>(length);
                                                String myId = BaseData.qingqingUserId();
                                                for (int i = 0; i < length; i++) {
                                                    UserProto.ChatUserInfo info = mResponse.members[i];
                                                    if (isForAt && myId != null && myId.equals(mResponse.members[i].qingqingUserId)) { //@的时候需要剔除自己
                                                        continue;
                                                    }
                                                    ChatBean bean = new ChatBean(info, null);
                                                    tempList.add(bean);
                                                }
                                                Collections.sort(tempList, new NickComparator());

                                                return tempList;
                                            }
                                        }).doAfterNext(new Consumer<List<ChatBean>>() {
                                            @Override
                                            public void accept(@io.reactivex.annotations.NonNull List<ChatBean> chatBeen) throws Exception {
                                                saveGroupMemberContacts(mResponse.members);
                                            }
                                        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                                                .subscribeWith(new DisposableObserver<List<ChatBean>>() {
                                                    @Override
                                                    public void onNext(List<ChatBean> chatBeen) {
                                                        createOrClearList();
                                                        mList.addAll(chatBeen);
                                                        updateUI();
                                                        fillBackupList();
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                    }
                                                });
                                        if (compositeDisposable != null) {
                                            compositeDisposable.add(disposable);
                                        }
                                    }
                                }
                            }
                        }).req();
            }
        }
    }

    @CallSuper
    protected void initView() {
        mLlRoot = (LinearLayout) findViewById(R.id.ll_root);
        mRlTop = (RelativeLayout) findViewById(R.id.rl_top);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvDelete = (TextView) findViewById(R.id.tv_delete);
        mLlSearchBarContainer = (LinearLayout) findViewById(R.id.ll_search_bar_container);
        mRlDelete = (RecyclerView) findViewById(R.id.rl_delete);
        mIvSearch = (ImageView) findViewById(R.id.iv_search);
        mEtSearch = (LimitEditText) findViewById(R.id.et_search);
        mIvClearText = (ImageView) findViewById(R.id.iv_clear_text);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    @Override
    public void onItemClick(View v, int position) {
        if (!couldOperateUI() || position == RecyclerView.NO_POSITION) {
            return;
        }
    }

    @Override
    public void onClick(View v) {
        if (!couldOperateUI()) {
            return;
        }
    }

    protected static class DeleteMemberAdapter extends RecyclerView.Adapter<DeleteMemberViewHolder> {

        List<ChatBean> mList;

        public DeleteMemberAdapter(List<ChatBean> mList) {
            this.mList = mList;
        }

        @Override
        public DeleteMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar_to_delete, parent, false);
            return new DeleteMemberViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DeleteMemberViewHolder holder, int position) {
            holder.bindData(mList.get(position));
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }
    }

    protected static class DeleteMemberViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout mFlAvatarContainer;
        private AsyncImageViewV2 mIvAvatar;

        public DeleteMemberViewHolder(View itemView) {
            super(itemView);
            mFlAvatarContainer = (FrameLayout) itemView.findViewById(R.id.fl_avatar_container);
            mIvAvatar = (AsyncImageViewV2) itemView.findViewById(R.id.iv_avatar);
        }

        public void bindData(ChatBean chatBean) {
            UserProto.ChatUserInfo info = null;
            if (chatBean.userInfo != null) {
                info = chatBean.userInfo;
            } else if (chatBean.userInfoAdmin != null && chatBean.userInfoAdmin.chatUserInfo != null) {
                info = chatBean.userInfoAdmin.chatUserInfo;
            }
            if (info != null) {
                mIvAvatar.setImageUrl(ImageUrlUtil.getHeadImg(info.newHeadImage), LogicConfig.getDefaultHeadIcon(info.sex));
            }
        }
    }

    protected static class ChatMembersAdapter extends RecyclerView.Adapter {

        List<ChatBean> mList;

        OnItemClickListener mListener;

        boolean isInDeleteMode;//是否处于删除模式下

        @Nullable
        public String queryText;

        public ChatMembersAdapter(List<ChatBean> mList, OnItemClickListener mListener, boolean isInDeleteMode) {
            this.mList = mList;
            this.mListener = mListener;
            this.isInDeleteMode = isInDeleteMode;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            if (viewType == R.layout.item_chat_member) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_member, parent, false);
                return new ChatMemberViewHolder(itemView, mListener);
            } else {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_member_not_found, parent, false);
                return new UserNotFoundHolder(itemView, mListener);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int itemViewType = getItemViewType(position);
            if (itemViewType == R.layout.item_chat_member) {
                ((ChatMemberViewHolder) holder).bindData(mList.get(position), isInDeleteMode, queryText);
            } else {
                ((UserNotFoundHolder) holder).bindData();
            }
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public int getItemViewType(int position) {
            ChatBean bean = mList.get(position);
            if (bean.userInfo == null && bean.userInfoAdmin == null) {
                return R.layout.item_chat_member_not_found;
            } else {
                return R.layout.item_chat_member;
            }
        }
    }

    public static class HorizontalDividerDecoration extends RecyclerView.ItemDecoration {
        int dividerHeight;
        private ColorDrawable mDivider;

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            if (mDivider == null) {
                mDivider = new ColorDrawable(ContextCompat.getColor(parent.getContext(), R.color.divider_list_color));
            }
            final int itemCount = parent.getChildCount();
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            for (int i = 0; i < itemCount; i++) {
                final View child = parent.getChildAt(i);
                if (child == null) return;
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + dividerHeight;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (dividerHeight == 0) {
                dividerHeight = DisplayUtil.dp2px(1f);
            }
            outRect.set(0, 0, 0, dividerHeight);
        }
    }

    protected static class ChatMemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mIvCheck;
        private AsyncImageViewV2 mIvAvatar;
        private TextView mTvNickAndTeachAge;
        private TextView mTvNickAndPhoneNum;
        private ImageView mIvAdminTag;


        OnItemClickListener mListener;

        public ChatMemberViewHolder(View itemView, OnItemClickListener mListener) {
            super(itemView);
            this.mListener = mListener;
            mIvCheck = (ImageView) itemView.findViewById(R.id.iv_check);
            mIvAvatar = (AsyncImageViewV2) itemView.findViewById(R.id.iv_avatar);
            mIvAdminTag = (ImageView) itemView.findViewById(R.id.iv_admin_tag);
            mTvNickAndTeachAge = (TextView) itemView.findViewById(R.id.tv_nick_and_teach_age);
            mTvNickAndPhoneNum = (TextView) itemView.findViewById(R.id.tv_nick_and_phone_num);

            itemView.setOnClickListener(this);
        }

        /**
         * 如果是删除模式下要统一显示左边的ImageView
         *
         * @param userInfo
         * @param isInDeleteMode
         * @param queryText      用于本地搜索时高亮显示
         */
        public void bindData(@NonNull ChatBean userInfo, boolean isInDeleteMode, @Nullable String queryText) {
            if (userInfo.userInfo != null || userInfo.userInfoAdmin != null) {
                //左边的勾选
                if (isInDeleteMode) {
                    mIvCheck.setVisibility(View.VISIBLE);
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                        mIvCheck.setImageResource(userInfo.inDeleteList ? R.drawable.icon_select_studentresouce : R.drawable.icon_unselected_studentresouce);
                    } else if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                        mIvCheck.setImageResource(userInfo.inDeleteList ? R.drawable.icon_continue_yes : R.drawable.icon_unselected_studentresouce);
                    }
                } else {
                    mIvCheck.setVisibility(View.GONE);
                }
                //昵称头像
                UserProto.ChatUserInfo info = null;
                @ColorInt int highLightColor = 0;
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                    highLightColor = ContextCompat.getColor(mTvNickAndPhoneNum.getContext(), R.color.accent_orange);
                } else {
                    highLightColor = ContextCompat.getColor(mTvNickAndPhoneNum.getContext(), R.color.primary_blue);
                }
                if (userInfo.userInfo != null) {//非管理员查看群成员列表
                    if (mTvNickAndPhoneNum.getVisibility() != View.GONE) {
                        mTvNickAndPhoneNum.setVisibility(View.GONE);
                    }
                    info = userInfo.userInfo;
                    mIvAvatar.setImageUrl(ImageUrlUtil.getHeadImg(userInfo.userInfo.newHeadImage), LogicConfig.getDefaultHeadIcon(userInfo.userInfo.sex));
                    if (!TextUtils.isEmpty(queryText)) { //搜索的时候
                        SpannableString spannableString = new SpannableString(userInfo.userInfo.nick);
                        ForegroundColorSpan span;
                        int index = userInfo.userInfo.nick.indexOf(queryText);
                        if (index > -1) {
                            span = new ForegroundColorSpan(highLightColor);
                            spannableString.setSpan(span, index, index + queryText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            mTvNickAndTeachAge.setText(spannableString);
                        }
                    } else {
                        mTvNickAndTeachAge.setText(userInfo.userInfo.nick);
                    }
                } else if (userInfo.userInfoAdmin != null) {//管理员查看群成员列表
                    info = userInfo.userInfoAdmin.chatUserInfo;
                    mIvAvatar.setImageUrl(ImageUrlUtil.getHeadImg(userInfo.userInfoAdmin.chatUserInfo.newHeadImage), LogicConfig.getDefaultHeadIcon(userInfo.userInfoAdmin.chatUserInfo.sex));
                    TeachingResearchImProto.ChatUserInfoForTeachingResearchGroupAdmin.ChatUserInfoForTeachingResearchGroupAdminTeacherExtend extend = userInfo.userInfoAdmin.teacherExtend;
                    if (extend != null) { //这是一个老师，需要显示实名和手机号****
                        String realNameAndPhoneText = extend.teacherRealName + "-" + extend.maskPhone;
                        if (mTvNickAndPhoneNum.getVisibility() != View.VISIBLE) {
                            mTvNickAndPhoneNum.setVisibility(View.VISIBLE);
                        }
                        if (!TextUtils.isEmpty(queryText)) {//搜索的时候要高亮
                            ForegroundColorSpan span;
                            SpannableString spannableString;
                            int index = info.nick.indexOf(queryText);
                            if (index > -1) {
                                span = new ForegroundColorSpan(highLightColor);
                                spannableString = new SpannableString(info.nick);
                                spannableString.setSpan(span, index, index + queryText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                mTvNickAndTeachAge.setText(spannableString);//昵称和教龄
                            } else {
                                mTvNickAndTeachAge.setText(info.nick); //昵称和教龄
                            }
                            index = realNameAndPhoneText.indexOf(queryText);
                            if (index > -1) {
                                span = new ForegroundColorSpan(highLightColor);
                                spannableString = new SpannableString(realNameAndPhoneText);
                                spannableString.setSpan(span, index, index + queryText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                mTvNickAndPhoneNum.setText(spannableString); //实名和手机号
                            } else {
                                mTvNickAndPhoneNum.setText(realNameAndPhoneText); //实名和手机号
                            }
                        } else {
                            mTvNickAndTeachAge.setText(info.nick);
                            mTvNickAndPhoneNum.setText(realNameAndPhoneText);
                        }
                    } else if (userInfo.userInfoAdmin.chatUserInfo != null) { //管理员查看群成员列表，但这是一个助教，所以也就没有实名，手机号这些东西
                        if (mTvNickAndPhoneNum.getVisibility() != View.GONE) {//是为了让老师昵称这些东西居中
                            mTvNickAndPhoneNum.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(queryText)) {
                            SpannableString spannableString = new SpannableString(userInfo.userInfoAdmin.chatUserInfo.nick);
                            ForegroundColorSpan span;
                            int index = userInfo.userInfoAdmin.chatUserInfo.nick.indexOf(queryText);
                            if (index > -1) {
                                span = new ForegroundColorSpan(highLightColor);
                                spannableString.setSpan(span, index, index + queryText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                mTvNickAndTeachAge.setText(spannableString);
                            }
                        } else {
                            mTvNickAndTeachAge.setText(userInfo.userInfoAdmin.chatUserInfo.nick);
                        }
                    }
                }
                if (info != null) {
                    if (isAdmin(info)) {
                        mIvAdminTag.setVisibility(View.VISIBLE);
                    } else {
                        mIvAdminTag.setVisibility(View.GONE);
                    }
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    protected static class UserNotFoundHolder extends RecyclerView.ViewHolder {

        public UserNotFoundHolder(View itemView, OnItemClickListener mListener) {
            super(itemView);
        }

        public void bindData() {
        }
    }



    public static boolean isAdmin(TeachingResearchImProto.ChatUserInfoForTeachingResearchGroupAdmin member) {
        if (member != null && member.chatUserInfo != null) {
            return isAdmin(member.chatUserInfo);
        }
        return false;
    }



    /**
     * 判断当前是否是管理员身份
     *
     * @return
     */
    protected boolean amIAdmin() {
        if (userRole != null) {
            for (int i = 0; i < userRole.length; i++) {
                if (userRole[i] == UserProto.ChatGroupUserRole.manager_chat_group_user_role ||
                        userRole[i] == UserProto.ChatGroupUserRole.owner_chat_group_user_role) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void createOrClearList() {
        if (mList == null) {
            mList = new ArrayList<>();
        } else {
            mList.clear();
        }
    }

    @CallSuper
    protected void deleteSelectedMembers() {
        if (mDeleteList == null || mDeleteList.size() == 0) {
            return;
        }
        if (!TextUtils.isEmpty(mGroupChatId)) {
            ImProto.KickChatGroupMemberRequest kickChatGroupMemberRequest = new ImProto.KickChatGroupMemberRequest();
            kickChatGroupMemberRequest.chatGroupId = mGroupChatId;
            int size = mDeleteList.size();
            kickChatGroupMemberRequest.qingqingUserIds = new String[size];
            for (int i = 0; i < size; i++) {
                ChatBean bean = mDeleteList.get(i);
                if (bean != null) {
                    if (bean.userInfoAdmin != null) {
                        kickChatGroupMemberRequest.qingqingUserIds[i] = bean.userInfoAdmin.chatUserInfo.qingqingUserId; //老师端管理员
                    } else if (bean.userInfo != null) {
                        kickChatGroupMemberRequest.qingqingUserIds[i] = bean.userInfo.qingqingUserId; //非管理员
                    }
                }
            }
            kickChatGroupMemberRequest.hasChatGroupId = true;
            newProtoReq(CommonUrl.CHAT_KICK_USER.url()).setSendMsg(kickChatGroupMemberRequest)
                    .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {

                        @Override
                        public void onDealResult(Object result) {
                            ArrayList<String> list = deliverDeletedMembers();
                            Intent intent = new Intent();
                            if (list != null) {
                                intent.putStringArrayListExtra(BaseParamKeys.PARAM_DELETED_GROUP_CHAT_MEMBERS, list);
                            }
                            setResult(RESULT_OK, intent);
                            ToastWrapper.showWithIcon(R.string.text_deleted, R.drawable.icon_toast_yes);
                            finish();
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
        } else {// 说明群未生成
            performLocalDelete();
        }
    }

    @CallSuper
    protected void performLocalDelete() {
        if (mDeleteList == null || mDeleteList.size() == 0) {
            return;
        }
        ArrayList<String> list = deliverDeletedMembers();
        Intent intent = new Intent();
        if (list != null) {
            intent.putStringArrayListExtra(BaseParamKeys.PARAM_DELETED_GROUP_CHAT_MEMBERS, list);
        }
        setResult(RESULT_OK, intent);
        ToastWrapper.showWithIcon(R.string.text_deleted, R.drawable.icon_toast_yes);
        finish();
    }


    @Nullable
    protected ArrayList<String> deliverDeletedMembers() {
        ArrayList<String> list = null;
        if (mDeleteList != null && mDeleteList.size() > 0) {
             list = new ArrayList<>();
            for (int i = 0; i < mDeleteList.size(); i++) {
                ChatBean bean = mDeleteList.get(i);
                if (bean.userInfo != null) {
                    list.add(bean.userInfo.qingqingUserId);
                } else if (bean.userInfoAdmin != null) {
                    list.add(bean.userInfoAdmin.chatUserInfo.qingqingUserId);
                }
            }
        }
        return list;
    }

    protected CompatDialog mConfirmDeleteMemberDialog;

    @CallSuper
    protected void showConfirmDeleteDialog() {

    }

    protected void fillBackupList() {
        if (mListBackup == null) {
            mListBackup = new ArrayList<>();
        } else {
            mListBackup.clear();
        }
        mListBackup.addAll(mList);
    }

    @CallSuper
    protected void setTitleWithGroupMemberCount() {
        if (mList != null && mList.size() > 0) {
            mTvTitle.setText("群成员（" + mList.size() + "）");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    @Override
    public void onSetStatusBarMode() {
        setStatusBarColor(R.color.bg_color, true);
    }
    
    protected void saveGroupMemberContacts(UserProto.ChatUserInfo... chatUserInfos) {
        
    }
    
    public static boolean isAdmin(UserProto.ChatUserInfo chatUserInfo) {
        if (chatUserInfo != null && chatUserInfo.userRole != null && chatUserInfo.userRole.length > 0) {
            for (int i = 0,size = chatUserInfo.userRole.length; i < size; i++) {
                if (chatUserInfo.userRole[i] == UserProto.ChatGroupUserRole.manager_chat_group_user_role ||
                        chatUserInfo.userRole[i] == UserProto.ChatGroupUserRole.owner_chat_group_user_role) {
                    return true;
                }
            }
        }
        return false;
    }
}

