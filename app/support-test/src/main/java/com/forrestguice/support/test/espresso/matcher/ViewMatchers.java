package com.forrestguice.support.test.espresso.matcher;

import android.support.design.widget.TabLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.allOf;
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

    public static Matcher<View> tabLayout() {
        return allOf(isDisplayed(), isAssignableFrom(TabLayout.class));
    }

    /**
     * from https://stackoverflow.com/a/42368341
     */
    public static Matcher<View> navigationButton() {
        return allOf(
                isAssignableFrom(AppCompatImageButton.class),
                withParent(isAssignableFrom(Toolbar.class)));
    }

    /*public static Matcher<View> withTabText(String text)
    {
        return new BoundedMatcher<View, TabItem>(TabItem.class)
        {
            @Override
            public void describeTo(Description description) {
                description.appendText("with tab: ");
                description.appendText(text);
            }

            public boolean matchesSafely(TabItem item) {
                return (item != null && item.text != null) && text.equals(item.text.toString());
            }
        };
    }*/

    /**
     * based on https://stackoverflow.com/a/73584446
     */
    public static Matcher<View> isMenuDropDownListView() {
        return withClassName(endsWith("MenuDropDownListView"));
    }
    public static Matcher<View> withPositionInMenuDropDownListView(int position) {
        return allOf(withParent(isMenuDropDownListView()), withParentIndex(position));
    }
    public static Matcher<View> isMenuItemWithTextInMenuDropDownListView(String text) {
        return allOf(withParent(isMenuDropDownListView()), withChild(withChild(withChild(withText(text)))));
    }
    public static Matcher<View> isMenuItemWithTextInMenuDropDownListView(int textResID) {
        return allOf(withParent(isMenuDropDownListView()), withChild(withChild(withChild(withText(textResID)))));
    }

    public static Matcher<View> isCheckBox() {
        return withClassName(endsWith("CheckBox"));
    }
    public static Matcher<View> isRadioButton() {
        return withClassName(endsWith("RadioButton"));
    }

    public static Matcher<View> isCheckBoxWithTextInDropDownMenu(int textResID) {
        return allOf(
                isCheckBox(),
                withParent(
                        withParent(
                                isMenuItemWithTextInMenuDropDownListView(textResID)
                        )
                )
        );
    }
    public static Matcher<View> isRadioButtonWithTextInDropDownMenu(int textResID) {
        return allOf(
                isRadioButton(),
                withParent(
                        withParent(
                                isMenuItemWithTextInMenuDropDownListView(textResID)
                        )
                )
        );
    }

    /**
     * based on https://stackoverflow.com/a/34607110
     */
    public static Matcher<View> withTextAsDoubleApproximateTo(final double value, final double tolerance, final String[] ignoreSymbols)
    {
        return new TypeSafeMatcher<View>()
        {
            @Override
            protected boolean matchesSafely(View view)
            {
                if (view instanceof TextView)
                {
                    TextView textView = (TextView) view;
                    String text = textView.getText().toString();
                    for (String symbol : ignoreSymbols) {
                        text = text.replaceAll(symbol, "");
                    }
                    double v = Double.parseDouble(text);
                    return Math.abs(v - value) <= tolerance;

                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("value is not approximate to " + value + " (differs by more than " + tolerance + ")");
            }
        };
    }
    public static Matcher<View> withTextAsDoubleApproximateTo(final double value, final double tolerance)
    {
        return withTextAsDoubleApproximateTo(value, tolerance, new String[] {
                "°", "∠", "δ", "$", "%", "#", "!", "\\(", "\\)", "\\*",
                "N", "NNE", "NE", "ENE",
                "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW",
                "W", "WNW", "NW", "NNW" });
    }

}