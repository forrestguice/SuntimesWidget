/**
    Copyright (C) 2017-2018 Forrest Guice
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
import java.util.ArrayList;

@SuppressWarnings("Convert2Diamond")
public class SuntimesThemeXML implements SuntimesThemeIO
{
    public static final String KEY_THEMES = "themes";
    public static final String KEY_THEME = "theme";
    public static final String VERSION = "1.3";

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
    public boolean write(Context context, BufferedOutputStream out, SuntimesTheme[] themes) throws IOException
    {
        signalExportStarted();
        if (themes != null)
        {
            XmlSerializer xml = Xml.newSerializer();
            xml.setFeature(XML_INDENT, indent);
            xml.setOutput(out, encoding);
            xml.startDocument(encoding, true);

            xml.startTag(namespace, KEY_THEMES);
            xml.attribute(namespace, SuntimesTheme.THEME_VERSION, VERSION);

            int i = 0;
            int n = themes.length;
            for (SuntimesTheme theme : themes)
            {
                xml.startTag(namespace, KEY_THEME);
                xml.attribute(namespace, SuntimesTheme.THEME_NAME, theme.themeName());
                xml.attribute(namespace, SuntimesTheme.THEME_VERSION, Integer.toString(theme.themeVersion()));
                xml.attribute(namespace, SuntimesTheme.THEME_DISPLAYSTRING, theme.themeDisplayString());
                xml.attribute(namespace, SuntimesTheme.THEME_ISDEFAULT, Boolean.toString(theme.themeIsDefault));

                xml.startTag(namespace, SuntimesTheme.THEME_PADDING_LEFT);
                xml.text(Integer.toString(theme.themePadding[0]));
                xml.endTag(namespace, SuntimesTheme.THEME_PADDING_LEFT);

                xml.startTag(namespace, SuntimesTheme.THEME_PADDING_TOP);
                xml.text(Integer.toString(theme.themePadding[1]));
                xml.endTag(namespace, SuntimesTheme.THEME_PADDING_TOP);

                xml.startTag(namespace, SuntimesTheme.THEME_PADDING_RIGHT);
                xml.text(Integer.toString(theme.themePadding[2]));
                xml.endTag(namespace, SuntimesTheme.THEME_PADDING_RIGHT);

                xml.startTag(namespace, SuntimesTheme.THEME_PADDING_BOTTOM);
                xml.text(Integer.toString(theme.themePadding[3]));
                xml.endTag(namespace, SuntimesTheme.THEME_PADDING_BOTTOM);

                xml.startTag(namespace, SuntimesTheme.THEME_BACKGROUND);
                xml.text(theme.getBackground().name());
                xml.endTag(namespace, SuntimesTheme.THEME_BACKGROUND);

                xml.startTag(namespace, SuntimesTheme.THEME_TEXTCOLOR);
                xml.text(colorToString(theme.getTextColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_TEXTCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_TITLECOLOR);
                xml.text(colorToString(theme.getTitleColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_TITLECOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_TIMECOLOR);
                xml.text(colorToString(theme.getTimeColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_TIMECOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_TIMESUFFIXCOLOR);
                xml.text(colorToString(theme.getTimeSuffixColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_TIMESUFFIXCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_SUNRISECOLOR);
                xml.text(colorToString(theme.getSunriseTextColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_SUNRISECOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_RISEICON_FILL_COLOR);
                xml.text(colorToString(theme.getSunriseIconColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_RISEICON_FILL_COLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_RISEICON_STROKE_COLOR);
                xml.text(colorToString(theme.getSunriseIconStrokeColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_RISEICON_STROKE_COLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_RISEICON_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getSunriseIconStrokeWidth()));
                xml.endTag(namespace, SuntimesTheme.THEME_RISEICON_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesTheme.THEME_NOONCOLOR);
                xml.text(colorToString(theme.getNoonTextColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_NOONCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_NOONICON_FILL_COLOR);
                xml.text(colorToString(theme.getNoonIconColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_NOONICON_FILL_COLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_NOONICON_STROKE_COLOR);
                xml.text(colorToString(theme.getNoonIconStrokeColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_NOONICON_STROKE_COLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_NOONICON_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getNoonIconStrokeWidth()));
                xml.endTag(namespace, SuntimesTheme.THEME_NOONICON_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesTheme.THEME_SUNSETCOLOR);
                xml.text(colorToString(theme.getSunsetTextColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_SUNSETCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_SETICON_FILL_COLOR);
                xml.text(colorToString(theme.getSunsetIconColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_SETICON_FILL_COLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_SETICON_STROKE_COLOR);
                xml.text(colorToString(theme.getSunsetIconStrokeColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_SETICON_STROKE_COLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_SETICON_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getSunsetIconStrokeWidth()));
                xml.endTag(namespace, SuntimesTheme.THEME_SETICON_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesTheme.THEME_SPRINGCOLOR);
                xml.text(Integer.toString(theme.getSpringColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_SPRINGCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_SUMMERCOLOR);
                xml.text(Integer.toString(theme.getSummerColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_SUMMERCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_FALLCOLOR);
                xml.text(Integer.toString(theme.getFallColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_FALLCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_WINTERCOLOR);
                xml.text(Integer.toString(theme.getWinterColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_WINTERCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_MOONRISECOLOR);
                xml.text(Integer.toString(theme.getMoonriseTextColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_MOONRISECOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_MOONSETCOLOR);
                xml.text(Integer.toString(theme.getMoonsetTextColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_MOONSETCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_MOONWANINGCOLOR);
                xml.text(Integer.toString(theme.getMoonWaningColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_MOONWANINGCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_MOONWAXINGCOLOR);
                xml.text(Integer.toString(theme.getMoonWaxingColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_MOONWAXINGCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_MOONFULLCOLOR);
                xml.text(Integer.toString(theme.getMoonFullColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_MOONFULLCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_MOONNEWCOLOR);
                xml.text(Integer.toString(theme.getMoonNewColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_MOONNEWCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_MOONFULL_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getMoonFullStroke()));
                xml.endTag(namespace, SuntimesTheme.THEME_MOONFULL_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesTheme.THEME_MOONNEW_STROKE_WIDTH);
                xml.text(Integer.toString(theme.getMoonNewStroke()));
                xml.endTag(namespace, SuntimesTheme.THEME_MOONNEW_STROKE_WIDTH);

                xml.startTag(namespace, SuntimesTheme.THEME_TIMEBOLD);
                xml.text(Boolean.toString(theme.getTimeBold()));
                xml.endTag(namespace, SuntimesTheme.THEME_TIMEBOLD);

                xml.startTag(namespace, SuntimesTheme.THEME_TITLEBOLD);
                xml.text(Boolean.toString(theme.getTitleBold()));
                xml.endTag(namespace, SuntimesTheme.THEME_TITLEBOLD);

                xml.startTag(namespace, SuntimesTheme.THEME_TITLESIZE);
                xml.text(Float.toString(theme.getTitleSizeSp()));
                xml.endTag(namespace, SuntimesTheme.THEME_TITLESIZE);

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
                            themes = readThemes(parser);
                        }
                        break;
                }
                parseEvent = parser.next();
            }

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
                        SuntimesTheme theme = readTheme(parser);
                        themes.add(theme);
                        signalImportProgress(theme, i, n);
                        n = i = i + 1;
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (tag.equalsIgnoreCase(KEY_THEMES))
                    {
                        break loop;
                    }
                    break;
            }
        }
        SuntimesTheme themesArray[] = new SuntimesTheme[themes.size()];
        return themes.toArray(themesArray);
    }

    private SuntimesTheme readTheme(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        SuntimesTheme theme = new SuntimesTheme();
        String themeName = parser.getAttributeValue(namespace, SuntimesTheme.THEME_NAME);
        if (themeName == null)
        {
            Log.w("readTheme", "missing " + SuntimesTheme.THEME_VERSION);
            theme.themeName = "";
        } else {
            theme.themeName = themeName;
        }

        String themeVersion = parser.getAttributeValue(namespace, SuntimesTheme.THEME_VERSION);
        if (themeVersion == null)
        {
            Log.w("readTheme", "missing " + SuntimesTheme.THEME_VERSION);
        } else {
            theme.themeVersion = Integer.parseInt(themeVersion);
        }

        String themeString = parser.getAttributeValue(namespace, SuntimesTheme.THEME_DISPLAYSTRING);
        if (themeString == null)
        {
            Log.w("readTheme", "missing " + SuntimesTheme.THEME_DISPLAYSTRING);
            theme.themeDisplayString = theme.themeName;

        } else {
            theme.themeDisplayString = themeString;
        }

        int parseEvent = parser.next();
        loop: while (parseEvent != XmlPullParser.END_DOCUMENT)
        {
            String tag = parser.getName();
            switch (parseEvent)
            {
                case XmlPullParser.TEXT:
                    String value = parser.getText();
                    if (tag.equalsIgnoreCase(SuntimesTheme.THEME_BACKGROUND))
                    {
                        theme.themeBackground = ThemeBackground.getThemeBackground(backgroundStringToId(value));

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_PADDING_LEFT)) {
                        theme.themePadding[0] = Integer.parseInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_PADDING_TOP)) {
                        theme.themePadding[1] = Integer.parseInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_PADDING_RIGHT)) {
                        theme.themePadding[2] = Integer.parseInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_PADDING_BOTTOM)) {
                        theme.themePadding[3] = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_TITLECOLOR)) {
                        theme.themeTitleColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_TEXTCOLOR)) {
                        theme.themeTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_TIMECOLOR)) {
                        theme.themeTimeColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_TIMESUFFIXCOLOR)) {
                        theme.themeTimeSuffixColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_SUNRISECOLOR)) {
                        theme.themeSunriseTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_RISEICON_FILL_COLOR)) {
                        theme.themeSunriseIconColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_RISEICON_STROKE_COLOR)) {
                        theme.themeSunriseIconStrokeColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_RISEICON_STROKE_WIDTH)) {
                        theme.themeSunriseIconStrokeWidth = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_NOONCOLOR)) {
                        theme.themeNoonTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_NOONICON_FILL_COLOR)) {
                        theme.themeNoonIconColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_NOONICON_STROKE_COLOR)) {
                        theme.themeNoonIconStrokeColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_NOONICON_STROKE_WIDTH)) {
                        theme.themeNoonIconStrokeWidth = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_SUNSETCOLOR)) {
                        theme.themeSunsetTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_SETICON_FILL_COLOR)) {
                        theme.themeSunsetIconColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_SETICON_STROKE_COLOR)) {
                        theme.themeSunsetIconStrokeColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_SETICON_STROKE_WIDTH)) {
                        theme.themeSunsetIconStrokeWidth = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_SPRINGCOLOR)) {
                        theme.themeSpringColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_SUMMERCOLOR)) {
                        theme.themeSummerColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_FALLCOLOR)) {
                        theme.themeFallColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_WINTERCOLOR)) {
                        theme.themeWinterColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_MOONRISECOLOR)) {
                        theme.themeMoonriseTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_MOONSETCOLOR)) {
                        theme.themeMoonsetTextColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_MOONWANINGCOLOR)) {
                        theme.themeMoonWaningColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_MOONWAXINGCOLOR)) {
                        theme.themeMoonWaxingColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_MOONFULLCOLOR)) {
                        theme.themeMoonFullColor = colorStringToInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_MOONNEWCOLOR)) {
                        theme.themeMoonNewColor = colorStringToInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_MOONFULL_STROKE_WIDTH)) {
                        theme.themeMoonFullStroke = Integer.parseInt(value);
                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_MOONNEW_STROKE_WIDTH)) {
                        theme.themeMoonNewStroke = Integer.parseInt(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_TIMEBOLD)) {
                        theme.themeTimeBold = Boolean.parseBoolean(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_TITLEBOLD)) {
                        theme.themeTitleBold = Boolean.parseBoolean(value);

                    } else if (tag.equalsIgnoreCase(SuntimesTheme.THEME_TITLESIZE)) {
                        theme.themeTitleSize = Float.parseFloat(value);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (tag.equalsIgnoreCase(KEY_THEME))
                    {
                        break loop;
                    }
                    break;
            }
        }
        return theme;
    }

    private int colorStringToInt(String value)
    {
        return Color.parseColor(value);
    }
    private String colorToString(int color)
    {
        return String.format("#%08X", color);
    }

    private int backgroundStringToId(String value)
    {
        try {
            ThemeBackground background = ThemeBackground.valueOf(value);
            return background.getResID();

        } catch (IllegalArgumentException e) {
            Log.e("backgroundStringToId", "Background " + value + " not found. " + e);
        }
        return ThemeBackground.DARK.getResID();
    }

}
