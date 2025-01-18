package com.forrestguice.support.design.app;

public class FragmentActivity extends android.support.v4.app.FragmentActivity
{
    public FragmentManagerInterface getSupportFragmentManagerCompat() {
        return FragmentManagerCompat.create(getSupportFragmentManager());
    }

    public static FragmentActivityInterface wrap(android.support.v4.app.FragmentActivity activity) {
        return new FragmentActivityCompat(activity);
    }

    public static class FragmentActivityCompat implements FragmentActivityInterface
    {
        public FragmentActivityCompat(android.support.v4.app.FragmentActivity activity) {
            this.activity = activity;
        }
        protected android.support.v4.app.FragmentActivity activity;

        @Override
        public android.support.v4.app.FragmentActivity getActivity() {
            return activity;
        }
    }
}