package com.forrestguice.support.annotation;

@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.LOCAL_VARIABLE, java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.PACKAGE})
public @interface NonNull {
    // to enable lint checks: Android Studio -> File -> Settings -> Editor -> Inspections
    // -> Probable Bugs -> Constant Conditions & Exceptions -> Configure Annotations
    // then + to select and add this class
}
