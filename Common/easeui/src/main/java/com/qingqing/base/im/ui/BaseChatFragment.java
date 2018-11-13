package com.qingqing.base.im.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.easeui.R;
import com.easemob.easeui.model.EaseNotifier;
import com.easemob.easeui.ui.EaseChatFragment;
import com.easemob.easeui.ui.EaseChatFragment.EaseChatFragmentListener;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.easemob.easeui.widget.chatrow.EaseCustomChatRowProvider;
import com.easemob.util.PathUtil;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.activity.HtmlActivity;
import com.qingqing.base.bean.UserBehaviorLogExtraData;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.dialog.CompSheetDialogBuilder;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.dialog.component.CompDialogSheetConsole;
import com.qingqing.base.dialog.component.SimpleSheetListContentAdapter;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.CmdMsgParser;
import com.qingqing.base.im.Constant;
import com.qingqing.base.im.DataSyncListener;
import com.qingqing.base.im.ExtFieldParser;
import com.qingqing.base.im.domain.ChatMessage;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.domain.ExtField;
import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.ClipboardUtil;
import com.qingqing.base.utils.CommonActivityUtil;
import com.qingqing.base.utils.ListUtil;
import com.qingqing.base.view.ToastWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by huangming on 2015/12/24.
 */
public class BaseChatFragment extends EaseChatFragment
        implements EaseChatFragmentListener , DataSyncListener {
    
    // 避免和基类定义的常量可能发生的冲突，常量从11开始定义
    
    private static final int REQUEST_CODE_SELECT_VIDEO = 11;
    private static final int REQUEST_CODE_SELECT_FILE = 12;
    private static final int REQUEST_CODE_GROUP_DETAIL = 13;
    private static final int REQUEST_CODE_CONTEXT_MENU = 14;
    
    private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 1;
    private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 2;
    private static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 3;
    private static final int MESSAGE_TYPE_RECV_VIDEO_CALL = 4;
    
    protected static final int MENU_ITEM_SINGLE = 1;
    protected static final int MENU_ITEM_GROUP = 2;
    
    private static final long SINGLE_REVOKE_TIME_LIMIT = 2 * 60 * 1000;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setChatFragmentListener(this);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChatManager.getInstance().addSyncGroupListener(this);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ChatManager.getInstance().removeSyncGroupListener(this);
    }
    
    protected String getMenuText() {
        String menuText = " ";
        if (chatType == Constant.CHATTYPE_SINGLE && couldOperateUI()) {
            ContactInfo contactInfo = ChatManager.getInstance().getChatModel()
                    .getContactModel().getContactInfo(toChatUsername);
            if (contactInfo != null) {
                if (contactInfo.getType() == ContactInfo.Type.Assistant) {
                    menuText = getString(R.string.contact_assistant_menu_text);
                }
                else if (contactInfo.getType() == ContactInfo.Type.Student) {
                    menuText = getString(R.string.contact_student_menu_text);
                }
                else if (contactInfo.getType() == ContactInfo.Type.Teacher) {
                    menuText = getString(R.string.contact_assistant_menu_text);
                }
            }
        }
        else if (chatType == Constant.CHATTYPE_GROUP && couldOperateUI()) {
            menuText = getString(R.string.group_detail_menu_text);
        }
        return menuText;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item != null ? item.getItemId() : 0;
        switch (itemId) {
            case MENU_ITEM_SINGLE:
                break;
            case MENU_ITEM_GROUP:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_VIDEO: // 发送选中的视频
                    if (data != null) {
                        int duration = data.getIntExtra("dur", 0);
                        String videoPath = data.getStringExtra("path");
                        File file = new File(PathUtil.getInstance().getImagePath(),
                                "thvideo" + System.currentTimeMillis());
                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            Bitmap ThumbBitmap = ThumbnailUtils
                                    .createVideoThumbnail(videoPath, 3);
                            ThumbBitmap.compress(CompressFormat.JPEG, 100, fos);
                            fos.close();
                            sendVideoMessage(videoPath, file.getAbsolutePath(), duration);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case REQUEST_CODE_SELECT_FILE: // 发送选中的文件
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            sendFileByUri(uri);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        
    }
    
    @Override
    protected void onSendMessage(EMMessage message) {
        super.onSendMessage(message);
        if (chatType == Constant.CHATTYPE_GROUP) {
            generateMsgExtFromUser(message);
            // EaseCommonUtils.setMessageAtMember(message,
            // EaseAtMessageHelper.get().getToAtUserList().toArray(new
            // String[]{}));
        }
        
        // 苹果 apns 推送标题
        addMsgAPNSExt(message);
    }
    
    private void addMsgAPNSExt(EMMessage message) {
        try {
            JSONObject object = new JSONObject();
            object.accumulate(ExtField.Attr.EM_PUSH_TITLE,
                    EaseNotifier.getNotificationTitle(message));
            message.setAttribute(ExtField.Attr.EM_APNS_EXT, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void generateMsgExtFromUser(EMMessage message) {
        ExtFieldParser.setExtUser(message, selfInfo.qingqingUserId, selfInfo.nick,
                selfInfo.newHeadImage, selfInfo.userType, selfInfo.sex,
                ListUtil.arrayToList(selfInfo.userRole), getTeacherRole());
    }
    
    protected ArrayList<Integer> getTeacherRole() {
        return null;
    }
    
    @Override
    public EaseCustomChatRowProvider onSetCustomChatRowProvider() {
        // 设置自定义listview item提供者
        return new CustomChatRowProvider();
    }
    
    @Override
    public void onEnterGroupChatMemberListActivity(@NonNull String groupChatId,
            int requestCode) {
        
    }
    
    @Override
    public void onSetMessageAttributes(EMMessage message) {}
    
    @Override
    public void onEnterToChatDetails() {
        if (chatType == Constant.CHATTYPE_GROUP) {
            EMGroup group = EMGroupManager.getInstance().getGroup(toChatUsername);
            if (group == null) {
                ToastWrapper.show(R.string.gorup_not_found);
                return;
            }
        }
        else if (chatType == Constant.CHATTYPE_CHATROOM) {}
        else if (chatType == Constant.CHATTYPE_SINGLE) {
            
        }
    }
    
    // 头像点击事件
    @Override
    public void onAvatarClick(String username) {
        // 头像点击事件
    }
    
    @Override
    public boolean onAvatarClick(ChatMessage message) {
        return false;
    }
    
    // 头像长按事件
    @Override
    public void onAvatarLongClick(String username) {
        if (chatType == Constant.CHATTYPE_GROUP) {
            inputAtUsername(username);
        }
    }
    
    @Override
    public boolean onAvatarLongClick(ChatMessage message) {
        return false;
    }
    
    @Override
    public boolean onMessageBubbleClick(EMMessage message) {
        // 消息框点击事件，demo这里不做覆盖，如需覆盖，return true
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        if (cmdMsg != null && cmdMsg.msgType == CmdMsg.CMD_TYPE_REC_TEACHER) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            String userName = bundle.getString(CmdMsg.RecTeacher.QQ_TEACHER_ID);
            String secondId = bundle.getString(CmdMsg.RecTeacher.TEACHER_SECOND_ID);
            onRecommendTeacherClick(userName, secondId);
            onRecommendTeacherClick(userName, secondId, cmdMsg.msgType);
            return true;
        }
        else if (cmdMsg != null && (cmdMsg.msgType == CmdMsg.CMD_TYPE_CONSULT_ST
                || cmdMsg.msgType == CmdMsg.CMD_TYPE_CONSULT_TA)) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            String userName = bundle.getString(CmdMsg.Consult.QQ_TEACHER_ID);
            String secondId = bundle.getString(CmdMsg.Consult.TEACHER_SECOND_ID);
            onRecommendTeacherClick(userName, secondId);
            onRecommendTeacherClick(userName, secondId, cmdMsg.msgType);
            return true;
        }
        else if (cmdMsg != null
                && cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_COURSE_REPORT) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            String shareCode = bundle.getString(CmdMsg.Group.SHARE_CODE);
            
            Intent intent = new Intent(getActivity(), HtmlActivity.class);
            intent.putExtra(BaseParamKeys.PARAM_STRING_URL, String
                    .format(CommonUrl.COURSE_REPORT_SHARE_H5_URL.url().url(), shareCode));
            startActivity(intent);
        }
        else if (cmdMsg != null && cmdMsg.msgType == CmdMsg.CMD_TYPE_GROUP_RANK) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            String shareCode = bundle.getString(CmdMsg.Group.SHARE_CODE);
            
            Intent intent = new Intent(getActivity(), HtmlActivity.class);
            intent.putExtra(BaseParamKeys.PARAM_STRING_URL,
                    String.format(CommonUrl.RANK_SHARE_H5_URL.url().url(), shareCode));
            startActivity(intent);
        }
        else if (cmdMsg != null
                && cmdMsg.msgType == CmdMsg.CMD_TYPE_SHARE_PLAN_SUMMARIZE) {
            Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
            
            String refId = bundle.getString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_REF_ID);
            String type = bundle.getString(CmdMsg.Group.SHARE_PLAN_SUMMARIZE_TYPE);
            if (!TextUtils.isEmpty(type)) {
                if (type.equals("teach_plan")) {
                    CommonActivityUtil.enterTeachPlanPreviewH5(getActivity(), refId,
                            false);
                }
                else if (type.equals("summarize")) {
                    CommonActivityUtil.enterTeachSummaryPreviewH5(getActivity(), refId,
                            false);
                }
            }
        }
        else if (message != null && (message.getBody() instanceof LocationMessageBody)) {
            LocationMessageBody messageBody = (LocationMessageBody) message.getBody();
            Intent intent = new Intent(getActivity(), MapActivity.class);
            intent.putExtra(Constant.EXTRA_LONGITUDE, messageBody.getLongitude());
            intent.putExtra(Constant.EXTRA_LATITUDE, messageBody.getLatitude());
            startActivity(intent);
        }
        return false;
    }
    
    @Deprecated
    public void onRecommendTeacherClick(String userName, String secondId) {}
    
    public void onRecommendTeacherClick(String userName, String secondId, int msgType) {
        
    }
    
    @Override
    public void onMessageBubbleLongClick(final EMMessage message) {
        // 消息框长按
        final Activity activity = getActivity();
        final ArrayList<CharSequence> items = new ArrayList<>();
        if (message.getChatType() == EMMessage.ChatType.ChatRoom
                && message.getType() == EMMessage.Type.TXT) {
            items.add(activity.getString(R.string.text_dlg_list_item_copy));
        }
        else if (message.getChatType() != EMMessage.ChatType.ChatRoom) {
            if (message.getType() == EMMessage.Type.TXT) {
                items.add(activity.getString(R.string.text_dlg_list_item_copy));
            }
            items.add(activity.getString(R.string.text_dlg_list_item_delete));
            // 6.1.0 新增助教端单聊2分钟内可撤回
            if ((message.getChatType() == EMMessage.ChatType.GroupChat
                    && canRevoke(getCurrentUserRole())
                    || (BaseData.getClientType() == AppCommon.AppType.qingqing_ta
                            && message.getChatType() == EMMessage.ChatType.Chat
                            && isMessageInRevokeTime(message) && isNotCmdMsg(message)))) {
                items.add(activity.getString(R.string.text_dlg_list_item_revoke));
            }
        }
        else {
            return;
        }
        new CompatDialog.Builder(activity, R.style.Theme_Dialog_Compat_Only_List)
                .setItems(items.toArray(new CharSequence[] {}),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Logger.i(TAG,
                                        "onMessageBubbleLongClick : type = "
                                                + message.getType().name() + ", which = "
                                                + which);
                                handleMessageLongClick(message, items.get(which));
                                dialog.dismiss();
                            }
                        })
                .show();
    }
    
    private boolean isNotCmdMsg(EMMessage message) {
        CmdMsg cmdMsg = CmdMsgParser.getCmdMsg(message);
        return cmdMsg == null;
    }
    
    private boolean isMessageInRevokeTime(EMMessage message) {
        return Math.abs(message.getMsgTime()
                - NetworkTime.currentTimeMillis()) < SINGLE_REVOKE_TIME_LIMIT;
    }
    
    private void handleMessageLongClick(EMMessage message, CharSequence content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        if (content.equals(getResources().getString(R.string.text_dlg_list_item_copy))) {
            copyMessage(message);
        }
        else if (content
                .equals(getResources().getString(R.string.text_dlg_list_item_delete))) {
            deleteMessage(message);
        }
        else if (content
                .equals(getResources().getString(R.string.text_dlg_list_item_revoke))) {
            revokeMessage(message);
        }
    }
    
    private boolean canRevoke(int[] roles) {
        if (roles == null) {
            return false;
        }
        
        for (int role : roles) {
            if (role == UserProto.ChatGroupUserRole.manager_chat_group_user_role
                    || role == UserProto.ChatGroupUserRole.owner_chat_group_user_role) {
                return true;
            }
        }
        return false;
    }
    
    private void revokeMessage(final EMMessage message) {
        new CompSheetDialogBuilder(getActivity())
                .setChoiceContent(
                        new String[] { getResources()
                                .getString(R.string.text_dlg_revoke_confirm),
                                getResources().getString(R.string.ok) },
                        new SimpleSheetListContentAdapter.ContentUpdateListener() {
                            @Override
                            public void onSheetContentUpdate(int position,
                                    Button sheetButton) {
                                switch (position) {
                                    case 0:
                                        sheetButton.setTextColor(getResources()
                                                .getColor(R.color.gray_dark));
                                        sheetButton.setTextSize(12);
                                        break;
                                    case 1:
                                        break;
                                }
                            }
                        })
                .setChoiceListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                break;
                            case 1:
                                if (chatType == Constant.CHATTYPE_GROUP) {
                                    ImProto.ChatGroupMsgRevokeRequest request = new ImProto.ChatGroupMsgRevokeRequest();
                                    request.chatGroupId = message.getTo();
                                    request.messsageId = message.getMsgId();
                                    
                                    newProtoReq(CommonUrl.CHAT_GROUP_REVOKE_URL.url())
                                            .setSendMsg(request)
                                            .setLoadingIndication(getActivity(),
                                                    getResources().getString(
                                                            R.string.text_dlg_revoking),
                                                    false)
                                            .req();
                                    
                                }
                                else if (chatType == Constant.CHATTYPE_SINGLE) {
                                    ImProto.SingleMsgRevokeRequest request = new ImProto.SingleMsgRevokeRequest();
                                    request.qingqingUserId = message.getTo();
                                    request.messsageId = message.getMsgId();
                                    
                                    newProtoReq(CommonUrl.CHAT_SINGLE_REVOKE_URL.url())
                                            .setSendMsg(request)
                                            .setLoadingIndication(getActivity(),
                                                    getResources().getString(
                                                            R.string.text_dlg_revoking),
                                                    false)
                                            .req();
                                }
                                dialog.dismiss();
                                break;
                        }
                        
                    }
                })
                .setComponentConsole(
                        new CompDialogSheetConsole(getActivity()).setButtonTextColorRes(
                                DialogInterface.BUTTON_NEGATIVE, R.color.black))
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }
    
    private void copyMessage(EMMessage message) {
        ClipboardUtil.copyToClipBoard(((TextMessageBody) message.getBody()).getMessage());
        ToastWrapper.show(R.string.tips_message_text_copy);
    }
    
    private void deleteMessage(EMMessage message) {
        if (conversation != null && message != null) {
            conversation.removeMessage(message.getMsgId());
            messageList.refresh();
        }
    }
    
    @Override
    public boolean onExtendMenuItemClick(int itemId, View view) {
        switch (itemId) {
            case ITEM_VIDEO: // 视频
                Intent intent = new Intent(getActivity(), ImageGridActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                    UserBehaviorLogManager.INSTANCE().saveClickLog(
                            StatisticalDataConstants.LOG_PAGE_TEACHER_IM_DETAIL,
                            StatisticalDataConstants.CLICK_CHAT_EXPAND,
                            new UserBehaviorLogExtraData.Builder().addExtraData(
                                    StatisticalDataConstants.LOG_EXTRA_E_CHAT_EXPAND_TYPE,
                                    3).build());
                }
                break;
            case ITEM_FILE: // 一般文件
                // demo这里是通过系统api选择文件，实际app中最好是做成qq那种选择发送文件
                selectFileFromLocal();
                break;
            case ITEM_VOICE_CALL: // 音频通话
                startVoiceCall();
                break;
            case ITEM_VIDEO_CALL: // 视频通话
                startVideoCall();
                break;
            
            default:
                break;
        }
        // 不覆盖已有的点击事件
        return false;
    }
    
    /**
     * 选择文件
     */
    protected void selectFileFromLocal() {
        Intent intent = null;
        if (Build.VERSION.SDK_INT < 19) { // 19以后这个api不可用，demo这里简单处理成图库选择图片
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            
        }
        else {
            intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }
    
    /**
     * 拨打语音电话
     */
    protected void startVoiceCall() {
        if (!EMChatManager.getInstance().isConnected()) {
            ToastWrapper.show(R.string.not_connect_to_server);
        }
        else {
            // startActivity(new Intent(getContext(),
            // VoiceCallActivity.class).putExtra("username", toChatUsername)
            // .putExtra("isComingCall", false));
            // // voiceCallBtn.setEnabled(false);
            // inputMenu.hideExtendMenuContainer();
        }
    }
    
    /**
     * 拨打视频电话
     */
    protected void startVideoCall() {
        if (!EMChatManager.getInstance().isConnected())
            ToastWrapper.show(R.string.not_connect_to_server);
        else {
            // startActivity(new Intent(getContext(),
            // VideoCallActivity.class).putExtra("username", toChatUsername)
            // .putExtra("isComingCall", false));
            // // videoCallBtn.setEnabled(false);
            // inputMenu.hideExtendMenuContainer();
        }
    }
    
    @Override
    public void onSyncComplete(boolean success) {
        if (success) {
            reqChatGroupMemebersIfNeeded();
            setTitle();
        }
    }
    
    /**
     * chat row provider
     */
    private final class CustomChatRowProvider implements EaseCustomChatRowProvider {
        @Override
        public int getCustomChatRowTypeCount() {
            // 音、视频通话发送、接收共4种
            return 4;
        }
        
        @Override
        public int getCustomChatRowType(EMMessage message) {
            if (message.getType() == EMMessage.Type.TXT) {
                // 语音通话类型
                if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL,
                        false)) {
                    return message.direct == EMMessage.Direct.RECEIVE
                            ? MESSAGE_TYPE_RECV_VOICE_CALL
                            : MESSAGE_TYPE_SENT_VOICE_CALL;
                }
                else if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL,
                        false)) {
                    // 视频通话
                    return message.direct == EMMessage.Direct.RECEIVE
                            ? MESSAGE_TYPE_RECV_VIDEO_CALL
                            : MESSAGE_TYPE_SENT_VIDEO_CALL;
                }
            }
            return 0;
        }
        
        @Override
        public EaseChatRow getCustomChatRow(EMMessage message, int position,
                BaseAdapter adapter) {
            return null;
        }
    }
    
    protected String getTitle() {
        String title = "";
        if (chatType == Constant.CHATTYPE_SINGLE) {
            title = IMUtils.getName(toChatUsername);
        }
        else if (chatType == Constant.CHATTYPE_GROUP) {
            EMGroup group = EMGroupManager.getInstance().getGroup(toChatUsername);
            if (group != null) {
                title = group.getGroupName();
            }
            else {
                title = ChatManager.getInstance().getGroupNameFromConversation(
                        EMChatManager.getInstance().getConversation(toChatUsername));
            }
        }
        return title;
    }
    
    protected void setTitle() {
        String title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
            setExtendTitle("");
            if (chatType == Constant.CHATTYPE_GROUP) {
                EMGroup group = EMGroupManager.getInstance().getGroup(toChatUsername);
                if (group != null) {
                    setExtendTitle("（" + group.getMembers().size() + "）");
                }
            }
        }
    }
    
    protected void toTeacherHome(String qingQingUserId, final Class targetClass) {
        if (!TextUtils.isEmpty(qingQingUserId)) {
            Intent intent = new Intent(getActivity(), targetClass);
            Bundle arguments = new Bundle();
            arguments.putString("second_id", qingQingUserId);
            intent.putExtra("arguments", arguments);
            startActivity(intent);
        }
        // final TeacherProto.SimpleQingQingTeacherIdRequest request = new
        // TeacherProto.SimpleQingQingTeacherIdRequest();
        // request.qingqingTeacherId = qingQingUserId;
        // newProtoReq(CommonUrl.GET_TEACHER_SECOND_ID_URL.url())
        // .setSendMsg(request)
        // .setRspListener(new
        // ProtoListener(TeacherProto.SimpleTeacherSecondIdResponse.class) {
        // @Override
        // public void onDealResult(Object result) {
        // TeacherProto.SimpleTeacherSecondIdResponse response =
        // (TeacherProto.SimpleTeacherSecondIdResponse) result;
        // String teacherSecondId = response.teacherSecondId;
        // if (!TextUtils.isEmpty(teacherSecondId)) {
        // Intent intent = new Intent(getContext(), targetClass);
        // Bundle arguments = new Bundle();
        // arguments.putString("second_id",
        // teacherSecondId);
        // intent.putExtra("arguments", arguments);
        // startActivity(intent);
        // } else {
        // Log.e(TAG, "teacher secondId == null ");
        // }
        // }
        // }).req();
    }
    
}
