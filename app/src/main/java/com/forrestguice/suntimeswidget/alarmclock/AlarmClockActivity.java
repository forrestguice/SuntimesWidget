/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.AboutDialog;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

public class AlarmClockActivity extends AppCompatActivity
{
    public static final String EXTRA_SHOWHOME = "showHome";

    private static final String DIALOGTAG_HELP = "help";
    private static final String DIALOGTAG_ABOUT = "about";

    private static final String KEY_LISTVIEW_TOP = "alarmlisttop";
    private static final String KEY_LISTVIEW_INDEX = "alarmlistindex";

    private ActionBar actionBar;
    private ListView alarmList;
    private FloatingActionButton actionButton;

    private AlarmClockListTask updateTask = null;
    private static final SuntimesUtils utils = new SuntimesUtils();

    public AlarmClockActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    /**
     * OnCreate: the Activity initially created
     * @param icicle a Bundle containing saved state
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
        SuntimesUtils.initDisplayStrings(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_alarmclock);
        initViews(this);
    }

    /**
     * OnStart: the Activity becomes visible
     */
    @Override
    public void onStart()
    {
        super.onStart();
        updateViews(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        saveListViewPosition(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        restoreListViewPosition(savedState);
    }

    /**
     * ..based on stack overflow answer by ian
     * https://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */
    private void saveListViewPosition( Bundle outState)
    {
        int i = alarmList.getFirstVisiblePosition();
        outState.putInt(KEY_LISTVIEW_INDEX, i);

        int top = 0;
        View firstItem = alarmList.getChildAt(0);
        if (firstItem != null)
        {
            top = firstItem.getTop() - alarmList.getPaddingTop();
        }
        outState.putInt(KEY_LISTVIEW_TOP, top);
    }

    private void restoreListViewPosition(@NonNull Bundle savedState )
    {
        int i = savedState.getInt(KEY_LISTVIEW_INDEX, -1);
        if (i >= 0)
        {
            int top = savedState.getInt(KEY_LISTVIEW_TOP, 0);
            alarmList.setSelectionFromTop(i, top);
        }
    }

    /**
     * initialize ui/views
     * @param context a context used to access resources
     */
    protected void initViews(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();

        boolean showHome = getIntent().getBooleanExtra(EXTRA_SHOWHOME, false);
        if (actionBar != null && showHome)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        actionButton = (FloatingActionButton) findViewById(R.id.btn_addAlarm);
        actionButton.setOnClickListener(onActionButtonClick);

        alarmList = (ListView)findViewById(R.id.alarmList);
        View emptyView = findViewById(android.R.id.empty);
        emptyView.setOnClickListener(onEmptyViewClick);
        alarmList.setEmptyView(emptyView);
    }

    /**
     * onActionButtonClick
     */
    private View.OnClickListener onActionButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            AlarmClockItem alarm = new AlarmClockItem();
            alarm.label = "alarm label";
            alarm.location = new WidgetSettings.Location("Location", "33.4500", "-111.9400", "385");
            alarm.event = SolarEvents.SUNRISE;
            alarm.enabled = true;
            alarm.vibrate = true;
            alarm.repeating = true;

            AlarmClockUpdateTask task = new AlarmClockUpdateTask(AlarmClockActivity.this, true);
            task.setTaskListener(new AlarmClockUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result)
                {
                    if (result) {
                        updateViews(AlarmClockActivity.this);
                    }
                }
            });
            task.execute(alarm);
        }
    };

    /**
     * onEmptyViewClick
     */
    private View.OnClickListener onEmptyViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    };

    /**
     * updateViews
     * @param context context
     */
    protected void updateViews(Context context)
    {
        if (updateTask != null) {
            updateTask.cancel(true);
            updateTask = null;
        }

        updateTask = new AlarmClockListTask(this, alarmList);
        updateTask.execute();
    }

    /**
     * showHelp
     */
    protected void showHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(getString(R.string.help_alarmclock));
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    /**
     * showAbout
     */
    protected void showAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
    }

    /**
     * AlarmClockUpdateTask
     */
    public static class AlarmClockUpdateTask extends AsyncTask<AlarmClockItem, Void, Boolean>
    {
        protected AlarmClockDatabaseAdapter db;
        private boolean flag_add = false;

        public AlarmClockUpdateTask(Context context)
        {
            db = new AlarmClockDatabaseAdapter(context.getApplicationContext());
        }

        public AlarmClockUpdateTask(Context context, boolean flag_add)
        {
            db = new AlarmClockDatabaseAdapter(context.getApplicationContext());
            this.flag_add = flag_add;
        }

        @Override
        protected Boolean doInBackground(AlarmClockItem... items)
        {
            db.open();
            boolean updated = true;
            for (AlarmClockItem item : items) {
                updated = updated && ((flag_add
                        ? (db.addAlarm(item.asContentValues(false)) > 0)
                        : (db.updateAlarm(item.rowID, item.asContentValues(false)))));
            }
            db.close();
            return updated;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (listener != null)
                listener.onFinished(result);
        }

        protected AlarmClockUpdateTaskListener listener = null;
        public void setTaskListener( AlarmClockUpdateTaskListener l )
        {
            listener = l;
        }

        public static abstract class AlarmClockUpdateTaskListener
        {
            public void onFinished(Boolean result) {}
        }
    }

    /**
     * AlarmClockDeleteTask
     */
    public static class AlarmClockDeleteTask extends AsyncTask<Long, Void, Boolean>
    {
        protected AlarmClockDatabaseAdapter db;

        public AlarmClockDeleteTask(Context context)
        {
            db = new AlarmClockDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected Boolean doInBackground(Long... rowIDs)
        {
            db.open();
            boolean removed = true;
            for (long rowID : rowIDs) {
                removed = removed && db.removeAlarm(rowID);
            }
            db.close();
            return removed;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (listener != null)
                listener.onFinished(result);
        }

        protected AlarmClockDeleteTaskListener listener = null;
        public void setTaskListener( AlarmClockDeleteTaskListener l )
        {
            listener = l;
        }

        public static abstract class AlarmClockDeleteTaskListener
        {
            public void onFinished(Boolean result) {}
        }
    }

    /**
     * AlarmClockListTask
     */
    public static class AlarmClockListTask extends AsyncTask<String, Void, AlarmClockAdapter>
    {
        private AlarmClockDatabaseAdapter db;
        private WeakReference<Context> contextRef;
        private WeakReference<ListView> alarmListRef;

        public AlarmClockListTask(Context context, ListView list)
        {
            contextRef = new WeakReference<>(context);
            db = new AlarmClockDatabaseAdapter(context.getApplicationContext());
            alarmListRef = new WeakReference<>(list);
        }

        @Override
        protected AlarmClockAdapter doInBackground(String... strings)
        {
            ArrayList<AlarmClockItem> items = new ArrayList<>();
            db.open();
            Cursor cursor = db.getAllAlarms(0, true);
            while (!cursor.isAfterLast())
            {
                ContentValues entryValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, entryValues);
                items.add(new AlarmClockItem(entryValues));
                cursor.moveToNext();
            }
            db.close();

            Context context = contextRef.get();
            if (context != null)
                return new AlarmClockAdapter(context, items);
            else return null;
        }

        @Override
        protected void onPostExecute(AlarmClockAdapter result)
        {
            if (result != null)
            {
                ListView alarmList = alarmListRef.get();
                if (alarmList != null) {
                    alarmList.setAdapter(result);
                }
            }
        }
    }

    /**
     * AlarmClockAdapter
     */
    @SuppressWarnings("Convert2Diamond")
    public static class AlarmClockAdapter extends ArrayAdapter<AlarmClockItem>
    {
        private Context context;
        private ArrayList<AlarmClockItem> items;

        private int alarmEnabledColor, alarmDisabledColor;

        @SuppressLint("ResourceType")
        public AlarmClockAdapter(Context context, ArrayList<AlarmClockItem> items)
        {
            super(context, R.layout.layout_listitem_alarmclock, items);
            this.context = context;
            this.items = items;

            int[] attrs = { R.attr.alarmCardEnabled, R.attr.alarmCardDisabled};
            TypedArray a = context.obtainStyledAttributes(attrs);
            alarmEnabledColor = ContextCompat.getColor(context, a.getResourceId(0, R.color.alarm_enabled_dark));
            alarmDisabledColor = ContextCompat.getColor(context, a.getResourceId(1, R.color.alarm_disabled_dark));
            a.recycle();
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return itemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return itemView(position, convertView, parent);
        }

        private View itemView(int position, View convertView, @NonNull final ViewGroup parent)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(R.layout.layout_listitem_alarmclock, parent, false);  // always re-inflate (ignore convertView)
            final AlarmClockItem item = items.get(position);

            //ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            //icon.setImageResource(item.icon);

            final View card = view.findViewById(R.id.layout_alarmcard);
            if (card != null)
            {
                card.setBackgroundColor(item.enabled ? alarmEnabledColor : alarmDisabledColor);
            }

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            if (text != null) {
                text.setText(item.label);
            }

            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            if (text2 != null) {
                text2.setText(item.event != null ? item.event.getLongDisplayString() : "Clock Time");
            }

            TextView text_datetime = (TextView) view.findViewById(R.id.text_datetime);
            if (text_datetime != null) {
                Calendar alarmTime = Calendar.getInstance();
                alarmTime.setTimeInMillis(item.timestamp);
                text_datetime.setText(utils.calendarTimeShortDisplayString(context, alarmTime).getValue());
            }

            TextView text_location = (TextView) view.findViewById(R.id.text_location_label);
            if (text_location != null)
            {
                if (item.location != null)
                {
                    String coordString = context.getString(R.string.location_format_latlon, item.location.getLatitude(), item.location.getLongitude());
                    String labelString = item.location.getLabel();
                    String displayString = labelString + "\n" + coordString;
                    SpannableString displayText = SuntimesUtils.createBoldSpan(null, displayString, labelString);
                    displayText = SuntimesUtils.createRelativeSpan(displayText, displayString, coordString, 0.75f);

                    text_location.setText(displayText);
                    text_location.setVisibility(View.VISIBLE);

                } else {
                    text_location.setVisibility(View.INVISIBLE);
                }
            }

            Switch switch_enabled = (Switch) view.findViewById(R.id.switch_enabled);
            if (switch_enabled != null)
            {
                switch_enabled.setChecked(item.enabled);
                switch_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        enableAlarm(item, card, isChecked);
                    }
                });
            }

            TextView text_ringtone = (TextView) view.findViewById(R.id.text_ringtone);
            if (text_ringtone != null)
            {
                text_ringtone.setText(item.ringtone != null ? item.ringtone : "none");  // TODO
            }

            CheckBox check_vibrate = (CheckBox) view.findViewById(R.id.check_vibrate);
            if (check_vibrate != null)
            {
                check_vibrate.setChecked(item.vibrate);
                check_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        item.vibrate = isChecked;
                        item.modified = true;
                        onAlarmModified(item);
                    }
                });
            }

            CheckBox check_repeat = (CheckBox) view.findViewById(R.id.check_repeat);
            if (check_repeat != null)
            {
                check_repeat.setChecked(item.repeating);
                check_repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        item.repeating = isChecked;
                        item.modified = true;
                        onAlarmModified(item);
                    }
                });
            }

            ImageButton overflow = (ImageButton) view.findViewById(R.id.overflow_menu);
            if (overflow != null)
            {
                overflow.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {

                        showOverflowMenu(item, v, view);
                    }
                });
            }

            return view;
        }


        /**
         * @param item associated AlarmClockItem
         * @param buttonView button that triggered menu
         * @param itemView view associated with item
         */
        protected void showOverflowMenu(final AlarmClockItem item, final View buttonView, final View itemView)
        {
            PopupMenu menu = new PopupMenu(context, buttonView);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.alarmcontext, menu.getMenu());

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    switch (menuItem.getItemId())
                    {
                        case R.id.deleteAlarm:
                            deleteAlarm(item, itemView);
                            return true;

                        default:
                            return false;
                    }
                }
            });

            SuntimesUtils.forceActionBarIcons(menu.getMenu());
            menu.show();
        }

        /**
         * onAlarmModified
         * @param item AlarmClockItem
         * @return true modifications were saved
         */
        protected void onAlarmModified(final AlarmClockItem item)
        {
            if (item.modified)
            {
                AlarmClockUpdateTask task = new AlarmClockUpdateTask(context);
                task.execute(item);
            }
        }

        /**
         * enableAlarm
         * @param item AlarmClockItem
         * @param enabled enabled/disabled
         */
        protected void enableAlarm(final AlarmClockItem item, View itemView, boolean enabled)
        {
            item.enabled = enabled;
            item.modified = true;
            onAlarmModified(item);

            itemView.setBackgroundColor(enabled ? alarmEnabledColor : alarmDisabledColor);
            if (enabled)
            {
                Toast msg = Toast.makeText(context, "alarm " + item.rowID + " enabled", Toast.LENGTH_SHORT);  // TODO
                msg.show();
            }
        }

        /**
         * deleteAlarm
         * @param item AlarmClockItem
         */
        protected void deleteAlarm(final AlarmClockItem item, final View itemView)
        {
            AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.deletealarm_dialog_title))
                    .setMessage(context.getString(R.string.deletealarm_dialog_message, item.rowID + ""))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(context.getString(R.string.deletealarm_dialog_ok), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            AlarmClockDeleteTask deleteTask = new AlarmClockDeleteTask(context);
                            deleteTask.setTaskListener(new AlarmClockDeleteTask.AlarmClockDeleteTaskListener()
                            {
                                @Override
                                public void onFinished(Boolean result)
                                {
                                    if (result)
                                    {
                                        final Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
                                        animation.setAnimationListener(new Animation.AnimationListener()
                                        {
                                            @Override
                                            public void onAnimationStart(Animation animation) {}
                                            @Override
                                            public void onAnimationRepeat(Animation animation) {}
                                            @Override
                                            public void onAnimationEnd(Animation animation)
                                            {
                                                items.remove(item);
                                                notifyDataSetChanged();
                                                Toast.makeText(context, context.getString(R.string.deletealarm_toast_success, item.rowID + ""), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        itemView.startAnimation(animation);
                                    }
                                }
                            });
                            deleteTask.execute(item.rowID);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.deletealarm_dialog_cancel), null);
            confirm.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarmclock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_help:
                showHelp();
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        SuntimesUtils.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

}
