package com.forrestguice.support.design.app;

import android.support.v4.app.FragmentManager;

public class FragmentManagerCompat implements FragmentManagerInterface
{
    public FragmentManagerCompat(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }
    protected FragmentManager fragmentManager;

    @Override
    public FragmentManager get() {
        return fragmentManager;
    }
}