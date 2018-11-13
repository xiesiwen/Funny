package com.qingqing.base.nim.view;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.base.view.editor.LimitEditText;

/**
 * Created by huangming on 2016/8/23.
 */
public class ChatInputMenu extends LinearLayout implements View.OnClickListener {
    
    private boolean mSpeakForbidden;
    
    private TextView mSpeakForbiddenView;
    
    private View mSetModeAudioView;
    private View mSetModeKeyboardView;
    
    private View mDisplayExtendMenuView;
    private View mSendView;
    
    private TextView mPressToSpeakView;
    
    private LimitEditText mMsgTextView;
    
    private IExtendMenuView mExtendMenuView;
    
    private ChatInputMenuListener mMenuListener;
    
    public ChatInputMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    void setMenuListener(ChatInputMenuListener listener) {
        this.mMenuListener = listener;
    }
    
    private ChatInputMenuListener getMenuListener() {
        return mMenuListener;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mSpeakForbiddenView = (TextView) findViewById(R.id.tv_speak_forbidden);
        
        mSetModeAudioView = findViewById(R.id.view_set_mode_audio);
        mSetModeAudioView.setOnClickListener(this);
        mSetModeKeyboardView = findViewById(R.id.view_set_mode_keyboard);
        mSetModeKeyboardView.setOnClickListener(this);
        
        mPressToSpeakView = (TextView) findViewById(R.id.tv_press_to_speak);
        
        mPressToSpeakView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        mPressToSpeakView.setText(R.string.text_release_to_send);
                        break;
                    case MotionEvent.ACTION_UP:
                    default:
                        mPressToSpeakView.setText(R.string.button_pushtotalk);
                        break;
                }
                if (getMenuListener() != null) {
                    return getMenuListener().onPressToSpeakBtnTouch(v, event);
                }
                return false;
            }
        });
        
        mDisplayExtendMenuView = findViewById(R.id.btn_display_extend_menu);
        mDisplayExtendMenuView.setOnClickListener(this);
        mSendView = findViewById(R.id.btn_send);
        mSendView.setOnClickListener(this);
        
        mMsgTextView = (LimitEditText) findViewById(R.id.et_msg_text);
        mMsgTextView.addTextChangedListener(new LimitedTextWatcher() {
            @Override
            public void afterTextChecked(Editable s) {
                boolean hasText = s != null && s.length() > 0;
                mDisplayExtendMenuView.setVisibility(hasText ? GONE : VISIBLE);
                mSendView.setVisibility(hasText ? VISIBLE : GONE);
            }
        });
        mMsgTextView.setOnClickListener(this);
    }
    
    void setExtendMenu(IExtendMenuView extendMenu) {
        mExtendMenuView = extendMenu;
    }
    
    private IExtendMenuView getExtendMenuView() {
        return mExtendMenuView;
    }
    
    public void setSpeakForbidden(boolean speakForbidden) {
        this.mSpeakForbidden = speakForbidden;
        mSpeakForbiddenView.setVisibility(isSpeakForbidden() ? VISIBLE : GONE);
    }
    
    public void setSpeakForbiddenText(String speakForbiddenText) {
        mSpeakForbiddenView.setText(speakForbiddenText);
    }
    
    public boolean isSpeakForbidden() {
        return mSpeakForbidden;
    }
    
    @Override
    public void onClick(View v) {
        if (v == mSendView) {
            clickSendView();
        }
        else if (v == mDisplayExtendMenuView) {
            clickDisplayExtendMenuView();
        }
        else if (v == mSetModeAudioView) {
            clickSetModeAudioView();
        }
        else if (v == mSetModeKeyboardView) {
            clickSetModeKeyboardView();
        }
        else if (v == mMsgTextView) {
            clickMsgTextView();
        }
    }
    
    private void clickMsgTextView() {
        if (getExtendMenuView() != null) {
            getExtendMenuView().hide();
        }
    }
    
    private void clickSendView() {
        if (getMenuListener() != null) {
            getMenuListener().onSendTextMessage(mMsgTextView.getText().toString());
        }
        mMsgTextView.setText("");
    }
    
    private void clickSetModeAudioView() {
        mSetModeKeyboardView.setVisibility(VISIBLE);
        mSetModeAudioView.setVisibility(GONE);
        mPressToSpeakView.setVisibility(VISIBLE);
        mMsgTextView.setVisibility(GONE);
        if (getExtendMenuView() != null) {
            getExtendMenuView().hide();
        }
        UIUtil.hideSoftInput(getContext());
    }
    
    private void clickSetModeKeyboardView() {
        mSetModeKeyboardView.setVisibility(GONE);
        mSetModeAudioView.setVisibility(VISIBLE);
        mPressToSpeakView.setVisibility(GONE);
        mMsgTextView.setVisibility(VISIBLE);
    }
    
    private void clickDisplayExtendMenuView() {
        mSetModeAudioView.setVisibility(VISIBLE);
        if (getExtendMenuView() != null) {
            boolean extendMenuVisible = getExtendMenuView().isVisible();
            getExtendMenuView().toggleVisible();
            if (!extendMenuVisible) {
                clickSetModeKeyboardView();
            }
        }
        UIUtil.hideSoftInput(getContext());
    }

    public void clearOutInputText() {
        if(mMsgTextView != null) {
            mMsgTextView.setText("");
        }
    }
}
