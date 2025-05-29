package com.forrestguice.suntimeswidget.support.espresso;

import android.support.test.espresso.ViewAssertion;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

public class ViewAssertionHelper
{
    public static ViewAssertion assertShown = matches(isDisplayed());
    public static ViewAssertion assertShownCompletely = matches(isDisplayingAtLeast(90));
    public static ViewAssertion assertHidden = matches(not(isDisplayed()));
    public static ViewAssertion assertEnabled = matches(allOf(isEnabled(), isDisplayed()));
    public static ViewAssertion assertDisabled = matches(allOf(not(isEnabled()), isDisplayed()));
    public static ViewAssertion assertFocused = matches(allOf(isEnabled(), isDisplayed(), hasFocus()));
    public static ViewAssertion assertClickable = matches(isClickable());
    public static ViewAssertion assertSelected = matches(isSelected());
    public static ViewAssertion assertChecked = matches(isChecked());
    public static ViewAssertion assertNotChecked = matches(isNotChecked());
    public static ViewAssertion assertContainsText(String text) {
        return matches(withText(containsString(text)));
    }
}