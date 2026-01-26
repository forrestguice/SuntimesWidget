/**
    Copyright (C) 2014-2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;

import java.util.ArrayList;
import java.util.Arrays;

import static com.forrestguice.suntimeswidget.calculator.settings.SolarEvents.*;

/**
 * ArrayAdapter that displays SolarEvents items (with icon) as list or dropdown.
 */
public class SolarEventsAdapter extends ArrayAdapter<SolarEvents>
{
    private final Context context;
    private final ArrayList<SolarEvents> choices;
    private final boolean northward;

    public SolarEventsAdapter(Context context, ArrayList<SolarEvents> choices, boolean northward)
    {
        super(context, R.layout.layout_listitem_solarevent, choices);
        this.context = context;
        this.choices = choices;
        this.northward = northward;
    }

    public static int[] getIconDimen(Resources resources, SolarEvents event)
    {
        int width, height;
        switch (event)
        {
            case NEWMOON:
            case FULLMOON:
            case NOON: case MIDNIGHT:
                width = height = (int)resources.getDimension(R.dimen.sunIconLarge_width);
                break;

            case FIRSTQUARTER:
            case THIRDQUARTER:
                height = (int)resources.getDimension(R.dimen.sunIconLarge_width);
                width = height / 2;
                break;

            case CROSS_SPRING: case CROSS_SUMMER: case CROSS_AUTUMNAL: case CROSS_WINTER:
            case EQUINOX_SPRING: case SOLSTICE_SUMMER: case EQUINOX_AUTUMNAL: case SOLSTICE_WINTER:
                width = height = (int)resources.getDimension(R.dimen.sunIconLarge_width) / 2;
                break;

            default:
                width = (int)resources.getDimension(R.dimen.sunIconLarge_width);
                height = (int)resources.getDimension(R.dimen.sunIconLarge_height);
                break;
        }
        return new int[] {width, height};
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        return alarmItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
    {
        return alarmItemView(position, convertView, parent);
    }

    private View alarmItemView(int position, View convertView, @NonNull ViewGroup parent)
    {
        View view = convertView;
        if (view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.layout_listitem_solarevent, parent, false);
        }

        int[] iconAttr = { choices.get(position).getIcon(northward) };
        TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
        int def = R.drawable.ic_moon_rise;
        int iconResource = typedArray.getResourceId(0, def);
        typedArray.recycle();

        ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
        SolarEvents event = choices.get(position);
        adjustIcon(iconResource, icon, event);

        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(event.getLongDisplayString());

        return view;
    }

    public static void adjustIcon(int iconResource, ImageView icon, SolarEvents event)
    {
        adjustIcon(iconResource, icon, event, 8);
    }
    public static void adjustIcon(int iconResource, ImageView icon, SolarEvents event, int marginDp)
    {
        Resources resources = icon.getContext().getResources();
        int defWidth = (int)resources.getDimension(R.dimen.sunIconLarge_width);
        int[] dimen = getIconDimen(resources, event);

        ViewGroup.LayoutParams iconParams = icon.getLayoutParams();
        iconParams.width = dimen[0];
        iconParams.height = dimen[1];

        if (iconParams instanceof ViewGroup.MarginLayoutParams)
        {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) iconParams;
            float vertMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp, resources.getDisplayMetrics());
            float horizMargin = (vertMargin + (defWidth - dimen[0])) / 2f;
            params.setMargins((int)horizMargin, (int)vertMargin, (int)horizMargin, (int)vertMargin);
        }

        icon.setImageDrawable(null);
        icon.setBackgroundResource(iconResource);
    }

    public ArrayList<SolarEvents> getChoices() {
        return choices;
    }

    public static SolarEventsAdapter createAdapter(Context context, boolean northward)
    {
        ArrayList<SolarEvents> choices = new ArrayList<SolarEvents>(Arrays.asList(
                MORNING_ASTRONOMICAL, MORNING_NAUTICAL, MORNING_BLUE8, MORNING_CIVIL, MORNING_BLUE4,
                SUNRISE, MORNING_GOLDEN,
                NOON, EVENING_GOLDEN,
                SUNSET, EVENING_BLUE4, EVENING_CIVIL, EVENING_BLUE8, EVENING_NAUTICAL, EVENING_ASTRONOMICAL, MIDNIGHT,
                MOONRISE, MOONNOON, MOONSET, MOONNIGHT,
                NEWMOON, FIRSTQUARTER, FULLMOON, THIRDQUARTER,
                CROSS_SPRING, CROSS_SUMMER, CROSS_AUTUMNAL, CROSS_WINTER,
                EQUINOX_SPRING,  SOLSTICE_SUMMER,  EQUINOX_AUTUMNAL, SOLSTICE_WINTER
        ));
        return new SolarEventsAdapter(context, choices, northward);
    }
}
