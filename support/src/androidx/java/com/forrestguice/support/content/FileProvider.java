package com.forrestguice.support.content;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class FileProvider extends androidx.core.content.FileProvider {

    public static File getExternalStorageDownloadDirectory(Context context)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            return context.getExternalFilesDir(null);  // TODO: getExternalFilesDir is private to the app; do we have a public alternative?
        } else {
            //noinspection deprecation
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }
    }

}