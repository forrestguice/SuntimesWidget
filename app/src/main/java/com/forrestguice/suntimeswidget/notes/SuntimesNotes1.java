/**
    Copyright (C) 2014 Forrest Guice
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
import com.forrestguice.suntimeswidget.calculator.SuntimesDataset;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.Date;

/**
 * The first implementation of SuntimesNotes; needs to be renamed badly.
 */
public class SuntimesNotes1 implements SuntimesNotes
{
    protected static SuntimesUtils utils = new SuntimesUtils();

    private int noteIndex = 0;
    private NoteChangedListener changedListener;
    private NoteData currentNote = null;

    private Context context;
    private SuntimesDataset dataset;

    public SuntimesNotes1()
    {
        changedListener = new NoteChangedListener()
        {
            @Override
            public void onNoteChanged(NoteData note, int transition) { /* default: do nothing */ }
        };
    }

    @Override
    public void init(Context context, SuntimesDataset dataset)
    {
        this.context = context;
        this.dataset = dataset;
    }

    @Override
    public boolean isInitialized()
    {
        return (context != null);
    }

    @Override
    public int noteCount()
    {
        return 0;
    }

    @Override
    public int getNoteIndex()
    {
        return noteIndex;
    }

    @Override
    public boolean setNoteIndex(int noteIndex)
    {
        return false;
    }

    @Override
    public NoteData getNote()
    {
        return currentNote;
    }

    @Override
    public NoteData getNote(int noteIndex)
    {
        if (noteIndex >=0 && noteIndex < noteCount())
        {
            // TODO
            return null;
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
            Calendar now = dataset.now();
            Date time = now.getTime();

            if (dataset.isNight(time))
            {
                // show next "rising" note
                WidgetSettings.TimeMode currentNoteMode = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                int currentNote = currentNoteMode.getRiseOrder();

                int nextNote = 0;
                if (hasNextRiseNote(currentNote))
                {
                    nextNote = currentNote + 1;
                }

                WidgetSettings.TimeMode nextNoteMode = WidgetSettings.TimeMode.getModeForRiseOrder(nextNote);
                WidgetSettings.saveTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID, nextNoteMode);

                Log.d("showNextRiseNote", "... current = " + currentNote + ", next = " + nextNote + ", mode = " + nextNoteMode.name());

            } else {
                // show next "setting" note
                WidgetSettings.TimeMode currentNoteMode = WidgetSettings.loadTimeNoteSetPref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                int currentNote = currentNoteMode.getSetOrder();

                int nextNote = 0;
                if (hasNextSetNote(currentNote))
                {
                    nextNote = currentNote + 1;
                }

                WidgetSettings.TimeMode nextNoteMode = WidgetSettings.TimeMode.getModeForSetOrder(nextNote);
                WidgetSettings.saveTimeNoteSetPref(context, AppWidgetManager.INVALID_APPWIDGET_ID, nextNoteMode);

                Log.d("showNextSetNote", "... current = " + currentNote + ", next = " + nextNote + ", mode = " + nextNoteMode.name());
            }

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
            Date time = now.getTime();

            if (dataset.isNight(time))
            {
                // show previous "rising" note
                WidgetSettings.TimeMode currentNoteMode = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                int currentNote = currentNoteMode.getRiseOrder();

                int prevNote = WidgetSettings.TimeMode.NOON.getRiseOrder();
                if (hasPreviousRiseNote(currentNote))
                {
                    prevNote = currentNote - 1;
                }

                WidgetSettings.TimeMode prevNoteMode = WidgetSettings.TimeMode.getModeForRiseOrder(prevNote);
                WidgetSettings.saveTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID, prevNoteMode);

            } else {
                // show previous "setting" note
                WidgetSettings.TimeMode currentNoteMode = WidgetSettings.loadTimeNoteSetPref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                int currentNote = currentNoteMode.getSetOrder();

                int prevNote = WidgetSettings.TimeMode.ASTRONOMICAL.getSetOrder();
                if (hasPreviousSetNote(currentNote))
                {
                    prevNote = currentNote - 1;
                    if (prevNote < 0)
                        prevNote = 0;
                }

                WidgetSettings.TimeMode prevNoteMode = WidgetSettings.TimeMode.getModeForSetOrder(prevNote);
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
        return (riseOrder < WidgetSettings.TimeMode.NOON.getRiseOrder());
    }
    private boolean hasNextSetNote( int setOrder )
    {
        return (setOrder < WidgetSettings.TimeMode.ASTRONOMICAL.getSetOrder());
    }

