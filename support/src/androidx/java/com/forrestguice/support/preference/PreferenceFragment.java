package com.forrestguice.support.preference;

import android.os.Bundle;

import com.forrestguice.annotation.Nullable;

public abstract class PreferenceFragment extends android.preference.PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        onCreatePreferences(savedInstanceState, null);
    }

    //@Override
    public abstract void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey);

}
