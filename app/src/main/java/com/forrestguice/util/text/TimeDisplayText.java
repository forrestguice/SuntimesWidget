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

package com.forrestguice.util.text;

public class TimeDisplayText
{
    private long rawValue = 0;
    private String value;
    private String units;
    private String suffix;

    public TimeDisplayText()
    {
        this.value = "";
        this.units = "";
        this.suffix = "";
    }

    public TimeDisplayText(String value)
    {
        this.value = value;
        this.units = "";
        this.suffix = "";
    }

    public TimeDisplayText(String value, String units, String suffix)
    {
        this.value = value;
        this.units = units;
        this.suffix = suffix;
    }

    public void setRawValue(long value)
    {
        rawValue = value;
    }

    public long getRawValue()
    {
        return rawValue;
    }

    public String getValue()
    {
        return value;
    }

    public String getUnits()
    {
        return units;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append(value);

        boolean valueNotEmpty = !value.isEmpty();
        boolean unitsNotEmpty = !units.isEmpty();

        if (unitsNotEmpty)
        {
            if (valueNotEmpty)
                s.append(" ");
            s.append(units);
        }

        if (!suffix.isEmpty())
        {
            if (valueNotEmpty || unitsNotEmpty)
                s.append(" ");
            s.append(suffix);
        }

        return s.toString();
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !TimeDisplayText.class.isAssignableFrom(obj.getClass()))
            return false;

        final TimeDisplayText other = (TimeDisplayText) obj;

        if (!value.equals(other.getValue()))
            return false;

        if (!units.equals(other.getUnits()))
            return false;

        //noinspection RedundantIfStatement
        if (!suffix.equals(other.getSuffix()))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = this.value.hashCode();
        hash = hash * 37 + units.hashCode();
        hash = hash * 37 + suffix.hashCode();
        return hash;
    }
}
