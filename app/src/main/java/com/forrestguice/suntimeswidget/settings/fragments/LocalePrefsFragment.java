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
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.views.Toast;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Locale Prefs
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LocalePrefsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppSettings.initLocale(getActivity());
        Log.i(SuntimesSettingsActivity.LOG_TAG, "LocalePrefsFragment: Arguments: " + getArguments());

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_locale, false);
        addPreferencesFromResource(R.xml.preference_locale);

        initPref_locale(LocalePrefsFragment.this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_locale(PreferenceFragment fragment)
    {
        Preference localeModePref = fragment.findPreference(AppSettings.PREF_KEY_LOCALE_MODE);
        ListPreference localePref = (ListPreference) fragment.findPreference(AppSettings.PREF_KEY_LOCALE);
        initPref_locale(fragment.getActivity(), localeModePref, localePref);
    }

    public static Pair<CharSequence[], CharSequence[]> getEntries(final Activity activity)
    {
        final String[] localeDisplay = activity.getResources().getStringArray(R.array.locale_display);
        final String[] localeDisplayNative = activity.getResources().getStringArray(R.array.locale_display_native);
        final String[] localeValues = activity.getResources().getStringArray(R.array.locale_values);

        Integer[] index = getSortedOrder(localeDisplayNative);
        CharSequence[] entries = new CharSequence[localeValues.length];
        CharSequence[] values = new CharSequence[localeValues.length];
        for (int i=0; i<index.length; i++)
        {
            int j = index[i];
            CharSequence formattedDisplayString;
            CharSequence localeDisplay_j = (localeDisplay.length > j ? localeDisplay[j] : localeValues[j]);
            CharSequence localeDisplayNative_j = (localeDisplayNative.length > j ? localeDisplayNative[j] : localeValues[j]);

            if (localeDisplay_j.equals(localeDisplayNative_j)) {
                formattedDisplayString = localeDisplayNative_j;

            } else {
                String localizedName = "(" + localeDisplay_j + ")";
                String displayString = localeDisplayNative_j + " " + localizedName;
                formattedDisplayString = SuntimesUtils.createRelativeSpan(null, displayString, localizedName, 0.7f);
            }

            entries[i] = formattedDisplayString;
            values[i] = localeValues[j];
        }
        return new Pair<>(entries, values);
    }

    public static void initPref_locale(final Activity activity, Preference localeModePref, ListPreference localePref)
    {
        Pair<CharSequence[], CharSequence[]> a = getEntries(activity);
        localePref.setEntries(a.first);
        localePref.setEntryValues(a.second);

        AppSettings.LocaleMode localeMode = AppSettings.loadLocaleModePref(activity);
        localePref.setEnabled(localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE);

        if (localePref != null) {
            localePref.setOnPreferenceChangeListener(onLocaleChanged(activity, localePref));
        }
        if (localeModePref != null) {
            localeModePref.setOnPreferenceChangeListener(onLocaleChanged(activity, localeModePref));
        }
    }

    /**
     * @param stringArray array to perform sort on
     * @return a sorted array of indices pointing into stringArray
     */
    protected static Integer[] getSortedOrder(final String[] stringArray)
    {
        Integer[] index = new Integer[stringArray.length];
        for (int i=0; i < index.length; i++)
        {
            index[i] = i;
        }
        Arrays.sort(index, new Comparator<Integer>()
        {
            public int compare(Integer i1, Integer i2)
            {
                return stringArray[i1].compareTo(stringArray[i2]);
            }
        });
        return index;
    }

    protected static Preference.OnPreferenceChangeListener onLocaleChanged(final Context context, final Preference pref)
    {
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(context, context.getString(R.string.restart_required_message), Toast.LENGTH_LONG).show();
                return true;
            }
        };
    }

}
