package com.qingqing.project.offline.order.v3;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.dialog.CompDialog;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.view.recycler.VerticalDividerDecoration;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.TimeSlice;
import com.qingqing.qingqingbase.ui.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * 展示选择的时间界面
 *
 * Created by tanwei on 2017/8/7.
 */

public class SelectTimeResultFragment extends BaseFragment {
    
    public static final String TAG = "timeResult";
    
    private static final int REQUEST_CODE_SEL_TIME = 1;
    
    private SelectTimeParamsV3 mParams;
    
    private TimeAdapter mAdapter;
    
    private int clickIndex;

    private CompDialog clearConfirmDialog;

    private TimeComparator timeComparator;
    
    private Button btnConfirm;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            mParams = getArguments()
                    .getParcelable(BaseParamKeys.PARAM_PARCEL_SELECT_TIME);
        }
        else {
            getActivity().finish();
        }
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_select_time_result, container,
                false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setTitle(R.string.text_confirm_course_time);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.text_confirm_course_time);

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.fragment_order_select_time_result_btn_commit) {
                    if (mFragListener instanceof SelectTimeFragListener) {
                        ((SelectTimeFragListener) mFragListener).done(mParams);
                    }
                }
            }
        };

        btnConfirm = (Button) view
                .findViewById(R.id.fragment_order_select_time_result_btn_commit);
        String text = getString(R.string.text_submit)
                + getString(R.string.text_format_total_time, mParams.getCount());
        btnConfirm.setText(text);
        btnConfirm.setOnClickListener(clickListener);
        
        RecyclerView timeRecyclerView = (RecyclerView) view
                .findViewById(R.id.fragment_order_select_time_result_list);
        mAdapter = new TimeAdapter(getActivity(), mParams.getTimeList());
        mAdapter.setParams(mParams);
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        timeRecyclerView.setAdapter(mAdapter);
        timeRecyclerView.addItemDecoration(new VerticalDividerDecoration(getActivity()));
        
        mAdapter.setItemClickListener(new CircleTimeAdapter.ItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh, int position) {
                clickIndex = position;
                // 保存选中的时间，设置到当前时间，并删除原列表的时间
                TimeSlice timeSlice = mParams.getTimeList().remove(clickIndex);
                mParams.setSelectedTime(timeSlice);

                Intent intent = new Intent(getActivity(),
                        InternalSelectTimeActivity.class);
                intent.putExtra(BaseParamKeys.PARAM_PARCEL_SELECT_TIME, mParams);
                mParams.setSelectDateTime(timeSlice.getStartDate().getTime());
                mParams.setDatePosition(clickIndex + 1);
                startActivityForResult(intent, REQUEST_CODE_SEL_TIME);
            }
        });

        // 6.1添加限制时间检查
        checkTimeIfLimit(true);
    }

    private void checkTimeIfLimit(boolean showTip) {
        if (mParams.isTimeLimited()) {
            
            int errorCount = mParams.checkTimeLimited();
            btnConfirm.setEnabled(errorCount == 0);
            
            if (showTip && errorCount > 0) {
                String message = getString(R.string.text_winter_pack_time_error_count,
                        errorCount,
                        DateUtils.mYearAndMonthAndDateFormat
                                .format(new Date(mParams.getLimitTimeStart())),
                        DateUtils.mYearAndMonthAndDateFormat
                                .format(new Date(mParams.getLimitTimeEnd())));
                OrderDialogUtil.showDialog(getActivity(), message);
            }
        }
    }

    private void showClearConfirmDialog() {
        
        if (clearConfirmDialog == null) {
            
            clearConfirmDialog = OrderDialogUtil.showDialog(getActivity(),
                    getString(R.string.text_dialog_reset_confirm_title),
                    getString(R.string.text_dialog_reset_confirm_content),
                    getString(R.string.text_sel_time_reset),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mParams.getTimeList().clear();
                            getActivity().onBackPressed();
                        }
                    }, getString(R.string.cancel));
        }
        else {
            clearConfirmDialog.show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_clear_time, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_clear_time) {
            showClearConfirmDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        TimeSlice timeSlice = null;
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SEL_TIME
                && data != null) {
            ArrayList<TimeSlice> list = data.getParcelableArrayListExtra(
                    BaseParamKeys.PARAM_ARRAY_LIST_SELECTED_TIME);
            
            if (list != null && list.size() > 0) {
                
                timeSlice = list.get(0);
            }
            else {
                Logger.w(TAG, "sel time result error");
            }
        }

        // 恢复之前保存的时间
        if (timeSlice == null) {
            timeSlice = mParams.getSelectedTime();
        }
        
        mParams.getTimeList().add(timeSlice);
        
        if (timeComparator == null) {
            timeComparator = new TimeComparator();
        }
        Collections.sort(mParams.getTimeList(), timeComparator);
        
        mAdapter.notifyDataSetChanged();

        // 6.1添加限制时间检查
        checkTimeIfLimit(false);
    }
}
