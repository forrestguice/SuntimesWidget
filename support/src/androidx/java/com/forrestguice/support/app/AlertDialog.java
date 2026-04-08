package com.forrestguice.support.app;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Button;
import android.widget.ListView;

public class AlertDialog extends androidx.appcompat.app.AlertDialog
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
        if (dialog instanceof androidx.appcompat.app.AlertDialog) {
            ((androidx.appcompat.app.AlertDialog) dialog).setButton(button, text, listener);
        }
    }

    @Nullable
    public static Button getButton(DialogInterface dialog, int button) {
        if (dialog instanceof androidx.appcompat.app.AlertDialog) {
            return ((androidx.appcompat.app.AlertDialog) dialog).getButton(button);
        } else return null;
    }

    @Nullable
    public static ListView getListView(DialogInterface dialog) {
        if (dialog instanceof androidx.appcompat.app.AlertDialog) {
            return ((androidx.appcompat.app.AlertDialog) dialog).getListView();
        } else return null;
    }
}