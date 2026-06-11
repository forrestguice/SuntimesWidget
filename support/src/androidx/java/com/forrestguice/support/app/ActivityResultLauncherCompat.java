package com.forrestguice.support.app;

import android.content.Intent;

import com.forrestguice.annotation.Nullable;

public interface ActivityResultLauncherCompat {
    void launch(Intent intent);
    void launch(Intent intent, @Nullable ActivityOptionsCompat options);
}
