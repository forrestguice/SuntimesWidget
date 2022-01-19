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

package com.forrestguice.suntimeswidget.map;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MapProjectionTest extends SuntimesActivityTestBase
{
    private Context mockContext;

    @Before
    public void setup() {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
    }

    @Test
    public void test_WorldMapEquiazimuthal2_toCartesian_40n100w()
    {
        double R = 1;
        double[] center = new double[] {40,-100};
        double[][][] testCoords = new double[][][] {{{-20, 100}}};
        double[][][] testResult = new double[][][] {{{-1.943713, 1.848154}}};

        double e = 1 * 6.0477621 * (Math.cos(Math.toRadians(40)) * Math.sin(Math.toRadians(-20)) -
                Math.sin(Math.toRadians(40)) * Math.cos(Math.toRadians(-20)) * Math.cos(Math.toRadians(200)));
        Log.d("DEBUG", "y should be " + e);

        test_WorldMapEquiazimuthal2_points(center, R, testCoords, testResult);
    }

    @Test
    public void test_WorldMapEquiazimuthal2_toCartesian_equatorial()
    {
        double[][] t0, t10, t50, t_50, t_50_, t80, t90;      // page 196, "Map Projections - A Working Manual, U.S. Geological Survey Professional Paper 1395" by by John P. Snyder
        double[][] r0, r10, r50, r_50, r_50_, r80, r90;

        // 0, 10, 80, 90 north, east
        t0 = new double[][]  {{0,0},       {0,10},            {0,20},            {0,30},            {0,40},            {0,50},            {0,60},            {0,70},            {0,80},            {0,90}};
        r0 = new double[][]  {{0,0},       {0.17453,0},       {0.34907,0},       {0.52360,0},       {0.69813,0},       {0.87266,0},       {1.04720,0},       {1.22173,0},       {1.39636,0},       {1.57080,0}};
        t10 = new double[][] {{10,0},      {10,10},           {10,20},           {10,30},           {10,40},           {10,50},           {10,60},           {10,70},           {10,80},           {10,90}};
        r10 = new double[][] {{0,0.17453}, {0.17275,0.17541}, {0.34546,0.17810}, {0.51807,0.18270}, {0.69054,0.18943}, {0.86278,0.19859}, {1.03472,0.21067}, {1.20620,0.22634}, {1.37704,0.24656}, {1.54693,0.27277}};
        t80 = new double[][] {{80,0},      {80,10},           {80,20},           {80,30},           {80,40},           {80,50},           {80,60},           {80,70},           {80,80},           {80,90}};
        r80 = new double[][] {{0,1.39626}, {0.04281,1.39829}, {0.08469,1.40434}, {0.12469,1.41435}, {0.16188,1.42823}, {0.19529,1.44581}, {0.22399,1.46686}, {0.24706,1.49104}, {0.26358,1.51792}, {0.27277,1.54693}};
        t90 = new double[][] {{90,0},      {90,10},           {90,20},           {90,30},           {90,40},           {90,50},           {90,60},           {90,70},           {90,80},           {90,90}};
        r90 = new double[][] {{0,1.57080}, {0,1.57080},       {0,1.57080},       {0,1.57080},       {0,1.57080},       {0,1.57080},       {0,1.57080},       {0,1.57080},       {0,1.57080},       {0,1.57080}};
        // 50 north, east
        t50 = new double[][]   {{50,0},        {50,10},             {50,20},             {50,30},             {50,40},             {50,50},             {50,60},             {50,70},             {50,80},             {50,90}};
        r50 = new double[][]   {{0,0.87266},   {0.12765,0.87609},   {0.25441,0.88647},   {0.37931,0.90408},   {0.50127,0.92938},   {0.61904,0.96306},   {0.73106,1.00602},   {0.83535,1.05942},   {0.92935,1.12464},   {1.00969,1.20330}};
        // 50 north, west
        //t50 = new double[][]   {{50,0},        {50,-10},            {50,-20},            {50,-30},            {50,-40},            {50,-50},            {50,-60},            {50,-70},            {50,-80},            {50,-90}};
        //r50 = new double[][]   {{0,0.87266},   {-0.12765,0.87609},  {-0.25441,0.88647},  {-0.37931,0.90408},  {-0.50127,0.92938},  {-0.61904,0.96306},  {-0.73106,1.00602},  {-0.83535,1.05942},  {-0.92935,1.12464},  {-1.00969,1.20330}};
        // 50 south, east
        t_50 = new double[][]  {{-50,0},       {-50,10},            {-50,20},            {-50,30},            {-50,40},            {-50,50},            {-50,60},            {-50,70},            {-50,80},            {-50,90}};
        r_50 = new double[][]  {{0,-0.87266},  {0.12765,-0.87609},  {0.25441,-0.88647},  {0.37931,-0.90408},  {0.50127,-0.92938},  {0.61904,-0.96306},  {0.73106,-1.00602},  {0.83535,-1.05942},  {0.92935,-1.12464},  {1.00969,-1.20330}};
        // 50 south, west
        t_50_ = new double[][] {{-50,0},       {-50,-10},           {-50,-20},           {-50,-30},           {-50,-40},           {-50,-50},           {-50,-60},           {-50,-70},           {-50,-80},           {-50,-90}};
        r_50_ = new double[][] {{-0,-0.87266}, {-0.12765,-0.87609}, {-0.25441,-0.88647}, {-0.37931,-0.90408}, {-0.50127,-0.92938}, {-0.61904,-0.96306}, {-0.73106,-1.00602}, {-0.83535,-1.05942}, {-0.92935,-1.12464}, {-1.00969,-1.20330}};

        double[] center = new double[] {0,0};
        double[][][] testCoords = new double[][][] { t0, t10, t50, t_50, t_50_, t80, t90 };
        double[][][] testResult = new double[][][] { r0, r10, r50, r_50, r_50_, r80, r90 };
        test_WorldMapEquiazimuthal2_points(center, 1, testCoords, testResult);

        test_WorldMapEquiazimuthal2_points(center, 1, new double[][][] {{{0,0}}}, new double[][][] {{{0,0}}});
        test_WorldMapEquiazimuthal2_points(center, 1, new double[][][] {{{0,180}}}, new double[][][] {{{Math.PI,0}}});
        test_WorldMapEquiazimuthal2_points(center, 1, new double[][][] {{{0,-180}}}, new double[][][] {{{-Math.PI,0}}});
        test_WorldMapEquiazimuthal2_points(center, 1, new double[][][] {{{90,0}}}, new double[][][] {{{0,Math.PI/2}}});
        test_WorldMapEquiazimuthal2_points(center, 1, new double[][][] {{{-90,0}}}, new double[][][] {{{0,-Math.PI/2}}});
        //test_WorldMapEquiazimuthal2_points(center, new double[][][] {{{-90,-180}}}, new double[][][] {{{0,-Math.PI/2}}});          // TODO: edge case
        //test_WorldMapEquiazimuthal2_points(center, new double[][][] {{{-90,180}}}, new double[][][] {{{Math.PI/2,-Math.PI/2}}});   // TODO: edge case
    }

    protected void test_WorldMapEquiazimuthal2_points(double[] center, double R, double[][][] testCoords, double[][][] testResult)
    {
        WorldMapTask.WorldMapOptions options = new WorldMapTask.WorldMapOptions();
        options.map = null;
        options.map_night = null;
        options.hasTransparentBaseMap = true;
        options.center = center;

        WorldMapEquiazimuthal2 projection = new WorldMapEquiazimuthal2();
        projection.center = options.center;

        double[] point, coords;
        for (int i=0; i<testCoords.length; i++)
        {
            for (int j=0; j<testCoords[i].length; j++)
            {
                coords = testCoords[i][j];
                point = projection.toCartesian(coords[0], coords[1], R);
                String tag = "["+coords[0]+","+coords[1]+ "]";
                testPointEquals(point, testResult[i][j], tag);
                Log.d("RESULT", "[lat, lon] = " + coords[0] + "," + coords[1] + ", [x, y] = " + point[0] + "," + point[1]);
            }
        }
    }

    protected void testPointEquals( double[] point, double[] expected, String tag )
    {
        assertEquals(tag, expected[0], point[0], 0.0001);
        assertEquals(tag, expected[1], point[1], 0.0001);
    }

}
