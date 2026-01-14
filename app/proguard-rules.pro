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
-keepclassmembers class *.MenuBuilder {
    void setOptionalIconsVisible(boolean);
}

# keep SuntimesCalculator noarg constructors
-keepclassmembers class * implements com.forrestguice.suntimes.calculator.core.SuntimesCalculator {
   public <init>();
}

# keep SuntimesTheme constructors
-keepclassmembers class * extends com.forrestguice.suntimeswidget.themes.SuntimesTheme {
   public <init>(android.content.Context);
}

# keep classes from support library
-keep class com.forrestguice.support.widget.SearchView { *; }

# keep descriptor classes
-keep,includedescriptorclasses class com.flask.colorpicker.renderer.ColorWheelRenderer
-keep,includedescriptorclasses class com.flask.colorpicker.slider.OnValueChangedListener

-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.calculator.SuntimesData
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset

-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.equinox.EquinoxView$EquinoxViewListener

-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.getfix.LocationConfigView$LocationViewMode
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.getfix.LocationConfigView$LocationConfigViewListener

-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.graph.LightMapView$LightMapTaskListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.graph.LightGraphView$LightGraphTaskListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.graph.LineGraphView$LineGraphTaskListener

-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.map.WorldMapTask$WorldMapOptions
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.map.WorldMapTask$WorldMapTaskListener

-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.moon.MoonApsisView$MoonApsisViewListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.moon.MoonPhasesView1$MoonPhasesViewListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.moon.MoonRiseSetView1$MoonRiseSetViewListener

-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.notes.NoteViewFlipper$ViewFlipperListener

-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.calculator.settings.TrackingMode
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode

#-keep class **.SPX
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    !static !transient <fields>;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}
#-keepclassmembers class **.SPX implements java.io.Externalizable {
#    static final long serialVersionUID;
#    <init>();
#    public void writeExternal(java.io.ObjectOutput);
#    public void readExternal(java.io.ObjectInput);
#    java.lang.Object readResolve();
#}
