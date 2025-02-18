package com.forrestguice.suntimeswidget.support.espresso.action;

import android.support.design.widget.TabLayout;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

public class ViewActionsContrib
{
    public static ViewAction swipeRightTo(final int x) {
        return swipeHorizontalTo(x);
    }
    public static ViewAction swipeLeftTo(final int x) {
        return swipeHorizontalTo(x);
    }
    public static ViewAction swipeHorizontalTo(final int x)
    {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER, new CoordinatesProvider()
        {
            @Override
            public float[] calculateCoordinates(View view)
            {
                int[] position = new int[2];
                view.getLocationOnScreen(position);
                return new float[] { x, position[1] };
            }
        }, Press.FINGER);
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