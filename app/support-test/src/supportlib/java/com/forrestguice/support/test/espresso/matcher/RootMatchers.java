package com.forrestguice.support.test.espresso.matcher;

import android.support.test.espresso.Root;
import android.view.View;

import org.hamcrest.Matcher;

public class RootMatchers
{
    public static Matcher<Root> isFocusable() {
        return android.support.test.espresso.matcher.RootMatchers.isFocusable();
    }

    public static Matcher<Root> isTouchable() {
        return android.support.test.espresso.matcher.RootMatchers.isTouchable();
    }

    public static Matcher<Root> isDialog() {
        return android.support.test.espresso.matcher.RootMatchers.isDialog();
    }

    public static Matcher<Root> isPlatformPopup() {
        return android.support.test.espresso.matcher.RootMatchers.isPlatformPopup();
    }

    public static Matcher<Root> withDecorView(final Matcher<View> decorViewMatcher) {
        return android.support.test.espresso.matcher.RootMatchers.withDecorView(decorViewMatcher);
    }
}