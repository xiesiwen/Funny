package com.qingqing.project.offline.homework;

import android.view.View;

/**
 * RecyclerView带长按的Listener
 */

public interface OnItemClickListener {
    void onItemClick(View view, int position);
    void onItemLongClick(View view , int position);
}
