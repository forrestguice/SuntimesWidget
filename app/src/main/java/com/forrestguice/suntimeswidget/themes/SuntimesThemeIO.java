/**
    Copyright (C) 2017-2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.themes;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface SuntimesThemeIO
{
    boolean write(Context context, OutputStream out, SuntimesTheme[] themes) throws IOException;
    SuntimesTheme[] read(Context context, BufferedInputStream in) throws IOException;

    void setProgressListener( ProgressListener listener );
    void clearProgressListener();

    @SuppressWarnings("EmptyMethod")
    abstract class ProgressListener
    {
        public void onExportStarted() {}
        public void onExported( SuntimesTheme theme, int i, int n ) {}
        public void onExportFinished( boolean retValue ) {}

        public void onImportStarted() {}
        public void onImported( SuntimesTheme theme, int i, int n ) {}
        public void onImportFinished( boolean retValue ) {}
    }
}
