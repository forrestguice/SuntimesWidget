# SuntimesWidget
An Android app (and home screen widget) that displays sunrise, sunset, civil/nautical/astronomical twilight times for a given location.

The app:
* displays the current time, and notes the time until next sunrise, sunset, or civil/nautical/astronomical twilight.
* can set an alarm for next sunrise, sunset, or civil/nautical/astronomical twilight.
* can display the configured location on a map (requires a map application).
* *does not* require GPS. The location is manually specified by default (and optionally obtained from GPS).
* *does not* require network connectivity. Calculations are performed locally on the device (does not rely on third-party weather services). The app currently uses [sunrisesunsetlib-java](http://mikereedell.github.io/sunrisesunsetlib-java/) to calculate times.

**Widget Features**

The widget is resizable and has layouts for:
* a 1x1 widget that displays EITHER the sunrise OR sunset time.
* a 1x1 widget that displays BOTH the sunrise AND sunset times.
* a 1x2 widget that displays the sunrise and sunset times, and displays the difference in rise and set times between today and tomorrow (or today and yesterday).
* a 1x3 widget that displays the sunrise and sunset times, and displays the difference in the amount of daylight (in minutes) between today and tomorrow (or today and yesterday).

The widget is themeable and provides:
* a Dark theme (and dark w/ transparent background)
* a Light theme (and light w/ transparent background)

The widget is (re)configurable:
* configure location (latitude / longitude)
* configure timezone
* configure widget theme (dark theme, light theme, transparent themes)
* configure user-defined title (supports limited substitutions)
* configure comparison mode (against tomorrow or yesterday)


**Legal Stuff**

The goal of this project is an app that is free and open-source (FOSS). Get the source code (under GPLv3) at: https://github.com/forrestguice/SuntimesWidget
