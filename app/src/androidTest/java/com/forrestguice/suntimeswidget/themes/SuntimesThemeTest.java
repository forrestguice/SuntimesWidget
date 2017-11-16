/**
    Copyright (C) 2017 Forrest Guice
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
import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SuntimesThemeTest extends SuntimesActivityTestBase
{
    private Context context;
    private SuntimesTheme darkTheme, testTheme;

    @Before
    public void init()
    {
        context = activityRule.getActivity();

        darkTheme = new DarkTheme(context);
        darkTheme.saveTheme(WidgetThemes.getSharedPreferences(context));

        testTheme = new TestTheme(context);
        testTheme.deleteTheme(WidgetThemes.getSharedPreferences(context));
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_initTheme()
    {
        String themeName = DarkTheme.THEME_NAME;
        SuntimesTheme defTheme = new TestTheme(context);
        SuntimesTheme theme = new SuntimesTheme();
        boolean initialized = theme.initTheme(context, WidgetThemes.PREFS_THEMES, themeName, defTheme);
        assertTrue(initialized);
        verifyInit(theme, darkTheme);
    }

    protected void verifyInit(SuntimesTheme theme, SuntimesTheme truth)
    {
        assertTrue("theme name should match", theme.themeName().equals(truth.themeName()));
        assertTrue("theme version should match", theme.themeVersion() == truth.themeVersion());
        assertTrue("theme display should match", theme.themeDisplayString().equals(truth.themeDisplayString()));
        assertTrue("theme isDefault should match", theme.isDefault() == truth.isDefault());

        assertTrue("theme backgroundID should match", theme.getBackgroundId() == truth.getBackgroundId());
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
        assertTrue("theme rise color should match", theme.getSunriseTextColor() == truth.getSunriseTextColor());
        assertTrue("theme set color should match", theme.getSunsetTextColor() == truth.getSunsetTextColor());
    }

    @Test
    public void test_saveTheme()
    {
        SharedPreferences themePref = WidgetThemes.getSharedPreferences(context);
        SuntimesTheme theme = new TestTheme(context);
        SuntimesTheme.ThemeDescriptor savedTheme = theme.saveTheme(themePref);

        assertTrue("STUB", true == false);  // TODO
    }

    @Test
    public void test_deleteTheme()
    {
        SharedPreferences themePref = WidgetThemes.getSharedPreferences(context);
        SuntimesTheme theme = new TestTheme(context);
        theme.saveTheme(themePref);

        assertTrue("STUB", true == false);  // TODO
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public static final String TESTDEF_NAME = "test";
    public static final String TESTDEF_DISPLAYSTRING = "Test";
    public static final int TESTDEF_VERSION = 1;
    public static final int TESTDEF_BACKGROUND_ID = R.drawable.bg_widget_dark;
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
            this.themeBackground = TESTDEF_BACKGROUND_ID;
            this.themePadding = TESTDEF_PADDING;
            this.themeTitleSize = TESTDEF_TITLESIZE;
            this.themeTextSize = TESTDEF_TEXTSIZE;
            this.themeTimeSize = TESTDEF_TIMESIZE;
            this.themeTimeSuffixSize = TESTDEF_TIMESUFFIXSIZE;
            this.themeTitleColor = ContextCompat.getColor(context, TESTDEF_TITLECOLOR_ID);
            this.themeTextColor = ContextCompat.getColor(context, TESTDEF_TEXTCOLOR_ID);
            this.themeSunriseTextColor = ContextCompat.getColor(context, TESTDEF_SUNRISECOLOR_ID);
            this.themeSunsetTextColor = ContextCompat.getColor(context, TESTDEF_SUNSETCOLOR_ID);
            this.themeTimeColor = ContextCompat.getColor(context, TESTDEF_TIMECOLOR_ID);
            this.themeTimeSuffixColor = ContextCompat.getColor(context, TESTDEF_TIMESUFFIXCOLOR_ID);
        }

        @Override
        public ThemeDescriptor themeDescriptor()
        {
            return new ThemeDescriptor(TESTDEF_NAME, TESTDEF_DISPLAYSTRING, TESTDEF_VERSION);
        }
    }

}
