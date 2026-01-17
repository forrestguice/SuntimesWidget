package com.forrestguice.support.app;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.appcompat.view.ActionMode;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.view.ActionModeCompat;

import java.util.Map;

public class AppCompatActivity extends androidx.appcompat.app.AppCompatActivity implements OnActivityResultCompat, OnPermissionResultCompat
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

    //
    // ActivityResult
    //

    @CallSuper
    public void onActivityResultCompat(int requestCode, int resultCode, Intent data) {
        Log.d("DEBUG", "onActivityResultCompat: " + requestCode + ", result: " + resultCode);
    }

    public void startActivityForResultCompat(Intent intent, int requestCode) {
        startActivityForResultCompat(intent, requestCode, null);
    }
    public void startActivityForResultCompat(Intent intent, int requestCode, ActivityOptionsCompat options)
    {
        if (!launchers.startActivityForResultCompat(intent, requestCode, options))
        {
            Log.e("AppCompatActivity", "startActivityForResultCompat: requestCode " + requestCode + " not found! did you remember to call `registerForActivityResultCompat` first?");
            //noinspection deprecation
            startActivityForResult(intent, requestCode, (options != null ? options.toBundle() : null));
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public ActivityResultLauncherCompat registerForActivityResultCompat(int requestCode) {
        return registerForActivityResultCompat(requestCode, this);
    }
    public ActivityResultLauncherCompat registerForActivityResultCompat(final int requestCode, final OnActivityResultCompat onResult) {
        return launchers.registerForActivityResultCompat(this, requestCode, onResult);
    }
    protected ActivityResultLaunchHelper launchers = new ActivityResultLaunchHelper();


    //
    // Permission Result
    //
    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull Map<String, Boolean> results) {
        Log.d("DEBUG", "onRequestPermissionsResult: (dialog fragment) " + requestCode);
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResultCompat(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("DEBUG", "onRequestPermissionsResultCompat: (dialog fragment) " + requestCode);
    }

    public void requestPermissionsCompat(String[] permissions, int requestCode)
    {
        if (!launchers.requestPermissions(permissions, requestCode))
        {
            Log.e("DialogBase", "requestPermissionCompat: requestCode " + requestCode + " not found! did you remember to call `requestPermissionsCompat` first?");
            //noinspection deprecation
            ActivityCompat.requestPermissions(this, permissions, requestCode);    // fallback
        }
    }

    public PermissionResultLauncherCompat registerForPermissionResult(int requestCode) {
        return registerForPermissionResult(requestCode, this);
    }
    public PermissionResultLauncherCompat registerForPermissionResult(int requestCode, OnPermissionResultCompat onResult) {
        return launchers.registerForPermissionResult(this, requestCode, onResult);
    }


}