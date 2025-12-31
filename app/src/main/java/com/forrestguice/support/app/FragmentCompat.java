package com.forrestguice.support.app;

import android.support.v4.app.Fragment;

public abstract class FragmentCompat
{
    public abstract Fragment getFragment();

    public static FragmentCompat from(final Fragment fragment)
    {
        return new FragmentCompat()
        {
            @Override
            public Fragment getFragment() {
                return fragment;
            }
        };
    }
}