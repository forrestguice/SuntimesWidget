package com.forrestguice.support.design.app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;

public class AppCompatActivity extends android.support.v7.app.AppCompatActivity
{
    @Nullable
    public android.support.v7.view.ActionMode startSupportActionMode(CharSequence title, @NonNull ActionMode.Callback callback)
    {
        android.support.v7.view.ActionMode actionMode = super.startSupportActionMode(callback);
        if (actionMode != null) {
            actionMode.setTitle(title);
        }
        return actionMode;
    }
}