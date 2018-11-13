package com.qingqing.project.offline.view.calendar;

import java.util.ArrayList;
import java.util.List;

import com.qingqing.base.view.BasePagerAdapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * @author huangming
 * @date 2015-7-28
 */
public abstract class DayPagerAdapter extends BasePagerAdapter<GridView> {
    private List<GridView> mRecycledViews = new ArrayList<>();
    
    public DayPagerAdapter(Context context) {
        super(context);
    }
    
    @Override
    public GridView createView(ViewGroup container, int position) {
        GridView gridView = null;
        if (mRecycledViews.size() > 0) {
            gridView = mRecycledViews.remove(0);
        }
        return createGridView(container, gridView, position);
    }
    
    public abstract void update(GridView view, int position);
    
    public abstract GridView createGridView(ViewGroup container, GridView convertView,
            int position);
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        if (object instanceof GridView) {
            GridView child = (GridView) object;
            if (child.getParent() == null) {
                mRecycledViews.add(child);
            }
        }
    }
}
