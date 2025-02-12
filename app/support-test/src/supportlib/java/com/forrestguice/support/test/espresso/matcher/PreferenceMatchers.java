package com.forrestguice.support.test.espresso.matcher;

import android.preference.Preference;

import org.hamcrest.Matcher;

public class PreferenceMatchers
{
    public static Matcher<Preference> withSummary(final int resourceId) {
        return android.support.test.espresso.matcher.PreferenceMatchers.withSummary(resourceId);
    }

    public static Matcher<Preference> withSummaryText(String summary) {
        return android.support.test.espresso.matcher.PreferenceMatchers.withSummaryText(summary);
    }
    public static Matcher<Preference> withSummaryText(final Matcher<String> summaryMatcher) {
        return android.support.test.espresso.matcher.PreferenceMatchers.withSummaryText(summaryMatcher);
    }

    public static Matcher<Preference> withTitle(final int resourceId) {
        return android.support.test.espresso.matcher.PreferenceMatchers.withTitle(resourceId);
    }

    public static Matcher<Preference> withTitleText(String title) {
        return android.support.test.espresso.matcher.PreferenceMatchers.withTitleText(title);
    }
    public static Matcher<Preference> withTitleText(final Matcher<String> titleMatcher) {
        return android.support.test.espresso.matcher.PreferenceMatchers.withTitleText(titleMatcher);
    }

    public static Matcher<Preference> isEnabled() {
        return android.support.test.espresso.matcher.PreferenceMatchers.isEnabled();
    }

    public static Matcher<Preference> withKey(String key) {
        return android.support.test.espresso.matcher.PreferenceMatchers.withKey(key);
    }
    public static Matcher<Preference> withKey(final Matcher<String> keyMatcher) {
        return android.support.test.espresso.matcher.PreferenceMatchers.withKey(keyMatcher);
    }

}