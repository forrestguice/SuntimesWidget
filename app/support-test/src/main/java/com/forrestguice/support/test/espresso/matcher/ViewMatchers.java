package com.forrestguice.support.test.espresso.matcher;

import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static org.hamcrest.Matchers.allOf;

public class ViewMatchers
{
    public static Matcher<View> hasLinks() {
        return android.support.test.espresso.matcher.ViewMatchers.hasLinks();
    }

    public static Matcher<View> isDisplayed() {
        return android.support.test.espresso.matcher.ViewMatchers.isDisplayed();
    }

    public static Matcher<View> hasFocus() {
        return android.support.test.espresso.matcher.ViewMatchers.hasFocus();
    }

    public static Matcher<View> hasSibling(final Matcher<View> siblingMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.hasSibling(siblingMatcher);
    }

    public static Matcher<View> isEnabled() {
        return android.support.test.espresso.matcher.ViewMatchers.isEnabled();
    }

    public static Matcher<View> isClickable() {
        return android.support.test.espresso.matcher.ViewMatchers.isClickable();
    }

    public static Matcher<View> isChecked() {
        return android.support.test.espresso.matcher.ViewMatchers.isChecked();
    }

    public static Matcher<View> isNotChecked() {
        return android.support.test.espresso.matcher.ViewMatchers.isNotChecked();
    }

    public static Matcher<View> isSelected() {
        return android.support.test.espresso.matcher.ViewMatchers.isSelected();
    }

    public static Matcher<View> isRoot() {
        return android.support.test.espresso.matcher.ViewMatchers.isRoot();
    }

    public static Matcher<View> isDescendantOfA(final Matcher<View> ancestorMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA(ancestorMatcher);
    }

    public static Matcher<View> isDisplayingAtLeast(final int areaPercentage) {
        return android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(areaPercentage);
    }

    public static Matcher<View> withClassName(final Matcher<String> classNameMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withClassName(classNameMatcher);
    }

    public static Matcher<View> withParent(final Matcher<View> parentMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withParent(parentMatcher);
    }

    public static Matcher<View> withId(final int id) {
        return android.support.test.espresso.matcher.ViewMatchers.withId(id);
    }

    public static Matcher<View> withText(String text) {
        return android.support.test.espresso.matcher.ViewMatchers.withText(text);
    }
    public static Matcher<View> withText(final Matcher<String> stringMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withText(stringMatcher);
    }

    public static Matcher<View> withSpinnerText(final int resourceId) {
        return android.support.test.espresso.matcher.ViewMatchers.withSpinnerText(resourceId);
    }
    public static Matcher<View> withSpinnerText(String text) {
        return android.support.test.espresso.matcher.ViewMatchers.withSpinnerText(text);
    }
    public static Matcher<View> withSpinnerText(final Matcher<String> stringMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withSpinnerText(stringMatcher);
    }

    public static Matcher<View> withContentDescription(final int resourceId) {
        return android.support.test.espresso.matcher.ViewMatchers.withContentDescription(resourceId);
    }
    public static Matcher<View> withContentDescription(String text) {
        return android.support.test.espresso.matcher.ViewMatchers.withContentDescription(text);
    }
    public static Matcher<View> withContentDescription(
            final Matcher<? extends CharSequence> charSequenceMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withContentDescription(charSequenceMatcher);
    }

    /**
     * from https://stackoverflow.com/a/42368341
     */
    public static Matcher<View> navigationButton() {
        return allOf(
                isAssignableFrom(AppCompatImageButton.class),
                withParent(isAssignableFrom(Toolbar.class)));
    }

}