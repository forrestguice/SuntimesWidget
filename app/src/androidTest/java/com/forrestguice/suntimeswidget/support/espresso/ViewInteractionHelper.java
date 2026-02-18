package com.forrestguice.suntimeswidget.support.espresso;

import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.ViewInteraction;
import android.view.View;

import org.hamcrest.Matcher;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;

public class ViewInteractionHelper
{
    /**
     * @param viewInteraction a ViewInteraction wrapping some view
     * @return true view is checked, false otherwise
     */
    public static boolean viewIsChecked(ViewInteraction viewInteraction)
    {
        final boolean[] isChecked = {true};
        viewInteraction.withFailureHandler(new FailureHandler()
        {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher)
            {
                isChecked[0] = false;
            }
        }).check(matches(isChecked()));
        return isChecked[0];
    }

    /**
     * ViewInteractionInterface
     */
    public interface ViewInteractionInterface
    {
        ViewInteraction get();
    }

    public static ViewInteractionInterface wrap(final ViewInteraction viewInteraction) {
        return new ViewInteractionInterface() {
            @Override
            public ViewInteraction get() {
                return viewInteraction;
            }
        };
    }

}