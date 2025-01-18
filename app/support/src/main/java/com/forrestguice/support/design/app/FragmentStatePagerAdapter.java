package com.forrestguice.support.design.app;

import android.support.v4.app.FragmentManager;

import com.forrestguice.support.design.widget.BottomSheetDialogFragment;

public abstract class FragmentStatePagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter
{
    public FragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * additional constructors to help implementors avoid imports to FragmentManager
     */
    public FragmentStatePagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
    }
    public FragmentStatePagerAdapter(Fragment fragment) {
        super(fragment.getChildFragmentManager());
    }
    public FragmentStatePagerAdapter(DialogFragment fragment) {
        super(fragment.getChildFragmentManager());
    }
    public FragmentStatePagerAdapter(BottomSheetDialogFragment fragment) {
        super(fragment.getChildFragmentManager());
    }
}