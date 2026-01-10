package com.forrestguice.support.app;

import android.app.Activity;
import androidx.appcompat.view.ActionMode;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.view.ActionModeCompat;

public class AppCompatActivity extends androidx.appcompat.app.AppCompatActivity
{
    @Nullable
    public static ActionModeCompat startSupportActionMode(Activity activity, @NonNull final ActionModeCompat.Callback callback)
    {
        if (activity instanceof androidx.appcompat.app.AppCompatActivity) {
            ActionMode actionMode = ((androidx.appcompat.app.AppCompatActivity) activity).startSupportActionMode(ActionModeCompat.from(callback));
            ActionModeCompat result = new ActionModeCompat(actionMode);
            callback.setActionMode(result);
            return result;
        } else {
            Log.e("AppCompatActivity", "startSupportActionMode failed! supplied activity is not an instance of android.support.v7.app.AppCompatActivity");
            return null;
        }
    }
}