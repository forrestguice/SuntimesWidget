/**
    Copyright (C) 2021 Forrest Guice
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

package com.forrestguice.suntimeswidget.actions;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.calculator.CalculatorProvider;
import com.forrestguice.suntimeswidget.settings.WidgetActions;

import java.util.HashMap;
import java.util.Set;

import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.AUTHORITY;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_ACTION;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_CLASS;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_COLOR;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_DATA;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_DESC;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_EXTRAS;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_MIMETYPE;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_NAME;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_PACKAGE;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_PROVIDER_VERSION;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_TAGS;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_TITLE;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.COLUMN_ACTION_TYPE;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.QUERY_ACTIONS;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.QUERY_ACTION_PROJECTION_MIN;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.VERSION_CODE;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.VERSION_NAME;


/**
 * SuntimesActionsProvider
 * @see SuntimesActionsContract
 */
public class SuntimesActionsProvider extends ContentProvider
{
    private static final int URIMATCH_ACTIONS = 0;
    private static final int URIMATCH_ACTION = 10;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY(), QUERY_ACTIONS, URIMATCH_ACTIONS);                  // content://AUTHORITY/actions
        uriMatcher.addURI(AUTHORITY(), QUERY_ACTIONS + "/*", URIMATCH_ACTION);      // content://AUTHORITY/actions/[themeName]
    }

    private static String AUTHORITY() {
        return BuildConfig.SUNTIMES_AUTHORITY_ROOT + ".action.provider";
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * query
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        HashMap<String, String> selectionMap = CalculatorProvider.processSelection(CalculatorProvider.processSelectionArgs(selection, selectionArgs));
        Cursor retValue = null;

        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_ACTION:
                Log.d(getClass().getSimpleName(), "URIMATCH_ACTION");
                retValue = queryActions(uri.getLastPathSegment(), uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_ACTIONS:
                Log.d(getClass().getSimpleName(), "URIMATCH_ACTIONS");
                retValue = queryActions(null, uri, projection, selectionMap, sortOrder);
                break;

            default:
                Log.e(getClass().getSimpleName(), "Unrecognized URI! " + uri);
                break;
        }
        return retValue;
    }

    /**
     * queryThemes
     */
    private Cursor queryActions(@Nullable String actionID, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        Context context = getContext();
        String[] columns = (projection != null ? projection : QUERY_ACTION_PROJECTION_MIN);
        MatrixCursor retValue = new MatrixCursor(columns);

        if (context != null)
        {
            String[] actions = ((actionID != null)
                    ? new String[] { actionID }
                    : WidgetActions.loadActionLaunchList(context, 0).toArray(new String[0]));
            int i = 0;
            for (String action : actions) {
                if (action != null) {
                    retValue.addRow(createRow(getActionValues(context, action, columns), columns, i++));
                }
            }
        }
        return retValue;
    }

    protected ContentValues getActionValues(@NonNull Context context, @NonNull String actionID, @NonNull String[] projection)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACTION_NAME, actionID);

        for (String column : projection)
        {
            switch (column)
            {
                case COLUMN_ACTION_NAME:
                    values.put(COLUMN_ACTION_NAME, actionID);
                    break;
                case COLUMN_ACTION_COLOR:
                    values.put(column, Integer.parseInt(WidgetActions.loadActionLaunchPref(context, 0, actionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_COLOR)));
                    break;
                case COLUMN_ACTION_TAGS:
                    Set<String> tags = WidgetActions.loadActionTags(context, 0, actionID);
                    values.put(column, WidgetActions.stringSetToString(tags));
                    break;

                case COLUMN_ACTION_TITLE:
                case COLUMN_ACTION_DESC:
                case COLUMN_ACTION_TYPE:
                case COLUMN_ACTION_CLASS:
                case COLUMN_ACTION_PACKAGE:
                case COLUMN_ACTION_ACTION:
                case COLUMN_ACTION_DATA:
                case COLUMN_ACTION_MIMETYPE:
                case COLUMN_ACTION_EXTRAS:
                    values.put(column, WidgetActions.loadActionLaunchPref(context, 0, actionID, column));
                    break;
            }
        }
        return values;
    }

    private Object[] createRow(ContentValues actionValues, String[] columns, int rowID)
    {
        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case "_id":
                    row[i] = rowID;
                    break;

                case COLUMN_ACTION_PROVIDER_VERSION:
                    row[i] = VERSION_NAME;
                    break;

                case COLUMN_ACTION_PROVIDER_VERSION_CODE:
                    row[i] = VERSION_CODE;
                    break;

                case COLUMN_ACTION_COLOR:
                    row[i] = actionValues.getAsInteger(columns[i]);
                    break;

                default:
                    row[i] = actionValues.getAsString(columns[i]);
                    break;
            }
        }
        return row;
    }

}