/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.forrestguice.support.app;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;

public class ActivityOptionsCompat extends androidx.core.app.ActivityOptionsCompat
{
    protected ActivityOptionsCompat() {}

    @NonNull
    public static ActivityOptionsCompat makeCustomAnimation(@NonNull Context context, int enterResId, int exitResId) {
        if (Build.VERSION.SDK_INT >= 16) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeCustomAnimation(context,
                    enterResId, exitResId));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeScaleUpAnimation(@NonNull View source, int startX, int startY, int startWidth, int startHeight) {
        if (Build.VERSION.SDK_INT >= 16) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeScaleUpAnimation(
                    source, startX, startY, startWidth, startHeight));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeClipRevealAnimation(@NonNull View source, int startX, int startY, int width, int height) {
        if (Build.VERSION.SDK_INT >= 23) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeClipRevealAnimation(
                    source, startX, startY, width, height));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeThumbnailScaleUpAnimation(@NonNull View source, @NonNull Bitmap thumbnail, int startX, int startY) {
        if (Build.VERSION.SDK_INT >= 16) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeThumbnailScaleUpAnimation(
                    source, thumbnail, startX, startY));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeSceneTransitionAnimation(@NonNull Activity activity, @NonNull View sharedElement, @NonNull String sharedElementName) {
        if (Build.VERSION.SDK_INT >= 21) {
            return new ActivityOptionsCompat.ActivityOptionsCompatImpl(ActivityOptions.makeSceneTransitionAnimation(
                    activity, sharedElement, sharedElementName));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static ActivityOptionsCompat makeSceneTransitionAnimation(@NonNull Activity activity, Pair<View, String>... sharedElements) {
        if (Build.VERSION.SDK_INT >= 21) {
            android.util.Pair<View, String>[] pairs = null;
            if (sharedElements != null) {
                pairs = new android.util.Pair[sharedElements.length];
                for (int i = 0; i < sharedElements.length; i++) {
                    pairs[i] = android.util.Pair.create(
                            sharedElements[i].first, sharedElements[i].second);
                }
            }
            return new ActivityOptionsCompat.ActivityOptionsCompatImpl(
                    ActivityOptions.makeSceneTransitionAnimation(activity, pairs));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeTaskLaunchBehind() {
        if (Build.VERSION.SDK_INT >= 21) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeTaskLaunchBehind());
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeBasic() {
        if (Build.VERSION.SDK_INT >= 23) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeBasic());
        }
        return new ActivityOptionsCompat();
    }

    @RequiresApi(16)
    private static class ActivityOptionsCompatImpl extends ActivityOptionsCompat
    {
        private final ActivityOptions mActivityOptions;

        ActivityOptionsCompatImpl(ActivityOptions activityOptions) {
            mActivityOptions = activityOptions;
        }

        @Override
        public Bundle toBundle() {
            return mActivityOptions.toBundle();
        }

        @Override
        public void update(@NonNull androidx.core.app.ActivityOptionsCompat otherOptions) {
            if (otherOptions instanceof ActivityOptionsCompatImpl) {
                ActivityOptionsCompat.ActivityOptionsCompatImpl otherImpl =
                        (ActivityOptionsCompatImpl) otherOptions;
                mActivityOptions.update(otherImpl.mActivityOptions);
            }
        }

        @Override
        public void requestUsageTimeReport(@NonNull PendingIntent receiver) {
            if (Build.VERSION.SDK_INT >= 23) {
                mActivityOptions.requestUsageTimeReport(receiver);
            }
        }

        @NonNull
        @Override
        public ActivityOptionsCompat setLaunchBounds(@Nullable Rect screenSpacePixelRect) {
            if (Build.VERSION.SDK_INT < 24) {
                return this;
            }
            return new ActivityOptionsCompat.ActivityOptionsCompatImpl(
                    mActivityOptions.setLaunchBounds(screenSpacePixelRect));
        }

        @Override
        public Rect getLaunchBounds() {
            if (Build.VERSION.SDK_INT < 24) {
                return null;
            }
            return mActivityOptions.getLaunchBounds();
        }
    }

}