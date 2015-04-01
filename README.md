# SuntimesWidget
An Android widget that displays sunrise and sunset times.
This project uses [sunrisesunsetlib-java](http://mikereedell.github.io/sunrisesunsetlib-java/).

**Widget Features:**
* displays sunrise and sunset times (and related information) for a given location.
* *does not* require GPS (the location is manually specified). 
* *does not* require network connectivity; calculations are performed using sunrisesunsetlib-java.

The widget is resizable and has the following layouts:
* a 1x1 widget that displays EITHER the sunrise OR sunset time.
* a 1x1 widget that displays BOTH the sunrise AND sunset times.
* a 1x2 widget that displays the sunrise and sunset times, and displays the difference in rise and set times between today and tomorrow (or today and yesterday).
* a 1x3 widget that displays the sunrise and sunset times, and displays the difference in the amount of daylight (in minutes) between today and tomorrow (or today and yesterday).

The widget is themeable and has the following appearances:
* Dark theme (and dark w/ transparent background)
* Light theme (and light w/ transparent background)

The widget is configurable:
* configure location (latitude / longitude)
* configure timezone
* configure widget theme (dark theme, light theme, transparent themes)
* configure user-defined title (that supports limited substitutions)
* configure comparison mode (against tomorrow or yesterday)

**Legal Stuff**

This widget is open source. The source code is available (under GPLv3) at: https://github.com/forrestguice/SuntimesWidget
