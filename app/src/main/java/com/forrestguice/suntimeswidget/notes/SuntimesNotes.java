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

import android.content.Context;
import com.forrestguice.suntimeswidget.calculator.SuntimesDataset;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import java.util.Calendar;

public interface SuntimesNotes
{
    void init(Context context, SuntimesDataset dataset);
    boolean isInitialized();

    void setOnChangedListener( NoteChangedListener listener );
    NoteChangedListener getOnChangedListener();

    int noteCount();
    int getNoteIndex();
    boolean setNoteIndex( int noteIndex );
    NoteData getNote( int noteIndex );
    boolean hasNextNote();
    boolean hasPrevNote();

    void resetNoteIndex();

    boolean showNextNote();
    boolean showPrevNote();
    boolean showNote( SolarEvents.SolarEventField forField );

    void updateNote(Context context);
    void updateNote(Context context, Calendar now);
    void updateNote(Context context, Calendar now, int transition);

    NoteData getNote();
    void setNote( NoteData note, int transition );
}

