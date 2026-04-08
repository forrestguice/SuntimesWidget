package com.forrestguice.support.app;

import com.forrestguice.annotation.NonNull;

import java.util.Map;

/**
 * override either of these methods depending on required method signature; both will be called.
 */
public interface OnPermissionResultCompat {
    void onRequestPermissionsResult(int requestCode, @NonNull Map<String, Boolean> results);
    void onRequestPermissionsResultCompat(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
}
