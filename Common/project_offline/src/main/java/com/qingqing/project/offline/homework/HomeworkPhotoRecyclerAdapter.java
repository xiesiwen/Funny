package com.qingqing.project.offline.homework;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qingqing.api.proto.v1.ImageProto;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.project.offline.R;

import java.util.List;

/**
 * Created by tangyutian on 2016/11/21.
 * 作业详情里的图片adapter
 */

public class HomeworkPhotoRecyclerAdapter extends RecyclerView.Adapter<HomeworkPhotoRecyclerAdapter.MyViewHolder> {
    private List<ImageProto.ImageItem> mList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;



    public HomeworkPhotoRecyclerAdapter(Context context, List<ImageProto.ImageItem> imageItems){
        this.mContext = context;
        this.mList = imageItems;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_homework_photo,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.image.setImageUrl(ImageUrlUtil.getHeadImg(mList.get(position).imagePath), R.drawable.default_pic01);
        if (mOnItemClickListener !=null){
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.image,holder.getLayoutPosition());
                }
            });
            holder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.image,holder.getLayoutPosition());
                    return false;
                }
            });
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        AsyncImageViewV2 image;
        public MyViewHolder(View view){
            super(view);
            image = (AsyncImageViewV2)view.findViewById(R.id.img_content);
        }
    }

    public void setOnItemClickLitener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public static class MyDecoration extends RecyclerView.ItemDecoration{

        private Drawable mDivider;
        private int dividerLength = 50;

        public MyDecoration(Context context)
        {
            this(context,50);
        }

        public MyDecoration(Context context,int length){
            this(context,length,R.color.white);
        }

        public MyDecoration(Context context,int length,@ColorRes int color){
            mDivider = new ColorDrawable(context.getResources().getColor(color));
            dividerLength = length;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state)
        {

            drawHorizontal(c, parent);
            drawVertical(c, parent);

        }

        private int getSpanCount(RecyclerView parent)
        {
            // 列数
            int spanCount = -1;
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager)
            {

                spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            } else if (layoutManager instanceof StaggeredGridLayoutManager)
            {
                spanCount = ((StaggeredGridLayoutManager) layoutManager)
                        .getSpanCount();
            }
            return spanCount;
        }

        public void drawHorizontal(Canvas c, RecyclerView parent)
        {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin
                        + dividerLength;
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + dividerLength;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent)
        {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                final View child = parent.getChildAt(i);

                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getTop() - params.topMargin;
                final int bottom = child.getBottom() + params.bottomMargin;
                final int left = child.getRight() + params.rightMargin;
                final int right = left + dividerLength;

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private boolean isLastColum(RecyclerView parent, int pos, int spanCount,
                                    int childCount)
        {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager)
            {
                if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                {
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager)
            {
                int orientation = ((StaggeredGridLayoutManager) layoutManager)
                        .getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL)
                {
                    if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                    {
                        return true;
                    }
                } else
                {
                    childCount = childCount - childCount % spanCount;
                    if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                        return true;
                }
            }
            return false;
        }

        private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                                  int childCount)
        {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager)
            {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                    return true;
            } else if (layoutManager instanceof StaggeredGridLayoutManager)
            {
                int orientation = ((StaggeredGridLayoutManager) layoutManager)
                        .getOrientation();
                // StaggeredGridLayoutManager 且纵向滚动
                if (orientation == StaggeredGridLayoutManager.VERTICAL)
                {
                    childCount = childCount - childCount % spanCount;
                    // 如果是最后一行，则不需要绘制底部
                    if (pos >= childCount)
                        return true;
                } else
                // StaggeredGridLayoutManager 且横向滚动
                {
                    // 如果是最后一行，则不需要绘制底部
                    if ((pos + 1) % spanCount == 0)
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition,
                                   RecyclerView parent)
        {
            int spanCount = getSpanCount(parent);
            int childCount = parent.getAdapter().getItemCount();
            if (isLastRaw(parent, itemPosition, spanCount, childCount))// 如果是最后一行，则不需要绘制底部
            {
                outRect.set(0, 0, dividerLength, 0);
            } else if (isLastColum(parent, itemPosition, spanCount, childCount))// 如果是最后一列，则不需要绘制右边
            {
                outRect.set(0, 0, 0, dividerLength);
            } else
            {
                outRect.set(0, 0, dividerLength,
                        dividerLength);
            }
        }

    }
}
