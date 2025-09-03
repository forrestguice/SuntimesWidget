package com.forrestguice.suntimeswidget.calculator.settings.android;

import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.calendar.CalendarSettingsInterface;
import com.forrestguice.util.Resources;
import com.forrestguice.util.SharedPreferences;
import com.forrestguice.util.android.AndroidResources;

public class AndroidCalendarSettings implements CalendarSettingsInterface
{
    private final android.content.Context context;
    public AndroidCalendarSettings(android.content.Context context) {
        this.context = context;
    }

    public static AndroidCalendarSettings wrap(android.content.Context context) {
        return new AndroidCalendarSettings(context);
    }

    @Override
    public void saveCalendarFlag(int appWidgetId, String key, boolean value) {
        CalendarSettings.saveCalendarFlag(this, appWidgetId, key, value);
    }

    @Override
    public boolean loadCalendarFlag(int appWidgetId, String key, boolean defValue) {
        return CalendarSettings.loadCalendarFlag(this, appWidgetId, key, defValue);
    }

    @Override
    public void saveCalendarModePref(int appWidgetId, CalendarMode mode) {
        CalendarSettings.saveCalendarModePref(this, appWidgetId, mode);
    }

    @Override
    public CalendarMode loadCalendarModePref(int appWidgetId) {
        return CalendarSettings.loadCalendarModePref(this, appWidgetId);
    }

    @Override
    public void saveCalendarFormatPatternPref(int appWidgetId, String tag, String formatString) {
        CalendarSettings.saveCalendarFormatPatternPref(this, appWidgetId, tag, formatString);
    }

    @Override
    public String loadCalendarFormatPatternPref(int appWidgetId, String tag) {
        return CalendarSettings.loadCalendarFormatPatternPref(this, appWidgetId, tag);
    }

    @Override
    public void deleteCalendarFormatPatternPref(int appWidgetId, String tag) {
        CalendarSettings.deleteCalendarFormatPatternPref(this, appWidgetId, tag);
    }

    @Override
    public String defaultCalendarFormatPattern(String tag) {
        return CalendarSettings.defaultCalendarFormatPattern(tag);
    }

    @Override
    public void deleteCalendarPref(int appWidgetId, String key) {
        CalendarSettings.deleteCalendarPref(this, appWidgetId, key);
    }

    @Override
    public void deletePrefs(int appWidgetId) {
        CalendarSettings.deletePrefs(this, appWidgetId);
    }

    @Override
    public void initDisplayStrings() {
        CalendarSettings.initDisplayStrings(this);
    }

    @Override
    public Resources getResources() {
        return AndroidResources.wrap(context);
    }

    @Override
    public String getString(int id) {
        return context.getString(id);
    }

    @Override
    public String getString(int id, Object... formatArgs) {
        return context.getString(id, formatArgs);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int flags) {
        return AndroidSharedPreferences.wrap(context.getSharedPreferences(name, flags));
    }
}
