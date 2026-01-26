package com.forrestguice.support.app;

import android.content.Intent;

public interface OnActivityResultCompat
{
    void onActivityResultCompat(int requestCode, int resultCode, Intent data);
}
