/**
    Copyright (C) 2025 Forrest Guice
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

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTest;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppThemeTest extends SuntimesActivityTestBase
{
    @Test
    public void test_appTheme_system() {
        test_appTheme(new AppSettings.SystemThemeInfo());
    }

    @Test
    public void test_appTheme_systemContrast() {
        test_appTheme(new AppSettings.System1ThemeInfo());
    }

    @Test
    public void test_appTheme_dark() {
        test_appTheme(new AppSettings.DarkThemeInfo());
    }

    @Test
    public void test_appTheme_darkContrast() {
        test_appTheme(new AppSettings.DarkTheme1Info());
    }

    @Test
    public void test_appTheme_light() {
        test_appTheme(new AppSettings.LightThemeInfo());
    }

    @Test
    public void test_appTheme_lightContrast() {
        test_appTheme(new AppSettings.LightTheme1Info());
    }

    ///////////////////////////////////////////////////////////////////////////

    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class, true, false);

    protected Activity launchActivity(AppSettings.AppThemeInfo theme, AppSettings.TextSize textSize)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(SuntimesActivity.EXTRA_APPTHEME, theme.getExtendedThemeName(textSize));
        return activityRule.launchActivity(intent);
    }

    protected void test_appTheme(AppSettings.AppThemeInfo theme)
    {
        SuntimesActivityTest.MainActivityRobot robot = new SuntimesActivityTest.MainActivityRobot();
        for (AppSettings.TextSize textSize : AppSettings.TextSize.values())
        {
            Activity activity = launchActivity(theme, textSize);
            robot.assertActivityShown(activity);
            activity.finish();
        }
    }
}
