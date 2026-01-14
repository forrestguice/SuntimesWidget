/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.tiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.widgets.SuntimesConfigActivity0;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData2;
import com.forrestguice.suntimes.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.support.app.AlertDialog;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @see SuntimesTileService
 * @see SuntimesTileActivity
 */
@SuppressWarnings("Convert2Diamond")
public abstract class SuntimesTileBase
{
    protected abstract int appWidgetId();

    @Nullable
    protected abstract Intent getConfigIntent(Context context);

    @Nullable
    protected abstract Intent getLaunchIntent(Context context);

    @Nullable
    protected abstract Intent getLockScreenIntent(Context context);

    @Nullable
    protected abstract Drawable getDialogIcon(Context context);

    @Nullable
    protected abstract CharSequence formatDialogTitle(Context context);

    @Nullable
    protected abstract CharSequence formatDialogMessage(Context context);

    protected WeakReference<Activity> activityRef;
    protected TextView dialogView_title, dialogView_message;

    public SuntimesTileBase(@Nullable Activity activity)
    {
        super();
        activityRef = new WeakReference<>(activity);
    }

    protected void initDefaults(Context context) {}

    protected LayoutInflater getLayoutInflater(Context context) {
        return (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    protected void updateViews(Context context, TextView titleView, TextView messageView)
    {
        if (titleView != null) {
            titleView.setText(formatDialogTitle(context));
        }
        if (messageView != null) {
            messageView.setText(formatDialogMessage(context));
        }
    }

    @Nullable
    protected Dialog createDialog(final Context context)
    {
        SuntimesUtils.initDisplayStrings(context);

        @SuppressLint("InflateParams")
        View view = getLayoutInflater(context).inflate(R.layout.layout_dialog_tile, null);
        dialogView_title = view.findViewById(android.R.id.title);
        dialogView_message = view.findViewById(android.R.id.message);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.LockScreenDialogStyle);
        dialog.setView(view);

        Drawable icon = getDialogIcon(context);
        ImageView iconView = view.findViewById(android.R.id.icon);
        if (iconView != null)
        {
            if (icon != null) {
                iconView.setImageDrawable(icon);
            }
            iconView.setVisibility(icon != null ? View.VISIBLE : View.GONE);

        } else if (icon != null) {
            dialog.setIcon(icon);
        }

        ImageButton settingsButton = view.findViewById(R.id.button_settings);
        final Intent configIntent = getConfigIntent(context);
        if (configIntent != null && settingsButton == null) {
            dialog.setNeutralButton(context.getString(R.string.configAction_settings), null);
        }

        final Intent launchIntent = getLaunchIntent(context);
        if (launchIntent != null) {
            dialog.setPositiveButton(getLaunchIntentTitle(context), null);
        }

        final WeakReference<Context> contextRef = new WeakReference<>(context);
        final Dialog d = dialog.create();
        d.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                startUpdateTask(context, d);

                Button settingsButton = AlertDialog.getButton(dialog, AlertDialog.BUTTON_NEUTRAL);
                if (settingsButton != null) {
                    settingsButton.setOnClickListener(onActionClickListener(activityRef, contextRef, d, configIntent));
                }

                Button launchButton = AlertDialog.getButton(dialog, AlertDialog.BUTTON_POSITIVE);
                if (launchButton != null) {
                    launchButton.setOnClickListener(onActionClickListener(activityRef, contextRef, d, launchIntent));
                }
            }
        });

        d.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopUpdateTask();
                Activity activity = activityRef.get();
                if (activity != null) {
                    activity.finish();
                }
            }
        });

        if (settingsButton != null)
        {
            settingsButton.setVisibility(configIntent != null ? View.VISIBLE : View.GONE);
            if (configIntent != null) {
                settingsButton.setOnClickListener(onActionClickListener(activityRef, contextRef, d, configIntent));
            }
        }

        refreshUpdateTaskViews(context, d);
        return d;
    }

    private View.OnClickListener onActionClickListener(final WeakReference<Activity> activityRef, final WeakReference<Context> contextRef, final Dialog d, final Intent intent)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                unlockAndRun(activityRef.get(), new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (intent != null)
                        {
                            Context context = contextRef.get();
                            if (context != null) {
                                context.startActivity(intent);
                            }
                            d.dismiss();
                        }
                    }
                });
            }
        };
    }

    /**
     * When the activity is non-null and the device is locked, unlock it first and run r; otherwise just run r.
     * @param activity may be null
     * @param r to be run afterward
     */
    protected void unlockAndRun(final Activity activity, final Runnable r)
    {
        if (activity != null)
        {
            KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null && isKeyguardSecure(keyguardManager)) {
                requestDismissKeyguard(activity, keyguardManager, r);

            } else {
                r.run();
            }
        } else {
            r.run();
        }
    }

    protected void requestDismissKeyguard(@NonNull Activity activity, @NonNull KeyguardManager keyguardManager, @Nullable final Runnable r)
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            keyguardManager.requestDismissKeyguard(activity, new KeyguardManager.KeyguardDismissCallback()
            {
                @Override
                public void onDismissSucceeded() {
                    if (r != null) {
                        r.run();
                    }
                }
            });

        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            if (r != null) {
                r.run();  // TODO: run only on success; how?
            }
        }
    }

    protected boolean isKeyguardSecure(@NonNull KeyguardManager keyguardManager)
    {
        if (Build.VERSION.SDK_INT >= 16) {
            return keyguardManager.isKeyguardSecure();
        } else return false;
    }

    @NonNull
    protected String getLaunchIntentTitle(Context context) {
        String title = WidgetActions.loadActionLaunchPref(context, appWidgetId(), null, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
        return (title != null ? title : context.getString(R.string.app_name));
    }

    private Handler handler;
    @Nullable
    private Runnable updateTask = null;
    protected final Runnable updateTask(final WeakReference<Context> contextRef, final Dialog dialog)
    {
        return new Runnable() {
            @Override
            public void run()
            {
                Context context = contextRef.get();
                if (context!= null)
                {
                    refreshUpdateTaskViews(context, dialog);
                    handler.postDelayed(this, updateTaskRateMs());
                }
            }
        };
    }
    public static final int UPDATE_RATE = 3000;     // update rate: 3s
    public int updateTaskRateMs() {
        return UPDATE_RATE;
    }

    protected void startUpdateTask(Context context, Dialog dialog)
    {
        //Log.d("DEBUG", "startUpdateTask");
        if (handler == null) {
            handler = new Handler();
        }
        if (updateTask != null) {
            stopUpdateTask();
        }
        updateTask = updateTask(new WeakReference<Context>(context), dialog);
        handler.postDelayed(updateTask, updateTaskRateMs());
    }

    protected void stopUpdateTask()
    {
        //Log.d("DEBUG", "stopUpdateTask");
        if (handler != null && updateTask != null) {
            handler.removeCallbacks(updateTask);
            updateTask = null;
        }
    }

    protected void refreshUpdateTaskViews(Context context, @Nullable Dialog dialog) {
        updateViews(context, dialogView_title, dialogView_message);
    }

    protected Calendar now(Context context) {
        return Calendar.getInstance(timezone(context));
    }

    protected TimeZone timezone(Context context)
    {
        initData(context);
        return (data != null ? data.timezone() : WidgetTimezones.localMeanTime(location(context)));
    }

    public static boolean isLocalTime(String tzID) {
        return TimeZones.LocalMeanTime.TIMEZONEID.equals(tzID) || TimeZones.ApparentSolarTime.TIMEZONEID.equals(tzID)
                || TimeZones.SiderealTime.TZID_LMST.equalsIgnoreCase(tzID);
    }

    protected Location location(Context context) {
        return WidgetSettings.loadLocationPref(context, appWidgetId());
    }

    protected SuntimesRiseSetData2 initData(Context context) {
        return initData(context, false);
    }
    protected SuntimesRiseSetData2 initData(Context context, boolean replace)
    {
        if (data == null || replace) {
            data = new SuntimesRiseSetData2(context, appWidgetId());
            data.calculate(context);
        }
        return data;
    }
    protected SuntimesRiseSetData2 data = null;

    @Nullable
    public static <T extends Class<?>> Intent getConfigIntent(Context context, int appWidgetId, T configClass)
    {
        if (configClass != null)
        {
            Intent intent = new Intent(context, configClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(SuntimesConfigActivity0.EXTRA_RECONFIGURE, true);
            return intent;
        } else return null;
    }

    @Nullable
    protected static Intent getLaunchIntent(Context context, int appWidgetId, SuntimesRiseSetData2 data)
    {
        Intent intent = WidgetActions.createIntent(context.getApplicationContext(), appWidgetId, null, data, SuntimesActivity.class);
        if (intent != null) {
            return intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else return AlarmNotifications.getSuntimesIntent(context);
    }

}
