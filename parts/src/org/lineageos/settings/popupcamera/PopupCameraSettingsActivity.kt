/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.popupcamera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import org.lineageos.settings.R
import org.lineageos.settings.ui.ExpressiveChoiceDialog
import org.lineageos.settings.ui.ExpressiveGroup
import org.lineageos.settings.ui.ExpressiveIcon
import org.lineageos.settings.ui.ExpressiveRow
import org.lineageos.settings.ui.ExpressiveScaffold
import org.lineageos.settings.ui.ExpressiveScreen
import org.lineageos.settings.ui.ExpressiveSwitchRow
import org.lineageos.settings.ui.ExpressiveTheme

class PopupCameraSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpressiveTheme {
                PopupCameraScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
private fun PopupCameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PopupCameraPreferences(context) }
    var led by remember { mutableStateOf(prefs.isLedAllowed) }
    var sound by remember { mutableStateOf(prefs.soundEffect) }
    var picking by remember { mutableStateOf(false) }

    val entries = stringArrayResource(R.array.popupcamera_effects_entries).toList()
    val values = stringArrayResource(R.array.popupcamera_effects_values).toList()
    val selected = values.indexOf(sound).coerceAtLeast(0)

    ExpressiveScaffold(
        title = stringResource(R.string.popup_title),
        onBack = onBack,
    ) { padding ->
        ExpressiveScreen(modifier = Modifier.padding(padding)) {
            ExpressiveGroup(title = "") {
                item {
                    ExpressiveSwitchRow(
                        titleText = stringResource(R.string.popup_led_title),
                        summaryText = stringResource(R.string.popup_led_summary),
                        isChecked = led,
                        leading = { ExpressiveIcon(R.drawable.ic_popup_light) },
                        onChange = {
                            led = it
                            prefs.setLedAllowed(it)
                        },
                    )
                }
                item {
                    ExpressiveRow(
                        title = stringResource(R.string.popup_sound_title),
                        summary = entries[selected],
                        leading = { ExpressiveIcon(R.drawable.ic_popup_sound) },
                        onClick = { picking = true },
                    )
                }
            }
        }
    }

    if (picking) {
        ExpressiveChoiceDialog(
            title = stringResource(R.string.popup_sound_title),
            entries = entries,
            selectedIndex = selected,
            onSelect = {
                sound = values[it]
                prefs.setSoundEffect(values[it])
            },
            onDismiss = { picking = false },
        )
    }
}
