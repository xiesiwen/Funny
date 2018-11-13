package com.qingqing.project.offline.groupchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.api.proto.v1.im.TeachingResearchImProto;
import com.qingqing.base.log.Logger;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.teacherindex.TeacherIndexManager;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.setting.SettingToggleValueItem;
import com.qingqing.base.view.setting.SimpleSettingItem;
import com.qingqing.project.offline.R;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 群聊天设置页面， 助教端和老师端都用到
 */

public class BaseGroupChatSettingActivity extends BaseActionBarActivity implements View.OnClickListener {

    //View的成员变量开始
    protected LinearLayout mLlRoot;
    protected ScrollView mScrollView;
    protected LinearLayout mLlScrollviewChild;
    protected RecyclerView mRvMembers;
    protected FrameLayout mFlCheckoutGroupMembersContainer;
    protected TextView mTvCheckAllMembers;
    protected SimpleSettingItem mSsiGroupChatName;
    protected SimpleSettingItem mSsiGroupInformation;//6.1新增重要资讯
    protected LinearLayout mLlGroupAnnounceEntrance;
    protected TextView mTvGroupAnnounceTitle;
    protected TextView mTvGroupAnnounceEmpty;
    protected ImageView mIvAddAnnouncementForAdmin;
    protected LinearLayout mLlGroupChatContent;
    protected TextView mTvGroupChatAnnounceContent;
    protected SimpleSettingItem mSsiGroupChatMyNick;
    protected SimpleSettingItem mSsiGroupChatAverageTime;
    protected SettingToggleValueItem mSsiPinChat;
    protected SettingToggleValueItem mSsChatNoInterrupt;
    protected TextView mTvExitGroupChat;
    //View的成员变量结束

    protected ChatMemberBriefAdapter mAdapter;
    protected ArrayList<ChatBean> mList = new ArrayList<>();

    protected String mChatGroupId;
    protected int chatGroupType;
    protected int[] userRole;  // ChatGroupUserType ,用于区分是否是管理员

    @Nullable
    protected ImProto.ChatGroupDetailResponse mResponse;
    @Nullable
    protected TeachingResearchImProto.ChatGroupDetailForTeachingResearchGroupAdminResponse adminResponse; //仅在教研管理群admin的情况下使用

