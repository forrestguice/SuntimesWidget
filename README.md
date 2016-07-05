# SuntimesWidget
An Android app (and home screen widget) that displays sunrise, sunset, civil/nautical/astronomical twilight times for a given location.

The app:
* displays the current time, and notes the time until next sunrise, sunset, or civil/nautical/astronomical twilight.
* can set an alarm for next sunrise, sunset, or civil/nautical/astronomical twilight.
* can display the configured location on a map (requires a map application).
* *does not* require GPS. The location is manually specified by default (and optionally obtained from GPS).
* *does not* require network connectivity. Calculations are performed locally on the device (does not rely on third-party weather services). The app currently uses [sunrisesunsetlib-java](http://mikereedell.github.io/sunrisesunsetlib-java/) to calculate times.

**Widget Features**

The widget is themeable and provides:
* a dark theme (and dark w/ transparent background)
* a light theme (and light w/ transparent background)

The widget is (re)configurable:
* configure location (latitude / longitude)
* configure timezone
* configure widget theme (dark theme, light theme, transparent themes)
* configure user-defined title (supports limited substitutions)
* configure comparison mode (against tomorrow or yesterday)

The home screen widget is resizable and has layouts for:
* a 1x1 widget that displays either the sunrise OR sunset time.
* a 1x1 widget that displays both the sunrise AND sunset times.
* a 1x2 widget that displays the sunrise and sunset times, and displays the difference in rise and set times between today and tomorrow (or today and yesterday).
* a 1x3 widget that displays the sunrise and sunset times, and displays the difference in the amount of daylight (in minutes) between today and tomorrow (or today and yesterday).


<img alt="screenshot1" src='https://cloud.githubusercontent.com/assets/10246147/14938297/ab3697ee-0ed3-11e6-80c2-a9611c1f20cc.png' width="280px" />

<img alt="screenshot2" src='https://cloud.githubusercontent.com/assets/10246147/14938299/ad52bc2e-0ed3-11e6-8916-9b7e75057a62.png' width="280px" />


**Donations**

If you think there is some value in this software then <i>please feel free to pay</i> whatever you think is fair through <a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=NZJ5FJBCKY6K2">https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=NZJ5FJBCKY6K2</a>.

If you are in the giving mood, I also encourage you to give to <a href="http://mikereedell.github.io/sunrisesunsetlib-java/">http://mikereedell.github.io/sunrisesunsetlib-java/</a> as this app would not be possible without this library.

Any money given will indirectly go towards future software development; I will spend it on personal expenses, feel good knowing my code is worth something to someone, and (theoretically) feel a sense of obligation to continue supporting the app into the future. It is not technically a donation (as I am obligated to report anything given to me as earned income on my taxes).

**Legal Stuff**

The goal of this project is an app that is free and open-source (FOSS). Get the source code (under GPLv3) at: https://github.com/forrestguice/SuntimesWidget
