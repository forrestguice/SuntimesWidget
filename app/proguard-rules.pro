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
-keepclassmembers class * implements com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator {
   public <init>();
}

# keep classes from support library
-keep class android.support.v7.widget.SearchView { *; }

# keep descriptor classes
-keep,includedescriptorclasses class com.flask.colorpicker.renderer.ColorWheelRenderer
-keep,includedescriptorclasses class com.flask.colorpicker.slider.OnValueChangedListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.settings.WidgetSettings$TrackingMode
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.settings.WidgetSettings$SolsticeEquinoxMode
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.EquinoxView$EquinoxViewListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.LightMapView$LightMapTaskListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.LocationConfigView$LocationViewMode
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.LocationConfigView$LocationConfigViewListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.calculator.SuntimesData
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.settings.WidgetSettings$TrackingMode
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.graph.LineGraphView$LineGraphTaskListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.map.WorldMapTask$WorldMapOptions
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.map.WorldMapTask$WorldMapTaskListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.moon.MoonApsisView$MoonApsisViewListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.moon.MoonPhasesView1$MoonPhasesViewListener
-keep,includedescriptorclasses class com.forrestguice.suntimeswidget.moon.MoonRiseSetView1$MoonRiseSetViewListener

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
