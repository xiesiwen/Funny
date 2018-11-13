package com.qingqing.base.im.ui;

import android.os.Bundle;

import com.easemob.EMChatRoomChangeListener;
import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatRoom;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.im.ChatRoomModel;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.ToastWrapper;

/**
 * Created by huangming on 2016/5/28.
 */
public class BaseChatRoomFragment extends BaseChatFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onChatRoomInit();
    }

    @Override
    protected void onChatRoomInit() {
        super.onChatRoomInit();

        try {
            //final ProgressDialog pd = ProgressDialog.show(getContext(), "", "Joining......");
            EMChatManager.getInstance().joinChatRoom(toChatUsername, new EMValueCallBack<EMChatRoom>() {

                @Override
                public void onSuccess(final EMChatRoom value) {
                    Logger.i(TAG, "Chat Room Init ok ");
                    addChatRoomChangeListener();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!couldOperateUI() || !toChatUsername.equals(value.getUsername()))
                                return;
                            //pd.dismiss();
                            EMChatRoom room = EMChatManager.getInstance().getChatRoom(toChatUsername);
                            Logger.i(TAG, "join room success : " + room.getName());
                            onConversationInit();
                            onMessageListInit();
                        }
                    });
                }

                @Override
                public void onError(final int error, String errorMsg) {
                    // TODO Auto-generated method stub
                    Logger.e(TAG, "Chat Room Init error error = " + error + ", errorMsg = " + errorMsg);
                    //EMLog.d(TAG, "join room failure : " + error);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //pd.dismiss();
                            if (couldOperateUI()) {
                                getActivity().finish();
                                ToastWrapper.show("加入聊天室失败，请稍后重试");
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "joinChatRoom", e);
        }

    }

    protected void onChatRoomDestroyed() {

    }

    protected void addChatRoomChangeListener() {
        chatRoomChangeListener = new EMChatRoomChangeListener() {

            @Override
            public void onChatRoomDestroyed(String roomId, String roomName) {
                if (roomId.equals(toChatUsername)) {
                    Logger.i(TAG, "onChatRoomDestroyed");
                    BaseChatRoomFragment.this.onChatRoomDestroyed();
                }
            }

            @Override
            public void onMemberJoined(String roomId, String participant) {
            }

            @Override
            public void onMemberExited(String roomId, String roomName, String participant) {
            }

            @Override
            public void onMemberKicked(String roomId, String roomName, String participant) {
            }

        };

        EMChatManager.getInstance().addChatRoomChangeListener(chatRoomChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            EMChatManager.getInstance().leaveChatRoom(toChatUsername);
        } catch (Exception e) {
            Logger.e(TAG, "leaveChatRoom", e);
        }
        if (chatRoomChangeListener != null) {
            EMChatManager.getInstance().removeChatRoomChangeListener(chatRoomChangeListener);
        }

        ChatRoomModel.removeModel(toChatUsername);
    }

    public String getCurrentUserName() {
        return BaseData.qingqingUserId();
    }

}
