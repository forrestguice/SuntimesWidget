package com.forrestguice.support.app;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.view.ActionMode;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.view.ActionModeCompat;

import java.util.HashMap;
import java.util.Map;

public class AppCompatActivity extends android.support.v7.app.AppCompatActivity implements OnActivityResultCompat, OnPermissionResultCompat
{
    @Nullable
    public static ActionModeCompat startSupportActionMode(Activity activity, @NonNull final ActionModeCompat.Callback callback)
    {
        if (activity instanceof android.support.v7.app.AppCompatActivity) {
            ActionMode actionMode = ((android.support.v7.app.AppCompatActivity) activity).startSupportActionMode(ActionModeCompat.from(callback));
            ActionModeCompat result = new ActionModeCompat(actionMode);
            callback.setActionMode(result);
            return result;
        } else {
            Log.e("AppCompatActivity", "startSupportActionMode failed! supplied activity is not an instance of android.support.v7.app.AppCompatActivity");
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        OnActivityResultCompat onResult = results.get(requestCode);
        if (onResult != null) {
            onResult.onActivityResultCompat(requestCode, resultCode, data);
        }
    }
    public void onActivityResultCompat(int requestCode, int resultCode, Intent data) {}

    public void startActivityForResultCompat(Intent intent, int requestCode) {
        startActivityForResultCompat(intent, requestCode, null);
    }
    public void startActivityForResultCompat(Intent intent, int requestCode, ActivityOptionsCompat options)
    {
        ActivityResultLauncherCompat launcher = launchers.get(requestCode);
        if (launcher == null) {
            Log.e("AppCompatActivity", "startActivityForResultCompat: requestCode " + requestCode + " not found! did you remember to call `registerForActivityResultCompat` first?");
            startActivityForResult(intent, requestCode, (options != null ? options.toBundle() : null));
        } else launcher.launch(intent, options);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ActivityResultLauncherCompat registerForActivityResultCompat(int requestCode) {
        return registerForActivityResultCompat(requestCode, this);
    }
    public ActivityResultLauncherCompat registerForActivityResultCompat(final int requestCode, OnActivityResultCompat onResult)
    {
        results.put(requestCode, onResult);
        launchers.put(requestCode, new ActivityResultLauncherCompat()
        {
            @Override
            public void launch(Intent intent) {
                startActivityForResult(intent, requestCode);
            }

            @Override
            public void launch(Intent intent, ActivityOptionsCompat options) {
                startActivityForResult(intent, requestCode, (options != null ? options.toBundle() : null));
            }
        });
        return launchers.get(requestCode);
    }
    protected Map<Integer, ActivityResultLauncherCompat> launchers = new HashMap<>();
    protected Map<Integer, OnActivityResultCompat> results = new HashMap<>();

    //
    // PermissionRequest
    //

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResultCompat(requestCode, permissions, grantResults);
        onRequestPermissionsResult(requestCode, DialogBase.permissionResultsToMap(permissions, grantResults));
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull Map<String, Boolean> results) {}

    @CallSuper
    @Override
    public void onRequestPermissionsResultCompat(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {}

    public void requestPermissionsCompat(String[] permissions, int requestCode)
    {
        PermissionResultLauncherCompat launcher = permissionRequests.get(requestCode);
        if (launcher == null) {
            Log.e("AppCompatActivity", "requestPermissionsCompat: requestCode " + requestCode + " not found!");
            ActivityCompat.requestPermissions(AppCompatActivity.this, permissions, requestCode);
        } else launcher.requestPermissions(permissions);
    }

    public PermissionResultLauncherCompat registerForPermissionResult(int requestCode) {
        return registerForPermissionResult(requestCode, this);
    }
    public PermissionResultLauncherCompat registerForPermissionResult(int requestCode, OnPermissionResultCompat onResult)
    {
        permissionResults.put(requestCode, onResult);
        permissionRequests.put(requestCode, new PermissionResultLauncherCompat() {
            @Override
            public void requestPermissions(String[] permissions) {
                ActivityCompat.requestPermissions(AppCompatActivity.this, permissions, requestCode);
            }
        });
        return permissionRequests.get(requestCode);
    }
    protected Map<Integer, PermissionResultLauncherCompat> permissionRequests = new HashMap<>();
    protected Map<Integer, OnPermissionResultCompat> permissionResults = new HashMap<>();

}