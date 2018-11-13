package com.qingqing.base.view;

import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.StringUtil;

import android.support.annotation.CallSuper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

/**
 * 受限制Text监听，长度以及是否表情
 * 
 * @author huangming
 * @date 2015-6-25
 */
public class LimitedTextWatcher implements TextWatcher {
    
    public enum FilterMode {
        NUMBER, // 只允许数字
        A_Z_NUMBER, // 只允许字母和数字
        EMAIL, // 只允许email格式
        PASSWORD, // 只允许密码
        CHINESE_ENGLISH, // 中英文
        CHINESE_ENGLISH_NUMBER, // 中英文数字
        CHINESE_ENGLISH_SPACE_DOT, // 中英文数字空格英文点，首位不为空格
        // MOBILE, // 手机号
        // CITIZEN_CARD, // 身份证
        NO_CHINESE, // 禁止中文
        NO_EMOJI, // 禁止表情
        NO_CHINESE_EMOJI, // 禁止表情和中文
        NONE// 不做任何过滤
    }
    
    private int mMaxLength = -1;
    private FilterMode mFilterMode = FilterMode.NO_EMOJI;
    
    private static final String TAG = "LimitedTextWatcher";
    private EditInfo mEditInfo = new EditInfo();
    
    class EditInfo {
        boolean isAdding;
        CharSequence addedString;
        int startIdx;
        int count;
        int totalCount;
        
        void clear() {
            isAdding = false;
            addedString = null;
            startIdx = count = -1;
        }
    }
    
    public LimitedTextWatcher() {
        this(Integer.MAX_VALUE, FilterMode.NO_EMOJI);
    }
    
    public LimitedTextWatcher(int maxLength) {
        this(maxLength, FilterMode.NO_EMOJI);
    }
    
    public LimitedTextWatcher(FilterMode filter) {
        this(Integer.MAX_VALUE, filter);
    }
    
    public LimitedTextWatcher(int maxLength, FilterMode filter) {
        mMaxLength = maxLength;
        mFilterMode = filter;
    }
    
    public LimitedTextWatcher setMaxLength(int maxLength) {
        mMaxLength = maxLength;
        return this;
    }
    
    public LimitedTextWatcher setFilterMode(FilterMode mode) {
        mFilterMode = mode;
        return this;
    }
    
    public int getLeftCount() {
        return mMaxLength - mEditInfo.totalCount;
    }
    
    public int getMaxLength() {
        return mMaxLength;
    }
    
    public int getCurrentLength() {
        return mEditInfo.totalCount;
    }
    
    @Override
    public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
        
//        Logger.v(TAG, "  beforeTextChanged  " + "s=" + s + "  start=" + start
//                + "  count=" + count + "  after=" + after);
        mEditInfo.clear();
        mEditInfo.isAdding = (after >= count);
        mEditInfo.totalCount = s.length();
    }
    
    @Override
    public final void onTextChanged(CharSequence s, int start, int before, int count) {
//        Logger.v(TAG, "  onTextChanged  " + "s=" + s + "  start=" + start + "  count="
//                + count + "  before=" + before);
        if (mEditInfo.isAdding) {
            mEditInfo.startIdx = start;
            mEditInfo.count = count;
            try {
                mEditInfo.addedString = s.subSequence(start, start + count);
            } catch (Exception e) {
                Logger.w(TAG, "--onTextChanged--", e);
            }
        }
        
        mEditInfo.totalCount -= (before - count);
    }
    
    @Override
    @CallSuper
    public final void afterTextChanged(Editable s) {
//        Logger.v(TAG, "  afterTextChanged  " + "s=" + s);
        if (mEditInfo.isAdding) {
            // 增加字串
            if (!TextUtils.isEmpty(mEditInfo.addedString))
                filterTextAfterChanged(s);
        }

        afterTextChecked(s);
    }

    /**
     * 用于被子类覆写，以处理输入后的逻辑
     * */
    public void afterTextChecked(Editable s){

    }
    
    private void filterTextAfterChanged(Editable s) {
        
        boolean filterOk = true;
        String finalInput = null;
        
        switch (mFilterMode) {
        
            case NUMBER:
                
                // filterString =
                // StringUtil.filterNumber(mEditInfo.addedString.toString());
                // s.replace(mEditInfo.startIdx, mEditInfo.startIdx +
                // mEditInfo.count,
                // filterString);
                // return;
                if (!StringUtil.checkNumber(mEditInfo.addedString.toString()))
                    filterOk = false;
                break;
            case A_Z_NUMBER:
                if (!StringUtil.checkCharAndNumber(mEditInfo.addedString.toString()))
                    filterOk = false;
                break;
            case EMAIL:
                if (!StringUtil.checkEmail(s.toString()))
                    filterOk = false;
                break;
            case PASSWORD:
                if (!StringUtil.checkPassword(mEditInfo.addedString.toString()))
                    filterOk = false;
                break;
            case CHINESE_ENGLISH:
                finalInput = mEditInfo.addedString.toString();
                finalInput = finalInput.replaceAll("'", "");
                if (!StringUtil.checkChineseEnglish(finalInput))
                    filterOk = false;
                break;
            case CHINESE_ENGLISH_NUMBER:
                finalInput = mEditInfo.addedString.toString();
                finalInput = finalInput.replaceAll("'", "");
                if (!StringUtil.checkChineseEnglishNumber(finalInput))
                    filterOk = false;
                break;
            case CHINESE_ENGLISH_SPACE_DOT:
                finalInput = mEditInfo.addedString.toString();
                finalInput = finalInput.replaceAll("'", "");
                if (!StringUtil.checkChineseEnglishSpaceDot(finalInput)
                        || (mEditInfo.startIdx == 0 && finalInput.equals(" "))) {
                    filterOk = false;
                }
                break;
            case NO_CHINESE:
                if (StringUtil.containChinese(mEditInfo.addedString.toString()))
                    filterOk = false;
                break;
            case NO_EMOJI:
                if (StringUtil.containEmoji(mEditInfo.addedString.toString())
                        || StringUtil.containUtf8GraphChar(mEditInfo.addedString
                                .toString()))
                    filterOk = false;
                break;
            case NO_CHINESE_EMOJI:
                if (StringUtil.containChinese(mEditInfo.addedString.toString()))
                    filterOk = false;
                else if (StringUtil.containEmoji(mEditInfo.addedString.toString())
                        || StringUtil.containUtf8GraphChar(mEditInfo.addedString
                                .toString()))
                    filterOk = false;
                break;
            case NONE:
                break;
        }
        
        if (!filterOk) {
            s.delete(mEditInfo.startIdx, mEditInfo.startIdx + mEditInfo.count);
        }
        else {
            // 如果长度超过，则要截断
            int countForDelete = s.length() - mMaxLength;
            if (countForDelete > 0) {
                s.delete(mMaxLength, s.length());
            }
        }
    }
}
