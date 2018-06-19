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
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

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

    private int colorSunrise, colorSunset, colorMoonrise, colorMoonset;

    public SuntimesNotes()
    {
        changedListener = new NoteChangedListener()
        {
            @Override
            public void onNoteChanged(NoteData note, int transition) { /* default: do nothing */ }
        };
    }

    @SuppressWarnings("ResourceType")
    private void initColors(Context context)
    {
        int[] colorAttrs = { R.attr.sunriseColor, R.attr.sunsetColor, R.attr.moonriseColor, R.attr.moonsetColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        colorSunrise = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        colorSunset = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        colorMoonrise = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        colorMoonset = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
        typedArray.recycle();
    }

    public void init(Context context, SuntimesRiseSetDataset sundata, SuntimesMoonData moondata)
    {
        this.context = context;
        this.dataset = sundata;
        this.moondata = moondata;
        SuntimesUtils.initDisplayStrings(context);

        initColors(context);

        boolean hasGoldBlue = dataset.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_GOLDBLUE);
        boolean enabledGold = AppSettings.loadGoldHourPref(context);
        boolean enabledBlue = AppSettings.loadBlueHourPref(context);

        boolean hasMoon = (moondata != null && moondata.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_MOON));
        boolean enabledMoon = AppSettings.loadShowMoonPref(context);

        notesList = new ArrayList<NoteData>();
        for (SolarEvents event : SolarEvents.values())
        {
            if ((!hasGoldBlue || !enabledGold) && (event.equals(SolarEvents.EVENING_GOLDEN) || event.equals(SolarEvents.MORNING_GOLDEN)))
                continue;
            else if ((!hasGoldBlue || !enabledBlue) && (event.equals(SolarEvents.EVENING_BLUE8) || event.equals(SolarEvents.MORNING_BLUE8) || event.equals(SolarEvents.EVENING_BLUE4) || event.equals(SolarEvents.MORNING_BLUE4)))
                continue;
            else if ((!hasMoon || !enabledMoon) && (event.equals(SolarEvents.MOONRISE) || event.equals(SolarEvents.MOONSET)))
                continue;

            NoteData note = createNote(event);
            notesList.add(note);
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
        if (dataset.isCalculated())
        {
            SolarEvents currentNoteMode = WidgetSettings.loadTimeNoteRisePref(context, 0);
            int currentNote = getNoteIndex(currentNoteMode);

            int nextNote = 0;
            if (currentNote < notesList.size() - 1)
                nextNote = currentNote + 1;

            SolarEvents nextNoteMode = notesList.get(nextNote).noteMode;
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
        if (dataset.isCalculated())
        {
            SolarEvents currentNoteMode = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
            int currentNote = getNoteIndex(currentNoteMode);

            int prevNote = notesList.size() - 1;
            if (currentNote > 0)
            {
                prevNote = currentNote - 1;
            }

            SolarEvents prevNoteMode = notesList.get(prevNote).noteMode;
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
     * @param event the SolarEvent the note will display
     * @return a note object with icon, color, untilString, and noteString set (timestring empty).
     */
    private NoteData createNote(SolarEvents event)
    {
        int[] iconAttr = { event.getIcon() };
        TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
        int def = R.drawable.ic_moon_rise;
        int noteIcon = typedArray.getResourceId(0, def);
        typedArray.recycle();

        int noteColor;
        String untilString = prefixString(event, false);
        String noteString;

        switch (event)
        {
            case MOONRISE:
                noteColor = colorMoonrise;
                noteString = context.getString(R.string.until_moonrise);
                break;

            case MOONSET:
                noteColor = colorMoonset;
                noteString = context.getString(R.string.until_moonset);
                break;

            case MORNING_ASTRONOMICAL:
                noteColor = colorSunrise;
                noteString = context.getString(R.string.until_astroTwilight);
                break;
            case MORNING_NAUTICAL:
                noteColor = colorSunrise;
                noteString = context.getString(R.string.until_nauticalTwilight);
                break;
            case MORNING_BLUE8:
                noteColor = colorSunrise;
                noteString = context.getString(R.string.until_bluehour);
                break;
            case MORNING_CIVIL:
                noteColor = colorSunrise;
                noteString = context.getString(R.string.until_civilTwilight);
                break;
            case MORNING_BLUE4:
                noteColor = colorSunrise;
                noteString = context.getString(R.string.untilEnd_bluehour);
                break;
            case SUNRISE:
                noteColor = colorSunrise;
                noteString = context.getString(R.string.until_sunrise);
                break;
            case MORNING_GOLDEN:
                noteColor = colorSunrise;
                noteString = context.getString(R.string.untilEnd_goldhour);
                break;

            case NOON:
                noteColor = colorSunset;
                noteString = context.getString(R.string.until_noon);
                break;

            case EVENING_GOLDEN:
                noteColor = colorSunset;
                noteString = context.getString(R.string.until_goldhour);
                break;
            case SUNSET:
                noteColor = colorSunset;
                noteString = context.getString(R.string.until_sunset);
                break;
            case EVENING_BLUE4:
                noteColor = colorSunset;
                noteString = context.getString(R.string.until_bluehour);
                break;
            case EVENING_CIVIL:
                noteColor = colorSunset;
                noteString = context.getString(R.string.untilEnd_civilTwilight);
                break;
            case EVENING_BLUE8:
                noteColor = colorSunset;
                noteString = context.getString(R.string.untilEnd_bluehour);
                break;
            case EVENING_NAUTICAL:
                noteColor = colorSunset;
                noteString = context.getString(R.string.untilEnd_nauticalTwilight);
                break;
            case EVENING_ASTRONOMICAL:
            default:
                noteColor = colorSunset;
                noteString = context.getString(R.string.untilEnd_astroTwilight);
                break;
        }

        SuntimesUtils.TimeDisplayText timeString = new SuntimesUtils.TimeDisplayText();
        return new NoteData(event, timeString, untilString, noteString, noteIcon, noteColor);
    }

    private String prefixString(SolarEvents event, boolean useSince)
    {
        String prefix;
        if (useSince)
        {
            prefix = context.getString(R.string.since);

        } else {
            switch (event)
            {
                case MOONRISE:
                case MOONSET:
                case MORNING_ASTRONOMICAL:          // until
                case MORNING_NAUTICAL:
                case MORNING_BLUE8:
                case EVENING_BLUE4:
                case MORNING_CIVIL:
                case SUNRISE:
                case NOON:
                case EVENING_GOLDEN:
                case SUNSET:
                    prefix = context.getString(R.string.until);
                    break;

                case MORNING_GOLDEN:               // until_end
                case EVENING_CIVIL:
                case EVENING_BLUE8:
                case MORNING_BLUE4:
                case EVENING_NAUTICAL:
                case EVENING_ASTRONOMICAL:
                default:
                    prefix = context.getString(R.string.until_end);
                    break;
            }
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
        Calendar date, dateOther;
        switch (note.noteMode)
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
            case MORNING_ASTRONOMICAL:
                date = dataset.dataAstro.sunriseCalendarToday();
                dateOther = dataset.dataAstro.sunriseCalendarOther();
                break;
            case MORNING_NAUTICAL:
                date = dataset.dataNautical.sunriseCalendarToday();
                dateOther = dataset.dataNautical.sunriseCalendarOther();
                break;
            case MORNING_BLUE8:
                date = dataset.dataBlue8.sunriseCalendarToday();
                dateOther = dataset.dataBlue8.sunriseCalendarOther();
                break;
            case MORNING_CIVIL:
                date = dataset.dataCivil.sunriseCalendarToday();
                dateOther = dataset.dataCivil.sunriseCalendarOther();
                break;
            case MORNING_BLUE4:
                date = dataset.dataBlue4.sunriseCalendarToday();
                dateOther = dataset.dataBlue4.sunriseCalendarOther();
                break;
            case SUNRISE:
                date = dataset.dataActual.sunriseCalendarToday();
                dateOther = dataset.dataActual.sunriseCalendarOther();
                break;

            case MORNING_GOLDEN:
                date = dataset.dataGold.sunriseCalendarToday();
                dateOther = dataset.dataGold.sunriseCalendarOther();
                break;
            case NOON:
                date = dataset.dataNoon.sunriseCalendarToday();
                dateOther = dataset.dataNoon.sunriseCalendarOther();
                break;
            case EVENING_GOLDEN:
                date = dataset.dataGold.sunsetCalendarToday();
                dateOther = dataset.dataGold.sunsetCalendarOther();
                break;

            case SUNSET:
                date = dataset.dataActual.sunsetCalendarToday();
                dateOther = dataset.dataActual.sunsetCalendarOther();
                break;
            case EVENING_BLUE4:
                date = dataset.dataBlue4.sunsetCalendarToday();
                dateOther = dataset.dataBlue4.sunsetCalendarOther();
                break;
            case EVENING_CIVIL:
                date = dataset.dataCivil.sunsetCalendarToday();
                dateOther = dataset.dataCivil.sunsetCalendarOther();
                break;
            case EVENING_BLUE8:
                date = dataset.dataBlue8.sunsetCalendarToday();
                dateOther = dataset.dataBlue8.sunsetCalendarOther();
                break;
            case EVENING_NAUTICAL:
                date = dataset.dataNautical.sunsetCalendarToday();
                dateOther = dataset.dataNautical.sunsetCalendarOther();
                break;
            case EVENING_ASTRONOMICAL:
            default:
                date = dataset.dataAstro.sunsetCalendarToday();
                dateOther = dataset.dataAstro.sunsetCalendarOther();
                break;
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
        SolarEvents choice = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
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

    public NoteData getNote(SolarEvents event)
    {
        int i = getNoteIndex(event);
        if (i >= 0 && i < notesList.size())
            return notesList.get(i);
        else return null;
    }

    public int getNoteIndex(SolarEvents event)
    {
        for (int i=0; i< notesList.size(); i++)
        {
            NoteData note = notesList.get(i);
            if (note.noteMode.equals(event))
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
