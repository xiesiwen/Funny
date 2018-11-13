package com.qingqing.base.ui;

import java.lang.ref.WeakReference;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import com.qingqing.base.R;
import com.qingqing.base.log.Logger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * @author richie.wang
 *
 *
 *         用于 Activity or Fragment中 对 子Fragment 的管理 <br>
 * @version 4.5.5(2015.7.10)第一版<br>
 * @version 5.7.0(2017.3.30) <br>
 *          1.整理了switch模式的实现，去掉自己存储的list，改为使用FragmentManager里保存的Fragment list
 *          来做逻辑处理<br>
 *          2.push模式下，去掉了fragment相关的tag设置，只保留bottom fragment的tag
 */
public class FragmentAssist {
    
    public enum FragOPMode {
        MODE_NONE, // 默认无效值
        MODE_REPLACE, // 替换模式（每次进入的时候，都会delete之前的，add新的，操作默认会进入事务堆栈）
        MODE_SWITCH// 切换模式（只add一次，通过hide，show来控制）
    }
    
    private static final String TAG_FRAG_ASSIST = "FragmentAssist";
    
    private static final String TAG_BOTTOM = "ASSIST_FRAG_BOTTOM";
    private static final String TAG_BACK_STACK_PREFIX = "BACK_STACK_";

    private FragmentManager mFragMgr;
    private int mGroupID;
    private Stack<WeakReference<AbstractFragment>> mFragStack;
    private FragOPMode mFragOPMode = FragOPMode.MODE_NONE;
    private long mLastOpTime;
    private WeakReference<?> mUIUnitRef;
    
    // 如果要查看Fragment管理的详细操作信息，可以打开此开关
    // static {
    // FragmentManager.enableDebugLogging(true);
    // }
    
    public FragmentAssist(FragmentActivity activity) {
        this(activity, -1);
    }
    
    public FragmentAssist(AbstractFragment fragment) {
        this(fragment, -1);
    }
    
    public FragmentAssist(FragmentActivity activity, int groupID) {
        this(activity, groupID, FragOPMode.MODE_REPLACE);
    }
    
    public FragmentAssist(AbstractFragment fragment, int groupID) {
        this(fragment, groupID, FragOPMode.MODE_REPLACE);
    }
    
