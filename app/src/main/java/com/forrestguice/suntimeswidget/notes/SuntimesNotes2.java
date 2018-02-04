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
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * The second implementation of SuntimesNotes; it does the same as the first (but hopefully a little cleaner).
 */
@SuppressWarnings("Convert2Diamond")
public class SuntimesNotes2 implements SuntimesNotes
{
    protected static final SuntimesUtils utils = new SuntimesUtils();

    private ArrayList<NoteData> notesList;
    private HashMap<SolarEvents, NoteData> notesMap;

    private int noteIndex = 0;
    private NoteChangedListener changedListener;
    private NoteData currentNote = null;

    private Context context;
    private SuntimesRiseSetDataset dataset;

    public SuntimesNotes2()
    {
        changedListener = new NoteChangedListener()
        {
            @Override
            public void onNoteChanged(NoteData note, int transition) { /* default: do nothing */ }
        };
    }

    @Override
    public void init(Context context, SuntimesRiseSetDataset dataset)
    {
        this.context = context;
        this.dataset = dataset;

        notesList = new ArrayList<NoteData>();
        notesMap = new HashMap<SolarEvents, NoteData>();

        for (SolarEvents event : SolarEvents.values())
        {
            NoteData note = createNote(event);
            notesList.add(note);
            notesMap.put(event, note);
        }
    }

    @Override
    public boolean isInitialized()
    {
        return (context != null);
    }

    @Override
    public int noteCount()
    {
        return notesList.size();
    }

    @Override
    public int getNoteIndex()
    {
        return noteIndex;
    }

    @Override
    public boolean setNoteIndex(int noteIndex)
    {
        if (noteIndex >=0 && noteIndex < notesList.size())
        {
            this.noteIndex = noteIndex;
            NoteData note = notesList.get(noteIndex);
            updateNote(note, dataset.now());
            setNote(note, NoteChangedListener.TRANSITION_NEXT);
            return true;
        }
        return false;
    }

    @Override
    public boolean showNote( SolarEvents.SolarEventField forField )
    {
        return false;
    }

    @Override
    public NoteData getNote()
    {
        if (currentNote != null)
        {
            updateNote(currentNote, dataset.now());
        }
        return currentNote;
    }

    @Override
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
    @Override
    public boolean showNextNote()
    {
        if (dataset.isCalculated())
        {
            //String tag;
            int nextNote, currentNote;
            SolarEvents nextNoteMode;

            Calendar now = dataset.now();

            if (dataset.isNight(now))
            {
                // show next "rising" note
                SolarEvents currentNoteMode = WidgetSettings.loadTimeNoteRisePref(context, 0);
                currentNote = currentNoteMode.ordinal();
                nextNote = 0;
                if (hasNextRiseNote(currentNote))
                    nextNote = currentNote + 1;

                //tag = "showNextRiseNote";
                nextNoteMode = SolarEvents.values()[nextNote];
                WidgetSettings.saveTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID, nextNoteMode);

            } else {
                // show next "setting" note
                SolarEvents currentNoteMode = WidgetSettings.loadTimeNoteSetPref(context, 0);
                currentNote = currentNoteMode.ordinal();
                nextNote = 4;
                if (hasNextSetNote(currentNote))
                    nextNote = currentNote + 1;

                //tag = "showNextSetNote";
                nextNoteMode = SolarEvents.values()[nextNote];
                WidgetSettings.saveTimeNoteSetPref(context, 0, nextNoteMode);
            }

            //Log.d(tag, "... current = " + currentNote + ", next = " + nextNote + ", mode = " + nextNoteMode.name());
            updateNote(context, now, NoteChangedListener.TRANSITION_NEXT);
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
    @Override
    public boolean showPrevNote()
    {
        if (dataset.isCalculated())
        {
            Calendar now = dataset.now();

            if (dataset.isNight(now))
            {
                // show previous "rising" note
                SolarEvents currentNoteMode = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                int currentNote = currentNoteMode.ordinal();

                int prevNote = 4;
                if (hasPreviousRiseNote(currentNote))
                {
                    prevNote = currentNote - 1;
                }

                SolarEvents prevNoteMode = SolarEvents.values()[prevNote];
                WidgetSettings.saveTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID, prevNoteMode);

            } else {
                // show previous "setting" note
                SolarEvents currentNoteMode = WidgetSettings.loadTimeNoteSetPref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                int currentNote = currentNoteMode.ordinal();

                int prevNote = 8;
                if (hasPreviousSetNote(currentNote))
                {
                    prevNote = currentNote - 1;
                    if (prevNote < 4)
                        prevNote = 4;
                }

                SolarEvents prevNoteMode = SolarEvents.values()[prevNote];
                WidgetSettings.saveTimeNoteSetPref(context, AppWidgetManager.INVALID_APPWIDGET_ID, prevNoteMode);
            }

            updateNote(context, now, NoteChangedListener.TRANSITION_PREV);
            return true;

        } else {
            Log.w("showPreviousNote", "called before data was calculated!");
            return false;
        }
    }

