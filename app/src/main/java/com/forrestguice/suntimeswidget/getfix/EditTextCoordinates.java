/**
    Copyright (C) 2026 Forrest Guice
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

package com.forrestguice.suntimeswidget.getfix;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.forrestguice.support.widget.AppCompatEditText;

import java.util.Locale;

public class EditTextCoordinates extends AppCompatEditText
{
    public EditTextCoordinates(Context context) {
        super(context);
    }

    public EditTextCoordinates(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTextContextMenuItem(int id)
    {
        if (id == android.R.id.paste)
        {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && listener != null)
            {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null)
                {
                    String pastedText = clip.getItemAt(0).getText().toString();
                    double[] coordinates = parseCoordinates(pastedText);
                    if (coordinates != null && coordinates.length == 3) {
                        if (listener.onPasted(coordinates[0], coordinates[1], coordinates[2])) {    // onPasted( latitude, longitude, altitude )
                            return true;
                        }

                    } else if (coordinates != null && coordinates.length == 2) {
                        if (listener.onPasted(coordinates[0], coordinates[1])) {                    // onPasted( latitude, longitude )
                            return true;
                        }
                    }
                }
            }
        }
        return super.onTextContextMenuItem(id);
    }

    /**
     * @return array with 2 or 3 elements, or null if unable to parse coordinates
     */
    protected double[] parseCoordinates(String text, String delimiter)
    {
        if (text.contains(delimiter))
        {
            String[] parts = text.split(delimiter);
            switch (parts.length)
            {
                case 4:
                    String label = parts[0];    // ignore label
                    return new double[] {
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]) };
                case 3:
                    return new double[] {
                            Double.parseDouble(parts[0]),
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]) };
                case 2:
                    return new double[] {
                            Double.parseDouble(parts[0]),
                            Double.parseDouble(parts[1]) };
                default:
                    return null;
            }
        }
        return null;
    }

    protected double[] parseCoordinates(String text)
    {
        if (text.contains(",")) {
            return parseCoordinates(text, ",");

        } else if (text.contains(";")) {
            return parseCoordinates(text, ";");

        } else if (text.contains("|")) {
            return parseCoordinates(text, "|");
        }
        return null;
    }

    protected EditTextCoordinatesListener listener = null;
    public void setEditTextCoordinatesListener(EditTextCoordinatesListener listener) {
        this.listener = listener;
    }

    /**
     * EditTextCoordinatesListener
     */
    public interface EditTextCoordinatesListener
    {
        /**
         * @return true paste event was handled, false unhandled (will fallback to default behavior)
         */
        boolean onPasted(double latitude, double longitude);
        boolean onPasted(double latitude, double longitude, double altitude);
    }

    /**
     * SimpleEditTextCoordinatesListener
     */
    public static class SimpleEditTextCoordinatesListener implements EditTextCoordinatesListener
    {
        private final EditTextCoordinates text_locationLat, text_locationLon, text_locationAlt;

        public SimpleEditTextCoordinatesListener(EditTextCoordinates text_latitude, EditTextCoordinates text_longitude, EditTextCoordinates text_altitude)
        {
            text_locationLat = text_latitude;
            text_locationLon = text_longitude;
            text_locationAlt = text_altitude;
        }

        @Override
        public boolean onPasted(double latitude, double longitude)
        {
            if (text_locationLat != null) {
                text_locationLat.setText(String.format(Locale.getDefault(), "%.5f", latitude));
            }
            if (text_locationLon != null) {
                text_locationLon.setText(String.format(Locale.getDefault(), "%.5f", longitude));
            }
            return true;
        }

        @Override
        public boolean onPasted(double latitude, double longitude, double altitude)
        {
            if (text_locationLat != null) {
                text_locationLat.setText(String.format(Locale.getDefault(), "%.5f", latitude));
            }
            if (text_locationLon != null) {
                text_locationLon.setText(String.format(Locale.getDefault(), "%.5f", longitude));
            }
            if (text_locationAlt != null) {
                text_locationAlt.setText(String.format(Locale.getDefault(), "%.2f", altitude));
            }
            return true;
        }
    }

}
