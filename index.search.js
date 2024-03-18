var relearn_search_index = [
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Worldmap",
    "content": "To use backgrounds… Save full-sized images somewhere on your device, then open with Set background. Suntimes uses URI permissions to access images stored on the sdcard.\nProjection Map Backgrounds (click for full-size image) Equidistant Rectangular Equidistant Azimuthal Made with Natural Earth. https://www.naturalearthdata.com/about/terms-of-use/\nNASA Earth Observatory. Blue Marble: Next Generation. https://visibleearth.nasa.gov/view_cat.php?categoryID=1484\n",
    "description": "",
    "tags": null,
    "title": "Download Backgrounds",
    "uri": "/Suntimes/help/worldmap/downloadbackgrounds/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Data Substitutions",
    "content": "Date/Time:\n%t for time zone (id) (e.g. US/Arizona) %d for formatted date (e.g. February 12) %dd for day (short) (e.g. Mon) %dD for day (long) (e.g. Monday) %dY for year (e.g. 2018) %dt for formatted time (of last update) %dT for formatted time with seconds (of last update) %dm for time in milliseconds (of last update) %eot for formatted ’equation of time’ (of last update) %eot_m for ’equation of time’ milliseconds (of last update) Location:\n%loc for label (e.g. Phoenix) %lat for latitude %lon for longitude %lel for elevation (e.g. 385 meters) Misc:\n%s for data source (e.g. sunrisesunsetlib) %id for appWidgetID Sun Widgets:\n%m for mode (short) (e.g. Civil) %M for mode (long) (e.g. Civil Twilight) %o for order (e.g. Last/Next, Today) %em@\u003cevent\u003e event milliseconds %et@\u003cevent\u003e event formatted time %eT@\u003cevent\u003e event formatted time (with seconds) %eA@\u003cevent\u003e event altitude %eZ@\u003cevent\u003e event azimuth %eD@\u003cevent\u003e event declination %eR@\u003cevent\u003e event right ascension where \u003cevent\u003e is:\nSun WidgetsSun Position Widgets \u003cevent\u003e rising sr setting ss noon sn rising setting civil twilight cr cs nautical twilight nr ns astronomical twilight ar as blue hour 4deg b4r b4s blue hour 8deg b8r b8s golden hour gr gs Moon Widgets:\n%i for moon illumination (e.g. 25%) %M for moon phase (e.g. Waxing Crescent) %o for order (e.g. Last/Next, Today) Solstice Widgets:\n%m for mode (short) (e.g. Solstice) %M for mode (long) (e.g. Winter Solstice) %o for order (e.g. Closest Event, Upcoming Event) ",
    "description": "",
    "tags": null,
    "title": "Available Substitions",
    "uri": "/Suntimes/help/datasubstitutions/allsubstitions/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Data Sources",
    "content": " Library Description sunrisesunsetlib Not Recommended Somewhat inaccurate and sometimes buggy. Does not support altitude, seconds-based calculation, solstice, equinox, or sun position. Based on “Almanac for Computers” by the USNO. github.com/mikereedell/sunrisesunsetlib-java ca.rmen.sunrisesunset Partially Recommended Similar to sunrisesunsetlib but with reasonable precision. Does not support altitude, solstice, equinox, or sun position. Based on the algorithms published by NOAA. github.com/caarmen/SunriseSunset time4a-simple Not Recommended Somewhat inaccurate. Does not support altitude. Based on “Almanac for Computers” by the USNO. time4a-noaa Partially Recommended Same algorithm used by ca.rmen.sunrisesunset with reasonable precision. Does not support altitude. Based on the algorithms published by NOAA. time4a-cc Recommended Good precision taking the altitude of locations into account. Based on “Calendrical Calculations” by Dershowitz/Reingold. Supports all features. time4a-time4j Recommended Default Best precision taking the altitude of locations, the elliptic shape of the earth and typical weather conditions into account. Based on “Astronomical Algorithm” by Jean Meeus. Supports all features. github.com/MenoData/Time4A A few important details:\nDo not expect precision better than minutes. The app hides seconds by default (but this can be enabled). The precision of the USNO and NOAA algorithms tends to be very inaccurate in polar regions. The time4j and cc algorithms may differ substantially (up to 10 minutes) from algorithms that do not account for altitude. One difference between the time4j and cc algorithms is that cc only assumes the altitude of the observer by an approximated geodetic model, while time4j does it using a spheroid (WGS84) and the assumption of a standard atmosphere (for refraction). None of these algorithms are able to account for local topology (a mountain directly in front of you), or deviating local weather conditions. ",
    "description": "",
    "tags": null,
    "title": "Available Data Sources",
    "uri": "/Suntimes/help/datasources/availablesources/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes can perform user-defined actions when a widget is clicked, or when an alarm or notification is shown or dismissed.\nAdd Actions ",
    "description": "",
    "tags": null,
    "title": "Actions",
    "uri": "/Suntimes/help/actions/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Alarms",
    "content": "Suntimes must be not optimized for alarms to work reliably. Settings \u003e Apps \u003e Suntimes \u003e Battery \u003e Unrestricted\nOn most devices the optimized setting may delay alarms up to 15 minutes. Other devices may restrict alarms without special configuration.\nThe following devices are affected: Asus, Blackview, Huawei, LENOVO, Meizu, OnePlus, OPPO, realme, Samsung, Sony, Unihertz, Vivo, WIKO, and Xiaomi. Check https://dontkillmyapp.com/ for solutions.\n",
    "description": "",
    "tags": null,
    "title": "Battery Optimization",
    "uri": "/Suntimes/help/alarms/batteryopt/index.html"
  },
  {
    "breadcrumb": "Suntimes",
    "content": "Install with F-Droid Suntimes is available from F-Droid, an installable catalogue of FOSS (Free and Open Source Software) applications for the Android platform.\nIn addition to being included in the official F-Droid repository, Suntimes related apps can also be installed from:\nhttps://forrestguice.codeberg.page/SuntimesApps/repo https://forrestguice.github.io/SuntimesApps/repo These repositories can be added to your F-Droid client as an alternate download source. Note: these additional repos are the same and differ only in their hosting.\nPay as you feel Suntimes is available gratis, but if it has proven its value, please pay as you feel.\nPaypal Liberapay ko-fi Buy Me a Coffee Software development and maintenance requires the use of real world resources, and cannot be sustained without them. Your willingness to assign it real world value is greatly appreciated.\nMore information… How will money be spent?\nAll money received from these channels is saved and periodically spent on items necessary for development. It will be used to replace aging devices and other necessary hardware.\nAs an example, my current budget Android phone was purchased using these funds ($100).\nHow much has been collected?\nUnfortunately less than you might think, and much less than hoped for.\nThe current project income isn’t enough to pay for essentials like time, energy, or expertise; these resources are all volunteered, but replacing development hardware is a realistic goal. The project has been soliciting donations for ~7 years, and now has enough to purchase a low end Pixel device.\nLicense Suntimes source code is available under GPLv3.\nCopyright © 2014-2024 Forrest Guice\nhttps://github.com/forrestguice/SuntimesWidget\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.\n",
    "description": "",
    "tags": null,
    "title": "Get Suntimes",
    "uri": "/Suntimes/download/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Worldmap",
    "content": " The background image should be a png or jpeg, with recommended dimensions 1024 x 512 or 1024 x 1024 or greater (will be scaled to fit the screen). The default maps use vector data from Natural Earth, and raster data from Blue Marble: Next Generation. The default maps use a white foreground (landmasses), and transparent background (ocean). The final coloring is determined by the app’s theme. The graticule and other markings are drawn behind the background image (requires transparency). The background image must have the appropriate map projection, center, and aspect ratio to align correctly. World Map Aspect Projection Center proj4 Basic 2:1 Equidistant Rectangular [0,0] +proj=eqc +lat_ts=0 +lat_0=0 +lon_0=0 +x_0=0 +y_0=0 +a=6371007 +b=6371007 +units=m +no_defs Polar [north] 1:1 Equidistant Azimuthal [90,0] +proj=aeqd +lat_0=90 +lon_0=0 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs\" Polar [south] 1:1 Equidistant Azimuthal [-90,0] +proj=aeqd +lat_0=-90 +lon_0=0 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs\" Azimuthal Equidistant 1:1 Equidistant Azimuthal [LAT,LON] +proj=aeqd +lat_0=LAT +lon_0=LON +x_0=0 +y_0=0 +a=6371000 +b=6371000 +units=m +no_defs\" where LAT and LON define the center of the projection. A GIS application (e.g. QGIS) can be used to create new map backgrounds. The map data needs to be re-projected or warped (see proj4 definitions), and exported to an image with the appropriate aspect ratio. The final image should be optimized to reduce its size (e.g. pngquant).\n",
    "description": "",
    "tags": null,
    "title": "Creating Backgrounds",
    "uri": "/Suntimes/help/worldmap/createbackgrounds/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Data Substitutions",
    "content": "%loc, %M, or %t can be used to include the location name, event name, or time zone as part of a widget’s title.\n%dm and %em@\u003cevent\u003e can be used to pass time (milliseconds) as part of an action; e.g. uri content://com.android.calendar/time/%dm opens the calendar app.\n%lat and %lon can be used to pass the location as part of an action; e.g. uri geo:%lat,%lon opens the map app.\n",
    "description": "",
    "tags": null,
    "title": "Examples",
    "uri": "/Suntimes/help/datasubstitutions/examples/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Data Sources",
    "content": "The app can be extended to add additional data sources by implementing the SuntimesCalculator interface.\n",
    "description": "",
    "tags": null,
    "title": "Adding Data Sources",
    "uri": "/Suntimes/help/datasources/addingsources/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes Alarms is an alarm clock with support for daily repeating alarms and notifications.\nBattery Optimization Notification Settings ",
    "description": "",
    "tags": null,
    "title": "Alarms",
    "uri": "/Suntimes/help/alarms/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Alarms",
    "content": "Notifications must be enabled for alarms to display correctly. Settings \u003e Apps \u003e Suntimes \u003e Notifications \u003e Show Notifications\n",
    "description": "",
    "tags": null,
    "title": "Notification Settings",
    "uri": "/Suntimes/help/alarms/notificationsettings/index.html"
  },
  {
    "breadcrumb": "Suntimes",
    "content": "Documentation on some of Suntimes more technical features can be found here. Links to these help topics are also included within the app (look for Online Help).\nActions Alarms Data Sources Data Substitutions Worldmap ",
    "description": "",
    "tags": null,
    "title": "Help",
    "uri": "/Suntimes/help/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes uses third-party libraries to perform astronomical calculations.\nThe data source settings allow for choosing between different libraries (or choosing between different algorithms offered by those libraries). These are advanced settings that affect the speed and accuracy of calculations, and may limit which features are available.\nAvailable Data Sources Adding Data Sources ",
    "description": "",
    "tags": null,
    "title": "Data Sources",
    "uri": "/Suntimes/help/datasources/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes supports limited %substitutions, a set of tags that are replaced with values from the data set. Substitutions may be used within widget title text, or within action data or extras.\nAvailable Substitions Examples ",
    "description": "",
    "tags": null,
    "title": "Data Substitutions",
    "uri": "/Suntimes/help/datasubstitutions/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes can be configured to display a custom world map background.\nDownload Backgrounds Creating Backgrounds ",
    "description": "",
    "tags": null,
    "title": "Worldmap",
    "uri": "/Suntimes/help/worldmap/index.html"
  },
  {
    "breadcrumb": "",
    "content": "\nSuntimes is an Android app that displays sunrise and sunset, twilights, blue and golden hour, solstices and equinoxes, moonrise and moonset, moon phases and illumination. It includes an alarm clock, and a large collection of widgets and add-ons.\nSuntimes Alarms\nAn alarm clock with support for daily repeating alarms and notifications. Suntimes Calendars\nAn add-on that supplies events to the Calendar app.",
    "description": "",
    "tags": null,
    "title": "Suntimes",
    "uri": "/Suntimes/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Actions",
    "content": " Common Intents Intents and Intent Filters (docs) To add an action… create an Intent by declaring:\nIntent (leave fields empty for an implicit intent) Action An action string. e.g. android.intent.action.ACTION_VIEW Class A fully qualified class name (case-sensitive). This must be a complete definition that includes both the package and class name. The class is required for explicit intents - leave it blank to allow the system to decide which class to launch. e.g net.osmand.plus.activities.MapActivity Data A Uri that contains or points to attached data. e.g. geo:30,31. Limited %substitutions are supported. e.g. geo:%lat,%lon Mime The mime type of attached data (if applicable). Leave blank for most types of data. Extras An \u0026 delimited string containing key-value pairs. Values may be Strings, int, long, double, float, or boolean. Limited %substitutions are supported. e.g. key1=\"some string\" \u0026 key2=1 \u0026 key3=1L \u0026 key4=1D \u0026 key5=1F \u0026 key6=true \u0026 key7=%dm An app that displays package info is useful for discovering Activities, which can then be launched with an explicit intent.\n",
    "description": "",
    "tags": null,
    "title": "Add Actions",
    "uri": "/Suntimes/help/actions/addaction/index.html"
  },
  {
    "breadcrumb": "Suntimes",
    "content": "",
    "description": "",
    "tags": null,
    "title": "Categories",
    "uri": "/Suntimes/categories/index.html"
  },
  {
    "breadcrumb": "Suntimes",
    "content": "",
    "description": "",
    "tags": null,
    "title": "Tags",
    "uri": "/Suntimes/tags/index.html"
  }
]
