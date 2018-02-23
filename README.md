# Suntimes
Android app (and widget collection) that displays sunlight and moonlight times for a given location. 

[![GitHub release](https://img.shields.io/github/release/forrestguice/SuntimesWidget.svg)](https://github.com/forrestguice/SuntimesWidget/releases)
[![Build Status](https://travis-ci.org/forrestguice/SuntimesWidget.svg?branch=master)](https://travis-ci.org/forrestguice/SuntimesWidget)

Displays sunrise and sunset, twilights (civil / nautical / astronomical), blue and golden hour, solstices and equinoxes, moonrise and moonset, moon phases and illumination. 

<a href="https://f-droid.org/repository/browse/?fdid=com.forrestguice.suntimeswidget" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/></a>

The app:
* displays the current time (system timezone, custom timezone, or solar time), and notes the time until next sunrise, sunset, or twilight time.
* displays the solstices and equinoxes, and notes the time until the next event.
* can set an alarm for the next sunrise, sunset, or twilight time.
* can display the configured location on a map (requires a map application), and set the location from a map (using geo intent).

The app:
* *does not* require GPS. The location is manually specified by default (and optionally obtained from GPS).
* *does not* require network connectivity (or other unnecessary permissions). All calculations are performed locally on the device. 

Widgets are (re)configurable:
* location (latitude / longitude).
* timezone (system / custom), or <em>solar time</em> (local mean time, apparent solar time).
* user-defined titles (supporting limited substitutions).
* misc options: "show seconds", "show noon", and "show comparison", etc.

Widgets are themeable and provide:
* a theme editor and support for basic custom themes.
* a (default) dark theme (and dark w/ transparent background).
* a (default) light theme (and light w/ transparent background).

Widgets are resizable and include...

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

<img width="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/widget0_1x1_preview.png" align="center"></img>
<img width="288px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/widget0_2x1_preview.png" align="center"></img>
<img width="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/moonwidget4_1x1_preview.png" align="center"></img>
<img width="288px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/moonwidget0_2x1_preview.png" align="center"></img>
<img height="128px" src="https://github.com/forrestguice/SuntimesWidget/blob/master/app/src/main/res/drawable-nodpi/moonwidget0_3x1_preview.png" align="center"></img>
<br />Additional screenshots can be found on the <a href="https://github.com/forrestguice/SuntimesWidget/wiki/Screenshots">project wiki</a> and in the <a href="https://github.com/forrestguice/SuntimesWidget/tree/docs/doc/screenshots">docs branch</a>.

<img alt="screenshot1" src='https://github.com/forrestguice/SuntimesWidget/blob/docs/doc/screenshots/v0.7.0/en/activity-main0-dark.png' width="280px" />&nbsp;&nbsp;<img alt="screenshot1" src='https://github.com/forrestguice/SuntimesWidget/blob/docs/doc/screenshots/v0.7.0/en/activity-main0-light.png' width="280px" />

## Donations ##

Do you find value in this software? Pay as you like. 

[![paypal](https://www.paypalobjects.com/webstatic/en_US/i/btn/png/silver-rect-paypal-26px.png)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=NZJ5FJBCKY6K2) [![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=forrestguice&url=https://github.com/forrestguice/SuntimesWidget&title=Suntimes&tags=github&category=software)

I want to express my thanks to those who have sent me something. This is a very meaningful gesture.

## Bug Reports ##

You can report bugs using the issue tracker. Be aware that times are approximate and may differ from official sources (up to 10 minutes).

When submitting a bug please be detailed and specific. What did you expect the app to do, what did you actually observe? Bugs that can't be reproduced won't get fixed. If reporting inaccurate times include the lat/lon, timezone, and the date. Other useful information includes Android OS version and your specific device model.


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






