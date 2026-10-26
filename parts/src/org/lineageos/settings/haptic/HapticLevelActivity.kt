/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.haptic

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.lineageos.settings.R
import org.lineageos.settings.ui.ExpressiveFooter
import org.lineageos.settings.ui.ExpressiveGroup
import org.lineageos.settings.ui.ExpressiveRow
import org.lineageos.settings.ui.ExpressiveScaffold
import org.lineageos.settings.ui.ExpressiveScreen
import org.lineageos.settings.ui.ExpressiveSliderRow
import org.lineageos.settings.ui.ExpressiveTheme

class HapticLevelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpressiveTheme {
                HapticLevelScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
private fun HapticLevelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val supported = remember { HapticUtils.isSupported() }
    var level by remember { mutableIntStateOf(HapticUtils.getLevel(context)) }

    ExpressiveScaffold(
        title = stringResource(R.string.haptic_title),
        onBack = onBack,
    ) { padding ->
        ExpressiveScreen(modifier = Modifier.padding(padding)) {
            ExpressiveGroup(title = "") {
                item {
                    if (supported) {
                        ExpressiveSliderRow(
                            title = stringResource(R.string.haptic_level_title),
                            value = level,
                            valueRange = 0..HapticUtils.MAX_PERCENT,
                            valueLabel = { "$it%" },
                            onValueChange = {
                                level = it
                                // Short tick so the new strength can be felt while dragging.
                                HapticUtils.applyLevel(context, it, true)
                            },
                            onValueChangeFinished = {
                                HapticUtils.saveLevel(context, it)
                                vibrateOnce(context)
                            },
                        )
                    } else {
                        ExpressiveRow(
                            title = stringResource(R.string.haptic_level_title),
                            summary = stringResource(R.string.haptic_level_summary_incompatible),
                            enabled = false,
                        )
                    }
                }
            }
            ExpressiveFooter(stringResource(R.string.haptic_warning))
        }
    }
}

private fun vibrateOnce(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (vibrator.hasVibrator()) {
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
