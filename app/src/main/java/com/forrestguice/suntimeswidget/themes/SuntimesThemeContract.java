/**
    Copyright (C) 2018-2020 Forrest Guice
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

public interface SuntimesThemeContract
{
    String THEME_PROVIDER_VERSION = "theme_provider_version";                                       // String (provider version string)
    String THEME_PROVIDER_VERSION_CODE = "theme_provider_version_code";                             // int (provider version code)

    String THEME_NAME = "name";                                                                     // String
    String THEME_VERSION = "version";                                                               // int
    String THEME_ISDEFAULT = "isDefault";                                                           // int (boolean)
    String THEME_DISPLAYSTRING = "display";                                                         // String

    String THEME_BACKGROUND = "backgroundID";
    String THEME_BACKGROUND_COLOR = "backgroundColor";                                              // int (color)

    String THEME_PADDING = "padding";
    String THEME_PADDING_LEFT = "padding_left";                                                     // int (pixels)
    String THEME_PADDING_TOP = "padding_top";                                                       // int (pixels)
    String THEME_PADDING_RIGHT = "padding_right";                                                   // int (pixels)
    String THEME_PADDING_BOTTOM = "padding_bottom";                                                 // int (pixels)

    String THEME_TEXTCOLOR = "textcolor";                                                           // int (color)
    String THEME_TITLECOLOR = "titlecolor";                                                         // int (color)
    String THEME_TIMECOLOR = "timecolor";                                                           // int (color)
    String THEME_TIMESUFFIXCOLOR = "timesuffixcolor";                                               // int (color)
    String THEME_ACTIONCOLOR = "actioncolor";                                                       // int (color)
    String THEME_ACCENTCOLOR = "accentcolor";                                                       // int (color)

    String THEME_SUNRISECOLOR = "sunrisecolor";                                                     // int (color)
    String THEME_NOONCOLOR = "nooncolor";                                                           // int (color)
    String THEME_SUNSETCOLOR = "sunsetcolor";                                                       // int (color)

    String THEME_MOONRISECOLOR = "moonrisecolor";                                                   // int (color)
    String THEME_MOONSETCOLOR = "moonsetcolor";                                                     // int (color)
    String THEME_MOONWANINGCOLOR = "moonwaningcolor";                                               // int (color)
    String THEME_MOONWAXINGCOLOR = "moonwaxingcolor";                                               // int (color)
    String THEME_MOONNEWCOLOR = "moonnewcolor";                                                     // int (color)
    String THEME_MOONFULLCOLOR = "moonfullcolor";                                                   // int (color)

    String THEME_MOONFULL_STROKE_WIDTH = "moonfull_strokewidth";
    String THEME_MOONNEW_STROKE_WIDTH = "moonnew_strokewidth";
    float THEME_MOON_STROKE_MIN = 0.0f;
    float THEME_MOON_STROKE_DEF = 3.0f;
    float THEME_MOON_STROKE_MAX = 7.0f;

    String THEME_NOONICON_FILL_COLOR = "noonicon_fillcolor";                                        // int (color)
    String THEME_NOONICON_STROKE_COLOR = "noonicon_strokecolor";                                    // int (color)
    String THEME_NOONICON_STROKE_WIDTH = "noonicon_strokewidth";
    float THEME_NOONICON_STROKE_WIDTH_MIN = 0.0f;
    float THEME_NOONICON_STROKE_WIDTH_DEF = 3.0f;
    float THEME_NOONICON_STROKE_WIDTH_MAX = 7.0f;

    String THEME_RISEICON_FILL_COLOR = "riseicon_fillcolor";                                        // int (color)
    String THEME_RISEICON_STROKE_COLOR = "riseicon_strokecolor";                                    // int (color)
    String THEME_RISEICON_STROKE_WIDTH = "riseicon_strokewidth";
    float THEME_RISEICON_STROKE_WIDTH_MIN = 0.0f;
    float THEME_RISEICON_STROKE_WIDTH_DEF = 0.0f;
    float THEME_RISEICON_STROKE_WIDTH_MAX = 7.0f;

    String THEME_SETICON_FILL_COLOR = "seticon_fillcolor";                                          // int (color)
    String THEME_SETICON_STROKE_COLOR = "seticon_strokecolor";                                      // int (color)
    String THEME_SETICON_STROKE_WIDTH = "seticon_strokewidth";
    float THEME_SETICON_STROKE_WIDTH_MIN = 0.0f;
    float THEME_SETICON_STROKE_WIDTH_DEF = 0.0f;
    float THEME_SETICON_STROKE_WIDTH_MAX = 7.0f;

    String THEME_DAYCOLOR = "daycolor";                                                             // int (color)
    String THEME_CIVILCOLOR = "civilcolor";                                                         // int (color)
    String THEME_NAUTICALCOLOR = "nauticalcolor";                                                   // int (color)
    String THEME_ASTROCOLOR = "astrocolor";                                                         // int (color)
    String THEME_NIGHTCOLOR = "nightcolor";                                                         // int (color)

    String THEME_SPRINGCOLOR = "springcolor";                                                       // int (color)
    String THEME_SUMMERCOLOR = "summercolor";                                                       // int (color)
    String THEME_FALLCOLOR = "fallcolor";                                                           // int (color)
    String THEME_WINTERCOLOR = "wintercolor";                                                       // int (color)

    String THEME_MAP_BACKGROUNDCOLOR = "mapbackgroundcolor";                                        // int (color)
    String THEME_MAP_FOREGROUNDCOLOR = "mapforegroundcolor";                                        // int (color)
    String THEME_MAP_SHADOWCOLOR = "mapshadowcolor";                                                // int (color)
    String THEME_MAP_HIGHLIGHTCOLOR = "maphighlightcolor";                                          // int (color)

    String THEME_TITLESIZE = "titlesize";                                                           // float (sp)
    float THEME_TITLESIZE_MIN = 6.0f;
    float THEME_TITLESIZE_DEF = 10.0f;
    float THEME_TITLESIZE_MAX = 32.0f;
    String THEME_TITLEBOLD = "titlebold";                                                           // int (boolean)

    String THEME_TEXTSIZE = "textsize";                                                             // float (sp)
    float THEME_TEXTSIZE_MIN = 6.0f;
    float THEME_TEXTSIZE_DEF = 10.0f;
    float THEME_TEXTSIZE_MAX = 32.0f;

    String THEME_TIMESIZE = "timesize";                                                             // float (sp)
    float THEME_TIMESIZE_MIN = 6.0f;
    float THEME_TIMESIZE_DEF = 12.0f;
    float THEME_TIMESIZE_MAX = 32.0f;
    String THEME_TIMEBOLD = "timebold";                                                             // int (boolean)

    String THEME_TIMESUFFIXSIZE = "timesuffixsize";                                                 // float (sp)
    float THEME_TIMESUFFIXSIZE_MIN = 4.0f;
    float THEME_TIMESUFFIXSIZE_DEF = 6.0f;
    float THEME_TIMESUFFIXSIZE_MAX = 32.0f;

}
