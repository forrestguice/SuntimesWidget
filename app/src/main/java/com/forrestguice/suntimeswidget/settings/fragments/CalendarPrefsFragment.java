/**
    Copyright (C) 2014-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import com.forrestguice.suntimeswidget.AboutDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.AppSettings;

import com.forrestguice.support.preference.Preference;
import com.forrestguice.support.preference.PreferenceFragment;

/**
 * Calendar Prefs
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CalendarPrefsFragment extends PreferenceFragment
{
    public static String calendarPackage = "com.forrestguice.suntimescalendars";
    public static String calendarActivity = "com.forrestguice.suntimeswidget.calendar.SuntimesCalendarActivity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent calendarIntent = new Intent();
        calendarIntent.setComponent(new ComponentName(calendarPackage, calendarActivity));
        calendarIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(calendarIntent);
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
            return;

        } catch (Exception e) {
            Log.e("CalendarPrefs", "Unable to launch SuntimesCalendarActivity! " + e);
        }

        AppSettings.initLocale(getActivity());
        addPreferencesFromResource(R.xml.preference_calendar);
        Preference calendarReadme = (Preference) findPreference("appwidget_0_calendars_readme");
        if (calendarReadme != null)
        {
            calendarReadme.setSummary(SuntimesUtils.fromHtml(getString(R.string.help_calendar)));
            calendarReadme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Activity activity = getActivity();
                    if (activity != null) {
                        AboutDialog.openLink(activity, getString(R.string.help_addons_url));
                        activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
                    }
                    return false;
                }
            });
        }
        Log.i(SuntimesSettingsActivity.LOG_TAG, "CalendarPrefsFragment: Arguments: " + getArguments());
    }
}
