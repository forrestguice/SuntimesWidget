var relearn_search_index = [
  {
    "breadcrumb": "Suntimes \u003e Help \u003e App Dialogs \u003e World Map",
    "content": "To use backgrounds… Store full-sized images somewhere on your device, then open with Set background. Suntimes uses URI permissions to access images stored on the SD card.\nProjection Map Backgrounds (click for full-size image) Equidistant Rectangular Equidistant Azimuthal Made with Natural Earth. https://www.naturalearthdata.com/about/terms-of-use/\nNASA Earth Observatory. Blue Marble: Next Generation. https://visibleearth.nasa.gov/view_cat.php?categoryID=1484\n",
    "description": "",
    "tags": null,
    "title": "Download Backgrounds",
    "uri": "/Suntimes/help/dialogs/worldmap/downloadbackgrounds/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Data \u003e Data Substitutions",
    "content": "Date/Time:\n%t for time zone (id) (e.g. US/Arizona) %d for formatted date (e.g. February 12) %dd for day (short) (e.g. Mon) %dD for day (long) (e.g. Monday) %dY for year (e.g. 2018) %dt for formatted time (of last update) %dT for formatted time with seconds (of last update) %dm for time in milliseconds (of last update) %eot for formatted ’equation of time’ (of last update) %eot_m for ’equation of time’ milliseconds (of last update) Location:\n%loc for label (e.g. Phoenix) %lat for latitude %lon for longitude %lel for elevation (e.g. 385 meters) Misc:\n%s for data source (e.g. sunrisesunsetlib) %id for appWidgetID Sun Widgets:\n%m for mode (short) (e.g. Civil) %M for mode (long) (e.g. Civil Twilight) %o for order (e.g. Last/Next, Today) %em@\u003cevent\u003e event milliseconds %et@\u003cevent\u003e event formatted time %eT@\u003cevent\u003e event formatted time (with seconds) %eA@\u003cevent\u003e event altitude %eZ@\u003cevent\u003e event azimuth %eD@\u003cevent\u003e event declination %eR@\u003cevent\u003e event right ascension where \u003cevent\u003e is:\nSun WidgetsSun Position Widgets \u003cevent\u003e rising sr setting ss noon sn rising setting civil twilight cr cs nautical twilight nr ns astronomical twilight ar as blue hour 4deg b4r b4s blue hour 8deg b8r b8s golden hour gr gs Moon Widgets:\n%i for moon illumination (e.g. 25%) %M for moon phase (e.g. Waxing Crescent) %o for order (e.g. Last/Next, Today) Solstice Widgets:\n%m for mode (short) (e.g. Solstice) %M for mode (long) (e.g. Winter Solstice) %o for order (e.g. Closest Event, Upcoming Event) ",
    "description": "",
    "tags": null,
    "title": "Available Substitutions",
    "uri": "/Suntimes/help/more/data/datasubstitutions/allsubstitions/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Data",
    "content": "Suntimes supports limited %substitutions, a set of tags that are replaced with values from the data set.\nAvailable Substitutions Substitutions may be used within widget titles, alarm notes, or within action data or extras.\nExamples %loc, %M, or %t can be used to include the location name, event name, or time zone as part of a widget’s title.\n%dm and %em@\u003cevent\u003e can be used to pass time (milliseconds) as part of an action; e.g. URI content://com.android.calendar/time/%dm opens the calendar app.\n%lat and %lon can be used to pass the location as part of an action; e.g. URI geo:%lat,%lon opens the map app.\n",
    "description": "",
    "tags": null,
    "title": "Data Substitutions",
    "uri": "/Suntimes/help/more/data/datasubstitutions/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Data \u003e Data Sources",
    "content": " Library Description sunrisesunsetlib Not Recommended Somewhat inaccurate. Does not support altitude, seconds-based calculation, solstice, equinox, or sun position. Based on “Almanac for Computers” by the USNO. github.com/mikereedell/sunrisesunsetlib-java ca.rmen.sunrisesunset Partially Recommended Similar to sunrisesunsetlib but with reasonable precision. Does not support altitude, solstice, equinox, or sun position. Based on the algorithms published by NOAA. github.com/caarmen/SunriseSunset time4a-simple Not Recommended Somewhat inaccurate. Does not support altitude. Based on “Almanac for Computers” by the USNO. time4a-noaa Partially Recommended Same algorithm used by ca.rmen.sunrisesunset with reasonable precision. Does not support altitude. Based on the algorithms published by NOAA. time4a-cc Recommended Good precision taking the altitude of locations into account. Based on “Calendrical Calculations” by Dershowitz/Reingold. Supports all features. time4a-time4j Recommended Default Best precision taking the altitude of locations, the elliptic shape of the earth and typical weather conditions into account. Based on “Astronomical Algorithm” by Jean Meeus. Supports all features. github.com/MenoData/Time4A A few important details:\nDo not expect precision better than minutes. The app hides seconds by default (but this can be enabled). The precision of the USNO and NOAA algorithms tends to be very inaccurate in polar regions. The time4j and cc algorithms may differ substantially from algorithms that do not account for altitude (up to 10 minutes). The time4j and cc algorithms differ from each other in that cc assumes the altitude of the observer by an approximated geodetic model, while time4j does it using a spheroid (WGS84) and the assumption of a standard atmosphere (for refraction). None of these algorithms are able to account for local topography (a mountain directly in front of you), or deviating local weather conditions. ",
    "description": "",
    "tags": "advanced",
    "title": "Available Data Sources",
    "uri": "/Suntimes/help/more/data/datasources/availablesources/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Add-ons \u003e Natural Hour",
    "content": " Location Time Format Hours of Equal Length Hours of Unequal Length Location The location is configured from Suntimes.\nTime Format Natural Hour can display time using 6-hour, 12-hour, or 24-hour format.\nHours of Equal Length The inner dial displays hours of equal length. Standard options include system time, local mean time, or time zones configured by Suntimes.\nOthers options:\nBabylonian hours are counted from sunrise. Italic hours are counted from sunset. Italian Civil hours are counted from civil dusk. Julian hours are counted from solar noon. Hours of Unequal Length The outer dial displays hours of unequal length. These are known as Roman hours, temporal hours, or seasonal hours.\nDaytime, the period between sunrise and sunset, is divided evenly into 12 hours. The first hour (I) of the day begins with sunrise, while the twelfth hour (XII) ends at sunset.\nSimilarly, nighttime is also divided into 12 hours. The first hour (I) of the night begins with sunset, and the twelfth hour (XII) ends at sunrise.\nThese daytime and nighttime hours are not the same, with length depending on latitude and varying with the seasons. For example, in the winter the daytime hours are shorter and nighttime hours longer, and vice versa in the summer.\nHours Begin Natural Hour can be configured to start counting Roman hours:\nat sunrise \u0026 sunset (12) at civil dawn \u0026 civil dusk (12) at civil dawn (24) at sunrise (24) at noon (24) at sunset (24) at civil dusk (24) ",
    "description": "",
    "tags": null,
    "title": "Configuration",
    "uri": "/Suntimes/help/addons/naturalhour/configuration/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes needs to be configured for best results.\nWelcome Select a Place Select a Time Zone ",
    "description": "",
    "tags": null,
    "title": "Configuration",
    "uri": "/Suntimes/help/configuration/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Settings",
    "content": "Suntimes -\u003e Settings -\u003e General to change general settings.\nWelcome Screen Starts the first launch configuration wizard. This guided configuration is also shown the first time the app is launched.\nTime Format System format (default) 12-hour 24-hour Units of Length Metric (kilometers, meters) Imperial (miles, feet) The default value will depend on your specific locale.\nLocalize to Hemisphere Apply hemisphere based localization. This setting will swap labels and icons (seasons, moon phases, etc) when configured to locations in the southern hemisphere.\nShow Seconds Include seconds when displaying twilight times. Do not expect precision better than minutes. This option is disabled by default.\nUse Elevation Apply altitude based refinements when calculating data (times may differ up to 10 minutes).\nData Source Select the data source for sun or moon based calculations (Advanced). The default is time4j-4a.\n",
    "description": "",
    "tags": null,
    "title": "General",
    "uri": "/Suntimes/help/more/settings/general/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e App Dialogs",
    "content": " Location Current Time Next Event Sunrise, Sunset, Twilight Moon Solstice, Equinox Location Suntimes displays the location name and coordinates in the action bar.\nTap to change the location.\nTap to update the current location.\nTap to show the location on a map. Visibility of the map button can be changed from Suntimes -\u003e Settings User Interface -\u003e Show Map Button.\nCurrent Time Suntimes displays the current time and time zone in the upper left.\nTap the time zone, or use : -\u003e Set Time Zone to change the time zone.\nTip Suntimes will display an info icon when daylight saving (or a similar rule) is in effect. Suntimes will display a warning icon when the time zone disagrees with local mean time. This is a common misconfiguration. Tip Suntimes will announce the time (and other changes to the UI) when using verbose TalkBack. Suntimes -\u003e Settings User Interface -\u003e Verbose TalkBack.\nNext Event Suntimes tracks the time until the next event in the upper right.\nTap the event field, or swipe left or swipe right, to step through events.\nSunrise, Sunset, Twilight Suntimes displays today’s sunrise, sunset, and twilight times.\nUse : -\u003e View date to scroll to a specific day, or swipe left or swipe right to see past and future days. Tap or to reset to today.\nTap the column headers to show the time until sunrise or sunset.\nTap noon or the lightmap to open the sun dialog.\nTip Suntimes can also display custom events. Suntimes -\u003e Settings User Interface -\u003e Manage Events\nTip Visibility of the twilight fields can be changed from the User Interface settings. Suntimes -\u003e Settings User Interface -\u003e Display. The lightmap can be toggled from User Interface -\u003e Show Light Map.\nSuntimes can also put emphasis on one of the fields. This can be changed from User Interface -\u003e Emphasize Field.\nMoon Suntimes displays moonrise, moonset, phase and illumination. Tapping this info opens the moon dialog.\nTip Visibility of the moon info can be toggled from the User Interface settings. Suntimes -\u003e Settings User Interface -\u003e Show Moon.\nSolstice, Equinox Suntimes displays the next solstice, equinox, or cross-quarter day. Tapping this field opens the solstice dialog.\nTip Visibility of solstice tracking can be toggled from the User Interface settings. Suntimes -\u003e Settings User Interface -\u003e Show Solstice / Equinox.\nData Source Suntimes displays the data source in the lower right. Tapping this field opens General settings.\nThe use elevation option can be toggled from the lower left. Altitude-based refinements will be applied when enabled.\nTip Visibility of the data source can be changed from the User Interface settings. Suntimes -\u003e Settings User Interface -\u003e Show Data Source.\n",
    "description": "",
    "tags": null,
    "title": "Main",
    "uri": "/Suntimes/help/dialogs/main/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Add-ons \u003e Suntimes Calendars",
    "content": " Calendar Integration Add Calendars Remove Calendars Open Calendar Calendar Integration Suntimes Calendars works by integrating with the calendar app to provide its own custom calendars.\nIt provides calendars for:\nAstronomical Twilight Nautical Twilight Civil Twilight Daylight (sunrise, noon, sunset) Golden Hour Blue Hour Moon (rising / setting) Moon Phases Moon Apsis (apogee / perigee) Solstices / Equinoxes Tip Additional calendars are available using add-ons. For example, Solunar Periods provides a “Hunting and Fishing” calendar.\nNote Suntimes Calendars requires calendar permissions to add, remove, or update calendars. Permissions must be granted before performing these actions (but may be safely revoked at other times).\nAdd Calendars When calendar integration is disabled, select multiple items, then enable integration to add all calendars at once. When calendar integration is enabled, select items to add them individually. Tip Updating calendars may take several minutes. It is safe to close the app while waiting; the update will continue in the background.\nRemove Calendars While calendar integration is enabled, de-select calendars to remove them individually. Disable calendar integration to remove all custom calendars at once. Tip Uninstalling Suntimes Calendars leaves calendars untouched. The app must be re-installed to remove them.\nOpen Calendar A separate calendar app is required to view calendars.\nTap : -\u003e Open Calendar to open the default app.\nTip Events should appear in most calendar apps automatically. Check troubleshooting if calendars fail to appear.\n",
    "description": "",
    "tags": null,
    "title": "Manage Calendars",
    "uri": "/Suntimes/help/addons/suntimescalendars/manage/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Widgets",
    "content": " Add Remove Resize Reconfigure Create Backup Restore Backup Add To add a widget, navigate to the home screen, long-press to show the widget menu, then hold and drag the widget to a position on the screen.\nThe configuration screen will be shown. Tap to add the widget.\nRemove To remove a widget, long press on it, then hold and drag it to the zone at the top of the screen.\nResize To resize a widget, long press on it to reveal the control frame, then hold and drag the controls.\nTip Widgets can be configured to swap layouts, or scale text and icons, to use all available space.\nWidgets can also be aligned to an edge or corner when there is extra space.\nReconfigure Suntimes -\u003e Settings -\u003e Widgets to show the widget list.\nTap on a widget in the list to configure it.\nTap to configure widget actions.\nTap to configure widget themes.\nCreate Backup Suntimes -\u003e Settings -\u003e Widgets to show the widget list, then : -\u003e Create Backup to save widgets (and other settings) to file.\nRestore Backup Suntimes -\u003e Settings -\u003e Widgets to show the widget list, then : -\u003e Restore Backup to import settings from file.\nAfter selecting a backup file you will be prompted to select an import method:\nRestore Backup. Imported settings will be cached for now and restored later when requested by the launcher. The launcher must support this option.\nBest Guess. Imported settings will be applied to existing widgets by type.\nDirect Import. Imported settings will be applied directly. This only works correctly if the widget ids are unchanged.\n",
    "description": "",
    "tags": null,
    "title": "Manage Widgets",
    "uri": "/Suntimes/help/widgets/manage/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Add-ons",
    "content": "A 24-hour clock and Roman timekeeping add-on for Suntimes.\nConfiguration Natural Hour can be installed from:\nhttps://forrestguice.codeberg.page/SuntimesApps/repo https://forrestguice.github.io/SuntimesApps/repo The issue tracker can be used to report bugs or request features.\nNatural Hour source code is available under GPLv3.\n",
    "description": "",
    "tags": "add-on",
    "title": "Natural Hour",
    "uri": "/Suntimes/help/addons/naturalhour/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Alarms",
    "content": " Alarm Types Schedule by Time Schedule by Event Show Alarm Time Alarm Types Suntimes supports multiple alarm types. Tap to choose the type.\nAlarms will sound until they are dismissed or snoozed. Notifications remain in the tray until they are dismissed. Quick Notifications are shown for a brief moment. Tip Quick Notifications are automatically dismissed after 30s (changed from Suntimes -\u003e Settings -\u003e Alarms -\u003e Auto-dismiss after). Regular notifications will remain the tray until they are dismissed.\nSchedule by Time Tap the Time tab to schedule alarms by time.\nUse the picker to select a time, then tap to accept and edit the alarm.\nSuntimes can also schedule alarms using solar time. Tap to change the time standard:\nSystem Time Zone (default) Apparent Solar Time Local Mean Time Suntimes can schedule an alarm on a specific date. Tap to open the date picker. Choose a date, then tap to accept.\nSchedule by Event Tap the Event tab to schedule alarms by event.\nSelect an event from the drop-down list, then tap to accept and edit the alarm.\nTap to select a custom event (or an event provided by an add-on).\nTap to change the alarm’s location.\nShow Alarm Time The alarm time is displayed at the bottom of the dialog.\nThe icon is displayed if the time will be adjusted by a before/after offset. Tap the icon to momentarily display the combined alarm + offset. The alarm offset can be configured when editing the alarm.\n",
    "description": "",
    "tags": null,
    "title": "Set Alarm",
    "uri": "/Suntimes/help/alarms/setalarm/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More",
    "content": " Suntimes -\u003e Settings to change application settings.\nGeneral Suntimes -\u003e Settings -\u003e General to change general settings.\nAlarms Suntimes -\u003e Settings -\u003e Alarms to change alarm settings.\nLanguage Suntimes -\u003e Settings -\u003e Language to change language settings.\nPlaces Suntimes -\u003e Settings -\u003e Places to change place settings.\nUser Interface Suntimes -\u003e Settings -\u003e User Interface to change user interface settings.\nWidgets Suntimes -\u003e Settings -\u003e Widgets to reconfigure widgets, or configure widget themes, or widget actions.\n",
    "description": "",
    "tags": null,
    "title": "Settings",
    "uri": "/Suntimes/help/more/settings/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e App Dialogs",
    "content": " Current Position Light map Altitude Graph Seek Altitude Play (animation) View date Object Shadow Current Position Suntimes displays the sun’s current azimuth and altitude, azimuth at sunrise and sunset, and altitude at solar noon.\nLight map Suntimes can display a graph of daylight and twilight periods.\nTip Use Suntimes -\u003e Settings User Interface -\u003e Show Light Map to show the light map view on the main screen.\nAltitude Graph Use : -\u003e Altitude Graph to toggle the altitude graph, and : -\u003e Options to change the graph’s options.\nGrid Labels Axis Filled Path Show Moon Note Changes to the graph options are also applied to home screen widgets.\nSeek Altitude Suntimes can find the time when the sun will be at a given angle in the sky.\nUse : -\u003e Seek Altitude to specify the angle.\nTap to find the rising time, or to find the setting time.\nTip Tapping the sunrise, noon, or sunset areas (next to the altitude field) will seek to the time of those events.\nPlay (animation) Suntimes can animate the light map and altitude graphs at different rates:\n5m the frames are 5 minutes apart. 1d the frames are 24 hours apart. Tap and to step through frames one at a time.\nTap to play the animation, to pause it, and to reset to the current moment.\nView Date Use : -\u003e View date to open other dialogs at the selected date/time.\nObject Shadow Suntimes can find the length of the shadow cast by an object with a given height.\nUse : -\u003e Object Shadow to change the height.\nTap to reduce the object’s height, and to increase it.\nThe object height can also be changed from Suntimes -\u003e Settings User Interface -\u003e Object Shadow.\nNote The length of the shadow will be ∞ when the sun is below the horizon.\n",
    "description": "",
    "tags": null,
    "title": "Sun",
    "uri": "/Suntimes/help/dialogs/sun/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Configuration",
    "content": "Suntimes will display a welcome screen to assist with initial configuration.\nAppearance User Interface Location Time Zone Alarms Tip The welcome screen is automatically displayed the first time the app is launched. It can be revisited later from Suntimes -\u003e Settings -\u003e General -\u003e Welcome.\nAppearance Suntimes supports light and dark themes, and can switch between them to match the system. It also includes high contrast versions of these themes.\nSuntimes supports larger text sizes (while also respecting the system’s text size settings). Choose a larger text size to improve readability.\nTip Appearance settings can be changed later from Suntimes -\u003e Settings -\u003e User Interface -\u003e Appearance.\nUser Interface Suntimes can be customized to show or hide information. Choose the fields that should be displayed by the main screen (and hide others).\nTip It is also possible to show custom events that occur when the sun is at a given angle.\nUser Interface settings can be changed later from Suntimes -\u003e Settings -\u003e User Interface.\nLocation Select a place. Suntimes provides a locale specific default (and includes a large list of world places).\nUse Add World Places to add all defaults to the list of places, or Import to import a previously saved list.\nUse Get Location to add the device’s current location to the list.\nNote The location is manually configured by default; Suntimes does not require location permissions (optional).\nTime Zone Select a time zone. Suntimes displays information using the system time zone by default.\nTip Choose a time zone within a couple hours of local mean time for best results.\nVerify the time zone if reported times seem inaccurate or don’t make sense. A mismatch between the time zone and location is a common misconfiguration.\nAlarms Use Import to restore alarms from a previous installation of Suntimes.\nExtra configuration may be required for alarms to work reliably. Please follow all recommendations if you intend to use alarm features.\n",
    "description": "",
    "tags": null,
    "title": "Welcome",
    "uri": "/Suntimes/help/configuration/welcome/index.html"
  },
  {
    "breadcrumb": "Suntimes",
    "content": "Install with F-Droid Suntimes is available from F-Droid, an installable catalog of FOSS (Free and Open Source Software) applications for the Android platform.\nIn addition to being included in the official F-Droid repository, Suntimes related apps can also be installed from:\nhttps://forrestguice.codeberg.page/SuntimesApps/repo https://forrestguice.github.io/SuntimesApps/repo These repositories can be added to your F-Droid client as an alternate download source. Note: these additional repos are the same and differ only in their hosting.\nPay as you feel Suntimes is available gratis, but if it has proven its value, please pay as you feel.\nPaypal Liberapay ko-fi Buy Me a Coffee Software development and maintenance requires the use of real world resources, and cannot be sustained without them. Your willingness to assign it real world value is greatly appreciated.\nMore information… How will money be spent?\nAll money received from these channels is saved and periodically spent on items necessary for development. It will be used to replace aging devices and other necessary hardware.\nAs an example, my current budget Android phone was purchased using these funds ($100).\nHow much has been collected?\nUnfortunately less than you might think, and much less than hoped for.\nThe current project income isn’t enough to pay for essentials like time or expertise; these resources are all volunteered, but replacing development hardware is a realistic goal. The project has been soliciting donations for ~7 years, and now has enough to purchase a low end Pixel device.\nLicense Suntimes source code is available under GPLv3.\nCopyright © 2014-2024 Forrest Guice\nhttps://github.com/forrestguice/SuntimesWidget\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.\n",
    "description": "",
    "tags": null,
    "title": "Get Suntimes",
    "uri": "/Suntimes/download/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": " Main Sun Moon Solstice, Equinox World Map Use from the main screen to access app dialogs. Dialogs can also be shown by tapping different UI elements, or triggered using actions.\n",
    "description": "",
    "tags": null,
    "title": "App Dialogs",
    "uri": "/Suntimes/help/dialogs/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e App Dialogs \u003e World Map",
    "content": " The background image should be a png or jpeg, with recommended dimensions 1024 x 512 or 1024 x 1024 or greater (will be scaled to fit the screen). The default maps use vector data from Natural Earth, and raster data from Blue Marble: Next Generation. The default maps use a white foreground (landmasses), and transparent background (ocean). The final coloring is determined by the app’s theme. The graticule and other markings are drawn behind the background image (requires transparency). The background image must have the appropriate map projection, center, and aspect ratio to align correctly. World Map Aspect Projection Center proj4 Basic 2:1 Equidistant Rectangular [0,0] +proj=eqc +lat_ts=0 +lat_0=0 +lon_0=0 +x_0=0 +y_0=0 +a=6371007 +b=6371007 +units=m +no_defs Polar [north] 1:1 Equidistant Azimuthal [90,0] +proj=aeqd +lat_0=90 +lon_0=0 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs\" Polar [south] 1:1 Equidistant Azimuthal [-90,0] +proj=aeqd +lat_0=-90 +lon_0=0 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs\" Azimuthal Equidistant 1:1 Equidistant Azimuthal [LAT,LON] +proj=aeqd +lat_0=LAT +lon_0=LON +x_0=0 +y_0=0 +a=6371000 +b=6371000 +units=m +no_defs\" where LAT and LON define the center of the projection. A GIS application (e.g. QGIS) can be used to create new map backgrounds. The map data needs to be re-projected or warped (see proj4 definitions), and exported to an image with the appropriate aspect ratio. The final image should be optimized to reduce its size (e.g. pngquant).\n",
    "description": "",
    "tags": "advanced",
    "title": "Creating Backgrounds",
    "uri": "/Suntimes/help/dialogs/worldmap/createbackgrounds/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Data \u003e Data Sources",
    "content": "The app can be extended to add additional data sources by implementing the Suntimes Calculator interface.\n",
    "description": "",
    "tags": "advanced",
    "title": "Adding Data Sources",
    "uri": "/Suntimes/help/more/data/datasources/addingsources/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Data",
    "content": "Suntimes uses third-party libraries to perform astronomical calculations.\nThe data source settings allow for choosing between different libraries (or choosing between different algorithms offered by those libraries).\nSuntimes -\u003e Settings -\u003e General -\u003e Advanced\nThese are advanced settings that affect the speed and accuracy of calculations, and may limit which features are available.\nAvailable Data Sources Adding Data Sources ",
    "description": "",
    "tags": "Advanced",
    "title": "Data Sources",
    "uri": "/Suntimes/help/more/data/datasources/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Widgets",
    "content": " Layout Appearance General Time Zone Location Action Layout Suntimes groups widgets by type of data (sun, moon, etc). Each widget type offers a selection of different layouts. Change the layout settings to choose the widget that will be displayed at a given size.\nSwap layouts to fill available space Suntimes can swap between widget layouts when resized to take advantage of available space.\nEnabled :: The widget will switch between layouts depending on available space. Disabled :: The widget maintains its initial layout when resized. Use this with “scale text and icons” for extra large widgets. Appearance Theme Suntimes can customize the appearance of widgets with themes.\nScale text and icons Suntimes can scale widget text and icons to fill all available space.\nDisabled :: Text and icons are displayed at normal size. Enabled :: Text and icons will be scaled to fill available space. Align base Suntimes can align widgets to an edge or corner when there is extra space.\nTop-Left Top Top-Right Left Center Right Bottom-Left Bottom Bottom-Right General Show Title Widgets can be configured to show a title in the top left corner.\nTip Title text supports limited % substitutions.\nShow Labels Some widgets can be configured to show optional labels.\nTime Format Widgets can be configured to display time in 12 or 24 hour format.\nSystem Format (follows system settings) 12-hour 24-hour Suntimes (follows app settings) Data Source Widgets can be configured to use a specific data source.\nTime Zone System Time Zone The widget is configured to the current system time zone. [default]\nUser Defined The widget is configured to a given user-defined time zone.\nTap to sort the list, then select a time zone from the drop-down.\nTime Standard Widgets can be configured to display:\nLocal Mean Time (LMT) Apparent Solar Time (LTST) Local Sidereal Time (LMST) Greenwich Sidereal Time (GMST) Coordinated Universal Time (UTC) Use App Time Zone The widget will use the same time zone as the main app; changes to the app setting will be reflected by widgets.\nLocation User Defined The widget is configured to a given set of user-defined coordinates. [default]\nSelect a place from the drop-down list, or tap to open the places list.\nTap to change the coordinates or label (then tap to save changes, or to cancel). If the label was changed then the coordinates will be saved as a new place.\nCurrent (last known) The widget will use the “last known” location. The home screen widgets use the passive location provider, so a separate app is required to query the location.\nUse App Location The widget will use the same location as the main app; changes to the app setting will be reflected by widgets.\nAction Suntimes can perform an action whenever a widget is tapped.\nDo Nothing Update Widget Reconfigure Widget [default] Perform Action Update All Widgets ",
    "description": "",
    "tags": null,
    "title": "Configure Widgets",
    "uri": "/Suntimes/help/widgets/configure/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Alarms",
    "content": " Type Label Note Event (or time) Repeat Offset (before/after) Location Reminder Sound Vibration On Alert Action On Dismiss Action On Dismiss Challenge Tap to cancel without making changes.\nTap to save, or to save and enable.\nTap to save and disable.\nTap to cancel and delete.\nType Tap to change the alarm type:\nAlarm Notification Quick Notification Tip Quick Notifications are automatically dismissed after 30s. Suntimes -\u003e Settings -\u003e Alarms -\u003e Auto-dismiss after to change this setting.\nLabel Tap Label to change the alarm label.\nNote Tap to change the alarm note.\nTip The note is displayed with the label, and supports limited %substitutions.\nEvent (or time) Tap to change the alarm’s event or time.\nRepeat Suntimes supports daily repeating alarms, and will automatically reschedule alarms as event times change throughout the year.\nTap to change repeat options.\nOffset (before/after) Suntimes can schedule alarms before or after an event or specified time.\nTap at to change the offset options.\nTip Tap the alarm time preview to see the combined event + offset alarm time.\nLocation Suntimes can schedule alarms for events occurring in different locations.\nTap to change the location.\nReminder Suntimes can show a reminder several hours before an alarm triggers, and perform an additional action at that time.\nTap to toggle the reminder.\nTip The reminder notification allows alarms to be dismissed early. Dismissing a repeating alarm will automatically reschedule it for the next day.\nSound Suntimes can play a ringtone or audio file when an alarm sounds.\nTap to change the alert sound, or select No Sound for a silent alarm.\nTip Enable Suntimes -\u003e Settings -\u003e Alarms -\u003e Show all ringtones to allow selection of all system sounds regardless of their intended use.\nVibration Suntimes can cause the device to vibrate when an alarm triggers.\nTap to enable vibration.\nOn Alert Action Suntimes can perform an action when an alarm triggers.\nTap On Alert to change the alert action.\nOn Dismiss Action Suntimes can perform an action when dismissing an alarm.\nTap On Dismiss to change the dismiss action.\nOn Dismiss Challenge Suntimes can require passing a challenge before dismissing a sounding alarm.\nTap On Dismiss to change the dismiss challenge.\nNone Easy Math (single digit math problems) Tip Add-ons can provide additional challenges; e.g. Suntimes NFC can be used to dismiss alarms using NFC tags.\n",
    "description": "",
    "tags": null,
    "title": "Edit Alarm",
    "uri": "/Suntimes/help/alarms/editalarm/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Settings",
    "content": "Suntimes -\u003e Settings -\u003e Language to change language settings.\nMode System to use the system locale (default). User Defined to override the system locale. Note It is recommended to change System settings instead of overriding the locale. In some cases User Defined may fail to work as expected (partial translation).\nLanguage Suntimes has been translated into the following languages:\nArabic Basque Catalan Czech Dutch Esperanto French German Hungarian Italian Norwegian Polish Portuguese Russian Simplified Chinese Spanish Traditional Chinese Note All translations are volunteer contributions! On occasion a translation may be incomplete or may contain minor errors. See the contribution guidelines if you would like to help update or improve a translation.\n",
    "description": "",
    "tags": null,
    "title": "Language",
    "uri": "/Suntimes/help/more/settings/language/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Places",
    "content": " Places List Add World Places Search Add Place Copy, Edit, Remove Share Place Import, Export Clear Places Places List Suntimes -\u003e Settings -\u003e Places -\u003e Manage Places to open the places list.\nThe places list can also be reached by tapping when selecting a place.\nAdd World Places Suntimes includes coordinates for over 200 cities located around the globe.\nUse : -\u003e Add World Places to add world places to the list.\nSearch Tap to search the list of places.\nAdd Place To add a place, tap to show the add place dialog.\nTap to query the device’s current location, or manually input coordinates.\nFinally, tap to save the place.\nPermissions Getting the current location requires location permissions. When prompted, grant permissions and enable device location to use this feature.\nCopy, Edit, Remove To copy a place, select it, then tap : -\u003e Copy.\nTo remove a place, select it, then tap : -\u003e Delete.\nTo edit a place, select it, then tap edit. Modify the label or coordinates, then tap to save changes.\nShare Place To share a place, select it, then tap share.\nPlace coordinates can be displayed on a map, or by other applications that support location sharing.\nTip The currently configured place can be shared from the main screen using the map button. Import, Export Use : -\u003e Export to export the list of places to file.\nUse : -\u003e Import to import a previously exported list.\nTip It is also possible to export or import places by creating or restoring a backup.\nClear Places Use : -\u003e Clear to clear the list of places.\nTip Changes made to the place list will not affect existing configurations. It is safe to clear the list at any time. Places will be automatically added to the list when (re)configuring widgets or alarms.\n",
    "description": "",
    "tags": null,
    "title": "Manage Places",
    "uri": "/Suntimes/help/more/places/manageplaces/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Tiles",
    "content": " Add, Remove Tiles Configure Tiles Add, Remove Tiles Pull down from the top of the screen to open the system tray, then tap to edit.\nTo add a tile, hold and drag the tile to the top of the tray.\nTo remove a tile, hold and drag the tile to the bottom of the tray (unused area).\nConfigure Tiles From the system tray, tap a tile to open the tile dialog, then tap Settings (bottom left). The configuration screen will be shown.\nTip Tile settings can be included in backups and restored with other widget settings.\n",
    "description": "",
    "tags": null,
    "title": "Manage Tiles",
    "uri": "/Suntimes/help/tiles/manage/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e App Dialogs",
    "content": " Phase, Illumination, and Position Playback Moonrise, moonset Synodic Month Anomalistic Month More Options Phase, Illumination, and Position Suntimes displays the moon’s current phase, illumination, azimuth, and altitude.\nTip Use Suntimes -\u003e Settings User Interface -\u003e Show Moon to show this info in the main table.\nPlayback Use : -\u003e Playback to show the controls.\nTap and to step through positions at 5 minute intervals. Tap to reset to the current time (on the selected day).\nMoonrise, Moonset Suntimes displays the time of moonrise and moonset.\nSwipe left or right to scroll to past or upcoming days, or tap to reset to today.\nTip The month views will scroll automatically to show dates around the selected day.\nShow Lunar Noon Suntimes can also display the time of lunar noon and lunar midnight.\nUse : -\u003e Show Lunar Noon to toggle this option.\nSynodic Month Suntimes displays the time/date of the moon’s major phases (synodic month), and labels the “Super Moons”.\nSwipe left or right to scroll to future or past months, or tap to reset the view.\nColumns (phases) Use : -\u003e Columns (phases) to change the number of columns displayed.\nAnomalistic Month Suntimes displays the time/date of the moon’s apogee and perigee (anomalistic month), and the moon’s current distance.\nSwipe left or right to scroll to future or past months, or tap to reset the view.\nMore Options Tap on a field to show the context menu button.\nUse : -\u003e Set Alarm to set an alarm for the selected event.\nUse : -\u003e View Date to open other dialogs at the selected date/time.\nUse : -\u003e Share to copy text to clipboard.\n",
    "description": "",
    "tags": null,
    "title": "Moon",
    "uri": "/Suntimes/help/dialogs/moon/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Add-ons \u003e Suntimes Calendars",
    "content": " Location Window Color Event Flags Event Strings Event Template Reminders Location The location is configured from Suntimes.\nNote The location is applied when calendars are added.\nWindow Tap Calendar Window to change the time period of included events.\nNote The calendar window is applied when calendars are added.\nColor Tap -\u003e Color to change the calendar’s display color.\nTitle Tap -\u003e Title to change the calendar’s display title.\nEvent Flags Tap -\u003e Event Flags to customize included calendar events.\nUncheck events that should be omitted from the calendar.\nNote Event flags are applied when calendars are added.\nEvent Strings Tap -\u003e Event Flags -\u003e Event Strings to customize event display strings.\nEvent strings are available to the calendar template using %M.\nNote Event strings are applied when calendars are added.\nEvent Template Tap -\u003e Event Template to customize the template.\nEach event template supplies:\nName: A short event title. Location: The event’s location (optional). Description: The expanded event description. Templates use % substitutions:\n%cal calendar name %summary calendar summary %color calendar color hex %% % character %loc location name %lat location latitude %lon location longitude %lel location elevation %M event title (supplied by event strings) %em event milliseconds %eZ event azimuth %eA event altitude %eR event right ascension %eD event declination %dist moon distance %illum moon illumination % %phase moon minor phase Note Event templates are applied when calendars are added.\nReminders Tap -\u003e Reminders to configure calendar reminders.\nTap Add Reminder to add a reminder to all events in a calendar. Tap to remove reminders.\nTap to apply changes when done. Reminders will be updated immediately if the calendar is already enabled.\nTip Updating reminders may take several minutes. It is safe to close the app while waiting; the update will continue in the background.\n",
    "description": "",
    "tags": null,
    "title": "Options",
    "uri": "/Suntimes/help/addons/suntimescalendars/options/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Configuration",
    "content": "Suntimes requires location data to configure dialogs, alarms, and widgets.\nIt is recommended to configure the app to some user-defined location (such as a local park, intersection, or city center). Suntimes can also periodically query and update using the current location.\nUser-Defined Location Current (last known) User-Defined Location Suntimes is configured to a user-defined location by default. The use of location permissions is optional.\nConfigure the location using:\nPrevious place: choose a previously used place from the drop-down list. Places list: tap to open the places list, select an item, then tap to choose that place. Manual input: Tap to edit the displayed place. Change the place’s coordinates and label, then tap to save. The modified label will be added as a new place. Current location: Tap to edit the displayed place, then tap to query the device’s current location. Change the place’s label, then tap to save. The modified label will be added as a new place. Tip You can also use a separate app to pick the location. Sharing a location with Suntimes will open the location dialog to the selected coordinates.\nTip Suntimes supports coordinates with meter accuracy but this level of detail is optional. For best results, specify coordinates within 60 miles of your current position.\nCurrent (last known) Suntimes can be configured to use the current (last known) location. Location permissions are required.\nWhen configured to current (last known), the application will periodically query the current location whenever launched or resumed, and widgets will use the last known location when updated.\nChange the place settings to configure current (last known) behavior.\n",
    "description": "",
    "tags": null,
    "title": "Select a Place",
    "uri": "/Suntimes/help/configuration/selectplace/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Add-ons",
    "content": "An add-on that supplies events to the Calendar app.\nManage Calendars Options Troubleshooting Suntime Calendars can also be installed from:\nhttps://forrestguice.codeberg.page/SuntimesApps/repo https://forrestguice.github.io/SuntimesApps/repo The issue tracker can be used to report bugs or request features.\nSuntimes Calendars source code is available under GPLv3.\n",
    "description": "",
    "tags": "add-on",
    "title": "Suntimes Calendars",
    "uri": "/Suntimes/help/addons/suntimescalendars/index.html"
  },
  {
    "breadcrumb": "Suntimes",
    "content": "User Manual Documentation on Suntimes can be found here. Links to these topics are also included within the app ( Online Help).\nConfiguration App Dialogs Alarms Widgets Tiles Add-ons More User Support The issue tracker can be used to report bugs or request features.\n",
    "description": "",
    "tags": null,
    "title": "Help",
    "uri": "/Suntimes/help/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes Alarms is a sunlight aware alarm clock with support for daily repeating alarms and notifications.\nSet Alarm Edit Alarm Alarm List Alarm Settings Alarm Recommendations ",
    "description": "",
    "tags": null,
    "title": "Alarms",
    "uri": "/Suntimes/help/alarms/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Alarms",
    "content": " Alarm List Select, Deselect Add Alarm Edit Alarm Delete, Clear Snooze, Dismiss Sort Alarms Export, Import Alarm List The alarm list can be shown from Suntimes -\u003e Set Alarm, or by tapping Suntimes Alarms from the app launcher.\nTip Suntimes -\u003e Settings -\u003e Alarms -\u003e Launcher Icon to enable the Suntimes Alarms icon in the app launcher.\nSelect, Deselect Tap an item in the list to select it. Selected items display the alarm note and additional controls.\nTap on the lower left of the screen, or press back to clear the selection.\nAdd Alarm Tap to set an alarm. The alarm event dialog will be shown.\nEdit Alarm Tap an item in the list to select it. Tap the item again to edit the alarm.\nDelete, Clear Tap an item in the list to select it, then tap the button to remove the alarm.\nUse : -\u003e Clear to remove all alarms.\nSnooze, Dismiss Tap an item in the list to select it. Sounding alarms will display additional buttons.\nTap snooze or dismiss, or tap the item again to display the fullscreen notification.\nSort Alarms Use : -\u003e Sort to sort the alarm list by\nAlarm Time Creation Date Use Enabled first to move enabled items to the top of the list.\nUse Show Offsets to show before/after offsets separately from the alarm time.\nExport, Import Use : -\u003e Export to export the alarm list to file.\nUse : -\u003e Import to import alarms from a previous export.\nTip It is also possible to export or import alarms by creating or restoring a backup.\n",
    "description": "",
    "tags": null,
    "title": "Alarm List",
    "uri": "/Suntimes/help/alarms/alarmlist/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Places",
    "content": "Suntimes can query the device’s current location using the GPS or Network providers.\nTo configure this behavior, use Suntimes -\u003e Settings -\u003e Places to access the place settings.\nPermissions Getting the current location requires location permissions. When prompted, grant permissions and enable device location.\nLocation time limit When requesting the current location, Suntimes will actively wait for a location update until the time limit expires.\nThe default location time limit is 1 minute.\nNote Location requests may time out if GPS or Network location providers are unavailable (and the current location is older than the max age).\nLocation recent max age When requesting the current location, Suntimes will trigger a location update if the current location is older than the max age.\nThe default location max age is 5 minutes.\nPassive Location Suntimes can use the passive location provider to determine the device’s location. A separate app is required to make active update requests.\n",
    "description": "",
    "tags": null,
    "title": "Place Settings",
    "uri": "/Suntimes/help/more/places/placesettings/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Configuration",
    "content": "Suntimes can be configured to use a given time zone (or other time standards).\nSystem Time Zone User-Defined Time Zone Time Standard System Time Zone Suntimes displays time using the system time zone by default.\nUser-Defined Time Zone Suntimes can display time using a specific time zone.\nChoose user-defined, tap to sort the list, then select a time zone from the drop-down.\nTap -\u003e Recommend Time Zone to suggest a time zone closely matching local mean time.\nNote Time zone recommendations work best for named places in the IANA time zone database and may produce inaccurate results for other places.\nTip Choose a time zone within a couple hours of local mean time for best results.\nVerify the time zone if reported times seem inaccurate or don’t make sense. A mismatch between the time zone and location is a common misconfiguration.\nTip Suntimes will display an info icon when daylight saving (or a similar rule) is in effect. Time Standard Suntimes can also display time using:\nLocal Mean Time (LMT) Apparent Solar Time (LTST) Local Sidereal Time (LMST) Greenwich Sidereal Time (GMST) Coordinated Universal Time (UTC) ",
    "description": "",
    "tags": null,
    "title": "Select a Time Zone",
    "uri": "/Suntimes/help/configuration/timezone/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e App Dialogs",
    "content": " Solstices, Equinoxes (tracking) Cross-Quarter Days Tropical Year More Options Solstices, Equinoxes (tracking) Suntimes displays the solstices and equinoxes, and can track the number of days until or since those events.\nUse : -\u003e Track to change tracking to the recent, nearest, or upcoming event.\nThis option can also be changed from the User Interface settings. Suntimes -\u003e Settings User Interface -\u003e Solstice Tracking.\nTip Tracking can be enabled in the main view using Suntimes -\u003e Settings User Interface -\u003e Show Solstice / Equinox.\nCross-Quarter Days Suntimes can display the midpoints between each solstice and equinox (the cross-quarter days).\nUse : -\u003e Cross-Quarter Days to toggle this setting.\nThis option can also be changed from the User Interface settings. Suntimes -\u003e Settings User Interface -\u003e Cross-Quarter Days.\nNote Changes to the Cross-Quarter Days setting are also applied to home screen widgets.\nTropical Year Suntimes displays the length of the tropical year.\nMore Options Tap on a row to select it and reveal the context menu button.\nUse : -\u003e Set Alarm to set an alarm for the selected event.\nUse : -\u003e View Date to open other dialogs at the selected date/time.\nUse : -\u003e Share to copy text to clipboard.\n",
    "description": "",
    "tags": null,
    "title": "Solstice, Equinox",
    "uri": "/Suntimes/help/dialogs/solstice/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Widgets",
    "content": "Suntimes can customize the appearance of widgets using widget themes.\nDefaults Manage Themes To access the theme list Suntimes -\u003e Settings -\u003e Widgets -\u003e Themes\nAdd, Edit, Copy, Remove To add, edit, copy, or remove themes, navigate to the theme list.\nTo add a theme, tap add.\nTo copy a theme, select it, then tap copy to create a new theme with the same values.\nTo edit a theme, select it, then tap edit.\nTo remove a theme, select it, then tap delete.\nNote The default themes cannot be modified; the edit and remove actions will not be shown. Share, Export, Import From the theme list, select a theme, then tap to share the theme as a file.\nUse : -\u003e Export to export all themes to file.\nUse : -\u003e Import to import previously shared or exported themes.\nEdit Themes TODO\n",
    "description": "",
    "tags": null,
    "title": "Widget Themes",
    "uri": "/Suntimes/help/widgets/themes/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Add-ons \u003e Suntimes Calendars",
    "content": " Calendars are not shown (missing calendars) Calendars are not shown (missing calendars) The calendars managed by Suntimes Calendars should automatically appear in your Calendar app. For some apps however they may fail to appear without extra configuration.\nFossify Calendar Fossify Calendar (and other forks of Simple Calendar) require enabling CalDAV sync.\nadd calendars from Suntimes Calendars. from Fossify Calendar verify that : -\u003e Settings -\u003e CalDAV -\u003e CalDAV sync is checked. from Fossify Calendar navigate to Settings -\u003e Manage synced calendars and enable each calendar entry. from Fossify Calendar use : -\u003e Refresh CalDAV calendars. Google Calendar (Android 15) Some versions of Google Calendar may require enabling the Suntimes local account.\nadd calendars from Suntimes Calendars. from Google Calendar navigate to Settings -\u003e Manage accounts. enable the Suntimes account (listed under Non-Google accounts). ",
    "description": "",
    "tags": null,
    "title": "Troubleshooting",
    "uri": "/Suntimes/help/addons/suntimescalendars/troubleshooting/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Settings",
    "content": "Suntimes -\u003e Settings -\u003e User Interface to change user interface settings.\nAppearance Text Size Display (Default Events) Display (Custom Events) Display (Other) Miscellaneous Tap Actions Appearance Light always shows light theme. Dark always shows dark dark. System default follows the system theme, and will automatically switch between light and dark themes. Light Theme Default (Light) High Contrast (Light) Dark Theme Default (Dark) High Contrast (Dark) Text Size Small (12) Normal (14) Large (16) Extra Large (18) Tip The text size setting also respects the system settings.\nAdjust system text size first, then override the size in Suntimes for readability if necessary.\nDisplay (Default Events) Tap options to toggle the visibility of default fields on the main screen.\nAstronomical Twilight Nautical Twilight Blue Hour Civil Twilight Actual Time (sunrise, sunset) Solar Noon Golden Hour Emphasize Field The emphasized field is displayed with increased text size (defaults to actual time).\nDisplay (Custom Events) Tap Manage Events to add or remove custom events.\nDisplay (Other) Twilight\nHeader Icon. Show column header icons. Header Text. Show column header text. [None, Labels, Azimuth] Light Map. Show a stacked bar chart of day, night, and twilight periods. Moon\nMoon. Show moon rise and set times, phase, and illumination. Lunar Noon. Show lunar noon and lunar midnight as part of the moon dialog. Solstice / Equinox\nSolstice / Equinox. Show time until next solstice or equinox. Cross-Quarter Days. Include midpoints between solstices and equinoxes. Solstice Tracking. Track the [Recent, Nearest, Upcoming] event. Object Shadow. Display the length of a shadow cast by an object with a given height (defaults to 1.83 meters).\nMiscellaneous Show Map Button. Show an action bar button that opens the default map app. Show Data Source. Show a label indicating the current configuration. Show Warnings. Show configuration warning messages. Verbose TalkBack. Announce automated changes to the UI. More options:\nShow Weeks. Divide time spans greater than 7 days into weeks (e.g. 15d becomes 2w 1d). Show Hours. Include hours and minutes in time spans greater than a day. Show Time (with dates). Include the time when displaying dates. Tap Actions The main screen can be customized to perform specific actions when parts of the UI are tapped.\nThe default tap actions are:\nOn Clock Tap, show next upcoming event. On Date Tap, swap cards (today/tomorrow). On Date Long Press, open the calendar. On Note Tap, show the next note. Tap on each setting to pick from suggested actions, or tap the icon to select from all actions.\n",
    "description": "",
    "tags": null,
    "title": "User Interface",
    "uri": "/Suntimes/help/more/settings/userinterface/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e App Dialogs",
    "content": " Maps Map Options Play Share (Record) View Date Set Background Maps Basic Blue Marble Polar (North) Polar (South) Azimuthal Equidistant (Phoenix) Map Options Use : -\u003e Options to change the map’s options.\nSunlight (shadow) Moonlight Location (dot) Graticule (minor grid lines) Major Lines (equator, tropics, arctic circle) Note The map options are also applied to home screen widgets.\nPlay (animation) Suntimes can animate the world map at different rates.\n15m the frames are 15 minutes apart. 1d the frames are 24 hours apart. Tap and to step through frames one at a time.\nTap to play the animation, to pause it, and to reset to the current moment.\nTip Long pressing the map will also play the animation, and tapping it again will pause.\nShare (Record) Suntimes can share individual frames, or record all frames in an animation.\nUse : -\u003e Share to share the current map view.\nTo share an animation, long press to start recording. When finished, tap (where the play button was previously) to share the frames (as .zip).\nView Date Use : -\u003e View date to open other dialogs at the selected date/time.\nSet Background Suntimes can be configured to display a custom world map background.\nDownload Backgrounds Creating Backgrounds Use : -\u003e Center -\u003e Set Background to change the map’s background image.\n",
    "description": "",
    "tags": null,
    "title": "World Map",
    "uri": "/Suntimes/help/dialogs/worldmap/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Alarms",
    "content": "Suntimes -\u003e Settings -\u003e Alarms to reach alarm settings.\nAlarms Dismiss Challenge None (default) Easy Math. Solve single digit math problems to dismiss alarms. Reminder within Show a reminder notification within 12 hours of each alarm.\nSnooze for Snooze alarms for up to 120 minutes.\nSnooze limit Change the snooze limit to limit the number of times snooze can be used.\nTimeout after Alarms will timeout after sounding for too long without user intervention.\nQuick Notification Quick Notifications are displayed for less than a minute and then automatically dismissed (defaults to 30s).\nSounds Volumes Tap to opens system volume settings.\nGradually increase volume Gradually increase the alarm volume over time (defaults to 10s).\nShow all ringtones Show all system sounds during selection (ignore ringtone type). Miscellaneous Launcher Icon Show alarm clock icon in the app tray. Import Warning Imported alarms might revert some settings to defaults.\nDo not show this warning again. Boot Completed Alarms are automatically rescheduled a few minutes after reboot. This setting displays the last time alarms were rescheduled and the total amount of time that was consumed.\nTap this setting to trigger the boot completed routine manually.\nExperimental Power Off Alarm Wake the device from the power off state.\nThis feature is hardware dependent. Supported devices will display the name of the package responsible for implementing this feature. Unsupported devices will display null.\nFor supported devices, tapping this setting will request power off alarm permissions.\nNote This feature is incomplete and has been reported as non-functional. If you have supporting hardware, please considering testing and creating a detailed report.\n",
    "description": "",
    "tags": null,
    "title": "Alarm Settings",
    "uri": "/Suntimes/help/alarms/settings/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Data",
    "content": "Suntimes can provide data to plugins or add-on apps through a content-provider.\n",
    "description": "",
    "tags": "Advanced",
    "title": "Data Provider",
    "uri": "/Suntimes/help/more/data/dataprovider/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More",
    "content": "Suntimes displays astronomical event times in dialogs and widgets. It can also use events to schedule repeating alarms.\nDefault Events Custom Events Add, Edit Delete, Clear Export, Import Add-on Events Default Events sunrise, sunset, \u0026 solar noon civil, nautical, \u0026 astronomical twilight times blue hour, \u0026 golden hour moonrise, moonset, lunar noon, \u0026 lunar midnight major moon phases solstices, equinoxes, \u0026 cross-quarter days Custom Events Suntimes allows defining events with a user-defined angle. Similar to defaults, these custom events can be displayed in the app, in widgets, and used to schedule alarms.\nSuntimes -\u003e Settings -\u003e User Interface -\u003e Manage Events to configure custom events.\nAdd, Edit Tap to add an event. To edit an existing event, select an item, then tap edit. The edit dialog will be shown.\nEnter a label, angle, and (optional) color, then tap to save the event.\nThe button toggles visibility of the event on the main screen.\nDelete, Clear To remove an event, select an item, then tap delete.\nUse : -\u003e Clear to clear all events.\nNote When removing custom events, any alarms, notifications, or widgets using that event may no longer work and will need to be reconfigured.\nExport, Import Use : -\u003e Export to export events to file, and : -\u003e Import to import events from a previous export.\nTip It is also possible to export or import events by creating or restoring a backup.\nAdd-on Events Suntimes supports additional events through add-ons.\nFor example, Interval Midpoints can be used to schedule alarms between events (at calculated midpoints), and the Natural Hour app can be used to schedule alarms using roman time.\n",
    "description": "",
    "tags": null,
    "title": "Events",
    "uri": "/Suntimes/help/more/events/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes includes a variety of home screen widgets.\nManage Widgets Configure Widgets Widget Themes Time \u0026 Date Sun Moon Solstice, Equinox ",
    "description": "",
    "tags": null,
    "title": "Widgets",
    "uri": "/Suntimes/help/widgets/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Alarms",
    "content": " Battery Optimization Notification Settings Autostart Permission Battery Optimization Suntimes must be not optimized for alarms to work reliably.\nSettings -\u003e Apps -\u003e Suntimes -\u003e Battery -\u003e Unrestricted\nNote On most devices the optimized setting may delay alarms up to 15 minutes. Other devices may restrict alarms without special configuration.\nThe following devices are affected: Asus, Blackview, Huawei, LENOVO, Meizu, OnePlus, OPPO, realme, Samsung, Sony, Unihertz, Vivo, WIKO, and Xiaomi. Check https://dontkillmyapp.com/ for solutions.\nNotification Settings Notifications must be enabled for alarms to display correctly.\nSettings -\u003e Apps -\u003e Suntimes -\u003e Notifications -\u003e Show Notifications\nAutostart Permission Suntimes must be granted autostart permissions or alarms may fail to work after a reboot (Xiaomi devices only).\nSecurity app \u003e Permissions \u003e Auto-start\n",
    "description": "",
    "tags": null,
    "title": "Alarm Recommendations",
    "uri": "/Suntimes/help/alarms/recommendations/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": "Suntimes can display information in the system tray as a quick settings tile.\nAvailable Tiles Tile Dialog Tile Action Manage Tiles Available Tiles Clock Tile shows the current time in a given time zone or time standard. Next Event Tile shows the time of the next twilight event. Tile Dialog From the system tray, tap on a tile to open the tile dialog to display more information.\nTile Action From the tile dialog, tap the action button (bottom right). The default action is to open the main screen.\nTip The action can be assigned from the tile configuration screen.\n",
    "description": "",
    "tags": null,
    "title": "Tiles",
    "uri": "/Suntimes/help/tiles/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": " Natural Hour\nA 24-hour clock \u0026 Roman timekeeping add-on app.",
    "description": "",
    "tags": null,
    "title": "Add-ons",
    "uri": "/Suntimes/help/addons/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More",
    "content": "Suntimes is configured using location data and maintains a private collection of places.\nManage Places Place Settings ",
    "description": "",
    "tags": null,
    "title": "Places",
    "uri": "/Suntimes/help/more/places/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help",
    "content": " Settings Events Places Actions Data Documentation ",
    "description": "",
    "tags": null,
    "title": "More",
    "uri": "/Suntimes/help/more/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More",
    "content": "Suntimes can perform user-defined actions when a widget is clicked, or when an alarm or notification is shown or dismissed.\nAdd Actions ",
    "description": "",
    "tags": null,
    "title": "Actions",
    "uri": "/Suntimes/help/more/actions/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More",
    "content": "Suntimes performs all calculations locally on the device.\nThis data is displayed by dialogs in the app, by home screen widgets, and can used to schedule alarms and notifications. Data is also available to plugins and add-ons apps through a provider.\nData Substitutions Data Sources Data Provider ",
    "description": "",
    "tags": null,
    "title": "Data",
    "uri": "/Suntimes/help/more/data/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More",
    "content": "This user manual (and other documentation) is maintained at https://github.com/forrestguice/Suntimes/issues.\nYou can help improve it by reporting typos, dead links, and other errors.\nMirrors This user manual is published to:\nhttps://forrestguice.codeberg.page/Suntimes/ https://forrestguice.github.io/Suntimes/ Versions v0.1.1, 2025-06-03; minor updates; adds pages for Natural Hour and Suntimes Calendars. v0.1.0, 2024-05-01, written for Suntimes v0.15.14. ",
    "description": "",
    "tags": null,
    "title": "Documentation",
    "uri": "/Suntimes/help/more/documentation/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e Add-ons",
    "content": " Add-ons are separate applications that integrate with or extend Suntimes through the use of public interfaces. Add-ons may start intents, use content-providers, or implement content-provider contracts.\nAdd-ons must declare the READ_CALCULATOR permission. This permission is granted during installation.\nSee https://github.com/forrestguice/SuntimesWidget/wiki/Interfaces for a list of interfaces available to add-ons (and other applications).\n",
    "description": "",
    "tags": "Advanced",
    "title": "Interfaces",
    "uri": "/Suntimes/help/addons/interfaces/index.html"
  },
  {
    "breadcrumb": "",
    "content": "\nSuntimes is an Android app that displays sunrise and sunset, twilight, blue and golden hour, solstices and equinoxes, moonrise and moonset, moon phases and illumination. It includes an alarm clock, and a large collection of widgets and add-ons.\nNatural Hour\nA 24-hour clock \u0026 Roman timekeeping add-on app.",
    "description": "",
    "tags": null,
    "title": "Suntimes",
    "uri": "/Suntimes/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Tags",
    "content": "",
    "description": "",
    "tags": null,
    "title": "Tag :: Advanced",
    "uri": "/Suntimes/tags/advanced/index.html"
  },
  {
    "breadcrumb": "Suntimes",
    "content": "",
    "description": "",
    "tags": null,
    "title": "Tags",
    "uri": "/Suntimes/tags/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Help \u003e More \u003e Actions",
    "content": " Common Intents Intents and Intent Filters (docs) To add an action… create an Intent by declaring:\nIntent (leave fields empty for an implicit intent) Action An action string. e.g. android.intent.action.ACTION_VIEW Class A fully qualified class name (case-sensitive). This must be a complete definition that includes both the package and class name. The class is required for explicit intents - leave it blank to allow the system to decide which class to launch. e.g net.osmand.plus.activities.MapActivity Data A URI that contains or points to attached data. e.g. geo:30,31. Limited %substitutions are supported. e.g. geo:%lat,%lon Mime The mime type of attached data (if applicable). Leave blank for most types of data. Extras An \u0026 delimited string containing key-value pairs. Values may be Strings, int, long, double, float, or boolean. Limited %substitutions are supported. e.g. key1=\"some string\" \u0026 key2=1 \u0026 key3=1L \u0026 key4=1D \u0026 key5=1F \u0026 key6=true \u0026 key7=%dm An app that displays package info is useful for discovering Activities, which can then be launched with an explicit intent.\n",
    "description": "",
    "tags": "advanced",
    "title": "Add Actions",
    "uri": "/Suntimes/help/more/actions/addaction/index.html"
  },
  {
    "breadcrumb": "Suntimes \u003e Tags",
    "content": "",
    "description": "",
    "tags": null,
    "title": "Tag :: Add-On",
    "uri": "/Suntimes/tags/add-on/index.html"
  },
  {
    "breadcrumb": "Suntimes",
    "content": "",
    "description": "",
    "tags": null,
    "title": "Categories",
    "uri": "/Suntimes/categories/index.html"
  }
]
