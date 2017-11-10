/**
    Copyright (C) 2017 Forrest Guice
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

public class SuntimesThemeXML implements SuntimesThemeIO
{
    public static final String KEY_THEMES = "themes";
    public static final String KEY_THEME = "theme";

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
     * @param context
     * @param out
     * @param themes
     * @return
     * @throws IOException
     */
    @Override
    public boolean write(Context context, BufferedOutputStream out, SuntimesTheme[] themes) throws IOException
    {
        if (themes != null)
        {
            XmlSerializer xml = Xml.newSerializer();
            xml.setFeature(XML_INDENT, indent);
            xml.setOutput(out, encoding);
            xml.startDocument(encoding, true);

            xml.startTag(namespace, KEY_THEMES);
            for (SuntimesTheme theme : themes)
            {
                xml.startTag(namespace, KEY_THEME);
                xml.attribute(namespace, SuntimesTheme.THEME_NAME, theme.themeName());
                xml.attribute(namespace, SuntimesTheme.THEME_VERSION, theme.themeVersion() + "");
                xml.attribute(namespace, SuntimesTheme.THEME_DISPLAYSTRING, theme.themeDisplayString());

                xml.startTag(namespace, SuntimesTheme.THEME_PADDING_LEFT);
                xml.text(theme.themePadding[0] + "");
                xml.endTag(namespace, SuntimesTheme.THEME_PADDING_LEFT);

                xml.startTag(namespace, SuntimesTheme.THEME_PADDING_TOP);
                xml.text(theme.themePadding[1] + "");
                xml.endTag(namespace, SuntimesTheme.THEME_PADDING_TOP);

                xml.startTag(namespace, SuntimesTheme.THEME_PADDING_RIGHT);
                xml.text(theme.themePadding[2] + "");
                xml.endTag(namespace, SuntimesTheme.THEME_PADDING_RIGHT);

                xml.startTag(namespace, SuntimesTheme.THEME_PADDING_BOTTOM);
                xml.text(theme.themePadding[3] + "");
                xml.endTag(namespace, SuntimesTheme.THEME_PADDING_BOTTOM);

                xml.startTag(namespace, SuntimesTheme.THEME_BACKGROUND);
                xml.text(backgroundIdToString(theme.getBackgroundId()));
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

                xml.startTag(namespace, SuntimesTheme.THEME_SUNSETCOLOR);
                xml.text(colorToString(theme.getSunsetTextColor()));
                xml.endTag(namespace, SuntimesTheme.THEME_SUNSETCOLOR);

                xml.startTag(namespace, SuntimesTheme.THEME_TITLESIZE);
                xml.text(theme.getTitleSizeSp() + "");
                xml.endTag(namespace, SuntimesTheme.THEME_TITLESIZE);

                xml.endTag(null, KEY_THEME);
            }
            xml.endTag(null, KEY_THEMES);

            xml.endDocument();
            xml.flush();
            return true;
        }
        return false;
    }

    /**
     * @param context
     * @param in
     * @return
     * @throws IOException
     */
    @Override
    public SuntimesTheme[] read(Context context, BufferedInputStream in) throws IOException
    {
        SuntimesTheme themes[] = new SuntimesTheme[0];
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
            Log.e("SuntimesThemeXML.read", "Failed to read themes :: " + e1);

        } catch (IOException e2) {
            Log.e("SuntimesThemeXML.read", "Failed to read themes :: " + e2);
        }
        return themes;
    }

    private SuntimesTheme[] readThemes(XmlPullParser parser) throws XmlPullParserException, IOException
    {
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
                    if (value.equalsIgnoreCase(SuntimesTheme.THEME_BACKGROUND))
                    {
                        theme.themeBackground = backgroundStringToId(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_PADDING_LEFT)) {
                        theme.themePadding[0] = Integer.parseInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_PADDING_TOP)) {
                        theme.themePadding[1] = Integer.parseInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_PADDING_RIGHT)) {
                        theme.themePadding[2] = Integer.parseInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_PADDING_BOTTOM)) {
                        theme.themePadding[3] = Integer.parseInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_TITLECOLOR)) {
                        theme.themeTitleColor = colorStringToInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_TEXTCOLOR)) {
                        theme.themeTextColor = colorStringToInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_TIMECOLOR)) {
                        theme.themeTimeColor = colorStringToInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_TIMESUFFIXCOLOR)) {
                        theme.themeTimeSuffixColor = colorStringToInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_SUNRISECOLOR)) {
                        theme.themeSunriseTextColor = colorStringToInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_SUNSETCOLOR)) {
                        theme.themeSunsetTextColor = colorStringToInt(value);
                    } else if (value.equalsIgnoreCase(SuntimesTheme.THEME_TITLESIZE)) {
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
    private String backgroundIdToString(int backgroundID)
    {
        int i = ThemeBackground.ordinal(backgroundID);
        if (i >= 0)
        {
            return ThemeBackground.values()[i].name();
        }
        return ThemeBackground.DARK.name();
    }
}
