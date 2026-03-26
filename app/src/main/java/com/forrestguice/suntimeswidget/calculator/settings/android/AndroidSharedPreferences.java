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

package com.forrestguice.suntimeswidget.calculator.settings.android;

import android.os.Build;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.prefs.SharedPreferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AndroidSharedPreferences implements SharedPreferences
{
    private final android.content.SharedPreferences prefs;
    public AndroidSharedPreferences(android.content.SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public static AndroidSharedPreferences wrap(android.content.SharedPreferences prefs) {
        return new AndroidSharedPreferences(prefs);
    }

    private static final HashMap<OnSharedPreferenceChangeListener, android.content.SharedPreferences.OnSharedPreferenceChangeListener> listeners = new HashMap<>();

    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener)
    {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener l = new android.content.SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(android.content.SharedPreferences sharedPreferences, String key) {
                listener.onSharedPreferenceChanged(AndroidSharedPreferences.this, key);
            }
        };
        listeners.put(listener, l);
        prefs.registerOnSharedPreferenceChangeListener(l);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listeners.remove(listener));
    }

    @Override
    public Editor edit()
    {
        return new Editor()
        {
            private final android.content.SharedPreferences.Editor edit = prefs.edit();

            @Override
            public Editor putString(String key, String value) {
                edit.putString(key, value);
                return this;
            }

            @Override
            public Editor putStringSet(String key, Set<String> values)
            {
                if (Build.VERSION.SDK_INT >= 11) {
                    edit.putStringSet(key, values);
                } else {
                    edit.putString(key, stringSetToString(values));
                }
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                edit.putInt(key, value);
                return this;
            }

            @Override
            public Editor putLong(String key, long value) {
                edit.putLong(key, value);
                return this;
            }

            @Override
            public Editor putFloat(String key, float value) {
                edit.putFloat(key, value);
                return this;
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                edit.putBoolean(key, value);
                return this;
            }

            @Override
            public Editor remove(String key) {
                edit.remove(key);
                return this;
            }

            @Override
            public Editor clear() {
                edit.clear();
                return this;
            }

            @Override
            public boolean commit() {
                return edit.commit();
            }

            @Override
            public void apply() {
                edit.apply();
            }
        };
    }

    @Override
    public boolean contains(String key) {
        return prefs.contains(key);
    }

    @Override
    public Map<String, ?> getAll() {
        return prefs.getAll();
    }

    @Override
    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    @Override
    public String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues)
    {
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getStringSet(key, defValues);
        } else {
            String s = prefs.getString(key, null);
            return (s != null) ? new TreeSet<>(Arrays.asList(s.split("\\|"))) : null;
        }
    }

    public static String stringSetToString(@Nullable Set<String> values)
    {
        if (values != null) {
            StringBuilder s = new StringBuilder();
            for (String v : values) {
                s.append(v).append("|");
            }
            return s.toString();
        } else {
            return null;
        }
    }
}
