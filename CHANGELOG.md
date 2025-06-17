### ~

### v0.16.7 (2025-05-12)
* fixes app crash when twilight alarms have an attached note (#872).
* improves searching places when names contain special characters.
* adds world places; ~120 places; South American capitals and other major cities.
* adds "check all" button to the world places dialog; fixes bug where dialog allows all items unchecked.
* adds an indicator to the moon dialog when the displayed time is adjusted by DST (#870).
* adds an additional "clear alarms" preference to alarm settings.

### v0.16.6 (2025-04-16)
* adds option for "solar midnight" (#835).
* adds world places: ~110 places; North American capitals, major cities, and others.
* adds an indicator to sun dialog when the displayed time is adjusted by DST.
* fixes bug where "sun position grid does not re-align when DST changes" (#867).
* fixes bug where "sun position graph labels are always displayed using 12h time".
* improves UI when configuring time offsets for custom events, alarm, and bedtime settings.
* increases maximum "auto-dismiss after" notification value from 5 minutes to 12 hours (#726).
* enhances `%e` data patterns to work with all widget types (no longer limited to sun widgets).
* adds data patterns: `%es`, `%eS`, `%h`, and `%H`; shadow length, and observer height.
* adds data patterns: `%ea`, `%ez`, `%ed`, and `%er`; altitude, azimuth, declination, right-ascension.

### v0.16.5 (2025-02-14)
* adds an option to the moon dialog, solstice dialog, and solstice widget to only show the number of days remaining (#846, #847).
* adds "gradually increase volume (curve)" to alarm options; defaults to cubic. The volume increases gradually in the beginning, rapidly increasing toward the end.
* enhances the alarm dismiss activity to display the current time using the alarm's time zone (#849).
* enhances the "custom event" alarm selector to display a time preview.
* fixes app crash when using menu actions while refreshing location (#862).
* fixes app crash when toggling wallpaper from the theme configuration activity.
* fixes bug where "warnings only show the first message when multiple warnings are queued".
* fixes bug where "gradually increase volume" is only applied to the left channel.
* fixes bug where "flippable widget fails to change views" (#855).
* fixes bug where the moon dialog opens with today's information instead of the selected day (#858).
* fixes bug where the color collection activity loses the current selection; fixes bug where the share action is disabled for default colors.
* fixes bug where alarm edit screen layout is cropped in landscape mode; input chips may now flow to use space more efficiently.
* fixes bug where the "location dialog shows the wrong icon when 'use app location' is checked".
* changes defaults; "bright alarms" are now enabled by default.
* changes defaults; "gradually increase brightness" changed from 60 to 30 seconds.
* changes defaults; "gradually increase volume" changed from 10 to 30 seconds.
* extends UI test coverage (#864).

### v0.16.4 (2024-12-04)
* fixes bug "boot completed hangs after phone restart" (#842).
* fixes bug "FGS not allowed to start from BOOT_COMPLETED!" (Android 15).
* fixes bug where "moon dialog displays the wrong phase label" (#843).
* fixes bug where custom color labels are invisible when ellipsized (missing text).
* fixes bug where text is cropped in moon day widget (#845).
* fixes bugs in widget previews; missing padding, missing map foreground.
* adds "preview" action to the "bright alarm colors" selector.
* adds `AFTER_BOOT_COMPLETED`; changes `ACTION_BOOT_COMPLETED` so that it defers scheduling alarms until a few moments later (#842).
* adds time-out when querying various content providers to avoid potential ANRs if a provider fails to respond.
* updates build; replaces jitpack.io artifacts, adds git submodule.
* updates translation to Polish and Esperanto (eo, pl) (#841 by Verdulo).

### v0.16.3 (2024-11-04)
* enhances the quick settings tiles to support displaying their dialogs over the lock screen.
* improves the appearance of the quick settings tile dialogs (replaces AlertDialog).
* fixes app crash when configuring quick settings tiles.
* fixes bug where quick settings tiles use the wrong default values.
* adds a "restore defaults" action to the widget configuration activity.
* enhances "bedtime mode" to support Direct Boot; responds to `LOCKED_BOOT_COMPLETED` to restore bedtime state after a reboot.
* adds "DND rules" option to "bedtime mode"; this advanced option allows choosing between using automatic DND rules, or overriding DND directly (#818).
* fixes the bedtime notification tap action; tapping the notification opens the bedtime activity.
* revises the notification text displayed when the alarm foreground service does periodic work.
* fixes bug where battery optimization warning is displayed on devices without power management (Android TV).
* fixes miscellaneous bugs in the color picker; cropped text on smaller screens; state lost when changing orientation; edit/delete buttons mistakenly enabled for default items; empty/invisible list items.
* drops support for overriding the app theme using widget themes (this functionality is replaced by the "custom colors" UI).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#839 by James Liu).

### v0.16.2 (2024-09-23)
* adds "bright alarm colors"; allows customization of fullscreen alarm colors (#741).
* adds "do-not-disturb" preference that indicates the state of the required permission (#818).
* adds "app restricted" warning; "Alarms may fail to work reliably" when the app is in the rare or restricted app-standby-bucket.
* fixes crash in sunlight widget (#735).
* fixes missing "bright alarm" setting on Android 14 (#741).
* fixes bug where bedtime do-not-disturb fails to activate (#818).
* fixes bug where bedtime notifications are hidden due to low priority (adds a separate bedtime notification channel).
* fixes bug where the alarm foreground service fails to stop after triggering notifications.
* fixes missing notification text when the alarm foreground service does periodic work.
* fixes text contrast/readability issues when modifying custom colors (support for "color roles").
* enhances custom colors to allow for user-defined labels.
* fixes bug where color dialog fails to show the alpha slider, and other miscellaneous improvements.
* fixes bug where the "tap action" preference click area is misaligned.
* fixes bug where welcome activity is cropped in landscape orientation (Android TV).
* fixes the "back" gesture so that it dismisses visible warnings first (Android TV).
* fixes inaccurate default place coordinates; Bangui, Conakry.
* updates build; removes jcenter; updates `com.jraska:falcon` to `2.2.0` (#825).
* updates translation to Norwegian (nb) (#832 by FTno).
* updates translation to Polish and Esperanto (eo, pl) (#833 by Verdulo).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#836 by James Liu).

### v0.16.1 (2024-08-19)
* adds "material palette" to the color dialog.
* adds text color preview, and other miscellaneous color picker improvements.
* adds "cross-hair" option to sunlight graph; adds "share bitmap" action.
* fixes sunlight graph so that it shows jump/skip in time zone dst (#735).
* fixes inconsistent text/point sizes between graph views.
* fixes bug where the lightmap widget is rendered incorrectly (#812).
* fixes bug where "sunlight dialog axis labels don't follow user settings (always 12 hour time)". (#824)
* enhances Alarm Settings warnings; show a warning when alarm notifications are disabled on the lock screen (#332).
* enhances Alarm Settings warnings; show warnings when alarm channel is muted, or notifications are temporarily paused/suspended.
* fixes broken "full-screen notifications" preference click listener; the UI now reports the current state of the required permission (#802).
* fixes bug "widget does not update automatically" (#806); periodically detects and recovers stale widgets.
* fixes bug where the alarm dialog fails to switch to the correct tab when scheduling events.
* fixes bug where dialog updates continue running after the dialog is closed.
* fixes bugs in color dialog related to `FragmentPagerAdapter`; fixes crash in color dialog on rotation.
* fixes bug where color sheet fails to retain state on rotation.
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#813 by James Liu).
* updates translation to Norwegian (nb) (#817 by FTno).
* updates translation to Brazilian Portuguese (pt-br) (#819 by naoliv).
* updates translation to Polish and Esperanto (eo, pl) (#824 by Verdulo).

### v0.16.0 (2024-07-31)
* adds "sidebar navigation", and an option to change the "launcher activity" (#505).
* adds support for custom events based on "shadow length" (#331).
* adds support for customizing the app color scheme; override map, graph, and highlight colors.
* adds "sunlight graph" dialog (of sunlight over the year) (#735), "earliest/latest sunrise/sunset" info (#753), and a 3x2 graph widget (to sun position widgets).
* adds "use app location" option to alarms; reschedules alarms automatically when the location is changed (#768, #808).
* adds "bedtime mode" to alarms; helps schedule do-not-disturb during sleep hours (#425).
* adds "high brightness" option (#741) and swipeable buttons (#738) to the alarm screen.
* adds "next alarm" quick settings tile, and 2x2 and 3x2 "next alarm" widgets (#766).
* adds support for notification channels (api26+).
* adds permission `USE_FULL_SCREEN_INTENT` (needed for alarms over the lock screen) [PERMISSION]; fixes bug "alarms fail to display over lock screen" (#802).
* adds permission `FOREGROUND_SERVICE` (needed for alarms and notifications) [PERMISSION].
* adds permission `ACCESS_NOTIFICATION_POLICY` (needed to enable do-not-disturb at bedtime) [PERMISSION].
* fixes navigation bugs when using D-pad within alarm screens (Android TV).
* fixes incorrect default places coordinates (3 places updated).
* fixes bug "Sun Position screen altitude has the wrong colour during Nautical/Astronomical Twilight" (#805).
* changes cross-quarter days to use "culturally neutral cross quarter day names" (#804).
* updates default app and widget themes, and adds additional default widget actions.
* updates Time4A dependency from 4.4.2-2019c to 4.8-2021a.
* updates constraint-layout dependency from 1.0.2 to 2.0.4.
* updates targetSdkVersion (25 -> 26 -> 28) (#725), and build tools to 28.0.3.
* increments minSdkVersion (10 -> 14) (#122); building for api10 remains possible for now by reverting changes in `build.gradle`.

### v0.15.16 (2024-06-17)
* adds "online help" links to existing help dialogs (#797).
* fixes bug where "moon phase alarm time is incorrect" (#803).
* fixes bugs when using d-pad navigation within dialogs (Android TV).
* updates translation to Norwegian (nb) (#796, #801 by FTno).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#799, #800 by James Liu).

### v0.15.15 (2024-05-14)
* adds online user manual; https://forrestguice.github.io/Suntimes/help/ or https://forrestguice.codeberg.page/Suntimes/help/
* fixes app crash when using custom themes (#792).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#791 by James Liu).
* updates translation to Polish and Esperanto (eo, pl) (#793 by Verdulo).

### v0.15.14 (2024-04-15)
* adds translation to Arabic (ar) (contributed by Alelg) (#786).
* adds to list of world places, and allows adding world places by continent (#785).
* adds mirror for help urls and website; some locales may point to GitHub hosted resources instead (#629).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#788 by James Liu).

### v0.15.13 (2024-03-19)
* adds app options to show daylight comparison (today/tomorrow) (#773).
* increases the maximum before/after alarm offset (#779); fixes localization of display values.
* fixes app crash when launching the app after using "restore backup" (#783).
* replaces links to "online help" and improves help presentation; the app's website is now hosted on Codeberg (https://forrestguice.codeberg.page/Suntimes/) (#629).
* now mirroring git repository to Codeberg (https://codeberg.org/forrestguice/Suntimes) (#629).

### v0.15.12 (2024-03-01)
* increases the range of supported dates from +-2.5 years to +-500 years (#770).
* fixes bug where date selector allows selecting unsupported dates (#770), and other miscellaneous UI changes.
* fixes bug where alarm screen back button overlaps the dismiss button (#777).
* fixes bug in date widget where the scaled text is not centered (#763).
* fixes ambiguity in minutes abbreviation; replaces "m" with "min" for all translations that default to metric (#773).
* updates translation to Russian (ru) (#775 by Adelechka).

### v0.15.11 (2024-02-05)
* adds "create/restore backup" option; saves all configuration data as json text (#757).
* adds "export/import widget" option; save/load individual widget configurations.
* adds support for restoring all widgets from backup (requires launcher implementing `AppWidgetManager.ACTION_APPWIDGET_RESTORED`).
* adds donation link to the about dialog; adds Liberapay to donation options (#574); https://liberapay.com/forrestguice/
* fixes bug in date widget where the scaled text is too large (#763).
* increments `CalculatorProviderContract` version 6 -> 7; adds columns for event position data.
* updates translation to Polish and Esperanto (eo, pl) (#761, #769 by Verdulo).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#765 by James Liu).

### v0.15.10 (2024-01-14)
* adds layouts and resources for very small screens (#727); experimental support for wearables.
* adds 'columns' setting to moon dialog; 2, 3 or 4 columns.
* fixes app crash when tapping on the date field (#751).
* fixes bug "can't interact with app after install (flashes/strobes)" (#760).
* fixes bug "custom date format is not saved"; adds calendar format pattern "EE, MMM d" (#759).
* fixes bug where the solstice widget displays "cross-quarter days" when disabled by the app (#755).
* fixes bug where moon dialog content is clipped (#754).
* increments `CalculatorProviderContract` version 5 -> 6; fixes columns for "cross-quarter days".
* changes "header labels" default to "none" for translations with longer strings (de, fr, hu, nb, nl, pt_BR) (#754).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#752 by James Liu).

### v0.15.9 (2023-12-03)
* adds a `back` button to the alarm dismiss activity (#750).
* fixes navigation bugs when using D-pad (Android TV).
* fixes app crash after changing `data source` (#743).
* fixes bug where toolbar fails to apply text size setting.
* fixes bug where alarm dismiss challenge is shown after alarm has timed-out.
* fixes default colors to improve contrast and readability (#744).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#745 by James Liu).

### v0.15.8 (2023-10-24)
* adds a warning when the app is configured to "current location" but location permissions are denied (#733).
* changes the location label when switching away from "current location" mode (#733).
* fixes bug in "current location" mode; the location automatically refreshes when the activity is resumed (#733).
* fixes bug where the time zone selector shows the wrong system time zone (#733).
* fixes bug where the alarm event icon and text are out of alignment.
* refactors alarm adapter click listeners (bind rowID instead of position).
* updates translation to Hungarian (hu) (#736 by titanicbobo).

### v0.15.7 (2023-09-10)
* adds a warning to SuntimesAlarms when the "Autostart" setting is disabled (Xiomi devices only) (#730).
* fixes bug "time refreshes aren't happening properly" (#705).
* fixes bug where the update loop continues running in the background after the activity is no longer visible.
* fixes bugs where rapidly clicking triggers actions more than once (throttled click listeners).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#728 by James Liu).

### v0.15.6 (2023-07-21)
* improves time zone defaults (localized default values).
* improves time zone recommendations; fixes recommendation when place names contain spaces or special characters.
* adds "recommend time zone" action to time zone dialog.
* fixes time zone list to show the correct display name and offset when day light saving is applied.
* fixes app crash when addons attempt to open settings with an invalid fragment.
* changes labels for cross-quarter days (#719); Imbolc, Beltane, Lughnasadh, Samhain.
* changes snooze notification so that it no longer triggers fullscreen intent (#724).
* updates translation to Polish and Esperanto (eo, pl) (#722 by Verdulo).
* updates translation to Brazilian Portuguese (pt-br) (#721 by naoliv).

### v0.15.5 (2023-07-01)
* adds Hijri calendar to the date widget (#714).
* fixes bug where alarms using Apparent Solar Time drift over time (#715).
* fixes bug where app dialogs display Apparent Solar Time with reduced accuracy.
* fixes bug where actions fail to apply all available %substitutions.
* fixes bug where actions fail to apply the correct `extra type` to %substitutions.
* increases max snooze from 59 to 120 minutes; increases max "auto dismiss" from 59 to 300 seconds.
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#716 by James Liu).

### v0.15.4 (2023-06-03)
* adds support for Android TV.
* adds "current location" mode to widgets (#707); widgets use the "last known" location from any provider.
* enhances custom events to support an offset in minutes (#537).
* enhances custom events to accept angle as decimal (#704).
* fixes a bug where the "lunar noon" and "lunar midnight" notes are displayed when the option is disabled.
* fixes cropping in ActionBar when using "large" or "extra large" text, and other layout improvements.
* fixes bug where the "3x1 sun widget" doesn't appear in the widget list (#711); adds 3x1 sun widget preview image.
* updates translation to Polish and Esperanto (eo, pl) (#712 by Verdulo).

### v0.15.3 (2023-04-10)
* updates translation to French (fr) (#702 by grenagit).

### v0.15.2 (2023-03-20)
* adds themed alarms icon (Android 13+); updates shortcut icons (adaptive).
* adds "quick notification" alarm shortcut; adds "world map" app shortcut; removes "themes" shortcut.
* adds help to the "alarm note" dialog (supports substitutions).
* adds a warning message when overriding the locale; "the app may need to be restarted before changes take full effect".
* fixes bug where overriding the locale is not immediately applied to existing widgets.
* fixes bug where changes to settings from the WelcomeActivity are not applied until after the app is restarted.
* fixes bug "sun position doesn't update on main screen" (#695).
* fixes bug "broken 2x1 sun widget preview".
* misc refactoring (reorganizes WidgetLayout and SettingsActivity classes).
* fixes spelling error in translation to German (de) (#698 by Das-Nichts).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#700 by James Liu).

### v0.15.1 (2023-02-15)
* adds themed icon (Android 13+).
* adds Chinese, Indian, Japanese, Korean, Minguo, and Vietnamese calendars to the date widget (#692).
* adds %eZ@ (azimuth), %eD@ (declination), and %eR@ (right ascension) title substitutions (#677).
* fixes bug "app crash when showing moon dialog from shortcut" (#691).
* fixes bug "widgets crash with Lawnchair" (#690).
* fixes bug "navigation bar is white instead of black with dark theme" (#696).
* fixes bug where the main table header displays azimuth at civil twilight instead of sunrise/sunset.
* fixes bug where scaled text is cropped in 1x1 moon widget and 1x1 date widget.
* updates translation to Norwegian (nb) (#693 by FTno).
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#688 by James Liu).

### v0.15.0 (2023-01-31)
* adds "Welcome Dialog"; a guided introduction and initial configuration wizard (#603).
* adds "High Contrast" app themes (#492, #615); changes default theme to "System default" (#666).
* adds "Text Size" setting; display "small", "normal", "large", or "extra-large" text (#492, #615, #622, #656).
* adds support for "Quick Settings" tiles; show information in the settings tray; "clock tile" and "next event tile" (#399).
* adds support for "Cross-Quarter Days" (#551); solstice and equinox midpoints.
* adds support for custom sun elevation events. Show custom events from a widget, or use them to schedule alarms and notifications (#522, #537, #598).
* adds "date" widget; show the date using different calendars (Coptic, Ethiopian, Gregorian, Hebrew, Julian, Solar Hijiri, Thai Solar) (#398); adds "date" option to clock widget (Gregorian).
* adds support for "Quick Notifications" (#552); notifications are automatically dismissed after a few moments.
* adds alarm "note"; show user-defined text as part of notifications and other UI; supports %s substitutions.
* adds alarm "snooze limit" option; defaults to "no limit".
* adds support for alarm challenges (and addons); dismiss alarms after solving "easy math" problems; dismiss alarms with an NFC tag (https://github.com/forrestguice/SuntimesNFC).
* adds alarm "reminder" option (#628), and "reminder action" option; notifications may perform a custom action when dismissed.
* enhances alarm cards; list UI now indicates state, animates sounding and snoozing states, and displays action buttons when applicable.
* adds alarm list sort options; "Enabled First", "Show Offsets" (#611); adds "Clear Selection" button.
* adds "Altitude Graph" to Sun Position dialog; adds 3x2 "Altitude Graph" to sun position widgets (#625).
* adds "Seek Altitude" to Sun Position dialog (#625); jump to sunrise, sunset, noon, or user-defined sun elevation.
* adds Moon dialog playback controls; step forward/back by 5m increments.
* adds "lunar noon" and "lunar midnight" to Moon dialog; enhances rise/set view (swipe-able, seeks forward/back).
* adds "moon day" widget (#345); show number of days since the new moon.
* changes the main card to display moon illumination as a range (#384, #572).
* adds "Emphasize Field" to User Interface settings; enlarges text displayed by main table (#622, #615).
* removes "Set Date" dialog; replaced with "View Date" (#613); cards now seek to date instead of (re)centering.
* enhances solstice/equinox card (swipe-able, cross-quarter dates).
* adds 3x1 sun widget (#423); an expanded version of the 2x1 widget.
* adds "use app time zone" widget option.
* adds TimeZone dialog preview; replaces TimeZone dialog sort action mode with a context menu.
* misc dialog improvements; updates to Location, Date, and TimeZone dialog layouts.
* misc settings; "object shadow" moved from "General" to "User Interface".
* fixes bug "sun/moon drop-downs show wrong data" (#613).
* fixes bug "sun position is out of alignment" (#601).
* fixes bug where already enabled alarms fail to reschedule after modifying alarm details.
* fixes tooltip help (now available for all buttons); press and hold to display help.
* increments alarm database version 3 -> 4; adds "note", "flags", "actionID2", and "actionID3" columns.
* increments `CalculatorProviderContract` version 4 -> 5; adds columns for cross-quarter days, tropical year, text size, time zone mode, solar time mode, and eot.

### v0.14.12 (2022-12-22)
* adds a boot notification if battery optimization is enabled (and alarms are active).
* fixes bug "dismiss alarm fails to remove alarm notification" (#665).
* fixes bug when re-scheduling repeating moon phase alarms (#494; "unreliable full moon alarm").
* fixes bug where toast messages are unreadable when using system dark mode (white text on a white background) (Android 12) (#660).
* fixes bug in AlarmEditActivity where the UI fails to display the configured label.
* fixes bug "alarm reminder is not shown after changing 'reminder within' setting" (#659).
* fixes potential NPE in AlarmNotifications `updateAlarmTime_` methods when repeatingDays is null.
* fixes potential NPE in AlarmEditActivity when alarm location is null.
* fixes potential NPE in AlarmAddons when addon event pickers supply an invalid uri (missing provider, missing permissions).

### v0.14.11 (2022-11-29)
* adds permission REQUEST_IGNORE_BATTERY_OPTIMIZATIONS (#651) [PERMISSION]; SuntimesAlarms now makes a direct request to be added to the whitelist.
* adds a help dialog that explains battery optimization warnings; adds online help; a revised message is shown for devices that are likely to break alarms.
* adds data substitutions `eot` (formatted equation of time), and `eot_m` (equation of time millis) (#649).
* adds "Boot Completed" to the Alarm Settings; shows reboot information and manually triggers "reschedule all" (#653).
* adds a foreground service notification that is shown while rescheduling all alarms (#653).
* changes SuntimesAlarms to reschedule all alarms after an app upgrade (ACTION_MY_PACKAGE_REPLACED), or if it detects that boot_completed has failed to run.
* adds `suntimes.action.widgets.UPDATE_ALL` to SuntimesActivity (#649); causes app launch to also trigger a widget update.
* adds `OPEN_SETTINGS` to widget actions; enhances the action editor with Suntimes specific action suggestions.
* fixes bug 'alarm list shows stale values when opened immediately after reboot' (#647).
* fixes bug 'unable to disable alarm reminder notification' (#650); adds 'never' button to preference dialog; notification can now be dismissed by swiping.
* fixes bug in LocationDialog where the spinner is blank after canceling PlacesActivity request.
* fixes bug in LocationDialog where location request doesn't automatically start after granting permissions.
* fixes crash in LocationDialog when clicking "Use last position".
* miscellaneous 2x1 sun widget fixes; scale text/icons, centers layout (#423).
* updates translation to Norwegian (nb) (#648 by FTno).
* updates translation to Traditional Chinese (zh_TW) (#646, #657 by James Liu).
* updates translation to Simplified Chinese (zh_CN) (#645 by sr093906, #646, #657 by James Liu).

### v0.14.10 (2022-11-08)
* adds "abbreviated month names" widget option (#625).
* adds a help dialog to the alarm edit activity (#628); adds "day light saving" to main help dialog.
* fixes bug "alarms do not compensate for time zone changes" (#643).
* fixes bug "BOOT_COMPLETED fails to reschedule stale alarms" (#641).
* fixes bug "alarm list doesn't update after repeating alarms are dismissed" (#640).
* fixes bug where the "light theme" is only partially applied (when system dark mode is also enabled).
* fixes 3x1 sun position widget "scale text and icons" option; changes labels to bold for better readability (#625).

### v0.14.9 (2022-10-26)
* adds "update all" to widget actions (#625).
* adds a warning to SuntimesAlarms when battery optimization is enabled (api23+), or STAMINA mode is enabled (sony devices only).
* fixes app crash when the default alarm ringtone is unavailable (#634).
* fixes wrong/missing colors when using system dark mode.
* fixes "size of sun in 2x1 and 3x1 lightmap widgets" (#624).
* fixes lightmap "long click" to be consistent with a normal click.
* fixes alarm notification "dismiss" label to help improve context (#628).
* updates build; gradle wrapper to `gradle-5.0`.
* updates translation to Norwegian (nb) (#632 by FTno).
* updates translation to German (de) (#631 by CSTRSK).
* updates translation to Czech (cs) (#630 by utaxiu).

### v0.14.8 (2022-09-26)
* fixes crash when location is set to high latitudes (#623).
* fixes appearance of location icons when using system dark mode.
* fixes bug where the "observer height" preference sometimes displays stale values.
* adds link to online help for widget "title substitutions".
* updates translation to Polish and Esperanto (eo, pl) (#619 by Verdulo).

### v0.14.7 (2022-08-02)
* adds support for system dark mode (night mode).
* adds option to show/hide the map button; fixes map icon (#573).
* fixes app crash when changing locales (#482).
* fixes app crash when exporting alarms (#612).
* fixes bug "alarm import/export does not retain sorted order" (#610).
* fixes bug "alarm list is not sorted after adding items" (#609).

### v0.14.6 (2022-06-04)
* fixes crash when changing an alarm's time/event (#605).
* fixes bug where units setting is ignored (altitude displayed in feet) (#604); [Android Go]
* updates translation to Czech (cs) (#606 by utaxiu).

### v0.14.5 (2022-05-14)
* fixes bug "default alarm sound fails to play" (#593); adds fallback ringtones (rtttl).
* fixes bug "sounding/snoozing notification is unexpectedly canceled" (#594).
* fixes bugs in alarm dialog; dialog creates items of wrong type; dialog last selection not saved.
* fixes bug where alarm import is unable to select previously exported files (#588).
* adds export file selection (alarms/places/themes) using Storage Access Framework (api19+) (older devices still use `ACTION_SEND`).
* adds import warning dialog; alarm sounds and actions may revert to defaults (not retained).
* adds widget title %substitutions; %em (event time millis), %et (formatted event time), %eT (formatted event time w/ seconds), and %eA (event angle) (#599).

### v0.14.4 (2022-05-03)
* adds import/export to SuntimesAlarms (#588); save and load alarms as JSON.
* adds widget layouts (3x1 sun position); show the lightmap graph with reduced height (#589).
* extends map widgets to use the dialog configuration; shared options for center/background/tint, sunlight/moonlight, location, latitudes, and graticule (#493).
* extends the widget action dialog to suggest package/class names (#546).
* fixes bug where widget actions that use an explicit intent fail to launch (#546).
* fixes bug where widget action extras are not correctly applied (#546); ints omitted, longs applied as String.
* fixes bug where the sun position dialog and map dialog animations run in the background (#582).
* fixes bug in sun position widgets where the theme colors aren't applied; fixes default colors (#589); adds graph pointFill and pointStroke colors.
* fixes bug where single-select menu items are displayed as checkboxes instead of radio buttons (#590).
* fixes bug where the widget theme spinner fails to show the background preview.
* fixes issue with 'Advanced' settings discoverability (#581).
* fixes SuntimesAlarms intent-filter to support standard AlarmClock intents; `android.intent.action.SHOW_ALARMS`, `android.intent.action.DISMISS_ALARM` (`EXTRA_ALARM_SEARCH_MODE`), and `android.intent.action.SNOOZE_ALARM` (`android.intent.extra.alarm.SNOOZE_DURATION`); adds 'snooze alarm' and 'dismiss alarm' default actions.
* updates translation to Polish and Esperanto (eo, pl) (#585 by Verdulo).
* updates translation to Brazilian Portuguese (pt-br) (#587 by naoliv).

### v0.14.3 (2022-04-11)
* adds click to solar noon field; opens the lightmap dialog (#562).
* changes click on sunrise/sunset headers; opens the lightmap dialog if configured to show azimuth (#562).
* fixes lunar noon field; omit on days it doesn't occur (#572).
* fixes bug "solstice dialog 'view date' menu doesn't work" (#577).
* fixes bug where the AlarmNotifications service fails to stop (battery use in background) (#575).
* fixes ANR when showing alarm dialog (#576); misc changes to ringtone management.
* fixes app crash when using 'fallback to last location'.
* fixes bug where changes made in the PlacesActivity aren't displayed by the location spinner.
* changes action prefix to "suntimes.action"; remaps legacy actions.
* misc layout changes (enlarged click areas); misc cleanup/refactoring.

### v0.14.2 (2022-03-14)
* fixes crash when using 'sun position' app shortcut (#567).
* fixes bug where "search places doesn't work" (#566).
* fixes bug where '1x1 moon widget' illumination is always displayed (fails to be hidden) (#563).
* changes default 'time zone mode' back to `system`; reverts change from 7c288be (#565).
* adds extras to SuntimesActivity intent; `ACTION_VIEW_SUN` and `ACTION_VIEW_WORLDMAP` now accept `EXTRA_SHOW_DATE`; `ACTION_ADD_ALARM` accepts `EXTRA_SOLAREVENT`.
* updates translation to Norwegian (nb) (#568 by FTno).
* updates translations to Polish (pl) and Esperanto (eo) (#571 by Verdulo).

### v0.14.1 (2022-02-22)
* fixes crash when using "current location" (#556).
* fixes bug "lightmap for tomorrow card fails to display" (#557).
* fixes bug "content-provider supplies the wrong time zone" (#554).
* adds default world places; ~34 additional locations.    
* adds 'online-help' link to actions help dialog (#489, #546).
* changes default 'solar time' mode to Local Mean Time.
* minor fixes to `en` translations; e.g. `en_US` and `en_CA` display 'fall equinox'.
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#553 by James Liu).
* updates translation to Norwegian (nb) (#561 by FTno).

### v0.14.0 (2022-02-07)
* extends add-on functionality; adds content-providers for actions, alarms, and alarm events.
* adds support for add-on alarms; set an alarm depending on a calculation (#516); the alarm dialog displays a popup menu of add-on AlarmPickers (hidden if none).
* adds support for 'power off alarms' (#426); adds permission POWER_OFF_ALARM. [PERMISSION].
* adds a date picker to the alarm dialog; its now possible to schedule alarms at a given date+time (repeat yearly).
* adds playback (Reset, Play/Pause) to the lightmap dialog (#519).
* adds altitude at solar noon to the main card view (#524); adds an option display the rising/setting azimuth is place of header labels (#485).
* adds option 'localize to hemisphere'; swap the waxing/waning moon phase icons for southern hemisphere (northward view) (#526).
* adds 'tropical year' to the solstice dialog, and 'recent' to solstice tracking modes.
* adds world map "Azimuthal projection centered on custom coordinates" (#493); adds custom map backgrounds; 'set center', 'set map background', 'tint map', 'graticule' and 'debug lines' options.
* updates world map backgrounds; "Blue Marble" updated to include bathymetry.
* adds widget option to "use the app location".
* adds widget option to override 12h/24h time format setting.
* adds widget option to "fill entire cell" or "align base" to top-left, right, bottom, ...
* adds widget option to "scale text and icons" (extra large widgets).
* renames widget option "grow with available space" to "swap layouts to fill space"; default changed to false; adds help text.
* reorganizes widget configuration; enhances widget selectors; layout selector now shows a themed preview, theme selector shows background.
* misc alarm ui improvements; adds a submenu to the alarm edit activity; an alternate way access/discover alarm features (#416).
* misc solstice dialog improvements; adds context menus, overflow menu, and tracking submenu.
* misc world map and lightmap dialog improvements; adds overflow menu; adds time zone submenu.
* misc dialog improvements; navigate between dialogs or addons with the selected date/time.

### v0.13.19 (2022-01-24)
* adds help for `data source` setting; adds link to online help (#223).
* reorganizes General settings (#223, #533); `show seconds` restored to `general`; `data source` moved into `advanced`; hides `experimental`. 
* updates translations to Simplified Chinese (zh_CN) and Traditional Chinese (zh_TW) (#543 by James Liu).
* updates translation to Norwegian (nb) (#542 by FTno).

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
