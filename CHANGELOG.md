### ~

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
* updates translations (eo, pl) (#184, #186).

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
* updates translations (eo, pl) (#180).

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
* updates translations (eo, pl) (#171, #172, #175); adds translated fastlane metadata.

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
* updates translations (eo, pl) (#160, #162, #163).

### v0.6.2 (2018-02-03)
* adds option "verbose accessibility"; better support for TalkBack. 
* misc usability fixes (MainActivity, AlarmDialog); better support for TalkBack. 
* misc layout fixes (improved accessibility); better support for "large text".
* fixes bug "solstice/equinox dates not localized" (#146).
* adds "adaptive" launcher icon (used by api26+).
* updates translations (eo, pl) (#148, #149).
 
### v0.6.1 (2018-01-23)
* adds translations to Catalan (ca) and Spanish (es-ES) (contributed by Raulvo) (#141).
* fixes bug; expected "11h 55s", actual "11h55s" (#b61d942).
* enhances the calculator selector used by widget configuration (now shows descriptive text).
* misc accessibility fixes (labelFor, dropDownVerticalOffset).
* adds web links in the About Dialog to the changelog and version commit.  
* updates translations (eo, pl) (#137).

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
* updates translations (eo, pl) (#123, #124).

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
* updates translations (eo, pl) (#115, #116).

### v0.5.0 (2017-11-18)
* auto-backup reenabled.
* adds data source; Time4A (time4a-simple, time4a-noaa, time4a-cc, time4a-time4j) (contributions by MenoData) (#103).
* adds support for custom themes; theme editor activity (add / edit), theme selector activity (copy / delete / export) (#7).
* adds widget option "show noon" (#102); adds noon field to 1x3 widget, adds noon to flippable widget.
* adds widget option "show comparison" (show/hide comparison field on 1x3 widgets).
* adds option to show data source label in app UI / misc data source related UI enhancements.
* adds translation to Hungarian (hu) (contributed by Erci) (#106).
* updates translations (eo, pl) (#110).

### v0.4.1 (2017-10-31)
* fixes data source setting not honored (#104).

### v0.4.0 (2017-06-12)
* adds translation to French (fr) (contributed by Jej) (#92).
* adds time format option (12hr / 24hr time) (#22).
* adds daylight savings time warning (#90).
* automatic backups now disabled.
* fixes app layout; times unreadable when using large font setting (related to #43).
* fixes lightmap rendering (now optimized to do work off the UI thread).
* updates translations (eo, pl) (#93, #94, #96, #97).

### v0.3.1 (2017-04-15)
* misc UI tweaks, styles, and strings.
* fixes widget preview images.
* fixes ui bug "table switches unintentionally" (#20).
* fixes "allow resize" option (disabled for api 16 and under).
* updates translations (eo, pl) (#86, #87).
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
* updates translations (eo, pl) (#69, #72, #73, #79, #80, #83, #84).

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
* updates translations (eo, pl) (#51).

### v0.2.1 (2016-08-30)
* fixes unreadable app layouts when using non-english locales (#43).
* fixes missing actionbar overflow icons.
* fixes lat/lon input; touch dialog fields to begin editing (#37).
* updates translations (eo, pl) (#46, #47).

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
