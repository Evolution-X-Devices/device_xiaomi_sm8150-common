package org.lineageos.settings.display;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import vendor.lineage.livedisplay.IAntiFlicker;

public class DcDimmingTileService extends TileService {

    private static final String TAG = "DcDimmingTileService";

    private static IAntiFlicker getAntiFlickerService() {
        return IAntiFlicker.Stub.asInterface(
                ServiceManager.checkService(
                        IAntiFlicker.DESCRIPTOR + "/default"));
    }

    private void updateUI(boolean available, boolean enabled) {
        final Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        if (!available) {
            tile.setState(Tile.STATE_UNAVAILABLE);
        } else {
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        try {
            IAntiFlicker service = getAntiFlickerService();
            if (service == null) {
                Log.e(TAG, "Anti-flicker service not available");
                updateUI(false, false);
                return;
            }

            updateUI(true, service.getEnabled());
        } catch (RemoteException | RuntimeException e) {
            Log.e(TAG, "Failed to read DC dimming state", e);
            updateUI(false, false);
        }
    }

    @Override
    public void onClick() {
        super.onClick();

        try {
            IAntiFlicker service = getAntiFlickerService();
            if (service == null) {
                Log.e(TAG, "Anti-flicker service not available");
                updateUI(false, false);
                return;
            }

            final boolean enabled = !service.getEnabled();
            service.setEnabled(enabled);
            updateUI(true, enabled);
        } catch (RemoteException | RuntimeException e) {
            Log.e(TAG, "Failed to toggle DC dimming", e);
            updateUI(false, false);
        }
    }
}
