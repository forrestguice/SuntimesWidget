package com.forrestguice.suntimeswidget.calculator.settings.android;

import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.calendar.CalendarSettingsInterface;

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
        CalendarSettings.saveCalendarFlag(context, appWidgetId, key, value);
    }

    @Override
    public boolean loadCalendarFlag(int appWidgetId, String key, boolean defValue) {
        return CalendarSettings.loadCalendarFlag(context, appWidgetId, key, defValue);
    }

    @Override
    public void saveCalendarModePref(int appWidgetId, CalendarMode mode) {
        CalendarSettings.saveCalendarModePref(context, appWidgetId, mode);
    }

    @Override
    public CalendarMode loadCalendarModePref(int appWidgetId) {
        return CalendarSettings.loadCalendarModePref(context, appWidgetId);
    }

    @Override
    public void saveCalendarFormatPatternPref(int appWidgetId, String tag, String formatString) {
        CalendarSettings.saveCalendarFormatPatternPref(context, appWidgetId, tag, formatString);
    }

    @Override
    public String loadCalendarFormatPatternPref(int appWidgetId, String tag) {
        return CalendarSettings.loadCalendarFormatPatternPref(context, appWidgetId, tag);
    }

    @Override
    public void deleteCalendarFormatPatternPref(int appWidgetId, String tag) {
        CalendarSettings.deleteCalendarFormatPatternPref(context, appWidgetId, tag);
    }

    @Override
    public String defaultCalendarFormatPattern(String tag) {
        return CalendarSettings.defaultCalendarFormatPattern(tag);
    }

    @Override
    public void deleteCalendarPref(int appWidgetId, String key) {
        CalendarSettings.deleteCalendarPref(context, appWidgetId, key);
    }

    @Override
    public void deletePrefs(int appWidgetId) {
        CalendarSettings.deletePrefs(context, appWidgetId);
    }

    @Override
    public void initDisplayStrings() {
        CalendarSettings.initDisplayStrings(context);
    }
}
