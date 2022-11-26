# Suntimes
Android app (and widget collection) that displays sunlight and moonlight times for a given location. 

<a href="https://f-droid.org/repository/browse/?fdid=com.forrestguice.suntimeswidget" target="_blank">
<img src="https://forrestguice.github.io/SuntimesWidget/assets/images/get-it-on-fdroid.png" alt="Get it on F-Droid" height="68" align="left" /></a><br /><br />

[![F-Droid](https://img.shields.io/f-droid/v/com.forrestguice.suntimeswidget.svg)](https://f-droid.org/en/packages/com.forrestguice.suntimeswidget/)
[![GitHub release](https://img.shields.io/github/release/forrestguice/SuntimesWidget.svg)](https://github.com/forrestguice/SuntimesWidget/releases)
[![Android CI](https://github.com/forrestguice/SuntimesWidget/workflows/Android%20CI/badge.svg?branch=master)](https://github.com/forrestguice/SuntimesWidget/actions/workflows/android.yml)

* [Privacy and Permissions](#privacy-and-permissions)
* [Donations](#donations)
* [Bug Reports](#bug-reports)
* [Legal Stuff](#legal-stuff)
* [Contributions](#project-contributions)

Displays sunrise and sunset, twilights (civil / nautical / astronomical), blue and golden hour, solstices and equinoxes, moonrise and moonset, moon phases and illumination. 
    
<a href="https://forrestguice.github.io/SuntimesWidget/" target="_blank"><img align="left" src="https://forrestguice.github.io/SuntimesWidget/assets/images/ic_launcher_alarms.png" height="64" /></a> Suntimes Alarms<br />
An alarm clock for Suntimes.<br/><br />

<a href="https://github.com/forrestguice/SuntimesCalendars/"><img align="left" src="https://forrestguice.github.io/SuntimesWidget/assets/images/ic_launcher.png" height="64" /></a> <a href="https://github.com/forrestguice/SuntimesCalendars/">Suntimes Calendars</a> <br />
A calendar provider add-on for Suntimes.<br/><br />        

<a href="https://github.com/forrestguice/SolunarPeriods/"><img align="left" src="https://forrestguice.github.io/SuntimesWidget/assets/images/ic_solunar.png" height="64" /></a> <a href="https://github.com/forrestguice/SolunarPeriods/">Solunar Periods</a> <br />
A hunting and fishing add-on for Suntimes.<br/><br />

<a href="https://github.com/forrestguice/NaturalHour/"><img align="left" src="https://forrestguice.github.io/SuntimesWidget/assets/images/ic_naturalhour.png" height="64" /></a> <a href="https://github.com/forrestguice/NaturalHour/">Natural Hour</a> <br />
A 24-hour clock & roman timekeeping add-on for Suntimes.<br/><br />
    
The app:
* displays the current time (system time zone, custom time zone, or solar time) 
* displays the time until the next rising/setting event.
* displays the sunrise and sunset, blue and golden hour, and twilight times (civil / nautical / astronomical).
* displays the current moon phase, illumination, moonrise and moonset times, and major phase dates.
* displays the current sunlight and moonlight projected over a world map (basic equirectangular, blue marble).
* displays the solstices and equinoxes, and notes the time until the next event.
* displays the sun's current position, and its position at sunrise, sunset, and noon.
* displays the moon's current position, and its position at moonrise and moonset.
* includes an Alarm Clock that can set a repeating alarm or notification for the next sunrise or sunset (or other rising/setting event).
* can display the configured location on a map (requires a map application) and configure the location from a map (using geo intent).

The app:
* *does not* require GPS. The location is manually specified by default (and optionally obtained from GPS).
* *does not* require network connectivity (or other unnecessary permissions). All calculations are performed locally on the device. 

Widgets are (re)configurable:
* location (latitude, longitude, elevation).
* time zone (system / custom), sidereal time (lmst, gmst), or solar time (local mean time, apparent solar time).
* user-defined titles (supporting limited substitutions).
* misc. options: "use elevation", "show seconds", "show noon", and "show comparison", etc.

Widgets are themeable and provide:
* a theme editor and support for basic custom themes.
* a (default) dark theme (and dark w/ semi-transparent background).
* a (default) light theme (and light w/ transparent background).

Widgets are resizable and include...

**Clock widgets:**
* 1x1 digital clock widget that displays solar time (or a given timezone).

**Sun widgets:**
* 1x1 sun widget that displays the sunrise or sunset time.
* 1x1 sun widget that displays both sunrise and sunset times.
* 1x1 sun widget that flips between sunrise and sunset times.
* 2x1 sun widget that displays the sunrise and sunset times, and the difference in daylight between today and tomorrow (or yesterday).

**Moon widgets:**
* 1x1 moon widget that displays moonrise and moonset.
* 1x1 moon widget that displays next major phase.
* 1x1 moon widget that displays phase and illumination (or phase only, or illumination only).
* 2x1 moon widget that displays moonrise and moonset, phase, and illumination.
* 3x1 moon widget that displays upcoming major moon phases.

**Solstice widgets:**
* 1x1 solstice widget that tracks the upcoming solstice or equinox.

**Position widgets:**
* 1x1 sun position widget that tracks the sun's altitude and azimuth.
* 1x1 sun position widget that tracks the sun's declination and right ascension.
* 3x1 sun position widget that displays the lightmap graph, and tracks the sun's altitude and azimuth (current, sunrise/sunset, and at noon).
* 3x2 sun position widget that displays current sunlight and moonlight projected over a world map.

<img width="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/clockwidget_1x1_preview.png" align="center"></img>
<img width="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/widget1_preview.png" align="center"></img>
<img width="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/widget0_1x1_preview.png" align="center"></img>
<img width="288px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/widget0_2x1_preview.png" align="center"></img>
<img width="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/widget2_preview.png" align="center"></img>
<img width="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/moonwidget4_1x1_preview.png" align="center"></img>
<img width="288px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/moonwidget0_2x1_preview.png" align="center"></img>
<img height="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/moonwidget0_3x1_preview.png" align="center"></img>
<img height="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/sunposwidget0_1x1_preview.png" align="center"></img>
<img height="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/sunposwidget1_1x1_preview.png" align="center"></img>
<img height="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/sunposwidget0_3x1_preview.png" align="center"></img>
<img height="256px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/sunposwidget0_3x2_preview.png" align="center"></img>
<br />Additional screenshots can be found on the <a href="https://github.com/forrestguice/SuntimesWidget/wiki/Screenshots">project wiki</a> and in the <a href="https://github.com/forrestguice/SuntimesWidget/tree/gh-pages/doc/screenshots">gh-pages branch</a>.

<img alt="screenshot1" src='https://github.com/forrestguice/SuntimesWidget/blob/gh-pages/doc/screenshots/v0.13.0/en/activity-main0-dark.png' width="280px" />&nbsp;&nbsp;<img alt="screenshot2" src='https://github.com/forrestguice/SuntimesWidget/blob/gh-pages/doc/screenshots/v0.13.0/en/activity-alarms0-dark.png' width="280px" />&nbsp;&nbsp;<img alt="screenshot3" src='https://github.com/forrestguice/SuntimesWidget/blob/gh-pages/doc/screenshots/v0.13.0/en/activity-main0-light.png' width="280px" />

## Privacy and Permissions ##

Suntimes does not collect, store, or transmit personal user data. It contains <b>no advertising</b>, <b>no analytics</b>, <b>no trackers</b>, and <b>no unnecessary permissions</b>. 
[https://github.com/forrestguice/SuntimesWidget/wiki/Privacy](https://github.com/forrestguice/SuntimesWidget/wiki/Privacy)
    
The app benefits from the following permissions...

|Permission||Since Version|
|---|---|---|
|ACCESS_COARSE_LOCATION|To get current location.|v0.1.0|
|ACCESS_FINE_LOCATION|To get current location (GPS).|v0.1.0|
|BOOT_COMPLETED|To restore active alarms when the device boots.|v0.11.0|
|POWER_OFF_ALARM|To wake the device from the power off state.|v0.14.0|
|READ_EXTERNAL_STORAGE|To play alarm sounds located on the SD card.|v0.11.5, v0.13.8 (api&le;18)|
|SET_ALARM|To interact with the system AlarmClock app.|v0.1.0|
|REQUEST_IGNORE_BATTERY_OPTIMIZATIONS|To help ensure reliable delivery of alarms.|v0.14.11|
|WRITE_EXTERNAL_STORAGE|To export data (places, themes, etc.) to file.|v0.2.2 (api&le;18)|

Version `v0.13.8` removed READ_EXTERNAL_STORAGE for api&ge;19 (replaced with persistent URI permissions). 

Version `0.9.*` contained the following additional permissions (removed in v0.10.0)...

|Permission||Version|
|---|---|---|
|READ_CALENDAR|To interact with the Calendar app (access events).|v0.9.0|
|WRITE_CALENDAR|To interact with the Calendar app (add/remove events).|v0.9.0|
|READ_SYNC_STATS|To interact with the Calendar app (access custom calendars).|v0.9.0|
|WRITE_SYNC_SETTINGS|To interact with the Calendar app (add/remove custom calendars).|v0.9.0|

## Donations ##

Do you find value in this software? Pay as you feel. 

[![paypal](https://www.paypalobjects.com/webstatic/en_US/i/btn/png/silver-rect-paypal-26px.png)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=NZJ5FJBCKY6K2) 

I want to express my thanks to those who have sent me something. This is greatly appreciated.

## Bug Reports ##

Use the issue tracker to submit a bug report or a feature request.

When reporting a bug **please be detailed as possible**. What did you expect the app to do, what did you actually observe? Include the app version number in your report. Other useful information includes the Android OS version (and sometimes your specific device model).


## Legal Stuff

Copyright &#169; 2014-2022 Forrest Guice<br/>

The source code is available under [GPLv3](LICENSE) (https://github.com/forrestguice/SuntimesWidget).
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the [GNU General Public License](LICENSE) for more details.



Icons and images from:
* "Google Android Design Icons 20131120" [Apache License 2.0]
* "Google Material Icons" [Apache License 2.0] (https://material.io/icons/)
* "Material Design Icons" [SIL Open Font License 1.1] (https://materialdesignicons.com)
* NASA Earth Observatory. "Blue Marble: Next Generation" [Public Domain] (https://visibleearth.nasa.gov/view_cat.php?categoryID=1484)

Libraries used:
* Time4A [Apache License 2.0] (http://github.com/MenoData/Time4A) 
* ca.rmen.sunrisesunset [LGPL-2.1] (http://github.com/caarmen/SunriseSunset)
* sunrisesunsetlib-java [Apache License 2.0] (http://mikereedell.github.io/sunrisesunsetlib-java/) 
* QuadFlask/colorpicker [Apache License 2.0] (https://github.com/QuadFlask/colorpicker)
* GravitySnapHelper [Apache License 2.0] (https://github.com/rubensousa/GravitySnapHelper) 

## Project Contributions

German translation by <u>Henrik "HerHde" Hüttemann</u>, <u>Wolkenschieber</u>, <u>xnumad</u>, and <u>CSTRSK</u>.<br/>
Polish and Esperanto translations by <u>Verdulo</u>.<br/>
French translation by <u>Jej</u>, <u>Aloha</u> and <u>ldmpub</u>.<br/>
Hungarian translation by <u>Erci</u>.<br/>
Catalan and Spanish translations by <u><a href="https://github.com/Raulvo">Raulvo</a></u> and <u><a href=https://github.com/gnusuari0>gnusuari0</a></u>.<br/>
Basque translation by <u>beriain</u>.<br/>
Norwegian translation by <u>FTno</u>.<br/>
Italian translation by <u>Matteo Caoduro</u> and <u>GiovaGa</u>.<br/>
Traditional Chinese translation by <u><a href=https://github.com/pggdt>ft42</a></u>, and <u><a href=https://github.com/jamesliu96>James Liu</a></u>.<br />
Brazilian Portuguese translation by <u><a href=https://github.com/netosilva15>NetoSilva</a></u>, <u>Nelson A. de Oliveira</u>, and <u>Enrico S. B. Fraletti</u>.<br />
Russian translation by <u><a href=https://github.com/rchintsov>Ruslan Chintsov</a></u>.<br />
Dutch translation by <u><a href=https://github.com/joppla>Joppla</a></u>.<br />
Czech translation by <u><a href=https://github.com/utaxiu>utaxiu</a></u>.<br />
Simplified Chinese translation by <u><a href=https://github.com/jamesliu96>James Liu</a></u>, and <u><a href=https://github.com/sr093906>sr093906</a></u>.<br />

[Contributions to the project](CONTRIBUTING.md) are welcome.






