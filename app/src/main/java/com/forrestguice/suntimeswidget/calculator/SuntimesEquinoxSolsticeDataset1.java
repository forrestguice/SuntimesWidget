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

package com.forrestguice.suntimeswidget.calculator;

import android.content.Context;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

/**
 * An extended version of SuntimesEquinoxSolsticeDataset that includes the cross-quarter days.
 */
public class SuntimesEquinoxSolsticeDataset1 extends SuntimesEquinoxSolsticeDataset
{
    public SuntimesEquinoxSolsticeData dataCrossSpring;
    public SuntimesEquinoxSolsticeData dataCrossSummer;
    public SuntimesEquinoxSolsticeData dataCrossAutumnal;
    public SuntimesEquinoxSolsticeData dataCrossWinter;
    public SuntimesEquinoxSolsticeData[] dataCrossQuarterDays;

    public SuntimesEquinoxSolsticeDataset1(Context context)
    {
        super(context);
        initCrossQuarterData();
    }

    public SuntimesEquinoxSolsticeDataset1(Context context, int appWidgetId)
    {
        super(context, appWidgetId);
        initCrossQuarterData();
    }

    protected void initCrossQuarterData()
    {
        dataCrossSpring = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataCrossSpring.setTimeMode(WidgetSettings.SolsticeEquinoxMode.CROSS_SPRING);

        dataCrossSummer = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataCrossSummer.setTimeMode(WidgetSettings.SolsticeEquinoxMode.CROSS_SUMMER);

        dataCrossAutumnal = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataCrossAutumnal.setTimeMode(WidgetSettings.SolsticeEquinoxMode.CROSS_AUTUMN);

        dataCrossWinter = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataCrossWinter.setTimeMode(WidgetSettings.SolsticeEquinoxMode.CROSS_WINTER);

        dataCrossQuarterDays = new SuntimesEquinoxSolsticeData[] { dataCrossSpring, dataCrossSummer, dataCrossAutumnal, dataCrossWinter };
    }

    @Override
    public void calculateData(Context context)
    {
        super.calculateData(context);
        SuntimesCalculator calculator = dataEquinoxSpring.calculator();
        SuntimesCalculatorDescriptor descriptor = dataEquinoxSpring.calculatorMode();
        for (SuntimesEquinoxSolsticeData data : dataCrossQuarterDays)
        {
            data.setCalculator(calculator, descriptor);
            data.calculate(context);
        }
    }

    @Override
    public SuntimesEquinoxSolsticeData[] dataset()
    {
        return new SuntimesEquinoxSolsticeData[] {
                dataCrossSpring, dataEquinoxSpring,
                dataCrossSummer, dataSolsticeSummer,
                dataCrossAutumnal, dataEquinoxAutumnal,
                dataCrossWinter, dataSolsticeWinter };
    }

    @Override
    public void setLocation(Location location)
    {
        super.setLocation(location);
        for (SuntimesEquinoxSolsticeData data : dataCrossQuarterDays) {
            data.setLocation(location);
        }
    }

    @Override
    public void invalidateCalculation()
    {
        super.invalidateCalculation();
        for (SuntimesEquinoxSolsticeData data : dataCrossQuarterDays) {
            data.invalidateCalculation();
        }
    }

    @Override
    public void setTodayIs(Calendar date)
    {
        super.setTodayIs(date);
        for (SuntimesEquinoxSolsticeData data : dataCrossQuarterDays) {
            data.setTodayIs(date);
        }
    }
}
