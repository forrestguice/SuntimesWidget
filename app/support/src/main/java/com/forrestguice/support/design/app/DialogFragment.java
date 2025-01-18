package com.forrestguice.support.design.app;

public class DialogFragment extends android.support.v4.app.DialogFragment
        implements android.content.DialogInterface.OnCancelListener, android.content.DialogInterface.OnDismissListener
{
    public FragmentManagerInterface getFragmentManagerCompat() {
        return FragmentManagerCompat.create(getFragmentManager());
    }

    public FragmentManagerInterface getChildFragmentManagerCompat() {
        return FragmentManagerCompat.create(getChildFragmentManager());
    }
}