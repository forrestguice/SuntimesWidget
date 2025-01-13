package com.forrestguice.support.design.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
}