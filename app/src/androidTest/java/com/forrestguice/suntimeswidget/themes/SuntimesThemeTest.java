/**
    Copyright (C) 2017-2018 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.themes;

import android.content.Context;
//import android.content.res.Resources;
import android.graphics.Color;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SuntimesThemeTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Test
    public void test_initTheme()
    {
        Context context = activityRule.getActivity();
        SuntimesTheme darkTheme = new DarkTheme(context);
        SuntimesTheme testTheme = new TestTheme(context);
        testTheme.saveTheme(context, WidgetThemes.PREFS_THEMES);

        SuntimesTheme theme = new SuntimesTheme();
        boolean initialized = theme.initTheme(context, WidgetThemes.PREFS_THEMES, TestTheme.THEME_NAME, darkTheme);
        assertTrue(initialized);
        verifyInit(theme, testTheme);

        testTheme.deleteTheme(context, WidgetThemes.PREFS_THEMES);
        SuntimesTheme theme1 = new SuntimesTheme();
        theme1.initTheme(context, WidgetThemes.PREFS_THEMES, TestTheme.THEME_NAME, darkTheme);
        verifyInit(theme, darkTheme);    // verify deleted (init should be to default: darkTheme)
    }

    protected void verifyInit(SuntimesTheme theme, SuntimesTheme truth)
    {
        assertTrue("theme name should match " + truth.themeName() + " (was " + theme.themeName() + ")", theme.themeName().equals(truth.themeName()));
        assertTrue("theme version should match", theme.themeVersion() == truth.themeVersion());
        assertTrue("theme display should match", theme.themeDisplayString().equals(truth.themeDisplayString()));
        assertTrue("theme isDefault should match", theme.isDefault() == truth.isDefault());

        assertTrue("theme backgroundID should match", theme.getBackground() == truth.getBackground());
        assertTrue("theme padding should match [0]", theme.getPadding()[0] == truth.getPadding()[0]);
        assertTrue("theme padding should match [1]", theme.getPadding()[1] == truth.getPadding()[1]);
        assertTrue("theme padding should match [2]", theme.getPadding()[2] == truth.getPadding()[2]);
        assertTrue("theme padding should match [3]", theme.getPadding()[3] == truth.getPadding()[3]);

        assertTrue("theme title size should match", theme.getTitleSizeSp() == truth.getTitleSizeSp());
        assertTrue("theme text size should match", theme.getTextSizeSp() == truth.getTextSizeSp());
        assertTrue("theme time size should match", theme.getTimeSizeSp() == truth.getTimeSizeSp());
        assertTrue("theme timeSuffix size should match", theme.getTimeSuffixSizeSp() == truth.getTimeSuffixSizeSp());

        assertTrue("theme title color should match", theme.getTitleColor() == truth.getTitleColor());
        assertTrue("theme text color should match", theme.getTextColor() == truth.getTextColor());
        assertTrue("theme time color should match", theme.getTimeColor() == truth.getTimeColor());
        assertTrue("theme suffix color should match", theme.getTimeSuffixColor() == truth.getTimeSuffixColor());

        assertTrue("theme rise text color should match", theme.getSunriseTextColor() == truth.getSunriseTextColor());
        assertTrue("theme rise icon fill color should match", theme.getSunriseIconColor() == truth.getSunriseIconColor());
        assertTrue("theme rise icon stroke color should match", theme.getSunriseIconStrokeColor() == truth.getSunriseIconStrokeColor());
        assertTrue("theme rise icon stroke width should match", theme.getSunriseIconStrokeWidth() == truth.getSunriseIconStrokeWidth());

        assertTrue("theme noon text color should match", theme.getNoonTextColor() == truth.getNoonTextColor());
        assertTrue("theme noon icon fill color should match", theme.getNoonIconColor() == truth.getNoonIconColor());
        assertTrue("theme noon icon stroke color should match", theme.getNoonIconStrokeColor() == truth.getNoonIconStrokeColor());
        assertTrue("theme noon icon stroke width should match", theme.getNoonIconStrokeWidth() == truth.getNoonIconStrokeWidth());

        assertTrue("theme set color should match", theme.getSunsetTextColor() == truth.getSunsetTextColor());
        assertTrue("theme set icon fill color should match", theme.getSunsetIconColor() == truth.getSunsetIconColor());
        assertTrue("theme set icon stroke color should match", theme.getSunsetIconStrokeColor() == truth.getSunsetIconStrokeColor());
        assertTrue("theme set icon stroke width should match", theme.getSunriseIconStrokeWidth() == truth.getSunriseIconStrokeWidth());

        assertTrue("theme winter color should match", theme.getWinterColor() == truth.getWinterColor());
        assertTrue("theme spring color should match", theme.getSpringColor() == truth.getSpringColor());
        assertTrue("theme summer color should match", theme.getSummerColor() == truth.getSummerColor());
        assertTrue("theme fall color should match", theme.getFallColor() == truth.getFallColor());
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public static final String TESTDEF_NAME = "test";
    public static final String TESTDEF_DISPLAYSTRING = "Test";
    public static final int TESTDEF_VERSION = 1;
    public static final ThemeBackground TESTDEF_BACKGROUND = ThemeBackground.DARK;
    public static final int[] TESTDEF_PADDING = {1, 2, 3, 4};
    public static final float TESTDEF_TITLESIZE = 10;
    public static final float TESTDEF_TEXTSIZE = 10;
    public static final float TESTDEF_TIMESIZE = 12;
    public static final float TESTDEF_TIMESUFFIXSIZE = 6;
    public static final int TESTDEF_TITLECOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int TESTDEF_TEXTCOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int TESTDEF_SUNRISECOLOR_ID = R.color.sunIcon_color_rising_dark;
    public static final int TESTDEF_SUNSETCOLOR_ID = R.color.sunIcon_color_setting_dark;
    public static final int TESTDEF_TIMECOLOR_ID = android.R.color.primary_text_dark;
    public static final int TESTDEF_TIMESUFFIXCOLOR_ID = android.R.color.tertiary_text_dark;

    public static class TestTheme extends SuntimesTheme
    {
        public TestTheme(Context context)
        {
            super();
            this.themeVersion = TESTDEF_VERSION;
            this.themeName = TESTDEF_NAME;
            this.themeIsDefault = true;
            this.themeDisplayString = TESTDEF_DISPLAYSTRING;
            this.themeBackground = TESTDEF_BACKGROUND;
            this.themePadding = TESTDEF_PADDING;
            this.themeTitleSize = TESTDEF_TITLESIZE;
            this.themeTextSize = TESTDEF_TEXTSIZE;
            this.themeTimeSize = TESTDEF_TIMESIZE;
            this.themeTimeSuffixSize = TESTDEF_TIMESUFFIXSIZE;
            this.themeTitleColor = ContextCompat.getColor(context, TESTDEF_TITLECOLOR_ID);
            this.themeTextColor = ContextCompat.getColor(context, TESTDEF_TEXTCOLOR_ID);
            this.themeTimeColor = ContextCompat.getColor(context, TESTDEF_TIMECOLOR_ID);
            this.themeTimeSuffixColor = ContextCompat.getColor(context, TESTDEF_TIMESUFFIXCOLOR_ID);

            this.themeSunriseTextColor = ContextCompat.getColor(context, TESTDEF_SUNRISECOLOR_ID);
            this.themeSunriseIconColor = Color.GREEN;
            this.themeSunriseIconStrokeColor = Color.YELLOW;
            this.themeSunriseIconStrokeWidth = 1;

            this.themeNoonTextColor = Color.WHITE;
            this.themeNoonIconColor = Color.CYAN;
            this.themeNoonIconStrokeColor = Color.MAGENTA;
            this.themeNoonIconStrokeWidth = 2;

            this.themeSunsetTextColor = ContextCompat.getColor(context, TESTDEF_SUNSETCOLOR_ID);
            this.themeSunsetIconColor = Color.BLUE;
            this.themeSunsetIconStrokeColor = Color.RED;
            this.themeSunsetIconStrokeWidth = 3;

            this.themeWinterColor = Color.BLUE;
            this.themeSpringColor = Color.GREEN;
            this.themeSummerColor = Color.YELLOW;
            this.themeFallColor = Color.RED;
        }

        @Override
        public ThemeDescriptor themeDescriptor()
        {
            return new ThemeDescriptor(TESTDEF_NAME, TESTDEF_DISPLAYSTRING, TESTDEF_VERSION);
        }
    }

}
