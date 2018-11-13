package com.qingqing.qingqingbase.ui;

import com.qingqing.base.view.Toolbar;
import com.qingqing.qingqingbase.R;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by huangming on 2015/12/15. ToolBar:适配先前版本
 */
public abstract class BaseActionBarActivity extends BaseActivity {

    protected Toolbar mToolbar;
    protected TextView mTitle;
    private ViewGroup mContentParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.abs_ac_screen);
        initializeToolbar();

    }

    public Toolbar getToolBar() {
        return mToolbar;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (getToolBar() != null) {
            getToolBar().setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        if (getToolBar() != null) {
            getToolBar().setTitle(titleId);
        }
    }

    @Override
    public void onSetStatusBarMode() {
        setStatusBarColor(R.color.white_light,true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 调用在setContentView之后
     */
    protected void initializeToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.ac_tool_bar);
        mContentParent = (ViewGroup) findViewById(R.id.ac_screen_container);
        if (mToolbar == null) {
            throw new IllegalStateException(
                    "Layout is required to include a Toolbar with id " + "'tool_bar'");
        }
        if (mContentParent == null) {
            throw new IllegalStateException("mContentParent can not be null!");
        }

        setSupportActionBar(mToolbar);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        ensureOnlyToolbar();
        mContentParent.addView(view, params);
    }

    private void ensureOnlyToolbar() {
        int count = mContentParent.getChildCount();
        int index = 0;
        while (index < count) {
            View child = mContentParent.getChildAt(index);
            if (child == mToolbar) {
                index++;
            }
            else {
                mContentParent.removeView(child);
                count--;
            }
        }
    }

    @Deprecated
    public void showActionBar() {
        if (mToolbar != null) {
            mToolbar.setVisibility(View.VISIBLE);
        }
    }

    @Deprecated
    public void hideActionBar() {
        if (mToolbar != null) {
            mToolbar.setVisibility(View.GONE);
        }
    }

    @Deprecated
    public void setActionBarTitle(String title) {
        setTitle(title);
    }

    @Deprecated
    public void setActionBarTitle(int resId) {
        setTitle(resId);
    }

    @Deprecated
    public void setCustomTheme(int theme) {}

    protected ActionBar getMyActionBar() {
        return getSupportActionBar();
    }

   /* public void setStatusBarColor(int color) {
        // if (mDrawerLayout != null) {
        // mDrawerLayout.setStatusBarBackgroundColor(color);
        // }
    }*/
}
