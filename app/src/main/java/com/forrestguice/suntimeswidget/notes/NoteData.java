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

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

public class NoteData
{
    public SolarEvents noteMode;
    public SuntimesUtils.TimeDisplayText timeText;
    public String prefixText;
    public String noteText;
    public int noteIconResource;
    public int noteColor;
    public long timestamp;

    public NoteData(SolarEvents noteMode, SuntimesUtils.TimeDisplayText timeText, String prefixText, String noteText, int noteIconResource, int noteColor)
    {
        this.noteMode = noteMode;
        this.timeText = timeText;
        this.prefixText = prefixText;
        this.noteText = noteText;
        this.noteIconResource = noteIconResource;
        this.noteColor = noteColor;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !NoteData.class.isAssignableFrom(obj.getClass()))
            return false;

        final NoteData other = (NoteData) obj;
        if (other.noteMode != noteMode)
            return false;

        if (!other.timeText.getValue().equals(timeText.getValue()))
            return false;

        return true;
    }
}
