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
}
