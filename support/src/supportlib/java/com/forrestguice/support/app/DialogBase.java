/**
    Copyright (C) 2025 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.forrestguice.support.app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.forrestguice.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public abstract class DialogBase extends DialogFragment implements OnActivityResultCompat
{
    public DialogBase() {
        setArguments(new Bundle());
    }

    @NonNull
    public Bundle getArgs()
    {
        Bundle args = getArguments();
        Bundle retValue = args;
        if (args == null) {
            setArguments(retValue = new Bundle());
        }
        return retValue;
    }

    public static int getTouchOutsideResourceID() {
        return android.support.design.R.id.touch_outside;    // support libraries
        //return com.google.android.material.R.id.touch_outside;   // androidx
    }

    public static void disableTouchOutsideBehavior(Dialog dialog)
    {
        Window window = (dialog != null ? dialog.getWindow() : null);
        if (window != null) {
            View decorView = window.getDecorView().findViewById(getTouchOutsideResourceID());
            decorView.setOnClickListener(null);
        }
    }

    @NonNull
    public FragmentManager getParentFragmentManager()
    {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null) {
            throw new IllegalStateException("fragment manager is null! did you remember to call isAdded first?");
        }
        return fragmentManager;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
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
                startActivityForResult(intent, requestCode, options.toBundle());
            }
        });
        return launchers.get(requestCode);
    }
    protected Map<Integer, ActivityResultLauncherCompat> launchers = new HashMap<>();
    protected Map<Integer, OnActivityResultCompat> results = new HashMap<>();
}