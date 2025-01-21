package com.forrestguice.support.test.espresso.action;

import android.support.design.widget.TabLayout;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

import javax.annotation.Nonnull;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

public class ViewActions
{
    public static ViewAction click() {
        return android.support.test.espresso.action.ViewActions.click();
    }

    public static ViewAction swipeLeft() {
        return android.support.test.espresso.action.ViewActions.swipeLeft();
    }

    public static ViewAction swipeRight() {
        return android.support.test.espresso.action.ViewActions.swipeRight();
    }

    public static ViewAction swipeDown() {
        return android.support.test.espresso.action.ViewActions.swipeDown();
    }

    public static ViewAction swipeUp() {
        return android.support.test.espresso.action.ViewActions.swipeUp();
    }

    public static ViewAction closeSoftKeyboard() {
        return android.support.test.espresso.action.ViewActions.closeSoftKeyboard();
    }

    public static ViewAction pressImeActionButton() {
        return android.support.test.espresso.action.ViewActions.pressImeActionButton();
    }

    public static ViewAction pressBack() {
        return android.support.test.espresso.action.ViewActions.pressBack();
    }

    public static ViewAction pressMenuKey() {
        return android.support.test.espresso.action.ViewActions.pressMenuKey();
    }

    public static ViewAction doubleClick() {
        return android.support.test.espresso.action.ViewActions.doubleClick();
    }

    public static ViewAction longClick() {
        return android.support.test.espresso.action.ViewActions.longClick();
    }

    public static ViewAction scrollTo() {
        return android.support.test.espresso.action.ViewActions.scrollTo();
    }

    public static ViewAction replaceText(@Nonnull String stringToBeSet) {
        return android.support.test.espresso.action.ViewActions.replaceText(stringToBeSet);
    }

    /**
     * from https://stackoverflow.com/a/51262525
     */
    public static ViewAction selectTabAtPosition(final int position)
    {
        return new ViewAction()
        {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(TabLayout.class));
            }

            @Override
            public String getDescription() {
                return "with tab at index " + position;
            }

            @Override
            public void perform(UiController uiController, View view)
            {
                if (view instanceof TabLayout)
                {
                    TabLayout tabLayout = (TabLayout) view;
                    TabLayout.Tab tab = tabLayout.getTabAt(position);
                    if (tab != null) {
                        tab.select();
                    }
                }
            }
        };
    }

}