    @Override
    public boolean hasNextNote()
    {
        return true;
    }
    public boolean hasNextRiseNote( int riseOrder )
    {
        return (riseOrder < 4);
    }
    private boolean hasNextSetNote( int setOrder )
    {
        return (setOrder < 8);
    }

    @Override
    public boolean hasPrevNote()
    {
        return true;
    }

    @Override
    public void resetNoteIndex()
    {

    }

    private boolean hasPreviousRiseNote( int riseOrder )
    {
        return (riseOrder > 0);
    }
    private boolean hasPreviousSetNote( int setOrder )
    {
        return (setOrder > 4);
    }

    @Override
    public void setOnChangedListener(NoteChangedListener listener)
    {
        changedListener = listener;
    }

    @Override
    public NoteChangedListener getOnChangedListener()
    {
        return changedListener;
    }


    @Override
    public void updateNote(Context context)
    {
        updateNote(context, dataset.now(), NoteChangedListener.TRANSITION_NONE);
    }

    @Override
    public void updateNote(Context context, Calendar now)
    {
        updateNote(context, now, NoteChangedListener.TRANSITION_NONE);
    }

    /**
     * Create an empty note for a given SolarEvent.
     * @param event the SolarEvent the note will display
     * @return a note object with icon, color, untilString, and noteString set (timestring empty).
     */
    private NoteData createNote(SolarEvents event)
    {
        int noteIcon;
        int noteColor;
        String untilString;
        String noteString;

        switch (event)
        {
            case MORNING_ASTRONOMICAL:
                noteIcon = R.drawable.ic_sunrise_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_rising);
                untilString = context.getString(R.string.until);
                noteString = context.getString(R.string.until_astroTwilight);
                break;
            case MORNING_NAUTICAL:
                noteIcon = R.drawable.ic_sunrise_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_rising);
                untilString = context.getString(R.string.until);
                noteString = context.getString(R.string.until_nauticalTwilight);
                break;
            case MORNING_CIVIL:
                noteIcon = R.drawable.ic_sunrise_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_rising);
                untilString = context.getString(R.string.until);
                noteString = context.getString(R.string.until_civilTwilight);
                break;
            case SUNRISE:
                noteIcon = R.drawable.ic_sunrise_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_rising);
                untilString = context.getString(R.string.until);
                noteString = context.getString(R.string.until_sunrise);
                break;

            case NOON:
                noteIcon = R.drawable.ic_noon_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_setting);
                untilString = context.getString(R.string.until);
                noteString = context.getString(R.string.until_noon);
                break;

            case SUNSET:
                noteIcon = R.drawable.ic_sunset_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_setting);
                untilString = context.getString(R.string.until);
                noteString = context.getString(R.string.until_sunset);
                break;
            case EVENING_CIVIL:
                noteIcon = R.drawable.ic_sunset_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_setting);
                untilString = context.getString(R.string.until_end);
                noteString = context.getString(R.string.untilEnd_civilTwilight);
                break;
            case EVENING_NAUTICAL:
                noteIcon = R.drawable.ic_sunset_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_setting);
                untilString = context.getString(R.string.until_end);
                noteString = context.getString(R.string.untilEnd_nauticalTwilight);
                break;
            case EVENING_ASTRONOMICAL:
            default:
                noteIcon = R.drawable.ic_sunset_large;
                noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_setting);
                untilString = context.getString(R.string.until_end);
                noteString = context.getString(R.string.untilEnd_astroTwilight);
                break;
        }

        SuntimesUtils.TimeDisplayText timeString = new SuntimesUtils.TimeDisplayText();
        return new NoteData(event, timeString, untilString, noteString, noteIcon, noteColor);
    }

    /**
     * Update a note with respect to given time 'now'.
     * @param note the note object to be updated
     * @param now the time to update the note against
     */
    private void updateNote(NoteData note, Calendar now)
    {
        Date time = now.getTime();
        Date eventTime;
        boolean afterToday;

        switch (note.noteMode)
        {
            case MORNING_ASTRONOMICAL:
                Date morningAstro = dataset.dataAstro.sunriseCalendarToday().getTime();
                afterToday = time.after(morningAstro);
                eventTime = afterToday ? dataset.dataAstro.sunriseCalendarOther().getTime() : morningAstro;
                break;
            case MORNING_NAUTICAL:
                Date morningNautical = dataset.dataNautical.sunriseCalendarToday().getTime();
                afterToday = time.after(morningNautical);
                eventTime = afterToday ? dataset.dataNautical.sunriseCalendarOther().getTime() : morningNautical;
                break;
            case MORNING_CIVIL:
                Date morningCivil = dataset.dataCivil.sunriseCalendarToday().getTime();
                afterToday = time.after(morningCivil);
                eventTime = afterToday ? dataset.dataCivil.sunriseCalendarOther().getTime() : morningCivil;
                break;
            case SUNRISE:
                Date sunrise = dataset.dataActual.sunriseCalendarToday().getTime();
                afterToday = time.after(sunrise);
                eventTime = afterToday ? dataset.dataActual.sunriseCalendarOther().getTime() : sunrise;
                break;

            case NOON:
                Date noon = dataset.dataNoon.sunriseCalendarToday().getTime();
                afterToday = time.after(noon);
                eventTime = afterToday ? dataset.dataNoon.sunriseCalendarOther().getTime() : noon;
                break;

            case SUNSET:
                Date sunset = dataset.dataActual.sunsetCalendarToday().getTime();
                afterToday = time.after(sunset);
                eventTime = afterToday ? dataset.dataActual.sunsetCalendarOther().getTime() : sunset;
                break;
            case EVENING_CIVIL:
                Date eveningCivil = dataset.dataCivil.sunsetCalendarToday().getTime();
                afterToday = time.after(eveningCivil);
                eventTime = afterToday ? dataset.dataCivil.sunsetCalendarOther().getTime() : eveningCivil;
                break;
            case EVENING_NAUTICAL:
                Date eveningNautical = dataset.dataNautical.sunsetCalendarToday().getTime();
                afterToday = time.after(eveningNautical);
                eventTime = afterToday ? dataset.dataNautical.sunsetCalendarOther().getTime() : eveningNautical;
                break;
            case EVENING_ASTRONOMICAL:
            default:
                Date eveningAstro = dataset.dataAstro.sunsetCalendarToday().getTime();
                afterToday = time.after(eveningAstro);
                eventTime = afterToday ? dataset.dataAstro.sunsetCalendarOther().getTime() : eveningAstro;
                break;
        }

        note.timeText = utils.timeDeltaDisplayString(time, eventTime);
        note.time = eventTime;
    }

    @Override
    public void updateNote(Context context, Calendar now, int transition)
    {
        SolarEvents noteMode;

        Date time = now.getTime();
        Date sunrise = dataset.dataActual.sunriseCalendarToday().getTime();
        Date sunsetAstroTwilight = dataset.dataAstro.sunsetCalendarToday().getTime();

        boolean afterSunriseToday = time.after(sunrise);
        if (afterSunriseToday && time.before(sunsetAstroTwilight))
        {
            // a time after sunrise (but before night)

            int setChoice = WidgetSettings.loadTimeNoteSetPref(context, AppWidgetManager.INVALID_APPWIDGET_ID).ordinal();
            Date sunset = dataset.dataActual.sunsetCalendarToday().getTime();
            if (time.before(sunset) && setChoice <= SolarEvents.SUNSET.ordinal())
            {
                Date noon = dataset.dataNoon.sunriseCalendarToday().getTime();
                if (time.before(noon) && setChoice <= 0)
                {
                    // morning: note the time until noon
                    noteMode = SolarEvents.NOON;

                } else {
                    // afternoon: note the time until sunset
                    noteMode = SolarEvents.SUNSET;
                }

            } else {
                Date civilTwilight = dataset.dataCivil.sunsetCalendarToday().getTime();
                if (time.before(civilTwilight) && setChoice <= SolarEvents.EVENING_CIVIL.ordinal())
                {
                    // civil twilight: note time until end of civil twilight
                    noteMode = SolarEvents.EVENING_CIVIL;

                } else {
                    Date nauticalTwilight = dataset.dataNautical.sunsetCalendarToday().getTime();
                    if (time.before(nauticalTwilight) && setChoice <= SolarEvents.EVENING_NAUTICAL.ordinal())
                    {
                        // nautical twilight: note time until end of nautical twilight
                        noteMode = SolarEvents.EVENING_NAUTICAL;

                    } else {
                        // astronomical twilight: note time until night
                        noteMode = SolarEvents.EVENING_ASTRONOMICAL;
                    }
                }
            }

        } else {
            // a time before sunrise
            int riseChoice = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID).ordinal();
            Date astroTwilight = afterSunriseToday ? dataset.dataAstro.sunriseCalendarOther().getTime()
                                                   : dataset.dataAstro.sunriseCalendarToday().getTime();
            if (time.before(astroTwilight) && riseChoice <= SolarEvents.MORNING_ASTRONOMICAL.ordinal())
            {
                // night: note time until astro twilight today
                noteMode = SolarEvents.MORNING_ASTRONOMICAL;

            } else {
                Date nauticalTwilight = afterSunriseToday ? dataset.dataNautical.sunriseCalendarOther().getTime()
                                                          : dataset.dataNautical.sunriseCalendarToday().getTime();

                if (time.before(nauticalTwilight) && riseChoice <= SolarEvents.MORNING_NAUTICAL.ordinal())
                {
                    // astronomical twilight: note time until nautical twilight
                    noteMode = SolarEvents.MORNING_NAUTICAL;

                } else {
                    Date civilTwilight = afterSunriseToday ? dataset.dataCivil.sunriseCalendarOther().getTime()
                                                           : dataset.dataCivil.sunriseCalendarToday().getTime();
                    if (time.before(civilTwilight) && riseChoice <= SolarEvents.MORNING_CIVIL.ordinal())
                    {
                        // nautical twilight: note time until civil twilight
                        noteMode = SolarEvents.MORNING_CIVIL;

                    } else {
                        if (riseChoice <= 3)
                        {
                            // civil twilight: note time until sunrise
                            noteMode = SolarEvents.SUNRISE;

                        } else {
                            // civil twilight: note time until noon
                            noteMode = SolarEvents.NOON;
                        }
                    }
                }
            }
        }

        NoteData note = notesMap.get(noteMode);
        updateNote(note, dataset.now());
        if (currentNote == null || !currentNote.equals(note))
        {
            setNote(note, NoteChangedListener.TRANSITION_NEXT);
        }
    }

    @Override
    public void setNote(NoteData note, int transition)
    {
        currentNote = note;
        changedListener.onNoteChanged(currentNote, transition);
    }
}
