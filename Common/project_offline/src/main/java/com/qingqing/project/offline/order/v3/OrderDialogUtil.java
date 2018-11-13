package com.qingqing.project.offline.order.v3;

import android.app.Activity;
import android.content.DialogInterface;

import com.qingqing.base.dialog.CompDefaultDialogBuilder;
import com.qingqing.base.dialog.CompDialog;
import com.qingqing.base.dialog.component.CompDialogDefaultConsole;
import com.qingqing.base.dialog.component.CompDialogDefaultTitle;
import com.qingqing.base.dialog.component.CompDialogImageOutHeader;
import com.qingqing.base.dialog.component.CompDialogUIComponent;
import com.qingqing.project.offline.R;

/**
 * 封装下单相关dialog ui
 *
 * <P>
 * postive btn固定橙色
 * </P>
 *
 * Created by tanwei on 2017/8/29.
 */

public class OrderDialogUtil {
    
    public static CompDialog showDialog(Activity activity, String title,
            String positiveBtnText,
            final DialogInterface.OnClickListener positiveBtnListener,
            String negativeBtnText) {
        return showDialog(activity, title, null, positiveBtnText, positiveBtnListener,
                negativeBtnText);
    }
    
    public static CompDialog showDialog(Activity activity, String title, String content,
            String positiveBtnText,
            final DialogInterface.OnClickListener positiveBtnListener,
            String negativeBtnText) {
        return showDialog(activity, title, content, positiveBtnText, positiveBtnListener,
                negativeBtnText, null);
    }
    
    public static CompDialog showDialog(Activity activity, String title, String content,
            String positiveBtnText,
            final DialogInterface.OnClickListener positiveBtnListener,
            String negativeBtnText,
            final DialogInterface.OnClickListener negativeBtnListener) {
        CompDefaultDialogBuilder dialogBuilder = new CompDefaultDialogBuilder(activity)
                .setTitle(title);
        if (content == null) {
            dialogBuilder.setComponentContent(null);
        }
        else {
            dialogBuilder.setContent(content);
        }
        CompDialog dialog = dialogBuilder
                .setComponentConsole(
                        new CompDialogDefaultConsole(activity).setButtonTextColorRes(
                                DialogInterface.BUTTON_POSITIVE, R.color.accent_orange))
                .setPositiveButton(positiveBtnText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if (positiveBtnListener != null) {
                                    positiveBtnListener.onClick(dialogInterface, i);
                                }
                            }
                        })
                .setNegativeButton(negativeBtnText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (negativeBtnListener != null) {
                                    negativeBtnListener.onClick(dialog, which);
                                }
                            }
                        })
                .build();

        if (content == null) {
            CompDialogUIComponent uiComponent = dialog
                    .getUIComponent(CompDialogUIComponent.Type.TITLE);
            if (uiComponent instanceof CompDialogDefaultTitle) {
                ((CompDialogDefaultTitle) uiComponent).setHeight(activity.getResources()
                        .getDimensionPixelOffset(R.dimen.dimen_60));
            }
        }
        
        dialog.show();
        
        return dialog;
    }

    public static CompDialog showDialog(Activity activity, String content) {
        CompDefaultDialogBuilder dialogBuilder = new CompDefaultDialogBuilder(activity);
        dialogBuilder.setContent(content);
        CompDialog dialog = dialogBuilder
                .setComponentConsole(
                        new CompDialogDefaultConsole(activity).setButtonTextColorRes(
                                DialogInterface.BUTTON_POSITIVE, R.color.accent_orange))
                .setPositiveButton(R.string.got_it,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .build();
        
        dialog.show();
        
        return dialog;
    }
    
    public static CompDialog showTeacherTimeBusyDialog(Activity activity,
            final DialogInterface.OnClickListener positiveBtnListener,
            final DialogInterface.OnClickListener negativeBtnListener) {
        CompDialog dialog = new CompDefaultDialogBuilder(activity)
                .setComponentHeader(new CompDialogImageOutHeader(activity)
                        .setHeaderImage(R.drawable.icon_choosetime_prompt))
                .setTitle(R.string.text_select_time_busy_title)
                .setContent(R.string.text_select_time_busy_ind)
                .setComponentConsole(
                        new CompDialogDefaultConsole(activity).setButtonTextColorRes(
                                DialogInterface.BUTTON_POSITIVE, R.color.accent_orange))
                .setPositiveButton(R.string.text_select_time_busy,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if (positiveBtnListener != null) {
                                    positiveBtnListener.onClick(dialogInterface, i);
                                }
                            }
                        })
                .setNegativeButton(R.string.text_select_another_time_when_busy,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (negativeBtnListener != null) {
                                    negativeBtnListener.onClick(dialog, which);
                                }
                            }
                        })
                .setCancelable(false).show();
        
        return dialog;
    }
    
}
