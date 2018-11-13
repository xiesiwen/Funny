package com.qingqing.project.offline.groupchat;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.qingqingbase.R;

/**
 * Created by dubo on 15/12/21.
 */
public abstract class GroupChatSettingActivity extends BaseActionBarActivity {
    
    private GroupChatSettingFragment mGroupChatSettingFragment;
    private GroupChatEditNameFragment mGroupChatEditNameFragment;
    private String groupId;
    private MenuItem mMenuSaveProfile;
    private String mMenuTitle = ""; // 菜单按钮显示的内容
    private static final int SAVE_PROFILE_MENU_ID = 7;// 保存按钮的menu id
    private boolean isShowMenu = false; // 是否显示右上角的菜单按钮
    private String mEditGroupName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_fragment);
        setFragGroupID(R.id.full_screen_fragment_container);
        if (getIntent() != null) {
            groupId = getIntent().getStringExtra("groupId");
            if (TextUtils.isEmpty(groupId)) {
                ToastWrapper.show("组id不正确");
                return;
            }
        }
        showMenu("", false);
        mGroupChatSettingFragment = createChatFragment();
        prepareBottomFragment();
        
    }
    
    protected abstract GroupChatSettingFragment createChatFragment();
    
    public String getGroupId() {
        return groupId;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenuSaveProfile = menu.add(Menu.NONE, SAVE_PROFILE_MENU_ID, Menu.NONE, "");
        MenuItemCompat.setShowAsAction(mMenuSaveProfile,
                MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        
        if (TextUtils.isEmpty(mMenuTitle)) {
            mMenuTitle = "";
        }
        if (mMenuSaveProfile != null) {
            mMenuSaveProfile.setVisible(isShowMenu);
            mMenuSaveProfile.setTitle(mMenuTitle);
        }
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == SAVE_PROFILE_MENU_ID) {
            if (mFragAssist.getTopFragment() == mGroupChatEditNameFragment) {
                mEditGroupName = mGroupChatEditNameFragment.getContent();
                saveGroupInfo(groupId, mEditGroupName);
            }
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 显示（or隐藏）菜单
     *
     * @param menuTitle
     *            菜单显示的内容
     * @param isShowMenu
     *            是否显示菜单
     */
    public void showMenu(String menuTitle, boolean isShowMenu) {
        this.mMenuTitle = menuTitle;
        this.isShowMenu = isShowMenu;
        supportInvalidateOptionsMenu();
    }
    
    private void prepareBottomFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("groupId", groupId);
        mGroupChatSettingFragment.setArguments(bundle);
        mGroupChatSettingFragment
                .setFragListener(new GroupChatSettingFragment.SetingListener() {
                    @Override
                    public void toEditNameFragment(String content) {
                        goEditFragment(content);
                    }
                    
                    @Override
                    public void onStart() {
                        setTitle(R.string.chat_group_title);
                        showMenu("", false);
                    }
                    
                    @Override
                    public void onStop() {
                        showMenu("", false);
                    }
                });
        mFragAssist.setBottom(mGroupChatSettingFragment);
    }
    
    private void goEditFragment(String content) {
        mGroupChatEditNameFragment = new GroupChatEditNameFragment();
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        mGroupChatEditNameFragment.setArguments(bundle);
        mGroupChatEditNameFragment
                .setFragListener(new GroupChatEditNameFragment.EditNameFragListener() {
                    @Override
                    public void EditName(String name) {
                        mEditGroupName = name;
                    }
                    
                    @Override
                    public void onStart() {
                        showMenu("保存", true);
                        setTitle(R.string.group_edit_title);
                    }
                    
                    @Override
                    public void onStop() {
                        
                    }
                });
        mFragAssist.push(mGroupChatEditNameFragment);
    }
    
    public void saveGroupInfo(String groupId, String content) {
        ImProto.ChatGroupUpdateRequest chatGroupUpdateRequest = new ImProto.ChatGroupUpdateRequest();
        chatGroupUpdateRequest.chatGroupId = groupId;
        chatGroupUpdateRequest.chatGroupName = TextUtils.isEmpty(content) ? "" : content;
        newProtoReq(CommonUrl.CHAT_GROUP_UPDATE_INFO.url()).setSendMsg(chatGroupUpdateRequest)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        ToastWrapper.show("保存成功");
                        showMenu("", false);
                        onBackPressed();
                        // 添加
                        // ChatManager.getInstance().asyncFetchGroupFromServer(groupId,
                        // null);
                    }
                }).req();
    }
}
