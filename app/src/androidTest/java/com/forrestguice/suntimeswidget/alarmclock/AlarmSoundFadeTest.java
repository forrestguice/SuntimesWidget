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

package com.forrestguice.suntimeswidget.alarmclock;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Build;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AlarmSoundFadeTest
{
    public Context mockContext;

    @Before
    public void init() {
        mockContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void test_VolumeShaper_linear()
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            VolumeShaper.Configuration.Builder config0 = new VolumeShaper.Configuration.Builder(VolumeShaper.Configuration.LINEAR_RAMP);
            config0.setDuration(30000);

            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(mockContext, RingtoneManager.TYPE_ALARM);
            test_VolumeShaper(uri, config0.build());
        }
    }

    @Test
    public void test_VolumeShaper_scurve()
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            VolumeShaper.Configuration.Builder config1 = new VolumeShaper.Configuration.Builder(VolumeShaper.Configuration.SCURVE_RAMP);
            config1.setDuration(30000);

            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(mockContext, RingtoneManager.TYPE_ALARM);
            test_VolumeShaper(uri, config1.build());
        }
    }

    @Test
    public void test_VolumeShaper_cubic()
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            VolumeShaper.Configuration.Builder config1 = new VolumeShaper.Configuration.Builder(VolumeShaper.Configuration.CUBIC_RAMP);
            config1.setDuration(30000);

            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(mockContext, RingtoneManager.TYPE_ALARM);
            test_VolumeShaper(uri, config1.build());
        }
    }

    @TargetApi(26)
    public void test_VolumeShaper(final Uri uri, final VolumeShaper.Configuration volumeShaperConfig)
    {
        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_ALARM);
        player.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
            {
                Log.e("TEST", "onError: MediaPlayer error " + what + " (" + extra + ")");
                return false;
            }
        });

        try
        {
            player.setDataSource(mockContext, uri);
            player.prepare();

            player.setLooping(true);
            if (Build.VERSION.SDK_INT >= 16) {
                player.setNextMediaPlayer(null);
            }

            AudioManager audioManager = (AudioManager) mockContext.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }

            VolumeShaper fadeVolume = player.createVolumeShaper(volumeShaperConfig);
            fadeVolume.apply(VolumeShaper.Operation.PLAY);
            player.start();
            Log.i("TEST", "startAlert: playing " + uri);

            long t0 = System.currentTimeMillis();
            //noinspection StatementWithEmptyBody
            while (System.currentTimeMillis() - t0 <= volumeShaperConfig.getDuration()) {
                //Log.i("TEST", "startAlert: still playing " + uri);
            }

        } catch (Exception e) {
            Assert.fail("failed to startAlert: " + e);
        }
    }

}
