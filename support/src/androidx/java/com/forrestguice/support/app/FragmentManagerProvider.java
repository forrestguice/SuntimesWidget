package com.forrestguice.support.app;

import android.content.Context;

public interface FragmentManagerProvider
{
    Context getContext();
    FragmentManagerCompat getFragmentManagerCompat();
}