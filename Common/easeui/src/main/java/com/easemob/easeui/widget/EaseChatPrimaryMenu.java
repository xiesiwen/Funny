package com.easemob.easeui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.easemob.easeui.model.EaseAtMessageHelper;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.view.editor.LimitEditText;

import java.util.List;

/**
 * 聊天输入栏主菜单栏
 *
 */
public class EaseChatPrimaryMenu extends EaseChatPrimaryMenuBase
        implements OnClickListener {
    private LimitEditText editText;
    private View buttonSetModeKeyboard;
    private RelativeLayout edittextLayout;
    private View buttonSetModeVoice;
    private View buttonSend;
    private View buttonPressToSpeak;
    private TextView tvPressToSpeak;
    private ImageView faceNormal;
    private ImageView faceChecked;
    private ImageButton buttonMore;
    private RelativeLayout faceLayout;
    private Context context;
    private EaseVoiceRecorderView voiceRecorderView;
    private boolean showFace = true;

    public EaseChatPrimaryMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    
    public EaseChatPrimaryMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public EaseChatPrimaryMenu(Context context) {
        super(context);
        init(context, null);
    }
    
    private void init(final Context context, AttributeSet attrs) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_primary_menu,
                this);
        editText = (LimitEditText) findViewById(R.id.et_sendmessage);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        edittextLayout = (RelativeLayout) findViewById(R.id.edittext_layout);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonSend = findViewById(R.id.btn_send);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        tvPressToSpeak = (TextView) findViewById(R.id.tv_press_to_speak);
        faceNormal = (ImageView) findViewById(R.id.iv_face_normal);
        faceChecked = (ImageView) findViewById(R.id.iv_face_checked);
        faceLayout = (RelativeLayout) findViewById(R.id.rl_face);
        buttonMore = (ImageButton) findViewById(R.id.btn_more);
        faceLayout.setVisibility(showFace?VISIBLE:GONE);
        
        buttonSend.setOnClickListener(this);
        buttonSetModeKeyboard.setOnClickListener(this);
        buttonSetModeVoice.setOnClickListener(this);
        buttonMore.setOnClickListener(this);
        faceLayout.setOnClickListener(this);
        editText.setOnClickListener(this);
        editText.requestFocus();
        
        // 监听文字框
        editText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    buttonMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                }
                else {
                    buttonMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
                
                // @功能
                if (count == 1 && s.charAt(start) == '@') {
                    listener.onAtFunctionInput();
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                
                // 删除 @ 的用户
                if (after - count < 0) {
                    String s1 = s.toString().substring(0, start + 1);
                    int lastAtIndex = s1.lastIndexOf("@");
                    if (lastAtIndex >= 0) {
                        String sBefore = s.toString().substring(lastAtIndex,
                                start + count);
                        String sAfter = s.toString().substring(lastAtIndex,
                                start + after);
                        
                        List<String> atUserListBefore = EaseAtMessageHelper.get()
                                .getAtMessageUsernames(sBefore, groupId);
                        
                        List<String> atUserListAfter = EaseAtMessageHelper.get()
                                .getAtMessageUsernames(sAfter, groupId);
                        
                        boolean hasDeleteAtUser = false;
                        String deletedAtUser = "";
                        if (atUserListBefore != null && atUserListAfter == null) {
                            hasDeleteAtUser = true;
                            deletedAtUser = atUserListBefore.get(0);
                        }
                        else if (atUserListBefore != null
                                && atUserListBefore.size() > atUserListAfter.size()) {
                            hasDeleteAtUser = true;
                            deletedAtUser = atUserListBefore.get(atUserListAfter.size());
                        }
                        
                        if (hasDeleteAtUser) {
                            s = TextUtils.concat(s.subSequence(0, lastAtIndex),
                                    s.subSequence(start, s.length() - 1));
                            List<String> finalAtUsererList = EaseAtMessageHelper.get()
                                    .getAtMessageUsernames(s.toString(), groupId);
                            
                            if (finalAtUsererList == null
                                    || !finalAtUsererList.contains(deletedAtUser)) {
                                EaseAtMessageHelper.get().removeAtUser(deletedAtUser);
                            }
                            
                            editText.setText(s);
                        }
                    }
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            
            }
        });
            
        buttonPressToSpeak.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        tvPressToSpeak.setText(R.string.text_release_to_send);
                        break;
                    case MotionEvent.ACTION_UP:
                    default:
                        tvPressToSpeak.setText(R.string.button_pushtotalk);
                        break;
                }
                if (listener != null) {
                    return listener.onPressToSpeakBtnTouch(v, event);
                }
                return false;
            }
        });
    }
    
    @Override
    public void onTextInsert(CharSequence text) {
        int start = editText.getSelectionStart();
        Editable editable = editText.getEditableText();
        editable.insert(start, text);
        setModeKeyboard();
    }

    /**
     * 设置长按说话录制控件
     *
     * @param voiceRecorderView
     */
    public void setPressToSpeakRecorderView(EaseVoiceRecorderView voiceRecorderView) {
        this.voiceRecorderView = voiceRecorderView;
    }
    
    /**
     * 表情输入
     *
     * @param emojiContent
     */
    public void onEmojiconInputEvent(CharSequence emojiContent) {
        editText.append(emojiContent);
    }
    
    /**
     * 表情删除
     */
    public void onEmojiconDeleteEvent() {
        if (!TextUtils.isEmpty(editText.getText())) {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                    KeyEvent.KEYCODE_ENDCALL);
            editText.dispatchKeyEvent(event);
        }
    }
    
    /**
     * 点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_send) {
            if (listener != null) {
                String s = editText.getText().toString();
                listener.onSendBtnClicked(s);
                editText.setText("");
            }
        }
        else if (id == R.id.btn_set_mode_voice) {
            setModeVoice();
            showNormalFaceImage();
            switch (BaseData.getClientType()) {
                case AppCommon.AppType.qingqing_student:
                    break;
                case AppCommon.AppType.qingqing_teacher:
                    UserBehaviorLogManager.INSTANCE().saveClickLog(
                            StatisticalDataConstants.LOG_PAGE_TEACHER_IM_DETAIL,
                            StatisticalDataConstants.CLICK_TEACHER_IM_VOICE);
                    break;
            }
            if (listener != null)
                listener.onToggleVoiceBtnClicked();
        }
        else if (id == R.id.btn_set_mode_keyboard) {
            setModeKeyboard();
            showNormalFaceImage();
            if (listener != null)
                listener.onToggleVoiceBtnClicked();
        }
        else if (id == R.id.btn_more) {
            buttonSetModeVoice.setVisibility(View.VISIBLE);
            buttonSetModeKeyboard.setVisibility(View.GONE);
            edittextLayout.setVisibility(View.VISIBLE);
            buttonPressToSpeak.setVisibility(View.GONE);
            showNormalFaceImage();
            if (listener != null)
                listener.onToggleExtendClicked();
        }
        else if (id == R.id.et_sendmessage) {
            faceNormal.setVisibility(View.VISIBLE);
            faceChecked.setVisibility(View.INVISIBLE);
            if (listener != null)
                listener.onEditTextClicked();
        }
        else if (id == R.id.rl_face) {
            toggleFaceImage();
            if (listener != null) {
                listener.onToggleEmojiconClicked();
            }
        }
        else {}
    }
    
    /**
     * 显示语音图标按钮
     *
     */
    protected void setModeVoice() {
        hideKeyboard();
        edittextLayout.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        buttonMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        faceNormal.setVisibility(View.VISIBLE);
        faceChecked.setVisibility(View.INVISIBLE);
        
    }
    
    /**
     * 显示键盘图标
     */
    protected void setModeKeyboard() {
        edittextLayout.setVisibility(View.VISIBLE);
        buttonSetModeKeyboard.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        // mEditTextContent.setVisibility(View.VISIBLE);
        editText.requestFocus();
        // buttonSend.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(editText.getText())) {
            buttonMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        }
        else {
            buttonMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        }
        
    }
    
    protected void toggleFaceImage() {
        if (faceNormal.getVisibility() == View.VISIBLE) {
            showSelectedFaceImage();
        }
        else {
            showNormalFaceImage();
        }
    }
    
    private void showNormalFaceImage() {
        faceNormal.setVisibility(View.VISIBLE);
        faceChecked.setVisibility(View.INVISIBLE);
    }
    
    private void showSelectedFaceImage() {
        faceNormal.setVisibility(View.INVISIBLE);
        faceChecked.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onExtendMenuContainerHide() {
        showNormalFaceImage();
    }

    public void setShowFace(boolean showFace) {
        this.showFace = showFace;
        if (faceLayout!=null){
            faceLayout.setVisibility(showFace?VISIBLE:GONE);
        }
    }

}
