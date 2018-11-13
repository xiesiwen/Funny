package com.qingqing.project.offline.homework;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.api.image.proto.v1.MediaResource;
import com.qingqing.api.proto.v1.ImageProto.ImageItem;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.serviceslice.ServiceSliceProto;
import com.qingqing.api.proto.v1.util.Common;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.media.audiomachine.AudioDownloadMachine;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.audio.AudioDownloadView;
import com.qingqing.base.view.pager.HomeworkGalleryPage;
import com.qingqing.base.view.pager.IconPageIndicator;
import com.qingqing.base.view.pager.NoReuseViewPagerAdapter;
import com.qingqing.base.view.pager.Page;
import com.qingqing.base.view.pager.ViewPagerAdapter;
import com.qingqing.project.offline.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.View.VISIBLE;

/**
 * Created by tangyutian on 2016/9/22.
 * 作业答案详情
 */

public abstract class BaseHomeworkAnswerDetailActivity extends BaseActionBarActivity {
    protected int appType = -1;
    protected long answer_id = -1;

    protected TextView mTvMessageTitle;
    protected TextView mTvMessage;
    protected RecyclerView mRvPhoto;
    protected TextView mTvCreateTime;
    protected ArrayList<ImageItem> photoList = new ArrayList<ImageItem>();
    protected MediaResource.EncodedAudioItem audio = null;

    private Dialog mGalleryDialog;
    private ViewPager mGalleryViewPager;
    private TextView mGalleryTextView;
    private List<Page> galleryPageList = new ArrayList<Page>();
    private NoReuseViewPagerAdapter mGalleryAdapter;
    private IconPageIndicator mGalleryIndicator;
    private AudioDownloadView mAvAudioPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_answer_detail);
        getType();
        findViews();
        reqDetail();
