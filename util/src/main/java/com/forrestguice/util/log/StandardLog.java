/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.util.log;

public class StandardLog implements LogInterface
{
    @Override
    public void d(String tag, String data) {
        System.out.println("D/" + tag + ": " + data);
    }

    @Override
    public void i(String tag, String data) {
        System.out.println("I/" + tag + ": " + data);
    }

    @Override
    public void e(String tag, String data) {
        System.err.println("E/" + tag + ": " + data);
    }

    @Override
    public void e(String tag, String data, Throwable t) {
        System.err.println("E/" + tag + ": " + data + ", " + t.toString());
        t.printStackTrace(System.err);
    }

    @Override
    public void w(String tag, String data) {
        System.out.println("W/" + tag + ": " + data);
    }

    @Override
    public void wtf(String tag, String data) {
        System.err.println("WTF/" + tag + ": " + data);
    }
}
