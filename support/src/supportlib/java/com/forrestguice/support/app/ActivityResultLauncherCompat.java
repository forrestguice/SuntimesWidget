package com.forrestguice.support.app;

import android.content.Intent;

public interface ActivityResultLauncherCompat {
    void launch(Intent intent);
    void launch(Intent intent, ActivityOptionsCompat options);
}
