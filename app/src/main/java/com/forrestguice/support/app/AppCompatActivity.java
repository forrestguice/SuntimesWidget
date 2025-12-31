package com.forrestguice.support.app;

import android.app.Activity;
import android.support.v7.view.ActionMode;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.view.ActionModeCompat;

public class AppCompatActivity extends android.support.v7.app.AppCompatActivity
{
    @Nullable
    public static ActionModeCompat startSupportActionMode(Activity activity, @NonNull final ActionModeCompat.Callback callback)
    {
        if (activity instanceof android.support.v7.app.AppCompatActivity) {
            ActionMode actionMode = ((android.support.v7.app.AppCompatActivity) activity).startSupportActionMode(ActionModeCompat.from(callback));
            ActionModeCompat result = new ActionModeCompat(actionMode);
            callback.setActionMode(result);
            return result;
        } else return null;
    }
}