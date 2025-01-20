package com.forrestguice.support.test.espresso;

import android.content.Context;
import android.os.Looper;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.List;

public class Espresso
{
    public static DataInteraction onData(Matcher<? extends Object> dataMatcher) {
        return android.support.test.espresso.Espresso.onData(dataMatcher);
    }

    public static ViewInteraction onView(final Matcher<View> viewMatcher) {
        return android.support.test.espresso.Espresso.onView(viewMatcher);
    }

    public static void registerLooperAsIdlingResource(Looper looper) {
        android.support.test.espresso.Espresso.registerLooperAsIdlingResource(looper);
    }

    public static void registerLooperAsIdlingResource(Looper looper, boolean considerWaitIdle) {
        android.support.test.espresso.Espresso.registerLooperAsIdlingResource(looper, considerWaitIdle);
    }

    public static boolean registerIdlingResources(IdlingResource... resources) {
        return android.support.test.espresso.Espresso.registerIdlingResources(resources);
    }

    public static boolean unregisterIdlingResources(IdlingResource... resources) {
        return android.support.test.espresso.Espresso.unregisterIdlingResources(resources);
    }

    public static List<IdlingResource> getIdlingResources() {
        return android.support.test.espresso.Espresso.getIdlingResources();
    }

    public static void closeSoftKeyboard() {
        android.support.test.espresso.Espresso.closeSoftKeyboard();
    }

    public static void openContextualActionModeOverflowMenu() {
        Espresso.openContextualActionModeOverflowMenu();
    }

    public static void pressBack() {
        android.support.test.espresso.Espresso.pressBack();
    }

    public static void openActionBarOverflowOrOptionsMenu(Context context) {
        android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu(context);
    }
}