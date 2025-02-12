package com.forrestguice.support.design.app;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

public class AlertDialog extends android.support.v7.app.AlertDialog implements android.content.DialogInterface
{
    protected AlertDialog(@NonNull Context context) {
        super(context);
    }

    protected AlertDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AlertDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static void setButton(DialogInterface dialog, int whichButton, CharSequence text, DialogInterface.OnClickListener listener)
    {
        android.support.v7.app.AlertDialog d = getAlertDialog(dialog);
        if (d != null) {
            d.setButton(whichButton, text, listener);
        } else Log.e("AlertDialog", "failed to setButton!");
    }

    @Nullable
    public static Button getButton(DialogInterface dialog, int whichButton)
    {
        android.support.v7.app.AlertDialog d = getAlertDialog(dialog);
        return (d != null ? d.getButton(whichButton) : null);
    }

    @Nullable
    public static ListView getListView(DialogInterface dialog)
    {
        android.support.v7.app.AlertDialog d = getAlertDialog(dialog);
        return (d != null ? d.getListView() : null);
    }

    @Nullable
    public static android.support.v7.app.AlertDialog getAlertDialog(DialogInterface dialog)
    {
        try {
            return (android.support.v7.app.AlertDialog) dialog;
        } catch (Exception e) {
            Log.e("AlertDialog", "failed to getAlertDialog: " + e);
            return null;
        }
    }

}