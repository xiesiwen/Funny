package com.qingqing.project.offline.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.qingqing.api.proto.v1.ImageProto;
import com.qingqing.base.SelectPictureManager;
import com.qingqing.base.core.UploadManager;
import com.qingqing.base.dialog.CompDefaultDialogBuilder;
import com.qingqing.base.utils.DisplayUtil;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.pager.IconPageIndicator;
import com.qingqing.base.view.pager.NoReuseViewPagerAdapter;
import com.qingqing.base.view.pager.Page;
import com.qingqing.base.view.pager.UploadGalleryPage;
import com.qingqing.base.view.pager.ViewPagerAdapter;
import com.qingqing.project.offline.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangyutian on 2017/5/8.
 * 基本所有设置都需要写在xml里
 * showMode支持grid、横向和竖向的linear
 * gridCount仅当showMode是grid的时候有用，这时候childItem的width最好是match_parent
 * childItem可以自定义图片，需要一个id是R.id.iv_img_content的{@link AsyncImageViewV2}
 * showAdd是在最后显示一个添加图片的+号，不显示的时候也可以手动调用{@link #addPhoto()}
 * maxSize是最多添加图片的上限
 * showDelete是全屏显示图片的dialog有没有删除按钮
 * dividerHeight和dividerColor是控制分割线
 * uploadType控制上传类型,现在只支持了{@link ImageProto.ImageUploadType#homework_upload_type}和{@link ImageProto.ImageUploadType#study_trace_upload_type}
 * 需要添加图片需要{@link #setSelectPictureManager(SelectPictureManager)}
 * 需要上传图片需要{@link #setUploadListener(UploadListener)}
 */

public class ImageUploadView extends RecyclerView implements SelectPictureManager.SelectPicListener,View.OnClickListener,UploadManager.UploadProtoImgListener {
    private final int SHOW_MODE_GRID = 1;
    private final int SHOW_MODE_HORIZONTAL_LINEAR = 2;
    private final int SHOW_MODE_VERTICAL_LINEAR = 3;

    private final int UPLOAD_HOMEWORK = 1;
    private final int UPLOAD_STUDY_TRACE = 2;
    private boolean showAdd = false;
    private ArrayList mList = new ArrayList();
    private LayoutManager mLayoutManager;
    private int itemResource;
    private ImageUploadRecyclerAdapter mAdapter;
    private int maxSize = 9;
    private int dividerColor;
    private int dividerHeight;
    private SelectPictureManager mSelectPictureManager;
    private boolean showDelete = false;
    private Dialog mGalleryDialog;
    private ViewPager mGalleryViewPager;
    private TextView mGalleryTextView;
    private List<Page> galleryPageList = new ArrayList<Page>();
    private NoReuseViewPagerAdapter mGalleryAdapter;
    private IconPageIndicator mGalleryIndicator;
    private ArrayList<ImageProto.ImageItem> mUploadList = new ArrayList<ImageProto.ImageItem>();
    private UploadListener mUploadListener;
    private boolean isUploading = false;
    private int uploadType = UPLOAD_HOMEWORK;



    public ImageUploadView(Context context){
        this(context, null);
    }

    public ImageUploadView(Context context, @Nullable AttributeSet attrs){
        this(context, attrs, 0);
    }

    public ImageUploadView(Context context, @Nullable AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        init(context,attrs);
    }

    private void init(Context context,@Nullable AttributeSet attrs){
        if (attrs!=null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageUploadView);
            int showMode = typedArray.getInt(R.styleable.ImageUploadView_showMode,SHOW_MODE_GRID);
            if (showMode == SHOW_MODE_GRID){
                int gridCount = typedArray.getInt(R.styleable.ImageUploadView_gridCount,3);
                mLayoutManager = new GridLayoutManager(context,gridCount);
            }else if (showMode == SHOW_MODE_HORIZONTAL_LINEAR){
                mLayoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
            }else{
                mLayoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
            }
            setLayoutManager(mLayoutManager);
            showDelete = typedArray.getBoolean(R.styleable.ImageUploadView_showDelete,false);
            uploadType = typedArray.getInt(R.styleable.ImageUploadView_uploadType,UPLOAD_HOMEWORK);
            showAdd = typedArray.getBoolean(R.styleable.ImageUploadView_showAdd,false);
            maxSize = typedArray.getInt(R.styleable.ImageUploadView_maxSize,9);
            itemResource = typedArray.getResourceId(R.styleable.ImageUploadView_childItem,R.layout.item_upload_photo);
            dividerColor = typedArray.getColor(R.styleable.ImageUploadView_dividerColor,getContext().getResources().getColor(R.color.transparent));
            dividerHeight = typedArray.getDimensionPixelSize(R.styleable.ImageUploadView_dividerHeight, DisplayUtil.dp2px(6));
            addItemDecoration(new MyDecoration(getContext(),dividerHeight,dividerColor));
            mAdapter = new ImageUploadRecyclerAdapter();
            setAdapter(mAdapter);
            typedArray.recycle();
        }
    }

    public void setData(List list){
        if (mAdapter == null){
            mList.addAll(list);
        }else{
            mList.clear();
            mList.addAll(list);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            if (mGalleryDialog.isShowing()) {
                mGalleryDialog.dismiss();
            }
        } else if (v.getId() == R.id.delete) {
            new CompDefaultDialogBuilder(getContext())
                    .setContent(getContext().getString(R.string.text_alert_confirm_delete))
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            int current = mGalleryViewPager.getCurrentItem();
                            mList.remove(current);
                            mAdapter.notifyDataSetChanged();
                            galleryPageList.remove(current);
                            mGalleryAdapter.notifyDataSetChanged();
                            mGalleryIndicator.notifyDataSetChanged();
                            if (galleryPageList.size() == 0 && mGalleryDialog.isShowing()) {
                                mGalleryDialog.dismiss();
                            } else {
                                mGalleryTextView.setText((mGalleryViewPager.getCurrentItem() + 1) + " / " + mGalleryAdapter.getItemCount());
                            }
                        }
                    })
                    .setNegativeButton("取消",null).setCancelable(false).show();
        }else {
            try {
                int position = (int) v.getTag();
                if (showAdd && position == mAdapter.getItemCount() - 1) {
                    addPhoto();
                }else{
                    enterGallery(position);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void enterGallery(int position){
        if (mGalleryDialog ==null) {
            mGalleryDialog = new Dialog(getContext());
            FrameLayout decorView = (FrameLayout) mGalleryDialog.getWindow().getDecorView();
            decorView.removeAllViews();
            decorView.setBackgroundResource(R.color.translucence_black);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dlg_homework_gallery,decorView,false);
            decorView.addView(view);
            WindowManager.LayoutParams lp = mGalleryDialog.getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            mGalleryDialog.getWindow().setAttributes(lp);
            mGalleryDialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
            mGalleryViewPager = (ViewPager) mGalleryDialog.findViewById(R.id.viewpager);
            mGalleryTextView = (TextView) mGalleryDialog.findViewById(R.id.gallery_count);

            mGalleryAdapter = new NoReuseViewPagerAdapter(galleryPageList) {
                @Override
                public ImageView getIndicatorIcon(Context context, ViewGroup parent) {
                    return (ImageView) LayoutInflater.from(context).inflate(
                            R.layout.indicator_icon_guide, parent, false);
                }
            };
            mGalleryAdapter.setOnPageClickListener(new ViewPagerAdapter.OnPageClickListener() {
                @Override
                public void onPageClick(View pageView, int position, Page page) {
                    if (mGalleryDialog.isShowing()){
                        mGalleryDialog.dismiss();
                    }
                }
            });
            mGalleryViewPager.setAdapter(mGalleryAdapter);
            mGalleryViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mGalleryTextView.setText((position+1)+" / "+ mGalleryAdapter.getItemCount());
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            mGalleryIndicator = (IconPageIndicator) mGalleryDialog.findViewById(R.id.indicator);
            mGalleryIndicator.setViewPager(mGalleryViewPager);
            mGalleryDialog.findViewById(R.id.back).setOnClickListener(this);
            mGalleryDialog.findViewById(R.id.delete).setOnClickListener(this);
            if (!showDelete){
                mGalleryDialog.findViewById(R.id.delete).setVisibility(GONE);
            }
        }
        mGalleryViewPager.removeAllViewsInLayout();
        galleryPageList.clear();
        for (int i=0;i<mList.size()-1;i++){
            galleryPageList.add(new UploadGalleryPage(mList.get(i)));
        }
        mGalleryAdapter.notifyDataSetChanged();
        mGalleryIndicator.notifyDataSetChanged();
        mGalleryTextView.setText(position+1+" / "+galleryPageList.size());
        mGalleryViewPager.setCurrentItem(position);
        mGalleryDialog.show();
    }

    /**
     * @return 能添加返回true，达到最大上限不能添加返回false
     */
    public boolean addPhoto(){
        if (mAdapter ==null){
            return false;
        }
        if (mList.size()>=maxSize){
            ToastWrapper.show("不能再添加更多了");
            return false;
        }else{
            if (mSelectPictureManager!=null){
                mSelectPictureManager.maxSize(maxSize-mList.size()).start();
                return true;
            }else{
                return false;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSelectPictureManager != null) {
            mSelectPictureManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startUpload(){
        if (!isUploading && mUploadListener!=null && mList.size()>0){
            isUploading = true;
            mUploadList.clear();
            if (mUploadListener!=null){
                mUploadListener.onStart();
            }
            for (int i=0;i<mList.size();i++){
                Object object = mList.get(i);
                if (checkCanUpload(object)){
                    if (checkNeedUpload(object)){
                        upload(object,i);
                        break;
                    }else{
                        if (i == mList.size() - 1) {
                            finishUpload();
                        }
                    }
                }else {
                    if (i == mList.size() - 1) {
                        finishUpload();
                    }
                }
            }
        }
    }

    @Override
    public void onUploadDone(int tag, boolean ret) {
        if (!ret) {
            isUploading = false;
            if (mUploadListener!=null){
                mUploadListener.onFail();
            }
        }
    }

    @Override
    public void onUploadImgDone(int tag, long picId, String picPath) {
        ImageProto.ImageItem imageItem = new ImageProto.ImageItem();
        imageItem.imageId = picId;
        imageItem.imagePath = picPath;
        imageItem.hasImagePath = true;
        imageItem.hasImageId = true;
        mUploadList.add(imageItem);
        if (tag == mList.size() - 1) {
           finishUpload();
        } else {
            for (int i = tag + 1; i < mList.size(); i++) {
                Object object = mList.get(i);
                if (checkCanUpload(object)) {
                    if (checkNeedUpload(object)) {
                        upload(object, i);
                        break;
                    } else {
                        if (i == mList.size() - 1) {
                            finishUpload();
                        }
                    }
                } else {
                    if (i == mList.size() - 1) {
                        finishUpload();
                    }
                }
            }
        }
    }

    private void upload(Object object,int position){
        if (object instanceof ImageProto.ImageItem){
            UploadManager.INSTANCE().uploadImgV2(uploadType==UPLOAD_HOMEWORK?ImageProto.ImageUploadType.homework_upload_type:ImageProto.ImageUploadType.study_trace_upload_type,position, new File(((ImageProto.ImageItem) object).imagePath), this);
        }else if (object instanceof String){
            UploadManager.INSTANCE().uploadImgV2(uploadType==UPLOAD_HOMEWORK?ImageProto.ImageUploadType.homework_upload_type:ImageProto.ImageUploadType.study_trace_upload_type,position, new File((String)object), this);
        }else if (object instanceof File){
            UploadManager.INSTANCE().uploadImgV2(uploadType==UPLOAD_HOMEWORK?ImageProto.ImageUploadType.homework_upload_type:ImageProto.ImageUploadType.study_trace_upload_type,position, (File) object, this);
        }
    }

    private void finishUpload(){
        isUploading = false;
        if (mUploadListener!=null){
            mUploadListener.onSuccess(mUploadList);
        }
    }

    private boolean checkCanUpload(Object object){
        if (object instanceof File){
            return true;
        }else if (object instanceof String){
            if (((String) object).contains("http")){
                return false;
            }else{
                return true;
            }
        }else if (object instanceof ImageProto.ImageItem){
            if (((ImageProto.ImageItem) object).imagePath.contains("http")){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    private boolean checkNeedUpload(Object object){
        if (object instanceof File){
            return true;
        }else if (object instanceof String){
            if (((String) object).startsWith("/homework") || ((String) object).startsWith("/studytrace") || ((String) object).startsWith("/pic")){
                return false;
            }else{
                return true;
            }
        }else if (object instanceof ImageProto.ImageItem){
            if (((ImageProto.ImageItem) object).imagePath.startsWith("/homework") || ((ImageProto.ImageItem) object).imagePath.startsWith("/studytrace") || ((ImageProto.ImageItem) object).imagePath.startsWith("/pic")){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public void setSelectPictureManager(SelectPictureManager manager){
        this.mSelectPictureManager = manager;
        mSelectPictureManager.setSelectPicListener(this);
    }

    @Override
    public void onPicSelected(int key, File outputFile) {
        mList.add(outputFile);
        if (mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }



    private String getHeadImg(String url){
        if (url.startsWith("/homework")  || url.startsWith("/studytrace") || url.startsWith("/pic")){
            return ImageUrlUtil.getHeadImg(url);
        }else if (url.contains("http")){
            return url;
        }else{
            return "";
        }
    }

    public void setUploadListener(UploadListener uploadListener) {
        mUploadListener = uploadListener;
    }

    private class ImageUploadRecyclerAdapter extends Adapter<ImageUploadRecyclerAdapter.ImageUploadViewHolder>{


        @Override
        public void onBindViewHolder(ImageUploadViewHolder holder, int position) {
            if (showAdd && position == getItemCount()-1){
                holder.iv.setImageUrl(null,0,0,R.drawable.icon_preparation_picture);
            }else {
                Object object = mList.get(position);
                if (object instanceof File) {
                    holder.iv.setImageUrl(Uri.fromFile((File) object),R.drawable.default_pic01);
                } else {
                    String url = null;
                    if (object instanceof ImageProto.ImageItem){
                        url = ((ImageProto.ImageItem) object).imagePath;
                    }else if (object instanceof String){
                        url = (String) object;
                    }
                    if (!TextUtils.isEmpty(url)){
                        holder.iv.setImageUrl(getHeadImg(url),R.drawable.default_pic01);
                    }
                }
            }
            holder.iv.setTag(position);
            holder.iv.setOnClickListener(ImageUploadView.this);
        }

        @Override
        public ImageUploadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ImageUploadViewHolder(LayoutInflater.from(parent.getContext()).inflate(itemResource,parent,false));
        }

        @Override
        public int getItemCount() {
            return mList.size()+ (showAdd?1:0);
        }

        class ImageUploadViewHolder extends ViewHolder{
            AsyncImageViewV2 iv;
            ImageUploadViewHolder(View view){
                super(view);
                iv = (AsyncImageViewV2)view.findViewById(R.id.iv_img_content);
            }
        }
    }

    private class MyDecoration extends ItemDecoration{
        private Drawable mDivider;
        private int dividerLength = 50;

        public MyDecoration(Context context,int length, int color){
            mDivider = new ColorDrawable(color);
            dividerLength = length;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, State state)
        {

            drawHorizontal(c, parent);
            drawVertical(c, parent);

        }

        private int getSpanCount(RecyclerView parent)
        {
            // 列数
            int spanCount = -1;
            LayoutManager layoutManager = parent.getLayoutManager();
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
                final LayoutParams params = (LayoutParams) child
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

                final LayoutParams params = (LayoutParams) child
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
            LayoutManager layoutManager = parent.getLayoutManager();
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
            }else if (layoutManager instanceof LinearLayoutManager){
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.VERTICAL){
                    return true;
                }else if (pos>=childCount-1){
                    return true;
                }
            }
            return false;
        }

        private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                                  int childCount)
        {
            LayoutManager layoutManager = parent.getLayoutManager();
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
            }else if (layoutManager instanceof LinearLayoutManager){
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL){
                    return true;
                }else if (pos>=childCount-1){
                    return true;
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
            if (isLastRaw(parent, itemPosition, spanCount, childCount) && isLastColum(parent, itemPosition, spanCount, childCount)){
                outRect.set(0,0,0,0);
            }else if (isLastRaw(parent, itemPosition, spanCount, childCount))// 如果是最后一行，则不需要绘制底部
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

    public interface UploadListener{
        void onStart();
        void onFail();
        void onSuccess(List<ImageProto.ImageItem> items);
    }
}
