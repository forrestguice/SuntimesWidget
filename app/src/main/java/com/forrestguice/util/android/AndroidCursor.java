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

import com.forrestguice.util.content.Cursor;

public class AndroidCursor implements Cursor
{
    protected final android.database.Cursor c;
    public AndroidCursor(android.database.Cursor cursor) {
        c = cursor;
    }

    public static Cursor wrap(android.database.Cursor cursor) {
        return new AndroidCursor(cursor);
    }

    @Override
    public int getCount() {
        return c.getCount();
    }

    @Override
    public int getPosition() {
        return c.getPosition();
    }

    @Override
    public boolean move(int i) {
        return c.move(i);
    }

    @Override
    public boolean moveToPosition(int i) {
        return c.moveToPosition(i);
    }

    @Override
    public boolean moveToFirst() {
        return c.moveToFirst();
    }

    @Override
    public boolean moveToLast() {
        return c.moveToLast();
    }

    @Override
    public boolean moveToNext() {
        return c.moveToNext();
    }

    @Override
    public boolean moveToPrevious() {
        return c.moveToPrevious();
    }

    @Override
    public boolean isFirst() {
        return c.isFirst();
    }

    @Override
    public boolean isLast() {
        return c.isLast();
    }

    @Override
    public boolean isBeforeFirst() {
        return c.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() {
        return c.isAfterLast();
    }

    @Override
    public int getColumnIndex(String s) {
        return c.getColumnIndex(s);
    }

    @Override
    public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
        return c.getColumnIndexOrThrow(s);
    }

    @Override
    public String getColumnName(int i) {
        return c.getColumnName(i);
    }

    @Override
    public String[] getColumnNames() {
        return c.getColumnNames();
    }

    @Override
    public int getColumnCount() {
        return c.getColumnCount();
    }

    @Override
    public byte[] getBlob(int i) {
        return c.getBlob(i);
    }

    @Override
    public String getString(int i) {
        return c.getString(i);
    }

    @Override
    public short getShort(int i) {
        return c.getShort(i);
    }

    @Override
    public int getInt(int i) {
        return c.getInt(i);
    }

    @Override
    public long getLong(int i) {
        return c.getLong(i);
    }

    @Override
    public float getFloat(int i) {
        return c.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        return c.getDouble(i);
    }

    @Override
    public int getType(int i) {
        return c.getType(i);
    }

    @Override
    public boolean isNull(int i) {
        return c.isNull(i);
    }

    @Override
    public void close() {
        c.close();
    }

    @Override
    public boolean isClosed() {
        return c.isClosed();
    }
}
