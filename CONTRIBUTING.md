# Contributing

1. [Donations](#donations)
2. [Bug Reports and Feature Requests](#bug-reports-and-feature-requests)
3. [Spread the Word](#spread-the-word)
4. [Translations](#translations)
5. [Plugins and Addons](#plugins-and-addons)
6. [Other](#other)

## Donations

Does this app provide value? Please consider a monetary contribution if your disposable income allows it. Putting a price on the binary is a great way to show support for software you care about.

<a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&amp;hosted_button_id=NZJ5FJBCKY6K2"><img src="/SuntimesWidget/assets/images/PP_logo_h_150x38.png" alt="paypal" /></a>

## Bug Reports and Feature Requests
Use the issue tracker to submit a bug report or a feature request.

When reporting a bug **please be detailed as possible**. What did you expect the app to do, what did you actually observe? Include the app version number in your report. Other useful information includes the Android OS version (and sometimes your specific device model).

## Spread the Word
Share this app. Tell your friends. Let others know about F-Droid and the software that's available there.

## Translations

 1. Determine your locale code and values-folder-name (e.g. values-en-rGB is British English); see https://github.com/championswimmer/android-locales for a list of codes.

 2. Fork the master repository and create a new branch for your edits.

 3. Edit the `/res/<values-folder>/strings.xml` file using your favorite text editor. If it doesn't exist then create it and copy from `/res/values/strings.xml` as a starting template.

 4. Translate strings. If updating a translation, the lines that are marked `TODO` need translation or review.
   * Do not add values that don't already appear in the default `/res/values/strings.xml`.
   * Do not translate values that are marked `translatable="false"`. These values should only exist in the default `/res/values/strings.xml`.
   * Do not translate values between `<xliff:g>` tags. These are either parameters or constant values that must remain the same.
   * For `string-array` values be mindful not to change the order of the entries. These `string-arrays` are usually mapped (one-to-one) with non-translatable values in the default `/res/values/strings.xml`.
<br /><br />
 5. Add your name to the credits.
  * For new translations, add an item to `locale_credits`, and corresponding items to `locale_values`, `locale_display`, and `locale_display_native` (in the default `/res/values/strings.xml`).
  * For existing translations, append your name to the corresponding line in `locale_credits` (using a `|` as a delimiter).
  <br /><br />
 6. Submit a pull request.

### Plugins and Addons
Add additional data sources (use your own algorithms) by implementing the `SuntimesCalculator` interface.
Access data from separate "addon" apps or widgets using the Suntimes ContentProvider. [https://github.com/forrestguice/SuntimesWidget/wiki/Interfaces](https://github.com/forrestguice/SuntimesWidget/wiki/Interfaces)

## Other
Use the issue tracker to start a discussion and get started. Your participation in the project in other ways is also welcome.
