package com.qingqing.project.offline.view.city;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qingqing.base.bean.Address;
import com.qingqing.base.bean.City;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.HanziToPinyin;
import com.qingqing.base.utils.UIUtil;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.BladeView;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.base.view.PartitionAdapter;
import com.qingqing.base.view.TagTextItemView;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.project.offline.R;
import com.qingqing.qingqingbase.ui.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xiejingwen on 2016/6/22.
 * 选择城市
 */
public class BaseSelectCityFragment extends BaseFragment
        implements AdapterView.OnItemClickListener , View.OnClickListener ,
        BladeView.SectionListener {
    private String TAG = "BaseSelectCityFragment";
    private List<City> mCityList = new ArrayList<>();
    
    private TextView mLocationCityView;
    private TextView mCurrentCityView;
    
    private int mCurCityId;
    private int mLocCityId;
    
    public static final int LOCATION_LOADING = -1;
    public static final int LOCATION_FAILED = 0;
    public static final int LOCATION_SUCCESS = 1;
    
    private View mViewLastSelectedCity;
    protected LimitEditText mETSearchCity;
    protected TextView mTVSearchCancel;
    private TextView mTVPinned;
    private ListView mLVSelectCityResult;
    protected ListView mLVSearchCityResult;
    private BladeView mBladeView;
    private RelativeLayout mRLSelectCity;
    private SearchCityPartitionAdapter mSelectCityPartitionAdapter;
    private SearchCityPartitionAdapter mSearchCityPartitionAdapter;
    private SparseArray<List<City>> mCitySparseArray = new SparseArray<List<City>>();
    private SparseArray<List<City>> mDefaultList = new SparseArray<>();
    
    private LimitedTextWatcher mTextWatcher = new LimitedTextWatcher() {

        @Override
        public void afterTextChecked(Editable s) {
            if (s != null) {
                refreshSearchList(s.toString());
            }
        }
    };
        
    public interface SelectCityFragListener extends FragListener {
        void startLocation();

        void chooseCity(int cityId);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_city, container, false);
    }
    
    @Override
    public void onClick(View v) {
        if (v == mETSearchCity) {
            if (mTVSearchCancel.getVisibility() != View.VISIBLE) {
                changeShowView(true);
            }
        }
        else if (v == mTVSearchCancel) {
            changeShowView(false);
            UIUtil.hideSoftInput(this);
            mETSearchCity.setText("");
        }
    }
    
    @Override
    public void onSectionSelected(int index, int actualIndex, String section) {
        int targetIndex = mSelectCityPartitionAdapter
                .getIndexByPartitionIndex(actualIndex) + mLVSelectCityResult.getHeaderViewsCount();
        if (Build.VERSION.SDK_INT >= 11) {
            mLVSelectCityResult.smoothScrollToPositionFromTop(targetIndex,0, 0);
        }
        else {
            mLVSelectCityResult.setSelection(targetIndex);
        }
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mETSearchCity = (LimitEditText) view.findViewById(R.id.et_search_city);
        mETSearchCity.addTextChangedListener(mTextWatcher);
        mETSearchCity.setHintTextColor(getResources().getColor(R.color.gray_dark));
        mTVSearchCancel = (TextView) view.findViewById(R.id.tv_search_cancel);
        mRLSelectCity = (RelativeLayout) view.findViewById(R.id.rl_select_city);
        mTVPinned = (TextView) view.findViewById(R.id.tv_pinned);
        mETSearchCity.setOnClickListener(this);
        mTVSearchCancel.setOnClickListener(this);

        mLVSelectCityResult = (ListView) view.findViewById(R.id.lv_select_city_result);
        mLVSearchCityResult = (ListView) view.findViewById(R.id.lv_search_city_result);
        mBladeView = (BladeView) view.findViewById(R.id.blade_view);
        if (mBladeView != null) {
            mLVSelectCityResult.setOnScrollListener(mBladeView);
            mBladeView.setSectionListener(this);
        }
        addHeaderViews();
        mLVSelectCityResult.setOnItemClickListener(this);
        mLVSelectCityResult.setOnScrollListener(new PinnedListScrollHelper(mTVPinned));
        mSelectCityPartitionAdapter = new SearchCityPartitionAdapter(getActivity());
        mLVSelectCityResult.setAdapter(mSelectCityPartitionAdapter);
        setUpDefaultList();
        updatePartition("", mSelectCityPartitionAdapter, true, true);
    }
    
    private void addHeaderViews() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout headView = (LinearLayout) inflater.inflate(R.layout.select_city_head,
                null);
        TextView textView = (TextView) headView.findViewById(R.id.tv_city_hot);
        textView.setText(R.string.city_list_text);
        GridView cityGridView = (GridView) headView.findViewById(R.id.list_city_hot);
        mLocationCityView = (TextView) headView.findViewById(R.id.tv_city_locating);
        mLocationCityView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocCityId > 0) {
                    chooseCity(mLocCityId);
                }
                else {
                    if (mFragListener instanceof SelectCityFragListener) {
                        ((SelectCityFragListener) mFragListener).startLocation();
                        mLocationCityView.setText(R.string.locating_city_text);
                    }
                }
            }
        });
        
        mCurrentCityView = (TextView) headView.findViewById(R.id.tv_city_visiting);
        mCurrentCityView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getString(R.string.unknown_city_text)
                        .equals(mCurrentCityView.getText().toString())) {
                    chooseCity(mCurCityId);
                }
            }
        });
        
        cityGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i,
                    long l) {
                if (i >= 0 && i < mCityList.size()) {
                    int cityId = mCityList.get(i).id;
                    if (cityId > 0) {
                        if (mViewLastSelectedCity != null) {
                            mViewLastSelectedCity.setSelected(false);
                        }
                        View cityName = view.findViewById(R.id.tv_city_name);
                        cityName.setSelected(true);
                        mViewLastSelectedCity = cityName;
                        chooseCity(cityId);
                    }
                }
            }
        });
        
        mCityList.clear();
        mCityList.addAll(getHotCityList());
        int count = mCityList.size() % 4;
        if (count > 0) {
            for (int i = 0; i < 4 - count; i++) {
                mCityList.add(City.NONE);
            }
        }
        Logger.d(TAG, "city size is " + mCityList.size());
        CityAdapter mAdapter = new CityAdapter(getActivity(), mCityList);
        cityGridView.setAdapter(mAdapter);
        String curCityName = DefaultDataCache.INSTANCE().getCityNameById(mCurCityId);
        if (!TextUtils.isEmpty(curCityName)) {
            mCurrentCityView.setText(curCityName);
        }
        else {
            mCurrentCityView.setText(R.string.unknown_city_text);
        }
        if (mFragListener instanceof SelectCityFragListener) {
            ((SelectCityFragListener) mFragListener).startLocation();
        }
        onAddHeadView(headView);
        mLVSelectCityResult.addHeaderView(headView);
    }

    protected void onAddHeadView(View headView){

    }

    protected List<City> getHotCityList(){
        return DefaultDataCache.INSTANCE().getHotCityList();
    }

    protected void chooseCity(int cityId){
        if (mFragListener instanceof SelectCityFragListener) {
            ((SelectCityFragListener) mFragListener)
                    .chooseCity(cityId);
        }
    }
    
    private void setUpDefaultList() {
        if (mDefaultList.size() == 0) {
            List<City> cityList = DefaultDataCache.INSTANCE().getCityList();
            for (City city : cityList) {
                if (!TextUtils.isEmpty(city.name)) {
                    char firstChar = HanziToPinyin.getSortKey(city.name).toUpperCase()
                            .charAt(0);
                    if (NameComparatorUtils.isLetter(firstChar)) {
                        List<City> names = mDefaultList.get(firstChar);
                        if (names == null) {
                            names = new ArrayList<City>();
                            mDefaultList.put(firstChar, names);
                        }
                        names.add(city);
                        
                    }
                    else {
                        List<City> names = mDefaultList.get('#');
                        if (names == null) {
                            names = new ArrayList<City>();
                            mDefaultList.put(firstChar, names);
                        }
                        names.add(city);
                        
                    }
                }
            }
        }
    }
    
    private void updatePartition(String search,
            SearchCityPartitionAdapter searchCityPartitionAdapter, boolean hasHead,
            boolean updateBladeView) {
        searchCityPartitionAdapter.clearPartitions();
        mCitySparseArray.clear();
        if (search.equals("")) {
            for (int i = 0; i < mDefaultList.size(); i++) {
                ArrayList<City> cities = (ArrayList<City>) ((ArrayList) mDefaultList
                        .get(mDefaultList.keyAt(i))).clone();
                mCitySparseArray.put(mDefaultList.keyAt(i), cities);
            }
            /*
             * for (City city: DefaultDataCache.INSTANCE().getCityList()){ if
             * (!TextUtils.isEmpty(city.name)){ char firstChar =
             * HanziToPinyin.getSortKey(city.name).toUpperCase().charAt(0);
             * updateCities(firstChar,city); } }
             */
        }
        else {
            ArrayList<City> results = new ArrayList<City>();
            if (NameComparatorUtils.isLetter(search)) {
                for (City city : DefaultDataCache.INSTANCE().getCityList()) {
                    if (HanziToPinyin.getSortKey(city.name).toUpperCase()
                            .startsWith(search.toUpperCase())) {
                        results.add(city);
                    }
                }
                for (City city : results) {
                    updateCities(search.toUpperCase().charAt(0), city);
                }
            }
            else {
                for (City city : DefaultDataCache.INSTANCE().getCityList()) {
                    if (city.name.startsWith(search)) {
                        results.add(city);
                    }
                }
                for (City city : results) {
                    updateCities(HanziToPinyin.getSortKey(search).toUpperCase().charAt(0),
                            city);
                }
            }
        }
        for (int i = 0; i < mCitySparseArray.size(); i++) {
            Collections.sort(mCitySparseArray.get(mCitySparseArray.keyAt(i)),
                    new NameComparatorUtils.CityComparator());
        }
        int size = mCitySparseArray.size();
        String[] fullSectionArray = new String[size];
        boolean[] isAbsentSections = new boolean[size];
        int sectionIndex = 0;
        for (char k = 'A'; k <= 'Z'; k++) {
            List<City> cities = mCitySparseArray.get(k);
            if (cities != null) {
                PartitionAdapter.Partition partition = new PartitionAdapter.Partition<City>(
                        false, hasHead);
                partition.headInfo = String.valueOf(k);
                partition.infos = cities;
                searchCityPartitionAdapter.addPartition(partition);
                isAbsentSections[sectionIndex] = true;
                fullSectionArray[sectionIndex] = String.valueOf(k);
                sectionIndex++;
            }
        }
        if (mCitySparseArray.get('#') != null) {
            PartitionAdapter.Partition partition = new PartitionAdapter.Partition<City>(
                    false, hasHead);
            partition.headInfo = "#";
            partition.infos = mCitySparseArray.get('#');
            searchCityPartitionAdapter.addPartition(partition);
            isAbsentSections[sectionIndex] = true;
            fullSectionArray[sectionIndex] = String.valueOf('#');
        }
        if (updateBladeView && mBladeView != null) {
            mBladeView.setSections(fullSectionArray);
            mBladeView.setAbsentSections(isAbsentSections);
            mBladeView.setCurrentSectionIndex(mBladeView.getSectionIndex(0));
        }
        searchCityPartitionAdapter.notifyDataSetChanged();
    }
    
    protected void updateCities(char firstChar, City city) {
        if (NameComparatorUtils.isLetter(firstChar)) {
            List<City> names = mCitySparseArray.get(firstChar);
            if (names == null) {
                names = new ArrayList<City>();
                mCitySparseArray.put(firstChar, names);
            }
            names.add(city);
        }
        else {
            List<City> names = mCitySparseArray.get('#');
            if (names == null) {
                names = new ArrayList<City>();
                mCitySparseArray.put(firstChar, names);
            }
            names.add(city);
        }
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            setTitle(R.string.select_city_title);
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object cityItem = null;
        if (mLVSearchCityResult.getVisibility() == View.VISIBLE) {
            cityItem = mSearchCityPartitionAdapter.getItem(position);
        }
        else {
            cityItem = mSelectCityPartitionAdapter
                    .getItem(position - mLVSelectCityResult.getHeaderViewsCount());
        }
        if (cityItem instanceof City) {
            chooseCity(((City) cityItem).id);
        }
    }
    
    protected void changeShowView(boolean showSearchList) {
        if (showSearchList) {
            mTVSearchCancel.setVisibility(View.VISIBLE);
            mLVSearchCityResult.setVisibility(View.VISIBLE);
            mRLSelectCity.setVisibility(View.GONE);
        }
        else {
            mTVSearchCancel.setVisibility(View.GONE);
            mLVSearchCityResult.setVisibility(View.INVISIBLE);
            mRLSelectCity.setVisibility(View.VISIBLE);
        }
    }
    
    private void refreshSearchList(String searchKey) {
        if (mSearchCityPartitionAdapter == null) {
            mSearchCityPartitionAdapter = new SearchCityPartitionAdapter(getContext());
            mSearchCityPartitionAdapter.setShowHead(false);
            mLVSearchCityResult.setAdapter(mSearchCityPartitionAdapter);
            mLVSearchCityResult.setOnItemClickListener(this);
        }
        if (TextUtils.isEmpty(searchKey)) {
            mSearchCityPartitionAdapter.clearPartitions();
            mSearchCityPartitionAdapter.notifyDataSetChanged();
        }
        else {
            updatePartition(searchKey, mSearchCityPartitionAdapter, false, false);
        }
    }
    
    public void setCurrent(int cityId) {
        mCurCityId = cityId;
    }
    
    public void setLocation(@NonNull int location, @Nullable int cityId) {
        switch (location) {
            case LOCATION_FAILED:
                mLocationCityView.setText(R.string.location_city_error_text);
                break;
            case LOCATION_LOADING:
                mLocationCityView.setText(R.string.locating_city_text);
                break;
            case LOCATION_SUCCESS:
                mLocCityId = cityId;
                String cityName = DefaultDataCache.INSTANCE().getCityNameById(mLocCityId);
                if (mLocCityId > 0 && mCurCityId <= 0 && !TextUtils.isEmpty(cityName)) {
                    mCurCityId = mLocCityId;
                    mCurrentCityView.setText(cityName);
                }
                mLocationCityView.setText(TextUtils.isEmpty(cityName)
                        ? Address.getLocation().city.name : cityName);
                break;
        }
    }
    
    class CityAdapter extends BaseAdapter<City> {
        
        public CityAdapter(Context context, List<City> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_city_grid, parent,
                    false);
        }
        
        @Override
        public BaseAdapter.ViewHolder<City> createViewHolder() {
            return new CityHolder();
        }
    }
    
    class CityHolder extends BaseAdapter.ViewHolder<City> {
        
        private TextView cityName;
        
        @Override
        public void init(Context context, View convertView) {
            convertView.setBackground(new StateListDrawable());
            cityName = (TextView) convertView.findViewById(R.id.tv_city_name);
        }
        
        @Override
        public void update(Context context, City data) {
            
            if (data == City.NONE) {
                cityName.setVisibility(View.INVISIBLE);
            }
            else {
                cityName.setVisibility(View.VISIBLE);
                cityName.setText(data.name);
                if (cityName instanceof TagTextItemView) {
                    ((TagTextItemView) cityName)
                            .setBgNormalColor(getResources().getColor(R.color.white));
                }
                // cityName.setSelected(data.id == mCurCityId && mCurCityId >
                // 0);
                // if (cityName.isSelected()) {
                // mViewLastSelectedCity = cityName;
                // }
            }
        }
    }
    
}
