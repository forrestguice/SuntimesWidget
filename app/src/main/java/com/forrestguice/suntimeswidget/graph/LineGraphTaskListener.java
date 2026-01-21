/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.graph;

import android.graphics.Bitmap;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.util.concurrent.ProgressListener;

import java.util.Collection;

@SuppressWarnings("EmptyMethod")
public abstract class LineGraphTaskListener implements ProgressListener<Bitmap, Bitmap>
{
    @Override
    public void onStarted() {}
    public void onDataModified(SuntimesRiseSetDataset data) {}
    public void onFrame(Bitmap frame, long offsetMinutes) {}
    public void afterFrame(Bitmap frame, long offsetMinutes) {}
    @Override
    public void onFinished(Bitmap result) {}
    @Override
    public void onProgressUpdate(Collection<Bitmap> values) {}
}
