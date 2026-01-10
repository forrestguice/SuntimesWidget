package com.forrestguice.support.app;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public abstract class FragmentManagerCompat
{
    public abstract FragmentManager getFragmentManager();

    public Fragment findFragmentById(int i) {
        return getFragmentManager().findFragmentById(i);
    }

    public Fragment findFragmentByTag(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }

    public static FragmentManagerCompat from(final Fragment fragment) {
        return from(fragment, false);
    }
    public static FragmentManagerCompat from(final Fragment fragment, final boolean useChildFragmentManager)
    {
        return new FragmentManagerCompat()
        {
            @Override
            public FragmentManager getFragmentManager() {
                return (useChildFragmentManager ? fragment.getChildFragmentManager() : fragment.getFragmentManager());
            }
        };
    }
    public static FragmentManagerCompat from(final FragmentActivity fragment)
    {
        return new FragmentManagerCompat()
        {
            @Override
            public FragmentManager getFragmentManager() {
                return fragment.getSupportFragmentManager();
            }
        };
    }
}