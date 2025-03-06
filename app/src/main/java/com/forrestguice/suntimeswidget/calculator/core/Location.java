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

package com.forrestguice.suntimeswidget.calculator.core;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Location
 */
public class Location implements Parcelable
{
    public static String pattern_latLon = "#.#####";

    private String label;
    private String latitude;   // decimal degrees (DD)
    private String longitude;  // decimal degrees (DD)
    private String altitude;   // meters above the WGS 84 reference ellipsoid
    private boolean useAltitude = true;

    /**
     * @param latitude decimal degrees (DD) string
     * @param longitude decimal degrees (DD) string
     */
    public Location(String latitude, String longitude )
    {
        this(null, latitude, longitude, "0", true);
    }

    /**
     * @param label display name
     * @param latitude decimal degrees (DD) string
     * @param longitude decimal degrees (DD) string
     */
    public Location(@Nullable String label, String latitude, String longitude )
    {
        this(label, latitude, longitude, "0", true);
    }

    public Location(@Nullable String label, String latitude, String longitude, String altitude )
    {
        this(label, latitude, longitude, altitude, true);
    }

    /**
     * @param label display name
     * @param latitude decimal degrees (DD) string
     * @param longitude decimal degrees (DD) string
     * @param altitude feet or meters string
     * @param altitudeUnitsMetric true altitude is meters, false altitude is feet
     */
    public Location(String label, String latitude, String longitude, String altitude, boolean altitudeUnitsMetric)
    {
        this.label = (label == null) ? "" : label;
        this.latitude = latitude;
        this.longitude = longitude;

        if (!altitudeUnitsMetric)
        {
            try {
                this.altitude = Double.toString(feetToMeters(Double.parseDouble(altitude)));

            } catch (NumberFormatException e) {
                Log.e("Location", "Invalid altitude " + altitude + " (ft); unable to make conversion.");
                this.altitude = "";
            }
        } else {
            this.altitude = altitude;
        }
    }

    /**
     * @param label display name
     * @param location an android.location.Location object (that might be obtained via GPS or otherwise)
     */
    public Location(String label, @NonNull android.location.Location location)
    {
        double rawLatitude = location.getLatitude();
        double rawLongitude = location.getLongitude();
        double rawAltitude = location.getAltitude();

        DecimalFormat formatter = decimalDegreesFormatter();

        this.label = label;
        this.latitude = formatter.format(rawLatitude);
        this.longitude = formatter.format(rawLongitude);
        this.altitude = rawAltitude + "";
    }

    /**
     * @param other Location
     */
    public Location(Location other)
    {
        this.label = other.label;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.altitude = other.altitude;
        this.useAltitude = other.useAltitude;
    }

    /**
     * @return a user-defined display label / location name
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return latitude in decimal degrees (DD)
     */
    public String getLatitude()
    {
        return latitude;
    }

    public Double getLatitudeAsDouble()
    {
        double latitudeDouble = Double.parseDouble(latitude);
        if (latitudeDouble > 90 || latitudeDouble < -90)
        {
            double s = Math.signum(latitudeDouble);
            double adjusted = (s * 90) - (latitudeDouble % (s * 90));
            Log.w("Location", "latitude is out of range! adjusting.. " + latitudeDouble + " -> " + adjusted);
            latitudeDouble = adjusted;
        }
        return latitudeDouble;
    }

    /**
     * @return longitude in decimal degrees (DD)
     */
    public String getLongitude()
    {
        return longitude;
    }

    public Double getLongitudeAsDouble()
    {
        Double longitudeDouble = Double.parseDouble(longitude);
        if (longitudeDouble > 180 || longitudeDouble < -180)
        {
            double s = Math.signum(longitudeDouble);
            double adjusted = (longitudeDouble % (s * 180)) - (s * 180);
            Log.w("Location", "longitude is out of range! adjusting.. " + longitudeDouble + " -> " + adjusted);
            longitudeDouble = adjusted;
        }
        if (longitudeDouble == 180d) {
            longitudeDouble = -180d;
        }
        return longitudeDouble;
    }

    /**
     * @return altitude in meters
     */
    public String getAltitude()
    {
        return altitude;
    }

    public Double getAltitudeAsDouble()
    {
        if (!useAltitude || altitude.isEmpty())
            return 0.0;
        else {
            try {
                return Double.parseDouble(altitude);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }
    public Integer getAltitudeAsInteger()
    {
        if (!useAltitude || altitude.isEmpty())
            return 0;
        else return getAltitudeAsDouble().intValue();
    }
    public void setUseAltitude( boolean enabled )
    {
        useAltitude = enabled;
    }
    public boolean useAltitude() {
        return useAltitude;
    }

    /**
     * @return a "geo" URI describing this Location
     */
    public Uri getUri()
    {
        String uriString = "geo:" + latitude + "," + longitude;
        if (altitude != null && !altitude.isEmpty()) {
            uriString += "," + altitude;
        }
        return Uri.parse(uriString);
    }

    public double distanceTo(Location other) {
        return distanceBetween(this, other);
    }

    private static final double R = 6371.009;   // km

    public static double distanceBetween(Location location1, Location location2)
    {
        double lat1 = Math.toRadians(location1.getLatitudeAsDouble());
        double lat2 = Math.toRadians(location2.getLatitudeAsDouble());
        double deltaLon = Math.abs(Math.toRadians(location2.getLongitudeAsDouble()) - Math.toRadians(location1.getLongitudeAsDouble()));
        double deltaSigma = acos((sin(lat1) * sin(lat2)) + (cos(lat1) * cos(lat2) * cos(deltaLon)));  // spherical law of cosines
        return deltaSigma * R;
    }

    /**
     * @return a decimal degrees string "latitude, longitude" describing this location
     */
    public String toString()
    {
        return latitude + ", " + longitude;
    }

    /**
     * @param obj another Location object
     * @return true the locations are the same (label, lat, lon, and alt), false they are different somehow
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Location))
        {
            return false;
        } else {
            Location that = (Location)obj;
            return (this.getLabel().equals(that.getLabel()))
                    && (this.getLatitude().equals(that.getLatitude()))
                    && (this.getLongitude().equals(that.getLongitude()))
                    && (this.getAltitude().equals(that.getAltitude()));
        }
    }

    public static DecimalFormat decimalDegreesFormatter()
    {
        DecimalFormat formatter = (DecimalFormat)(NumberFormat.getNumberInstance(Locale.US));
        formatter.applyLocalizedPattern(pattern_latLon);
        return formatter;
    }

    private static double feetToMeters(double feet) {
        return (feet * (1d / 3.28084d) );
    }

    /**
     * @param in Parcel
     */
    public Location( Parcel in )
    {
        this.label = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.altitude = in.readString();
        this.useAltitude = (in.readInt() == 1);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(label);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(altitude);
        dest.writeInt(useAltitude ? 1 : 0);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Location createFromParcel(Parcel in)
        {
            return new Location(in);
        }

        public Location[] newArray(int size)
        {
            return new Location[size];
        }
    };

}
