/**
    Copyright (C) 2014-2022 Forrest Guice
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

import com.forrestguice.util.text.TimeDisplayText;

import java.util.Date;

public class NoteData
{
    public String noteMode;
    public TimeDisplayText timeText;
    public String prefixText;
    public String noteText;
    public int noteIconResource;
    public int noteIconStroke;
    public int textColor, iconColor, iconColor2;
    public Date time;
    public boolean tomorrow = false;
    public boolean squareIcon = false;

    public NoteData(String noteMode, TimeDisplayText timeText, String prefixText, String noteText, int noteIconResource, int textColor, int iconColor, int iconColor2, int noteIconStroke, boolean squareIcon)
    {
        this.noteMode = noteMode;
        this.timeText = timeText;
        this.prefixText = prefixText;
        this.noteText = noteText;
        this.noteIconResource = noteIconResource;
        this.textColor = textColor;
        this.iconColor = iconColor;
        this.iconColor2 = iconColor2;
        this.noteIconStroke = noteIconStroke;
        this.squareIcon = squareIcon;
    }

    public NoteData( NoteData other )
    {
        this.noteMode = other.noteMode;
        this.timeText = other.timeText;
        this.prefixText = other.prefixText;
        this.noteText = other.noteText;
        this.noteIconResource = other.noteIconResource;
        this.textColor = other.textColor;
        this.iconColor = other.iconColor;
        this.iconColor2 = other.iconColor2;
        this.noteIconStroke = other.noteIconStroke;
        this.squareIcon = other.squareIcon;
        this.time = other.time;
        this.tomorrow = other.tomorrow;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !NoteData.class.isAssignableFrom(obj.getClass()))
            return false;

        final NoteData other = (NoteData) obj;
        if (!other.noteMode.equals(noteMode))
            return false;

        if (!other.timeText.getValue().equals(timeText.getValue()))
            return false;

        //Log.d("NoteData.equals", "these notes are the same");
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = this.noteMode.hashCode();
        hash = hash * 37 + this.timeText.hashCode();
        return hash;
    }

    @Override
    public String toString()
    {
        return "NoteData[" + this.noteMode + " (" + this.time + " in" + timeText + ")" + "]";
    }
}