    protected static final int REQUEST_CODE_VIEW_ALL_MEMBER = 1086;
    protected static final int REQUEST_CODE_VIEW_ADD_MEMBER = 1087;
    protected static final int REQUEST_CODE_VIEW_DELETE_MEMBER = 1088;
    protected static final int REQUEST_CODE_EDIT_GROUP_NAME = 1089;
    protected static final int REQUEST_CODE_VIEW_ANNOUNCEMENT = 1090;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.activity_base_group_chat_setting);
        initView();
        addListeners();
        getChatGroupDetail();
    }

    /**
     * 从Intent里面拿东西什么的，先于setContentView调用
     */
    @CallSuper
    protected void initData() {
        Intent intent = getIntent();

        mChatGroupId = intent.getStringExtra(BaseParamKeys.PARAM_STRING_CHAT_GROUP_ID);
        userRole = intent.getIntArrayExtra(BaseParamKeys.PARAM_INT_CHAT_GROUP_USER_ROLE);
        chatGroupType = intent.getIntExtra(BaseParamKeys.PARAM_INT_GROUP_TYPE, ImProto.ChatGroupType.normal_chat_group_type);

      /*  mChatGroupId = "18268765093890";
        userRole = new int[]{UserProto.ChatGroupUserRole.manager_chat_group_user_role};
        chatGroupType = ImProto.ChatGroupType.teaching_research_chat_group_type;*/
        clearOrCreateList();
    }

    @CallSuper
    protected void addListeners() {
        mSsiGroupChatName.setOnClickListener(this);
        mLlGroupAnnounceEntrance.setOnClickListener(this);
        mTvExitGroupChat.setOnClickListener(this);
        mFlCheckoutGroupMembersContainer.setOnClickListener(this);
        if (amIAdmin()&&chatGroupType== ImProto.ChatGroupType.teaching_research_chat_group_type) {
            mSsiGroupChatAverageTime.setOnClickListener(this);
        }
        if (chatGroupType == ImProto.ChatGroupType.teaching_research_chat_group_type) {
            mSsiGroupInformation.setOnClickListener(this);
        }
    }

    @CallSuper
    protected void initView() {
        mLlRoot = (LinearLayout) findViewById(R.id.ll_root);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mLlScrollviewChild = (LinearLayout) findViewById(R.id.ll_scrollview_child);
        mRvMembers = (RecyclerView) findViewById(R.id.rv_members);
        mFlCheckoutGroupMembersContainer = (FrameLayout) findViewById(R.id.fl_checkout_group_members_container);
        mTvCheckAllMembers = (TextView) findViewById(R.id.tv_check_all_members);
        mSsiGroupChatName = (SimpleSettingItem) findViewById(R.id.ssi_group_chat_name);
        mSsiGroupInformation = (SimpleSettingItem) findViewById(R.id.ssi_group_information);
        mLlGroupAnnounceEntrance = (LinearLayout) findViewById(R.id.ll_group_announce_entrance);
        mTvGroupAnnounceTitle = (TextView) findViewById(R.id.tv_group_announce_title);
        mTvGroupAnnounceEmpty = (TextView) findViewById(R.id.tv_group_announce_empty);
        mIvAddAnnouncementForAdmin = (ImageView) findViewById(R.id.iv_add_announcement_for_admin);
        mLlGroupChatContent = (LinearLayout) findViewById(R.id.ll_group_chat_content);
        mTvGroupChatAnnounceContent = (TextView) findViewById(R.id.tv_group_chat_announce_content);
        mSsiGroupChatMyNick = (SimpleSettingItem) findViewById(R.id.ssi_group_chat_my_nick);
        mSsiGroupChatAverageTime = (SimpleSettingItem) findViewById(R.id.ssi_group_chat_average_time);
        mSsiPinChat = (SettingToggleValueItem) findViewById(R.id.ssi_pin_chat);
        mSsChatNoInterrupt = (SettingToggleValueItem) findViewById(R.id.ss_chat_no_interrupt);
        mTvExitGroupChat = (TextView) findViewById(R.id.tv_exit_group_chat);

        if (amIAdmin()&&chatGroupType == ImProto.ChatGroupType.teaching_research_chat_group_type) {
            mSsiGroupChatAverageTime.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public void onClick(View v) {
        if (!couldOperateUI()) {
            return;
        }
    }

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

    /** 是否是群主
     * @return
     */
    protected boolean amIOwner() {
        if (userRole != null) {
            for (int i = 0,size = userRole.length; i <size; i++) {
                if (userRole[i] == UserProto.ChatGroupUserRole.owner_chat_group_user_role) {
                    return true;
                }
            }
        }
        return false;
    }

    protected CompatDialog mExitGroupChatDialog;

   protected void showConfirmExitGroupChatDialog() {
        if (mExitGroupChatDialog == null) {
            mExitGroupChatDialog = new CompatDialog.Builder(this,
                    R.style.Theme_Dialog_Compat_NoTitleAlert_Red_Positive_Button)
                    .setMessage(R.string.text_dl_config_exit_group_chat)
                    .setPositiveButton(R.string.quit,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    exitGroupChat();
                                }
                            })
                    .setNegativeButton(R.string.text_cancel, null).create();
        }
        mExitGroupChatDialog.show();
    }


    /**
     *
     */
    @CallSuper
    protected void showConfirmDismissDialog() {
        new CompatDialog.Builder(this, R.style.Theme_Dialog_Compat_NoTitleAlert)
                .setMessage(R.string.confirm_disband_group)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,
                                                int i) {
                                dialogInterface.dismiss();
                                dismissGroupChat();
                            }
                        })
                .setNegativeButton(R.string.text_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,
                                                int i) {
                                dialogInterface.dismiss();
                            }
                        }).setCancelable(true).show();
    }

    @WorkerThread
    @CallSuper
    protected void dismissGroupChat() {
        ImProto.SimpleChatGroupIdRequest simpleChatGroupIdRequest = new ImProto.SimpleChatGroupIdRequest();
        simpleChatGroupIdRequest.chatGroupId = mChatGroupId;
        newProtoReq(CommonUrl.CHAT_GROUP_DISMISS.url())
                .setSendMsg(simpleChatGroupIdRequest)
                .setRspListener(
                        new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                if (couldOperateUI()) {
                                    try {
                                        EMChatManager.getInstance().deleteConversation(mChatGroupId);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }

                            @Override
                            public boolean onDealError(int errorCode,
                                                       Object result) {
                                ToastWrapper.show(getErrorHintMessage("解散群组失败"));
                                return true;
                            }
                        }).req();
    }

    /**
     * 退出群聊
     */
    @WorkerThread
    protected void exitGroupChat() {
        ImProto.SimpleChatGroupIdRequest request = new ImProto.SimpleChatGroupIdRequest();
        request.chatGroupId = mChatGroupId;
        newProtoReq(CommonUrl.CHAT_EXIT_GROUP.url()).
                setSendMsg(request)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        try {
                            EMChatManager.getInstance().deleteConversation(mChatGroupId);
                        } catch (Exception e) {
                            Logger.d("delete error");
                        }
                        setResult(RESULT_OK);//通知聊天页面finish
                        finish();
                    }
                }).req();
    }

    protected void clearOrCreateList() {
        if (mList == null) {
            mList = new ArrayList<>();
        } else {
            mList.clear();
        }
    }

    @CallSuper
    protected void saveGroupMemberContacts(UserProto.ChatUserInfo... chatUserInfos) {
    }

    @CallSuper
    protected void saveGroupMemberContacts(TeachingResearchImProto.ChatUserInfoForTeachingResearchGroupAdmin... chatUserInfos) {
    }

   protected CompositeDisposable compositeDisposable = new CompositeDisposable();


    /**
     * 拉接口，更新Ui
     */
    @CallSuper
    protected void  getChatGroupDetail() {
        if (!TextUtils.isEmpty(mChatGroupId)) {
            ImProto.SimpleChatGroupIdRequest request = new ImProto.SimpleChatGroupIdRequest();
            request.chatGroupId = mChatGroupId;
            if (amIAdmin()&&chatGroupType==ImProto.ChatGroupType.teaching_research_chat_group_type) {
                newProtoReq(CommonUrl.CHAT_GROUP_INFO_ADMIN.url()).setSendMsg(request)
                        .setRspListener(new ProtoListener(TeachingResearchImProto.ChatGroupDetailForTeachingResearchGroupAdminResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                if (couldOperateUI()) {
                                    adminResponse = (TeachingResearchImProto.ChatGroupDetailForTeachingResearchGroupAdminResponse) result;
                                    if (adminResponse != null) {
                                        setTitle("群设置（" + adminResponse.members.length + "）");
                                        updateAnnouncement();
                                        Disposable disposable = Observable.fromCallable(new Callable<List<ChatBean>>() {
                                            @Override
                                            public List<ChatBean> call() throws Exception {
                                                int length = adminResponse.members.length;
                                                List<ChatBean> tempList = new ArrayList<>(length);
                                                for (int i = 0; i < length; i++) {
                                                    tempList.add(new ChatBean(null, adminResponse.members[i]));
                                                }
                                                Collections.sort(tempList, new NickComparator());
                                                return tempList;
                                            }
                                        }).doAfterNext(new Consumer<List<ChatBean>>() {
                                            @Override
                                            public void accept(@NonNull List<ChatBean> chatBeen) throws Exception {
                                                saveGroupMemberContacts(adminResponse.members);

                                                ArrayList<String> userIds = new ArrayList<>();
                                                for(TeachingResearchImProto.ChatUserInfoForTeachingResearchGroupAdmin each:adminResponse.members){
                                                    userIds.add(each.chatUserInfo.qingqingUserId);
                                                }
                                                TeacherIndexManager.getInstance().loadGroupData(userIds.toArray(new String[]{}));
                                            }
                                        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<ChatBean>>() {
                                            @Override
                                            public void onNext(List<ChatBean> chatUserInfos) {
                                                clearOrCreateList();
                                                mList.addAll(chatUserInfos);
                                                updateUI();
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
                                    setTitle("群设置（" + mResponse.members.length + "）");
                                    updateAnnouncement();
                                    if (mResponse != null) {
                                        Disposable disposable = Observable.fromCallable(new Callable<List<ChatBean>>() {
                                            @Override
                                            public List<ChatBean> call() throws Exception {
                                                int size = mResponse.members.length;
                                                List<ChatBean> tempList = new ArrayList<>(size);
                                                for (int i = 0; i < size; i++) {
                                                    ChatBean bean = new ChatBean(mResponse.members[i], null);
                                                    tempList.add(bean);
                                                }
                                                Collections.sort(tempList, new NickComparator());

                                                return tempList;
                                            }
                                        }).doAfterNext(new Consumer<List<ChatBean>>() {
                                            @Override
                                            public void accept(@NonNull List<ChatBean> chatBeen) throws Exception {
                                                saveGroupMemberContacts(mResponse.members);

                                                ArrayList<String> userIds = new ArrayList<>();
                                                for(UserProto.ChatUserInfo each:mResponse.members){
                                                    userIds.add(each.qingqingUserId);
                                                }
                                                TeacherIndexManager.getInstance().loadGroupData(userIds.toArray(new String[]{}));
                                            }
                                        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                                                .subscribeWith(new DisposableObserver<List<ChatBean>>() {
                                                    @Override
                                                    public void onNext(List<ChatBean> chatBeen) {
                                                        clearOrCreateList();
                                                        mList.addAll(chatBeen);
                                                        updateUI();
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
    protected void updateUI() {
        if (mScrollView.getVisibility() != View.VISIBLE) {
            mScrollView.setVisibility(View.VISIBLE);
        }
        updateGroupInformation();//更新重要资讯入口
    }

    /**
     * 更新重要资讯入口，6.1新增
     */
    @CallSuper
    protected void updateGroupInformation() {
        if (chatGroupType==ImProto.ChatGroupType.teaching_research_chat_group_type) {
            if (mSsiGroupInformation.getVisibility() != View.VISIBLE) {
                mSsiGroupInformation.setVisibility(View.VISIBLE);
            }
            String groupInfoName="";
            if (mResponse != null) {
                groupInfoName = mResponse.groupInfoName;
            } else if (adminResponse != null) {
                groupInfoName = adminResponse.groupInfoName;
            }
            if (!TextUtils.isEmpty(groupInfoName)) {
                mSsiGroupInformation.setHasAction(true);
                mSsiGroupInformation.setValueColor(R.color.black).setValue(groupInfoName);
            } else {
                mSsiGroupInformation.setHasAction(false);
                mSsiGroupInformation.setValueColor(R.color.gray).setValue("");
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }


    protected void setTitleWithGroupCount() {
        if (mList != null) {
           setTitle("群设置(" + mList.size() + "）");
        }
    }


    @CallSuper
    protected void filterDeletedMembers(@NonNull Intent data) {
        List<String> deletedList = data.getStringArrayListExtra(BaseParamKeys.PARAM_DELETED_GROUP_CHAT_MEMBERS);
        if (deletedList != null&&mList!=null) {
            int length = deletedList.size(); //返回被删除的成员的id
            for (int i = 0; i < length; i++) {
                String id = deletedList.get(i);
                if (!TextUtils.isEmpty(id)) {
                    for (ListIterator<ChatBean> iterator = mList.listIterator(); iterator.hasNext();) {
                        ChatBean bean = iterator.next();
                        UserProto.ChatUserInfo info = null;
                        if (bean != null) {
                            if (bean.userInfo != null) {
                                info = bean.userInfo;
                            } else if (bean.userInfoAdmin != null) {
                                info = bean.userInfoAdmin.chatUserInfo;
                            }
                            if (info != null) {
                                if (!TextUtils.isEmpty(info.qingqingUserId) && info.qingqingUserId.equals(id)) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
            setTitleWithGroupCount();
        }
    }

    protected void enterAverageHourActivity() {
        // TODO: 2017/6/27 进入H5页面 群平均课时数
    }

    protected int getUserType() {
        if (userRole != null) {
            for (int i = 0; i < userRole.length; i++) {
                if (userRole[i] == UserProto.ChatGroupUserRole.manager_chat_group_user_role ||
                        userRole[i] == UserProto.ChatGroupUserRole.owner_chat_group_user_role) {
                    return 2;
                }
            }
        }
        return 3;
    }

    protected int getChatType() {
        int charType = 1;
        switch (chatGroupType) {
            case ImProto.ChatGroupType.teaching_research_chat_group_type:
                charType = 2;
                break;
            case ImProto.ChatGroupType.normal_chat_group_type:
                charType = 1;
                break;
        }
        return charType;
    }

    protected void updateAnnouncement() {
        if (couldOperateUI()) {
            boolean amIAdmin = amIAdmin();
            if (adminResponse != null) {
                if (TextUtils.isEmpty(adminResponse.groupAnnounce)) {
                    mTvGroupAnnounceEmpty.setVisibility(View.VISIBLE);
                    if (amIAdmin) { //管理员进来的时候，如果群公告为空，要显示向右的箭头 以及那个“无”的文字
                        mIvAddAnnouncementForAdmin.setVisibility(View.VISIBLE);
                    } else {
                        mIvAddAnnouncementForAdmin.setVisibility(View.INVISIBLE); //显示INVISIBLE是为了不让TextView往右边挪动
                    }
                    mLlGroupChatContent.setVisibility(View.GONE);
                } else {
                    mLlGroupChatContent.setVisibility(View.VISIBLE);
                    mTvGroupChatAnnounceContent.setText(adminResponse.groupAnnounce);
                    mTvGroupAnnounceEmpty.setVisibility(View.GONE);
                    mIvAddAnnouncementForAdmin.setVisibility(View.INVISIBLE);
                }
            } else if ( mResponse != null) {
                if (TextUtils.isEmpty(mResponse.groupAnnounce)) {
                    mTvGroupAnnounceEmpty.setVisibility(View.VISIBLE);
                    if (amIAdmin) {  //管理员进来的时候，如果群公告为空，要显示向右的箭头
                        mIvAddAnnouncementForAdmin.setVisibility(View.VISIBLE);
                    } else {
                        mIvAddAnnouncementForAdmin.setVisibility(View.INVISIBLE);
                    }
                    mLlGroupChatContent.setVisibility(View.GONE);
                } else {
                    mLlGroupChatContent.setVisibility(View.VISIBLE);
                    mTvGroupChatAnnounceContent.setText(mResponse.groupAnnounce);
                    mTvGroupAnnounceEmpty.setVisibility(View.GONE);
                }
            }
        }
    }
}
