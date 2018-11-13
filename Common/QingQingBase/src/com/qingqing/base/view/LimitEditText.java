package com.qingqing.base.view;

import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.qingqingbase.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * 封装针对EditText的统一处理逻辑
 *
 * <p>支持xml设置最大输入长度 maxLength</p>
 * <p>支持xml设置赛选类型 filterMode</p>
 *
 * Created by tanwei on 2018/1/10.
 */

public class LimitEditText extends EditText {
    
    private LimitedTextWatcher limitedTextWatcher;
    
    public LimitEditText(Context context) {
        super(context);
        
        initSetting();
    }
    
    public LimitEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        initSetting();
    }
    
    protected void initSetting() {
        limitedTextWatcher = new LimitedTextWatcher();
        super.addTextChangedListener(limitedTextWatcher);
    }
    
    /**
     * see
     * {@link com.qingqing.base.view.LimitedTextWatcher#setFilterMode(LimitedTextWatcher.FilterMode)}
     *
     * @param mode
     *            see
     *            {@link com.qingqing.base.view.LimitedTextWatcher.FilterMode}
     *
     * @return this
     */
    public LimitEditText setFilterMode(LimitedTextWatcher.FilterMode mode) {
        limitedTextWatcher.setFilterMode(mode);
        return this;
    }
    
    /**
     * see {@link com.qingqing.base.view.LimitedTextWatcher#setMaxLength(int)}
     * 
     * @param max
     *            最大输入长度
     * @return this
     */
    public LimitEditText setMaxLength(int max) {
        limitedTextWatcher.setMaxLength(max);
        return this;
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {

        // 支持设置null清除默认watcher
        if (watcher != limitedTextWatcher) {
            removeTextChangedListener(limitedTextWatcher);
        }

        if (watcher != null) {
            super.addTextChangedListener(watcher);
        }
    }
    
    private LimitedTextWatcher.FilterMode parseFilterMode(int value) {
        switch (value) {
            case 0:
                return LimitedTextWatcher.FilterMode.NUMBER;
            case 1:
                return LimitedTextWatcher.FilterMode.A_Z_NUMBER;
            case 2:
                return LimitedTextWatcher.FilterMode.EMAIL;
            case 3:
                return LimitedTextWatcher.FilterMode.PASSWORD;
            case 4:
                return LimitedTextWatcher.FilterMode.CHINESE_ENGLISH;
            case 5:
                return LimitedTextWatcher.FilterMode.CHINESE_ENGLISH_NUMBER;
            case 6:
                return LimitedTextWatcher.FilterMode.CHINESE_ENGLISH_SPACE_DOT;
            case 7:
                return LimitedTextWatcher.FilterMode.NO_CHINESE;
            case 8:
                return LimitedTextWatcher.FilterMode.NO_EMOJI;
            case 9:
                return LimitedTextWatcher.FilterMode.NO_CHINESE_EMOJI;
            case 10:
                return LimitedTextWatcher.FilterMode.NONE;
            
            default:
                return LimitedTextWatcher.FilterMode.NO_EMOJI;
        }
    }
}
