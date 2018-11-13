package com.qingqing.base.nim.view;

/**
 * Created by huangming on 2016/8/23.
 */
public interface IExtendMenuView {

    boolean isVisible();

    void toggleVisible();

    void show();

    void hide();

    boolean onBackPressed();

}
