package com.forrestguice.support.content;

public class FileProvider extends android.support.v4.content.FileProvider {

    public static File getExternalStorageDownloadDirectory(Context context) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

}
