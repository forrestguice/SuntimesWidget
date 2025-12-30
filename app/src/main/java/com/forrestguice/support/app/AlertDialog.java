package com.forrestguice.support.app;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;

public class AlertDialog extends android.support.v7.app.AlertDialog
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

    public static void setButton(DialogInterface dialog, int button, CharSequence text, DialogInterface.OnClickListener listener) {
        if (dialog instanceof android.support.v7.app.AlertDialog) {
            ((android.support.v7.app.AlertDialog) dialog).setButton(button, text, listener);
        }
    }

    @Nullable
    public static Button getButton(DialogInterface dialog, int button) {
        if (dialog instanceof android.support.v7.app.AlertDialog) {
            return ((android.support.v7.app.AlertDialog) dialog).getButton(button);
        } else return null;
    }
}