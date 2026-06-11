package com.forrestguice.support.app;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public abstract class FragmentManagerCompat
{
    @Nullable
    public abstract FragmentManager getFragmentManager();

    @Nullable
    public Fragment findFragmentById(int i) {
        return (getFragmentManager() != null ? getFragmentManager().findFragmentById(i) : null);
    }

    @Nullable
    public Fragment findFragmentByTag(String tag) {
        return (getFragmentManager() != null ? getFragmentManager().findFragmentByTag(tag) : null);
    }

    public static FragmentManagerCompat from(final Fragment fragment) {
        return from(fragment, false);
    }
    public static FragmentManagerCompat from(final Fragment fragment, final boolean useChildFragmentManager)
    {
        return new FragmentManagerCompat()
        {
            @Nullable
            @Override
            public FragmentManager getFragmentManager() {
                return (useChildFragmentManager ? fragment.getChildFragmentManager()
                        : fragment.isAdded() ? fragment.getParentFragmentManager() : null);
            }
        };
    }
    public static FragmentManagerCompat from(final FragmentActivity fragment)
    {
        return new FragmentManagerCompat()
        {
            @Nullable
            @Override
            public FragmentManager getFragmentManager() {
                return fragment.getSupportFragmentManager();
            }
        };
    }
}