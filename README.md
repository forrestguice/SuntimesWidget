# SuntimesWidget
An Android app (and widget collection) that displays sunlight and moonlight times for a given location. 
[![Build Status](https://travis-ci.org/forrestguice/SuntimesWidget.svg?branch=master)](https://travis-ci.org/forrestguice/SuntimesWidget)

Displays sunrise and sunset, civil/nautical/astronomical twilight, blue/gold hour, solstices/equinoxes, moonrise and moonset, moon phases and illumination. 

<a href="https://f-droid.org/repository/browse/?fdid=com.forrestguice.suntimeswidget" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/></a>

**App and Widget Features**

The app:
* displays the current time, and notes the time until next sunrise, sunset, blue/gold hour, or civil/nautical/astronomical twilight.
* displays the next solstice or equinox, and notes the time until that event.
* can set an alarm for next sunrise, sunset, blue/gold hour, or civil/nautical/astronomical twilight.
* can display the configured location on a map (requires a map application).
* *does not* require GPS. The location is manually specified by default (and optionally obtained from GPS).
* *does not* require network connectivity. Calculations are performed locally on the device using.

The widgets are themeable and provide:
* a theme editor and support for basic custom themes.
* a (default) dark theme (and dark w/ transparent background).
* a (default) light theme (and light w/ transparent background).

The widgets are (re)configurable:
* configure location (latitude / longitude)
* configure timezone
* configure widget theme
* configure user-defined title (supports limited substitutions)
* configure "show noon" and "show comparison"
* configure "comparison mode" (against tomorrow or yesterday)

The widgets are resizable and include...

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

<img alt="screenshot1" src='https://github.com/forrestguice/SuntimesWidget/blob/docs/doc/screenshots/v0.7.0/en/activity-main0-dark.png' width="280px" />&nbsp;<img alt="screenshot1" src='https://github.com/forrestguice/SuntimesWidget/blob/docs/doc/screenshots/v0.7.0/en/activity-main0-light.png' width="280px" />
Additional screenshots can be found on the <a href="https://github.com/forrestguice/SuntimesWidget/wiki/Screenshots">project wiki</a> and in the <a href="https://github.com/forrestguice/SuntimesWidget/tree/docs/doc/screenshots">docs branch</a>.

**Bug Reports**

You can report bugs using the issue tracker. Be aware that times are approximate and may differ from official sources (up to 10 minutes).

When submitting a bug please be detailed and specific. What did you expect the app to do, what did you actually observe? Bugs that can't be reproduced won't get fixed. If reporting inaccurate times include the lat/lon, timezone, and the date. Other useful information includes Android OS version and your specific device model.

**Donations**

Pay as you like. [![paypal](https://www.paypalobjects.com/webstatic/en_US/i/btn/png/silver-rect-paypal-26px.png)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=NZJ5FJBCKY6K2)
<br />I want to express my thanks to those who have sent me something. Its a very meaningful gesture.

## Legal Stuff

The goal of this project is an app that is free and open-source (FOSS). The source code is available under *GPLv3* (https://github.com/forrestguice/SuntimesWidget).

Icons borrowed from:
* "Google Android Design Icons 20131120" [Apache License 2.0]
* "Google Material Icons" [Apache License 2.0] (https://material.io/icons/)
* "Material Design Icons" [SIL Open Font License 1.1] (https://materialdesignicons.com)

Libraries used:
* Time4A [LGPL-2.1] (http://github.com/MenoData/Time4A) 
* ca.rmen.sunrisesunset [LGPL-2.1] (http://github.com/caarmen/SunriseSunset)
* sunrisesunsetlib-java [Apache License 2.0] (http://mikereedell.github.io/sunrisesunsetlib-java/) 
* QuadFlask/colorpicker [Apache License 2.0] (https://github.com/QuadFlask/colorpicker) 

## Project Contributions

German translation by <u>Henrik "HerHde" Hüttemann</u>.<br/>
Polish and Esperanto translations by <u>Verdulo</u>.<br/>
French translation by <u>Jej</u>.<br/>
Hungarian translation by <u>Erci</u>.<br/>
Catalan and Spanish translations by <u><a href="https://github.com/Raulvo">Raulvo</a></u>.

[Contributions to the project](CONTRIBUTING.md) are welcome.






