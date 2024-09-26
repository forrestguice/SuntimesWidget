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
            case MORNING_ASTRONOMICAL: return prefix + SUFFIX_MORNING_ASTRONOMICAL;
            case EVENING_ASTRONOMICAL: return prefix + SUFFIX_EVENING_ASTRONOMICAL;
            case MORNING_NAUTICAL: return prefix + SUFFIX_MORNING_NAUTICAL;
            case EVENING_NAUTICAL: return prefix + SUFFIX_EVENING_NAUTICAL;
            case MORNING_CIVIL: return prefix + SUFFIX_MORNING_CIVIL;
            case EVENING_CIVIL: return prefix + SUFFIX_EVENING_CIVIL;
            case SUNRISE: return prefix + SUFFIX_SUNRISE;
            case NOON: return prefix + SUFFIX_NOON;
            //case MIDNIGHT: return prefix + SUFFIX_MIDNIGHT;    // TODO: midnight
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

    public static HashMap<SolarEvents, String> getPatternsForEvent(final String pattern, SolarEvents[] events)
    {
        return getPatternsForEvent(events, new HashMap<SolarEvents, String>(), new PatternForEventInterface() {
            public String getPatternForEvent(SolarEvents event) {
                return DataSubstitutions.getPatternForEvent(pattern, event);
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
        String[] patterns = new String[] { PATTERN_M, PATTERN_m, PATTERN_o };

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
        displayString = displayString.replaceAll(PATTERN_m, modeDisplayShort);
        displayString = displayString.replaceAll(PATTERN_M, modeDisplayLong);

        WidgetSettings.RiseSetOrder order = WidgetSettings.loadRiseSetOrderPref(context, data.appWidgetID());
        displayString = displayString.replaceAll(PATTERN_o, order.toString());

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
        String locPattern = PATTERN_loc;            // "%loc";
        String latPattern = PATTERN_lat;            // "%lat";
        String lonPattern = PATTERN_lon;            // "%lon";
        String altPattern = PATTERN_lel;            // "%lel";
        String eotPattern = PATTERN_eot;            // "%eot";
        String eotMillisPattern = PATTERN_eot_m;    // "%eot_m";
        String timezoneIDPattern = PATTERN_t;       // "%t";
        String datasourcePattern = PATTERN_s;       // "%s";
        String widgetIDPattern = PATTERN_id;        // "%id";
        String datePattern = PATTERN_d;             // "%d";
        String dateYearPattern = PATTERN_dY;        // "%dY";
        String dateDayPattern = PATTERN_dD;         // "%dD";
        String dateDayPatternShort = PATTERN_dd;    //"%dd";
        String dateTimePattern = PATTERN_dT;        //"%dT";
        String dateTimePatternShort = PATTERN_dt;   //"%dt";
        String dateMillisPattern = PATTERN_dm;      //"%dm";
        String observerHeightPattern0 = PATTERN_h;  //"%h";
        String observerHeightPattern1 = PATTERN_H;  //"%H";
        String percentPattern = PATTERN_PERCENT;    // "%%";

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
