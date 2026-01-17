package com.forrestguice.support.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class ActivityResultLaunchHelper
{
    public void startActivityForResultCompat(Intent intent, int requestCode) {
        startActivityForResultCompat(intent, requestCode, null);
    }
    public boolean startActivityForResultCompat(Intent intent, int requestCode, ActivityOptionsCompat options)
    {
        ActivityResultLauncherCompat launcher = launchers.get(requestCode);
        if (launcher == null) {
            Log.e("AppCompatActivity", "startActivityForResultCompat: requestCode " + requestCode + " not found! did you remember to call `registerForActivityResultCompat` first?");
            return false;
        }
        launcher.launch(intent, options);
        return true;
    }

    public boolean requestPermissions(String[] permissions, int requestCode) {
        PermissionResultLauncherCompat launcher = permissionRequests.get(requestCode);
        if (launcher == null) {
            Log.e("AppCompatActivity", "requestPermissions: requestCode " + requestCode + " not found! did you remember to call `registerForPermissionResult` first?");
            return false;
        }
        launcher.requestPermissions(permissions);
        return true;
    }

    /**
     * registerForActivityResultCompat
     */
    public ActivityResultLauncherCompat registerForActivityResultCompat(ActivityResultCaller caller, final int requestCode, final OnActivityResultCompat onResult)
    {
        final ActivityResultLauncher<Intent> launcher = caller.registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>()
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
    public ActivityResultLauncherCompat getLauncher(int requestCode) {
        return launchers.get(requestCode);
    }

    /**
     * registerForPermissionResult
     */
    public PermissionResultLauncherCompat registerForPermissionResult(ActivityResultCaller caller, int requestCode, OnPermissionResultCompat onResult)
    {
        ActivityResultLauncher<String[]> launcher = caller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>()
        {
            @Override
            public void onActivityResult(Map<String, Boolean> results) {
                Pair<String[], int[]> r = permissionResultMapToArrays(results);
                onResult.onRequestPermissionsResultCompat(requestCode, r.first, r.second);    // old signature
                onResult.onRequestPermissionsResult(requestCode, results);                    // new signature
            }
        });
        permissionRequests.put(requestCode, new PermissionResultLauncherCompat() {
            @Override
            public void requestPermissions(String[] permissions) {
                launcher.launch(permissions);
            }
        });
        return permissionRequests.get(requestCode);
    }
    protected Map<Integer, PermissionResultLauncherCompat> permissionRequests = new HashMap<>();
    public PermissionResultLauncherCompat getPermissionRequest(int requestCode) {
        return permissionRequests.get(requestCode);
    }

    protected static Pair<String[], int[]> permissionResultMapToArrays(Map<String, Boolean> map)
    {
        String[] permissions = map.keySet().toArray(new String[0]);
        int[] grantResults = new int[permissions.length];

        for (int i=0; i<permissions.length; i++) {
            Boolean b = map.get(permissions[i]);
            grantResults[i] = ((b != null && b) ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED);
        }

        return new Pair<>(permissions, grantResults);
    }

}