/**
    Copyright (C) 2020 Forrest Guice
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

import android.os.Parcel;
import android.os.Parcelable;

import com.forrestguice.suntimeswidget.calculator.core.Location;

public class PlaceItem implements Parcelable
{
    public static final String TAG_DEFAULT = "[default]";

    public long rowID = -1;
    public Location location = null;
    public String comment = null;

    public PlaceItem() {}

    public PlaceItem(long rowID, Location location)
    {
        this.rowID = rowID;
        this.location = location;
    }
    public PlaceItem(long rowID, Location location, String comment)
    {
        this.rowID = rowID;
        this.location = location;
        this.comment = comment;
    }

    public PlaceItem( Parcel in )
    {
        this.rowID = in.readLong();
        this.location = in.readParcelable(getClass().getClassLoader());
        //this.isDefault = (in.readInt() == 1);
        this.comment = in.readString();
    }

    public boolean isDefault() {
        return hasTag(PlaceItem.TAG_DEFAULT);
    }
    public boolean hasTag(String tag) {
        return (comment != null && comment.contains(tag));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(rowID);
        dest.writeParcelable(location, 0);
        //dest.writeInt(isDefault ? 1 : 0);
        dest.writeString(comment);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public PlaceItem createFromParcel(Parcel in) {
            return new PlaceItem(in);
        }
        public PlaceItem[] newArray(int size)
        {
            return new PlaceItem[size];
        }
    };
}
