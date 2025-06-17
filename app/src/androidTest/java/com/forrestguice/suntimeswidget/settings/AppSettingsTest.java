/**
    Copyright (C) 2017-2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
@RunWith(AndroidJUnit4.class)
public class AppSettingsTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    private Context context;

    @Before
    public void init()
    {
        context = activityRule.getActivity();
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_localeModePref()
    {
        String defaultValue0 = AppSettings.PREF_DEF_LOCALE_MODE.name();
        String defaultValue1 = context.getResources().getString(R.string.def_app_locale_mode);
        assertTrue("defaults should match", defaultValue0.equals(defaultValue1));

        AppSettings.LocaleMode value = AppSettings.loadLocaleModePref(context);
    }

    @Test
    public void test_localePref()
    {
        String defaultValue0 = AppSettings.PREF_DEF_LOCALE;
        String defaultValue1 = context.getResources().getString(R.string.def_app_locale);
        assertTrue("defaults should match", defaultValue0.equals(defaultValue1));

        String value = AppSettings.loadLocalePref(context);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_themePref()
    {
        //String defaultValue0 = AppSettings.PREF_DEF_APPEARANCE_THEME;
        //String defaultValue1 = context.getResources().getString(R.string.def_app_appearance_theme);
        //assertTrue("defaults should match", defaultValue0.equals(defaultValue1));

        String value = AppSettings.loadThemePref(context);
    }

    @Test
    public void test_showWarningsPref()
    {
        boolean defaultValue0 = AppSettings.PREF_DEF_UI_SHOWWARNINGS;
        boolean defaultValue1 = context.getResources().getBoolean(R.bool.def_app_ui_showwarnings);
        assertTrue("defaults should match", defaultValue0 == defaultValue1);

        boolean value = AppSettings.loadShowWarningsPref(context);
    }

    @Test
    public void test_showFieldsPref()
    {
        verify_showFields(false);
        verify_showFields(true);
    }

    private void verify_showFields(boolean value)
    {
        for (int i=0; i<AppSettings.NUM_FIELDS; i++)
        {
            AppSettings.saveShowFieldsPref(context, i, value);
            boolean[] showFields = AppSettings.loadShowFieldsPref(context);
            assertTrue("field " + i + " should be + " + value, showFields[i] == value);
        }
    }

    @Test
    public void test_showLightmapPref()
    {
        boolean defaultValue0 = AppSettings.PREF_DEF_UI_SHOWLIGHTMAP;
        boolean defaultValue1 = context.getResources().getBoolean(R.bool.def_app_ui_showlightmap);
        assertTrue("defaults should match", defaultValue0 == defaultValue1);

        boolean value = AppSettings.loadShowLightmapPref(context);
    }

    @Test
    public void test_showEquinoxPref()
    {
        boolean defaultValue0 = AppSettings.PREF_DEF_UI_SHOWEQUINOX;
        boolean defaultValue1 = context.getResources().getBoolean(R.bool.def_app_ui_showequinox);
        assertTrue("defaults should match", defaultValue0 == defaultValue1);

        boolean value = AppSettings.loadShowEquinoxPref(context);
    }

    @Test
    public void test_showHeaderIcon()
    {
        boolean defaultValue0 = AppSettings.PREF_DEF_UI_SHOWHEADER_ICON;
        boolean defaultValue1 = context.getResources().getBoolean(R.bool.def_app_ui_showheader_icon);
        assertTrue("defaults should match", defaultValue0 == defaultValue1);
        boolean value = AppSettings.loadShowHeaderIconPref(context);
    }

    @Test
    public void test_showHeaderText()
    {
        int defaultValue0 = AppSettings.PREF_DEF_UI_SHOWHEADER_TEXT;
        int defaultValue1 = Integer.parseInt(context.getResources().getString(R.string.def_app_ui_showheader_text));
        assertTrue("defaults should match", defaultValue0 == defaultValue1);
        int value = AppSettings.loadShowHeaderTextPref(context);
    }

    @Test
    public void test_showDataSourcePref()
    {
        boolean defaultValue0 = AppSettings.PREF_DEF_UI_SHOWDATASOURCE;
        boolean defaultValue1 = context.getResources().getBoolean(R.bool.def_app_ui_showdatasource);
        assertTrue("defaults should match", defaultValue0 == defaultValue1);

        boolean value = AppSettings.loadDatasourceUIPref(context);
    }

    @Test
    public void test_clockTapPref()
    {
        String defaultValue0 = AppSettings.PREF_DEF_UI_CLOCKTAPACTION;
        String defaultValue1 = context.getResources().getString(R.string.def_app_ui_clocktapaction);
        assertTrue("defaults should match", defaultValue0.equals(defaultValue1));

        String value = AppSettings.loadClockTapActionPref(context);
    }

    @Test
    public void test_dateTapPref()
    {
        String defaultValue0 = AppSettings.PREF_DEF_UI_DATETAPACTION;
        String defaultValue1 = context.getResources().getString(R.string.def_app_ui_datetapaction);
        assertTrue("defaults should match", defaultValue0.equals(defaultValue1));

        String value = AppSettings.loadDateTapActionPref(context);
    }

    @Test
    public void test_noteTapPref()
    {
        String defaultValue0 = AppSettings.PREF_DEF_UI_NOTETAPACTION;
        String defaultValue1 = context.getResources().getString(R.string.def_app_ui_notetapaction);
        assertTrue("defaults should match", defaultValue0.equals(defaultValue1));

        String value = AppSettings.loadNoteTapActionPref(context);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_timeZoneSortPref()
    {
        AppSettings.saveTimeZoneSortPref(context, WidgetTimezones.TimeZoneSort.SORT_BY_ID);
        WidgetTimezones.TimeZoneSort sort2 = AppSettings.loadTimeZoneSortPref(context);
        assertTrue("pref should be SORT_BY_ID but was " + sort2, sort2 == WidgetTimezones.TimeZoneSort.SORT_BY_ID);

        AppSettings.saveTimeZoneSortPref(context, WidgetTimezones.TimeZoneSort.SORT_BY_OFFSET);
        WidgetTimezones.TimeZoneSort sort1 = AppSettings.loadTimeZoneSortPref(context);
        assertTrue("pref should be SORT_BY_OFFSET but was " + sort1, sort1 == WidgetTimezones.TimeZoneSort.SORT_BY_OFFSET);

        AppSettings.saveTimeZoneSortPref(context, AppSettings.PREF_DEF_UI_TIMEZONESORT);
        WidgetTimezones.TimeZoneSort sort0 = AppSettings.loadTimeZoneSortPref(context);
        assertTrue("pref should be default (SORT_BY_ID) but was " + sort0, sort0 == AppSettings.PREF_DEF_UI_TIMEZONESORT && sort0 == WidgetTimezones.TimeZoneSort.SORT_BY_ID);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_gpsMaxAgePref()
    {
        String defaultValue0 = context.getResources().getString(R.string.def_getFix_maxAge);
        long defaultValue = Long.parseLong(defaultValue0);
        assertTrue("default must be positive", defaultValue > 0);

        boolean found = false;
        String[] values = context.getResources().getStringArray(R.array.getFix_maxAge_values);
        for (String value : values)
        {
            found = value.equals(defaultValue0);
            if (found)
                break;
        }
        assertTrue("default must belong to R.array.getFix_maxAge_values", found);
    }

    @Test
    public void test_gpsMaxElapsedPref()
    {
        String defaultValue0 = context.getResources().getString(R.string.def_getFix_maxElapsed);
        long defaultValue = Long.parseLong(defaultValue0);
        assertTrue("default must be positive", defaultValue > 0);

        boolean found = false;
        String[] values = context.getResources().getStringArray(R.array.getFix_maxElapse_values);
        for (String value : values)
        {
            found = value.equals(defaultValue0);
            if (found)
                break;
        }
        assertTrue("default must belong to R.array.getFix_maxElapse_values", found);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_dialogDoNotShowAgain()
    {
        String dialogKey1 = "testdialog1";
        String dialogKey2 = "testdialog2";

        AppSettings.setDialogDoNotShowAgain(context, dialogKey1, false);
        AppSettings.setDialogDoNotShowAgain(context, dialogKey2, true);
        assertFalse(AppSettings.checkDialogDoNotShowAgain(context, dialogKey1));
        assertTrue(AppSettings.checkDialogDoNotShowAgain(context, dialogKey2));

        AppSettings.setDialogDoNotShowAgain(context, dialogKey1, true);
        AppSettings.setDialogDoNotShowAgain(context, dialogKey2, false);
        assertTrue(AppSettings.checkDialogDoNotShowAgain(context, dialogKey1));
        assertFalse(AppSettings.checkDialogDoNotShowAgain(context, dialogKey2));
    }

}
