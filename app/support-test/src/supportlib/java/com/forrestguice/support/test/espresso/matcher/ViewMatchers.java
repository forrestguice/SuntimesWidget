package com.forrestguice.support.test.espresso.matcher;

import android.view.View;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.is;

public class ViewMatchers
{
    public static Matcher<View> withEffectiveVisibility(final android.support.test.espresso.matcher.ViewMatchers.Visibility visibility) {
        return android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility(visibility);
    }

    public static Matcher<View> withAlpha(final float alpha) {
        return android.support.test.espresso.matcher.ViewMatchers.withAlpha(alpha);
    }

    public static Matcher<View> withParent(final Matcher<View> parentMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withParent(parentMatcher);
    }

    public static Matcher<View> withChild(final Matcher<View> childMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withChild(childMatcher);
    }

    public static Matcher<View> hasChildCount(final int childCount) {
        return android.support.test.espresso.matcher.ViewMatchers.hasChildCount(childCount);
    }

    public static Matcher<View> hasMinimumChildCount(final int minChildCount) {
        return android.support.test.espresso.matcher.ViewMatchers.hasMinimumChildCount(minChildCount);
    }

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

    public static Matcher<View> hasDescendant(final Matcher<View> descendantMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.hasDescendant(descendantMatcher);
    }

    public static Matcher<View> hasBackground(final int drawableId) {
        return android.support.test.espresso.matcher.ViewMatchers.hasBackground(drawableId);
    }

    public static Matcher<View> hasTextColor(final int colorResId) {
        return android.support.test.espresso.matcher.ViewMatchers.hasTextColor(colorResId);
    }

    public static Matcher<View> isEnabled() {
        return android.support.test.espresso.matcher.ViewMatchers.isEnabled();
    }

    public static Matcher<View> isClickable() {
        return android.support.test.espresso.matcher.ViewMatchers.isClickable();
    }

    public static Matcher<View> withHint(String hintText) {
        return android.support.test.espresso.matcher.ViewMatchers.withHint(hintText);
    }
    public static Matcher<View> withHint(final Matcher<String> stringMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withHint(stringMatcher);
    }
    public static Matcher<View> withHint(final int resourceId) {
        return android.support.test.espresso.matcher.ViewMatchers.withHint(resourceId);
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
    public static Matcher<View> isCompletelyDisplayed() {
        return android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed();
    }

    public static Matcher<View> withClassName(final Matcher<String> classNameMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withClassName(classNameMatcher);
    }

    public static Matcher<View> withId(final int id) {
        return android.support.test.espresso.matcher.ViewMatchers.withId(id);
    }

    public static Matcher<View> withId(final Matcher<Integer> integerMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withId(integerMatcher);
    }

    public static Matcher<View> withResourceName(String name) {
        return withResourceName(is(name));
    }

    public static Matcher<View> withResourceName(final Matcher<String> stringMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withResourceName(stringMatcher);
    }

    public static Matcher<View> withTagKey(final int key) {
        return android.support.test.espresso.matcher.ViewMatchers.withTagKey(key);
    }
    public static Matcher<View> withTagKey(final int key, final Matcher<Object> objectMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withTagKey(key, objectMatcher);
    }
    public static Matcher<View> withTagValue(final Matcher<Object> tagValueMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withTagValue(tagValueMatcher);
    }

    public static Matcher<View> withText(String text) {
        return android.support.test.espresso.matcher.ViewMatchers.withText(text);
    }
    public static Matcher<View> withText(final Matcher<String> stringMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.withText(stringMatcher);
    }
    public static Matcher<View> withText(final int resourceId) {
        return android.support.test.espresso.matcher.ViewMatchers.withText(resourceId);
    }

    public static Matcher<View> withSubstring(String substring) {
        return android.support.test.espresso.matcher.ViewMatchers.withSubstring(substring);
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

    public static Matcher<View> hasErrorText(final Matcher<String> stringMatcher) {
        return android.support.test.espresso.matcher.ViewMatchers.hasErrorText(stringMatcher);
    }
    public static Matcher<View> hasErrorText(final String expectedError) {
        return android.support.test.espresso.matcher.ViewMatchers.hasErrorText(expectedError);
    }

    public static Matcher<View> withInputType(final int inputType) {
        return android.support.test.espresso.matcher.ViewMatchers.withInputType(inputType);
    }

    public static Matcher<View> withParentIndex(final int index) {
        return android.support.test.espresso.matcher.ViewMatchers.withParentIndex(index);
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

    public static Matcher<View> isAssignableFrom(final Class<? extends View> clazz) {
        return android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom(clazz);
    }
}