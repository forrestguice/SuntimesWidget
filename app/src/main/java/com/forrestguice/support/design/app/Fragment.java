package com.forrestguice.support.design.app;

public class Fragment extends android.support.v4.app.Fragment
        implements android.content.ComponentCallbacks, android.view.View.OnCreateContextMenuListener, android.arch.lifecycle.LifecycleOwner, android.arch.lifecycle.ViewModelStoreOwner
{
    public FragmentManagerInterface getFragmentManagerCompat() {
        return new FragmentManagerCompat(getFragmentManager());
    }

    public FragmentManagerInterface getChildFragmentManagerCompat() {
        return new FragmentManagerCompat(getChildFragmentManager());
    }
}