# SuntimesWidget
An Android app (and home screen widget) that displays sunrise, sunset, civil/nautical/astronomical twilight times for a given location.

<a href="https://f-droid.org/repository/browse/?fdid=com.forrestguice.suntimeswidget" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/></a>

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

**Bug Reports**

You can report bugs using the issue tracker. Be aware that times are approximate and may differ from official sources (up to 10 minutes).

When submitting a bug please be detailed and specific. What did you expect the app to do, what did you actually observe? Bugs that can't be reproduced won't get fixed. If reporting inaccurate times include the lat/lon, timezone, and the date. Other useful information includes Android OS version and your specific device model.

**Donations**

Feel free to pay whatever you think is fair using [![paypal](https://www.paypalobjects.com/webstatic/en_US/i/btn/png/silver-rect-paypal-26px.png)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=NZJ5FJBCKY6K2)

Any money given will indirectly go towards software development. It is not technically a donation since I am obligated to report anything given to me as earned income (and I will probably apply it toward personal expenses). I want to express my thanks to those who have sent me something. In total it has amounted to lunch and a coffee but its a very meaningful gesture.

**Legal Stuff**

The goal of this project is an app that is free and open-source (FOSS). The source code is available under *GPLv3* (https://github.com/forrestguice/SuntimesWidget).

Icons borrowed from:
* "Google Android Design Icons 20131120" [Apache License 2.0]
* "Google Material Icons" [Apache License 2.0] (https://material.io/icons/)
* "Material Design Icons" [SIL Open Font License 1.1] (https://materialdesignicons.com)

**Project Contributions**

[Contributions to the project](CONTRIBUTING.md) are welcome.

German translation by <u>Henrik "HerHde" Hüttemann</u>.<br/>
Polish and Esperanto translations by <u>verdulo</u>.<br/>
French translation by <u>Jej</u>.<br/>








