package com.qingqing.base.im.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.fragment.PtrListFragment;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.ContactModel;
import com.qingqing.base.im.DataRefreshListener;
import com.qingqing.base.im.DataSyncListener;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.im.utils.ContactUtils;
import com.qingqing.base.utils.HanziToPinyin;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BladeView;
import com.qingqing.base.view.PartitionAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 通用联系人列表fragment
 *
 * Created by huangming on 2015/12/22.
 */
public class ContactPartitionListFragment extends PtrListFragment
        implements DataSyncListener , AdapterView.OnItemClickListener ,
        DataRefreshListener , BladeView.SectionListener {
    
    private static final String TAG = "ContactPartitionListFragment";
    
    protected SparseArray<List<ContactInfo>> mCharSparseArray = new SparseArray<List<ContactInfo>>();
    protected BladeView mBladeView;
    protected boolean mHeadIconRoundRectStyle = false;
    protected ContactModel mModel;
    protected BaseAdapter mAdapter;
    protected ListView mListView;
    protected List<ContactInfo> mContactList = new ArrayList<>();
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mModel = ChatManager.getInstance().getChatModel().getContactModel();
        mModel.addSyncListener(this);
        final Activity activity = getActivity();
        
        mListView = createListView(view);
        mListView.setOnItemClickListener(this);
        
        mBladeView = (BladeView) view.findViewById(R.id.blade_view);
        if (mListView != null && mBladeView != null) {
            mListView.setOnScrollListener(mBladeView);
            mBladeView.setSectionListener(this);
        }
        
        mAdapter = createAdapter(activity);
        initListView(view);
    }
    
    protected void initListView(View view) {
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(view.findViewById(R.id.view_empty));
    }
    
    protected ListView createListView(View view) {
        return (mPtrListView != null) ? mPtrListView
                : (ListView) view.findViewById(R.id.list_contact);
    }
    
    protected BaseAdapter createAdapter(Context context) {
        return new ContactPartitionAdapter(context);
    }
    
    protected void update() {
        
    }
    
    protected ContactPartitionAdapter getAdapter() {
        return ((ContactPartitionAdapter) mAdapter);
    }
    
    protected void updateView() {
        getAdapter().clearPartitions();
        mCharSparseArray.clear();
        Collections.sort(mContactList, new ContactUtils.ContactComparator());
        for (ContactInfo contactInfo : mContactList) {
            String name = ContactUtils.getContactName(contactInfo);
            if (!TextUtils.isEmpty(name)) {
                String sortKey = HanziToPinyin.getSortKey(name).toUpperCase();
                char firstChar = TextUtils.isEmpty(sortKey) ? '#' : sortKey.charAt(0);
                if (ContactUtils.isLetter(firstChar)) {
                    List<ContactInfo> contactInfoList = mCharSparseArray.get(firstChar);
                    if (contactInfoList == null) {
                        contactInfoList = new ArrayList<ContactInfo>();
                        mCharSparseArray.put(firstChar, contactInfoList);
                    }
                    contactInfoList.add(contactInfo);
                }
                else {
                    List<ContactInfo> contactInfoList = mCharSparseArray.get('#');
                    if (contactInfoList == null) {
                        contactInfoList = new ArrayList<ContactInfo>();
                        mCharSparseArray.put('#', contactInfoList);
                    }
                    contactInfoList.add(contactInfo);
                }
            }
        }
        int size = mCharSparseArray.size();
        String[] fullSectionArray = new String[size];
        boolean[] isAbsentSections = new boolean[size];
        int sectionIndex = 0;
        for (char k = 'A'; k <= 'Z'; k++) {
            List<ContactInfo> contactInfoList = mCharSparseArray.get(k);
            if (contactInfoList != null) {
                PartitionAdapter.Partition partition = new PartitionAdapter.Partition<String>(
                        false, true);
                partition.headInfo = String.valueOf(k);
                partition.infos = contactInfoList;
                getAdapter().addPartition(partition);
                isAbsentSections[sectionIndex] = true;
                fullSectionArray[sectionIndex] = String.valueOf(k);
                sectionIndex++;
            }
        }
        if (mCharSparseArray.get('#') != null) {
            PartitionAdapter.Partition partition = new PartitionAdapter.Partition<String>(
                    false, true);
            partition.headInfo = String.valueOf('#');
            partition.infos = mCharSparseArray.get('#');
            getAdapter().addPartition(partition);
            isAbsentSections[sectionIndex] = true;
            fullSectionArray[sectionIndex] = String.valueOf('#');
        }
        if (mBladeView != null) {
            mBladeView.setSections(fullSectionArray);
            mBladeView.setAbsentSections(isAbsentSections);
            mBladeView.setCurrentSectionIndex(mBladeView.getSectionIndex(0));
        }
        getAdapter().notifyDataSetChanged();
    }
    
    protected void asyncFetchDataIfNeeded() {
        if (mModel != null && !mModel.isSynced()) {
            mModel.asyncFetchData();
        }
    }
    
    protected void asyncFetchData() {
        if (mModel != null) {
            mModel.asyncFetchData();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mModel.removeSyncListener(this);
        mModel = null;
    }
    
    @Override
    public void onSyncComplete(boolean success) {
        if (success) {
            update();
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
    }
    
    @Override
    public void onRefresh() {
        update();
    }
    
    @Override
    public void onSectionSelected(int index, int actualIndex, String section) {
        int targetIndex = getAdapter().getIndexByPartitionIndex(actualIndex);
        mListView.smoothScrollToPositionFromTop(targetIndex, 0, 0);
    }
    
    protected class ContactPartitionAdapter extends PartitionAdapter<ContactInfo> {
        
        protected TextView nameTv;
        protected TextView headTv;
        
        public ContactPartitionAdapter(Context context) {
            super(context);
        }
        
        @Override
        protected View newView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_partition_contact,
                    parent, false);
        }
        
        @Override
        protected void bindView(View v, int partition,
                final List<ContactInfo> contactInfoList, Object headInfo, int offset,
                boolean isHead) {
            final ContactInfo contactInfo = !isHead && contactInfoList != null
                    && offset >= 0 && offset < contactInfoList.size()
                            ? contactInfoList.get(offset)
                            : null;
            final int position = getPosByPartitionAndOffset(partition, offset);
            updateView(v, contactInfo, headInfo, position, isHead);
            
        }
        
        protected void updateView(View v, ContactInfo contactInfo, Object headInfo,
                int position, boolean isHead) {
            v.findViewById(R.id.item_partition_content)
                    .setVisibility(isHead ? View.GONE : View.VISIBLE);
            v.findViewById(R.id.item_partition_head)
                    .setVisibility(isHead && headInfo != null ? View.VISIBLE : View.GONE);
            
            final AsyncImageViewV2 avatarImg = (AsyncImageViewV2) v
                    .findViewById(R.id.img_avatar);
            if (mHeadIconRoundRectStyle)
                avatarImg.setShowRoundRectStyle();
            nameTv = (TextView) v.findViewById(R.id.tv_name);
            headTv = (TextView) v.findViewById(R.id.tv_head);
            
            if (isHead) {
                headTv.setText((String) headInfo);
            }
            else {
                if (contactInfo != null) {
                    avatarImg.setImageUrl(
                            ImageUrlUtil.getHeadImg(contactInfo.getAvatar()),
                            LogicConfig.getDefaultHeadIcon(contactInfo.getSex()));
                    nameTv.setText(ContactUtils.getContactName(contactInfo));
                }
            }
        }
    }
}
