package com.forrestguice.suntimeswidget.support.espresso.matcher;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withParentIndex;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.allOf;

/**
 * This class contains view matchers that are copied from or based on examples found on the internet.
 */
public class ViewMatchersContrib
{
    public static Matcher<View> tabLayout() {
        return allOf(isDisplayed(), isAssignableFrom(TabLayout.class));
    }

    /**
     *  https://stackoverflow.com/a/39756832
     */
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index)
    {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    /**
     * from https://stackoverflow.com/a/42368341
     */
    public static Matcher<View> navigationButton() {
        return allOf(
                isAssignableFrom(AppCompatImageButton.class),
                withParent(isAssignableFrom(Toolbar.class)));
    }

    @TargetApi(12)
    public static Matcher<View> hasDrawable(int drawableResourceId) {
        return new DrawableMatcher(drawableResourceId);
    }
    @TargetApi(12)
    public static Matcher<View> hasDrawable(Drawable drawable) {
        return new DrawableMatcher(drawable);
    }
    @TargetApi(12)
    public static class DrawableMatcher extends TypeSafeMatcher<View>
    {
        protected int withResourceID;
        public DrawableMatcher(int withResourceId) {
            this.withResourceID = withResourceId;
        }

        protected Drawable drawable = null;
        public DrawableMatcher(Drawable drawable) {
            this.drawable = drawable;
        }

        @Override
        protected boolean matchesSafely(View view)
        {
            if (!(view instanceof ImageView) && !(view instanceof AppCompatImageButton) && !(view instanceof ImageButton)) {
                return false;
            }
            Drawable withDrawable = drawable;
            if (drawable == null) {
                withDrawable = view.getContext().getResources().getDrawable(withResourceID);
                if (withDrawable == null) {
                    return false;
                }
            }

            Drawable d = null;
            if (view instanceof ImageView) {
                d = ((ImageView) view).getDrawable();
            } else if (view instanceof AppCompatImageButton) {
                d = ((AppCompatImageButton) view).getDrawable();
            } else if (view instanceof ImageButton) {
                d = ((ImageButton) view).getDrawable();
            }
            return createBitmap(withDrawable).sameAs(createBitmap(d));
        }

        @Override
        public void describeTo(Description description)
        {
            if (drawable != null) {
                description.appendText("with drawable: ");
                description.appendValue(drawable.toString());
            } else {
                description.appendText("with drawable resource id: ");
                description.appendValue(withResourceID);
            }
        }

        @Nullable
        private Bitmap createBitmap(@Nullable Drawable d)
        {
            if (d != null)
            {
                Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                d.setBounds(0, 0, c.getWidth(), c.getHeight());
                d.draw(c);
                return b;

            } else {
                return null;
            }
        }
    }

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
            boolean valueWasParsed = false;
            double valueDifferedBy = 0;

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
                    try {
                        double v = Double.parseDouble(text);
                        valueWasParsed = true;
                        valueDifferedBy = Math.abs(v - value);
                        return valueDifferedBy <= tolerance;

                    } catch (NumberFormatException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with double value: " + value);
                description.appendText(valueWasParsed
                        ? " [match differed by " + valueDifferedBy + " (tolerance of " + tolerance + ")]"
                        : " [NumberFormatException]" );
            }
        };
    }
    public static Matcher<View> withTextAsDoubleApproximateTo(final double value, final double tolerance)
    {
        return withTextAsDoubleApproximateTo(value, tolerance, new String[] {
                "°", "∠", "δ", "$", "%", "#", "!", "\\(", "\\)", "\\*",    // strip these symbols if found
                "N", "NNE", "NE", "ENE",
                "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW",
                "W", "WNW", "NW", "NNW" });
    }

    public static Matcher<View> withTextAsDateInFormat(final SimpleDateFormat expectedFormat)
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
                    try {
                        expectedFormat.parse(text).getTime();
                        return true;
                    } catch (ParseException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with date: " + expectedFormat.format(Calendar.getInstance()));
            }
        };
    }

    public static Matcher<View> withTextAsDate(final SimpleDateFormat[] allowedFormats, final Calendar calendar0, final long tolerance, final boolean allowSurroundingText)
    {
        return new TypeSafeMatcher<View>()
        {
            boolean dateWasParsed = false;
            long dateDifferedBy = 0;

            @Override
            protected boolean matchesSafely(View view)
            {
                if (view instanceof TextView)
                {
                    String text = ((TextView) view).getText().toString();
                    for (SimpleDateFormat format : allowedFormats)
                    {
                        format.setTimeZone(calendar0.getTimeZone());
                        try {
                            Calendar calendar1 = Calendar.getInstance(calendar0.getTimeZone());
                            calendar1.setTimeInMillis(format.parse(text).getTime());
                            calendar1.set(Calendar.YEAR, calendar0.get(Calendar.YEAR));
                            dateWasParsed = true;

                            if (!allowSurroundingText) {
                                text = text.replaceAll(format.format(calendar1.getTimeInMillis()), "").trim();
                                if (!text.isEmpty()) {
                                    continue;
                                }
                            }

                            long d = Math.abs(calendar1.getTimeInMillis() - calendar0.getTimeInMillis());
                            if (d <= tolerance) {
                                return true;
                            }
                            dateDifferedBy = d;
                        } catch (ParseException e) { /* EMPTY */ }
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("with date: " + allowedFormats[0].format(calendar0.getTimeInMillis()));
                description.appendText(dateWasParsed
                        ? " [match differed by " + dateDifferedBy + " ms (tolerance of " + tolerance + " ms)]"
                        : " [ParseException (wrong format?)]");
            }
        };
    }

    /**
     * https://stackoverflow.com/a/34286462
     */
    public static Matcher<View> isShowingError()
    {
        return new TypeSafeMatcher<View>()
        {
            @Override
            public boolean matchesSafely(View view) {
                if (view instanceof EditText) {
                    return (((EditText) view).getError() != null);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with error text");
            }
        };
    }
}