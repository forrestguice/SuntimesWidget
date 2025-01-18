package com.forrestguice.support.test.espresso.assertion;

import android.support.test.espresso.ViewAssertion;
import android.view.View;

import org.hamcrest.Matcher;

public class ViewAssertions
{
    public static ViewAssertion doesNotExist() {
        return android.support.test.espresso.assertion.ViewAssertions.doesNotExist();
    }

    public static ViewAssertion matches(final Matcher<? super View> viewMatcher) {
        return android.support.test.espresso.assertion.ViewAssertions.matches(viewMatcher);
    }

    public static ViewAssertion selectedDescendantsMatch(final Matcher<View> selector, final Matcher<View> matcher) {
        return android.support.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch(selector, matcher);
    }
}