//        test();
    }
    private void test(){
        ServiceSliceProto.HomeworkAnswerDetailResponse response = new ServiceSliceProto.HomeworkAnswerDetailResponse();
        response.hasAnswerTime = true;
        response.answerTime = 1231231313132L;
        response.hasContent =true;
        response.content = "adadaasdas";
        String[] pic = new String[]{"http://member.51dzw.com/CompanyFile/201212/20121213141042704270.jpg","http://www.eeworld.com.cn/uploadfile/mcu/uploadfile/201107/20110730094213617.png","http://www.eeworld.com.cn/uploadfile/mcu/uploadfile/201107/20110730094215943.png","http://www.eeworld.com.cn/uploadfile/mcu/uploadfile/201107/20110730094215943.png","http://www.eeworld.com.cn/uploadfile/mcu/uploadfile/201107/20110730094215943.png","http://www.eeworld.com.cn/uploadfile/mcu/uploadfile/201107/20110730094215943.png","http://www.eeworld.com.cn/uploadfile/mcu/uploadfile/201107/20110730094215943.png","http://www.eeworld.com.cn/uploadfile/mcu/uploadfile/201107/20110730094215943.png","http://www.eeworld.com.cn/uploadfile/mcu/uploadfile/201107/20110730094215943.png"};
//        response.imgs = pic;

        if (appType== AppCommon.AppType.qingqing_teacher){
            UserProto.SimpleUserInfoV2 student = new UserProto.SimpleUserInfoV2();
            student.nick = "智障";
            response.studentInfo = student;
        }
        setupView(response);
    }

    protected void getType(){
        appType = DefaultDataCache.INSTANCE().getAppType();
        if (getIntent()!=null){
            answer_id = getIntent().getLongExtra(BaseParamKeys.PARAM_LONG_HOMEWORK_ANSWER_ID ,-1);
        }
        if (answer_id==-1){
            finishBecasuseNoType();
        }
    }

    private void finishBecasuseNoType() {
        ToastWrapper.show("数据错误");
        this.finish();
    }

    protected void findViews(){
        mTvMessageTitle = (TextView) findViewById(R.id.tv_message_title);
        mTvMessage = (TextView) findViewById(R.id.tv_message);
        mRvPhoto = (RecyclerView)findViewById(R.id.rv_photo);
        mTvCreateTime = (TextView) findViewById(R.id.tv_create_time);
        ViewStub av = (ViewStub) findViewById(R.id.av_audio_play);
        av.setLayoutResource(getAudioViewLayout());
        mAvAudioPlay = (AudioDownloadView)av.inflate();
        mAvAudioPlay.setVisibility(View.GONE);
    }

    protected void reqDetail(){
        final Common.SimpleLongRequest request = new Common.SimpleLongRequest();
        request.data = answer_id;
        newProtoReq(CommonUrl.GET_HOMEWORK_ANSWER_DETAIL.url())
                .setSendMsg(request)
                .setRspListener(new ProtoListener(ServiceSliceProto.HomeworkAnswerDetailResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        setupView((ServiceSliceProto.HomeworkAnswerDetailResponse)result);
                        setResult(RESULT_OK);
                    }
                }).req();
    }

    protected void setupView(ServiceSliceProto.HomeworkAnswerDetailResponse response){
        switch (appType){
            case AppCommon.AppType.qingqing_student:
                setTitle(R.string.title_homework_answer_detail_activity_student);
                mTvMessageTitle.setText(getString(R.string.title_homework_answer_detail,""));
                break;
            case AppCommon.AppType.qingqing_teacher:
                setTitle(R.string.title_homework_answer_detail_activity_teacher);
                mTvMessageTitle.setText(getString(R.string.title_homework_answer_detail,response.studentInfo==null?"":response.studentInfo.nick));
                break;
        }
        photoList.clear();
        audio = null;
        mTvMessage.setText("");
        mTvCreateTime.setText("");
        if (response.hasAnswerTime) {
            mTvCreateTime.setVisibility(View.VISIBLE);
            mTvCreateTime.setText(getString(R.string.text_homework_upload_time,
                    DateUtils.ymdhmSdf.format(response.answerTime)));
        }else{
            mTvCreateTime.setVisibility(View.GONE);
        }
        if (response.audio != null) {
            audio = response.audio;
            mAvAudioPlay.updateMachine(new AudioDownloadMachine(this,response.audio.encodedMediaId,response.audio.timeLength));
            mAvAudioPlay.setVisibility(View.VISIBLE);
        }else {
            mAvAudioPlay.setVisibility(View.GONE);
        }
        if (response.hasContent && !TextUtils.isEmpty(response.content)) {
            mTvMessage.setVisibility(View.VISIBLE);
            mTvMessage.setText(response.content);
        }else{
            mTvMessage.setVisibility(View.GONE);
        }
        if (response.imgs.length > 0) {
            mRvPhoto.setVisibility(VISIBLE);
            photoList.addAll(Arrays.asList(response.imgs));
            mRvPhoto.setLayoutManager(new GridLayoutManager(this,3));
            HomeworkPhotoRecyclerAdapter adapter = new HomeworkPhotoRecyclerAdapter(this,photoList);
            adapter.setOnItemClickLitener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    enterGallery(position);
                }

                @Override
                public void onItemLongClick(View view, int position) {

                }
            });
            mRvPhoto.setAdapter(adapter);
            mRvPhoto.addItemDecoration(new HomeworkPhotoRecyclerAdapter.MyDecoration(this));
        }else{
            mRvPhoto.setVisibility(View.GONE);
        }
    }

    private void enterGallery(int position) {
        if (mGalleryDialog == null) {
            mGalleryDialog = new Dialog(this);
            FrameLayout decorView = (FrameLayout) mGalleryDialog.getWindow()
                    .getDecorView();
            decorView.removeAllViews();
            decorView.setBackgroundResource(R.color.translucence_black);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.dlg_homework_detail_gallery, decorView, false);
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
            mGalleryViewPager
                    .addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset,
                                                   int positionOffsetPixels) {}

                        @Override
                        public void onPageSelected(int position) {
                            mGalleryTextView.setText((position + 1) + " / "
                                    + mGalleryAdapter.getItemCount());
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {}
                    });
            mGalleryIndicator = (IconPageIndicator) mGalleryDialog
                    .findViewById(R.id.indicator);
            mGalleryIndicator.setViewPager(mGalleryViewPager);
            mGalleryDialog.findViewById(R.id.back).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mGalleryDialog.isShowing()) {
                                mGalleryDialog.dismiss();
                            }
                        }
                    });
            mGalleryDialog.findViewById(R.id.iv_save_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String url = ImageUrlUtil.getOriginImg(photoList.get(mGalleryViewPager.getCurrentItem()).imagePath);
                    ImageUrlUtil.getImageCacheType(url, new ImageUrlUtil.getImageCacheTypeListener() {
                        @Override
                        public void inCache(int type) {
                            switch (type){
                                case ImageUrlUtil.FRESCO_CACHE_TYPE_DISK:
                                    String path = ImageUrlUtil.saveImageFileFromDiskCache(url);
                                    if (path!=null){
                                        ToastWrapper.show("保存路径为"+path);
                                    }else{
                                        ToastWrapper.show("保存失败");
                                    }
                                    break;
                                case ImageUrlUtil.FRESCO_CACHE_TYPE_MEMORY:
                                    ImageUrlUtil.saveImageFileFromMemoryCache(url, BaseHomeworkAnswerDetailActivity.this, new ImageUrlUtil.saveImageFileFromMemoryCacheListener() {
                                        @Override
                                        public void onSuccess(String path) {
                                            if (path!=null){
                                                ToastWrapper.show("保存路径为"+path);
                                            }else{
                                                ToastWrapper.show("保存失败");
                                            }
                                        }

                                        @Override
                                        public void onFail() {
                                            ToastWrapper.show("保存失败");
                                        }
                                    });
                                    break;
                            }
                        }

                        @Override
                        public void notInCache() {

                        }
                    });
            }
            });

        }
        mGalleryViewPager.removeAllViewsInLayout();
        galleryPageList.clear();
        for (int i = 0; i < photoList.size(); i++) {
            galleryPageList.add(new HomeworkGalleryPage(photoList.get(i)));
        }
        mGalleryAdapter.notifyDataSetChanged();
        mGalleryIndicator.notifyDataSetChanged();
        mGalleryTextView.setText(position + 1 + " / " + galleryPageList.size());
        mGalleryViewPager.setCurrentItem(position);
        mGalleryDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (appType == AppCommon.AppType.qingqing_student){
            getMenuInflater().inflate(R.menu.menu_homework_answer, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_edit) {
            toEditAnswer(answer_id);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioPlayerController.stopCurrentPlayer();
    }

    protected abstract void toEditAnswer(long answer_id);

    protected abstract int getAudioViewLayout();

}
