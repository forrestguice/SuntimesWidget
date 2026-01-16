package com.forrestguice.support.app;

import android.app.Activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.CallSuper;
import androidx.appcompat.view.ActionMode;

import android.content.Intent;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.view.ActionModeCompat;

import java.util.HashMap;
import java.util.Map;

public class AppCompatActivity extends androidx.appcompat.app.AppCompatActivity implements OnActivityResultCompat
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

    public interface ActivityResultLauncherCompat {
        void launch(Intent intent);
        void launch(Intent intent, ActivityOptionsCompat options);
    }

    @CallSuper
    public void onActivityResultCompat(int requestCode, int resultCode, Intent data) {
        Log.d("DEBUG", "onActivityResultCompat: " + requestCode + ", result: " + resultCode);
    }

    public void startActivityForResultCompat(Intent intent, int requestCode) {
        startActivityForResultCompat(intent, requestCode, null);
    }
    public void startActivityForResultCompat(Intent intent, int requestCode, ActivityOptionsCompat options)
    {
        ActivityResultLauncherCompat launcher = launchers.get(requestCode);
        if (launcher == null) {
            Log.e("AppCompatActivity", "startActivityForResultCompat: requestCode " + requestCode + " not found! did you remember to call `registerForActivityResultCompat` first?");
            //noinspection deprecation
            startActivityForResult(intent, requestCode, (options != null ? options.toBundle() : null));
        } else launcher.launch(intent, options);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ActivityResultLauncherCompat registerForActivityResultCompat(int requestCode) {
        return registerForActivityResultCompat(requestCode, this);
    }
    public ActivityResultLauncherCompat registerForActivityResultCompat(final int requestCode, final OnActivityResultCompat onResult)
    {
        final ActivityResultLauncher<Intent> launcher = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>()
        {
            @Override
            public void onActivityResult(ActivityResult result) {
                onResult.onActivityResultCompat(requestCode, result.getResultCode(), result.getData());
            }
        });
        launchers.put(requestCode, new ActivityResultLauncherCompat()
        {
            @Override
            public void launch(Intent intent) {
                launcher.launch(intent);
            }

            @Override
            public void launch(Intent intent, ActivityOptionsCompat options) {
                launcher.launch(intent, options);
            }
        });
        return launchers.get(requestCode);
    }
    protected Map<Integer, ActivityResultLauncherCompat> launchers = new HashMap<>();

}