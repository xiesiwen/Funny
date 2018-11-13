package com.qingqing.project.offline.view.ncalendar;

/**
 * Created by huangming on 2017/2/8.
 */

public interface DirectionIndicator {

    int DOWNWARD_INDICATION = 1;
    int UPWARD_INDICATION = 2;

    void onIndicate(int direction);

}
