/**
    Copyright (C) 2014-2024 Forrest Guice
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DataSubstitutions
{
    @Nullable
    public static <T extends SuntimesData> SuntimesCalculator.Position getPositionForEvent(SolarEvents event, @Nullable T data)
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
    public static <T extends SuntimesData> Double getAltitudeForEvent(SolarEvents event, @Nullable T data)
    {
        SuntimesCalculator.Position position = getPositionForEvent(event, data);
        return (position != null ? position.elevation : null);
    }

    @Nullable
    public static <T extends SuntimesData> Double getAzimuthForEvent(SolarEvents event, @Nullable T data)
    {
        SuntimesCalculator.Position position = getPositionForEvent(event, data);
        return (position != null ? position.azimuth : null);
    }

    @Nullable
    public static <T extends SuntimesData> Double getDeclinationForEvent(SolarEvents event, @Nullable T data)
    {
        SuntimesCalculator.Position position = getPositionForEvent(event, data);
        return (position != null ? position.declination : null);
    }

    @Nullable
    public static <T extends SuntimesData> Double getRightAscensionForEvent(SolarEvents event, @Nullable T data)
    {
        SuntimesCalculator.Position position = getPositionForEvent(event, data);
        return (position != null ? position.rightAscension : null);
    }

    @Nullable
    public static Double getShadowLengthForEvent(Context context, SolarEvents event, @Nullable SuntimesRiseSetData data)
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
    public static <T extends SuntimesData> Calendar getCalendarForEvent(SolarEvents event, @NonNull T data)
    {
        if (data == null || data.calculator() == null) {
            return null;
        }

        switch (event)
        {
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
    public static String getPatternForEvent(@NonNull String prefix, SolarEvents event)
    {
        switch (event)
        {
            case MORNING_ASTRONOMICAL: return prefix + "ar";
            case EVENING_ASTRONOMICAL: return prefix + "as";
            case MORNING_NAUTICAL: return prefix + "nr";
            case EVENING_NAUTICAL: return prefix + "ns";
            case MORNING_CIVIL: return prefix + "cr";
            case EVENING_CIVIL: return prefix + "cs";
            case SUNRISE: return prefix + "sr";
            case NOON: return prefix + "sn";
            //case MIDNIGHT: return prefix + "sm";    // TODO: midnight
            case SUNSET: return prefix + "ss";
            case MORNING_GOLDEN: return prefix + "gr";
            case EVENING_GOLDEN: return prefix + "gs";
            case MORNING_BLUE4: return prefix + "b4r";
            case EVENING_BLUE4: return prefix + "b4s";
            case MORNING_BLUE8: return prefix + "b8r";
            case EVENING_BLUE8: return prefix + "b8s";
            case MOONRISE: return prefix + "lr";
            case MOONSET: return prefix + "ls";
            case MOONNOON: return prefix + "ln";
            case MOONNIGHT: return prefix + "lm";
            default: return null;
        }
    }

    public interface PatternForEventInterface {
        String getPatternForEvent(SolarEvents event);
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

    @Nullable
    public static String getPatternForEvent_em(SolarEvents event) {
        return getPatternForEvent("%em@", event);    // miliseconds
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_em(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_em(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_et(SolarEvents event) {
        return getPatternForEvent("%et@", event);    // formatted time
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_et(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_et(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_eT(SolarEvents event) {
        return getPatternForEvent("%eT@", event);    // formatted time (wth seconds)
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_eT(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_eT(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_ea(SolarEvents event) {
        return getPatternForEvent("%ea@", event);    // angle (deg)
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_ea(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_ea(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_eA(SolarEvents event) {
        return getPatternForEvent("%eA@", event);    // formatted angle (deg)
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_eA(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_eA(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_ez(SolarEvents event) {
        return getPatternForEvent("%ez@", event);
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_ez(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_ez(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_eZ(SolarEvents event) {
        return getPatternForEvent("%eZ@", event);
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_eZ(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_eZ(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_ed(SolarEvents event) {
        return getPatternForEvent("%ed@", event);
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_ed(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_ed(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_eD(SolarEvents event) {
        return getPatternForEvent("%eD@", event);
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_eD(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_eD(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_er(SolarEvents event) {
        return getPatternForEvent("%er@", event);    // angle (deg)
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_er(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_er(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_eR(SolarEvents event) {
        return getPatternForEvent("%eR@", event);    // formatted angle (deg)
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_eR(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_eR(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_es(SolarEvents event) {
        return getPatternForEvent("%es@", event);    // shadow length (meters)
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_es(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_es(event);
            }
        });
    }

    @Nullable
    public static String getPatternForEvent_eS(SolarEvents event) {
        return getPatternForEvent("%eS@", event);    // formatted shadow length (meters or feet)
    }
    public static HashMap<SolarEvents, String> getPatternsForEvent_eS(SolarEvents[] events) {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return getPatternForEvent_eS(event);
            }
        });
    }

    public static String removePatterns(String displayString, Collection<String> patterns) {
        String value = displayString;
        for (String pattern : patterns) {
            value = value.replaceAll(pattern, "");
        }
        return value;
    }
    public static String removePattern(String displayString, String pattern) {
        return displayString.replaceAll(pattern, "");
    }

    /**
     * @param patterns hashmap of input patterns
     * @param appendTo the list that non-null patterns will be appended to
     * @return input patterns for method chaining
     */
    public static HashMap<SolarEvents, String> appendTo(HashMap<SolarEvents, String> patterns, List<String> appendTo)
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
    public static boolean containsAtLeastOne(String displayString, List<String> patterns)
    {
        for (String pattern : patterns) {
            if (displayString.contains(pattern)) {
                return true;
            }
        }
        return false;
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
        String displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData) data);
        String modePattern = "%M";
        String modePatternShort = "%m";
        String orderPattern = "%o";
        String[] patterns = new String[] { modePattern, modePatternShort, orderPattern };

        List<String> positionPatterns = new ArrayList<>();

        SolarEvents[] events = SolarEvents.values(); //{ SolarEvents.SUNRISE, SolarEvents.NOON, SolarEvents.SUNSET };
        HashMap<SolarEvents, String> patterns_em = appendTo(getPatternsForEvent_em(events), positionPatterns);
        HashMap<SolarEvents, String> patterns_et = appendTo(getPatternsForEvent_et(events), positionPatterns);
        HashMap<SolarEvents, String> patterns_eT = appendTo(getPatternsForEvent_eT(events), positionPatterns);
        HashMap<SolarEvents, String> patterns_ea = appendTo(getPatternsForEvent_ea(events), positionPatterns);   // angle/elevation
        HashMap<SolarEvents, String> patterns_eA = appendTo(getPatternsForEvent_eA(events), positionPatterns);   // angle/elevation (formatted)
        HashMap<SolarEvents, String> patterns_ez = appendTo(getPatternsForEvent_ez(events), positionPatterns);   // azimuth
        HashMap<SolarEvents, String> patterns_eZ = appendTo(getPatternsForEvent_eZ(events), positionPatterns);   // azimuth (formatted)
        HashMap<SolarEvents, String> patterns_ed = appendTo(getPatternsForEvent_ed(events), positionPatterns);   // declination
        HashMap<SolarEvents, String> patterns_eD = appendTo(getPatternsForEvent_eD(events), positionPatterns);   // declination (formatted)
        HashMap<SolarEvents, String> patterns_er = appendTo(getPatternsForEvent_er(events), positionPatterns);   // right-ascension
        HashMap<SolarEvents, String> patterns_eR = appendTo(getPatternsForEvent_eR(events), positionPatterns);   // right-ascension (formatted)
        HashMap<SolarEvents, String> patterns_es = appendTo(getPatternsForEvent_es(events), positionPatterns);   // shadow length (meters)
        HashMap<SolarEvents, String> patterns_eS = appendTo(getPatternsForEvent_eS(events), positionPatterns);   // shadow length display (formatted, meters or feet)

        if (data == null)
        {
            displayString = removePatterns(displayString, Arrays.asList(patterns));
            for (String pattern : positionPatterns) {
                displayString = removePattern(displayString, pattern);
            }
            return displayString;
        }

        WidgetSettings.TimeMode timeMode = null;
        String modeDisplayShort = "";
        String modeDisplayLong = "";
        if (data instanceof SuntimesRiseSetData)
        {
            SuntimesRiseSetData d = (SuntimesRiseSetData) data;
            timeMode = d.timeMode();
            modeDisplayShort = timeMode.getShortDisplayString();
            modeDisplayLong = timeMode.getLongDisplayString();

            WidgetSettings.RiseSetDataMode timeModeItem = d.dataMode();
            if (timeModeItem instanceof WidgetSettings.EventAliasTimeMode) {
                String label = EventSettings.loadEventValue(context, timeModeItem.name(), EventSettings.PREF_KEY_EVENT_LABEL);
                if (label != null) {
                    modeDisplayLong = modeDisplayShort = label;
                }
            }
        }
        displayString = displayString.replaceAll(modePatternShort, modeDisplayShort);
        displayString = displayString.replaceAll(modePattern, modeDisplayLong);

        WidgetSettings.RiseSetOrder order = WidgetSettings.loadRiseSetOrderPref(context, data.appWidgetID());
        displayString = displayString.replaceAll(orderPattern, order.toString());

        for (SolarEvents event : events)
        {
            if (!DataSubstitutions.containsAtLeastOne(displayString, positionPatterns)) {
                continue;
            }

            String pattern_em = patterns_em.get(event);
            String pattern_et = patterns_et.get(event);
            String pattern_eT = patterns_eT.get(event);
            String pattern_ea = patterns_ea.get(event);
            String pattern_eA = patterns_eA.get(event);
            String pattern_ez = patterns_ez.get(event);
            String pattern_eZ = patterns_eZ.get(event);
            String pattern_ed = patterns_ed.get(event);
            String pattern_eD = patterns_eD.get(event);
            String pattern_er = patterns_er.get(event);
            String pattern_eR = patterns_eR.get(event);
            String pattern_es = patterns_es.get(event);
            String pattern_eS = patterns_eS.get(event);

            Calendar eventTime;
            T d = data;
            if (data instanceof SuntimesRiseSetData)
            {
                SuntimesRiseSetData data0 = (SuntimesRiseSetData) data;
                d = (T) (event == SolarEvents.NOON && data0.getLinked() != null ? data0.getLinked() : data0);
                if (event == SolarEvents.SUNRISE) {
                    event = SolarEvents.valueOf(timeMode, true);
                } else if (event == SolarEvents.SUNSET) {
                    event = SolarEvents.valueOf(timeMode, false);
                }
                eventTime = ((SuntimesRiseSetData) d).getEvents(event.isRising())[0];

            } else {
                eventTime = getCalendarForEvent(event, data);
            }

            if (eventTime != null)
            {
                if (displayString.contains(pattern_em)) {
                    displayString = displayString.replaceAll(pattern_em, eventTime.getTimeInMillis() + "");
                }
                if (displayString.contains(pattern_et)) {
                    displayString = displayString.replaceAll(pattern_et, utils.calendarTimeShortDisplayString(context, eventTime, false).toString());
                }
                if (displayString.contains(pattern_eT)) {
                    displayString = displayString.replaceAll(pattern_eT, utils.calendarTimeShortDisplayString(context, eventTime, true).toString());
                }
                if (displayString.contains(pattern_ez) || displayString.contains(pattern_eZ)) {
                    Double value = getAzimuthForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_ez, (value != null ? value + "" : ""));
                    displayString = displayString.replaceAll(pattern_eZ, (value != null ? utils.formatAsDirection(value, 1) : ""));
                }
                if (displayString.contains(pattern_ed) || displayString.contains(pattern_eD)) {
                    Double value = getDeclinationForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_ed, (value != null ? value + "" : ""));
                    displayString = displayString.replaceAll(pattern_eD, (value != null ? utils.formatAsDeclination(value, 1).toString() : ""));
                }
                if (displayString.contains(pattern_er) || displayString.contains(pattern_eR)) {
                    Double value = getRightAscensionForEvent(event, d);
                    displayString = displayString.replaceAll(pattern_er, (value != null ? value + "" : ""));
                    displayString = displayString.replaceAll(pattern_eR, (value != null ? utils.formatAsRightAscension(value, 1).toString() : ""));
                }

                if (d instanceof SuntimesRiseSetData)
                {
                    SuntimesRiseSetData d0 = (SuntimesRiseSetData) d;
                    if (displayString.contains(pattern_ea) || displayString.contains(pattern_eA)) {
                        Double angle = (d0.angle() != null ? Double.valueOf(d0.angle()) : getAltitudeForEvent(event, d));
                        displayString = displayString.replaceAll(pattern_ea, (angle != null ? angle + "" : ""));
                        displayString = displayString.replaceAll(pattern_eA, (angle != null ? utils.formatAsDegrees(angle, 1) : ""));
                    }

                    if (displayString.contains(pattern_es) || displayString.contains(pattern_eS))
                    {
                        WidgetSettings.LengthUnit lengthUnit = WidgetSettings.loadLengthUnitsPref(context, data.appWidgetID());
                        Double value = getShadowLengthForEvent(context, event, d0);
                        displayString = displayString.replaceAll(pattern_es, (value != null ? value + "" : ""));
                        displayString = displayString.replaceAll(pattern_eS, (value != null ? utils.formatAsHeight(context, value, lengthUnit, 1, false).toString() : ""));
                    }

                } else {
                    if (displayString.contains(pattern_ea) || displayString.contains(pattern_eA)) {
                        Double angle = getAltitudeForEvent(event, d);
                        displayString = displayString.replaceAll(pattern_ea, (angle != null ? angle + "" : ""));
                        displayString = displayString.replaceAll(pattern_eA, (angle != null ? utils.formatAsDegrees(angle, 1) : ""));
                    }
                }

            } else {
                for (String pattern : positionPatterns) {
                    displayString = displayString.replaceAll(pattern, "");
                }
            }

        }

        return displayString;
    }

    /**
     * displayStringForTitlePattern
     * @param context
     * @param titlePattern
     * @param data
     * @return
     */
    public static String displayStringForTitlePattern(Context context, String titlePattern, @Nullable SuntimesData data)
    {
        String displayString = titlePattern;
        String locPattern = "%loc";
        String latPattern = "%lat";
        String lonPattern = "%lon";
        String altPattern = "%lel";
        String eotPattern = "%eot";
        String eotMillisPattern = "%eot_m";
        String timezoneIDPattern = "%t";
        String datasourcePattern = "%s";
        String widgetIDPattern = "%id";
        String datePattern = "%d";
        String dateYearPattern = "%dY";
        String dateDayPattern = "%dD";
        String dateDayPatternShort = "%dd";
        String dateTimePattern = "%dT";
        String dateTimePatternShort = "%dt";
        String dateMillisPattern = "%dm";
        String observerHeightPattern0 = "%h";
        String observerHeightPattern1 = "%H";
        String percentPattern = "%%";

        if (data == null)
        {
            String[] patterns = new String[] { locPattern, latPattern, lonPattern, altPattern,          // in order of operation
                    timezoneIDPattern, datasourcePattern, widgetIDPattern,
                    dateTimePatternShort, dateTimePattern, dateDayPatternShort, dateDayPattern, dateYearPattern, dateMillisPattern, datePattern,
                    observerHeightPattern0, observerHeightPattern1,
                    percentPattern };

            for (int i=0; i<patterns.length; i++) {
                displayString = displayString.replaceAll(patterns[i], "");
            }
            return displayString;
        }

        if (!data.isCalculated()) {
            data.calculate();
        }

        Location location = data.location();
        String timezoneID = data.timezone().getID();
        String datasource = (data.calculatorMode() == null) ? "" : data.calculatorMode().getName();
        String appWidgetID = (data.appWidgetID() != null ? String.format("%s", data.appWidgetID()) : "");

        displayString = displayString.replaceAll(locPattern, location.getLabel());
        displayString = displayString.replaceAll(latPattern, location.getLatitude());
        displayString = displayString.replaceAll(lonPattern, location.getLongitude());

        if (displayString.contains(altPattern))
        {
            String altitudeDisplay = (WidgetSettings.loadLengthUnitsPref(context, 0) == WidgetSettings.LengthUnit.IMPERIAL)
                    ? (int)WidgetSettings.LengthUnit.metersToFeet(location.getAltitudeAsDouble()) + ""
                    : location.getAltitudeAsInteger() + "";
            displayString = displayString.replaceAll(altPattern, altitudeDisplay);
        }

        if (displayString.contains(eotPattern) || displayString.contains(eotMillisPattern))
        {
            long eot = WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(data.calendar().getTimeInMillis(), data.calculator());
            displayString = displayString.replaceAll(eotMillisPattern, eot+"");
            displayString = displayString.replaceAll(eotPattern, ((eot < 0) ? "-" : "+") + utils.timeDeltaLongDisplayString(eot, true).getValue());
        }

        displayString = displayString.replaceAll(timezoneIDPattern, timezoneID);
        displayString = displayString.replaceAll(datasourcePattern, datasource);
        displayString = displayString.replaceAll(widgetIDPattern, appWidgetID);

        if (displayString.contains(datePattern))
        {
            displayString = displayString.replaceAll(dateTimePatternShort, utils.calendarTimeShortDisplayString(context, data.now(), false).toString());
            displayString = displayString.replaceAll(dateTimePattern, utils.calendarTimeShortDisplayString(context, data.now(), true).toString());
            displayString = displayString.replaceAll(dateDayPatternShort, utils.calendarDayDisplayString(context, data.calendar(), true).toString());
            displayString = displayString.replaceAll(dateDayPattern, utils.calendarDayDisplayString(context, data.calendar(), false).toString());
            displayString = displayString.replaceAll(dateYearPattern, utils.calendarDateYearDisplayString(context, data.calendar()).toString());
            displayString = displayString.replaceAll(dateMillisPattern, Long.toString(data.calendar().getTimeInMillis()));
            displayString = displayString.replaceAll(datePattern, utils.calendarDateDisplayString(context, data.calendar(), false).toString());
        }

        if (displayString.contains(observerHeightPattern0) || displayString.contains(observerHeightPattern1))
        {
            WidgetSettings.LengthUnit lengthUnit = WidgetSettings.loadLengthUnitsPref(context, data.appWidgetID());
            float height = WidgetSettings.loadObserverHeightPref(context, data.appWidgetID());    // %h
            displayString = displayString.replaceAll(observerHeightPattern0, height + "");
            displayString = displayString.replaceAll(observerHeightPattern1, utils.formatAsHeight(context, height, lengthUnit, 2, true).toString());    // %H
        }

        displayString = displayString.replaceAll(percentPattern, "%");
        return displayString;
    }

}
