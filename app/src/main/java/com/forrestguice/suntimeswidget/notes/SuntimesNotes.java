/**
    Copyright (C) 2014-2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.notes;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.res.TypedArray;
import com.forrestguice.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.cards.CardColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

@SuppressWarnings("Convert2Diamond")
public class SuntimesNotes
{
    protected static final SuntimesUtils utils = new SuntimesUtils();

    private ArrayList<NoteData> notesList;

    private int noteIndex = 0;
    private NoteChangedListener changedListener;
    private NoteData currentNote = null;

    private Context context;
    private SuntimesRiseSetDataset dataset;
    private SuntimesMoonData moondata;         // may be null

    private CardColorValues colors;
    //private int colorSunrise, colorSunriseStroke;
    //private int colorSunset, colorSunsetStroke;
    //private int colorMoonrise, colorMoonset;
    //private int colorNoon, colorNoonStroke;
    private int strokeWidthRising, strokeWidthSetting, strokeWidthNoon;

    public SuntimesNotes(Context context)
    {
        colors = new CardColorValues(context);
        changedListener = new NoteChangedListener()
        {
            @Override
            public void onNoteChanged(NoteData note, int transition) { /* default: do nothing */ }
        };
    }

    public void setColors(Context context, @Nullable ColorValues values)
    {
        if (values != null ) {
            colors = new CardColorValues(values);
        } else {
            themeViews(context);
        }
    }

    @SuppressWarnings("ResourceType")
    private void themeViews(Context context)
    {
        if (themeOverride == null)
        {
            strokeWidthRising = strokeWidthSetting = 0;
            strokeWidthNoon = context.getResources().getDimensionPixelSize(R.dimen.noonIcon_width_border);

            //int[] colorAttrs = { R.attr.table_risingColor, R.attr.table_settingColor, R.attr.table_moonRisingColor, R.attr.table_moonSettingColor };
            //TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            //int def = R.color.transparent;
            //colorSunrise = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
            //colorSunset = colorNoon = colorNoonStroke = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
            //colorMoonrise = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
            //colorMoonset = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
            //typedArray.recycle();

        } else {
            colors.setColor(CardColorValues.COLOR_RISING_SUN, themeOverride.getSunriseIconColor());
            colors.setColor(CardColorValues.COLOR_RISING_SUN_TEXT, themeOverride.getSunriseTextColor());
            //colorSunriseStroke = themeOverride.getSunriseIconStrokeColor());
            colors.setColor(CardColorValues.COLOR_SETTING_SUN, themeOverride.getSunsetIconColor());
            colors.setColor(CardColorValues.COLOR_SETTING_SUN_TEXT, themeOverride.getSunsetTextColor());
            //colorSunsetStroke = themeOverride.getSunsetIconStrokeColor();
            //colorNoon = themeOverride.getNoonIconColor();
            //colorNoonStroke = themeOverride.getNoonIconStrokeColor();
            strokeWidthNoon = themeOverride.getNoonIconStrokePixels(context);
            strokeWidthRising = themeOverride.getSunriseIconStrokePixels(context);
            strokeWidthSetting = themeOverride.getSunsetIconStrokePixels(context);
            colors.setColor(CardColorValues.COLOR_RISING_MOON, themeOverride.getMoonriseTextColor());
            colors.setColor(CardColorValues.COLOR_RISING_MOON_TEXT, themeOverride.getMoonriseTextColor());
            colors.setColor(CardColorValues.COLOR_SETTING_MOON, themeOverride.getMoonsetTextColor());
            colors.setColor(CardColorValues.COLOR_SETTING_MOON_TEXT, themeOverride.getMoonsetTextColor());
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
        }
    }

    public void init(Context context, SuntimesRiseSetDataset sundata, SuntimesMoonData moondata)
    {
        this.context = context;
        this.dataset = sundata;
        this.moondata = moondata;
        SuntimesUtils.initDisplayStrings(context);

        themeViews(context);

        boolean[] showFields = AppSettings.loadShowFieldsPref(context);
        boolean enabledActual = showFields[AppSettings.FIELD_ACTUAL];
        boolean enabledCivil = showFields[AppSettings.FIELD_CIVIL];
        boolean enabledNautical = showFields[AppSettings.FIELD_NAUTICAL];
        boolean enabledAstro = showFields[AppSettings.FIELD_ASTRO];
        boolean enabledNoon = showFields[AppSettings.FIELD_NOON];

        boolean hasGoldBlue = dataset.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_GOLDBLUE);
        boolean enabledGold = showFields[AppSettings.FIELD_GOLD];
        boolean enabledBlue = showFields[AppSettings.FIELD_BLUE];

        boolean hasMoon = (moondata != null && moondata.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_MOON));
        boolean enabledMoon = AppSettings.loadShowMoonPref(context);
        boolean enabledLunarNoon = AppSettings.loadShowLunarNoonPref(context);

        notesList = new ArrayList<NoteData>();
        for (SolarEvents event : SolarEvents.values())
        {
            if ((!hasGoldBlue || !enabledGold) && (event.equals(SolarEvents.EVENING_GOLDEN) || event.equals(SolarEvents.MORNING_GOLDEN)))
                continue;
            else if ((!hasGoldBlue || !enabledBlue) && (event.equals(SolarEvents.EVENING_BLUE8) || event.equals(SolarEvents.MORNING_BLUE8) || event.equals(SolarEvents.EVENING_BLUE4) || event.equals(SolarEvents.MORNING_BLUE4)))
                continue;
            else if ((!hasMoon || !enabledMoon) && (event.equals(SolarEvents.MOONRISE) || event.equals(SolarEvents.MOONSET) || event.equals(SolarEvents.MOONNOON) || event.equals(SolarEvents.MOONNIGHT)))
                continue;
            else if ((!enabledLunarNoon) && (event.equals(SolarEvents.MOONNOON) || event.equals(SolarEvents.MOONNIGHT)))
                continue;
            else if (!enabledNoon && (event.equals(SolarEvents.NOON)))
                continue;
            else if (!enabledAstro && (event.equals(SolarEvents.EVENING_ASTRONOMICAL) || event.equals(SolarEvents.MORNING_ASTRONOMICAL)))
                continue;
            else if (!enabledNautical && (event.equals(SolarEvents.EVENING_NAUTICAL) || event.equals(SolarEvents.MORNING_NAUTICAL)))
                continue;
            else if (!enabledCivil && (event.equals(SolarEvents.EVENING_CIVIL) || event.equals(SolarEvents.MORNING_CIVIL)))
                continue;
            else if (!enabledActual && (event.equals(SolarEvents.SUNSET) || event.equals(SolarEvents.SUNRISE)))
                continue;
            else if (event.equals(SolarEvents.FULLMOON) || event.equals(SolarEvents.NEWMOON) || event.equals(SolarEvents.FIRSTQUARTER) || event.equals(SolarEvents.THIRDQUARTER))
                continue;
            else if (event.equals(SolarEvents.EQUINOX_SPRING) || event.equals(SolarEvents.SOLSTICE_SUMMER) || event.equals(SolarEvents.EQUINOX_AUTUMNAL) || event.equals(SolarEvents.SOLSTICE_WINTER))
                continue;
            else if (event.equals(SolarEvents.CROSS_SPRING) || event.equals(SolarEvents.CROSS_SUMMER) || event.equals(SolarEvents.CROSS_AUTUMNAL) || event.equals(SolarEvents.CROSS_WINTER))
                continue;

            NoteData note = createNote(event.name());
            notesList.add(note);
        }

        for (String eventID : EventSettings.loadVisibleEvents(context)) {
            notesList.add(createNote(eventID + "_" + AlarmEventProvider.ElevationEvent.SUFFIX_RISING));
            notesList.add(createNote(eventID + "_" + AlarmEventProvider.ElevationEvent.SUFFIX_SETTING));
        }

        updateNotes(dataset.now());
    }

    public boolean isInitialized()
    {
        return (context != null);
    }

    public int noteCount()
    {
        return notesList.size();
    }

    public int getNoteIndex()
    {
        if (currentNote != null)
        {
            return notesList.indexOf(currentNote);
        }
        return noteIndex;
    }

    public boolean setNoteIndex(int noteIndex)
    {
        if (noteIndex >=0 && noteIndex < notesList.size())
        {
            this.noteIndex = noteIndex;
            NoteData note = notesList.get(noteIndex);
            updateNote(note, dataset.now());
            setNote(note, NoteChangedListener.TRANSITION_NEXT);
            WidgetSettings.saveTimeNoteRisePref(context, 0, note.noteMode);
            return true;
        }
        return false;
    }

    public boolean showNote( SolarEvents.SolarEventField forField )
    {
        return false;
    }

    @Nullable
    public NoteData getNote()
    {
        if (currentNote != null)
        {
            updateNote(currentNote, dataset.now());
        }
        return currentNote;
    }

    public NoteData getNote(int noteIndex)
    {
        if (noteIndex >=0 && noteIndex < notesList.size())
        {
            NoteData note = notesList.get(noteIndex);
            updateNote(note, dataset.now());
            return note;
        }
        return null;
    }

    /**
     * Switch to the next note (in ordered set of notes).
     * @return true if the note was changed, false otherwise
     */
    public boolean showNextNote()
    {
        if (notesList.size() <= 0)
            return false;

        if (dataset.isCalculated())
        {
            String currentNoteMode = WidgetSettings.loadTimeNoteRisePref(context, 0);
            int currentNote = getNoteIndex(currentNoteMode);

            int nextNote = 0;
            if (currentNote < notesList.size() - 1) {
                nextNote = currentNote + 1;
            }

            String nextNoteMode = notesList.get(nextNote).noteMode;
            WidgetSettings.saveTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID, nextNoteMode);

            //Log.d("showNextNote", "... current = " + currentNote + ", next = " + nextNote + ", mode = " + nextNoteMode.name());
            updateNote(context, dataset.now(), NoteChangedListener.TRANSITION_NEXT);
            return true;

        } else {
            Log.w("showNextNote", "called before data was calculated!");
            return false;
        }
    }

    /**
     * Switch to the previous note (in ordered set of notes).
     * @return true if the note was changed, false otherwise
     */
    public boolean showPrevNote()
    {
        if (notesList.size() <= 0)
            return false;

        if (dataset.isCalculated())
        {
            String currentNoteMode = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
            int currentNote = getNoteIndex(currentNoteMode);

            int prevNote = notesList.size() - 1;
            if (currentNote > 0) {
                prevNote = currentNote - 1;
            }

            String prevNoteMode = notesList.get(prevNote).noteMode;
            WidgetSettings.saveTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID, prevNoteMode);
            updateNote(context, dataset.now(), NoteChangedListener.TRANSITION_PREV);
            return true;

        } else {
            Log.w("showPreviousNote", "called before data was calculated!");
            return false;
        }
    }

    public boolean hasNextNote()
    {
        return noteIndex < notesList.size() - 1;
    }

    public boolean hasPrevNote()
    {
        return noteIndex > 0;
    }

    public void setOnChangedListener(NoteChangedListener listener)
    {
        changedListener = listener;
    }

    public NoteChangedListener getOnChangedListener()
    {
        return changedListener;
    }

    public void updateNotes(Calendar now)
    {
        for (NoteData note : notesList)
        {
            updateNote(note, now);
        }

        Collections.sort(notesList, new Comparator<NoteData>()
        {
            @Override
            public int compare(NoteData o1, NoteData o2)
            {
                boolean o1Null = (o1 == null || o1.time == null);
                boolean o2Null = (o2 == null || o2.time == null);

                if (o1Null && o2Null)
                    return 0;

                else if (o1Null)
                    return -1;

                else if (o2Null)
                    return 1;

                else return o1.time.compareTo(o2.time);
            }
        });
    }

    /**
     * Create an empty note for a given SolarEvent.
     * @param eventID the SolarEvent the note will display
     * @return a note object with icon, color, untilString, and noteString set (timestring empty).
     */
    private NoteData createNote(String eventID)
    {
        //Log.d("DEBUG", "createNote: " + eventID);
        int iconStroke = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
        int noteIcon = R.drawable.ic_moon_rise;
        int textColor = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
        int iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
        int iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);  // _STROKE
        String untilString = prefixString(eventID, false);
        String noteString = "";

        if (SolarEvents.hasValue(eventID))
        {
            SolarEvents event = SolarEvents.valueOf(eventID);

            int[] iconAttr = { event.getIcon() };
            TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
            noteIcon = typedArray.getResourceId(0, R.drawable.ic_moon_rise);
            typedArray.recycle();

            switch (event)
            {
                case MOONRISE:
                    iconStroke = strokeWidthRising;
                    iconColor = iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_MOON);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_MOON_TEXT);
                    noteString = context.getString(R.string.until_moonrise);
                    break;

                case MOONSET:
                    iconStroke = strokeWidthSetting;
                    iconColor = iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_MOON);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_MOON_TEXT);
                    noteString = context.getString(R.string.until_moonset);
                    break;

                case MOONNOON:
                    iconStroke = strokeWidthNoon;
                    iconColor = iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_MOON);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_MOON_TEXT);
                    noteString = context.getString(R.string.until_moonnoon);
                    break;

                case MOONNIGHT:
                    iconStroke = strokeWidthNoon;
                    iconColor = colors.getColor(CardColorValues.COLOR_SETTING_MOON);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_MOON);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_MOON_TEXT);
                    noteString = context.getString(R.string.until_moonnight);
                    break;

                case MORNING_ASTRONOMICAL:
                    iconStroke = strokeWidthRising;
                    iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
                    noteString = context.getString(R.string.until_astroTwilight);
                    break;
                case MORNING_NAUTICAL:
                    iconStroke = strokeWidthRising;
                    iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
                    noteString = context.getString(R.string.until_nauticalTwilight);
                    break;
                case MORNING_BLUE8:
                    iconStroke = strokeWidthRising;
                    iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
                    noteString = context.getString(R.string.until_bluehour);
                    break;
                case MORNING_CIVIL:
                    iconStroke = strokeWidthRising;
                    iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
                    noteString = context.getString(R.string.until_civilTwilight);
                    break;
                case MORNING_BLUE4:
                    iconStroke = strokeWidthRising;
                    iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
                    noteString = context.getString(R.string.untilEnd_bluehour);
                    break;
                case SUNRISE:
                    iconStroke = strokeWidthRising;
                    iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
                    noteString = context.getString(R.string.until_sunrise);
                    break;
                case MORNING_GOLDEN:
                    iconStroke = strokeWidthRising;
                    iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
                    noteString = context.getString(R.string.untilEnd_goldhour);
                    break;

                case NOON:
                    iconStroke = strokeWidthNoon;
                    iconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);
                    noteString = context.getString(R.string.until_noon);
                    break;

                case EVENING_GOLDEN:
                    iconStroke = strokeWidthSetting;
                    iconColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);
                    noteString = context.getString(R.string.until_goldhour);
                    break;
                case SUNSET:
                    iconStroke = strokeWidthSetting;
                    iconColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);
                    noteString = context.getString(R.string.until_sunset);
                    break;
                case EVENING_BLUE4:
                    iconStroke = strokeWidthSetting;
                    iconColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);
                    noteString = context.getString(R.string.until_bluehour);
                    break;
                case EVENING_CIVIL:
                    iconStroke = strokeWidthSetting;
                    iconColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);
                    noteString = context.getString(R.string.untilEnd_civilTwilight);
                    break;
                case EVENING_BLUE8:
                    iconStroke = strokeWidthSetting;
                    iconColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);
                    noteString = context.getString(R.string.untilEnd_bluehour);
                    break;
                case EVENING_NAUTICAL:
                    iconStroke = strokeWidthSetting;
                    iconColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);
                    noteString = context.getString(R.string.untilEnd_nauticalTwilight);
                    break;
                case EVENING_ASTRONOMICAL:
                default:
                    iconStroke = strokeWidthSetting;
                    iconColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    iconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
                    textColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);
                    noteString = context.getString(R.string.untilEnd_astroTwilight);
                    break;
            }

        } else {
            boolean isRising = eventID.endsWith(AlarmEventProvider.ElevationEvent.SUFFIX_RISING);
            String eventID0 = new String(eventID);
            if (eventID0.endsWith("_" + AlarmEventProvider.ElevationEvent.SUFFIX_RISING) ||
                eventID0.endsWith("_" + AlarmEventProvider.ElevationEvent.SUFFIX_SETTING)) {
                eventID0 = eventID0.substring(0, eventID0.lastIndexOf("_"));
            }

            if (EventSettings.hasEvent(context, eventID0))
            {
                EventSettings.EventAlias event = EventSettings.loadEvent(context, eventID0);
                if (event != null)
                {
                    int[] iconAttr = { R.attr.sunriseIconLarge, R.attr.sunsetIconLarge };
                    TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
                    noteIcon = typedArray.getResourceId((isRising ? 0 : 1), R.drawable.ic_moon_rise);
                    typedArray.recycle();

                    iconStroke = strokeWidthSetting;
                    noteString = event.getLabel();
                    iconColor = iconColor2 = textColor = event.getColor();
                }
            }
        }

        SuntimesUtils.TimeDisplayText timeString = new SuntimesUtils.TimeDisplayText();
        return new NoteData(eventID, timeString, untilString, noteString, noteIcon, textColor, iconColor, iconColor2, iconStroke);
    }

    private String prefixString(String eventID, boolean useSince)
    {
        String prefix;
        if (useSince)
        {
            prefix = context.getString(R.string.since);

        } else if (SolarEvents.hasValue(eventID)) {
            SolarEvents event = SolarEvents.valueOf(eventID);
            switch (event)
            {
                // until
                case MOONRISE: case MOONSET: case MOONNOON: case MOONNIGHT:
                case MORNING_ASTRONOMICAL: case MORNING_NAUTICAL: case MORNING_BLUE8: case EVENING_BLUE4: case MORNING_CIVIL:
                case SUNRISE: case NOON: case EVENING_GOLDEN: case SUNSET:
                    prefix = context.getString(R.string.until);
                    break;

                // until_end
                case MORNING_GOLDEN: case EVENING_CIVIL: case EVENING_BLUE8: case MORNING_BLUE4: case EVENING_NAUTICAL: case EVENING_ASTRONOMICAL:
                default:
                    prefix = context.getString(R.string.until_end);
                    break;
            }

        } else {
            prefix = context.getString(R.string.until);  // TODO
        }
        return prefix;
    }

    /**
     * Update a note with respect to given time 'now'.
     * @param note the note object to be updated
     * @param now the time to update the note against
     */
    private void updateNote(NoteData note, Calendar now)
    {
        Calendar date = null, dateOther = null;

        if (SolarEvents.hasValue(note.noteMode))
        {
            SolarEvents event = SolarEvents.valueOf(note.noteMode);
            switch (event)
            {
                case MOONRISE:
                    if (moondata == null) {
                        return;
                    }
                    date = moondata.moonriseCalendarToday();
                    dateOther = moondata.moonriseCalendarTomorrow();
                    break;
                case MOONSET:
                    if (moondata == null) {
                        return;
                    }
                    date = moondata.moonsetCalendarToday();
                    dateOther = moondata.moonsetCalendarTomorrow();
                    break;

                case MOONNOON:
                    if (moondata == null) {
                        return;
                    }
                    date = moondata.getLunarNoonToday();
                    dateOther = moondata.getLunarNoonTomorrow();
                    break;
                case MOONNIGHT:
                    if (moondata == null) {
                        return;
                    }
                    date = moondata.getLunarMidnightToday();
                    dateOther = moondata.getLunarMidnightTomorrow();
                    break;

                case MORNING_ASTRONOMICAL: case MORNING_NAUTICAL: case MORNING_BLUE8: case MORNING_CIVIL: case MORNING_BLUE4:
                case SUNRISE: case MORNING_GOLDEN: case NOON: case EVENING_GOLDEN:
                case SUNSET: case EVENING_BLUE4: case EVENING_CIVIL: case EVENING_BLUE8: case EVENING_NAUTICAL: case EVENING_ASTRONOMICAL:
                default:
                    WidgetSettings.TimeMode mode = SolarEvents.toTimeMode(event);
                    if (mode != null)
                    {
                        SuntimesRiseSetData d = dataset.getData(mode.name());
                        if (d != null) {
                            date = (event.isRising() ? d.sunriseCalendarToday() : d.sunsetCalendarToday());
                            dateOther = (event.isRising() ? d.sunriseCalendarOther() : d.sunsetCalendarOther());
                        }
                    }
                    break;
            }

        } else {
            String eventID = note.noteMode;
            boolean isRising = eventID.endsWith(AlarmEventProvider.ElevationEvent.SUFFIX_RISING);
            if (eventID.endsWith("_" + AlarmEventProvider.ElevationEvent.SUFFIX_RISING) ||
                    eventID.endsWith("_" + AlarmEventProvider.ElevationEvent.SUFFIX_SETTING)) {
                eventID = eventID.substring(0, eventID.lastIndexOf("_"));
            }

            SuntimesRiseSetData d = dataset.getData(eventID);
            if (d != null) {
                date = (isRising ? d.sunriseCalendarToday() : d.sunsetCalendarToday());
                dateOther = (isRising ? d.sunriseCalendarOther() : d.sunsetCalendarOther());
            }
        }

        Date eventTime = null;
        Date time = now.getTime();

        boolean afterToday = (date == null || time.after(date.getTime()));
        if (afterToday)
        {
            if (dateOther != null)
            {
                eventTime = dateOther.getTime();
            }
        } else {
            eventTime = date.getTime();
        }

        note.tomorrow = afterToday;
        note.timeText = utils.timeDeltaDisplayString(time, eventTime);
        note.prefixText = prefixString(note.noteMode, (note.timeText.getRawValue() < 0));
        note.time = eventTime;
    }

    public void resetNoteIndex()
    {
        if (notesList.size() <= 0)
            return;

        Calendar now = dataset.now();
        Date time = now.getTime();
        long nearestTime = -1;

        NoteData nearestNote = notesList.get(0);
        for (NoteData note : notesList)
        {
            if (note.time != null)
            {
                long timeUntil = note.time.getTime() - time.getTime();
                if ((timeUntil > 0 && timeUntil < nearestTime) || nearestTime < 0)
                {
                    nearestTime = timeUntil;
                    nearestNote = note;
                }
            }
        }

        //Log.d("DEBUG", "note reset to " + nearestNote.noteMode);
        WidgetSettings.saveTimeNoteRisePref(context, 0, nearestNote.noteMode);
    }


    public void updateNote(Context context)
    {
        updateNote(context, dataset.now(), NoteChangedListener.TRANSITION_NONE);
    }

    public void updateNote(Context context, Calendar now)
    {
        updateNote(context, now, NoteChangedListener.TRANSITION_NONE);
    }

    public void updateNote(Context context, Calendar now, int transition)
    {
        String choice = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
        NoteData chosenNote = getNote(choice);

        if (chosenNote != null)
        {
            NoteData updatedNote = new NoteData(chosenNote);
            updateNote(updatedNote, now);

            if (currentNote == null || !currentNote.equals(updatedNote)) {
                //Log.d("updateNote", "changing the note to " + updatedNote.toString() + "[" + choice + "]");
                setNote(updatedNote, NoteChangedListener.TRANSITION_NEXT);
            }
        }
    }

    public NoteData getNote(String eventID)
    {
        //Log.d("DEBUG", "getNote: " + eventID);
        int i = getNoteIndex(eventID);
        if (i >= 0 && i < notesList.size())
            return notesList.get(i);
        else return null;
    }

    public int getNoteIndex(String eventID)
    {
        //Log.d("DEBUG", "getNoteIndex: " + eventID);
        for (int i=0; i< notesList.size(); i++)
        {
            NoteData note = notesList.get(i);
            if (note.noteMode.equals(eventID))
                return i;
        }
        return -1;
    }

    public void setNote(NoteData note, int transition)
    {
        currentNote = note;
        changedListener.onNoteChanged(currentNote, transition);
    }
}