    @Override
    public boolean hasPrevNote()
    {
        return true;
    }
    private boolean hasPreviousRiseNote( int riseOrder )
    {
        return (riseOrder > 0);
    }
    private boolean hasPreviousSetNote( int setOrder )
    {
        return (setOrder > 0);
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
    public void updateNote(Context context, Calendar now)
    {
        updateNote(context, now, NoteChangedListener.TRANSITION_NONE);
    }

    @Override
    public void updateNote(Context context, Calendar now, int transition)
    {
        SolarEvents noteMode;
        int noteIcon, noteColor;
        SuntimesUtils.TimeDisplayText timeString;
        String noteString, untilString;
        long timestamp;

        Date time = now.getTime();
        Date sunrise = dataset.dataActual.sunriseCalendarToday().getTime();
        Date sunsetAstroTwilight = dataset.dataAstro.sunsetCalendarToday().getTime();

        boolean afterSunriseToday = time.after(sunrise);
        if (afterSunriseToday && time.before(sunsetAstroTwilight))
        {
            // a time after sunrise (but before night)
            noteIcon = R.drawable.ic_sunset_large;
            noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_setting);

            int setChoice = WidgetSettings.loadTimeNoteSetPref(context, AppWidgetManager.INVALID_APPWIDGET_ID).getSetOrder();
            Date sunset = dataset.dataActual.sunsetCalendarToday().getTime();
            if (time.before(sunset) && setChoice <= WidgetSettings.TimeMode.OFFICIAL.getSetOrder())
            {
                untilString = context.getString(R.string.until);

                Date noon = dataset.dataNoon.sunriseCalendarToday().getTime();
                if (time.before(noon) && setChoice <= WidgetSettings.TimeMode.NOON.getSetOrder())
                {
                    // morning: note the time until noon
                    noteIcon = R.drawable.ic_noon_large;
                    timestamp = noon.getTime();
                    noteMode = SolarEvents.NOON;
                    timeString = utils.timeDeltaDisplayString(time, noon);
                    noteString = context.getString(R.string.until_noon);

                } else {
                    // afternoon: note the time until sunset
                    timestamp = sunset.getTime();
                    noteMode = SolarEvents.SUNSET;
                    timeString = utils.timeDeltaDisplayString(time, sunset);
                    noteString = context.getString(R.string.until_sunset);
                }

            } else {
                untilString = context.getString(R.string.until_end);

                Date civilTwilight = dataset.dataCivil.sunsetCalendarToday().getTime();
                if (time.before(civilTwilight) && setChoice <= WidgetSettings.TimeMode.CIVIL.getSetOrder())
                {
                    // civil twilight: note time until end of civil twilight
                    timestamp = civilTwilight.getTime();
                    noteMode = SolarEvents.EVENING_CIVIL;
                    timeString = utils.timeDeltaDisplayString(time, civilTwilight);
                    noteString = context.getString(R.string.untilEnd_civilTwilight);

                } else {
                    Date nauticalTwilight = dataset.dataNautical.sunsetCalendarToday().getTime();
                    if (time.before(nauticalTwilight) && setChoice <= WidgetSettings.TimeMode.NAUTICAL.getSetOrder())
                    {
                        // nautical twilight: note time until end of nautical twilight
                        timestamp = nauticalTwilight.getTime();
                        noteMode = SolarEvents.EVENING_NAUTICAL;
                        timeString = utils.timeDeltaDisplayString(time, nauticalTwilight);
                        noteString = context.getString(R.string.untilEnd_nauticalTwilight);

                    } else {
                        // astronomical twilight: note time until night
                        timestamp = sunsetAstroTwilight.getTime();
                        noteMode = SolarEvents.EVENING_ASTRONOMICAL;
                        timeString = utils.timeDeltaDisplayString(time, sunsetAstroTwilight);
                        noteString = context.getString(R.string.untilEnd_astroTwilight);
                    }
                }
            }

        } else {
            // a time before sunrise
            noteIcon = R.drawable.ic_sunrise_large;
            untilString = context.getString(R.string.until);
            noteColor = ContextCompat.getColor(context, R.color.sunIcon_color_rising);

            int riseChoice = WidgetSettings.loadTimeNoteRisePref(context, AppWidgetManager.INVALID_APPWIDGET_ID).getRiseOrder();
            Date astroTwilight = afterSunriseToday ? dataset.dataAstro.sunriseCalendarOther().getTime()
                    : dataset.dataAstro.sunriseCalendarToday().getTime();
            if (time.before(astroTwilight) && riseChoice <= WidgetSettings.TimeMode.ASTRONOMICAL.getRiseOrder())
            {
                // night: note time until astro twilight today
                timestamp = astroTwilight.getTime();
                noteMode = SolarEvents.MORNING_ASTRONOMICAL;
                timeString = utils.timeDeltaDisplayString(time, astroTwilight);
                noteString = context.getString(R.string.until_astroTwilight);

            } else {
                Date nauticalTwilight = afterSunriseToday ? dataset.dataNautical.sunriseCalendarOther().getTime()
                        : dataset.dataNautical.sunriseCalendarToday().getTime();

                if (time.before(nauticalTwilight) && riseChoice <= WidgetSettings.TimeMode.NAUTICAL.getRiseOrder())
                {
                    // astronomical twilight: note time until nautical twilight
                    timestamp = nauticalTwilight.getTime();
                    noteMode = SolarEvents.MORNING_NAUTICAL;
                    timeString = utils.timeDeltaDisplayString(time, nauticalTwilight);
                    noteString = context.getString(R.string.until_nauticalTwilight);

                } else {
                    Date civilTwilight = afterSunriseToday ? dataset.dataCivil.sunriseCalendarOther().getTime()
                            : dataset.dataCivil.sunriseCalendarToday().getTime();
                    if (time.before(civilTwilight) && riseChoice <= WidgetSettings.TimeMode.CIVIL.getRiseOrder())
                    {
                        // nautical twilight: note time until civil twilight
                        timestamp = civilTwilight.getTime();
                        noteMode = SolarEvents.MORNING_CIVIL;
                        timeString = utils.timeDeltaDisplayString(time, civilTwilight);
                        noteString = context.getString(R.string.until_civilTwilight);

                    } else {
                        if (riseChoice <= WidgetSettings.TimeMode.OFFICIAL.getRiseOrder())
                        {
                            // civil twilight: note time until sunrise
                            sunrise = afterSunriseToday ? dataset.dataActual.sunriseCalendarOther().getTime()
                                    : dataset.dataActual.sunriseCalendarToday().getTime();

                            timestamp = sunrise.getTime();
                            noteMode = SolarEvents.SUNRISE;
                            timeString = utils.timeDeltaDisplayString(time, sunrise);
                            noteString = context.getString(R.string.until_sunrise);

                        } else {
                            // civil twilight: note time until noon
                            Date noon = dataset.dataNoon.sunriseCalendarToday().getTime();
                            boolean afterNoonToday = time.after(noon);
                            if (afterNoonToday)
                            {
                                noon = dataset.dataNoon.sunriseCalendarOther().getTime();
                            }
                            timestamp = noon.getTime();
                            noteMode = SolarEvents.NOON;
                            noteIcon = R.drawable.ic_noon_large;
                            timeString = utils.timeDeltaDisplayString(time, noon);
                            noteString = context.getString(R.string.until_noon);
                        }

                    }
                }
            }
        }

        NoteData note = new NoteData(noteMode, timeString, untilString, noteString, noteIcon, noteColor);
        note.timestamp = timestamp;

        if (currentNote == null)
        {
            setNote(note, NoteChangedListener.TRANSITION_NEXT);

        } else if (!currentNote.equals(note)) {
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
