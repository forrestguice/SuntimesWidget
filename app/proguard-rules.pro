# Add project specific ProGuard rules here.
#
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:


# don't obfuscate; the code is GPLed so what is the point, ...and this has a way
# of screwing up functionality that uses reflection (loading plugins from class
# name, etc) - to enable obfuscation these classes must be identified and exempted.
-dontobfuscate

# keep MenuBuilder .. reflection used in SuntimesActivity.forceActionBarIcons
-keepclassmembers class **.MenuBuilder {
    void setOptionalIconsVisible(boolean);
}

# keep SuntimesCalculator noarg constructors
-keepclassmembers class * implements com.forrestguice.suntimeswidget.calculator.SuntimesCalculator {
   public <init>();
}