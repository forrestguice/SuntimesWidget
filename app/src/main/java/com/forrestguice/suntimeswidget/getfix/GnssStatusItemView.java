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
package com.forrestguice.suntimeswidget.getfix;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

import java.util.Locale;

public class GnssStatusItemView extends FrameLayout
{
    public static final String TAG = "GpsStatusView";

    protected ViewHolder holder;
    protected int id = -1;
    protected int constellation = -1;

    public GnssStatusItemView(Context context) {
        super(context);
        init(context);
    }
    public GnssStatusItemView(Context context, AttributeSet attr) {
        super(context, attr);
        init(context);
    }

    public void init(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(ViewHolder.getLayoutResID(), this);
        holder = new ViewHolder(context, this);
        updateViews(context, null);
    }

    public void updateViews(Context context, GnssStatusView.SatelliteItem item)
    {
        id = (item != null ? item.id : -1);
        constellation = (item != null ? item.constellation : -1);

        if (holder != null) {
            holder.updateViews(context, item);
        }
    }

    /**
     * ViewHolder; wrap this class to use as RecyclerView.ViewHolder
     */
    public static class ViewHolder
    {
        public static int getLayoutResID() {
            return R.layout.layout_view_gpsstatus_item;
        }

        private static final SuntimesUtils utils = new SuntimesUtils();

        public TextView text_label;
        public TextView text_cnr;
        public TextView text_position;
        public CheckBox check_used;
        public CheckBox check_signal;
        public CheckBox check_almanac;
        public CheckBox check_ephemeris;

        public ViewHolder(Context context, @NonNull View itemView)
        {
            text_label = (TextView) itemView.findViewById(R.id.text_satellite_label);
            text_cnr = (TextView) itemView.findViewById(R.id.text_satellite_cnr);
            text_position = (TextView) itemView.findViewById(R.id.text_satellite_position);
            check_used = (CheckBox) itemView.findViewById(R.id.check_satellite_usedInFix);
            check_signal = (CheckBox) itemView.findViewById(R.id.check_satellite_signal);
            check_almanac = (CheckBox) itemView.findViewById(R.id.check_satellite_almanac);
            check_ephemeris = (CheckBox) itemView.findViewById(R.id.check_satellite_ephemeris);
        }

        public void updateViews(Context context, @Nullable GnssStatusView.SatelliteItem item)
        {
            if (text_label != null) {
                text_label.setText(item != null ? context.getString(R.string.configLabel_getFix_gnss_labelFormat,
                        GpsDebugDisplay.constellationTypeLabel(item.constellation, false), item.id + "")
                        : "");
            }
            if (text_cnr != null) {
                text_cnr.setText(item != null ? context.getString(R.string.configLabel_getFix_gnss_cnrFormat,
                        String.format(Locale.getDefault(), "%.2f", item.cnr))
                        : "");
            }
            if (text_position != null) {
                text_position.setText(item != null ? context.getString(R.string.configLabel_getFix_gnss_positionFormat,
                        utils.formatAsDirection(item.azimuth, 2),
                        utils.formatAsElevation(item.elevation, 2).toString())
                        : "");
                text_position.setVisibility((item != null && item.azimuth != 0 && item.elevation != 0) ? View.VISIBLE : View.GONE);
            }
            if (check_used != null) {
                check_used.setOnCheckedChangeListener(null);
                check_used.setChecked(item != null && item.usedInFix);
                check_used.setOnCheckedChangeListener(onCheckedChanged);
            }
            if (check_signal != null) {
                check_signal.setOnCheckedChangeListener(null);
                check_signal.setChecked(item != null && item.cnr != 0);
                check_signal.setOnCheckedChangeListener(onCheckedChanged);
            }
            if (check_ephemeris != null) {
                check_ephemeris.setOnCheckedChangeListener(null);
                check_ephemeris.setChecked(item != null && item.hasEphemeris);
                check_ephemeris.setOnCheckedChangeListener(onCheckedChanged);
            }
            if (check_almanac != null) {
                check_almanac.setOnCheckedChangeListener(null);
                check_almanac.setChecked(item != null && item.hasAlmanac);
                check_almanac.setOnCheckedChangeListener(onCheckedChanged);
            }
        }

        protected CompoundButton.OnCheckedChangeListener onCheckedChanged = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(!isChecked);    // disallow changing checked state
            }
        };
    }
}
