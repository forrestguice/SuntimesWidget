/**
    Copyright (C) 2017-2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.themes;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

@SuppressWarnings("Convert2Diamond")
public class SuntimesThemeXML implements SuntimesThemeIO
{
    public static final String KEY_THEMES = "themes";
    public static final String KEY_THEME = "theme";
    public static final String VERSION = "1.5";

    public SuntimesThemeXML() {}

    /**
     * Property: namespace
     */
    private String namespace = null;
    public String getNamespace()
    {
        return namespace;
    }
    public void setNamespace(String value)
    {
        namespace = value;
    }

    /**
     * Property: encoding
     */
    private String encoding = "UTF-8";
    public String getEncoding()
    {
        return encoding;
    }
    public void setEncoding(String value)
    {
        encoding = value;
    }

    /**
     * Property: indent
     */
    private static final String XML_INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output";
    private boolean indent = true;
    public boolean indent()
    {
        return indent;
    }
    public void setIndent(boolean value)
    {
        indent = value;
    }

    /**
     * Property: progress listener
     */
    protected ProgressListener listener = null;
    @Override
    public void setProgressListener( ProgressListener listener )
    {
        this.listener = listener;
    }
    @Override
    public void clearProgressListener()
    {
        listener = null;
    }
    private void signalExportStarted()
    {
        if (listener != null)
        {
            listener.onExportStarted();
        }
    }
    private void signalExportProgress(SuntimesTheme theme, int i, int n)
    {
        if (listener != null)
        {
            listener.onExported(theme, i, n);
        }
    }
    private void signalExportFinished( boolean value )
    {
        if (listener != null)
        {
            listener.onExportFinished(value);
        }
    }

    private void signalImportStarted()
    {
        if (listener != null)
        {
            listener.onImportStarted();
        }
    }
    private void signalImportProgress(SuntimesTheme theme, int i, int n)
    {
        if (listener != null)
        {
            listener.onImported(theme, i, n);
        }
    }
    private void signalImportFinished( boolean value )
    {
        if (listener != null)
        {
            listener.onImportFinished(value);
        }
    }

    /**
     * @param context a context used to access resources
     * @param out a BufferedOutputStream (open and ready to be written to)
     * @param themes an array of themes to be written
     * @return true themes written, false otherwise
     * @throws IOException if failed to write to out
     */
    @Override
    public boolean write(Context context, OutputStream out, SuntimesTheme[] themes) throws IOException
    {
        signalExportStarted();
        if (themes != null)
        {
            XmlSerializer xml = Xml.newSerializer();
            xml.setFeature(XML_INDENT, indent);
            xml.setOutput(out, encoding);
            xml.startDocument(encoding, true);

            xml.startTag(namespace, KEY_THEMES);
            xml.attribute(namespace, SuntimesThemeContract.THEME_VERSION, VERSION);

            int i = 0;
            int n = themes.length;
            for (SuntimesTheme theme : themes)
            {
                xml.startTag(namespace, KEY_THEME);
                xml.attribute(namespace, SuntimesThemeContract.THEME_NAME, theme.themeName());
                xml.attribute(namespace, SuntimesThemeContract.THEME_VERSION, Integer.toString(theme.themeVersion()));
                xml.attribute(namespace, SuntimesThemeContract.THEME_DISPLAYSTRING, theme.themeDisplayString());
                xml.attribute(namespace, SuntimesThemeContract.THEME_ISDEFAULT, Boolean.toString(theme.themeIsDefault));

                xml.startTag(namespace, SuntimesThemeContract.THEME_PADDING_LEFT);
                xml.text(Integer.toString(theme.themePadding[0]));
                xml.endTag(namespace, SuntimesThemeContract.THEME_PADDING_LEFT);

                xml.startTag(namespace, SuntimesThemeContract.THEME_PADDING_TOP);
                xml.text(Integer.toString(theme.themePadding[1]));
                xml.endTag(namespace, SuntimesThemeContract.THEME_PADDING_TOP);

                xml.startTag(namespace, SuntimesThemeContract.THEME_PADDING_RIGHT);
                xml.text(Integer.toString(theme.themePadding[2]));
                xml.endTag(namespace, SuntimesThemeContract.THEME_PADDING_RIGHT);

                xml.startTag(namespace, SuntimesThemeContract.THEME_PADDING_BOTTOM);
                xml.text(Integer.toString(theme.themePadding[3]));
                xml.endTag(namespace, SuntimesThemeContract.THEME_PADDING_BOTTOM);

                xml.startTag(namespace, SuntimesThemeContract.THEME_BACKGROUND);
                xml.text(theme.getBackground().name());
                xml.endTag(namespace, SuntimesThemeContract.THEME_BACKGROUND);

                xml.startTag(namespace, SuntimesThemeContract.THEME_BACKGROUND_COLOR);
                xml.text(colorToString(theme.getBackgroundColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_BACKGROUND_COLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_TEXTCOLOR);
                xml.text(colorToString(theme.getTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_TEXTCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_TITLECOLOR);
                xml.text(colorToString(theme.getTitleColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_TITLECOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_TIMECOLOR);
                xml.text(colorToString(theme.getTimeColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_TIMECOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_TIMESUFFIXCOLOR);
                xml.text(colorToString(theme.getTimeSuffixColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_TIMESUFFIXCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_ACTIONCOLOR);
                xml.text(colorToString(theme.getActionColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_ACTIONCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_ACCENTCOLOR);
                xml.text(colorToString(theme.getAccentColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_ACCENTCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_SUNRISECOLOR);
                xml.text(colorToString(theme.getSunriseTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_SUNRISECOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_RISEICON_FILL_COLOR);
                xml.text(colorToString(theme.getSunriseIconColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_RISEICON_FILL_COLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_RISEICON_STROKE_COLOR);
                xml.text(colorToString(theme.getSunriseIconStrokeColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_RISEICON_STROKE_COLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getSunriseIconStrokeWidth()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesThemeContract.THEME_NOONCOLOR);
                xml.text(colorToString(theme.getNoonTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_NOONCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_NOONICON_FILL_COLOR);
                xml.text(colorToString(theme.getNoonIconColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_NOONICON_FILL_COLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_NOONICON_STROKE_COLOR);
                xml.text(colorToString(theme.getNoonIconStrokeColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_NOONICON_STROKE_COLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getNoonIconStrokeWidth()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesThemeContract.THEME_SUNSETCOLOR);
                xml.text(colorToString(theme.getSunsetTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_SUNSETCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_SETICON_FILL_COLOR);
                xml.text(colorToString(theme.getSunsetIconColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_SETICON_FILL_COLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_SETICON_STROKE_COLOR);
                xml.text(colorToString(theme.getSunsetIconStrokeColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_SETICON_STROKE_COLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getSunsetIconStrokeWidth()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesThemeContract.THEME_DAYCOLOR);
                xml.text(colorToString(theme.getDayColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_DAYCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_CIVILCOLOR);
                xml.text(colorToString(theme.getCivilColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_CIVILCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_NAUTICALCOLOR);
                xml.text(colorToString(theme.getNauticalColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_NAUTICALCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_ASTROCOLOR);
                xml.text(colorToString(theme.getAstroColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_ASTROCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_NIGHTCOLOR);
                xml.text(colorToString(theme.getNightColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_NIGHTCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_SPRINGCOLOR);
                xml.text(colorToString(theme.getSpringColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_SPRINGCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_SUMMERCOLOR);
                xml.text(colorToString(theme.getSummerColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_SUMMERCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_FALLCOLOR);
                xml.text(colorToString(theme.getFallColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_FALLCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_WINTERCOLOR);
                xml.text(colorToString(theme.getWinterColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_WINTERCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONRISECOLOR);
                xml.text(colorToString(theme.getMoonriseTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONRISECOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONSETCOLOR);
                xml.text(colorToString(theme.getMoonsetTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONSETCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONWANINGCOLOR);
                xml.text(colorToString(theme.getMoonWaningColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONWANINGCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONWAXINGCOLOR);
                xml.text(colorToString(theme.getMoonWaxingColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONWAXINGCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONFULLCOLOR);
                xml.text(colorToString(theme.getMoonFullColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONFULLCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONNEWCOLOR);
                xml.text(colorToString(theme.getMoonNewColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONNEWCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONWANINGCOLOR_TEXT);
                xml.text(colorToString(theme.getMoonWaningTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONWANINGCOLOR_TEXT);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONWAXINGCOLOR_TEXT);
                xml.text(colorToString(theme.getMoonWaxingTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONWAXINGCOLOR_TEXT);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONFULLCOLOR_TEXT);
                xml.text(colorToString(theme.getMoonFullTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONFULLCOLOR_TEXT);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONNEWCOLOR_TEXT);
                xml.text(colorToString(theme.getMoonNewTextColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONNEWCOLOR_TEXT);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONFULL_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getMoonFullStroke()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONFULL_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MOONNEW_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getMoonNewStroke()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MOONNEW_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesThemeContract.THEME_TIMEBOLD);
                xml.text(Boolean.toString(theme.getTimeBold()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_TIMEBOLD);

                xml.startTag(namespace, SuntimesThemeContract.THEME_TITLEBOLD);
                xml.text(Boolean.toString(theme.getTitleBold()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_TITLEBOLD);

                xml.startTag(namespace, SuntimesThemeContract.THEME_TITLESIZE);
                xml.text(Float.toString(theme.getTitleSizeSp()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_TITLESIZE);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MAP_BACKGROUNDCOLOR);
                xml.text(colorToString(theme.getMapBackgroundColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MAP_BACKGROUNDCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MAP_FOREGROUNDCOLOR);
                xml.text(colorToString(theme.getMapForegroundColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MAP_FOREGROUNDCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MAP_SHADOWCOLOR);
                xml.text(colorToString(theme.getMapShadowColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MAP_SHADOWCOLOR);

                xml.startTag(namespace, SuntimesThemeContract.THEME_MAP_HIGHLIGHTCOLOR);
                xml.text(colorToString(theme.getMapHighlightColor()));
                xml.endTag(namespace, SuntimesThemeContract.THEME_MAP_HIGHLIGHTCOLOR);

                xml.endTag(null, KEY_THEME);
                signalExportProgress(theme, i, n);
                i++;
            }
            xml.endTag(null, KEY_THEMES);

            xml.endDocument();
            xml.flush();
            signalExportFinished(true);
            return true;
        }
        signalExportFinished(false);
        return false;
    }

    /**
     * @param context a context used to access resources
     * @param in a BufferedInputStream that is open and ready to be read from
     * @return an array of SuntimesTheme
     * @throws IOException if failed to read from in
     */
    @Override
    public SuntimesTheme[] read(Context context, BufferedInputStream in) throws IOException
    {
        signalImportStarted();
        SuntimesTheme themes[] = new SuntimesTheme[0];
        boolean noErrors = true;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);

            XmlPullParser parser = factory.newPullParser();
            parser.setInput(in, null);

            int parseEvent = parser.getEventType();
            while (parseEvent != XmlPullParser.END_DOCUMENT)
            {
                String tag = parser.getName();
                switch (parseEvent)
                {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase(KEY_THEMES))
                        {
                            //Log.d("SuntimesThemeXML.read", "START_TAG: " + tag);
                            themes = readThemes(parser);
                        } else Log.w("SuntimesThemeXML.read", "unrecognized: " + tag);
                        break;
                }
                parseEvent = parser.next();
            }
            //Log.d("SuntimesThemeXML.read", "done");

        } catch (XmlPullParserException e1) {
            Log.e("SuntimesThemeXML.read", "Failed to parse themes :: " + e1);
            noErrors = false;

        } catch (IOException e2) {
            Log.e("SuntimesThemeXML.read", "Failed to read themes :: " + e2);
            noErrors = false;
        }
        signalImportFinished(noErrors && themes.length > 0);
        return themes;
    }

    private SuntimesTheme[] readThemes(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        int i = 0, n = 0;
        ArrayList<SuntimesTheme> themes = new ArrayList<SuntimesTheme>();
        int parseEvent = parser.next();
        loop: while (parseEvent != XmlPullParser.END_DOCUMENT)
        {
            String tag = parser.getName();
            switch (parseEvent)
            {
                case XmlPullParser.START_TAG:
                    if (tag.equalsIgnoreCase(KEY_THEME))
                    {
                        //Log.d("SuntimesThemeXML.read", "START_TAG: " + tag);
                        SuntimesTheme theme = readTheme(parser);
                        themes.add(theme);
                        signalImportProgress(theme, i, n);
                        n = i = i + 1;
                        //Log.d("SuntimesThemeXML.read", "read " + theme.themeName);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (tag.equalsIgnoreCase(KEY_THEMES))
                    {
                        //Log.d("SuntimesThemeXML.read", "END_TAG: " + tag);
                        break loop;
                    }
                    break;

                default:
                    //Log.d("SuntimesThemeXML.read", "unrecognized: " + tag);
                    break;
            }
            parseEvent = parser.next();
        }
        SuntimesTheme themesArray[] = new SuntimesTheme[themes.size()];
        return themes.toArray(themesArray);
    }

    private SuntimesTheme readTheme(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        SuntimesTheme theme = new SuntimesTheme();
        String themeName = parser.getAttributeValue(namespace, SuntimesThemeContract.THEME_NAME);
        if (themeName == null)
        {
            Log.w("readTheme", "missing " + SuntimesThemeContract.THEME_VERSION);
            theme.themeName = "";
        } else {
            theme.themeName = themeName;
        }

        String themeVersion = parser.getAttributeValue(namespace, SuntimesThemeContract.THEME_VERSION);
        if (themeVersion == null)
        {
            Log.w("readTheme", "missing " + SuntimesThemeContract.THEME_VERSION);
        } else {
            theme.themeVersion = Integer.parseInt(themeVersion);
        }

        String themeString = parser.getAttributeValue(namespace, SuntimesThemeContract.THEME_DISPLAYSTRING);
        if (themeString == null)
        {
            Log.w("readTheme", "missing " + SuntimesThemeContract.THEME_DISPLAYSTRING);
            theme.themeDisplayString = theme.themeName;

        } else {
            theme.themeDisplayString = themeString;
        }

        int parseEvent = parser.next();
        loop: while (parseEvent != XmlPullParser.END_DOCUMENT)
        {
            String tag = parser.getName();
            if (tag == null) {
                parseEvent = parser.nextTag();
                continue;
            }

            switch (parseEvent)
            {
                case XmlPullParser.START_TAG:
                    parser.next();
                    String value = parser.getText();
                    //Log.d("readTheme", "TEXT: " + tag + " : " + value);

                    if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_BACKGROUND))
                    {
                        theme.themeBackground = SuntimesTheme.ThemeBackground.getThemeBackground(backgroundStringToId(value));
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_BACKGROUND_COLOR)) {
                        theme.themeBackgroundColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_PADDING_LEFT)) {
                        theme.themePadding[0] = Integer.parseInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_PADDING_TOP)) {
                        theme.themePadding[1] = Integer.parseInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_PADDING_RIGHT)) {
                        theme.themePadding[2] = Integer.parseInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_PADDING_BOTTOM)) {
                        theme.themePadding[3] = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_TITLECOLOR)) {
                        theme.themeTitleColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_TEXTCOLOR)) {
                        theme.themeTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_TIMECOLOR)) {
                        theme.themeTimeColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR)) {
                        theme.themeTimeSuffixColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_ACTIONCOLOR)) {
                        theme.themeActionColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_ACCENTCOLOR)) {
                        theme.themeAccentColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_SUNRISECOLOR)) {
                        theme.themeSunriseTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_RISEICON_FILL_COLOR)) {
                        theme.themeSunriseIconColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_RISEICON_STROKE_COLOR)) {
                        theme.themeSunriseIconStrokeColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH)) {
                        theme.themeSunriseIconStrokeWidth = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_NOONCOLOR)) {
                        theme.themeNoonTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_NOONICON_FILL_COLOR)) {
                        theme.themeNoonIconColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_NOONICON_STROKE_COLOR)) {
                        theme.themeNoonIconStrokeColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH)) {
                        theme.themeNoonIconStrokeWidth = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_SUNSETCOLOR)) {
                        theme.themeSunsetTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_SETICON_FILL_COLOR)) {
                        theme.themeSunsetIconColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_SETICON_STROKE_COLOR)) {
                        theme.themeSunsetIconStrokeColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH)) {
                        theme.themeSunsetIconStrokeWidth = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_DAYCOLOR)) {
                        theme.themeDayColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_CIVILCOLOR)) {
                        theme.themeCivilColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_NAUTICALCOLOR)) {
                        theme.themeNauticalColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_ASTROCOLOR)) {
                        theme.themeAstroColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_NIGHTCOLOR)) {
                        theme.themeNightColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_SPRINGCOLOR)) {
                        theme.themeSpringColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_SUMMERCOLOR)) {
                        theme.themeSummerColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_FALLCOLOR)) {
                        theme.themeFallColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_WINTERCOLOR)) {
                        theme.themeWinterColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONRISECOLOR)) {
                        theme.themeMoonriseTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONSETCOLOR)) {
                        theme.themeMoonsetTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONWANINGCOLOR)) {
                        theme.themeMoonWaningColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONWAXINGCOLOR)) {
                        theme.themeMoonWaxingColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONFULLCOLOR)) {
                        theme.themeMoonFullColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONNEWCOLOR)) {
                        theme.themeMoonNewColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONWANINGCOLOR_TEXT)) {
                        theme.themeMoonWaningTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONWAXINGCOLOR_TEXT)) {
                        theme.themeMoonWaxingTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONFULLCOLOR_TEXT)) {
                        theme.themeMoonFullTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONNEWCOLOR_TEXT)) {
                        theme.themeMoonNewTextColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONFULL_STROKE_WIDTH)) {
                        theme.themeMoonFullStroke = Integer.parseInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MOONNEW_STROKE_WIDTH)) {
                        theme.themeMoonNewStroke = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MAP_BACKGROUNDCOLOR)) {
                        theme.themeMapBackgroundColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MAP_FOREGROUNDCOLOR)) {
                        theme.themeMapForegroundColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MAP_SHADOWCOLOR)) {
                        theme.themeMapShadowColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_MAP_HIGHLIGHTCOLOR)) {
                        theme.themeMapHighlightColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_TIMEBOLD)) {
                        theme.themeTimeBold = Boolean.parseBoolean(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_TITLEBOLD)) {
                        theme.themeTitleBold = Boolean.parseBoolean(value);

                    } else if (tag.equalsIgnoreCase(SuntimesThemeContract.THEME_TITLESIZE)) {
                        theme.themeTitleSize = Float.parseFloat(value);
                    }
                    parseEvent = parser.nextTag();
                    break;

                case XmlPullParser.END_TAG:
                    if (tag.equalsIgnoreCase(KEY_THEME)) {
                        break loop;
                    }
                    parseEvent = parser.nextTag();
                    break;

                default:
                    //Log.d("readTheme", "unhandled: " + parseEvent);
                    parseEvent = parser.next();
                    break;
            }
        }

        if (theme.themeBackground == null) {
            theme.themeBackground = SuntimesTheme.ThemeBackground.DARK;
        }
        if (theme.themeActionColor == 0) {
            theme.themeActionColor = theme.themeSunsetIconColor;
        }
        if (theme.themeAccentColor == 0) {
            theme.themeAccentColor = theme.themeSunsetIconColor;
        }
        return theme;
    }

    private int colorStringToInt(String value)
    {
        try {
            return Color.parseColor(value);
        } catch (IllegalArgumentException e) {
            return Integer.parseInt(value);
        }
    }
    private String colorToString(int color)
    {
        return String.format("#%08X", color);
    }

    private int backgroundStringToId(String value)
    {
        try {
            SuntimesTheme.ThemeBackground background = SuntimesTheme.ThemeBackground.valueOf(value);
            return background.getResID();

        } catch (IllegalArgumentException e) {
            Log.e("backgroundStringToId", "Background " + value + " not found. " + e);
        }
        return SuntimesTheme.ThemeBackground.DARK.getResID();
    }

}
