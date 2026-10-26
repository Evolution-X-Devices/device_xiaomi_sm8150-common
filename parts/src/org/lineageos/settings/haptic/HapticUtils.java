/*
 * Copyright (C) 2022 The LineageOS Project
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
 * limitations under the License
 */

package org.lineageos.settings.haptic;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.preference.PreferenceManager;

import java.lang.Math;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.FileUtils;

public final class HapticUtils {

    public static final String PREF_LEVEL = "haptic_level_pref";
    public static final String PATH_LEVEL =
            "/sys/module/qti_haptics/parameters/vmax_mv_override";

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 3700;
    /** Upper end of the level shown in the UI, as a percentage of MAX_LEVEL. */
    public static final int MAX_PERCENT = 100;

    public static boolean isSupported() {
        return FileUtils.fileExists(PATH_LEVEL);
    }

    public static int getLevel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_LEVEL,
                context.getResources().getInteger(R.integer.haptic_level_default));
    }

    public static void saveLevel(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(PREF_LEVEL, value).apply();
    }

    public static void applyLevel(Context context, int value, boolean test) {
        if (FileUtils.fileExists(PATH_LEVEL)) {
            double level = value / 100.0 * (MAX_LEVEL - MIN_LEVEL) + MIN_LEVEL;
            int newValue = (int) Math.round(level);
            FileUtils.writeLine(PATH_LEVEL, String.valueOf(newValue));

            if (test) {
                Vibrator dev = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (dev.hasVibrator()) {
                    dev.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        }
    }

    public static void restoreLevel(Context context) {
        applyLevel(context, getLevel(context), false);
    }
}
