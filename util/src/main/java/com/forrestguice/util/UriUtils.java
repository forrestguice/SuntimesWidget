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

package com.forrestguice.util;

import com.forrestguice.annotation.Nullable;

public class UriUtils
{
    @Nullable
    public static String getLastPathSegment(@Nullable String uri)
    {
        if (uri != null)
        {
            String uri0 = uri.trim();
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.lastIndexOf("/"));
            }

            String[] parts = uri0.split("/");
            if (parts.length > 1) {
                return parts[parts.length - 1];
            } else return uri;
        } else return null;
    }

    @Nullable
    public static String getAuthority(@Nullable String uri) {
        if (uri != null) {
           return parseAuthority(uri, findSchemeSeparator(uri));
        } else return null;
    }

    ///////////////////////////////////////////

    /*
       The following methods are adapted from Android Uri.class
     */

    /*
     * Copyright (C) 2007 The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    /**
     * Parses an authority out of the given URI string.
     *
     * @param uriString URI string
     * @param ssi scheme separator index, -1 for a relative URI
     *
     * @return the authority or null if none is found
     */
    static String parseAuthority(String uriString, int ssi) {
        int length = uriString.length();
        // If "//" follows the scheme separator, we have an authority.
        if (length > ssi + 2
                && uriString.charAt(ssi + 1) == '/'
                && uriString.charAt(ssi + 2) == '/') {
            // We have an authority.
            // Look for the start of the path, query, or fragment, or the
            // end of the string.
            int end = ssi + 3;
            LOOP: while (end < length) {
                switch (uriString.charAt(end)) {
                    case '/': // Start of path
                    case '\\':// Start of path
                        // Per http://url.spec.whatwg.org/#host-state, the \ character
                        // is treated as if it were a / character when encountered in a
                        // host
                    case '?': // Start of query
                    case '#': // Start of fragment
                        break LOOP;
                }
                end++;
            }
            return uriString.substring(ssi + 3, end);
        } else {
            return null;
        }
    }

    /** Finds the first ':'. Returns -1 if none found. */
    private static int findSchemeSeparator(String uriString) {
        return uriString.indexOf(':');
    }
}
