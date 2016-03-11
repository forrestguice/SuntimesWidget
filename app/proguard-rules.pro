# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Forrest\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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
