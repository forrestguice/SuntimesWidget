# Contributing

1. [Submit a bug report / feature request](#bugReport)
2. [Add a translation](#addTranslation)
3. [Add a default location](#addDefaultLocation)
4. [Add a "data source"](#addDataSource)
5. [Other](#other)

## Submit a Bug Report or Feature Request
<a name="bugReport" />
Use the issue tracker to submit a bug report or a feature request.

Issues will be tagged **bug**, **feature**, or **enhancement** as they are recognized, and later added to specific **milestones** as they are fixed.

When submitting a bug **please be detailed and specific**. What did you expect the app to do, what did you actually observe? Bugs that can't be reproduced won't get fixed. Useful information includes Android OS version and your specific device model.

## Add a Translation 
<a name="addTranslation" />

**To add a new translation first..**
 1. Learn how android supports translation through resources; see https://developer.android.com/training/basics/supporting-devices/languages.html
 2. Learn how android determines which set of resources it uses; see https://developer.android.com/preview/features/multilingual-support.html
 3. Determine your locale code and values-folder-name (e.g. values-en-rGB is British English); see https://github.com/championswimmer/android-locales for a list of codes.
 
**When ready to start translating..**
 
 1. Fork the master repository and create a new branch for your edits.
 
 2. Edit the `/res/<values-folder>/strings.xml` file (e.g. `/res/values-en-rGB/strings.xml`).
   * If the folder doesn't exist, create it and copy from `/res/values/strings.xml` as a starting template.
   * If the folder/file already exists, copy-and-paste values (shadow them) from `/res/values/strings.xml`.
 
 3. Translate the string values.
   * Do not add new values that don't already appear in the default `/res/values/strings.xml`. The app will crash if it accidentally tries to use any value that isn't in the default.
   * Do not translate values that have the attribute `translatable="false"` (remove these definitions, they should only exist in the default `/res/values/strings.xml`)
   * Do not translate the values that fall between `<xliff:g>` tags; these are either parameters or constant values that must remain the same. You may translate the text that appears around these tags.
   * For `string-array` type values, be mindful not to add, remove, or change the order of the entries; these `string-arrays` are usually mapped (one-to-one) with non-translatable values in the default `/res/values/strings.xml`.
   * note: `<![CDATA[` tags are sometimes used to preserve HTML markup (which is later displayed by the view). You can add and extend these tags for values that already use them, but otherwise values do not expect HTML (and these tags shouldn't be used).

 4. Add your name to the credits; add a new line `translation by <your name>` to the `app_legal2` value in the default `/res/values/strings.xml`, and also in your translated `strings.xml`. 

 5. (optional) Create a new issue in the tracker if you have questions, require feedback, or would otherwise like to discuss your translation.
 
 6. Submit a pull request.

## Add a default location
<a name="addDefaultLocation" />

1. Follow the guidelines for adding a new translation, except only shadowing a small subset of values.
2. In your `res/<values-folder>/strings.xml` file add/modify the follow definitions with your locale specific values:

```
<string name="default_location_label">City Name</string>
<string name="default_location_latitude">34.5409</string>      <!-- decimal degrees (DD) -->
<string name="default_location_longitude">-112.4691</string>   <!-- decimal degrees (DD) -->
```
note: The lat/lon should use `.` as the decimal separator and include 4 decimal places.


## Add a "data source"
<a name="addDataSource" />
The app uses an interface (`suntimeswidget.calculator.SuntimesCalculator`) to perform the actual calculations (the default implementation uses `sunrisesunsetlib-java`). TODO: describe how to add alternative implementations.

## Other
<a name="other" />
Use the issue tracker to start a discussion and get started. Your participation in the project in other ways is also welcome.
