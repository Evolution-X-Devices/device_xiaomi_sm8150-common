/*
 * SPDX-FileCopyrightText: 2026 ergdevops
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.bypass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.lineageos.settings.R
import org.lineageos.settings.ui.ExpressiveFooter
import org.lineageos.settings.ui.ExpressiveGroup
import org.lineageos.settings.ui.ExpressiveScaffold
import org.lineageos.settings.ui.ExpressiveScreen
import org.lineageos.settings.ui.ExpressiveSwitchRow
import org.lineageos.settings.ui.ExpressiveTheme

class BypassChargingSettingsActivity : ComponentActivity() {

    private var blocked by mutableStateOf(false)
    private var enabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refresh()
        setContent {
            ExpressiveTheme {
                ExpressiveScaffold(
                    title = stringResource(R.string.bypass_charging_title),
                    onBack = { finish() },
                ) { padding ->
                    ExpressiveScreen(modifier = Modifier.padding(padding)) {
                        ExpressiveGroup(title = "") {
                            item {
                                ExpressiveSwitchRow(
                                    titleText = stringResource(R.string.bypass_charging_title),
                                    summaryText = stringResource(
                                        if (blocked) R.string.bypass_charging_blocked
                                        else R.string.bypass_charging_summary
                                    ),
                                    isChecked = enabled,
                                    isEnabled = !blocked,
                                    onChange = {
                                        BypassChargingUtils.setEnabled(this@BypassChargingSettingsActivity, it)
                                        enabled = it
                                    },
                                )
                            }
                        }
                        ExpressiveFooter(stringResource(R.string.bypass_charging_info))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // The quick settings tile and Charging control can change the state while we're away.
        refresh()
    }

    private fun refresh() {
        blocked = BypassChargingUtils.isChargingControlEnabled(this)
        enabled = !blocked && BypassChargingUtils.isEnabled(this)
    }
}
