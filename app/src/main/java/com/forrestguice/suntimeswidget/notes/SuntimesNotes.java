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
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesDataset;

import java.util.Calendar;

public interface SuntimesNotes
{
    public void init(Context context, SuntimesDataset dataset);
    public boolean isInitialized();

    public void setOnChangedListener( NoteChangedListener listener );
    public NoteChangedListener getOnChangedListener();

    public int noteCount();
    public int getNoteIndex();
    public boolean setNoteIndex( int noteIndex );
    public NoteData getNote( int noteIndex );
    public boolean hasNextNote();
    public boolean hasPrevNote();

    public void resetNoteIndex();

    public boolean showNextNote();
    public boolean showPrevNote();

    public void updateNote(Context context);
    public void updateNote(Context context, Calendar now);
    public void updateNote(Context context, Calendar now, int transition);

    public NoteData getNote();
    public void setNote( NoteData note, int transition );
}

