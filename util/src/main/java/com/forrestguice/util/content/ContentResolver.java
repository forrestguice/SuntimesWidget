package com.forrestguice.util.content;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

/**
 * mirrors android.content.ContentResolver
 */
public interface ContentResolver
{
    @Nullable
    Cursor query(@NonNull String uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder);

    /**
     * @return number of rows updated
     */
    int update(@NonNull String uri, @Nullable ContentValues values, @Nullable String where, @Nullable String[] selectionArgs);

    /**
     * @return uri
     */
    @Nullable
    String insert(@NonNull String uri, @Nullable ContentValues values);

    /**
     * @return number of rows inserted
     */
    int bulkInsert(@NonNull String uri, @NonNull ContentValues[] values);

    /**
     * @return number of rows deleted
     */
    int delete(@NonNull String uri, @Nullable String where, @Nullable String[] selectionArgs);

}
