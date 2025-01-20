package com.forrestguice.support.test.espresso.action;

import android.support.test.espresso.ViewAction;

import javax.annotation.Nonnull;

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

}