    public FragmentAssist(Object uiUnit, int groupID, FragOPMode opMode) {
        mUIUnitRef = new WeakReference<>(uiUnit);
        prepareFragMgr();
        mGroupID = groupID;
        mFragOPMode = opMode;
        mFragStack = new Stack<>();
        OnBackStackChangedListener stackListener = new OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Logger.d(TAG_FRAG_ASSIST, "cur fragment count = " + getFragmentsCount()
                        + "---cur back stack count=" + mFragMgr.getBackStackEntryCount());
            }
        };
        
        mFragMgr.addOnBackStackChangedListener(stackListener);
    }
    
    private void prepareFragMgr() {
        if (mUIUnitRef != null && mUIUnitRef.get() != null) {
            if (mUIUnitRef.get() instanceof FragmentActivity) {
                mFragMgr = ((FragmentActivity) mUIUnitRef.get())
                        .getSupportFragmentManager();
            }
            else if (mUIUnitRef.get() instanceof AbstractFragment) {
                mFragMgr = ((AbstractFragment) mUIUnitRef.get()).getChildFragmentManager();
            }
            else {
                throw new RuntimeException(
                        "what ? I just accept instance of FragmentActivity or AbstractFragment");
            }
        }
    }
    
    public FragmentAssist setGroupID(int groupID) {
        mGroupID = groupID;
        return this;
    }
    
    public FragmentAssist setFragOPMode(FragOPMode mode) {
        // if(mFragOPMode != FragOPMode.MODE_NONE){
        // throw new RuntimeException("frag mode cannot change!");
        // }
        mFragOPMode = mode;
        return this;
    }
    
    private void checkReplaceMode() {
        if (mFragOPMode != FragOPMode.MODE_REPLACE) {
            throw new RuntimeException(
                    "current mode != MODE_REPLACE,you can call setFragOPMode(FragOPMode.MODE_REPLACE)");
        }
    }
    
    private void checkSwitchMode() {
        if (mFragOPMode != FragOPMode.MODE_SWITCH) {
            throw new RuntimeException(
                    "current mode != MODE_SWITCH,you can call setFragOPMode(FragOPMode.MODE_SWITCH)");
        }
    }
    
    private boolean isUIValid() {
        return mUIUnitRef != null && mUIUnitRef.get() != null
                && ((mUIUnitRef.get() instanceof AbstractActivity
                        && ((AbstractActivity) mUIUnitRef.get()).couldOperateUI())
                        || (mUIUnitRef.get() instanceof AbstractFragment
                                && ((AbstractFragment) mUIUnitRef.get()).couldOperateUI()));
    }
    
    /** 设置activity 最底部的fragment */
    public void setBottom(AbstractFragment fragment) {
        
        checkReplaceMode();
        FragmentTransaction transaction = mFragMgr.beginTransaction();
        transaction.replace(mGroupID, fragment, TAG_BOTTOM);
        transaction.commitAllowingStateLoss();
        
        mFragStack.clear();
        mFragStack.push(new WeakReference<>(fragment));
    }
    
    public int getFragmentsCount() {
        return mFragStack.size();
    }

    public Stack<WeakReference<AbstractFragment>> getFragStack() {
        return mFragStack;
    }
    
    public boolean canGoback() {
        return mFragOPMode == FragOPMode.MODE_SWITCH || mFragStack.size() > 1;
    }
    
    public AbstractFragment getTopFragment() {
        
        try {
            WeakReference<AbstractFragment> fragRef = mFragStack.peek();
            if (fragRef != null) {
                return fragRef.get();
            }
        } catch (EmptyStackException e) {
            return null;
        }
        
        return null;
    }
    
    public int getBackStackCount() {
        return mFragMgr.getBackStackEntryCount();
    }
    
    /** 除了最底部的fragment以外，其他的都弹出 */
    public void popAllFront() {
        
        checkReplaceMode();
        while (mFragStack.size() > 1) {
            mFragStack.pop();
        }
        
        mFragMgr.popBackStack(TAG_BOTTOM, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
    
    // public void popAllFrontMenual() {
    // FragmentTransaction transaction = mFragMgr.beginTransaction();
    // int count = getFragmentsCount();
    // for (int i = count - 1; i > 0; i--) {
    // Fragment frag = mFragMgr.findFragmentByTag(TAG_FRAG_PREFIX + i);
    // if (frag != null) {
    // transaction.remove(frag);
    // }
    // }
    // transaction.commit();
    // }
    
    /** 压入一个fragment */
    public void push(AbstractFragment fragment) {
        push(fragment, true);
    }
    
    /** 压入一个fragment */
    public void push(AbstractFragment fragment, boolean needReplace) {
        push(fragment, needReplace, true);
    }
    
    public void push(AbstractFragment fragment, boolean needReplace, boolean needAni) {
        
        if (!isUIValid())
            return;
        
        checkReplaceMode();
        int count = getFragmentsCount();
        
        // 如果还没有bottom fragment，直接设置
        if (count <= 0) {
            Logger.w(TAG_FRAG_ASSIST, "auto call setBottom first : frag=" + fragment);
            setBottom(fragment);
            return;
        }
        
        // 如果顶部的fragment为空，忽略
        Fragment frag = getTopFragment();
        if (frag == null) {
            Logger.w(TAG_FRAG_ASSIST, "topFragment is null");
            return;
        }
        
        // 如果fragment 已添加，则忽略
        if (isFragExist(fragment)) {
            Logger.w(TAG_FRAG_ASSIST, "fragment  " + fragment + " already added");
            return;
        }
        
        // 4.7 加入时间检测 300ms内只允许一次push操作
        long curTime = System.currentTimeMillis();
        if (curTime - mLastOpTime > 0 && curTime - mLastOpTime < 300) {
            return;
        }
        
        mLastOpTime = curTime;
        
        FragmentTransaction transaction = mFragMgr.beginTransaction();
        if (needAni) {
            // transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.setCustomAnimations(R.anim.slide_alpha_in_from_right,
                    R.anim.slide_alpha_out_to_left, R.anim.slide_alpha_in_from_left,
                    R.anim.slide_alpha_out_to_right);
        }
        
        if (!needReplace) {
            transaction.hide(frag);
            transaction.add(mGroupID, fragment);
        }
        else {
            transaction.replace(mGroupID, fragment);
        }
        transaction.addToBackStack(TAG_BACK_STACK_PREFIX + count);
        transaction.commitAllowingStateLoss();
        mFragStack.push(new WeakReference<>(fragment));
    }
    
    /** 替换栈顶的fragment，不记录 */
    public void replace(AbstractFragment fragment) {
        
        checkReplaceMode();
        int count = getFragmentsCount();
        
        if (count <= 0) {
            throw new RuntimeException("should call setBottom first");
        }
        
        mFragStack.pop();
        mFragStack.push(new WeakReference<>(fragment));
        FragmentTransaction transaction = mFragMgr.beginTransaction();
        transaction.replace(mGroupID, fragment);
        transaction.commitAllowingStateLoss();
    }
    
    /** 弹出顶部的fragment */
    public boolean pop() {
        checkReplaceMode();
        if (getBackStackCount() > 0) {
            if (getFragmentsCount() > 0)
                mFragStack.pop();
            mFragMgr.popBackStack();
            return true;
        }
        
        return false;
    }
    
    private void hideCurrentSwitchedFragment(FragmentTransaction transaction,
            Fragment exceptFrag) {
        List<Fragment> fragList = mFragMgr.getFragments();
        if (fragList != null && !fragList.isEmpty()) {
            for (Fragment frag : fragList) {
                if (exceptFrag != frag && frag != null) {
                    if (!frag.isHidden())
                        transaction.hide(frag);
                    // frag.setUserVisibleHint(false);
                }
            }
        }
    }
    
    public void addForSwitch(AbstractFragment... fragments) {
        FragmentTransaction transaction = mFragMgr.beginTransaction();
        boolean ret = false;
        boolean eachRet;
        for (AbstractFragment frag : fragments) {
            eachRet = addForSwitch(transaction, frag);
            if (eachRet) {
                ret = true;
            }
        }
        if (ret) {
            transaction.commitAllowingStateLoss();
        }
    }
    
    /**
     * 基于MODE_SWITCH，事先添加fragment，默认为hide
     *
     * @param fragment
     *            想要切换的fragment
     */
    public void addForSwitch(AbstractFragment fragment) {
        Log.i("FragmentAssist", "addForSwitch  " + fragment);
        FragmentTransaction transaction = mFragMgr.beginTransaction();
        if (addForSwitch(transaction, fragment)) {
            transaction.commitAllowingStateLoss();
        }
    }
    
    private boolean isFragExist(AbstractFragment fragment) {
        List<Fragment> fragList = mFragMgr.getFragments();
        if (fragList != null && !fragList.isEmpty()) {
            for (Fragment frag : fragList) {
                if (frag != null && fragment == frag) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean addForSwitch(FragmentTransaction transaction, AbstractFragment fragment) {
        
        if (!isUIValid())
            return false;
        
        checkSwitchMode();
        boolean exist = isFragExist(fragment);
        if (!exist) {
            transaction.add(mGroupID, fragment).hide(fragment);
            // fragment.setUserVisibleHint(false);
            return true;
        }
        return false;
    }
    
    /**
     * 切换到某个fragment，只适用于MODE_SWITCH
     *
     * @param fragment
     *            想要切换的fragment
     * @return 切换是否成功
     */
    public boolean switchTo(AbstractFragment fragment) {
        
        if (!isUIValid())
            return false;
        
        checkSwitchMode();
        
        boolean exist = isFragExist(fragment);
        if (exist) {
            // 该frag已存在
            FragmentTransaction transaction = mFragMgr.beginTransaction();
            hideCurrentSwitchedFragment(transaction, fragment);
            // frag.setUserVisibleHint(true);
            transaction.show(fragment).commitAllowingStateLoss();
            
            // 已经添加过了的踢出，重新添加，保证在最上
            for (WeakReference ref : mFragStack) {
                if (ref.get() == fragment) {
                    mFragStack.remove(ref);
                    break;
                }
            }
            mFragStack.push(new WeakReference<>(fragment));
        }
        else {
            
            // 4.8 加入时间检测 300ms内只允许一次add操作，避免过于频繁的add导致崩溃
            long curTime = System.currentTimeMillis();
            if (curTime - mLastOpTime > 0 && curTime - mLastOpTime < 300) {
                return false;
            }
            mLastOpTime = curTime;
            
            // 已经添加过了，但是还没添加成功的，忽略
            for (WeakReference ref : mFragStack) {
                if (ref.get() == fragment) {
                    return false;
                }
            }
            FragmentTransaction transaction = mFragMgr.beginTransaction();
            hideCurrentSwitchedFragment(transaction, null);
            transaction.add(mGroupID, fragment).commitAllowingStateLoss();
            // fragment.setUserVisibleHint(true);
            mFragStack.push(new WeakReference<>(fragment));
        }
        return true;
    }
    
    /** 退出到指定fragment */
    public void popTillMatch(Class<? extends AbstractFragment> frag) {
        checkReplaceMode();
        while (mFragStack.size() > 1) {
            WeakReference<AbstractFragment> reference = mFragStack.peek();
            if (reference != null && reference.get().getClass().equals(frag)) {
                break;
            }
            else {
                mFragStack.pop();
                mFragMgr.popBackStackImmediate();
            }
        }
    }
}
