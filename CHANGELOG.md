### ~

* adds widget option to "use the app location"; defaults false.
* adds widget option to override 12h/24h time format setting.
* adds widget option to "fill entire cell" or "align base" to top-left, right, bottom, etc.
* adds widget option to "scale text and icons"; extra large widgets.
* renames widget option "grow with available space" to "swap layouts to fill space"; default changed to false; adds help text.
* enhances widget layout selector to show a themed preview.
* enhances widget theme selector to show background preview.
* enhances widget time mode selector (color tabs).
* reorganizes widget options.

### v0.13.18 (2022-01-14)
* changes the solstice/equinox card to always include seconds when expanded (#533).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#531, #532 by James Liu).
* fixes bug "rectangular world map widget unavailable" on some devices (#535).
* fixes bug "LocationListTask leaks database cursor" (#536).
* fixes bug "alarm dialog fails to consume touch events" (#529).
* fixes bug "invalid offsets are retained when editing an existing alarm's event" (#530).

### v0.13.17 (2022-01-02)
* adds translation to Simplified Chinese (zh_CN) (contributed by James Liu) (#527).
* updates translation to Traditional Chinese (zh_TW) (#527).
* fixes alarm notifications to include the 'offset' text (more descriptive); e.g. "5m before sunset" vs "sunset" (#522).
* fixes crash when loading AlarmCreateDialog with invalid settings (handle unknown event values).

### v0.13.16 (2021-11-29)
* adds translation to Czech (cs) (contributed by utaxiu) (#520).
* fixes bug where CalculatorProvider fails to apply altitude refinements.

### v0.13.15 (2021-11-15)
* updates translation to Norwegian (nb) (#515 by FTno).
* updates translations to Polish (pl) and Esperanto (eo) (#514 by Verdulo).

### v0.13.14 (2021-10-26)
* adds "Show Header Icon" and "Show Header Text" options to User Interface settings (#509, #511).
* fixes bug "twilight duration fails to switch to the next event" (#508).
* fixes bug "unable to select default widget themes" when overriding the app theme (#510).
* fixes bug in "1x1 sunrise/sunset only" widgets where the time displayed is for the wrong day (off-by-one).
* fixes app crash when table headers are clicked (but the corresponding rows have been hidden).

### v0.13.13 (2021-09-09)
* fixes bug "1x1 moon widget uses imperial units while the app metric" (#506).

### v0.13.12 (2021-06-20)
* updates translation to German (de) (#503 by wolkenschieber).

### v0.13.11 (2021-06-08)
* updates translation to Italian (it) (contributed by GiovaGa) (#499).
* updates translation to French (fr) (contributed by ldmpub) (#501).

### v0.13.10 (2021-05-05)
* adds 'import places'; import from a previously exported csv (#419).
* changes moon illumination message; "% illuminated" to "% @ <lunarNoon>" (#384).
* miscellaneous theme fixes and improvements (#492):
 ** app now applies theme text sizes.
 ** app now hides default widget themes from list (recommended for widgets only).
 ** app now shows a warning when the theme is changed; "app restart may be needed for changes to take effect".
 ** theme editor now allows choosing fractional sp values.
 ** theme editor now changes previews when map/graph colors are modified.
* fixes bug "current location title not translated" (#467).
* updates translation to Polish and Esperanto (eo, pl) (#491 by Verdulo).

### v0.13.9 (2021-03-03)
* updates translation to Catalan (ca) and Spanish (es) (#478, #479 by gnusuari0).
* updates translation to Dutch (nl) (#486, #487 by Joppla).
* updates translation to Norwegian (nb) (#488 by FTno).

### v0.13.8 (2021-02-09)
* adds translation to Dutch (nl) (contributed by Joppla) (#474, #480).
* updates translation to Catalan (ca) and Spanish (es) (contributed by gnusuari0) (#478, #479).
* improves alarm sound selection; adds menu: "No Sound", "Ringtone", "Audio File".
* removes READ_EXTERNAL_STORAGE permission (api≥19) (#473); alarm sounds now use persistent URI permissions instead. [PERMISSION]

### v0.13.7 (2021-01-26)
* fixes bug "alarm vibration stops after screen is off" (#470).
* fixes broken build caused by failing `colorpicker` dependency.
* updates translation to Polish and Esperanto (eo, pl) (#468 by Verdulo).

### v0.13.6 (2020-12-31)
* adds support for sidereal time (GMST, LMST) (#463).
* adds "alarm list" button to the alarm dialog (#455); shortcut to Suntimes Alarms.
* adds "SHOW_CARD" action to main activity; allows other apps to scroll to a given date.
* fixes widget click action so that it also triggers an update (#459).
* misc time zone dialog improvements.

### v0.13.5 (2020-12-04)
* adds translation to Russian (ru) (contributed by Ruslan Chintsov) (#458).
* updates translation to Brazilian Portuguese (pt-br) (#453 by efraletti).
* updates translation to Polish and Esperanto (eo, pl) (#460 by Verdulo).
* updates translation to Norwegian (nb) (#461 by FTno).
* fixes bug "NotificationService keeps running in background" (#456, #323).

### v0.13.4 (2020-11-16)
* updates translation to Brazilian Portuguese (pt-br) (#450 by naoliv).
* updates translation to Norwegian (nb) (#447 by FTno).
* updates translation to Polish and Esperanto (eo, pl) (#446 by Verdulo).

### v0.13.3 (2020-11-03)
* fixes crash in Manage Places when copying or editing places (#443).
* adds an object height slider (shadow length) to sun position dialog (#189, #442).
* updates translation to Brazilian Portuguese (pt-br) (#441 by naoliv).

### v0.13.2 (2020-10-26)
* adds support for overriding location when creating alarms via Intent.
* adds a hex color field to the rgb color picker.
* fixes bug where ColorDialog sliders sometimes start with the wrong value.
* fixes crash in AlarmClockActivity when notifications are disabled (#437).
* fixes bug "incorrect future and past number of days, when DST starts or ends" (#436).
* fixes spannable icons (too small / unreadable) to be consistent across devices.
* improves organization of "User Interface" settings (#434).
* exports the WidgetListActivity, ActionListActivity, and SettingsActivity; permits access to third-party apps with the `suntimes.permission.READ_CALCULATOR` permission.
* enhances the WidgetListActivity to show add-on widgets (and allow reconfigure).
* new permission: The app now uses `suntimes.permission.READ_CALCULATOR` (in addition to declaring it). Add-on apps may now use this permission to protect their own interfaces. [PERMISSION]

### v0.13.1 (2020-10-03)
* minor tweaks to app help topics (#432).
* fixes bug where deleted locations would reappear ("no way to remove default location" #430).
* updates translation to Polish and Esperanto (eo, pl) (#433 Verdulo).
* updates translation to Norwegian (nb) (#429 by FTno).

### v0.13.0 (2020-09-14)
* adds "lunar noon" and "lunar midnight" to notes and alarms.
* improves SuntimesAlarms support for polar regions; it is now possible to schedule alarms for infrequently occurring twilight events.
* adds PlacesActivity; adds support for searching places; adds support for deleting individual places (#419); adds undo clear/delete places; extends place selection to add-on apps.
* improves ColorDialog; now supports paging between different pickers; adds alternate quadflask color picker; adds simple RGB color picker; adds "recent colors" palette; refactored to BottomSheetDialog.
* adds ColorActivity; extends color selection to add-on apps.
* extends "On Tap" functionality to support user-defined actions; adds UI for adding, editing, and selecting user-defined actions.
* adds SuntimesAlarms actions; trigger a user-defined action when an alarm sounds or is dismissed.
* improves SuntimesAlarms UI; replaces AlarmList UI; adds AlarmEditActivity.
* (SuntimesAlarms) improves alarm list to allow rescheduling already active alarms (#355).
* (SuntimesAlarms) improves visibility of the "clock time", "apparent solar time", and "local mean time" alarm modes.
* (SuntimesAlarms) improves visibility of the alarm offset feature (#416).
* (SuntimesAlarms) improves alarm offset UI to show the actual alarm time (#424);
* (SuntimesAlarms) adds support for sorting the alarm list (#413); by time, by creation date.
* (SuntimesAlarms) adds support for undo delete alarms.
* (SuntimesAlarms) alarm database version 2 -> 3; adds columns "actionID0", "actionID1.
* adds %dm to %substitutions; widget update time in milliseconds.
* tweaks 2x1 sun widget layout (#423) to center the time.
* fixes adaptive icon shape.
* updates translation to Chinese (zh-tw) (#380).

### v0.12.11 (2020-06-16)
* fixes bug "sun position widget does not render midnight sun" (#421).
* fixes app crash when using fallback to "last location" (#420).

### v0.12.10 (2020-05-20)
* fixes bug "solar time alarms are offset by several minutes" (#414).
* updates translation to German (de) (#412 by xnumad).

### v0.12.9 (2020-05-10)
* fixes bug "Widget Title Substitution %lel uses meters only" (#410).
* fixes NullPointerException when refreshing location (#408).

### v0.12.8 (2020-04-30)
* adds ability to set an alarm in solar time (#403); adds `timezone` field to alarm item database; increments database version (`1` -> `2`).
* adds field to CalculatorProvider; `COLUMN_CONFIG_APP_THEME_OVERRIDE`; increments CalculatorProvider versionCode (`3` -> `4`).
* adds a ContentProvider (SuntimesThemeProvider) that provides access to user-defined themes.
* exports the ThemeList activity; permits access to third-party apps with the `suntimes.permission.READ_CALCULATOR` permission.
* fixes bug where NotificationService continues running after scheduling a notification or distant alarm.

### v0.12.7 (2020-04-12)
* adds "gradually increase volume" option to Suntimes Alarms (#396).
* fixes bug where CalculatorProvider fails to apply the selected time zone (#394).
* updates translation to Brazilian Portuguese (pt-br) (#400 by efraletti).
* updates translations to Polish (pl) and Esperanto (eo) (#402 by Verdulo).

### v0.12.6 (2020-03-16)
* adds fields to CalculatorProvider that provide access to general app configuration (timeIs24, showSeconds, showHours, showWeeks, useElevation, showWarnings, verboseTalkback, showFields, lengthUnits, and objectHeight).
* fixes bug where CalculatorProvider would fail to honor changes to the calculator selection or "use elevation" option.
* increments CalculatorProvider versionCode (`2` to `3`); note: the key used to access the providerVersionCode has also been changed (fixes typo).

### v0.12.5 (2020-02-08)
* adds a "dismiss" button to the reminder notification that allows repeating alarms to be dismissed early (reschedule+1); reveals the reminder notification for api21+ (#387).
* fixes the appearance of ActionModes displayed within BottomSheetDialogFragment.
* fixes bug where CalculatorProvider would sometimes return uninitialized defaults.
* no longer reschedules alarms in response to TIME_SET broadcast.

### v0.12.4 (2019-12-23)
* fixes app crash (time zone dialog) (#376).
* fixes bug where updating "current location" blocks dialogs from loading; potentially long lived AsyncTask now run in parallel with shorter lived tasks (THREAD_POOL_EXECUTOR) (#376).
* updates build; Android gradle plugin version updated to `com.android.tools.build:gradle:3.1.2`, gradle wrapper to `gradle-4.4`, and buildToolsVersion to `27.0.3`.

### v0.12.3 (2019-12-03)
* fixes bug "can't get location while gps is working and has a fix" (api17+) (#373).
* adds gps "recent max age" values "none" and "any" (ignores gps time when getting fix), and an option to fallback to the last location.
* improves the accuracy of gps results (applies a simple Kalman filter).
* updates translation to Norwegian (nb) (#374 by FTno).

### v0.12.2 (2019-11-04)
* fixes bug "Notifications don't respect Do Not Disturb" (#369).
* updates translation to Norwegian (nb) (#370 by FTno).

### v0.12.1 (2019-10-15)
* fixes app crash (moon phase alarms) (#365).
* updates translation to Norwegian (nb) (#366 by FTno).
* updates translations to Polish (pl) and Esperanto (eo) (#367 by Verdulo).

### v0.12.0 (2019-10-07)
* enhances the main table to allow scrolling future and past days (#173, #352).
* enhances the moon dialog to allow scrolling future and past months.
* enhances the solstice dialog to allow scrolling future and past years.
* adds moon apogee, perigee, and distance information to the moon dialog (#296).
* adds supermoon and micromoon labels for upcoming new and full moons (#296).
* adds 1x1 moon apogee/perigee widget (#296).
* adds 1x1 moon position widgets; azimuth & elevation, right ascension & declination, or current distance.
* enhances the World Map dialog to animate over time; play/pause or step forward/back to show changes over a span of hours or days (#284).
* adds world map projection "Polar [south]" (#284).
* adds 3x3 sun position widget; Polar [north], Polar [south] map projections (#284).
* enhances the Blue Marble map; the shadow area now displays "Earth's City Lights" imagery.
* enhances the World Map dialog; adds "major latitudes" and "location" options.
* enhances the World Map share action; using "share" while playing saves the animation to zip.
* adds events to Suntimes Alarms; now includes the moon phases ("full moon alarm"; #327) and solstices and equinoxes.
* replaces multiple "floating action buttons" with "floating action menu" UI.
* replaces most "alert dialogs" with "bottom sheet" UI.
* adds animations when transitioning between related activities.
* fixes the time zone dialog so that it retains user-defined settings when toggling between modes.
* improves the Widget Config activities (ActionBar).
* improves the About Dialog (adds tabbed activity).
* updates dependency (Time4A 4.4.2-2019c).

### v0.11.10 (2019-09-03)
* fixes app layout issues for Brazilian Portuguese (pt-br) and Norwegian (nb) translations (#356).
* fixes bug "date dialog fails to respect timezone setting" (#358).
* fixes bug "note selection lost on orientation change" (#359).
* updates translation to Brazilian Portuguese (pt-br) (#357 by naoliv).

### v0.11.9 (2019-08-22)
* fixes app crash when supplied with invalid data (bad URI, alarmID, or widgetID) ("Intent resolution bugs"; #353).
* updates translation to Brazilian Portuguese (pt-br) (#350 by Neto Silva).

### v0.11.8 (2019-07-30)
* fixes bug "half hour and 45-minute time zones are listed incorrectly" (#346).
* fixes bug "content provider uses a stale configuration" (#347).

### v0.11.7 (2019-07-08)
* updates translations to Spanish (es-es) and Catalan (ca) (#340, #343 by Raulvo).

### v0.11.6 (2019-06-24)
* fixes bug "Suntimes Alarms uses elevation even if unchecked" (#336).
* fixes bug "worldmap dialog fails to apply themed foreground color" (#337).
* fixes bug "unable to dismiss alarm when notifications are disabled" (#333); now falls back to directly triggering the fullscreen activity.
* adds a "Notifications" warning to the AlarmClock activity that is shown if notifications are disabled. Notifications are required for alarms to display correctly (#333).
* adds a "Notifications" preference to the Alarm settings. This preference warns when notifications are disabled, or configured to not show on the lock screen (#332, #333).
* adds a "Volumes" preference to the Alarm settings (navigates to system Sound settings).
* optimizes basemaps and preview pngs; size reduced by ~50% using pngquant.
* updates translations to Polish (pl) and Esperanto (eo) (#330, #339 by Verdulo).

### v0.11.5 (2019-05-31)
* adds support for playing alarm sounds from the file system (mp3, ogg, etc). [Selecting files requires a file manager app with support for ringtone selection.]
* fixes bug "alarm sound fails to play from external storage" (#326); alarm notifications will fallback to the default ringtone if the selected sound cannot be played.
* new permission: READ_EXTERNAL_STORAGE. This permission is needed to play sounds located on the SD card (#326). [PERMISSION]
* improves language resolution for Spanish locales (#147).
* improves the About Dialog (better translation credits).

### v0.11.4 (2019-05-07)
* fixes bug "NotificationService is always running" (#323, #324).
* fixes bug "AlarmClockActivity started multiple times" (#325, #324).
* removes the "location mode" selector from widget configuration (widgets only support the "user-defined" mode) (#10).
* updates translation to Brazilian Portuguese (pt-br) (#320 by Neto Silva).

### v0.11.3 (2019-04-17)
* fixes crash when opening general settings (Esperanto) (#315).
* adds "rise/set order" help button to the widget configuration activity.
* misc. layout and icon changes (Widget List, About).
* updates translations to Polish (pl) and Esperanto (eo) (#317 by Verdulo).
* updates translation to Brazilian Portuguese (pt-br) (#319 by Neto Silva).

### v0.11.2 (2019-04-08)
* adds an option to disable the alarm clock launcher icon (#305).
* adds translation to Brazilian Portuguese (pt-br) (contributed by Neto Silva) (#304).
* updates translations to Polish (pl) and Esperanto (eo) (#307 by Verdulo).
* misc. widget layout fixes. Widgets now ignore the "large text" accessibility setting (#306). Use a custom theme to increase text size.
* fixes app crash (world map dialog)  (#309).

### v0.11.1 (2019-03-28)
* updates translations to Spanish (es-es) and Catalan (ca) (#301 by Raulvo).
* updates translations to Polish (pl) and Esperanto (eo) (#302 by Verdulo).

### v0.11.0 (2019-03-12)
* adds "Suntimes Alarms", an Alarm Clock (#140, #250, #261) with support for daily repeating alarms and notifications.
* adds a clock widget (#154, #260); displays solar time (Local Mean Time, Apparent Solar Time), or the time in a given timezone.
* adds a "share" action to the World Map dialog (exports to png) (#284).
* changes the default solar time mode to "Apparent Solar Time"; adds a help button to solar time mode selector.
* new permission: BOOT_COMPLETED. This permission is needed to restore active alarms after reboot. [PERMISSION]
* new permission: VIBRATE. This permission is used by alarm notifications. [PERMISSION]
* misc style and layout fixes.

### v0.10.3 (2019-01-31)
* adds app shortcuts (Android 7.1+); a shortcut to the Widget List, a shortcut to the Theme Editor (#288).
* reveals previously hidden azimuthal map projection in the World Map dialog (#284); layout issues for this projection continue to exist for smaller screens.
* fixes a CalculatorProvider bug where sun/moon queries returned the wrong data type (Calendar obj vs long timestamp).

### v0.10.2 (2019-01-10)
* fixes bug "'get location' does not honor the 'units of length' pref" (#290).
* improves the accuracy of the apparent solar time calculation (#291).
* updates translation to Basque (eu) (#294 by beriain).

### v0.10.1 (2018-12-23)
* fixes bug "sun/moon circles are difficult to see (too small)" (#286) on lightmap and worldmap widgets.
* updates translation to Norwegian (nb) (#285 by FTno).
* updates dependency (Time4A 4.2-2018g).

### v0.10.0 (2018-12-09)
* adds support for themes to the app; it is now possible to customize the app's appearance using widget themes (#264, #275).
* adds "order" option to sun and moon widgets; "display tomorrow's sunrise once sunset time has passed" (#190).
* adds "distance units" (imperial, metric) to General Settings; display distances (altitude/elevation, shadow length) using meters or feet (#273).
* adds "shadow length" to the Sun Position dialog (#189, #273), and "object height" to General Settings.
* adds support for third-party apps and widgets through a ContentProvider (#266, #276); https://github.com/forrestguice/SuntimesWidget/wiki/Interfaces.
* removes Calendar permissions (READ_CALENDAR, WRITE_CALENDAR, READ_SYNC_STATS, WRITE_SYNC_SETTINGS);
* removes Calendar Integration; this feature is now available as a separate apk (#239, #266, #277); https://github.com/forrestguice/SuntimesCalendars.
* extends the widget update strategy to support per widget updates; the sun and moon widgets may now trigger an update shortly after each event (in addition to the daily update at midnight).
* adds dst label to timezone dialog; displayed when selected timezone is using daylight saving time (#274).
* adds eot label to timezone dialog; displayed for apparent solar time (#274).
* adds "import themes" and "share themes" (export) to the theme list activity (#275).
* adds "action color" to themes (button press color) (#275).
* fixes cropped text in theme config activity (#254); misc layout improvements.
* updates the legacy icon to match the appearance of the adaptive icon (#272).
* updates translation to Polish and Esperanto (eo, pl) (#282 by Verdulo).
* updates build; Android gradle plugin version bumped to `com.android.tools.build:gradle:3.0.0` (and gradle wrapper to `gradle-4.1`).
* updates dependency (Time4A 4.1-2018g).

### v0.9.5 (2018-11-12)
* modifies the default colors to improve contrast and readability (#247, #264, #268).
* fixes bug "language selectors fails for some languages" (#262).
* fixes empty widgetlist; an oversized label and icon are now displayed when the list is empty.
* fixes worldmap bug where the sun/moon positions are drawn despite sunlight/moonlight options toggled off.
* fixes settings activity iconography (unique icons for each header); the previous patch only fixed this for older Android versions where the icon attribute is completely ignored.
* updates translation to Basque (eu) (#271 by beriain).
* updates translation to Norwegian (nb) (#270 by FTno).
* updates translation to Polish and Esperanto (eo, pl) (#263, #267 by Verdulo).

### v0.9.4 (2018-10-28)
* fixes bug "widgets missing" (#258); installLocation set to internalOnly.
* fixes appearance of main table for locales with header text shorter than event times; sunset column now has minWidth.
* fixes appearance of snackbar warnings; now styled by theme.
* fixes readability of snackbar warnings for locales with long action button text.
* fixes Settings Activity iconography; unique icons for each header.
* updates translations to Spanish (es-es) and Catalan (ca) (#255 by Raulvo).
* updates translation to Norwegian (nb) (#253 by FTno).
* adds translation to Traditional Chinese (zh-tw) (contributed by ft42) (#252).

### v0.9.3 (2018-10-10)
* adds help to the "theme list" activity.
* adds translation to Italian (it) (contributed by Matteo Caoduro) (#249).

### v0.9.2 (2018-09-12)
* adds altitude ui to the datasource card; toggles altitude (hidden when datasource lacks support) (#245).
* fixes app crash when location has altitude greater equal 11000m (validation off-by-one) (#243).

### v0.9.1 (2018-08-30)
* fixes bug "export places; commas in place names break csv output bug" (#240).
* fixes bug "momentary hang/pause when adding or reconfiguring sunposition widgets (3x1 and 3x2)".
* adds permission explanations to fastlane app description.
* adds runtime permission explanations (a dialog displayed prior to each permission request).
* adds privacy link to about dialog (links to https://github.com/forrestguice/SuntimesWidget/wiki/Privacy); added privacy statement to readme.
* updates translation to Polish and Esperanto (eo, pl) (#238, #241 by Verdulo).
* updates translation to Norwegian (nb) (#236 by FTno).

### v0.9.0 (2018-08-14)
* changes default sun data source to time4a-time4j (supporting altitude based refinements).
* changes default 'en' location to New York City, and default 'en-US' location to Phoenix.
* enhances the data source selector; now tags the default source, and sources loaded via plugin.
* enhances data sources (plugins); support for loading external sources (not included with app) (#229).
* adds elevation to all default locations; default 'en' location changed to New York City, default 'en-US' location changed to Phoenix.  
* adds elevation UI to Location settings, main ActionBar, and widget title substitutions.
* adds app pref "Use Elevation"; apply altitude based refinements; defaults true.
* adds app pref "On Date Long Press"; defaults to "Show Calendar".
* adds permissions READ_CALENDAR, WRITE_CALENDAR; needed to interact w/ Calendar app (add/remove events in custom calendars).
* adds permissions READ_SYNC_STATS, WRITE_SYNC_SETTINGS; needed to provide custom calendars (add/remove calendars via SyncAdapter).
* adds a SyncAdapter (LOCAL account) to provide the Calendar app with custom calendars.
* adds options to toggle visibility of twilight times displayed by the app (hide fields).   
* refactors widgetlist activity to use ActionBar (#230).
* adds 3x1 and 3x2 SunPosition previews to theme editor.
* adds 3x2 SunPosition widget showing world map.
* adds world map dialog to app; shows sunlight (day/night) and moonlight over an equirectangular map.
* adds theme "Dark (translucent)"; default theme with semitransparent widget background.
* adds to theming; custom background option (simple background color supporting transparency).
* updates translation to Polish and Esperanto (eo, pl) (#235 by Verdulo).
* updates translation to Norwegian (nb) (#224 by FTno).
* updates dependency (Time4A 3.44.2-2018b).

### v0.8.6 (2018-07-05)
* updates translation to French (fr) (#220 by Aloha68).
* updates translation to Norwegian (nb) (#221 by FTno).
* updates translations to Polish and Esperanto (eo, pl) (#217 by Verdulo).

### v0.8.5 (2018-06-18)
* add gps pref "Passive Location"; use the passive location provider (use a separate app to manage location updates).
* fixes bug where the "GPS is disabled. Enable it?" dialog is not shown.
* updates translation to Norwegian (nb) (#214, #215 by FTno).

### v0.8.4 (2018-06-07)
* fixes polar regions usable hours bug (#209).
* fixes app crash on polar regions when no rise/set events (#212).
* fixes lightmap polar regions bugs where wrong color is shown during perpetual day/night (#209).
* fixes lightmap bug where durations are drawn incorrectly (near boundaries up to timezone offset).
* fixes lat/lon input validation bug (#211).
* fixes date format localization bug (#210).

### v0.8.3 (2018-05-31)
* adds translation to Norwegian (nb) (contributed by FTno) (#206).
* removes unused option "show seconds" from sun position widgets.

### v0.8.2 (2018-05-11)
* adds to theming; graph colors; day, civil, nautical, astronomical, and night colors.
* fixes bug where "export places" creates an empty file (#204).
* enhances calculator fallback behavior; e.g. SuntimesMoonData now overrides the default (sunrisesunsetlib -> Time4A4J).
* fixes bug where solstice/equinox card fails to hide when calculator lacks support (and fallback was supplied).
* fixes app crash (MoonDialog) if calculator failed to load (and fallback was supplied) (#198).
* fixes app crash (General Settings) when initializing defaults (api26) (#198).
* fixes bug where user-defined language fails to override locale (api26) (#197).


### v0.8.1 (2018-04-26)
* misc layout changes (improvements for locales w/ long strings).
* fixes column alignment of solstice/equinox in main table; now aligns w/ the sunrise column.
* fixes column alignment of moonrise/moonset in main table; now aligns w/ the sunrise column.
* changes label alignment of 3x1 SunPosition widget; now centered (#188).
* adds enhancements to the language selector; now sorted alphabetically, now displays name of language in that language (and displays localized language name in parenthesis).
* adds translation to Basque (eu) (contributed by beriain) (#193, #194).
* updates translation to German (de) (contributed by Wolkenschieber) (#191, #192).

### v0.8.0 (2018-04-10)
* fixes app crash (in ThemeConfigActivity) when the app is configured to use a calculator that lacks support for the moon feature.
* adds click behavior to main table headers; clicking sunrise/sunset highlights next sunrise/sunset.
* changes the default alarm label format (label now includes shortDate).
* adds moonrise and moonset to the AlarmDialog.
* adds moon dialog to app; shows major phases, rising/setting times, rising/setting position, current position, phase, and illumination (current).
* adds moon info to app (main table); rise/set, phase, and illumination (at lunar noon) (#52, #183).
* adds "moon data source" to general settings.
* fixes bug where solstice dialog is not correctly initialized (when "show solstice" option is false).
* fixes widget update alarms (more precise); sun and moon widgets update at midnight, solstice widgets update every 3hr, and sun position widgets update every 5min.
* adds 3x1 sun position widget; shows lightmap (and optional azimuth and elevation labels) (#107).
* adds 1x1 sun position widget; shows right ascension and declination.
* adds 1x1 sun position widget; shows azimuth and elevation angles (#169).
* adds sun azimuth and elevation angles to lightmap dialog (#169).
* adds "sun position" overflow menu item; shows lightmap dialog.
* updates translations (eo, pl) (#184, #186 by Verdulo).

### v0.7.4 (2018-03-26)
* adds "restore defaults" button to "On Tap: Launch App" help dialog.
* fixes widget config edittext auto-correct behavior (now disabled for title text, launch app).
* fixes app crash when location supplied by GPS has negative altitude (some Samsung devices).
* adds moontimes icon (displayed by moon widgets in widget list).
* simplifies datepicker (removes mode spinner) (#173).
* app automatically restarts on day/night change (when using nightmode theme).
* app automatically restarts on theme change or locale change.
* app automatically advances to next note (when card is unswapped) (registers/unregisters an alarm for next event).
* app automatically updates on date change (registers/unregisters an alarm for midnight).
* fixes bug where the date has advanced to the next day but the displayed times have not (after midnight up to difference between local time and timezone).
* fixes bug where the formatted date is off by a day (before midnight up to difference between local time and timezone).
* updates translations (eo, pl) (#180 by Verdulo).

### v0.7.3 (2018-03-16)
* reorders user interfaces prefs (organized into categories).
* fixes talkback accessibility issues; snackbar warning messages not announced, datetext and timezone fields announcing unused tags, timezone dialog announcing previous state on mode change.
* fixes app crash (api14, api15) when accessibility settings are disabled.
* fixes app crash when location name contains special characters (#177).

### v0.7.2 (2018-03-11)
* adds clock tap action: "Set Time Zone"; timezone label now clickable (launches TimeZoneDialog).
* default date tap action changed to "Set Date" (#173).
* enhances the "Set Date" dialog; adds "Today" button, quicker date selection (#173).
* better support for talkback (announceForAccessibility api16 and under).
* adds widget option "show time (with date)"; include the time when displaying dates.
* adds widget option "show hours"; include hours/minutes in time spans greater than a day.
* adds widget title substitution; %dt and %dT are for time (of last widget update).
* adds widget title substitution; %id is for appWidgetID (for debug purposes).
* adds widget option "show labels"; show/hide extra labels.
* updates dependency (Time4A 3.40-2018b).
* updates url: AboutDialog now links https://forrestguice.github.io/SuntimesWidget/
* updates translations (eo, pl) (#171, #172, #175 by Verdulo); adds translated fastlane metadata.

### v0.7.1 (2018-02-28)
* fixes bug #120 (widget icons don't use theme icons); now works for all api versions.
* enhances the widget list; adds appWidgetID label to the list and widget (re)configure activity.
* adds widget ontap action; "update widget" to manually trigger widget updates.
* fixes (workaround) missing/deleted update alarms; opening the widget list now reschedules alarms (that may be missing / deleted (e.g. forced stop)).
* fixes widget update when a theme is modified; all widgets sharing that theme are updated.
* adds "themes" menu item to widgetlist activity; launches the theme editor (w/out navigating through widget config).
* adds "about" menu item to theme selector activity.
* fixes widget list scroll; state preserved on orientation change.
* fixes theme selector scroll; automatically scroll to the selected item.
* fixes theme selector ordering; sort items alphabetically, grid selector shows defaults first.
* enhances the theme selector; grid selector item layout shows more colors.
* enhances the theme selector; toggle the preview background (shows home screen wallpaper).
* enhances the theme editor; flip between multiple previews (adds moon widget previews).
* adds to theming; moon phase colors; the new moon, waxing, waning, and full moon colors are now themeable.
* adds to theming; "bold" title option, "bold" time option.

### v0.7.0 (2018-02-17)
* adds a 3x1 moon widget (showing major phases) (#52).
* adds a 2x1 moon widget (showing rise/set + phase + illumination) (#52).
* adds several 1x1 moon widgets (showing rise/set times, current phase, next phase, or illumination) (#52).
* adds twilight durations to the lightmap dialog.
* adds option to "places settings" to generate a list of world cities (by scanning locale defaults).
* adds "show weeks" option to app and solstice widget (#153).
* adds "show blue hour" option to app (#127).
* adds "show golden hour" option to app (#127).
* adds golden hour, blue hour (8deg), and blue hour (4deg) to rise/set times (#127).
* fixes en localization; e.g. "Fall" is better known as "Autumn" (#159), "color" vs "colour", etc.
* updates translations (eo, pl) (#160, #162, #163 by Verdulo).

### v0.6.2 (2018-02-03)
* adds option "verbose accessibility"; better support for TalkBack.
* misc usability fixes (MainActivity, AlarmDialog); better support for TalkBack.
* misc layout fixes (improved accessibility); better support for "large text".
* fixes bug "solstice/equinox dates not localized" (#146).
* adds "adaptive" launcher icon (used by api26+).
* updates translations (eo, pl) (#148, #149 by Verdulo).

### v0.6.1 (2018-01-23)
* adds translations to Catalan (ca) and Spanish (es-ES) (contributed by Raulvo) (#141).
* fixes bug; expected "11h 55s", actual "11h55s" (#b61d942).
* enhances the calculator selector used by widget configuration (now shows descriptive text).
* misc accessibility fixes (labelFor, dropDownVerticalOffset).
* adds web links in the About Dialog to the changelog and version commit.  
* updates translations (eo, pl) (#137 by Verdulo).

### v0.6.0 (2017-12-27)
* adds solstice/equinox tracking to app (#13).
* adds solstice/equinox widget (#13).
* adds option to show seconds in rise/set times displayed by widgets.
* adds option to show seconds in rise/set/delta times displayed by the app.

### v0.5.2 (2017-12-21)
* fixes "tomorrow will be" comparison; erroneously reported 1m (when actually 0s) for non-simple sources (1m is correct for sunrisesunsetlib and time4a-simple).
* misc refactoring to prevent memory leaks (LightMapTask, TimeZonesLoadTask).
* fixes flippable widget randomly displays 24hr time (#129).
* fixes table switch animation fails to play (#125).
* fixes automatic keyboard popup (WidgetConfigActivity, ThemeConfigActivity); prevent the keyboard from taking focus on activity start.
* fixes stale/duplicate items in theme selector.
* fixes theme preview icons (now shown) (api22+).
* fixes theme icons don't use theme colors (api22+) (#120).
* fixes crash when adding widgets (api22+) (#126).
* updates dependency (Time4A 3.38-2017c).
* updates translations (eo, pl) (#123, #124 by Verdulo).

### v0.5.1 (2017-12-03)
* changes default data source to time4a-noaa (fallback remains sunrisesunsetlib).
* fixes 2x1 widget to show seconds (e.g. "Tomorrow will be 1m 4s shorter").
* restricts auto-backup to app settings and themes (now excludes widget settings and sqlite db).
* adds collapsed UI state to ColorChooser; expanded by clicking label.
* adds to theme config activity: sunrise, sunset, and noon icon colors (fill, stroke, stroke width).
* fixes theme previews to display sunrise/sunset times (as configured by app) instead of static text.
* fixes widget sunrise, sunset, and noon icons to use theme colors.
* fixes widget titles to marquee over a single line when too long.
* fixes widgets to allow for vertical resize.
* lists 2x1 widget (previously only accessible by resizing 1x1 widget).
* fixes 2x1 layout for api versions <= 15 (previously inaccessible).
* updates dependency (Time4A 3.37-2017c).
* updates translations (eo, pl) (#115, #116 by Verdulo).

### v0.5.0 (2017-11-18)
* auto-backup reenabled.
* adds data source; Time4A (time4a-simple, time4a-noaa, time4a-cc, time4a-time4j) (contributions by MenoData) (#103).
* adds support for custom themes; theme editor activity (add / edit), theme selector activity (copy / delete / export) (#7).
* adds widget option "show noon" (#102); adds noon field to 1x3 widget, adds noon to flippable widget.
* adds widget option "show comparison" (show/hide comparison field on 1x3 widgets).
* adds option to show data source label in app UI / misc data source related UI enhancements.
* adds translation to Hungarian (hu) (contributed by Erci) (#106).
* updates translations (eo, pl) (#110 by Verdulo).

### v0.4.1 (2017-10-31)
* fixes data source setting not honored (#104).

### v0.4.0 (2017-06-12)
* adds translation to French (fr) (contributed by Jej) (#92).
* adds time format option (12hr / 24hr time) (#22).
* adds daylight savings time warning (#90).
* automatic backups now disabled.
* fixes app layout; times unreadable when using large font setting (related to #43).
* fixes lightmap rendering (now optimized to do work off the UI thread).
* updates translations (eo, pl) (#93, #94, #96, #97 by Verdulo).

### v0.3.1 (2017-04-15)
* misc UI tweaks, styles, and strings.
* fixes widget preview images.
* fixes ui bug "table switches unintentionally" (#20).
* fixes "allow resize" option (disabled for api 16 and under).
* updates translations (eo, pl) (#86, #87 by Verdulo).
* adds Junit and Espresso UI testing to the project.

### v0.3.0 (2017-02-23)
* adds data source; ca.rmen.sunrisesunset.
* adds solar time mode; local mean time, and apparent solar time (#66).
* adds "light map" horizontal stacked bar chart.
* adds "ui warnings" (snackbar alerts for unusual date or time zone configurations) (#54).
* adds time zone selector enhancements (color coding, sort by ID or UTC offset).
* adds widget layout option "allow resize" (disable widget resizing).
* adds widget title substitution; %s is for data source.
* fixes app crash (latitude edge case) (#74).
* fixes app crash ("set date" api10) (#75).
* fixes time zone selector loading (now loads asynchronously).
* fixes widget layout update bug / widget resizes itself after update (#77).
* fixes widget update behavior (now updates at midnight) (#77).
* updates translations (eo, pl) (#69, #72, #73, #79, #80, #83, #84 by Verdulo).

### v0.2.3 (2016-11-07)
* fixes alarm set incorrectly w/ user-defined timezones (#64).
* fixes solar noon sometimes incorrect for user-defined timezones (#65).
* fixes ui bug "dialog state lost on orientation change" (#63).
* fixes ui bug where TimeDateDialog settings were not immediately applied.

### v0.2.2 (2016-09-22)
* use network location provider if available (#49).
* fixes gps prefs not honored (#50).
* fixes app crash on SettingsActivity (api14, api15) (#55).
* fixes app crash on ExportPlaces (api18) (adds permission EXTERNAL_STORAGE) (#67).
* fixes misc SettingsActivity bugs (api10, api15) (#57, #58, #59, #60).
* updates translations (eo, pl) (#51 by Verdulo).

### v0.2.1 (2016-08-30)
* fixes unreadable app layouts when using non-english locales (#43).
* fixes missing actionbar overflow icons.
* fixes lat/lon input; touch dialog fields to begin editing (#37).
* updates translations (eo, pl) (#46, #47 by Verdulo).

### v0.2.0 (2016-08-13)
* adds translation to Polish (pl) and Esperanto (eo) (contributed by Verdulo) (#24, #26, #31, #35, #36).
* adds option to override the locale from within the app (#23).
* adds default location provided by the locale (#33).
* fixes lat/lon decimal separator bug (#29); fails to set the location when locale uses "," as the decimal separator.
* fixes timezone bug (#34); timezone ignored when using 24hr time.
* fixes ui bug "note doesn't adapt user defined dates" (#19).
* fixes ui bug "table switches unintentionally" (#20).

### v0.1.1 (2016-07-21)
* adds support for localization.
* adds translation to German (de) (contributed by Henrik "HerHde" H�ttemann) (#16).
* fixes app crash when using Show map without an installed map application.
* fixes app crash when sunrise or sunset does not occur for a given date/location.
* fixes "no data source" bug (#14); default value not properly displayed by settings -> general -> dataSource.
* fixes lat/lon precision; was unlimited but now rounded to 5 places (meter precision).
* fixes lat/lon input validation.

### v0.1.0 (2016-07-04)
* adds app activity that displays times for a given location and date
* adds widgetlist activity for reconfiguring home screen widgets
* fixes lat/lon input field bug

### v0.0.2 (2016-03-11)
* adds flippable widget
* adds ontap actions (reconfigure, launch activity)
* adds get gps fix (user defined location)

### v0.0.1 (2015-02-16)
* basic widget
