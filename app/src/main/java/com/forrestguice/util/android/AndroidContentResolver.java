/**
    Copyright (C) 2026 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.util.android;

import android.net.Uri;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.annotation.NonNull;
import com.forrestguice.util.content.ContentResolver;
import com.forrestguice.util.content.Cursor;

public class AndroidContentResolver implements ContentResolver
{
    protected android.content.ContentResolver resolver;
    public AndroidContentResolver(android.content.ContentResolver resolver) {
        this.resolver = resolver;
    }

    public static ContentResolver wrap(android.content.ContentResolver resolver) {
        return new AndroidContentResolver(resolver);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull String uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        android.database.Cursor c = resolver.query(Uri.parse(uri), projection, selection, selectionArgs, sortOrder);
        return (c != null ? AndroidCursor.wrap(c) : null);
    }
}
