package com.forrestguice.support.app;

import android.content.Intent;

import com.forrestguice.annotation.Nullable;

public interface OnActivityResultCompat {
    void onActivityResultCompat(int requestCode, int resultCode, @Nullable Intent data);
}
