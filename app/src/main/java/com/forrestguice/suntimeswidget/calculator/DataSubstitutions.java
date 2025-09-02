/**
    Copyright (C) 2014-2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator;

import android.content.Context;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.EventAliasTimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetDataMode;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetOrder;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.TrackingMode;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DataSubstitutions
{
    public static final String PATTERN_MARKER = "%";
    public static final String PATTERN_EVENT_MARKER = "@";

    public static final String PATTERN_m = "%m";    // mode (short)
    public static final String PATTERN_M = "%M";    // mode
    public static final String PATTERN_o = "%o";    // order
    public static final String PATTERN_loc = "%loc";
    public static final String PATTERN_lat = "%lat";
    public static final String PATTERN_lon = "%lon";
    public static final String PATTERN_lel = "%lel";
    public static final String PATTERN_eot = "%eot";
    public static final String PATTERN_eot_m = "%eot_m";
    public static final String PATTERN_t = "%t";
    public static final String PATTERN_s = "%s";
    public static final String PATTERN_id = "%id";
    public static final String PATTERN_d = "%d";
    public static final String PATTERN_dY = "%dY";
    public static final String PATTERN_dD = "%dD";
    public static final String PATTERN_dd = "%dd";
    public static final String PATTERN_dT = "%dT";
    public static final String PATTERN_dt = "%dt";
    public static final String PATTERN_dm = "%dm";
    public static final String PATTERN_h = "%h";
    public static final String PATTERN_H = "%H";
    public static final String PATTERN_i = "%i";
    public static final String PATTERN_PERCENT = "%%";

    public static final String PATTERN_em_at = "%em@";    // event millis
    public static final String PATTERN_et_at = "%et@";    // event time (formatted)
    public static final String PATTERN_eT_at = "%eT@";    // event time (formatted with seconds)
    public static final String PATTERN_ea_at = "%ea@";    // angle/elevation
    public static final String PATTERN_eA_at = "%eA@";    // angle/elevation (formatted)
    public static final String PATTERN_ez_at = "%ez@";    // azimuth
    public static final String PATTERN_eZ_at = "%eZ@";    // azimuth (formatted)
    public static final String PATTERN_ed_at = "%ed@";    // declination
    public static final String PATTERN_eD_at = "%eD@";    // declination (formatted)
    public static final String PATTERN_er_at = "%er@";    // right-ascension
    public static final String PATTERN_eR_at = "%eR@";    // right-ascension (formatted)
    public static final String PATTERN_es_at = "%es@";    // shadow length (meters)
    public static final String PATTERN_eS_at = "%eS@";    // shadow length display (formatted, meters or feet)

    public static final String SUFFIX_MORNING_ASTRONOMICAL = "ar";
    public static final String SUFFIX_EVENING_ASTRONOMICAL = "as";
    public static final String SUFFIX_MORNING_NAUTICAL = "nr";
    public static final String SUFFIX_EVENING_NAUTICAL = "ns";
    public static final String SUFFIX_MORNING_CIVIL = "cr";
    public static final String SUFFIX_EVENING_CIVIL = "cs";
    public static final String SUFFIX_SUNRISE = "sr";
    public static final String SUFFIX_NOON = "sn";
    public static final String SUFFIX_MIDNIGHT = "sm";
    public static final String SUFFIX_SUNSET = "ss";
    public static final String SUFFIX_MORNING_GOLDEN = "gr";
    public static final String SUFFIX_EVENING_GOLDEN = "gs";
    public static final String SUFFIX_MORNING_BLUE4 = "b4r";
    public static final String SUFFIX_EVENING_BLUE4 = "b4s";
    public static final String SUFFIX_MORNING_BLUE8 = "b8r";
    public static final String SUFFIX_EVENING_BLUE8 = "b8s";
    public static final String SUFFIX_MOONRISE = "lr";
    public static final String SUFFIX_MOONSET = "ls";
    public static final String SUFFIX_MOONNOON = "ln";
    public static final String SUFFIX_MOONNIGHT = "lm";

    public static final String[] ALL_PATTERNS_AT = new String[] { PATTERN_em_at, PATTERN_et_at, PATTERN_eT_at,
            PATTERN_ea_at, PATTERN_eA_at, PATTERN_ez_at, PATTERN_eZ_at, PATTERN_ed_at, PATTERN_eD_at,
            PATTERN_er_at, PATTERN_eR_at, PATTERN_es_at, PATTERN_eS_at,
    };
    public static final String[] ALL_AT_SUFFIXES = new String[] {
            SUFFIX_MORNING_ASTRONOMICAL, SUFFIX_EVENING_ASTRONOMICAL,
            SUFFIX_MORNING_NAUTICAL, SUFFIX_EVENING_NAUTICAL,
            SUFFIX_MORNING_CIVIL, SUFFIX_EVENING_CIVIL,
            SUFFIX_SUNRISE, SUFFIX_NOON, SUFFIX_MIDNIGHT, SUFFIX_SUNSET,
            SUFFIX_MORNING_GOLDEN, SUFFIX_EVENING_GOLDEN,
            SUFFIX_MORNING_BLUE4, SUFFIX_EVENING_BLUE4,
            SUFFIX_MORNING_BLUE8, SUFFIX_EVENING_BLUE8,
            SUFFIX_MOONRISE, SUFFIX_MOONSET, SUFFIX_MOONNOON, SUFFIX_MOONNIGHT
    };

    @Nullable
    public static <T extends SuntimesData> SuntimesCalculator.Position getPositionForEvent(@NonNull SolarEvents event, @Nullable T data)
    {
        if (data != null)
        {
            SuntimesCalculator calculator = data.calculator();
            Calendar datetime = getCalendarForEvent(event, data);
            return (datetime != null && calculator != null ? calculator.getSunPosition(datetime) : null);
        }
        return null;
    }

    @Nullable
    public static <T extends SuntimesData> Double getAltitudeForEvent(@NonNull SolarEvents event, @Nullable T data)
    {
        SuntimesCalculator.Position position = getPositionForEvent(event, data);
        return (position != null ? position.elevation : null);
    }

    @Nullable
    public static <T extends SuntimesData> Double getAzimuthForEvent(@NonNull SolarEvents event, @Nullable T data)
    {
        SuntimesCalculator.Position position = getPositionForEvent(event, data);
        return (position != null ? position.azimuth : null);
    }

    @Nullable
    public static <T extends SuntimesData> Double getDeclinationForEvent(@NonNull SolarEvents event, @Nullable T data)
    {
        SuntimesCalculator.Position position = getPositionForEvent(event, data);
        return (position != null ? position.declination : null);
    }

    @Nullable
    public static <T extends SuntimesData> Double getRightAscensionForEvent(@NonNull SolarEvents event, @Nullable T data)
    {
        SuntimesCalculator.Position position = getPositionForEvent(event, data);
        return (position != null ? position.rightAscension : null);
    }

    @Nullable
    public static <T extends SuntimesData> Double getShadowLengthForEvent(Context context, @NonNull SolarEvents event, @Nullable T data)
    {
        if (data != null)
        {
            SuntimesCalculator calculator = data.calculator();
            Calendar datetime = getCalendarForEvent(event, data);
            double objHeight = WidgetSettings.loadObserverHeightPref(context, data.appWidgetID());
            return (datetime != null && calculator != null ? calculator.getShadowLength(objHeight, datetime) : null);

        } else {
            return null;
        }
    }

    @Nullable
    public static <T extends SuntimesData> Calendar getCalendarForEvent(@NonNull SolarEvents event, @NonNull T data)
    {
        if (data == null || data.calculator() == null) {
            if (BuildConfig.DEBUG) {
                Log.w("DEBUG", "getCalendarForEvent: null data or calculator! returning null...");
            }
            return null;
        }

        switch (event)
        {
            case MIDNIGHT: return data.calculator().getSolarMidnightCalendarForDate(data.calendar());
            case NOON: return data.calculator().getSolarNoonCalendarForDate(data.calendar());
            case SUNRISE: return data.calculator().getOfficialSunriseCalendarForDate(data.calendar());
            case MORNING_CIVIL: return data.calculator().getCivilSunriseCalendarForDate(data.calendar());
            case MORNING_NAUTICAL: return data.calculator().getNauticalSunriseCalendarForDate(data.calendar());
            case MORNING_ASTRONOMICAL: return data.calculator().getAstronomicalSunriseCalendarForDate(data.calendar());
            case MORNING_GOLDEN: return data.calculator().getMorningGoldenHourForDate(data.calendar());
            case MORNING_BLUE8: return data.calculator().getMorningBlueHourForDate(data.calendar())[0];
            case MORNING_BLUE4: return data.calculator().getMorningBlueHourForDate(data.calendar())[1];

            case SUNSET: return data.calculator().getOfficialSunsetCalendarForDate(data.calendar());
            case EVENING_CIVIL: return data.calculator().getCivilSunsetCalendarForDate(data.calendar());
            case EVENING_NAUTICAL: return data.calculator().getNauticalSunsetCalendarForDate(data.calendar());
            case EVENING_ASTRONOMICAL: return data.calculator().getAstronomicalSunsetCalendarForDate(data.calendar());
            case EVENING_GOLDEN: return data.calculator().getEveningGoldenHourForDate(data.calendar());
            case EVENING_BLUE4: return data.calculator().getEveningBlueHourForDate(data.calendar())[0];
            case EVENING_BLUE8: return data.calculator().getEveningBlueHourForDate(data.calendar())[1];

            case MOONRISE: return data.calculator().getMoonTimesForDate(data.calendar()).riseTime;
            case MOONSET: return data.calculator().getMoonTimesForDate(data.calendar()).setTime;
            case MOONNOON:
                if (data instanceof SuntimesMoonData) {
                    return ((SuntimesMoonData) data).getLunarNoonToday();
                } else return null;   // requires matching data
            case MOONNIGHT:
                if (data instanceof SuntimesMoonData) {
                    return ((SuntimesMoonData) data).getLunarMidnightToday();
                } else return null;   // requires matching data

            case FULLMOON: case NEWMOON: case FIRSTQUARTER: case THIRDQUARTER:
                if (data instanceof SuntimesMoonData1) {
                    return ((SuntimesMoonData1) data).moonPhaseCalendar(SolarEvents.toMoonPhase(event));
                } else return data.calculator().getMoonPhaseNextDate(SolarEvents.toMoonPhase(event), data.calendar());

            case CROSS_SUMMER: case CROSS_WINTER: case CROSS_AUTUMNAL: case CROSS_SPRING:
                if (data instanceof SuntimesEquinoxSolsticeData) {
                    return ((SuntimesEquinoxSolsticeData) data).eventCalendarThisYear();
                } else return null;   // requires matching data

            case SOLSTICE_SUMMER: return data.calculator().getSummerSolsticeForYear(data.calendar());
            case SOLSTICE_WINTER: return data.calculator().getWinterSolsticeForYear(data.calendar());
            case EQUINOX_AUTUMNAL: return data.calculator().getAutumnalEquinoxForYear(data.calendar());
            case EQUINOX_SPRING: return data.calculator().getSpringEquinoxForYear(data.calendar());

            default: return data.calendar();
        }
    }

    @Nullable
    public static String getPatternForEvent(@NonNull String prefix, @NonNull SolarEvents event)
    {
        switch (event)
        {
            case MORNING_ASTRONOMICAL: return prefix + SUFFIX_MORNING_ASTRONOMICAL;
            case EVENING_ASTRONOMICAL: return prefix + SUFFIX_EVENING_ASTRONOMICAL;
            case MORNING_NAUTICAL: return prefix + SUFFIX_MORNING_NAUTICAL;
            case EVENING_NAUTICAL: return prefix + SUFFIX_EVENING_NAUTICAL;
            case MORNING_CIVIL: return prefix + SUFFIX_MORNING_CIVIL;
            case EVENING_CIVIL: return prefix + SUFFIX_EVENING_CIVIL;
            case SUNRISE: return prefix + SUFFIX_SUNRISE;
            case NOON: return prefix + SUFFIX_NOON;
            case MIDNIGHT: return prefix + SUFFIX_MIDNIGHT;
            case SUNSET: return prefix + SUFFIX_SUNSET;
            case MORNING_GOLDEN: return prefix + SUFFIX_MORNING_GOLDEN;
            case EVENING_GOLDEN: return prefix + SUFFIX_EVENING_GOLDEN;
            case MORNING_BLUE4: return prefix + SUFFIX_MORNING_BLUE4;
            case EVENING_BLUE4: return prefix + SUFFIX_EVENING_BLUE4;
            case MORNING_BLUE8: return prefix + SUFFIX_MORNING_BLUE8;
            case EVENING_BLUE8: return prefix + SUFFIX_EVENING_BLUE8;
            case MOONRISE: return prefix + SUFFIX_MOONRISE;
            case MOONSET: return prefix + SUFFIX_MOONSET;
            case MOONNOON: return prefix + SUFFIX_MOONNOON;
            case MOONNIGHT: return prefix + SUFFIX_MOONNIGHT;
            default: return null;
        }
    }

    public interface PatternForEventInterface {
        @Nullable String getPatternForEvent(SolarEvents event);
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent(SolarEvents[] events, HashMap<SolarEvents,String> patterns, PatternForEventInterface methodInterface)
    {
        for (SolarEvents event : events)
        {
            String pattern = methodInterface.getPatternForEvent(event);
            if (pattern != null) {
                patterns.put(event, pattern);
            }
        }
        return patterns;
    }

    public static HashMap<SolarEvents, String> getPatternsForEvent(final String pattern, SolarEvents[] events)
    {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface()
        {
            @Nullable
            public String getPatternForEvent(SolarEvents event) {
                return DataSubstitutions.getPatternForEvent(pattern, event);
            }
        });
    }

    public static String removePatterns(@NonNull String displayString, @NonNull Collection<String> patterns) {
        String value = displayString;
        for (String pattern : patterns) {
            value = value.replaceAll(pattern, "");
        }
        return value;
    }
    public static String removePattern(@NonNull String displayString, String pattern) {
        return displayString.replaceAll(pattern, "");
    }

    /**
     * @param patterns hashmap of input patterns
     * @param appendTo the list that non-null patterns will be appended to
     * @return input patterns for method chaining
     */
    @NonNull
    public static HashMap<SolarEvents, String> appendTo(@NonNull HashMap<SolarEvents, String> patterns, @NonNull List<String> appendTo)
    {
        for (String pattern : patterns.values()) {
            if (pattern != null) {
                appendTo.add(pattern);
            }
        }
        return patterns;
    }

    /**
     * @param displayString string containing patterns
     * @param patterns list of patterns to look for
     * @return true if the string contains at least one of the patterns, false if there were none
     */
    public static boolean containsAtLeastOne(@NonNull String displayString, @NonNull Iterable<String> patterns)
    {
        for (String pattern : patterns) {
            if (displayString.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static String addToSet(@NonNull Set<String> set, @Nullable String value)
    {
        if (value != null) {
            set.add(value);
        }
        return value;
    }

    protected static SuntimesUtils utils = new SuntimesUtils();
    public static void initDisplayStrings(Context context) {
        SuntimesUtils.initDisplayStrings(context);
    }

    /**
     * displayStringForTitlePattern
     */
    public static <T extends SuntimesData> String displayStringForTitlePattern0(Context context, String titlePattern, @Nullable T data)
    {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "displayStringForTitlePattern0: " + titlePattern);
        }

        String displayString;
        if (data instanceof SuntimesRiseSetData) {
            displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesRiseSetData) data);

        } else if (data instanceof SuntimesMoonData) {
            displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesMoonData) data);

        } else if (data instanceof SuntimesEquinoxSolsticeData) {
            displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesEquinoxSolsticeData) data);

        } else if (data instanceof SuntimesClockData) {
            displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesClockData) data);

        } else {
            displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData) data);
        }

        List<String> positionPatterns = new ArrayList<>();
        SolarEvents[] events = SolarEvents.values(); //{ SolarEvents.SUNRISE, SolarEvents.NOON, SolarEvents.SUNSET };
        HashMap<SolarEvents, String> patterns_em = appendTo(getPatternsForEvent(PATTERN_em_at, events), positionPatterns);
        HashMap<SolarEvents, String> patterns_et = appendTo(getPatternsForEvent(PATTERN_et_at, events), positionPatterns);
        HashMap<SolarEvents, String> patterns_eT = appendTo(getPatternsForEvent(PATTERN_eT_at, events), positionPatterns);
        HashMap<SolarEvents, String> patterns_ea = appendTo(getPatternsForEvent(PATTERN_ea_at, events), positionPatterns);   // angle/elevation
        HashMap<SolarEvents, String> patterns_eA = appendTo(getPatternsForEvent(PATTERN_eA_at, events), positionPatterns);   // angle/elevation (formatted)
        HashMap<SolarEvents, String> patterns_ez = appendTo(getPatternsForEvent(PATTERN_ez_at, events), positionPatterns);   // azimuth
        HashMap<SolarEvents, String> patterns_eZ = appendTo(getPatternsForEvent(PATTERN_eZ_at, events), positionPatterns);   // azimuth (formatted)
        HashMap<SolarEvents, String> patterns_ed = appendTo(getPatternsForEvent(PATTERN_ed_at, events), positionPatterns);   // declination
        HashMap<SolarEvents, String> patterns_eD = appendTo(getPatternsForEvent(PATTERN_eD_at, events), positionPatterns);   // declination (formatted)
        HashMap<SolarEvents, String> patterns_er = appendTo(getPatternsForEvent(PATTERN_er_at, events), positionPatterns);   // right-ascension
        HashMap<SolarEvents, String> patterns_eR = appendTo(getPatternsForEvent(PATTERN_eR_at, events), positionPatterns);   // right-ascension (formatted)
        HashMap<SolarEvents, String> patterns_es = appendTo(getPatternsForEvent(PATTERN_es_at, events), positionPatterns);   // shadow length (meters)
        HashMap<SolarEvents, String> patterns_eS = appendTo(getPatternsForEvent(PATTERN_eS_at, events), positionPatterns);   // shadow length display (formatted, meters or feet)

        if (data == null)
        {
            if (BuildConfig.DEBUG) {
                Log.d("DEBUG", "displayStringForTitlePattern0: null data; removing patterns");
            }
            for (String pattern : positionPatterns) {
                displayString = removePattern(displayString, pattern);
            }
            return displayString;
        }

        Set<String> eventPatterns = new TreeSet<>();
        for (SolarEvents event : events)
        {
            if (!displayString.contains(PATTERN_MARKER)) {
                if (BuildConfig.DEBUG) {
                    Log.d("DEBUG", "displayStringForTitlePattern0: done");
                }
                break;
            }

            eventPatterns.clear();
            String pattern_em = addToSet(eventPatterns, patterns_em.get(event));
            String pattern_et = addToSet(eventPatterns, patterns_et.get(event));
            String pattern_eT = addToSet(eventPatterns, patterns_eT.get(event));
            String pattern_ea = addToSet(eventPatterns, patterns_ea.get(event));
            String pattern_eA = addToSet(eventPatterns, patterns_eA.get(event));
            String pattern_ez = addToSet(eventPatterns, patterns_ez.get(event));
            String pattern_eZ = addToSet(eventPatterns, patterns_eZ.get(event));
            String pattern_ed = addToSet(eventPatterns, patterns_ed.get(event));
            String pattern_eD = addToSet(eventPatterns, patterns_eD.get(event));
            String pattern_er = addToSet(eventPatterns, patterns_er.get(event));
            String pattern_eR = addToSet(eventPatterns, patterns_eR.get(event));
            String pattern_es = addToSet(eventPatterns, patterns_es.get(event));
            String pattern_eS = addToSet(eventPatterns, patterns_eS.get(event));

            if (!DataSubstitutions.containsAtLeastOne(displayString, eventPatterns)) {
                if (BuildConfig.DEBUG) {
                    Log.d("DEBUG", "displayStringForTitlePattern0: no patterns for " + event.name() + "; continuing...");
                }
                continue;
            }

            Calendar eventTime = getCalendarForEvent(event, data);
            T d = data;

            if (BuildConfig.DEBUG) {
                Log.d("DEBUG", "displayStringForTitlePattern0: eventTime for " + event.name() + " is " + (eventTime != null ? eventTime.getTimeInMillis() : "null"));
            }
            if (eventTime != null)
            {
                if (pattern_em != null && displayString.contains(pattern_em)) {
                    displayString = displayString.replaceAll(pattern_em, eventTime.getTimeInMillis() + "");
                }
                if (pattern_et != null && displayString.contains(pattern_et)) {
                    displayString = displayString.replaceAll(pattern_et, utils.calendarTimeShortDisplayString(context, eventTime, false).toString());
                }
                if (pattern_eT != null && displayString.contains(pattern_eT)) {
                    displayString = displayString.replaceAll(pattern_eT, utils.calendarTimeShortDisplayString(context, eventTime, true).toString());
                }
                if (pattern_ez != null && displayString.contains(pattern_ez)) {
                    Double value = getAzimuthForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_ez, (value != null ? value + "" : ""));
                }
                if (pattern_eZ != null && displayString.contains(pattern_eZ)) {
                    Double value = getAzimuthForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_eZ, (value != null ? utils.formatAsDirection(value, 1) : ""));
                }
                if (pattern_ed != null && displayString.contains(pattern_ed)) {
                    Double value = getDeclinationForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_ed, (value != null ? value + "" : ""));
                }
                if (pattern_eD != null && displayString.contains(pattern_eD)) {
                    Double value = getDeclinationForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_eD, (value != null ? utils.formatAsDeclination(value, 1).toString() : ""));
                }
                if (pattern_er != null && displayString.contains(pattern_er)) {
                    Double value = getRightAscensionForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_er, (value != null ? value + "" : ""));
                }
                if (pattern_eR != null && displayString.contains(pattern_eR)) {
                    Double value = getRightAscensionForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_eR, (value != null ? utils.formatAsRightAscension(value, 1).toString() : ""));
                }
                if (pattern_ea != null && displayString.contains(pattern_ea)) {
                    Double angle = getAltitudeForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_ea, (angle != null ? angle + "" : ""));
                }
                if (pattern_eA != null && displayString.contains(pattern_eA)) {
                    Double angle = getAltitudeForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_eA, (angle != null ? utils.formatAsDegrees(angle, 1) : ""));
                }
                if (pattern_es != null && displayString.contains(pattern_es)) {
                    Double value = getShadowLengthForEvent(context, event, d);
                    displayString = displayString.replaceAll(pattern_es, (value != null ? value + "" : ""));
                }
                if (pattern_eS != null && displayString.contains(pattern_eS)) {
                    LengthUnit lengthUnit = WidgetSettings.loadLengthUnitsPref(context, data.appWidgetID());
                    Double value = getShadowLengthForEvent(context, event, d);
                    displayString = displayString.replaceAll(pattern_eS, (value != null ? SuntimesUtils.formatAsHeight(context, value, lengthUnit, 1, false).toString() : ""));
                }

            } else {
                if (BuildConfig.DEBUG) {
                    Log.d("DEBUG", "displayStringForTitlePattern0: null eventTime for " + event.name() + "; removing patterns");
                }
                for (String pattern : eventPatterns) {
                    displayString = displayString.replaceAll(pattern, "");
                }
            }
        }

        return displayString;
    }

    /**
     * displayStringForTitlePattern
     */
    public static String displayStringForTitlePattern(Context context, String titlePattern, @Nullable SuntimesData data)
    {
        String displayString = titlePattern;

        if (data == null)
        {
            String[] patterns = new String[] { PATTERN_loc, PATTERN_lat, PATTERN_lon, PATTERN_lel,          // in order of operation
                    PATTERN_t, PATTERN_s, PATTERN_id,
                    PATTERN_dt, PATTERN_dT, PATTERN_dd, PATTERN_dD, PATTERN_dY, PATTERN_dm, PATTERN_d,
                    PATTERN_h, PATTERN_H,
                    PATTERN_eot_m, PATTERN_eot,
                    PATTERN_PERCENT };

            for (int i=0; i<patterns.length; i++) {
                displayString = displayString.replaceAll(patterns[i], "");
            }
            return displayString;
        }

        if (!data.isCalculated()) {
            data.calculate(context);
        }

        Location location = data.location();
        String timezoneID = data.timezone().getID();
        String datasource = (data.calculatorMode() == null) ? "" : data.calculatorMode().getName();
        String appWidgetID = (data.appWidgetID() != null ? String.format("%s", data.appWidgetID()) : "");

        displayString = displayString.replaceAll(PATTERN_loc, location.getLabel());
        displayString = displayString.replaceAll(PATTERN_lat, location.getLatitude());
        displayString = displayString.replaceAll(PATTERN_lon, location.getLongitude());

        if (displayString.contains(PATTERN_lel))
        {
            String altitudeDisplay = (WidgetSettings.loadLengthUnitsPref(context, 0) == LengthUnit.IMPERIAL)
                    ? (int) LengthUnit.metersToFeet(location.getAltitudeAsDouble()) + ""
                    : location.getAltitudeAsInteger() + "";
            displayString = displayString.replaceAll(PATTERN_lel, altitudeDisplay);
        }

        if (displayString.contains(PATTERN_eot) || displayString.contains(PATTERN_eot_m))
        {
            long eot = WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(data.calendar().getTimeInMillis(), data.calculator());
            displayString = displayString.replaceAll(PATTERN_eot_m, eot+"");
            displayString = displayString.replaceAll(PATTERN_eot, ((eot < 0) ? "-" : "+") + utils.timeDeltaLongDisplayString(eot, true).getValue());
        }

        displayString = displayString.replaceAll(PATTERN_t, timezoneID);
        displayString = displayString.replaceAll(PATTERN_s, datasource);
        displayString = displayString.replaceAll(PATTERN_id, appWidgetID);

        if (displayString.contains(PATTERN_d))
        {
            displayString = displayString.replaceAll(PATTERN_dt, utils.calendarTimeShortDisplayString(context, data.now(), false).toString());
            displayString = displayString.replaceAll(PATTERN_dT, utils.calendarTimeShortDisplayString(context, data.now(), true).toString());
            displayString = displayString.replaceAll(PATTERN_dd, utils.calendarDayDisplayString(context, data.calendar(), true).toString());
            displayString = displayString.replaceAll(PATTERN_dD, utils.calendarDayDisplayString(context, data.calendar(), false).toString());
            displayString = displayString.replaceAll(PATTERN_dY, utils.calendarDateYearDisplayString(context, data.calendar()).toString());
            displayString = displayString.replaceAll(PATTERN_dm, Long.toString(data.calendar().getTimeInMillis()));
            displayString = displayString.replaceAll(PATTERN_d, utils.calendarDateDisplayString(context, data.calendar(), false).toString());
        }

        if (displayString.contains(PATTERN_h) || displayString.contains(PATTERN_H))
        {
            LengthUnit lengthUnit = WidgetSettings.loadLengthUnitsPref(context, data.appWidgetID());
            float height = WidgetSettings.loadObserverHeightPref(context, data.appWidgetID());    // %h
            displayString = displayString.replaceAll(PATTERN_h, height + "");
            displayString = displayString.replaceAll(PATTERN_H, SuntimesUtils.formatAsHeight(context, height, lengthUnit, 2, true).toString());    // %H
        }

        displayString = displayString.replaceAll(PATTERN_PERCENT, "%");
        return displayString;
    }

    /**
     * displayStringForTitlePattern
     */
    public static String displayStringForTitlePattern(Context context, String titlePattern, @Nullable SuntimesRiseSetData data)
    {
        String displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData) data);

        String[] patterns = new String[] { PATTERN_M, PATTERN_m, PATTERN_o };
        if (data == null) {
            return removePatterns(displayString, Arrays.asList(patterns));
        }

        SuntimesRiseSetData d = (SuntimesRiseSetData) data;
        TimeMode timeMode = d.timeMode();
        String modeDisplayShort = timeMode.getShortDisplayString();
        String modeDisplayLong = timeMode.getLongDisplayString();

        RiseSetDataMode timeModeItem = d.dataMode();
        if (timeModeItem instanceof EventAliasTimeMode) {
            String label = EventSettings.loadEventValue(context, timeModeItem.name(), EventSettings.PREF_KEY_EVENT_LABEL);
            if (label != null) {
                modeDisplayLong = modeDisplayShort = label;
            }
        }

        displayString = displayString.replaceAll(PATTERN_m, modeDisplayShort);
        displayString = displayString.replaceAll(PATTERN_M, modeDisplayLong);

        RiseSetOrder order = WidgetSettings.loadRiseSetOrderPref(context, data.appWidgetID());
        displayString = displayString.replaceAll(PATTERN_o, order.toString());

        return displayString;
    }

    /**
     * displayStringForTitlePattern
     */
    public static String displayStringForTitlePattern(Context context, String titlePattern, @Nullable SuntimesEquinoxSolsticeData data)
    {
        String displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData) data);

        if (data == null) {
            return displayString.replaceAll(PATTERN_m, "").replaceAll(PATTERN_M, "").replaceAll(PATTERN_o, "");
        }

        TrackingMode trackingMode = WidgetSettings.loadTrackingModePref(context, data.appWidgetID());
        SolsticeEquinoxMode timeMode = data.timeMode();

        displayString = displayString.replaceAll(PATTERN_m, timeMode.getShortDisplayString());
        displayString = displayString.replaceAll(PATTERN_M, timeMode.getLongDisplayString());
        displayString = displayString.replaceAll(PATTERN_o, trackingMode.toString());
        return displayString;
    }

    /**
     * displayStringForTitlePattern
     */
    public static String displayStringForTitlePattern(Context context, String titlePattern, @Nullable SuntimesClockData data)
    {
        String displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData) data);

        if (data == null) {
            return displayString.replaceAll(PATTERN_m, "").replaceAll(PATTERN_M, "");
        }

        CalendarMode mode = CalendarSettings.loadCalendarModePref(context, data.appWidgetID());
        displayString = displayString.replaceAll(PATTERN_m, mode.getDisplayString());
        displayString = displayString.replaceAll(PATTERN_M, mode.getDisplayString());
        return displayString;
    }

    /**
     * displayStringForTitlePattern
     */
    public static String displayStringForTitlePattern(Context context, String titlePattern, @Nullable SuntimesMoonData data)
    {
        String displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData) data);

        if (data != null && data.isCalculated())
        {
            RiseSetOrder order = WidgetSettings.loadRiseSetOrderPref(context, data.appWidgetID());

            displayString = displayString.replaceAll(PATTERN_m, data.getMoonPhaseToday().getShortDisplayString());
            displayString = displayString.replaceAll(PATTERN_M, data.getMoonPhaseToday().getLongDisplayString());
            displayString = displayString.replaceAll(PATTERN_o, order.toString());

            if (displayString.contains(PATTERN_i)) {
                NumberFormat percentage = NumberFormat.getPercentInstance();
                displayString = displayString.replaceAll(PATTERN_i, percentage.format(data.getMoonIlluminationToday()));
            }
        } else {
            displayString = displayString.replaceAll(PATTERN_m, "").replaceAll(PATTERN_M, "").replaceAll(PATTERN_o, "").replaceAll(PATTERN_i, "");
        }
        return displayString;
    }

}
