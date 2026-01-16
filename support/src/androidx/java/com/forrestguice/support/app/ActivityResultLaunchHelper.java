package com.forrestguice.support.app;

import android.content.Intent;
import android.util.Log;

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

}