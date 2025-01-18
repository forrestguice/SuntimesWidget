package com.forrestguice.support.design.app;

import android.support.v4.app.Fragment;

public class FragmentCompat implements FragmentInterface
{
    public FragmentCompat(Fragment fragment) {
        this.fragment = fragment;
    }
    protected Fragment fragment;

    @Override
    public Fragment get() {
        return fragment;
    }

    public static FragmentInterface create(Fragment fragment) {
        return new FragmentCompat(fragment);
    }
}